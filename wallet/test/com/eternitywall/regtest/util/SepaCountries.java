package com.eternitywall.regtest.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Riccardo Casatta @RCasatta on 19/10/17.
 */

public class SepaCountries {

/*
The 28 member states of the European Union, including
the 19 states that are in the Eurozone:
Austria, Belgium, Cyprus, Estonia, Finland, France, Germany, Greece, Ireland, Italy, Latvia, Lithuania, Luxembourg, Malta, Netherlands, Portugal, Slovakia, Slovenia, Spain
the nine states that are not in the Eurozone:
Bulgaria, Croatia, the Czech Republic, Denmark, Hungary, Poland, Romania, Sweden, United Kingdom.
The three states having signed the European Economic Area agreement:
Iceland, Liechtenstein, Norway.
A few other countries with agreements with the EU:
Monaco, San Marino, Switzerland.
*/

    private static final Set<String> sepaCountries = new HashSet<>(Arrays.asList("Austria", "Belgium", "Cyprus", "Estonia", "Finland", "France", "Germany", "Greece", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Malta", "Netherlands", "Portugal", "Slovakia", "Slovenia", "Spain", "Bulgaria", "Croatia", "Czech Republic", "Denmark", "Hungary", "Poland", "Romania", "Sweden", "United Kingdom", "Iceland", "Liechtenstein", "Norway", "Monaco", "San Marino", "Switzerland"));


    @Test
    public void init() {
        Locale locale = new Locale("en", "GB");

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        Set<String> ccSet = util.getSupportedRegions();

        for (String regionCode : ccSet) {
            Integer countryCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(regionCode);
            String regionName = getRegionDisplayName(regionCode, locale);
            System.out.println(regionCode + " " + countryCode + " " + regionName + " " + sepaCountries.contains(regionName));

        }



    }

    /** Returns the localized region name for the given region code. */
    public String getRegionDisplayName(String regionCode, Locale language) {
        return (regionCode == null || regionCode.equals("ZZ") ||
                regionCode.equals(PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY))
                ? "" : new Locale("", regionCode).getDisplayCountry(language);
    }
}
