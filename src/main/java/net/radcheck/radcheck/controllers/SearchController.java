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

    @Autowired
    private UserService userService;

    @Autowired
    private LatLonDao locationRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        String address = "The Gateway Arch, St. Louis, MO";
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Pick A Location To Measure");
        model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
        model.addAttribute("gmap", new GMap(address));

        return "index";
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

    @RequestMapping(value = "/build-report", method = RequestMethod.GET)
    public String chooseReport(Model model) {
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
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Choose which report you want to build");

        return "report/choose-a-report";
    }

    @RequestMapping(value = "/two-by-two", method=RequestMethod.GET)
    public String buildTwoByTwoReport(Model model) {
        User user = getAccount();
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
        Set<LatLon> reportLatLon = new HashSet<>();
        for (int index : locationIds) {
            if (reportLatLon.contains(locationRepository.findOne(index))) {
                errors.rejectValue("locationIds", "error.buildreportform");
            } else {
                reportLatLon.add(locationRepository.findOne(index));
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
        LatLon[] reportLocations = reportLatLon.toArray(new LatLon[0]);
        String[] locationNames = {"", "", "", ""};
        String[] locationClasses = {"", "", "", ""};
        String[] locationInfo = {"", "", "", ""};
        User user = getAccount();
        List<LatLon> savedLocations = user.getLocations();
        boolean isCurrent = true;
        for (LatLon location : savedLocations) {
            if (reportLocations[0].getId() == location.getId()) {
                Array.set(locationNames, 0, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 0, getRatingClass(location));
                Array.set(locationInfo, 0, getRatingInfo(location));
                Instant hereAndNow = Instant.now();
                Timestamp twentyOneHoursAgo = Timestamp.from(hereAndNow.minusMillis(twentyOneHours));
                if (!location.getUpdateTimestamp().after(twentyOneHoursAgo)) {
                    location = refreshLocation(location);
                    location.setViewCount(location.getViewCount() + 1);
                    locationRepository.save(location);
                }
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[1].getId() == location.getId()) {
                Array.set(locationNames, 1, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 1, getRatingClass(location));
                Array.set(locationInfo, 1, getRatingInfo(location));
                Instant hereAndNow = Instant.now();
                Timestamp twentyOneHoursAgo = Timestamp.from(hereAndNow.minusMillis(twentyOneHours));
                if (!location.getUpdateTimestamp().after(twentyOneHoursAgo)) {
                    location = refreshLocation(location);
                    location.setViewCount(location.getViewCount() + 1);
                    locationRepository.save(location);
                }
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[2].getId() == location.getId()) {
                Array.set(locationNames, 2, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 2, getRatingClass(location));
                Array.set(locationInfo, 2, getRatingInfo(location));
                Instant hereAndNow = Instant.now();
                Timestamp twentyOneHoursAgo = Timestamp.from(hereAndNow.minusMillis(twentyOneHours));
                if (!location.getUpdateTimestamp().after(twentyOneHoursAgo)) {
                    location = refreshLocation(location);
                    location.setViewCount(location.getViewCount() + 1);
                    locationRepository.save(location);
                }
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[3].getId() == location.getId()) {
                Array.set(locationNames, 3, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 3, getRatingClass(location));
                Array.set(locationInfo, 3, getRatingInfo(location));
                Instant hereAndNow = Instant.now();
                Timestamp twentyOneHoursAgo = Timestamp.from(hereAndNow.minusMillis(twentyOneHours));
                if (!location.getUpdateTimestamp().after(twentyOneHoursAgo)) {
                    location = refreshLocation(location);
                    location.setViewCount(location.getViewCount() + 1);
                    locationRepository.save(location);
                }
                if (!location.isCurrent()) {
                    isCurrent = false;
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
        model.addAttribute("location01", reportLocations[0]);
        model.addAttribute("name01", locationNames[0]);
        model.addAttribute("rating01", locationClasses[0]);
        model.addAttribute("info01", locationInfo[0]);
        model.addAttribute("location02", reportLocations[1]);
        model.addAttribute("name02", locationNames[1]);
        model.addAttribute("rating02", locationClasses[1]);
        model.addAttribute("info02", locationInfo[1]);
        model.addAttribute("location03", reportLocations[2]);
        model.addAttribute("name03", locationNames[2]);
        model.addAttribute("rating03", locationClasses[2]);
        model.addAttribute("info03", locationInfo[2]);
        model.addAttribute("location04", reportLocations[3]);
        model.addAttribute("name04", locationNames[3]);
        model.addAttribute("rating04", locationClasses[3]);
        model.addAttribute("info04", locationInfo[3]);
        model.addAttribute("key", mapsKey);

        return "report/2x2-report";
    }

    @RequestMapping(value = "/three-by-three", method=RequestMethod.GET)
    public String buildThreeByThreeReport(Model model){
        User user = getAccount();
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
        Set<LatLon> reportLatLon = new HashSet<>();
        for (int index : locationIds) {
            if (reportLatLon.contains(locationRepository.findOne(index))) {
                errors.rejectValue("locationIds", "error.buildreportform");
            } else {
                reportLatLon.add(locationRepository.findOne(index));
            }
        }
        if (errors.hasErrors()) {
            User user = getAccount();
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Build a 2x2 report");
            model.addAttribute("form", reportForm);
            return "redirect:/three-by-three?error=true";
        }
        LatLon[] reportLocations = reportLatLon.toArray(new LatLon[0]);
        String[] locationNames = {"", "", "", "", "", "", "", "", ""};
        String[] locationClasses = {"", "", "", "", "", "", "", "", ""};
        String[] locationInfo = {"", "", "", "", "", "", "", "", ""};
        User user = getAccount();
        List<LatLon> savedLocations = user.getLocations();
        boolean isCurrent = true;
        for (LatLon location : savedLocations) {
            if (reportLocations[0].getId() == location.getId()) {
                Array.set(locationNames, 0, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 0, getRatingClass(location));
                Array.set(locationInfo, 0, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
                }
            if (reportLocations[1].getId() == location.getId()) {
                Array.set(locationNames, 1, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 1, getRatingClass(location));
                Array.set(locationInfo, 1, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[2].getId() == location.getId()) {
                Array.set(locationNames, 2, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 2, getRatingClass(location));
                Array.set(locationInfo, 2, getRatingInfo(location));
            }
            if (reportLocations[3].getId() == location.getId()) {
                Array.set(locationNames, 3, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 3, getRatingClass(location));
                Array.set(locationInfo, 3, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[4].getId() == location.getId()) {
                Array.set(locationNames, 4, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 4, getRatingClass(location));
                Array.set(locationInfo, 4, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[5].getId() == location.getId()) {
                Array.set(locationNames, 5, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 5, getRatingClass(location));
                Array.set(locationInfo, 5, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[6].getId() == location.getId()) {
                Array.set(locationNames, 6, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 6, getRatingClass(location));
                Array.set(locationInfo, 6, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[7].getId() == location.getId()) {
                Array.set(locationNames, 7, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 7, getRatingClass(location));
                Array.set(locationInfo, 7, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
                }
            }
            if (reportLocations[8].getId() == location.getId()) {
                Array.set(locationNames, 8, user.getNames().get(user.getLocations().indexOf(location)));
                Array.set(locationClasses, 8, getRatingClass(location));
                Array.set(locationInfo, 8, getRatingInfo(location));
                if (!location.isCurrent()) {
                    isCurrent = false;
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
        model.addAttribute("location01", reportLocations[0]);
        model.addAttribute("name01", locationNames[0]);
        model.addAttribute("rating01", locationClasses[0]);
        model.addAttribute("info01", locationInfo[0]);
        model.addAttribute("location02", reportLocations[1]);
        model.addAttribute("name02", locationNames[1]);
        model.addAttribute("rating02", locationClasses[1]);
        model.addAttribute("info02", locationInfo[1]);
        model.addAttribute("location03", reportLocations[2]);
        model.addAttribute("name03", locationNames[2]);
        model.addAttribute("rating03", locationClasses[2]);
        model.addAttribute("info03", locationInfo[2]);
        model.addAttribute("location04", reportLocations[3]);
        model.addAttribute("name04", locationNames[3]);
        model.addAttribute("rating04", locationClasses[3]);
        model.addAttribute("info04", locationInfo[3]);
        model.addAttribute("location05", reportLocations[4]);
        model.addAttribute("name05", locationNames[4]);
        model.addAttribute("rating05", locationClasses[4]);
        model.addAttribute("info05", locationInfo[4]);
        model.addAttribute("location06", reportLocations[5]);
        model.addAttribute("name06", locationNames[5]);
        model.addAttribute("rating06", locationClasses[5]);
        model.addAttribute("info06", locationInfo[5]);
        model.addAttribute("location07", reportLocations[6]);
        model.addAttribute("name07", locationNames[6]);
        model.addAttribute("rating07", locationClasses[6]);
        model.addAttribute("info07", locationInfo[6]);
        model.addAttribute("location08", reportLocations[7]);
        model.addAttribute("name08", locationNames[7]);
        model.addAttribute("rating08", locationClasses[7]);
        model.addAttribute("info08", locationInfo[7]);
        model.addAttribute("location09", reportLocations[8]);
        model.addAttribute("name09", locationNames[8]);
        model.addAttribute("rating09", locationClasses[8]);
        model.addAttribute("info09", locationInfo[8]);
        model.addAttribute("key", mapsKey);

        return "report/3x3-report";
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
