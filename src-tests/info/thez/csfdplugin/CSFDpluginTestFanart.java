package info.thez.csfdplugin;

import com.moviejukebox.model.Movie;
import com.moviejukebox.tools.PropertiesUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CSFDpluginTestFanart {

    static CSFDplugin csfd;
    static Movie pulpFiction;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        pulpFiction = new Movie();
        pulpFiction.setTitle("22 Jump Street", "test");
        pulpFiction.setYear("2014", "test");

        PropertiesUtil.setProperty("fanart.movie.download", Boolean.TRUE);
        PropertiesUtil.setProperty("csfd.fanart", Boolean.TRUE);

        csfd = new CSFDplugin();
    }

    @Test
    public void testGetFanartURL() throws Exception {
        Assert.assertEquals("http://img.csfd.cz/files/images/film/photos/158/427/158427929_7923e5.jpg", csfd.getFanartURL(pulpFiction));
    }
}