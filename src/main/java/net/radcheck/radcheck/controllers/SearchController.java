package net.radcheck.radcheck.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.radcheck.radcheck.models.*;
import net.radcheck.radcheck.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.net.ssl.HttpsURLConnection;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@org.springframework.stereotype.Controller
@RequestMapping("/")
public class SearchController {

    private static String BaseURL = "https://api.safecast.org/measurements.json";
    private static String AqiURL = "https://api.airvisual.com/v2/nearest_city";
    private static String geoURL = "https://maps.googleapis.com/maps/api/geocode/json";
    private String decodedString;
    private String json;
    private String aqiDecodedString;
    private String aqiJson;
    private String geoDecodedString;
    private String geoJson;
    private static String mapsKey = "AIzaSyDen0WZLZt-OQ68yU5D5uoNb7sr34mdycQ";
    private static String aqiKey = "3RDkWgP8CSpxMTGFM";
    private String geocode;
    private static Gson gson = new Gson();
    private static String minScTs = "2011-03-10T00:00:00Z";

    @Autowired
    private UserService userService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        String address = "The Gateway Arch, St. Louis, MO";
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Pick A Location");
        model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
        model.addAttribute("address", address);
        model.addAttribute("gmap", new GMap(address));

        return "index";
    }


    @RequestMapping(value = "", method = RequestMethod.POST)
    public String mapsSearch(Model model, @ModelAttribute @Valid GMap newMap, Errors
                             errors, Principal principal) throws IOException {
        if (errors.hasErrors()) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Pick A Location");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
            model.addAttribute("address", newMap.getAddress());
            return "index";
        }

        Geo geoReturn = getGeo(newMap.getAddress());

        if (geoReturn.getResults().size() == 0 || !geoReturn.getStatus().equals("OK")) {
            // add an alert here about the maps API failing
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Pick A Location");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
            model.addAttribute("address", newMap.getAddress());
            return "index";
        }

        double aLatitude = geoReturn.getResults().get(0).getGeometry().getMarker().getGeoLatitude();
        double aLongitude = geoReturn.getResults().get(0).getGeometry().getMarker().getGeoLongitude();

        Collection<Measurements> safeCastReturns = getMeasurements(aLatitude, aLongitude);

        AirQuality airVisualReturn = getAQI(aLatitude, aLongitude);

        Query queryObject = makeQuery(safeCastReturns, airVisualReturn);

        LatLon latLonObject = makeLatLon(aLatitude, aLongitude, queryObject);

        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Current readings at this location");
        model.addAttribute("return", safeCastReturns);
        model.addAttribute("latitude", aLatitude);
        model.addAttribute("longitude", aLongitude);
        model.addAttribute("key", mapsKey);
        model.addAttribute("aqi", airVisualReturn);
        return "result";
    }

    @RequestMapping(value = "/manual", method=RequestMethod.GET)
    public String manualSearch(Model model, Principal principal) {
        model.addAttribute("title", "Pick A Location");
        model.addAttribute("measurements", new Measurements());
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));

        return "manual-search";
    }

    @RequestMapping(value = "/manual", method = RequestMethod.POST)
    public String manualResult(Model model,
                         @ModelAttribute @Valid Measurements newMeasurement,
                         Errors errors, Principal principal) throws IOException {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Pick A Location");
            model.addAttribute("measurements", newMeasurement);
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            return "manual-search";
        }

        double aLatitude = newMeasurement.getRadLat();
        double aLongitude = newMeasurement.getRadLng();

        if (aLatitude > 90.0) {
            aLatitude = 90.0;
        }
        if (aLatitude < -90.0) {
            aLatitude = -90.0;
        }
        if (aLongitude > 180.0) {
            aLongitude = 180.0;
        }
        if (aLongitude < -180.0) {
            aLongitude = -180.0;
        }

        Collection<Measurements> safeCastReturns = getMeasurements(aLatitude, aLongitude);

        AirQuality airVisualReturn = getAQI(aLatitude, aLongitude);

        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Current readings at the given location");
        model.addAttribute("return", safeCastReturns);
        model.addAttribute("latitude", aLatitude);
        model.addAttribute("longitude", aLongitude);
        model.addAttribute("key", mapsKey);
        model.addAttribute("aqi", airVisualReturn);

        return "result";
    }

    public Collection<Measurements> getMeasurements(double lat, double lng) {
        json = "";
        Instant newInstant = Instant.now();
        String capturedBefore = newInstant.toString();
        Instant oneYearAgo = newInstant.minusSeconds(31557600);
        String capturedAfter = oneYearAgo.toString();
        try {
            json = callSafecast(capturedBefore, capturedAfter, lat, lng);
        } catch (Exception e) { e.printStackTrace(); }
        if (!json.equals("")) {
            Type collectionType = new TypeToken<Collection<Measurements>>(){}.getType();
            Collection<Measurements> results = gson.fromJson(json, collectionType);
            return results;
        } else {
            try {
                json = callSafecast(capturedBefore, minScTs, lat, lng);
            } catch (Exception e) { e.printStackTrace(); }
            Type collectionType = new TypeToken<Collection<Measurements>>(){}.getType();
            Collection<Measurements> results = gson.fromJson(json, collectionType);
            return results;
        }
    }

    public AirQuality getAQI(double lat, double lng) throws IOException {
        aqiDecodedString = "";
        aqiJson = "";

        HttpsURLConnection aqiCall = (HttpsURLConnection) (new URL(AqiURL + "?lat=" + lat
                + "&lon=" + lng + "&key=" + aqiKey)).openConnection();
        aqiCall.setRequestProperty("Content-Type", "application/json");
        aqiCall.setRequestProperty("Accept", "application/json");
        aqiCall.setRequestMethod("GET");
        aqiCall.connect();

        try {
            BufferedReader aqiReader = new BufferedReader(new InputStreamReader(aqiCall.getInputStream()));
            while ((aqiDecodedString=aqiReader.readLine()) != null) {
                aqiJson+=aqiDecodedString;
            }
            aqiReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        aqiCall.disconnect();

        AirQuality results = gson.fromJson(aqiJson, AirQuality.class);
        return results;
    }
    public Geo getGeo(String query) throws IOException {
        geoJson = "";
        geoDecodedString = "";

        geocode = URLEncoder.encode(query, "UTF-8");

        HttpsURLConnection geoCall = (HttpsURLConnection) (new URL(geoURL + "?address=" + geocode).openConnection());
        geoCall.setRequestProperty("Content-Type", "application/json");
        geoCall.setRequestProperty("Accept", "application/json");
        geoCall.setRequestMethod("GET");
        geoCall.connect();

        try {
            BufferedReader geoReader = new BufferedReader(new InputStreamReader(geoCall.getInputStream()));
            while ((geoDecodedString=geoReader.readLine()) != null) {
                geoJson+=geoDecodedString;
            }
            geoReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        geoCall.disconnect();

        Geo results = gson.fromJson(geoJson, Geo.class);
        return results;
    }
    public User getAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        return user;
    }
    public String getUser() {
        String account = "";
        User theUser = getAccount();
        if (theUser != null) {
            account = theUser.getEmail();
        }
        return account;
    }
    public boolean checkAccount(String account) {
        boolean isLoggedIn = false;
        if (!account.equals("")) {
            isLoggedIn = true;
        }
        return isLoggedIn;
    }
    public String callSafecast(String capturedBefore, String capturedAfter, double lat, double lng) throws IOException {
        int distance = 1200;
        decodedString = "";
        String result = "";
        HttpsURLConnection scCall = (HttpsURLConnection) (new URL(BaseURL + "?distance=" + distance
                + "&latitude=" + lat + "&longitude=" + lng + "&captured_before=" +
        capturedBefore + "&captured_after=" + capturedAfter).openConnection());
        scCall.setRequestProperty("Content-Type", "application/json");
        scCall.setRequestProperty("Accept", "application/json");
        scCall.setRequestMethod("GET");
        scCall.connect();

        try {
            BufferedReader inreader = new BufferedReader(new InputStreamReader(scCall.getInputStream()));
            while ((decodedString=inreader.readLine()) != null) {
                result+=decodedString;
            }
            inreader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        scCall.disconnect();
        return result;
    }
    public Query makeQuery(Collection<Measurements> sc, AirQuality av) {
        Query newQuery = new Query();
        double rad = 0.0;
        ArrayList<Timestamp> ts = new ArrayList<>();
        for (Measurements entry : sc) {
            if (entry.getRadUnit().equals("cpm")) {
                rad += entry.getRadValue() / 330;
            } else {
                rad += entry.getRadValue();
            }
            Timestamp parsedDate = Timestamp.valueOf(entry.getRadTimestamp());
            ts.add(parsedDate);
        }
        rad = rad / sc.size();
        newQuery.setRadValue(rad);
        newQuery.setMinMeasurementTimestamp(Collections.min(ts));
        newQuery.setMaxMeasurementTimestamp(Collections.max(ts));
        newQuery.setAqiValue(av.getAqiData().getAqiCurrent().getAqiPollution().getAqiUsStd());
        Timestamp aqiParse = Timestamp.valueOf(av.getAqiData().getAqiCurrent().getAqiPollution().getPollutionTimestamp());
        newQuery.setAqiTimestamp(aqiParse);
        newQuery.setWindSpeed(av.getAqiData().getAqiCurrent().getAqiWeather().getWindSpeed());
        newQuery.setWindDirection(av.getAqiData().getAqiCurrent().getAqiWeather().getWindDirection());
        newQuery.setTemp(av.getAqiData().getAqiCurrent().getAqiWeather().getTempCelsius());
        newQuery.setMainPollutant(av.getAqiData().getAqiCurrent().getAqiPollution().getMainUsPollutant());
        newQuery.setWeatherIcon(av.getAqiData().getAqiCurrent().getAqiWeather().getWeatherIcon());
        newQuery.setCity(av.getAqiData().getAqiCity());
        newQuery.setCountry(av.getAqiData().getAqiCountry());
        // test for minimum radTimestamp by one year for isCurrent



        return newQuery;
    }
    public LatLon makeLatLon(double lat, double lon, Query query) {
        LatLon newLatLon = new LatLon();

        return newLatLon;
    }
}
