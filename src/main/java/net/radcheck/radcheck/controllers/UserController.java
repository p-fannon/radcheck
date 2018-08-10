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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class UserController {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static String mapsKey = "AIzaSyAqvB0THWS44yHV3OOBzQQ0znAst9V6uQA";

    @Autowired
    private UserService userService;

    @Autowired
    private LatLonDao latLonDao;

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
        String account = user.getEmail();
        LatLon location = (LatLon) session.getAttribute("candidateLocation");
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Name and confirm this location");
        model.addAttribute("user", user);
        model.addAttribute("key", mapsKey);
        model.addAttribute("submitForm", new AddLocationItemForm(user, location));
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
        LatLon newLocation = (LatLon) session.getAttribute("candidateLocation");
        String locationName = newForm.getLocationName();
        latLonDao.save(newLocation);
        session.setAttribute("locale", locationName);
        user.addLocation(newLocation, locationName);
        userService.saveUser(user);

        String account = user.getEmail();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Your user profile");
        model.addAttribute("user", user);
        session.removeAttribute("candidateLocation");

        return "redirect:/user/profile/?success=true";
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
