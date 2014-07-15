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
        pulpFiction.setTitle("Forrest Gump", "test");
        pulpFiction.setYear("1994", "test");

        PropertiesUtil.setProperty("fanart.movie.download", Boolean.TRUE);
        PropertiesUtil.setProperty("csfd.fanart", Boolean.TRUE);

        csfd = new CSFDplugin();
    }

    @Test
    public void testGetFanartURL() throws Exception {
        Assert.assertEquals("http://img.csfd.cz/files/images/film/photos/000/146/146448_08a693.jpg", csfd.getFanartURL(pulpFiction));
    }
}