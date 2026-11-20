package com.tonin.animalstrack.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

final class FormToken {
    private FormToken() { }

    static void prepare(HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (session) {
            if (session.getAttribute("formToken") == null) {
                session.setAttribute("formToken", UUID.randomUUID().toString());
            }
        }
    }

    static boolean valid(HttpServletRequest request) {
        Object token = request.getSession().getAttribute("formToken");
        return token != null && token.equals(request.getParameter("formToken"));
    }
}
