package info.thez.csfdplugin;

import com.moviejukebox.model.Movie;
import com.moviejukebox.tools.PropertiesUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

public class CSFDpluginTest {

    static CSFDplugin csfd;
    static Movie pulpFiction;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        pulpFiction = new Movie();
        pulpFiction.setTitle("pulp fiction", "test");
        pulpFiction.setYear("1994", "test");

        PropertiesUtil.setProperty("csfd.poster", Boolean.TRUE);

        csfd = new CSFDplugin();
        csfd.scan(pulpFiction);
    }

    @Test
    public void testgetPluginID() throws Exception {
        Assert.assertEquals("csfd", csfd.getPluginID());
    }

    @org.junit.Test
    public void testScanId() throws Exception {
        Assert.assertEquals("8852", pulpFiction.getId(csfd.getPluginID()));
    }

    @org.junit.Test
    public void testScanCertification() throws Exception {
        Assert.assertEquals("Nevhodný mládeži do 15 let", pulpFiction.getCertification());
    }

    @org.junit.Test
    public void testScanCountry() throws Exception {
        Assert.assertEquals("USA", pulpFiction.getCountry());
    }

    @org.junit.Test
    public void testScanCast() throws Exception {
        Collection<String> list = new LinkedHashSet<String>();
        list.add("John Travolta");
        list.add("Samuel L. Jackson");
        list.add("Tim Roth");
        list.add("Uma Thurman");
        list.add("Christopher Walken");
        list.add("Bruce Willis");
        list.add("Harvey Keitel");
        list.add("Amanda Plummer");
        list.add("Rosanna Arquette");
        list.add("Eric Stoltz");

        Assert.assertEquals(list, pulpFiction.getCast());
    }

    @org.junit.Test
    public void testScanDirectors() throws Exception {
        Collection<String> list = new LinkedHashSet<String>();
        list.add("Quentin Tarantino");

        Assert.assertEquals(list, pulpFiction.getDirectors());
    }

    @org.junit.Test
    public void testScanGenres() throws Exception {
        Collection<String> list = new TreeSet<String>();
        list.add("Krimi");
        list.add("Thriller");

        Assert.assertEquals(list, pulpFiction.getGenres());
    }

    @org.junit.Test
    public void testScanOriginalTitle() throws Exception {
        Assert.assertEquals("Pulp Fiction", pulpFiction.getOriginalTitle());
    }

    @org.junit.Test
    public void testScanPlot() throws Exception {
        Assert.assertEquals("Vincent Vega (John Travolta) a Jules Winnfield (Samuel L. Jackson) sú dvaja nerozluční treťotriedni gangstri v službách obávaného Marsellusa Wallace (Ving Rhames). Pred dokončením jedného \"jobu\" prezradí Vincent Julesovi, že Pán Veľký (teda Marsellus) ho požiadal, aby sa postaral o jeho sexy manželku Miu (Uma Thurman), kým bude na Floride. Vincent to považuje za skúšku lojality, v ktorej musí za každú cenu obstáť.Butch Coolidge (Bruce Willis) je boxér, ktorého kariéra sa chýli ku koncu....", this.pulpFiction.getPlot());
    }

    @org.junit.Test
    public void testScanRatings() throws Exception {
        Assert.assertEquals(91, pulpFiction.getRating());
    }

    @org.junit.Test
    public void testScanRuntime() throws Exception {
        Assert.assertEquals("154 min", pulpFiction.getRuntime());
    }

    @org.junit.Test
    public void testScanTitle() throws Exception {
        Assert.assertEquals("Pulp Fiction: Historky z podsvětí", pulpFiction.getTitle());
    }

    @org.junit.Test
    public void testScanWriters() throws Exception {
        Collection<String> list = new LinkedHashSet<String>();
        list.add("Quentin Tarantino");
        list.add("Roger Avary");

        Assert.assertEquals(list, pulpFiction.getWriters());
    }

    @org.junit.Test
    public void testScanYear() throws Exception {
        Assert.assertEquals("1994", pulpFiction.getYear());
    }

    @org.junit.Test
    public void testScanPosterURL() throws Exception {
        Assert.assertEquals("http://img.csfd.cz/files/images/film/posters/000/008/8547_5e55da.jpg", pulpFiction.getPosterURL());
    }


}