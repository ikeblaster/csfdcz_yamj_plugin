package info.thez.csfdplugin;

import com.moviejukebox.model.Movie;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CSFDpluginTestGetSearchID {

    static CSFDplugin csfd;
    static Movie pulpFiction;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        pulpFiction = new Movie();
        pulpFiction.setTitle("pulp fiction", "test");
        pulpFiction.setYear("1994", "test");

        csfd = new CSFDplugin();
    }

    @Test
    public void testGetCsfdId() throws Exception {
        pulpFiction.setId(CSFDplugin.CSFD_PLUGIN_ID, "1234");
        Assert.assertEquals("1234", csfd.getCsfdId(pulpFiction));
    }

    @Test
    public void testSearchCsfdId() throws Exception {
        Assert.assertEquals("8852", csfd.searchCsfdId(pulpFiction.getTitle(), pulpFiction.getYear()));
    }
}