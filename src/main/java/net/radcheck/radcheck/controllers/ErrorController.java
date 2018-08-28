package net.radcheck.radcheck.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

    private static final Logger errorLogger = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
            errorLogger.info("Error while serving a web page: " + statusCode);
        }
        model.addAttribute("title", "Uh oh");
        return "error";
    }
    @Override
    public String getErrorPath() {
        return "/error";
    }

}
