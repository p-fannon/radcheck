package net.radcheck.radcheck.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.radcheck.radcheck.models.AirQuality;
import net.radcheck.radcheck.models.GMap;
import net.radcheck.radcheck.models.Geo;
import net.radcheck.radcheck.models.Measurements;
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
import java.util.Collection;

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



    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        String address = "The Gateway Arch, St. Louis, MO";
        model.addAttribute("title", "Pick A Location");
        model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
        model.addAttribute("address", address);
        model.addAttribute("gmap", new GMap(address));

        return "index";
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public String mapsSearch(Model model, @ModelAttribute @Valid GMap newMap, Errors
                             errors) throws IOException {
        if (errors.hasErrors()) {
            model.addAttribute("title", "Pick A Location");
            model.addAttribute("key", "https://maps.googleapis.com/maps/api/js?key=" + mapsKey + "&callback=initMap");
            model.addAttribute("gmap", newMap);
            model.addAttribute("address", newMap.getAddress());
            return "index";
        }

        Geo geoReturn = getGeo(newMap.getAddress());

        if (geoReturn.getResults().size() == 0) {
            // add an alert here about the maps API failing
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

        model.addAttribute("title", "Current readings at this location");
        model.addAttribute("return", safeCastReturns);
        model.addAttribute("latitude", aLatitude);
        model.addAttribute("longitude", aLongitude);
        model.addAttribute("key", mapsKey);
        model.addAttribute("aqi", airVisualReturn);
        return "result";
    }

    @RequestMapping(value = "/manual", method=RequestMethod.GET)
    public String manualSearch(Model model) {
        model.addAttribute("title", "Pick A Location");
        model.addAttribute("measurements", new Measurements());

        return "manual-search";
    }

    @RequestMapping(value = "/manual", method = RequestMethod.POST)
    public String manualResult(Model model,
                         @ModelAttribute @Valid Measurements newMeasurement,
                         Errors errors) throws IOException {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Pick A Location");
            model.addAttribute("measurements", newMeasurement);
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

        model.addAttribute("title", "Current readings at the given location");
        model.addAttribute("return", safeCastReturns);
        model.addAttribute("latitude", aLatitude);
        model.addAttribute("longitude", aLongitude);
        model.addAttribute("key", mapsKey);
        model.addAttribute("aqi", airVisualReturn);

        return "result";
    }

    public Collection<Measurements> getMeasurements(double lat, double lng) throws IOException {
        int distance = 1200;
        decodedString = "";
        json = "";
        HttpsURLConnection scCall = (HttpsURLConnection) (new URL(BaseURL + "?distance=" + distance
                + "&latitude=" + lat + "&longitude=" + lng).openConnection());
        scCall.setRequestProperty("Content-Type", "application/json");
        scCall.setRequestProperty("Accept", "application/json");
        scCall.setRequestMethod("GET");
        scCall.connect();

        try {
            BufferedReader inreader = new BufferedReader(new InputStreamReader(scCall.getInputStream()));
            while ((decodedString=inreader.readLine()) != null) {
                json+=decodedString;
            }
            inreader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        scCall.disconnect();

        Type collectionType = new TypeToken<Collection<Measurements>>(){}.getType();
        Collection<Measurements> results = gson.fromJson(json, collectionType);
        return results;
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
}
