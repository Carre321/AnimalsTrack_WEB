package com.tonin.animalstrack.web.util;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionManager {
    public static HttpSession getSession(ServletRequest request) {
        return ((HttpServletRequest) request).getSession();
    }

    public static Object getAttribute(ServletRequest request, String attributeName) {
        return getSession(request).getAttribute(attributeName);
    }

    public static void setAttribute(ServletRequest request, String attributeName, Object value) {
        getSession(request).setAttribute(attributeName, value);
    }
}
