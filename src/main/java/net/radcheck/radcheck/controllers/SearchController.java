package net.radcheck.radcheck.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.radcheck.radcheck.models.*;
import net.radcheck.radcheck.models.data.LatLonDao;
import net.radcheck.radcheck.models.data.UserDao;
import net.radcheck.radcheck.models.forms.AddLocationItemForm;
import net.radcheck.radcheck.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.Instant;
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
                             errors, HttpSession session) throws IOException {
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
            return "redirect:/?maperror=true";
        }

        Geo geoReturn = getGeo(newMap.getAddress());

        if (geoReturn.getResults().size() == 0 || !geoReturn.getStatus().equals("OK")) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Pick A Location");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
            model.addAttribute("address", newMap.getAddress());
            return "redirect:/?apierror=true";
        }

        double aLatitude = geoReturn.getResults().get(0).getGeometry().getMarker().getGeoLatitude();
        double aLongitude = geoReturn.getResults().get(0).getGeometry().getMarker().getGeoLongitude();

        Collection<Measurements> safeCastReturns = getMeasurements(aLatitude, aLongitude);

        if (safeCastReturns.size() == 0) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "No results to display :(");
            return "null-result";
        }

        AirQuality airVisualReturn = getAQI(aLatitude, aLongitude);

        Query returnedQuery = makeQuery(safeCastReturns, airVisualReturn);

        LatLon returnedLatLon = new LatLon(aLatitude, aLongitude, returnedQuery);
        returnedQuery.setLocation(returnedLatLon);
        returnedQuery.setLat(aLatitude);
        returnedQuery.setLon(aLongitude);

        String ratingClass = getRatingClass(returnedLatLon);
        String ratingInfo = getRatingInfo(returnedLatLon);

        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Current readings at this location");
        model.addAttribute("return", returnedLatLon);
        model.addAttribute("rating_class", ratingClass);
        model.addAttribute("rating_info", ratingInfo);
        model.addAttribute("latitude", aLatitude);
        model.addAttribute("longitude", aLongitude);
        if (checkAccount(account)) {
            session.setAttribute("formLocation", returnedLatLon);
        }
        model.addAttribute("key", mapsKey);
        return "result";
    }

    @RequestMapping(value = "/manual", method=RequestMethod.GET)
    public String manualSearch(Model model) {
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
                         Errors errors, HttpSession session) throws IOException {

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

        if (safeCastReturns.size() == 0) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "No results to display :(");
            return "null-result";
        }

        AirQuality airVisualReturn = getAQI(aLatitude, aLongitude);

        Query returnedQuery = makeQuery(safeCastReturns, airVisualReturn);

        LatLon returnedLatLon = new LatLon(aLatitude, aLongitude, returnedQuery);
        returnedQuery.setLocation(returnedLatLon);
        returnedQuery.setLat(aLatitude);
        returnedQuery.setLon(aLongitude);

        String ratingClass = getRatingClass(returnedLatLon);
        String ratingInfo = getRatingInfo(returnedLatLon);

        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Current readings at this location");
        model.addAttribute("return", returnedLatLon);
        model.addAttribute("rating_class", ratingClass);
        model.addAttribute("rating_info", ratingInfo);
        model.addAttribute("latitude", aLatitude);
        model.addAttribute("longitude", aLongitude);
        if (checkAccount(account)) {
            session.setAttribute("formLocation", returnedLatLon);
        }
        model.addAttribute("key", mapsKey);

        return "result";
    }

    public Collection<Measurements> getMeasurements(double lat, double lng) throws IOException {
        json = "";
        Instant newInstant = Instant.now();
        String capturedBefore = newInstant.toString();
        Instant oneMonthAgo = newInstant.minusSeconds(2592000);
        String capturedAfter = oneMonthAgo.toString();
        json = callSafecast(capturedBefore, capturedAfter, lat, lng);
        if (!json.equals("[]")) {
            Type collectionType = new TypeToken<Collection<Measurements>>(){}.getType();
            Collection<Measurements> results = gson.fromJson(json, collectionType);
            return results;
        } else {
            json = "";
            Instant oneYearAgo = newInstant.minusSeconds(31557600);
            String newCapturedAfter = oneYearAgo.toString();
            json = callSafecast(capturedBefore, newCapturedAfter, lat, lng);
            if (!json.equals("[]")) {
                Type collectionType = new TypeToken<Collection<Measurements>>(){}.getType();
                Collection<Measurements> results = gson.fromJson(json, collectionType);
                return results;
            } else {
                json = "";
                json = callSafecast(capturedBefore, minScTs, lat, lng);
                Type collectionType = new TypeToken<Collection<Measurements>>(){}.getType();
                Collection<Measurements> results = gson.fromJson(json, collectionType);
                return results;
            }
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
    public String callSafecast(String capturedBefore, String capturedAfter, double lat, double lng) throws IOException {
        int distance = 1000;
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
        int measurementCount = 0;
        ArrayList<Timestamp> ts = new ArrayList<>();
        for (Measurements entry : sc) {
            if (entry.getRadUnit().equals("cpm")) {
                rad += entry.getRadValue() / 330;
                measurementCount++;
                Instant isoInstant = Instant.parse(entry.getRadTimestamp());
                Timestamp parsedDate = Timestamp.from(isoInstant);
                ts.add(parsedDate);
            } else if (entry.getRadUnit().equals("usv")) {
                rad += entry.getRadValue();
                measurementCount++;
                Instant isoInstant = Instant.parse(entry.getRadTimestamp());
                Timestamp parsedDate = Timestamp.from(isoInstant);
                ts.add(parsedDate);
            }
        }
        rad = rad / measurementCount;
        newQuery.setRadValue(rad);
        newQuery.setTotalMeasurements(measurementCount);
        Collections.sort(ts);
        newQuery.setMinMeasurementTimestamp(ts.get(0));
        newQuery.setMaxMeasurementTimestamp(ts.get(ts.size() - 1));
        newQuery.setAqiValue(av.getAqiData().getAqiCurrent().getAqiPollution().getAqiUsStd());
        Instant aqiInstant = Instant.parse(av.getAqiData().getAqiCurrent().getAqiPollution().getPollutionTimestamp());
        Timestamp aqiParse = Timestamp.from(aqiInstant);
        newQuery.setAqiTimestamp(aqiParse);
        newQuery.setWindSpeed(av.getAqiData().getAqiCurrent().getAqiWeather().getWindSpeed());
        newQuery.setWindDirection(av.getAqiData().getAqiCurrent().getAqiWeather().getWindDirection());
        newQuery.setTemp(av.getAqiData().getAqiCurrent().getAqiWeather().getTempCelsius());
        newQuery.setMainPollutant(av.getAqiData().getAqiCurrent().getAqiPollution().getMainUsPollutant());
        newQuery.setWeatherIcon(av.getAqiData().getAqiCurrent().getAqiWeather().getWeatherIcon());
        newQuery.setCity(av.getAqiData().getAqiCity());
        newQuery.setCountry(av.getAqiData().getAqiCountry());
        Instant rightNow = Instant.now();
        Instant oneYearAndADayAgo = rightNow.minusSeconds(31644000);
        Instant minTs = newQuery.getMinMeasurementTimestamp().toInstant();
        if (minTs.isAfter(oneYearAndADayAgo)) {
            newQuery.setCurrent(true);
        } else {
            newQuery.setCurrent(false);
        }
        double testRad = newQuery.getRadValue();
        int testAqi = newQuery.getAqiValue();
        if (testRad > .7714 || testAqi > 300) {
            newQuery.setRating("Hazardous");
        } else if (testRad > .5771 || testAqi > 200) {
            newQuery.setRating("Very unhealthy");
        } else if (testRad > .4028 || testAqi > 150) {
            newQuery.setRating("Unhealthy");
        } else if (testRad > .2485 || testAqi > 100) {
            newQuery.setRating("Unhealthy for sensitive groups");
        } else if (testRad > .1142 || testAqi > 50) {
            newQuery.setRating("Moderately safe");
        } else {
            newQuery.setRating("Safe");
        }

        return newQuery;
    }
    public String getRatingClass(LatLon location) {
        String ratingClass = "";
        switch (location.getQuery().getRating()) {
            case "Safe": ratingClass = "card bg-success text-white";
            break;
            case "Moderately safe": ratingClass = "card bg-success text-white";
            break;
            case "Unhealthy for sensitive groups": ratingClass = "card bg-warning text-white";
            break;
            case "Unhealthy": ratingClass = "card bg-warning text-white";
            break;
            case "Very unhealthy": ratingClass = "card bg-danger text-white";
            break;
            case "Hazardous": ratingClass = "card bg-danger text-white";
            break;
        }
        return ratingClass;
    }
    public String getRatingInfo(LatLon location) {
        String ratingInfo = "";
        switch (location.getQuery().getRating()) {
            case "Safe": ratingInfo = "The environment here poses little to no health risk. Enjoy your usual outdoor activities, and try opening windows to ventilate your home with oxygen-rich air.";
                break;
            case "Moderately safe": ratingInfo = "The environment here is acceptable and poses little health risk. Enjoy your usual outdoor activities, and try opening windows to ventilate your home with oxygen-rich air.";
                break;
            case "Unhealthy for sensitive groups": ratingInfo = "The general public should limit exposure to the environment. Sensitive groups should avoid any prolonged outdoor activities. Recommended to wear a pollution mask outdoors. Ventilation is discouraged. Air purifiers should be turned on if indoor levels of contamination are elevated.";
                break;
            case "Unhealthy": ratingInfo = "Exposure to the environment, particularly for sensitive groups, should be limited. Everyone should take care to wear a pollution mask. Ventilation is not recommended. Air purifiers should be turned on if indoor levels of contamination are elevated.";
                break;
            case "Very unhealthy": ratingInfo = "The general public should greatly reduce exposure to the environment. Sensitive groups should avoid any outdoor activities. Everyone should take care to wear a pollution mask and cover up exposed body parts. Ventilation is not recommended. Air purifiers should be turned on.";
                break;
            case "Hazardous": ratingInfo = "The general public should avoid exposure to the environment. Everyone should take care to wear a quality pollution mask and cover up exposed body parts. Ventilation is not recommended. Air purifiers should be turned on.";
                break;
        }
        return ratingInfo;
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
}
