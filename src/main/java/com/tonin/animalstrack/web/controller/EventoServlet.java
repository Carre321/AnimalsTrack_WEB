package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.Results;
import com.tonin.animaltrack.dao.criteria.AnimalCriteria;
import com.tonin.animaltrack.dao.criteria.EventoCriteria;
import com.tonin.animaltrack.model.Dosis;
import com.tonin.animaltrack.model.Evento;
import com.tonin.animaltrack.model.TipoEvento;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.model.dto.EventoDTO;
import com.tonin.animaltrack.service.impl.AnimalServiceImpl;
import com.tonin.animaltrack.service.impl.DosisServiceImpl;
import com.tonin.animaltrack.service.impl.EventoServiceImpl;
import com.tonin.animaltrack.service.impl.SemillaServiceImpl;
import com.tonin.animaltrack.service.impl.TipoEventoServiceImpl;
import com.tonin.animaltrack.service.impl.TratamientoServiceImpl;
import com.tonin.animaltrack.service.impl.VeterinarioServiceImpl;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@WebServlet("/controller/private/evento")
public class EventoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 20;

    private final EventoServiceImpl eventoService = new EventoServiceImpl();
    private final AnimalServiceImpl animalService = new AnimalServiceImpl();
    private final TipoEventoServiceImpl tipoService = new TipoEventoServiceImpl();
    private final VeterinarioServiceImpl veterinarioService = new VeterinarioServiceImpl();
    private final SemillaServiceImpl semillaService = new SemillaServiceImpl();
    private final TratamientoServiceImpl tratamientoService = new TratamientoServiceImpl();
    private final DosisServiceImpl dosisService = new DosisServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Long granjaId = selectedFarm(request);
        String action = action(request);
        String view = Views.EVENTO_SEARCH;
        request.setAttribute("eventoPrepared", true);

        try {
            if (action.equals(PValues.ACTION_SEARCH)) {
                if (granjaId != null) search(request, granjaId);
            } else if (isForm(action) || action.equals(PValues.ACTION_DETAIL)) {
                EventoDTO evento = null;
                if (!action.equals(PValues.ACTION_CREATE)) {
                    evento = findEvent(request, granjaId);
                    if (evento == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                }
                if (granjaId == null) {
                    response.sendRedirect(request.getContextPath() + "/controller/private/evento?action=search");
                    return;
                }
                request.setAttribute("evento", evento);
                request.setAttribute("eventoForm", isForm(action));
                if (isForm(action)) prepareForm(request, granjaId);
                view = Views.EVENTO_DETAIL;
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("eventoError", "Revisa los filtros, las fechas o el identificador.");
        } catch (Exception exception) {
            showError(request, response, exception);
        }
        request.getRequestDispatcher(view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        Long granjaId = selectedFarm(request);
        String action = action(request);

        if (request.getAttribute(AttributeNames.GRANJA_ERROR) != null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (!action.equals(PValues.ACTION_CREATE) && !action.equals(PValues.ACTION_UPDATE)
                && !action.equals(PValues.ACTION_DELETE)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        // Si se cambió de granja, toca volver a abrir la ficha.
        if (granjaId == null || !granjaId.toString().equals(request.getParameter(PNames.GRANJA_ID))) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Vuelve a abrir el evento en la granja seleccionada.");
            return;
        }

        EventoDTO current = null;
        boolean formLoaded = action.equals(PValues.ACTION_CREATE);
        request.setAttribute("eventoPrepared", true);
        try {
            if (!action.equals(PValues.ACTION_CREATE)) {
                current = findEvent(request, granjaId);
                if (current == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                formLoaded = true;
            }
            if (action.equals(PValues.ACTION_DELETE)) {
                eventoService.delete(current.getId(), granjaId);
                response.sendRedirect(request.getContextPath() + "/controller/private/evento?action=search");
                return;
            }
            Evento evento = readForm(request, granjaId);
            Long id;
            if (current == null) {
                id = eventoService.create(evento, granjaId).getId();
            } else {
                id = current.getId();
                evento.setId(id);
                eventoService.update(evento, granjaId);
            }
            response.sendRedirect(request.getContextPath() + "/controller/private/evento?action=detail&id=" + id + "&saved=1");
            return;
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("eventoError", exception.getMessage());
        } catch (Exception exception) {
            showError(request, response, exception);
        }

        request.setAttribute("evento", current);
        if (formLoaded && !action.equals(PValues.ACTION_DELETE)) {
            request.setAttribute("eventoForm", true);
            request.setAttribute("eventoSubmitted", true);
            try {
                prepareForm(request, granjaId);
            } catch (Exception exception) {
                showError(request, response, exception);
            }
        }
        request.getRequestDispatcher(Views.EVENTO_DETAIL).forward(request, response);
    }

    private void search(HttpServletRequest request, Long granjaId) throws Exception {
        request.setAttribute("tiposEvento", tipoService.findAll());
        request.setAttribute("veterinarios", veterinarioService.findAll());
        // Los filtros siempre se aplican dentro de la granja activa.
        EventoCriteria criteria = new EventoCriteria();
        criteria.setGranjaId(granjaId);
        criteria.setAnimalNombreLike(text(request, PNames.NOMBRE));
        criteria.setAnimalCrotalLike(text(request, PNames.CROTAL));
        criteria.setTipoEventoId(id(request, PNames.TIPO_EVENTO_ID));
        criteria.setVeterinarioId(id(request, PNames.VETERINARIO_ID));
        LocalDate desde = date(request, PNames.DESDE);
        LocalDate hasta = date(request, PNames.HASTA);
        if (desde != null && hasta != null && desde.isAfter(hasta)) throw new IllegalArgumentException();
        if (desde != null) criteria.setFechaDesde(desde.atStartOfDay());
        if (hasta != null) criteria.setFechaHasta(hasta.atTime(23, 59, 59));

        Long requested = id(request, PNames.PAGINA);
        int page = requested == null ? 1 : Math.toIntExact(Math.min(requested, 1 + (Integer.MAX_VALUE - 1) / PAGE_SIZE));
        Results<EventoDTO> results = eventoService.findByCriteria(criteria, (page - 1) * PAGE_SIZE + 1, PAGE_SIZE);
        int pages = Math.max(1, (int) ((results.getTotal() + PAGE_SIZE - 1L) / PAGE_SIZE));
        if (page > pages) {
            page = pages;
            results = eventoService.findByCriteria(criteria, (page - 1) * PAGE_SIZE + 1, PAGE_SIZE);
        }
        request.setAttribute(AttributeNames.RESULTS, results);
        request.setAttribute("pagina", page);
        request.setAttribute("paginas", pages);
    }

    private EventoDTO findEvent(HttpServletRequest request, Long granjaId) throws Exception {
        Long id = id(request, PNames.ID);
        if (id == null || granjaId == null) return null;
        EventoCriteria criteria = new EventoCriteria();
        criteria.setId(id);
        criteria.setGranjaId(granjaId);
        List<EventoDTO> events = eventoService.findByCriteria(criteria, 1, 1).getPageResults();
        return events.isEmpty() ? null : events.get(0);
    }

    private void prepareForm(HttpServletRequest request, Long granjaId) throws Exception {
        AnimalCriteria criteria = new AnimalCriteria();
        criteria.setGranjaId(granjaId);
        request.setAttribute("animales", animalService.findByCriteria(criteria));
        request.setAttribute("tiposEvento", tipoService.findAll());
        request.setAttribute("veterinarios", veterinarioService.findAll());
        request.setAttribute("semillas", semillaService.findAll());
        request.setAttribute("tratamientos", tratamientoService.findAll());
        request.setAttribute("dosis", dosisService.findAll());
    }

    private Evento readForm(HttpServletRequest request, Long granjaId) throws Exception {
        Evento evento = new Evento();
        Long animalId = id(request, PNames.ANIMAL_ID);
        AnimalDTO animal = animalId == null ? null : animalService.findById(animalId);
        if (animal == null || !granjaId.equals(animal.getGranjaId())) {
            throw new IllegalArgumentException("Selecciona un animal de esta granja.");
        }
        Long tipoId = id(request, PNames.TIPO_EVENTO_ID);
        TipoEvento tipo = tipoId == null ? null : tipoService.findById(tipoId);
        if (tipo == null) throw new IllegalArgumentException("Selecciona un tipo de evento.");
        evento.setAnimalId(animalId);
        evento.setTipoEventoId(tipoId);

        Long vet = id(request, PNames.VETERINARIO_ID);
        if (vet != null && veterinarioService.findById(vet) == null) {
            throw new IllegalArgumentException("Selecciona un veterinario válido.");
        }
        evento.setVeterinarioId(vet);

        try {
            LocalDate fecha = date(request, PNames.FECHA);
            if (fecha == null) throw new IllegalArgumentException("Introduce la fecha del evento.");
            String hora = text(request, PNames.HORA);
            evento.setFechaHora(fecha.atTime(hora == null ? LocalTime.MIDNIGHT : LocalTime.parse(hora)));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Revisa la fecha y la hora del evento.");
        }
        String precio = text(request, PNames.PRECIO);
        if (precio != null) {
            if (!precio.matches("[0-9]{1,8}([.,][0-9]{1,2})?")) {
                throw new IllegalArgumentException("Introduce un precio válido con hasta dos decimales.");
            }
            evento.setPrecioEvento(new BigDecimal(precio.replace(',', '.')));
        }
        evento.setObservaciones(limitedText(request, PNames.OBSERVACIONES, 500));

        // Guarda solo los campos que corresponden al tipo de evento.
        switch (tipo.getCodigo().toUpperCase(Locale.ROOT)) {
            case "REVISION", "PRENEZ" -> evento.setResultado(limitedText(request, PNames.RESULTADO, 30));
            case "IA" -> {
                Long semilla = id(request, PNames.SEMILLA_ID);
                if (semilla != null && semillaService.findById(semilla) == null) {
                    throw new IllegalArgumentException("Selecciona una semilla válida.");
                }
                evento.setSemillaId(semilla);
            }
            case "TRATAMIENTO" -> {
                Long tratamiento = id(request, PNames.TRATAMIENTO_ID);
                Long dosisId = id(request, PNames.DOSIS_ID);
                if (tratamiento != null && tratamientoService.findById(tratamiento) == null) {
                    throw new IllegalArgumentException("Selecciona un tratamiento válido.");
                }
                Dosis dosis = dosisId == null ? null : dosisService.findById(dosisId);
                if (dosisId != null && (dosis == null || tratamiento == null || !tratamiento.equals(dosis.getTratamientoId()))) {
                    throw new IllegalArgumentException("Selecciona una dosis del tratamiento elegido.");
                }
                evento.setTratamientoId(tratamiento);
                evento.setDosisId(dosisId);
            }
            default -> { }
        }
        return evento;
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        getServletContext().log("No se pudo completar la operación de eventos.", exception);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.setAttribute("eventoError", "No se pudo completar la operación. Inténtalo más tarde.");
    }

    private boolean isForm(String action) {
        return action.equals(PValues.ACTION_CREATE) || action.equals(PValues.ACTION_EDIT);
    }

    private String action(HttpServletRequest request) {
        String action = text(request, PNames.ACTION);
        return action == null ? PValues.ACTION_SEARCH : action.toLowerCase(Locale.ROOT);
    }

    private Long selectedFarm(HttpServletRequest request) {
        return request.getAttribute(AttributeNames.GRANJA_ERROR) == null
                ? (Long) SessionManager.getAttribute(request, AttributeNames.GRANJA_ID) : null;
    }

    private static Long id(HttpServletRequest request, String name) {
        String value = text(request, name);
        if (value == null) return null;
        try {
            long id = Long.parseLong(value);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Selecciona un valor válido.");
    }

    private static LocalDate date(HttpServletRequest request, String name) {
        String value = text(request, name);
        return value == null ? null : LocalDate.parse(value);
    }

    private static String limitedText(HttpServletRequest request, String name, int maxLength) {
        String value = text(request, name);
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException("El campo " + name + " admite hasta " + maxLength + " caracteres.");
        }
        return value;
    }

    private static String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
