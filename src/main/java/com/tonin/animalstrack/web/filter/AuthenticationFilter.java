package com.tonin.animalstrack.web.filter;

import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.model.dto.GranjaDTO;
import com.tonin.animaltrack.model.VeterinarioGranja;
import com.tonin.animaltrack.service.GranjaService;
import com.tonin.animaltrack.service.VeterinarioGranjaService;
import com.tonin.animaltrack.service.impl.GranjaServiceImpl;
import com.tonin.animaltrack.service.impl.VeterinarioGranjaServiceImpl;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.SessionManager;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthenticationFilter extends HttpFilter {
    private static final long serialVersionUID = 1L;
    private final GranjaService granjaService;
    private final VeterinarioGranjaService veterinarioGranjaService;

    public AuthenticationFilter() {
        this(new GranjaServiceImpl(), new VeterinarioGranjaServiceImpl());
    }

    public AuthenticationFilter(GranjaService granjaService, VeterinarioGranjaService veterinarioGranjaService) {
        this.granjaService = granjaService;
        this.veterinarioGranjaService = veterinarioGranjaService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        UsuarioLoginDTO user = (UsuarioLoginDTO) SessionManager.getAttribute(request, AttributeNames.USER);
        if (user != null) {
            if (request.getAttribute(AttributeNames.GRANJAS_PREPARED) == null) {
                prepareGranjas(request, response, user);
            }
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect(httpRequest.getContextPath() + Views.USUARIO_LOGIN);
        }
    }

    private void prepareGranjas(ServletRequest request, ServletResponse response, UsuarioLoginDTO user) {
        request.setAttribute(AttributeNames.GRANJAS_PREPARED, true);
        List<GranjaDTO> granjas = new ArrayList<>();
        try {
            if ("ADMINISTRADOR".equalsIgnoreCase(user.getRol())) {
                granjas = granjaService.findAll();
            } else if (user.getGanaderoId() != null) {
                granjas = granjaService.findByGanaderoId(user.getGanaderoId());
            } else if (user.getVeterinarioId() != null) {
                Map<Long, GranjaDTO> disponibles = new LinkedHashMap<>();
                for (VeterinarioGranja relacion : veterinarioGranjaService.findByVeterinarioId(user.getVeterinarioId())) {
                    GranjaDTO granja = granjaService.findById(relacion.getGranjaId());
                    if (granja != null) disponibles.put(granja.getId(), granja);
                }
                granjas = new ArrayList<>(disponibles.values());
            }
            if (granjas == null) granjas = new ArrayList<>();
        } catch (Exception exception) {
            ((HttpServletRequest) request).getServletContext().log("No se pudieron cargar las granjas.", exception);
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute(AttributeNames.GRANJA_ERROR, "No se pudieron cargar las granjas. Inténtalo más tarde.");
            granjas = new ArrayList<>();
        }
        Long selected = (Long) SessionManager.getAttribute(request, AttributeNames.GRANJA_ID);
        boolean available = false;
        for (GranjaDTO granja : granjas) {
            if (granja.getId().equals(selected)) available = true;
        }
        if (!available) selected = granjas.isEmpty() ? null : granjas.get(0).getId();
        SessionManager.setAttribute(request, AttributeNames.GRANJAS, granjas);
        SessionManager.setAttribute(request, AttributeNames.GRANJA_ID, selected);
    }
}
