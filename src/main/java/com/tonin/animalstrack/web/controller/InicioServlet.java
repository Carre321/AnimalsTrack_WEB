package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.criteria.EventoCriteria;
import com.tonin.animaltrack.model.dto.EventoDTO;
import com.tonin.animaltrack.service.impl.EventoServiceImpl;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.SessionManager;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/controller/private/inicio")
public class InicioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final EventoServiceImpl eventoService = new EventoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("inicioPrepared", true);
        Long granjaId = (Long) SessionManager.getAttribute(request, AttributeNames.GRANJA_ID);
        if (granjaId != null && request.getAttribute(AttributeNames.GRANJA_ERROR) == null) {
            try {
                EventoCriteria criteria = new EventoCriteria();
                criteria.setGranjaId(granjaId);
                List<Map<String, Object>> rows = proximosPartos(eventoService.findByCriteria(criteria), LocalDate.now());
                long atrasados = rows.stream().filter(row -> (long) row.get("dias") < 0).count();
                request.setAttribute("partos", rows);
                request.setAttribute("atrasados", atrasados);
                request.setAttribute("proximos", rows.size() - atrasados);
            } catch (Exception exception) {
                getServletContext().log("No se pudo cargar el seguimiento reproductivo.", exception);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                request.setAttribute("inicioError", "No se pudo cargar el seguimiento reproductivo.");
            }
        }
        request.getRequestDispatcher(Views.INICIO_PRIVADO).forward(request, response);
    }

    private List<Map<String, Object>> proximosPartos(List<EventoDTO> eventos, LocalDate hoy) {
        Map<Long, List<EventoDTO>> porAnimal = new LinkedHashMap<>();
        for (EventoDTO evento : eventos) {
            if (evento.getAnimalId() != null && evento.getFechaHora() != null) {
                porAnimal.computeIfAbsent(evento.getAnimalId(), id -> new ArrayList<>()).add(evento);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        // Igual que en el Desktop, se parte de la última inseminación.
        for (List<EventoDTO> historial : porAnimal.values()) {
            historial.sort(Comparator.comparing(EventoDTO::getFechaHora).thenComparing(EventoDTO::getId));
            EventoDTO inseminacion = null;
            EventoDTO confirmacion = null;
            for (EventoDTO evento : historial) {
                if (tipo(evento, "IA") || tipo(evento, "IN")) {
                    inseminacion = evento;
                    confirmacion = null;
                } else if (inseminacion != null && evento.getFechaHora().isAfter(inseminacion.getFechaHora())) {
                    if (tipo(evento, "PARTO") || tipo(evento, "ABORTO")) {
                        inseminacion = null;
                        confirmacion = null;
                    } else if ((tipo(evento, "PRENEZ") || tipo(evento, "REVISION"))
                            && evento.getResultado() != null && "POSITIVO".equalsIgnoreCase(evento.getResultado().trim())) {
                        confirmacion = evento;
                    }
                }
            }
            if (inseminacion == null || confirmacion == null) continue;
            LocalDate fin = inseminacion.getFechaHora().toLocalDate().plusMonths(9);
            if (!fin.isAfter(hoy.plusDays(15))) {
                rows.add(Map.of("evento", inseminacion, "confirmacion", confirmacion, "fin", fin,
                        "dias", ChronoUnit.DAYS.between(hoy, fin)));
            }
        }
        rows.sort(Comparator.comparing(row -> (LocalDate) row.get("fin")));
        return rows;
    }

    private boolean tipo(EventoDTO evento, String codigo) {
        return codigo.equalsIgnoreCase(evento.getTipoEventoCodigo() == null
                ? evento.getTipoEventoNombre() : evento.getTipoEventoCodigo());
    }
}
