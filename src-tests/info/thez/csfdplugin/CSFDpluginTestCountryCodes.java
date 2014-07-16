package info.thez.csfdplugin;

import com.moviejukebox.tools.WebBrowser;
import org.junit.Assert;
import org.junit.Test;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSFDpluginTestCountryCodes {

    /**
     * Check if we have codes for all countries against list from CSFD
     */
    @Test
    public void testCountryCodes() throws Exception {
        String page = new WebBrowser().request("http://www.csfd.cz/podrobne-vyhledavani/");

        Pattern pattern = Pattern.compile("<select [^>]*? name=\"origin\\[include\\]\\[\\]\">(.+?)</select>");
        Pattern pattern2 = Pattern.compile("<option value=\"\\d+\">([^<]+)</option>");
        Matcher matcher = pattern.matcher(page);

        Assert.assertEquals(true, matcher.find()); // jinak se zmenila stranka csfd

        String options = matcher.group(1);
        matcher = pattern2.matcher(options);

        boolean zeroMissing = true;
        System.out.println();

        while(matcher.find()) {
            String country = matcher.group(1);

            if(CSFDplugin.countryCodes.containsKey(country.toLowerCase())) continue;

            System.out.println("Missing country: " + country);
            zeroMissing = false;
        }

        Assert.assertTrue(zeroMissing);
    }
}