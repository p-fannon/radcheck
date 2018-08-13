package net.radcheck.radcheck.controllers;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.User;
import net.radcheck.radcheck.models.data.LatLonDao;
import net.radcheck.radcheck.models.data.UserDao;
import net.radcheck.radcheck.models.forms.AddLocationItemForm;
import net.radcheck.radcheck.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class UserController {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static String mapsKey = "AIzaSyAqvB0THWS44yHV3OOBzQQ0znAst9V6uQA";
    private static double angularDistance = 250 / 6371e3;

    @Autowired
    private UserService userService;

    @Autowired
    private LatLonDao locationRepository;

    @Autowired
    private UserDao userRepository;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model){
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Login");
        return "login";
    }
    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model){
        User user = new User();
        model.addAttribute("user", user);
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Register");
        return "registration";
    }
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String createNewUser(Model model, @ModelAttribute @Valid User user, Errors errors,
                                HttpServletRequest request) {
        String verify = request.getParameter("verify");
        if (!verify.equals(user.getPassword())) {
            String account = getUser();
            model.addAttribute("title", "Register");
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            return "redirect:registration?error=true";
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
            model.addAttribute("title", "Register");
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            return "registration";
        } else {
            userService.saveUser(user);
            model.addAttribute("successMessage", "User has been registered successfully");
            model.addAttribute("user", new User());
            model.addAttribute("title", "Success");
            String account = getUser();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
        }
        return "registration";
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
    public String userProfile(Model model) {
        User user = getAccount();
        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("title", "Your user profile");
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("names", user.getNames());
        model.addAttribute("user", user);

        return "user/profile";
    }
    @RequestMapping(value = "/confirm", method = RequestMethod.GET)
    public String confirmLocation(Model model, HttpSession session) {
        User user = getAccount();
        if (user.getLocations().size() > 19) {
            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Your user profile");
            model.addAttribute("user", user);
            session.removeAttribute("candidateLocation");
            return "redirect:/user/profile?max=true";
        }
        LatLon location = (LatLon) session.getAttribute("candidateLocation");
        if (user.getLocations().size() > 0) {
            List<LatLon> possibleDuplicates = checkDuplicates(location);
            if (possibleDuplicates.size() > 0) {
                String account = user.getEmail();
                model.addAttribute("account", account);
                model.addAttribute("isLoggedIn", checkAccount(account));
                model.addAttribute("title", "Duplicate locations found");
                model.addAttribute("user", user);
                model.addAttribute("locations", possibleDuplicates);
                model.addAttribute("key", mapsKey);
                session.removeAttribute("candidateLocation");
                return "duplicates";
            }
        }
        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Name and confirm this location");
        model.addAttribute("user", user);
        model.addAttribute("key", mapsKey);
        model.addAttribute("submitForm", new AddLocationItemForm(user, location));
        return "save-location";
    }
    @RequestMapping(value = "/confirm/{locationId}", method = RequestMethod.GET)
    public String confirmPersistentLocation(Model model, @PathVariable int locationId,
                                            HttpSession session) {
        User user = getAccount();
        if (user.getLocations().size() > 19) {
            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Your user profile");
            model.addAttribute("user", user);
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
                String account = user.getEmail();
                model.addAttribute("account", account);
                model.addAttribute("isLoggedIn", checkAccount(account));
                model.addAttribute("title", "Your user profile");
                model.addAttribute("user", user);
                session.setAttribute("locale", user.getNames().get(duplicateIndex));

                return "redirect:/user/profile?duplicate=true";
            }
        }
        session.setAttribute("candidateLocation", confirmLocation);
        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Name and confirm this location");
        model.addAttribute("user", user);
        model.addAttribute("key", mapsKey);
        model.addAttribute("submitForm", new AddLocationItemForm(user, confirmLocation));
        return "save-location";
    }
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String postConfirmation(Model model, @ModelAttribute @Valid AddLocationItemForm newForm,
                                   Errors errors, HttpSession session) {
        if (errors.hasErrors()) {
            LatLon aLocation = (LatLon) session.getAttribute("candidateLocation");
            User user = getAccount();
            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Name and confirm this location");
            model.addAttribute("user", user);
            model.addAttribute("key", mapsKey);
            model.addAttribute("submitForm", new AddLocationItemForm(user, aLocation));
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

            String account = user.getEmail();
            model.addAttribute("account", account);
            model.addAttribute("isLoggedIn", checkAccount(account));
            model.addAttribute("title", "Your user profile");
            model.addAttribute("user", user);
            session.removeAttribute("candidateLocation");

            return "redirect:/user/profile?success=true";
        }
        locationRepository.save(newLocation);
        session.setAttribute("locale", locationName);
        user.addLocation(newLocation, locationName);
        userRepository.save(user);

        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Your user profile");
        model.addAttribute("user", user);
        session.removeAttribute("candidateLocation");

        return "redirect:/user/profile?success=true";
    }

    public List<LatLon> checkDuplicates(LatLon userLocation) {
        double lat = Math.toRadians(userLocation.getLat());
        double lon = Math.toRadians(userLocation.getLon());
        double northLat = findBoundaryLat(lat, Math.toRadians(0.0));
        double southLat = findBoundaryLat(lat, Math.toRadians(180.0));
        double eastLon = findBoundaryLon(lat, lon, Math.toRadians(90.0));
        double westLon = findBoundaryLon(lat, lon, Math.toRadians(270.0));
        return locationRepository.findDuplicates(northLat, southLat, eastLon, westLon);
    }

    public double findBoundaryLat(double lat, double brng) {
        double diffLat = angularDistance * Math.cos( brng );
        double lat2 = lat + diffLat;
        return Math.toDegrees(lat2);
    }

    public double findBoundaryLon(double lat1, double lon, double brng) {
        double latDiff = angularDistance * Math.cos( brng );
        double lat2 = lat1 + latDiff;
        double pLatDiff = Math.log( Math.tan( lat2 / 2 + Math.PI / 4 ) / Math.tan( lat1 / 2 + Math.PI / 4 ));
        double q = Math.abs( pLatDiff ) > 10e-12 ? latDiff / pLatDiff : Math.cos( lat1 );
        double diffLon = angularDistance * Math.sin( brng ) / q;
        double lon2 = lon + diffLon;
        return Math.toDegrees(lon2);
    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
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
