package com.tonin.animalstrack.web.controller;

import jakarta.servlet.http.HttpServletRequest;

final class FormValues {
    private FormValues() { }

    static String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    static String field(HttpServletRequest request, String name, String label, int max, boolean required) {
        String value = text(request, name);
        if (value == null && required) throw new IllegalArgumentException("Introduce " + label + ".");
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException("El campo " + label + " admite hasta " + max + " caracteres.");
        }
        return value;
    }

    static Long id(HttpServletRequest request, String name) {
        String value = text(request, name);
        if (value == null) return null;
        try {
            long id = Long.parseLong(value);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Selecciona un valor válido.");
    }

    static Long requiredId(HttpServletRequest request, String name, String label) {
        Long id = id(request, name);
        if (id == null) throw new IllegalArgumentException("Selecciona " + label + ".");
        return id;
    }
}
