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
            "Washington Monument, Washington, DC", "Empire State Building, 5th Avenue, New York, NY", "Big Ben, London, UK",
            "Leaning Tower of Pisa, Pisa, Province of Pisa, Italy", "Colosseum, Piazza del Colosseo, Rome, Metropolitan City of Rome, Italy",
            "Golden Gate Bridge, Golden Gate Bridge, San Francisco, CA", "Cathédrale Notre-Dame de Paris, Parvis Notre-Dame - place Jean-Paul-II, Paris, France",
            "Eiffel Tower, Champ de Mars, Avenue Anatole France, Paris, France", "Tokyo Tower, 4 Chome-2-8 Shibakoen, Minato, Tokyo, Japan",
            "St. Peter's Basilica, Piazza San Pietro, Vatican City", "Sagrada Familia, Carrer de Mallorca, Barcelona, Spain",
            "The Little Mermaid, Langelinie, København Ø, Denmark", "Arc de Triomphe, Place Charles de Gaulle, Paris, France",
            "Berlin Wall Memorial, Bernauer Straße, Berlin, Germany", "Stonehenge, Amesbury, Salisbury, UK", "Uluru, Petermann NT, Australia",
            "Taj Mahal, Dharmapuri, Forest Colony, Tajganj, Agra, Uttar Pradesh, India", "Tower Bridge, United Kingdom",
            "Willis Tower, Sears Tower Skydeck, South Wacker Drive, Chicago, IL", "Brooklyn Bridge, Brooklyn, NY",
            "Burj Al Arab Jumeirah - Dubai - United Arab Emirates", "Acropolis of Athens, Athens, Greece",
            "Trevi Fountain, Piazza di Trevi, Rome, Metropolitan City of Rome, Italy", "St. Mark's Basilica & Campanile",
            "Times Square, Manhattan, New York, NY", "Louvre Museum, Rue de Rivoli, Paris, France", "Manneken Pis, Brussels",
            "Buckingham Palace, London, UK", "Neuschwanstein Castle, Neuschwansteinstraße, Schwangau, Germany",
            "Florence Cathedral, Piazza del Duomo, Florence, Metropolitan City of Florence, Italy",
            "Castle Rock, Edinburgh, UK", "Machu Picchu, Peru", "CN Tower, Front Street West, Toronto, ON, Canada",
            "Burj Khalifa", "Tower of London, London, UK", "Mont Saint-Michel, France", "Las Vegas Sign, South Las Vegas Boulevard, Las Vegas, NV",
            "Sacré-Cœur, rue du Chevalier-de-La-Barre, Paris, France", "St. Paul's Cathedral, London, UK", "Bethesda Fountain Central Park, Manhattan, NY",
            "Mount Fuji, Kitayama, Fujinomiya, Shizuoka Prefecture, Japan", "Old Faithful Geyser, Wyoming", "Rialto Bridge, Sestiere San Polo, Venice, Metropolitan City of Venice, Italy",
            "Space Needle, Seattle, WA", "Westminster Abbey, London, UK", "The Shard, London, UK", "The Gherkin, St Mary Axe, London, UK",
            "Luxor Temple, Luxor City, Luxor, Egypt", "Brandenburg Gate, Pariser Platz, Berlin, Germany", "Cologne Cathedral, Cologne, Germany",
            "The Pentagon, Washington, DC", "Cloud Gate, Chicago, IL", "Petra, Jordan", "Cankurtaran Mahallesi, The Hagia Sophia Mosque, Soğuk Çeşme Sokak, Fatih/Istanbul, Turkey", //56
            "Oriental Pearl Tower, Century Avenue, Lujiazui, Pudong, Shanghai, China", "Nyhavn, Copenhagen, Denmark", "Ponte Vecchio, Ponte Vecchio, Florence, Metropolitan City of Florence, Italy",
            "Western Wall, Jerusalem", "Sistine Chapel, Vatican City", "Spanish Steps, Piazza di Spagna, Rome, Metropolitan City of Rome, Italy",
            "Bridge of Sighs, Piazza San Marco, Venice, Metropolitan City of Venice, Italy", "Pompidou Center, Paris, France",
            "Great Buddha of Kamakura, 4 Chome-2-28 Hase, Kamakura, Kanagawa Prefecture, Japan", "Freedom Tower, World Trade Center, New York, NY",
            "Kronborg Castle, Kronborg, Helsingør, Denmark", "Shanghai World Financial Center, Lujiazui, Pudong, Shanghai, China",
            "Moulin Rouge, Boulevard de Clichy, Paris, France", "UNESCO World Heritage Kinderdijk, Nederwaard, Kinderdijk, Netherlands",
            "Hoover Dam, Nevada", "Catherine Palace, Garden Street, Pushkin, Saint Petersburg, Russia", //72
            "Berlin Cathedral Church, Am Lustgarten, Berlin, Germany", "Helsinki Cathedral, Unionsgatan, Helsinki, Finland", "Tivoli Gardens, Vesterbrogade, København V, Denmark",
            "Bath, England, UK", "Brighton Pier, Madeira Drive, Brighton, UK", "Alhambra, Calle Real de la Alhambra, Granada, Spain",
            "Palais des Papes, Place du Palais, Avignon, France", "Pont du Gard, Route du Pont du Gard, Vers-Pont-du-Gard, France",
            "Fortress Hohensalzburg, Mönchsberg, Salzburg, Austria", "Prague Castle, Prague 1, Czechia", "Chapel Bridge, Kapellbrücke, Lucerne, Switzerland", //83
            "Piazza del Campo, Siena, Province of Siena, Italy", "Atomium, Square de l'Atomium, Brussels, Belgium", "Hollywood Walk of Fame, Sunset Boulevard, Hollywood, CA",
            "Winter Palace, Palace Embankment, Saint Petersburg, Russia", "Amalienborg Palace, Amalienborg Slotsplads, København K, Denmark",
            "The British Museum, Great Russell Street, London, UK", "Oxford University, Oxford, UK", "Sultan Ahmet Mahallesi, Blue Mosque, Atmeydanı Caddesi, Fatih/Istanbul, Turkey",
            "Piccadilly Circus, London, UK", "Trafalgar Square, Trafalgar Square, London, UK", "Millau Bridge, Millau Viaduct, Millau, France",
            "Liberty Bell, Philadelphia, PA", "Disneyland Park, Disneyland Drive, Anaheim, CA", "Fisherman's Wharf, San Francisco, CA",
            "Reunion Tower, Dallas, TX", "Lincoln Memorial, Washington, DC", "Roswell & Area 51, New Mexico"};
    private static double[] lat = {38.624696, 38.639861, 38.889517, 40.748448, 51.500731, 43.722955, 41.890213, 37.819962, 48.852975,
            48.858373, 35.658585, 41.902171, 41.403637, 55.692891, 48.873796, 52.535052, 51.178929, -25.344363, 27.175023,
            51.505461, 41.878878, 40.706096, 25.141319, 37.971546, 41.900929, 45.434037, 40.759016, 48.860639, 50.845027,
            51.501391, 47.557580, 43.772861, 55.948625, -13.163132, 43.642574, 25.197205, 51.508122, 48.636066, 36.082068,
            48.886706, 51.513855, 40.774321, 35.360834, 44.460497, 45.437986, 47.620550, 51.499429, 51.504631, 51.514502,
            25.699550, 52.516307, 50.941262, 38.871890, 41.882662, 30.328482, 41.008699, 31.239703, 55.679961, 43.767956,
            31.776725, 41.902952, 41.905993, 45.434061, 48.860649, 35.316842, 40.713037, 56.039038, 31.234735, 48.884129,
            51.888373, 36.016100, 59.716134, 52.519080, 60.170435, 55.673696, 51.381069, 50.816863, 37.176093, 43.950864,
            43.947605, 47.794960, 50.091131, 47.051655, 43.318402, 50.894968, 34.098561, 59.940412, 55.684089, 51.519447,
            51.754962, 41.005416, 51.510096, 51.508043, 44.077507, 39.94964, 33.812145, 37.807979, 32.775418, 38.889302,
            33.394963};
    private static double[] lon = {-90.184778, -90.294099, -77.035290, -73.985665, -0.124626, 10.396596, 12.492230, -122.478277, 2.349900,
            2.294480, 139.745432, 12.453936, 2.174355, 12.599275, 2.295024, 13.390168, -1.826215, 131.036863, 78.042155,
            -0.075357, -87.635916, -73.996866, 55.185346, 23.725744, 12.483294, 12.339034, -73.984473, 2.337633, 4.349989,
            -0.141901, 10.749797, 11.255691, -3.200839, -72.544966, -79.387057, 55.274373, -0.075953, -1.511120, -115.172789,
            2.343104, -0.098356, -73.970849, 138.727853, -110.828135, 12.335895, -122.349277, -0.127572,  -0.086976, -0.080308,
            32.639051, 13.377683, 6.958129, -77.056267, -87.623306, 35.444351, 28.980117, 121.499753, 12.590916, 11.253144,
            35.234505, 12.454483, 12.482773, 12.340854, 2.352241, 139.535754, -74.013169, 12.621144, 121.507537, 2.332249,
            4.636890, -114.737743, 30.395653, 13.401067, 24.952165, 12.568147, -2.359021, -0.136741, -3.588143, 4.807695,
            4.534960, 13.047656, 14.401606, 8.307532, 11.332159, 4.341526, -118.325575, 30.313785, 12.592999, -0.126957,
            -1.254442, 28.976812, -0.134568, -0.128070, 3.022811, -75.150282, -117.918974, -122.417732, -96.808961, -77.050187,
            -104.523168};

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
        for (int i = 0; i < 100; i++) {
            if (rng >= range && rng < range + 0.00999999) {
                landmark = i;
            }
            range += 0.00999999;
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
