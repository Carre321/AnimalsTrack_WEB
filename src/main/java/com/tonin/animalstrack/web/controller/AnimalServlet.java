package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.Results;
import com.tonin.animaltrack.dao.criteria.AnimalCriteria;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.service.AnimalService;
import com.tonin.animaltrack.service.impl.AnimalServiceImpl;
import com.tonin.animaltrack.service.impl.RazaServiceImpl;
import com.tonin.animaltrack.service.impl.SexoServiceImpl;
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

@WebServlet("/controller/private/animal")
public class AnimalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnimalService animalService;

    public AnimalServlet() {
        this(new AnimalServiceImpl());
    }

    AnimalServlet(AnimalService animalService) {
        this.animalService = animalService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter(PNames.ACTION);
        String view = Views.ANIMAL_SEARCH;
        try {
            Long granjaId = (Long) SessionManager.getAttribute(request, AttributeNames.GRANJA_ID);
            if (request.getAttribute(AttributeNames.GRANJA_ERROR) != null) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                request.getRequestDispatcher(view).forward(request, response);
                return;
            }
            if (PValues.ACTION_DETAIL.equalsIgnoreCase(action)) {
                // Muestra el detalle si el animal es de la granja seleccionada.
                AnimalDTO animal = animalService.findById(id(request));
                if (animal == null || granjaId == null || !granjaId.equals(animal.getGranjaId())) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("animal", animal);
                view = Views.ANIMAL_DETAIL;
            } else if (PValues.ACTION_DELETE.equalsIgnoreCase(action)) {
                if (!"POST".equals(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                if (!AnimalFichaServlet.canEdit(request) || !FormToken.valid(request)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Comprueba la granja del animal antes de borrarlo.
                AnimalDTO animal = animalService.findById(id(request));
                if (animal == null || granjaId == null || !granjaId.equals(animal.getGranjaId())) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                if (animalService.delete(id(request))) {
                    response.sendRedirect(request.getContextPath() + "/controller/private/animal?action=search");
                    return;
                }
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("animalError", "No se ha encontrado el animal que quieres eliminar.");
            } else {
                // Busca por nombre o crotal dentro de la granja seleccionada.
                AnimalCriteria criteria = new AnimalCriteria();
                criteria.setNombreLike(trimToNull(request.getParameter(PNames.NOMBRE)));
                criteria.setCrotalLike(trimToNull(request.getParameter(PNames.CROTAL)));
                criteria.setGranjaId(granjaId);
                criteria.setSexoId(FormValues.id(request, "sexoId"));
                criteria.setRazaId(FormValues.id(request, "razaId"));
                criteria.setIncludeHistory("1".equals(request.getParameter("historial")));
                Long requested = FormValues.id(request, "pagina");
                int page = requested == null ? 1 : (int) Math.min(requested, 1 + (Integer.MAX_VALUE - 1) / 20);
                Results<AnimalDTO> results = granjaId == null ? new Results<>() : animalService.findByCriteria(criteria, (page - 1) * 20 + 1, 20);
                int pages = Math.max(1, (int) ((results.getTotal() + 19L) / 20));
                if (page > pages) {
                    page = pages;
                    if (granjaId != null) results = animalService.findByCriteria(criteria, (page - 1) * 20 + 1, 20);
                }
                request.setAttribute("pagina", page);
                request.setAttribute("paginas", pages);
                request.setAttribute(AttributeNames.RESULTS, results);
            }
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("animalError", "El identificador del animal no es válido.");
        } catch (Exception exception) {
            getServletContext().log("No se pudo completar la operación de animales.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("animalError", "No se pudo completar la operación. Inténtalo más tarde.");
        }
        if (Views.ANIMAL_SEARCH.equals(view) && request.getSession().getAttribute(AttributeNames.USER) != null
                && !PValues.ACTION_DELETE.equalsIgnoreCase(action)) {
            request.setAttribute("canEditAnimal", AnimalFichaServlet.canEdit(request));
            FormToken.prepare(request);
            if (request.getAttribute(AttributeNames.GRANJA_ERROR) == null) {
                try {
                    request.setAttribute("sexos", new SexoServiceImpl().findAll());
                    request.setAttribute("razas", new RazaServiceImpl().findAll());
                } catch (Exception exception) {
                    getServletContext().log("No se pudieron cargar los filtros de animales.", exception);
                    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    request.setAttribute("animalError", "No se pudieron cargar los filtros de animales.");
                }
            }
        }
        // Abre la JSP con los datos de la consulta.
        request.getRequestDispatcher(view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private Long id(HttpServletRequest request) {
        long value = Long.parseLong(request.getParameter(PNames.ID));
        if (value <= 0) throw new IllegalArgumentException("Invalid animal id");
        return value;
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
