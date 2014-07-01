/*
 *      Copyright (c) 2014 Ike Blaster
 *
 *      This file is source code of a plugin for Yet Another Movie Jukebox (YAMJ*).
 *       * http://code.google.com/p/moviejukebox/
 *
 */
package info.thez.csfdplugin;

import com.moviejukebox.model.Movie;
import com.moviejukebox.plugin.ImdbPlugin;
import com.moviejukebox.plugin.TheTvDBPlugin;
import com.moviejukebox.tools.PropertiesUtil;
import com.moviejukebox.tools.StringTools;
import com.moviejukebox.tools.SystemTools;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Plugin to retrieve movie data from Czech movie database www.csfd.cz.
 * <p/>
 * At first it gets data from IMDB (movies) or TvDB (series).<br>
 * Then it updates plot, rating and other data from CSFD via unofficial public <a href='http://csfdapi.cz'>CSFD API</a>.
 * <p/>
 * <b>NOTE:</b> the API contains limitations, for more details please see <a href='http://csfdapi.cz'>here</a>.
 */
public class CSFDplugin extends ImdbPlugin {

    public static final String CSFD_PLUGIN_ID = "csfd";
    private static final Logger LOG = Logger.getLogger(CSFDplugin.class);
    private static final String LOG_MESSAGE = "CSFDPlugin: ";
    private TheTvDBPlugin tvdb;

    // Get scraping options
    private boolean fanart = PropertiesUtil.getBooleanProperty("csfd.fanart", Boolean.FALSE);
    private boolean poster = PropertiesUtil.getBooleanProperty("csfd.poster", Boolean.FALSE);
    private boolean rating = PropertiesUtil.getBooleanProperty("csfd.rating", Boolean.TRUE);
    private boolean actors = PropertiesUtil.getBooleanProperty("csfd.actors", Boolean.TRUE);
    private boolean directors = PropertiesUtil.getBooleanProperty("csfd.directors", Boolean.TRUE);
    private boolean writers = PropertiesUtil.getBooleanProperty("csfd.writers", Boolean.TRUE);
    private boolean countryAll = PropertiesUtil.getProperty("csfd.countries", "all").equalsIgnoreCase("all");

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
        String csfdId = this.getCsfdId(movie);

        // Get base info from imdb or tvdb
        if(StringTools.isNotValidString(movie.getId(IMDB_PLUGIN_ID))) {
            if(!movie.isTVShow()) {
                super.scan(movie);
            } else {
                this.tvdb.scan(movie);
            }
        }


        // we have some CSFD ID
        if(StringTools.isValidString(csfdId)) {
            // get data from CSFD
            retval = this.updateCsfdMediaInfo(movie, csfdId);
        }

        return retval;
    }


    /**
     * Gets or find CSFD id matching the specified movie name. Tries
     */
    public String getCsfdId(Movie movie) {
        String csfdId = movie.getId(CSFD_PLUGIN_ID);

        if(StringTools.isNotValidString(csfdId)) {
            // store original russian title and year
            String name = movie.getOriginalTitle();
            String year = movie.getYear();

            // search movie ID on csfd with year
            csfdId = this.searchCsfdId(name, year);

            // in case of empty results, try it without the year
            if(StringTools.isNotValidString(csfdId)) {
                csfdId = this.searchCsfdId(name, Movie.UNKNOWN);
            }

            // set found ID
            movie.setId(CSFD_PLUGIN_ID, csfdId);
        }

        return csfdId;
    }


    /**
     * Retrieve CSFD id matching the specified movie name and year.
     */
    public String searchCsfdId(String movieName, String year) {
        try {
            String url = "http://csfdapi.cz/movie?search=";
            url += URLEncoder.encode(movieName, "UTF-8");

            if(StringTools.isValidString(year)) {
                String[] years = year.split("-");
                url += "+(" + years[0] + ")";
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
        }

        return null;
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
                StringBuilder sbCountry = new StringBuilder("");

                for(int i = 0; i < countries.size(); i++) {
                    if(i > 0) {
                        if(!this.countryAll) break;
                        sbCountry.append(Movie.SPACE_SLASH_SPACE);
                    }

                    sbCountry.append(this.getCountryCode(countries.get(i).toString()));
                }


                movie.setCountry(sbCountry.toString(), CSFD_PLUGIN_ID);
            }
            // endregion


            // region RATING
            if(this.rating && data.containsKey("rating")) {
                Map<String, Integer> ratings = new HashMap<String, Integer>();
                ratings.put(CSFD_PLUGIN_ID, Integer.valueOf(data.get("rating").toString()));
                movie.setRatings(ratings);
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
                posterUrl = posterUrl.replace("?h180", "");
                movie.setPosterURL(posterUrl);
            }
            // endregion



            JSONObject authors = (JSONObject) data.get("authors");
            int cnt;

            // region DIRECTOR
            if(this.directors && authors.containsKey("directors")) {
                JSONArray people = (JSONArray) authors.get("directors");

                movie.clearDirectors();
                cnt = 0;

                for(Object man : people) {
                    JSONObject obj = (JSONObject) man;
                    movie.addDirector(obj.get("name").toString(), CSFD_PLUGIN_ID);

                    if(cnt++ > this.directorMax) break;
                }
            }
            // endregion


            // region WRITERS / SCRIPT
            if(this.writers && authors.containsKey("script")) {
                JSONArray people = (JSONArray) authors.get("script");

                movie.clearWriters();
                cnt = 0;

                for(Object man : people) {
                    JSONObject obj = (JSONObject) man;
                    movie.addWriter(obj.get("name").toString(), CSFD_PLUGIN_ID);

                    if(cnt++ > this.writerMax) break;
                }
            }
            // endregion


            // region ACTORS
            if(this.actors && authors.containsKey("actors")) {
                JSONArray people = (JSONArray) authors.get("actors");

                movie.clearCast();
                cnt = 0;

                for(Object man : people) {
                    JSONObject obj = (JSONObject) man;
                    movie.addActor(obj.get("name").toString(), CSFD_PLUGIN_ID);

                    if(cnt++ > this.actorMax) break;
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



    /**
     * Get the fanart for the movie from the CSFD
     */
    @Override
    protected String getFanartURL(Movie movie) {
        String csfdId = this.getCsfdId(movie);


        if(!this.fanart || StringTools.isNotValidString(csfdId)) {
            return super.getFanartURL(movie);
        }

        try {
            String page = this.webBrowser.request("http://www.csfd.cz/film/" + csfdId + "/galerie/");

            Pattern pattern = Pattern.compile("<div class=\"photo\"[^']+'([^\\?]+)\\?");
            Matcher matcher = pattern.matcher(page);

            if(matcher.find()) {
                return "http:" + matcher.group(1);
            }


        } catch(IOException error) {
            LOG.error(LOG_MESSAGE + "Failed retreiving CSFD fanart for movie : " + movie.getTitle());
            LOG.error(LOG_MESSAGE + "Error : " + error.getMessage());
        }

        return Movie.UNKNOWN;
    }




    /**
     * Country codes map
     */
    public static final Map<String, String> countryCodes = new HashMap<String, String>() {{
        this.put("afgánistán", "AF");
        this.put("albánie", "AL");
        this.put("alžírsko", "DZ");
        this.put("argentina", "AR");
        this.put("arménie", "AM");
        this.put("austrálie", "AU");
        this.put("bahamy", "BS");
        this.put("bangladéš", "BD");
        this.put("barma", "MM");
        this.put("belgie", "BE");
        this.put("bhután", "BT");
        this.put("bolívie", "BO");
        this.put("bosna a hercegovina", "BA");
        this.put("brazílie", "BR");
        this.put("bulharsko", "BG");
        this.put("burkina faso", "BF");
        this.put("bělorusko", "BY");
        this.put("chile", "CL");
        this.put("chorvatsko", "HR");
        this.put("cz název", "CZ");
        this.put("dominik. republika", "DO");
        this.put("dánsko", "DK");
        this.put("egypt", "EG");
        this.put("ekvádor", "EC");
        this.put("en název", "US");
        this.put("estonsko", "EE");
        this.put("etiopie", "ET");
        this.put("fed. rep. jugoslávie", "HR"); // historical
        this.put("filipíny", "PH");
        this.put("finsko", "FI");
        this.put("francie", "FR");
        this.put("gruzie", "GE");
        this.put("guatemala", "GT");
        this.put("honduras", "HN");
        this.put("hong kong", "HK");
        this.put("indie", "IN");
        this.put("indonésie", "ID");
        this.put("irsko", "IE");
        this.put("irák", "IQ");
        this.put("irán", "IR");
        this.put("island", "IS");
        this.put("itálie", "IT");
        this.put("izrael", "IL");
        this.put("jamajka", "JM");
        this.put("japonsko", "JP");
        this.put("jižní afrika", "ZA");
        this.put("jižní korea", "KR");
        this.put("jordánsko", "JO");
        this.put("jugoslávie", "HR"); // historical
        this.put("kambodža", "KH");
        this.put("kamerun", "CM");
        this.put("kanada", "CA");
        this.put("katar", "QA");
        this.put("kazachstán", "KZ");
        this.put("keňa", "KE");
        this.put("kolumbie", "CO");
        this.put("kongo", "CG");
        this.put("korea", "KR"); // ambiguous
        this.put("kosovo", "XK"); // temporary
        this.put("kostarika", "CR");
        this.put("kuba", "CU");
        this.put("kuvajt", "KW");
        this.put("kyrgyzstán", "KG");
        this.put("libanon", "LB");
        this.put("lichtenštejnsko", "LI");
        this.put("litva", "LT");
        this.put("lotyšsko", "LV");
        this.put("makedonie", "MK");
        this.put("malajsie", "MY");
        this.put("mali", "ML");
        this.put("malta", "MT");
        this.put("maroko", "MA");
        this.put("mauretánie", "MR");
        this.put("maďarsko", "HU");
        this.put("mexiko", "MX");
        this.put("moldavsko", "MD");
        this.put("monako", "MC");
        this.put("mongolsko", "MN");
        this.put("mosambik", "MZ");
        this.put("nigérie", "NE");
        this.put("nikaragua", "NI");
        this.put("nizozemsko", "NL");
        this.put("norsko", "NO");
        this.put("nový zéland", "NZ");
        this.put("německo", "DE");
        this.put("palestina", "PS");
        this.put("panama", "PA");
        this.put("paraguay", "PY");
        this.put("peru", "PE");
        this.put("pobřeží slonoviny", "CI");
        this.put("polsko", "PL");
        this.put("portoriko", "PR");
        this.put("portugalsko", "PT");
        this.put("pákistán", "PK");
        this.put("rakousko", "AT");
        this.put("rakousko-uhersko", "AT"); // historical
        this.put("rumunsko", "RO");
        this.put("rusko", "RU");
        this.put("rwanda", "RW");
        this.put("saudská arábie", "SA");
        this.put("senegal", "SN");
        this.put("severní korea", "KP");
        this.put("singapur", "SG");
        this.put("sk název", "SK");
        this.put("slovensko", "SK");
        this.put("slovinsko", "SI");
        this.put("sovětský svaz", "SU");
        this.put("spojené arabské emiráty", "AE");
        this.put("srbsko a černá hora", "ME");
        this.put("srbsko", "RS");
        this.put("srí lanka", "LK");
        this.put("sýrie", "SY");
        this.put("tanzánie", "TZ");
        this.put("tchaj-wan", "TW");
        this.put("thajsko", "TH");
        this.put("tibet", "TI"); // suggested code, not in ISO
        this.put("tunisko", "TN");
        this.put("turecko", "TR");
        this.put("turkmenistan", "TM");
        this.put("ukrajina", "UA");
        this.put("uruguay", "UY");
        this.put("usa", "USA"); // US in ISO
        this.put("uzbekistán", "UZ");
        this.put("vatikán", "VA");
        this.put("velká británie", "GB");
        this.put("venezuela", "VE");
        this.put("vietnam", "VN");
        this.put("východní německo", "DD");
        this.put("zimbabwe", "ZW");
        this.put("západní německo", "DE");
        this.put("ázerbajdžán", "AZ");
        this.put("írák", "IQ");
        this.put("írán", "IR");
        this.put("čad", "TD");
        this.put("česko", "CZ");
        this.put("čína", "CN");
        this.put("řecko", "GR");
        this.put("španělsko", "ES");
        this.put("švédsko", "SE");
        this.put("švýcarsko", "CH");
    }};

    /**
     * Get country code for country name
     */
    public String getCountryCode(String country) {
        String key = country.toLowerCase();

        if(countryCodes.containsKey(key)) return countryCodes.get(key);
        return country;
    }


}