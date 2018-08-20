package net.radcheck.radcheck.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
            System.out.println(statusCode);
        }
        return "error";
    }
    @Override
    public String getErrorPath() {
        return "/error";
    }
}
