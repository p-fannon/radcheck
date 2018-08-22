package net.radcheck.radcheck.controllers;

import net.radcheck.radcheck.models.User;
import net.radcheck.radcheck.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

    private static final Logger errorLogger = LoggerFactory.getLogger(ErrorController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
            errorLogger.info("Error while serving a web page: " + statusCode);
        }
        String account = getUser();
        model.addAttribute("account", account);
        model.addAttribute("isLoggedIn", checkAccount(account));
        model.addAttribute("title", "Uh oh");
        return "error";
    }
    @Override
    public String getErrorPath() {
        return "/error";
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
