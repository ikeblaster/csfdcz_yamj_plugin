/*
 *      Copyright (c) 2014 Ike Blaster
 *
 *      This file is source code of a plugin for Yet Another Movie Jukebox (YAMJ*).
 *       * http://code.google.com/p/moviejukebox/
 *
 */
package info.thez.csfdplugin;

import com.moviejukebox.model.Movie;
import com.moviejukebox.model.Person;
import com.moviejukebox.model.enumerations.OverrideFlag;
import com.moviejukebox.plugin.ImdbPlugin;
import com.moviejukebox.plugin.TheTvDBPlugin;
import com.moviejukebox.tools.PropertiesUtil;
import com.moviejukebox.tools.StringTools;
import com.moviejukebox.tools.SystemTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.net.URLEncoder;


/**
 * Plugin to retrieve movie data from Czech movie database www.csfd.cz.
 * <p/>
 * At first it gets data from IMDB (movies) or TvDB (series).<br>
 * Then it updates plot, rating and other data from CSFD via unofficial public <a href='http://csfdapi.cz'>CSFD API</a>.
 * <p/>
 * <b>NOTE:</b> the API contains limitations, for more details please see <a href='http://csfdapi.cz'>here</a>.
 */
public class CSFDplugin extends ImdbPlugin {

    private static final Logger LOG = Logger.getLogger(CSFDplugin.class);
    private static final String LOG_MESSAGE = "CSFDPlugin: ";

    public static final String CSFD_PLUGIN_ID = "csfd";
    private static final String ENGLISH = "english";
    private TheTvDBPlugin tvdb;

    // Shows what name is on the first position with respect to divider
    private String titleLeader = PropertiesUtil.getProperty("csfd.title.leader", ENGLISH);
    private String titleDivider = PropertiesUtil.getProperty("csfd.title.divider", " - ");

    // Get scraping options
    private boolean poster = PropertiesUtil.getBooleanProperty("csfd.poster", Boolean.FALSE);
    private boolean rating = PropertiesUtil.getBooleanProperty("csfd.rating", Boolean.TRUE);
    private boolean actors = PropertiesUtil.getBooleanProperty("csfd.actors", Boolean.FALSE);
    private boolean directors = PropertiesUtil.getBooleanProperty("csfd.directors", Boolean.FALSE);
    private boolean writers = PropertiesUtil.getBooleanProperty("csfd.writers", Boolean.FALSE);
    private boolean countryAll = PropertiesUtil.getProperty("csfd.country", "first").equalsIgnoreCase("all");

    public CSFDplugin() {
        super();
        this.preferredCountry = PropertiesUtil.getProperty("imdb.preferredCountry", "Czech Republic");
        this.actorMax = PropertiesUtil.getReplacedIntProperty("movie.actor.maxCount", "plugin.people.maxCount.actor", 10);
        this.directorMax = PropertiesUtil.getReplacedIntProperty("movie.director.maxCount", "plugin.people.maxCount.director", 2);
        this.writerMax = PropertiesUtil.getReplacedIntProperty("movie.writer.maxCount", "plugin.people.maxCount.writer", 3);
        this.tvdb = new TheTvDBPlugin();
    }

    @Override
    public String getPluginID() {
        return CSFD_PLUGIN_ID;
    }


    /**
     * Get info for movie
     */
    @Override
    public boolean scan(Movie movie) {
        boolean retval = false;
        String csfdId = movie.getId(CSFD_PLUGIN_ID);


        if(StringTools.isNotValidString(csfdId)) {
            // store original russian title and year
            String name = movie.getOriginalTitle();
            String year = movie.getYear();

            final String previousTitle = movie.getTitle();
            int dash = previousTitle.indexOf(this.titleDivider);
            if(dash != -1) {
                if(this.titleLeader.equals(ENGLISH)) {
                    movie.setTitle(previousTitle.substring(0, dash), movie.getOverrideSource(OverrideFlag.TITLE));
                } else {
                    movie.setTitle(previousTitle.substring(dash), movie.getOverrideSource(OverrideFlag.TITLE));
                }
            }
            // Get base info from imdb or tvdb
            if(!movie.isTVShow()) {
                super.scan(movie);
            } else {
                this.tvdb.scan(movie);
            }


            // Let's replace dash (-) by space ( ) in Title.
            //name.replace(titleDivider, " ");
            dash = name.indexOf(this.titleDivider);
            if(dash != -1) {
                if(this.titleLeader.equals(ENGLISH)) {
                    name = name.substring(0, dash);
                } else {
                    name = name.substring(dash);
                }
            }

            // search movie ID on csfd with year
            csfdId = this.getCsfdId(name, year);

            // in case of empty results, try it without the year
            if(StringTools.isNotValidString(csfdId)) {
                csfdId = this.getCsfdId(name, Movie.UNKNOWN);
            }

            // set found ID
            movie.setId(CSFD_PLUGIN_ID, csfdId);
        }

        // we have some CSFD ID
        if(StringTools.isValidString(csfdId)) {
            // get data from CSFD
            retval = this.updateCsfdMediaInfo(movie, csfdId);
        }

        return retval;
    }



    /**
     * Retrieve CSFD matching the specified movie name and year.
     */
    public String getCsfdId(String movieName, String year) {
        try {
            String url = "http://csfdapi.cz/movie?search=";
            url += URLEncoder.encode(movieName, "UTF-8");

            if(StringTools.isValidString(year)) {
                String[] years = year.split("-");
                url += "+" + years[0];
            }

            String json = this.webBrowser.request(url);
            JSONArray data = (JSONArray) JSONValue.parse(json);

            // empty results
            if(data.size() == 0) return Movie.UNKNOWN;

            JSONObject firstMovie = (JSONObject) data.get(0);


            return firstMovie.get("id").toString(); // return ID of first movie in results

        } catch(Exception error) {
            LOG.error(LOG_MESSAGE + "Failed retreiving CSFD Id for movie : " + movieName);
            LOG.error(LOG_MESSAGE + "Error : " + error.getMessage());
            return Movie.UNKNOWN;
        }
    }



    /**
     * Get CSFD info for the specified movie
     */
    private boolean updateCsfdMediaInfo(Movie movie, String csfdId) {
        try {
            String json = this.webBrowser.request("http://csfdapi.cz/movie/" + csfdId);
            JSONObject data = (JSONObject) JSONValue.parse(json);




            // region NAMES
            JSONObject names = (JSONObject) data.get("names");

            if(names.containsKey("cs")) {
                movie.setTitle(names.get("cs").toString(), CSFD_PLUGIN_ID);
            }

            /*
            if(names.containsKey("en")) {
                movie.setOriginalTitle(names.get("en").toString(), movie.getOverrideSource(OverrideFlag.ORIGINALTITLE));
            }
            if(names.containsKey("originální")) {
                movie.setOriginalTitle(names.get("originální").toString(), movie.getOverrideSource(OverrideFlag.ORIGINALTITLE));
            }
            */
            // endregion



            // region YEAR
            if(data.containsKey("year")) {
                movie.setYear(data.get("year").toString(), CSFD_PLUGIN_ID);
            }
            // endregion



            // region GENRES
            if(data.containsKey("genres")) {
                JSONArray genres = (JSONArray) data.get("genres");
                movie.setGenres(genres, CSFD_PLUGIN_ID);
            }
            // endregion



            // region COUNTRIES
            if(data.containsKey("countries")) {
                JSONArray countries = (JSONArray) data.get("countries");
                String strCountry = this.countryAll ? StringUtils.join(countries, Movie.SPACE_SLASH_SPACE) : countries.get(0).toString(); // join all or get first country

                movie.setCountry(strCountry, CSFD_PLUGIN_ID);
            }
            // endregion



            // region RATING
            if(this.rating && data.containsKey("rating")) {
                movie.addRating(CSFD_PLUGIN_ID, Integer.valueOf(data.get("rating").toString()));
            }
            // endregion



            // region PLOT
            if(data.containsKey("plot")) {
                movie.setPlot(data.get("plot").toString(), CSFD_PLUGIN_ID);
            }
            // endregion



            // region CONTENT RATING
            if(data.containsKey("content_rating")) {
                movie.setCertification(data.get("content_rating").toString(), CSFD_PLUGIN_ID);
            }
            // endregion



            // region POSTER URL
            if(this.poster && data.containsKey("poster_url")) {
                String posterUrl = data.get("poster_url").toString();
                posterUrl = posterUrl.replace("?h180", "?h360");
                movie.setPosterURL(posterUrl);
            }
            // endregion



            JSONObject authors = (JSONObject) data.get("authors");


            // region DIRECTOR
            if(this.directors && authors.containsKey("directors")) {
                JSONArray people = (JSONArray) authors.get("directors");

                for(Object man : people) {
                    JSONObject obj = (JSONObject) man;
                    movie.addDirector(obj.get("name").toString(), CSFD_PLUGIN_ID);
                }
            }
            // endregion


            // region WRITERS / SCRIPT
            if(this.writers && authors.containsKey("script")) {
                JSONArray people = (JSONArray) authors.get("script");

                for(Object man : people) {
                    JSONObject obj = (JSONObject) man;
                    movie.addWriter(obj.get("name").toString(), CSFD_PLUGIN_ID);
                }
            }
            // endregion


            // region ACTORS
            if(this.actors && authors.containsKey("actors")) {
                JSONArray people = (JSONArray) authors.get("actors");

                for(Object man : people) {
                    JSONObject obj = (JSONObject) man;
                    movie.addActor(obj.get("name").toString(), CSFD_PLUGIN_ID);
                }
            }
            // endregion


        } catch(Exception error) {
            LOG.error(LOG_MESSAGE + "Failed retreiving movie data from CSFD : " + csfdId);
            LOG.error(SystemTools.getStackTrace(error));

            return false;
        }
        return true;
    }



    @Override
    public boolean scan(Person person) {
        return super.scan(person);
    }

}