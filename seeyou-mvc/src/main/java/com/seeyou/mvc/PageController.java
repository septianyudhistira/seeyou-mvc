package com.seeyou.mvc;

import com.seeyou.logging.MyLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */
public abstract class PageController {
    private String viewPath;

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public void handleRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

    }

    public void redirect(HttpServletResponse response, String path) {
        try {
            response.sendRedirect(path);
            response.setStatus(307);
        } catch (IOException var4) {
            MyLogger.error(var4.getMessage(), var4);
        }
    }

}
