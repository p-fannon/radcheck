package net.radcheck.radcheck.controllers;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.Role;
import net.radcheck.radcheck.models.User;
import net.radcheck.radcheck.models.data.LatLonDao;
import net.radcheck.radcheck.models.data.RoleDao;
import net.radcheck.radcheck.models.data.UserDao;
import net.radcheck.radcheck.models.forms.AddLocationItemForm;
import net.radcheck.radcheck.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class UserController {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Logger userLogger = LoggerFactory.getLogger(UserController.class);
    private static String mapsKey = "AIzaSyAqvB0THWS44yHV3OOBzQQ0znAst9V6uQA";
    private static double angularDistance = 250 / 6371e3;
    private static long twentyOneHours = 75600000L;

    @Autowired
    private UserService userService;

    @Autowired
    private LatLonDao locationRepository;

    @Autowired
    private UserDao userRepository;

    @Autowired
    private RoleDao roleRepository;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, @CookieValue(value="donate", defaultValue="0") String donateCookie,
                        HttpServletResponse response, HttpServletRequest request){
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Login");
        return "login";
    }
    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model,
                               @CookieValue(value="donate", defaultValue="0") String donateCookie,
                               HttpServletResponse response, HttpServletRequest request,
                               @CookieValue(value="consent", defaultValue="0") String consentCookie){
        boolean needToAsk;
        if (Integer.parseInt(consentCookie) == 0) {
            needToAsk = true;
            Cookie consent = new Cookie("consent", "1");
            consent.setMaxAge(60 * 60 * 24 * 365 * 10);
            consent.setHttpOnly(true);
            consent.setPath("/");
            response.addCookie(consent);
        } else {
            needToAsk = false;
        }
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        User user = new User();
        model.addAttribute("consent", needToAsk);
        model.addAttribute("user", user);
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Register");
        return "registration";
    }
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String createNewUser(Model model, @ModelAttribute @Valid User user, Errors errors,
                                @RequestParam (defaultValue = "false") boolean agree,
                                @CookieValue(value="donate", defaultValue="0") String donateCookie,
                                HttpServletResponse response, HttpServletRequest request) {
        if (!agree) {
            String account = getUser();
            model.addAttribute("consentMessage", "You must consent to the Privacy Policy " +
                    "and cookies to make an account");
            model.addAttribute("title", "Register");
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            return "registration";
        }
        String verify = user.getVerify();
        if (!verify.equals(user.getPassword())) {
            errors.rejectValue("verify", "error.user",
                    "Your passwords did not match. Please verify your password.");
        }
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists != null) {
            errors.rejectValue("email", "error.user",
                    "There is already a user registered with the email provided");
        }
        boolean isValid = validate(user.getEmail());
        if (!isValid) {
            errors.rejectValue("email", "error.user",
                        "Please provide a valid email address");
        }
        if (errors.hasErrors()) {
            String account = getUser();
            userLogger.info("Error during user registration: " + errors.getAllErrors().toString());
            model.addAttribute("title", "Register");
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            return "registration";
        } else {
            userService.saveUser(user);
            int donateCount = Integer.parseInt(donateCookie);
            boolean askDonate = checkDonate(donateCount);
            if (donateCount < 11) {
                response.addCookie(updateDonation(donateCount, request.getCookies()));
            }
            model.addAttribute("donate", askDonate);
            model.addAttribute("successMessage", "User has been registered successfully");
            model.addAttribute("user", new User());
            model.addAttribute("title", "Success");
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
        }
        return "registration";
    }
    @RequestMapping(value = "/privacy", method = RequestMethod.GET)
    public String privacy(Model model, @CookieValue(value="donate", defaultValue="0") String donateCookie,
                          HttpServletResponse response, HttpServletRequest request) {
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        String account = getUser();
        model.addAttribute("title", "Privacy Policy");
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));

        return "privacy";
    }
    @RequestMapping(value = "/admin/home", method = RequestMethod.GET)
    public String home(Model model){
        User user = getAccount();
        String account = user.getEmail();
        model.addAttribute("userName", "Welcome to your account at " + account);
        model.addAttribute("adminMessage","Content Available Only for Users with Admin Role");
        model.addAttribute("title", "Admin");
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        return "admin/home";
    }
    @RequestMapping(value = "/user/profile", method = RequestMethod.GET)
    public String userProfile(Model model, @CookieValue(value="donate", defaultValue="0") String donateCookie,
                              HttpServletResponse response, HttpServletRequest request) {
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        User user = getAccount();
        String account = user.getEmail();
        Instant rightNow = Instant.now();
        Timestamp refresher = Timestamp.from(rightNow.minusMillis(twentyOneHours));
        model.addAttribute("account", account);
        model.addAttribute("title", "Your user profile");
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("names", user.getNames());
        model.addAttribute("user", user);
        model.addAttribute("refresh", refresher);

        return "user/profile";
    }
    @RequestMapping(value = "/confirm", method = RequestMethod.GET)
    public String confirmLocation(Model model, HttpSession session,
                                  @CookieValue(value="donate", defaultValue="0") String donateCookie,
                                  HttpServletResponse response, HttpServletRequest request) {
        User user = getAccount();
        if (user.getLocations().size() > 19 && user.getRoles().equals(new HashSet<Role>(Arrays.asList(roleRepository.findByRole("USER"))))) {
            session.removeAttribute("candidateLocation");
            return "redirect:/user/profile?max=true";
        }
        LatLon location = (LatLon) session.getAttribute("candidateLocation");
        if (user.getLocations().size() > 0) {
            List<LatLon> possibleDuplicates = checkDuplicates(location);
            if (possibleDuplicates.size() > 0) {
                int donateCount = Integer.parseInt(donateCookie);
                boolean askDonate = checkDonate(donateCount);
                if (donateCount < 11) {
                    response.addCookie(updateDonation(donateCount, request.getCookies()));
                }
                model.addAttribute("donate", askDonate);
                String account = user.getEmail();
                model.addAttribute("account", account);
                model.addAttribute("isLoggedIn", checkAccount(account));
                model.addAttribute("title", "Duplicate locations found");
                model.addAttribute("locations", possibleDuplicates);
                model.addAttribute("key", mapsKey);
                session.removeAttribute("candidateLocation");
                return "duplicates";
            }
        }
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Name and confirm this location");
        model.addAttribute("key", mapsKey);
        model.addAttribute("submitForm", new AddLocationItemForm(location));
        return "save-location";
    }
    @RequestMapping(value = "/confirm/{locationId}", method = RequestMethod.GET)
    public String confirmPersistentLocation(Model model, @PathVariable int locationId,
                                            HttpSession session, @CookieValue(value="donate", defaultValue="0") String donateCookie,
                                            HttpServletResponse response, HttpServletRequest request) {
        User user = getAccount();
        if (user.getLocations().size() > 19) {
            return "redirect:/user/profile?max=true";
        }
        LatLon confirmLocation = locationRepository.findOne(locationId);
        List<LatLon> userLocations = user.getLocations();
        if (userLocations.size() > 0) {
            boolean isInAccount = false;
            int duplicateIndex = 0;
            for (LatLon location : userLocations) {
                if (location.getId() == confirmLocation.getId()) {
                    isInAccount = true;
                    duplicateIndex = userLocations.indexOf(location);
                }
            }
            if (isInAccount) {
                session.setAttribute("locale", user.getNames().get(duplicateIndex));

                return "redirect:/user/profile?duplicate=true";
            }
        }
        session.setAttribute("candidateLocation", confirmLocation);
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Name and confirm this location");
        model.addAttribute("key", mapsKey);
        model.addAttribute("submitForm", new AddLocationItemForm(confirmLocation));
        return "save-location";
    }
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String postConfirmation(@ModelAttribute @Valid AddLocationItemForm newForm,
                                   Errors errors, HttpSession session) {
        if (errors.hasErrors()) {
            userLogger.info("Error during location saving: " + errors.getAllErrors().toString());
            return "redirect:/confirm?error=true";
        }
        User user = getAccount();
        String locationName = newForm.getLocationName();
        LatLon newLocation = (LatLon) session.getAttribute("candidateLocation");
        if (newLocation.getId() != 0) {
            LatLon confirmLocation = locationRepository.findOne(newLocation.getId());
            session.setAttribute("locale", locationName);
            user.addLocation(confirmLocation, locationName);
            userRepository.save(user);
            session.removeAttribute("candidateLocation");

            return "redirect:/user/profile?success=true";
        }
        locationRepository.save(newLocation);
        session.setAttribute("locale", locationName);
        user.addLocation(newLocation, locationName);
        userRepository.save(user);
        session.removeAttribute("candidateLocation");

        return "redirect:/user/profile?success=true";
    }
    @RequestMapping(value = "/edit/{locationId}", method=RequestMethod.GET)
    public String editLocationName(Model model, @PathVariable int locationId,
                                   HttpSession session, @CookieValue(value="donate", defaultValue="0") String donateCookie,
                                   HttpServletResponse response, HttpServletRequest request) {
        User user = getAccount();
        LatLon editLocation = locationRepository.findOne(locationId);
        int donateCount = Integer.parseInt(donateCookie);
        boolean askDonate = checkDonate(donateCount);
        if (donateCount < 11) {
            response.addCookie(updateDonation(donateCount, request.getCookies()));
        }
        model.addAttribute("donate", askDonate);
        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Rename this location");
        model.addAttribute("key", mapsKey);
        model.addAttribute("submitForm", new AddLocationItemForm(editLocation));
        session.setAttribute("editLocation", editLocation);
        return "edit-location";
    }
    @RequestMapping(value = "/edit", method=RequestMethod.POST)
    public String confirmEditLocation(@ModelAttribute @Valid AddLocationItemForm editForm,
                                      Errors errors, HttpSession session) {
        if (errors.hasErrors()) {
            userLogger.info("Error during location editing: " + errors.getAllErrors().toString());
            LatLon aLocation = (LatLon) session.getAttribute("editLocation");
            return "redirect:/edit/" + aLocation.getId() + "?error=true";
        }
        User user = getAccount();
        LatLon updatedLocation = (LatLon) session.getAttribute("editLocation");
        String locationName = editForm.getLocationName();
        session.setAttribute("locale", locationName);
        user.editLocation(updatedLocation, locationName);
        userRepository.save(user);
        session.removeAttribute("editLocation");

        return "redirect:/user/profile?success=true";
    }
    @RequestMapping(value = "/delete/{locationId}", method=RequestMethod.GET)
    public String removeLocation(@PathVariable int locationId, HttpSession session) {
        User user = getAccount();
        LatLon deleteLocation = locationRepository.findOne(locationId);
        session.setAttribute("locale", user.getNames().get(user.getLocations().indexOf(deleteLocation)));
        user.removeLocation(deleteLocation);
        userRepository.save(user);

        return "redirect:/user/profile?remove=true";
    }

    private List<LatLon> checkDuplicates(LatLon userLocation) {
        double lat = Math.toRadians(userLocation.getLat());
        double lon = Math.toRadians(userLocation.getLon());
        double northLat = findBoundaryLat(lat, Math.toRadians(0.0));
        double southLat = findBoundaryLat(lat, Math.toRadians(180.0));
        double eastLon = findBoundaryLon(lat, lon, Math.toRadians(90.0));
        double westLon = findBoundaryLon(lat, lon, Math.toRadians(270.0));
        return locationRepository.findDuplicates(northLat, southLat, eastLon, westLon);
    }

    private double findBoundaryLat(double lat, double brng) {
        double diffLat = angularDistance * Math.cos( brng );
        double lat2 = lat + diffLat;
        return Math.toDegrees(lat2);
    }

    private double findBoundaryLon(double lat1, double lon, double brng) {
        double latDiff = angularDistance * Math.cos( brng );
        double lat2 = lat1 + latDiff;
        double pLatDiff = Math.log( Math.tan( lat2 / 2 + Math.PI / 4 ) / Math.tan( lat1 / 2 + Math.PI / 4 ));
        double q = Math.abs( pLatDiff ) > 10e-12 ? latDiff / pLatDiff : Math.cos( lat1 );
        double diffLon = angularDistance * Math.sin( brng ) / q;
        double lon2 = lon + diffLon;
        return Math.toDegrees(lon2);
    }

    private static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
    private boolean checkDonate(int cookie) {
        boolean needToAsk;
        if (cookie < 10) {
            needToAsk = false;
        } else if (cookie == 10) {
            needToAsk = true;
        } else {
            needToAsk = false;
        }
        return needToAsk;
    }
    private Cookie updateDonation(int count, Cookie[] cookies) {
        if (count == 0) {
            Cookie donate = new Cookie("donate", Integer.toString(count + 1));
            donate.setMaxAge(60 * 60 * 24 * 180);
            donate.setHttpOnly(true);
            donate.setPath("/");
            return donate;
        } else {
            for (Cookie entry : cookies) {
                if (entry.getName().equals("donate")) {
                    if (Integer.parseInt(entry.getValue()) < 11) {
                        entry.setValue(Integer.toString(count + 1));
                        entry.setMaxAge(60 * 60 * 24 * 180);
                        entry.setHttpOnly(true);
                        entry.setPath("/");
                        return entry;
                    } else {
                        entry.setMaxAge(60 * 60 * 24 * 180);
                        entry.setHttpOnly(true);
                        entry.setPath("/");
                        return entry;
                    }
                }
            }
        }
        return new Cookie("donate", "1");
    }
    private User getAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        return user;
    }
    private String getUser() {
        String account = "";
        User theUser = getAccount();
        if (theUser != null) {
            account = theUser.getEmail();
        }
        return account;
    }
    private boolean checkAccount(String account) {
        boolean isLoggedIn = false;
        if (!account.equals("")) {
            isLoggedIn = true;
        }
        return isLoggedIn;
    }
}
