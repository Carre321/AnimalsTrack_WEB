package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.criteria.GranjaCriteria;
import com.tonin.animaltrack.model.Granja;
import com.tonin.animaltrack.model.Provincia;
import com.tonin.animaltrack.model.Municipio;
import com.tonin.animaltrack.model.dto.GranjaDTO;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.GranjaService;
import com.tonin.animaltrack.service.MunicipioService;
import com.tonin.animaltrack.service.ProvinciaService;
import com.tonin.animaltrack.service.impl.GranjaServiceImpl;
import com.tonin.animaltrack.service.impl.MunicipioServiceImpl;
import com.tonin.animaltrack.service.impl.ProvinciaServiceImpl;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.PNames;
import com.tonin.animalstrack.web.util.PValues;
import com.tonin.animalstrack.web.util.SessionManager;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/controller/private/granja")
public class GranjaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final GranjaService granjaService;
    private final MunicipioService municipioService;
    private final ProvinciaService provinciaService;

    public GranjaServlet() {
        this(new GranjaServiceImpl(), new MunicipioServiceImpl(), new ProvinciaServiceImpl());
    }

    GranjaServlet(GranjaService granjaService, MunicipioService municipioService, ProvinciaService provinciaService) {
        this.granjaService = granjaService;
        this.municipioService = municipioService;
        this.provinciaService = provinciaService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (PValues.ACTION_SELECT.equalsIgnoreCase(request.getParameter(PNames.ACTION))) {
            // Cambia de granja desde el selector.
            if (!"POST".equals(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            select(request, response);
            return;
        }
        // Solo un ganadero puede crear una granja.
        UsuarioLoginDTO user = (UsuarioLoginDTO) SessionManager.getAttribute(request, AttributeNames.USER);
        if (user == null || !"GANADERO".equalsIgnoreCase(user.getRol()) || user.getGanaderoId() == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if ("POST".equals(request.getMethod()) && request.getAttribute(AttributeNames.GRANJA_ERROR) == null
                && create(request, response, user)) return;
        prepareForm(request, response);
        request.getRequestDispatcher(Views.GRANJA_CREATE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @SuppressWarnings("unchecked")
    private void select(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getAttribute(AttributeNames.GRANJA_ERROR) != null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "No se pudieron cargar las granjas.");
            return;
        }
        Long selected;
        try {
            selected = Long.valueOf(request.getParameter(PNames.GRANJA_ID));
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Selecciona una granja válida.");
            return;
        }
        // Comprueba que el usuario tiene acceso a la granja elegida.
        List<GranjaDTO> granjas = (List<GranjaDTO>) SessionManager.getAttribute(request, AttributeNames.GRANJAS);
        if (granjas != null) {
            for (GranjaDTO granja : granjas) {
                if (selected.equals(granja.getId())) {
                    SessionManager.setAttribute(request, AttributeNames.GRANJA_ID, selected);
                    String section = request.getParameter(PNames.SECCION);
                    String route = "inicio".equals(section) ? "inicio"
                            : "evento".equals(section) ? "evento?action=search" : "animal?action=search";
                    response.sendRedirect(request.getContextPath() + "/controller/private/" + route);
                    return;
                }
            }
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes acceso a esa granja.");
    }

    private boolean create(HttpServletRequest request, HttpServletResponse response, UsuarioLoginDTO user) throws IOException {
        try {
            Granja granja = new Granja();
            granja.setRega(required(request, PNames.REGA, "REGA", 20));
            granja.setNombre(required(request, PNames.NOMBRE, "nombre", 60));
            granja.setDireccion(required(request, PNames.DIRECCION, "dirección", 256));
            String postal = request.getParameter(PNames.CODIGO_POSTAL);
            postal = postal == null || postal.trim().isEmpty() ? null : postal.trim();
            if (postal != null && postal.length() > 10) throw new IllegalArgumentException("El código postal admite hasta 10 caracteres.");
            granja.setCodigoPostal(postal);
            Long municipioId;
            try {
                municipioId = Long.valueOf(request.getParameter(PNames.MUNICIPIO_ID));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Selecciona un municipio válido.");
            }
            if (municipioId <= 0 || municipioService.findById(municipioId) == null) {
                throw new IllegalArgumentException("Selecciona un municipio válido.");
            }
            granja.setMunicipioId(municipioId);
            // La granja queda a nombre del ganadero conectado.
            granja.setGanaderoId(user.getGanaderoId());
            GranjaCriteria criteria = new GranjaCriteria();
            criteria.setRega(granja.getRega());
            if (!granjaService.findByCriteria(criteria).isEmpty()) {
                throw new IllegalArgumentException("Ya existe una granja con ese REGA.");
            }
            GranjaDTO saved = granjaService.create(granja);
            if (saved == null || saved.getId() == null) throw new IllegalStateException("No se pudo crear la granja.");
            // Deja seleccionada la granja que se acaba de crear.
            SessionManager.setAttribute(request, AttributeNames.GRANJA_ID, saved.getId());
            response.sendRedirect(request.getContextPath() + "/controller/private/animal?action=search&created=1");
            return true;
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("granjaFormError", exception.getMessage());
        } catch (Exception exception) {
            if (exception instanceof SQLException && ((SQLException) exception).getErrorCode() == 1062) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("granjaFormError", "Ya existe una granja con ese REGA.");
            } else {
                getServletContext().log("No se pudo crear la granja.", exception);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                request.setAttribute("granjaFormError", "No se pudo crear la granja. Inténtalo más tarde.");
            }
        }
        return false;
    }

    private String required(HttpServletRequest request, String parameter, String label, int maxLength) {
        String value = request.getParameter(parameter);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Introduce " + label + ".");
        value = value.trim();
        if (value.length() > maxLength) throw new IllegalArgumentException("El campo " + label + " admite hasta " + maxLength + " caracteres.");
        return value;
    }

    private void prepareForm(HttpServletRequest request, HttpServletResponse response) {
        // Carga las opciones del campo municipio.
        request.setAttribute("granjaFormPrepared", true);
        try {
            Map<Long, String> provincias = new HashMap<>();
            for (Provincia provincia : provinciaService.findAll()) provincias.put(provincia.getId(), provincia.getNombre());
            List<Municipio> municipios = municipioService.findAll();
            if (municipios == null || municipios.isEmpty()) throw new IllegalStateException("No hay municipios disponibles.");
            request.setAttribute("municipios", municipios);
            request.setAttribute("provincias", provincias);
        } catch (Exception exception) {
            getServletContext().log("No se pudieron cargar los municipios de la granja.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("granjaFormError", "No se pudo cargar el formulario. Inténtalo más tarde.");
        }
    }
}
