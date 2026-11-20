package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.criteria.SemillaCriteria;
import com.tonin.animaltrack.model.Semilla;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.impl.RazaServiceImpl;
import com.tonin.animaltrack.service.impl.SemillaServiceImpl;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.SessionManager;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet("/controller/private/semilla")
public class SemillaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final SemillaServiceImpl semillaService = new SemillaServiceImpl();
    private final RazaServiceImpl razaService = new RazaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!allowed(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = FormValues.text(request, "action");
        if ("delete".equals(action) || "update".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            if ("edit".equals(action)) {
                Semilla semilla = semillaService.findById(FormValues.requiredId(request, "id", "semilla"));
                if (semilla == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("semilla", semilla);
                request.setAttribute("semillaForm", true);
            } else if ("create".equals(action)) {
                request.setAttribute("semillaForm", true);
            }
        } catch (Exception exception) {
            error(request, response, exception);
        }
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!allowed(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        if (!FormToken.valid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = FormValues.text(request, "action");
        if (!"create".equals(action) && !"update".equals(action) && !"delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            Long id = "create".equals(action) ? null : FormValues.requiredId(request, "id", "semilla");
            Semilla current = id == null ? null : semillaService.findById(id);
            if (id != null && current == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("semilla", current);
            if ("delete".equals(action)) {
                semillaService.delete(id);
            } else {
                // Mantiene los datos escritos si hay un error.
                request.setAttribute("semillaForm", true);
                request.setAttribute("semillaSubmitted", true);
                Semilla semilla = new Semilla();
                semilla.setId(id);
                semilla.setCodigo(FormValues.field(request, "codigo", "código", 40, true));
                semilla.setNombre(FormValues.field(request, "nombre", "nombre", 100, true));
                semilla.setDescripcion(FormValues.field(request, "descripcion", "descripción", 256, false));
                semilla.setRazaId(FormValues.id(request, "razaId"));
                if (semilla.getRazaId() != null && razaService.findById(semilla.getRazaId()) == null) {
                    throw new IllegalArgumentException("Selecciona una raza válida.");
                }
                Semilla existing = semillaService.findByCodigo(semilla.getCodigo());
                if (existing != null && !existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Ya existe una semilla con ese código.");
                }
                if (id == null) semillaService.create(semilla);
                else semillaService.update(semilla);
            }
            response.sendRedirect(request.getContextPath() + "/controller/private/semilla?saved=1");
            return;
        } catch (Exception exception) {
            error(request, response, exception);
        }
        show(request, response);
    }

    private void show(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("semillaPrepared", true);
        FormToken.prepare(request);
        try {
            request.setAttribute("razas", razaService.findAll());
            SemillaCriteria criteria = new SemillaCriteria();
            if ("GET".equals(request.getMethod())) {
                criteria.setCodigoLike(FormValues.text(request, "buscarCodigo"));
                criteria.setNombreLike(FormValues.text(request, "buscarNombre"));
                criteria.setRazaId(FormValues.id(request, "buscarRaza"));
            }
            request.setAttribute("semillas", semillaService.findByCriteria(criteria));
        } catch (Exception exception) {
            error(request, response, exception);
        }
        request.getRequestDispatcher(Views.SEMILLA).forward(request, response);
    }

    private boolean allowed(HttpServletRequest request) {
        UsuarioLoginDTO user = (UsuarioLoginDTO) SessionManager.getAttribute(request, AttributeNames.USER);
        return user != null && ("ADMINISTRADOR".equals(user.getRol()) || "VETERINARIO".equals(user.getRol()));
    }

    private void error(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("semillaError", exception.getMessage());
        } else if (exception instanceof SQLIntegrityConstraintViolationException) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            request.setAttribute("semillaError", "No se pudo guardar el cambio. Revisa el código y las relaciones de la semilla.");
        } else {
            getServletContext().log("Error al gestionar semillas.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("semillaError", "No se pudo completar la operación de semillas.");
        }
    }
}
