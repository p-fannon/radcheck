package net.radcheck.radcheck.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.radcheck.radcheck.models.*;
import net.radcheck.radcheck.models.data.LatLonDao;
import net.radcheck.radcheck.models.forms.BuildReportForm;
import net.radcheck.radcheck.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@org.springframework.stereotype.Controller
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
    private static String mapsKey = "AIzaSyAqvB0THWS44yHV3OOBzQQ0znAst9V6uQA"; // AIzaSyDen0WZLZt-OQ68yU5D5uoNb7sr34mdycQ
    private static String aqiKey = "3RDkWgP8CSpxMTGFM";
    private String geocode;
    private static Gson gson = new Gson();
    private static String minScTs = "2011-03-10T00:00:00Z";
    private static long twentyOneHours = 75600000L;
    private static String[] list = {"The Gateway Arch, St. Louis, MO", "Apotheosis of Saint Louis, Fine Arts Drive, St. Louis, MO",
            "Washington Monument, Washington, DC"};
    private static double[] lat = {38.624696, 38.639861, 38.889517};
    private static double[] lon = {-90.184778, -90.294099, -77.035290};

    @Autowired
    private UserService userService;

    @Autowired
    private LatLonDao locationRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        int address = pickRandomLandmark();
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Pick A Location To Measure");
        model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
        model.addAttribute("gmap", new GMap(list[address]));
        model.addAttribute("latitude", lat[address]);
        model.addAttribute("longitude", lon[address]);

        return "index";
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(Model model) {
        int address = pickRandomLandmark();
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Pick A Location To Measure");
        model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
        model.addAttribute("gmap", new GMap(list[address]));
        model.addAttribute("latitude", lat[address]);
        model.addAttribute("longitude", lon[address]);

        return "search";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String mapsSearch(Model model, @ModelAttribute @Valid GMap newMap, Errors
                             errors, HttpSession session) throws IOException {
        if (errors.hasErrors()) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Pick A Location To Measure");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
        }

        Geo geoReturn = getGeo(newMap.getAddress());

        if (geoReturn.getResults().size() == 0) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Pick A Location To Measure");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
            return "redirect:/?retry=true";
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

        if (!airVisualReturn.getAqiStatus().equals("success")) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Pick A Location To Measure");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
            return "redirect:/?retry=true";
        }

        LatLon returnedLatLon = makeQuery(safeCastReturns, airVisualReturn, aLatitude, aLongitude);

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
            session.setAttribute("candidateLocation", returnedLatLon);
        }
        model.addAttribute("key", mapsKey);
        return "result";
    }

    @RequestMapping(value = "/manual", method=RequestMethod.GET)
    public String manualSearch(Model model) {
        model.addAttribute("title", "Pick A Location To Measure");
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
            model.addAttribute("title", "Pick A Location To Measure");
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

        if (!airVisualReturn.getAqiStatus().equals("success")) {
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));;
            model.addAttribute("title", "Pick A Location To Measure");
            return "redirect:/?retry=true";
        }

        LatLon returnedLatLon = makeQuery(safeCastReturns, airVisualReturn, aLatitude, aLongitude);

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
            session.setAttribute("candidateLocation", returnedLatLon);
        }
        model.addAttribute("key", mapsKey);

        return "result";
    }

    @RequestMapping(value = "/view/{locationId}", method=RequestMethod.GET)
    public String viewSavedResult(Model model, @PathVariable int locationId,
                                  HttpSession session) throws IOException {
        LatLon returnedLatLon = locationRepository.findOne(locationId);
        Instant hereAndNow = Instant.now();
        Timestamp twentyOneHoursAgo = Timestamp.from(hereAndNow.minusMillis(twentyOneHours));
        if (!returnedLatLon.getUpdateTimestamp().after(twentyOneHoursAgo)) {
            returnedLatLon = refreshLocation(returnedLatLon);
            returnedLatLon.setViewCount(returnedLatLon.getViewCount() + 1);
            locationRepository.save(returnedLatLon);
        }
        String ratingClass = getRatingClass(returnedLatLon);
        String ratingInfo = getRatingInfo(returnedLatLon);

        User user = getAccount();
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Current readings at this saved location");
        model.addAttribute("return", returnedLatLon);
        model.addAttribute("rating_class", ratingClass);
        model.addAttribute("rating_info", ratingInfo);
        model.addAttribute("latitude", returnedLatLon.getLat());
        model.addAttribute("longitude", returnedLatLon.getLon());
        model.addAttribute("key", mapsKey);
        session.setAttribute("locale", user.getNames().get(user.getLocations().indexOf(returnedLatLon)));

        return "view-location";
    }

    @RequestMapping(value = "/two-by-two", method=RequestMethod.GET)
    public String buildTwoByTwoReport(Model model) {
        User user = getAccount();
        if (user.getLocations().size() < 4) {
            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("title", "Your user profile");
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("names", user.getNames());
            model.addAttribute("user", user);

            return "redirect:/user/profile?toofew=true";
        }
        String account = getUser();
        Instant rightNow = Instant.now();
        Timestamp refresher = Timestamp.from(rightNow.minusMillis(twentyOneHours));
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Build a 2x2 report");
        model.addAttribute("refresh", refresher);
        model.addAttribute("form", new BuildReportForm(user));

        return "report/2x2-builder";
    }

    @RequestMapping(value = "/two-by-two", method=RequestMethod.POST)
    public String serveTwoByTwoReport(@ModelAttribute @Valid BuildReportForm reportForm,
                                      Errors errors,
                                      @RequestParam List<Integer> locationIds,
                                      Model model) throws IOException {
        ArrayList<LatLon> orderedLatLon = new ArrayList<>();
        for (int index : locationIds) {
            if (orderedLatLon.contains(locationRepository.findOne(index))) {
                errors.rejectValue("locationIds", "error.buildreportform");
            } else {
                orderedLatLon.add(locationRepository.findOne(index));
            }
        }
        if (errors.hasErrors()) {
            User user = getAccount();
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Build a 2x2 report");
            model.addAttribute("form", reportForm);
            return "redirect:/two-by-two?error=true";
        }
        LatLon[] reportLocations = orderedLatLon.toArray(new LatLon[0]);
        String[] locationNames = {"", "", "", ""};
        String[] locationClasses = {"", "", "", ""};
        String[] locationInfo = {"", "", "", ""};
        User user = getAccount();
        List<LatLon> savedLocations = user.getLocations();
        boolean isCurrent = true;
        for (LatLon location : savedLocations) {
            for (int i = 0; i < 4; i++) {
                if (reportLocations[i].getId() == location.getId()) {
                    Instant hereAndNow = Instant.now();
                    Timestamp twentyOneHoursAgo = Timestamp.from(hereAndNow.minusMillis(twentyOneHours));
                    if (!location.getUpdateTimestamp().after(twentyOneHoursAgo)) {
                        location = refreshLocation(location);
                        location.setViewCount(location.getViewCount() + 1);
                        locationRepository.save(location);
                    }
                    Array.set(locationNames, i, user.getNames().get(user.getLocations().indexOf(location)));
                    Array.set(locationClasses, i, getRatingClass(location));
                    Array.set(locationInfo, i, getRatingInfo(location));
                    if (!location.isCurrent()) {
                        isCurrent = false;
                    }
                }
            }
        }
        String account = getUser();
        Date todayDate = Date.from(Instant.now());
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Your 2x2 Report");
        model.addAttribute("message", "Your report generated on " + todayDate.toString() + ":");
        model.addAttribute("current", isCurrent);
        for (int j = 0; j < 4; j++) {
            model.addAttribute("location0" + (j+1), reportLocations[j]);
            model.addAttribute("name0" + (j+1), locationNames[j]);
            model.addAttribute("rating0" + (j+1), locationClasses[j]);
            model.addAttribute("info0" + (j+1), locationInfo[j]);
        }
        model.addAttribute("key", mapsKey);

        return "report/2x2-report";
    }

    @RequestMapping(value = "/three-by-three", method=RequestMethod.GET)
    public String buildThreeByThreeReport(Model model){
        User user = getAccount();
        if (user.getLocations().size() < 9) {
            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("title", "Your user profile");
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("names", user.getNames());
            model.addAttribute("user", user);

            return "redirect:/user/profile?toofewthree=true";
        }
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Build a 3x3 report");
        model.addAttribute("form", new BuildReportForm(user));

        return "report/3x3-builder";
    }

    @RequestMapping(value = "/three-by-three", method=RequestMethod.POST)
    public String serveThreeByThreeReport(@ModelAttribute @Valid BuildReportForm reportForm,
                                          Errors errors,
                                          @RequestParam List<Integer> locationIds,
                                          Model model) {
        ArrayList<LatLon> orderedLatLon = new ArrayList<>();
        for (int index : locationIds) {
            if (orderedLatLon.contains(locationRepository.findOne(index))) {
                errors.rejectValue("locationIds", "error.buildreportform");
            } else {
                orderedLatLon.add(locationRepository.findOne(index));
            }
        }
        if (errors.hasErrors()) {
            User user = getAccount();
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Build a 3x3 report");
            model.addAttribute("form", reportForm);
            return "redirect:/three-by-three?error=true";
        }
        LatLon[] reportLocations = orderedLatLon.toArray(new LatLon[0]);
        String[] locationNames = {"", "", "", "", "", "", "", "", ""};
        String[] locationClasses = {"", "", "", "", "", "", "", "", ""};
        String[] locationInfo = {"", "", "", "", "", "", "", "", ""};
        User user = getAccount();
        List<LatLon> savedLocations = user.getLocations();
        boolean isCurrent = true;
        for (LatLon location : savedLocations) {
            for (int i = 0; i < 9; i++) {
                if (reportLocations[i].getId() == location.getId()) {
                    Array.set(locationNames, i, user.getNames().get(user.getLocations().indexOf(location)));
                    Array.set(locationClasses, i, getRatingClass(location));
                    Array.set(locationInfo, i, getRatingInfo(location));
                    if (!location.isCurrent()) {
                        isCurrent = false;
                    }
                }
            }
        }
        String account = getUser();
        Date todayDate = Date.from(Instant.now());
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Your 3x3 Report");
        model.addAttribute("message", "Your report generated on " + todayDate.toString() + ":");
        model.addAttribute("current", isCurrent);
        for (int j = 0; j < 9; j++) {
            model.addAttribute("location0" + (j+1), reportLocations[j]);
            model.addAttribute("name0" + (j+1), locationNames[j]);
            model.addAttribute("rating0" + (j+1), locationClasses[j]);
            model.addAttribute("info0" + (j+1), locationInfo[j]);
        }
        model.addAttribute("key", mapsKey);

        return "report/3x3-report";
    }

    @RequestMapping(value = "/four-by-four", method=RequestMethod.GET)
    public String buildFourByFourReport(Model model) {
        User user = getAccount();
        if (user.getLocations().size() < 16) {
            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("title", "Your user profile");
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("names", user.getNames());
            model.addAttribute("user", user);

            return "redirect:/user/profile?toofewfour=true";
        }
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Build a 3x3 report");
        model.addAttribute("form", new BuildReportForm(user));

        return "report/4x4-builder";
    }

    @RequestMapping(value = "/four-by-four", method=RequestMethod.POST)
    public String serveFourByFourReport(@ModelAttribute @Valid BuildReportForm reportForm,
                                        Errors errors,
                                        @RequestParam List<Integer> locationIds,
                                        Model model) {
        ArrayList<LatLon> orderedLatLon = new ArrayList<>();
        for (int index : locationIds) {
            if (orderedLatLon.contains(locationRepository.findOne(index))) {
                errors.rejectValue("locationIds", "error.buildreportform");
            } else {
                orderedLatLon.add(locationRepository.findOne(index));
            }
        }
        if (errors.hasErrors()) {
            User user = getAccount();
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Build a 4x4 report");
            model.addAttribute("form", reportForm);
            return "redirect:/four-by-four?error=true";
        }
        LatLon[] reportLocations = orderedLatLon.toArray(new LatLon[0]);
        String[] locationNames = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
        String[] locationClasses = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
        String[] locationInfo = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
        User user = getAccount();
        List<LatLon> savedLocations = user.getLocations();
        boolean isCurrent = true;
        for (LatLon location : savedLocations) {
            for (int i = 0; i < 16; i++) {
                if (reportLocations[i].getId() == location.getId()) {
                    Array.set(locationNames, i, user.getNames().get(user.getLocations().indexOf(location)));
                    Array.set(locationClasses, i, getRatingClass(location));
                    Array.set(locationInfo, i, getRatingInfo(location));
                    if (!location.isCurrent()) {
                        isCurrent = false;
                    }
                }
            }
        }
        String account = getUser();
        Date todayDate = Date.from(Instant.now());
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Your 4x4 Report");
        model.addAttribute("message", "Your report generated on " + todayDate.toString() + ":");
        model.addAttribute("current", isCurrent);
        for (int j = 0; j < 16; j++) {
            model.addAttribute("location0" + (j+1), reportLocations[j]);
            model.addAttribute("name0" + (j+1), locationNames[j]);
            model.addAttribute("rating0" + (j+1), locationClasses[j]);
            model.addAttribute("info0" + (j+1), locationInfo[j]);
        }
        model.addAttribute("key", mapsKey);

        return "report/4x4-report";
    }

    public int pickRandomLandmark() {
        int landmark = 0;
        double rng = Math.random();
        double range = 0.0;
        for (int i = 0; i < 3; i++) {
            if (rng >= range && rng < range + 0.333333333) {
                landmark = i;
            }
            range += 0.333333333;
        }
        return landmark;
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
        if (results == null) {
            AirQuality badResults = new AirQuality();
            badResults.setAqiStatus("failure");
            return badResults;
        }
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
    public LatLon makeQuery(Collection<Measurements> sc, AirQuality av, double lat, double lon) {
        LatLon newLatLon = new LatLon(lat, lon);
        double rad = 0.0;
        int measurementCount = 0;
        ArrayList<Timestamp> ts = new ArrayList<>();
        for (Measurements entry : sc) {
            if (entry.getRadUnit().equals("cpm")) {
                rad += entry.getRadValue() / 345;
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
        newLatLon.setRadValue(rad);
        newLatLon.setTotalMeasurements(measurementCount);
        Collections.sort(ts);
        newLatLon.setMinMeasurementTimestamp(ts.get(0));
        newLatLon.setMaxMeasurementTimestamp(ts.get(ts.size() - 1));
        newLatLon.setAqiValue(av.getAqiData().getAqiCurrent().getAqiPollution().getAqiUsStd());
        Instant aqiInstant = Instant.parse(av.getAqiData().getAqiCurrent().getAqiPollution().getPollutionTimestamp());
        Timestamp aqiParse = Timestamp.from(aqiInstant);
        newLatLon.setAqiTimestamp(aqiParse);
        newLatLon.setWindSpeed(av.getAqiData().getAqiCurrent().getAqiWeather().getWindSpeed());
        newLatLon.setWindDirection(av.getAqiData().getAqiCurrent().getAqiWeather().getWindDirection());
        newLatLon.setTemp(av.getAqiData().getAqiCurrent().getAqiWeather().getTempCelsius());
        newLatLon.setMainPollutant(av.getAqiData().getAqiCurrent().getAqiPollution().getMainUsPollutant());
        newLatLon.setWeatherIcon(av.getAqiData().getAqiCurrent().getAqiWeather().getWeatherIcon());
        newLatLon.setCity(av.getAqiData().getAqiCity());
        newLatLon.setCountry(av.getAqiData().getAqiCountry());
        Instant rightNow = Instant.now();
        Instant oneYearAndADayAgo = rightNow.minusSeconds(31644000);
        Instant minTs = newLatLon.getMinMeasurementTimestamp().toInstant();
        if (minTs.isAfter(oneYearAndADayAgo)) {
            newLatLon.setCurrent(true);
        } else {
            newLatLon.setCurrent(false);
        }
        newLatLon.setRating(getNewRating(newLatLon.getRadValue(), newLatLon.getAqiValue()));

        return newLatLon;
    }
    public LatLon refreshLocation(LatLon refresh) throws IOException {
        Collection<Measurements> refreshedMeasurements = getMeasurements(refresh.getLat(), refresh.getLon());
        refresh = updateMeasurements(refresh, refreshedMeasurements);
        AirQuality refreshedAqi = getAQI(refresh.getLat(), refresh.getLon());
        if (refreshedAqi.getAqiStatus().equals("success")) {
            refresh = updateAqi(refresh, refreshedAqi);
        }
        refresh.setRating(getNewRating(refresh.getRadValue(), refresh.getAqiValue()));

        return refresh;
    }
    public String getNewRating(double testRad, double testAqi) {
        String rating;
        if (testRad > .7714 || testAqi > 300) {
            rating = "Hazardous";
        } else if (testRad > .5771 || testAqi > 200) {
            rating = "Very unhealthy";
        } else if (testRad > .4028 || testAqi > 150) {
            rating = "Unhealthy";
        } else if (testRad > .2485 || testAqi > 100) {
            rating = "Unhealthy for sensitive groups";
        } else if (testRad > .1142 || testAqi > 50) {
            rating = "Moderately safe";
        } else {
            rating = "Safe";
        }

        return rating;
    }
    public String getRatingClass(LatLon location) {
        String ratingClass = "";
        switch (location.getRating()) {
            case "Safe": ratingClass = "card bg-success text-white";
            break;
            case "Moderately safe": ratingClass = "card bg-success text-white";
            break;
            case "Unhealthy for sensitive groups": ratingClass = "card bg-warning text-dark";
            break;
            case "Unhealthy": ratingClass = "card bg-warning text-dark";
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
        switch (location.getRating()) {
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
    public LatLon updateMeasurements(LatLon updateLocation, Collection<Measurements> measurements) {
        double rad = 0.0;
        int measurementCount = 0;
        ArrayList<Timestamp> ts = new ArrayList<>();
        for (Measurements entry : measurements) {
            if (entry.getRadUnit().equals("cpm")) {
                rad += entry.getRadValue() / 345;
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
        updateLocation.setRadValue(rad);
        updateLocation.setTotalMeasurements(measurementCount);
        Collections.sort(ts);
        updateLocation.setMinMeasurementTimestamp(ts.get(0));
        updateLocation.setMaxMeasurementTimestamp(ts.get(ts.size() - 1));
        Instant rightNow = Instant.now();
        Instant oneYearAndADayAgo = rightNow.minusSeconds(31644000);
        Instant minTs = updateLocation.getMinMeasurementTimestamp().toInstant();
        if (minTs.isAfter(oneYearAndADayAgo)) {
            updateLocation.setCurrent(true);
        } else {
            updateLocation.setCurrent(false);
        }

        return updateLocation;
    }
    public LatLon updateAqi(LatLon updateLocation, AirQuality aqi) {
        updateLocation.setAqiValue(aqi.getAqiData().getAqiCurrent().getAqiPollution().getAqiUsStd());
        Instant aqiInstant = Instant.parse(aqi.getAqiData().getAqiCurrent().getAqiPollution().getPollutionTimestamp());
        Timestamp aqiParse = Timestamp.from(aqiInstant);
        updateLocation.setAqiTimestamp(aqiParse);
        updateLocation.setWindSpeed(aqi.getAqiData().getAqiCurrent().getAqiWeather().getWindSpeed());
        updateLocation.setWindDirection(aqi.getAqiData().getAqiCurrent().getAqiWeather().getWindDirection());
        updateLocation.setTemp(aqi.getAqiData().getAqiCurrent().getAqiWeather().getTempCelsius());
        updateLocation.setMainPollutant(aqi.getAqiData().getAqiCurrent().getAqiPollution().getMainUsPollutant());
        updateLocation.setWeatherIcon(aqi.getAqiData().getAqiCurrent().getAqiWeather().getWeatherIcon());
        updateLocation.setCity(aqi.getAqiData().getAqiCity());
        updateLocation.setCountry(aqi.getAqiData().getAqiCountry());

        return updateLocation;
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
