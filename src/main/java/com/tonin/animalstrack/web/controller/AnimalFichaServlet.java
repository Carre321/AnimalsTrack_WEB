package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.criteria.AnimalCriteria;
import com.tonin.animaltrack.dao.criteria.EventoCriteria;
import com.tonin.animaltrack.model.Animal;
import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.impl.AnimalServiceImpl;
import com.tonin.animaltrack.service.impl.ContentServiceFileImpl;
import com.tonin.animaltrack.service.impl.EventoServiceImpl;
import com.tonin.animaltrack.service.impl.RazaServiceImpl;
import com.tonin.animaltrack.service.impl.SexoServiceImpl;
import com.tonin.animaltrack.utils.PhotoUtils;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.SessionManager;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@WebServlet("/controller/private/animal-ficha")
@MultipartConfig(fileSizeThreshold = 1048576, maxFileSize = PhotoUtils.MAX_BYTES,
        maxRequestSize = PhotoUtils.MAX_BYTES + 1048576)
public class AnimalFichaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnimalServiceImpl animales = new AnimalServiceImpl();
    private final SexoServiceImpl sexos = new SexoServiceImpl();
    private final RazaServiceImpl razas = new RazaServiceImpl();
    private final EventoServiceImpl eventos = new EventoServiceImpl();
    private final ContentServiceFileImpl contenidos = new ContentServiceFileImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String action = request.getParameter("action");
        boolean photo = "photo".equals(action);
        if (!photo && !"create".equals(action) && !"edit".equals(action) && !"detail".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Long farm = farm(request);
        if (farm == null || (!photo && !"detail".equals(action) && !canEdit(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            AnimalDTO current = "create".equals(action) ? null : animal(request, farm);
            if (!"create".equals(action) && current == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (photo) {
                sendPhoto(response, current.getId());
                return;
            }
            show(request, response, farm, current, !"detail".equals(action));
        } catch (IllegalArgumentException exception) {
            response.sendError(photo ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception exception) {
            getServletContext().log("No se pudo abrir la ficha del animal.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        if (!canEdit(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Long farm = farm(request);
        AnimalDTO current = null;
        boolean edit = false;
        try {
            if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
                try {
                    request.getParts();
                } catch (IllegalStateException exception) {
                    response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "La foto supera el tamaño permitido.");
                    return;
                }
            }
            if (!FormToken.valid(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (farm == null || !farm.toString().equals(request.getParameter("granjaId"))) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Vuelve a abrir la ficha en la granja seleccionada.");
                return;
            }
            String action = request.getParameter("action");
            boolean create = "create".equals(action);
            edit = create || "update".equals(action);
            current = create ? null : animal(request, farm);
            if (!create && current == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Long id;
            if (edit || "baja".equals(action)) {
                Animal animal = "baja".equals(action) ? copy(current) : readAnimal(request, farm, current);
                if ("baja".equals(action)) {
                    if (animal.getFechaBaja() == null) animal.setFechaBaja(Date.valueOf(LocalDate.now()));
                }
                if (create) id = animales.create(animal).getId();
                else {
                    id = current.getId();
                    if (!animales.update(animal)) throw new IllegalArgumentException("El animal ya no está disponible.");
                }
            } else if ("updatePhoto".equals(action) || "removePhoto".equals(action)) {
                id = current.getId();
                updatePhoto(request, id, "removePhoto".equals(action));
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/controller/private/animal-ficha?action=detail&id=" + id + "&saved=1");
            return;
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("animalError", exception.getMessage());
        } catch (Exception exception) {
            getServletContext().log("No se pudo guardar la ficha del animal.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("animalError", "No se pudo guardar el cambio.");
        }
        if (current == null && !edit) {
            response.sendError(response.getStatus());
            return;
        }
        request.setAttribute("animalSubmitted", edit);
        try {
            show(request, response, farm, current, edit);
        } catch (Exception exception) {
            getServletContext().log("No se pudo preparar la ficha del animal.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, Long farm, AnimalDTO current, boolean edit)
            throws Exception {
        request.setAttribute("fichaPrepared", true);
        request.setAttribute("animal", current);
        request.setAttribute("animalEdit", edit);
        request.setAttribute("canEditAnimal", canEdit(request));
        FormToken.prepare(request);
        if (edit) {
            request.setAttribute("sexos", sexos.findAll());
            request.setAttribute("razas", razas.findAll());
            AnimalCriteria criteria = new AnimalCriteria();
            criteria.setGranjaId(farm);
            criteria.setIncludeHistory(true);
            request.setAttribute("progenitores", animales.findByCriteria(criteria));
        } else {
            request.setAttribute("hasAnimalPhoto", !photos(current.getId()).isEmpty());
            EventoCriteria criteria = new EventoCriteria();
            criteria.setAnimalId(current.getId());
            criteria.setGranjaId(farm);
            request.setAttribute("eventos", eventos.findByCriteria(criteria, 1, 5).getPageResults());
        }
        request.getRequestDispatcher(Views.ANIMAL_FICHA).forward(request, response);
    }

    private Animal readAnimal(HttpServletRequest request, Long farm, AnimalDTO current) throws Exception {
        Animal animal = current == null ? new Animal() : copy(current);
        animal.setGranjaId(farm);
        animal.setCrotal(FormValues.field(request, "crotal", "crotal", 30, true));
        animal.setNombre(FormValues.field(request, "nombre", "nombre", 80, false));
        animal.setSexoId(FormValues.requiredId(request, "sexoId", "un sexo"));
        animal.setRazaId(FormValues.id(request, "razaId"));
        animal.setFechaNacimiento(date(request, "fechaNacimiento"));
        if (current != null) animal.setFechaBaja(date(request, "fechaBaja"));
        animal.setMadreInternaId(parent(request, "madreInternaId", farm, animal.getId(), animal.getMadreInternaId()));
        animal.setPadreInternoId(parent(request, "padreInternoId", farm, animal.getId(), animal.getPadreInternoId()));
        animal.setMadreExternaCrotal(FormValues.field(request, "madreExternaCrotal", "crotal de madre externa", 45, false));
        return animal;
    }

    private Long parent(HttpServletRequest request, String field, Long farm, Long current, Long previous) throws Exception {
        Long id = FormValues.id(request, field);
        if (id == null || id.equals(previous)) return id;
        AnimalDTO parent = animales.findById(id);
        if (id.equals(current) || parent == null || !farm.equals(parent.getGranjaId())) {
            throw new IllegalArgumentException("Selecciona un progenitor válido de esta granja.");
        }
        return id;
    }

    private Animal copy(AnimalDTO current) {
        Animal animal = new Animal();
        animal.setId(current.getId());
        animal.setNombre(current.getNombre());
        animal.setCrotal(current.getCrotal());
        animal.setGranjaId(current.getGranjaId());
        animal.setSexoId(current.getSexoId());
        animal.setRazaId(current.getRazaId());
        animal.setFechaNacimiento(current.getFechaNacimiento());
        animal.setFechaBaja(current.getFechaBaja());
        animal.setMadreInternaId(current.getMadreInternaId());
        animal.setMadreExternaCrotal(current.getMadreExternaCrotal());
        animal.setPadreInternoId(current.getPadreInternoId());
        animal.setEventPartoId(current.getEventPartoId());
        return animal;
    }

    private Date date(HttpServletRequest request, String name) {
        String value = FormValues.text(request, name);
        if (value == null) return null;
        try { return Date.valueOf(LocalDate.parse(value)); }
        catch (java.time.DateTimeException exception) { throw new IllegalArgumentException("Revisa las fechas del animal."); }
    }

    private void sendPhoto(HttpServletResponse response, Long id) throws Exception {
        List<ContentMetadata> photos = photos(id);
        ContentData photo = photos.size() == 1 ? contenidos.get(photos.get(0)) : null;
        if (photo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String format = PhotoUtils.format(photo.getRaw());
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentType("jpg".equals(format) ? "image/jpeg" : "image/" + format);
        response.getOutputStream().write(photo.getRaw());
    }

    private void updatePhoto(HttpServletRequest request, Long id, boolean remove) throws Exception {
        List<ContentMetadata> previous = photos(id);
        String format = null;
        if (!remove) {
            Part part = request.getPart("foto");
            if (part == null || part.getSize() == 0) throw new IllegalArgumentException("Selecciona una foto.");
            try (InputStream input = part.getInputStream()) {
                byte[] bytes = input.readNBytes(PhotoUtils.MAX_BYTES + 1);
                format = PhotoUtils.format(bytes);
                ContentMetadata metadata = new ContentMetadata();
                metadata.setEntityType("animal");
                metadata.setEntityId(id.toString());
                metadata.setResourceId("foto");
                metadata.setFormat(format);
                ContentData data = new ContentData();
                data.setMetadata(metadata);
                data.setRaw(bytes);
                contenidos.save(data);
            } finally {
                part.delete();
            }
        }
        // Guarda primero la nueva foto y luego quita el formato anterior.
        for (ContentMetadata old : previous) {
            if (!old.getFormat().equalsIgnoreCase(format)) contenidos.remove(old);
        }
    }

    private List<ContentMetadata> photos(Long id) throws Exception {
        return contenidos.listMetadata("animal", id.toString()).stream()
                .filter(metadata -> "foto".equals(metadata.getResourceId())).toList();
    }

    private AnimalDTO animal(HttpServletRequest request, Long farm) throws Exception {
        Long id = FormValues.requiredId(request, "id", "un animal");
        AnimalDTO current = animales.findById(id);
        return current != null && Objects.equals(farm, current.getGranjaId()) ? current : null;
    }

    private Long farm(HttpServletRequest request) {
        return request.getAttribute(AttributeNames.GRANJA_ERROR) == null
                ? (Long) SessionManager.getAttribute(request, AttributeNames.GRANJA_ID) : null;
    }

    static boolean canEdit(HttpServletRequest request) {
        UsuarioLoginDTO user = (UsuarioLoginDTO) SessionManager.getAttribute(request, AttributeNames.USER);
        return user != null && ("ADMINISTRADOR".equals(user.getRol()) || "GANADERO".equals(user.getRol()));
    }
}
