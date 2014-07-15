package info.thez.csfdplugin;

import java.util.*;

public class CSFDplugin_countryCodesDuplicates {

    public static void main(String[] args) {

        Map<String, String> cc = CSFDplugin.countryCodes;
        Collection<String> values = cc.values();

        ArrayList<String> codes = new ArrayList<String>();
        ArrayList<String> duplicates = new ArrayList<String>();

        // find duplicates
        for(String v : values) {
            if(codes.contains(v) && !duplicates.contains(v)) {
                duplicates.add(v);
            }

            codes.add(v);
        }

        // print duplicates with keys
        for(String v : duplicates) {
            for(Map.Entry<String, String> entry : cc.entrySet()) {
                if(entry.getValue().equals(v)) {
                    System.out.println(v + " - " + entry.getKey());
                }
            }
        }

    }
}