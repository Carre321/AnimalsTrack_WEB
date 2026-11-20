package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.criteria.AnimalCriteria;
import com.tonin.animaltrack.model.AnimalSemilla;
import com.tonin.animaltrack.model.Ganadero;
import com.tonin.animaltrack.model.Granja;
import com.tonin.animaltrack.model.Veterinario;
import com.tonin.animaltrack.model.VeterinarioGranja;
import com.tonin.animaltrack.model.dto.GranjaDTO;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.impl.AdministracionUsuarioService;
import com.tonin.animaltrack.service.impl.AnimalSemillaServiceImpl;
import com.tonin.animaltrack.service.impl.AnimalServiceImpl;
import com.tonin.animaltrack.service.impl.GanaderoServiceImpl;
import com.tonin.animaltrack.service.impl.GranjaServiceImpl;
import com.tonin.animaltrack.service.impl.MunicipioServiceImpl;
import com.tonin.animaltrack.service.impl.ProvinciaServiceImpl;
import com.tonin.animaltrack.service.impl.SemillaServiceImpl;
import com.tonin.animaltrack.service.impl.VeterinarioGranjaServiceImpl;
import com.tonin.animaltrack.service.impl.VeterinarioServiceImpl;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.SessionManager;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/controller/private/admin")
public class AdministracionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AdministracionUsuarioService usuarios = new AdministracionUsuarioService();
    private final GanaderoServiceImpl ganaderos = new GanaderoServiceImpl();
    private final VeterinarioServiceImpl veterinarios = new VeterinarioServiceImpl();
    private final GranjaServiceImpl granjas = new GranjaServiceImpl();
    private final MunicipioServiceImpl municipios = new MunicipioServiceImpl();
    private final ProvinciaServiceImpl provincias = new ProvinciaServiceImpl();
    private final AnimalServiceImpl animales = new AnimalServiceImpl();
    private final SemillaServiceImpl semillas = new SemillaServiceImpl();
    private final VeterinarioGranjaServiceImpl veterinarioGranja = new VeterinarioGranjaServiceImpl();
    private final AnimalSemillaServiceImpl animalSemilla = new AnimalSemillaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String section = section(request);
        if ("save".equals(request.getParameter("action")) || "delete".equals(request.getParameter("action"))) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        show(request, response, section);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        if (!isAdmin(request) || !FormToken.valid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String section = section(request);
        String action = request.getParameter("action");
        if (!"save".equals(action) && !"delete".equals(action)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        boolean delete = "delete".equals(action);
        try {
            switch (section) {
                case "granjas" -> saveFarm(request, delete);
                case "relaciones" -> saveRelation(request, delete);
                default -> saveUser(request, delete);
            }
            response.sendRedirect(request.getContextPath() + "/controller/private/admin?panel=" + section
                    + "&tipo=" + personType(request) + "&relacion=" + relation(request)
                    + ("relaciones".equals(section) ? "&primero=" + FormValues.id(request, "primero") : "") + "&saved=1");
            return;
        } catch (Exception exception) {
            error(request, response, exception);
        }
        request.setAttribute("adminSubmitted", !delete);
        show(request, response, section);
    }

    private void show(HttpServletRequest request, HttpServletResponse response, String section)
            throws IOException, ServletException {
        request.setAttribute("adminPrepared", true);
        request.setAttribute("panel", section);
        request.setAttribute("tipoPersona", personType(request));
        request.setAttribute("relacion", relation(request));
        FormToken.prepare(request);
        try {
            if ("relaciones".equals(section)) {
                prepareRelations(request);
            } else {
                Map<Long, String> names = new LinkedHashMap<>();
                provincias.findAll().forEach(provincia -> names.put(provincia.getId(), provincia.getNombre()));
                request.setAttribute("provincias", names);
                request.setAttribute("municipios", municipios.findAll());
                Long id = FormValues.id(request, "id");
                if ("granjas".equals(section)) {
                    request.setAttribute("filas", granjas.findAll());
                    if (id != null) request.setAttribute("ficha", granjas.findById(id));
                } else if ("veterinario".equals(personType(request))) {
                    request.setAttribute("filas", veterinarios.findAll());
                    if (id != null) request.setAttribute("ficha", veterinarios.findById(id));
                } else {
                    request.setAttribute("filas", ganaderos.findAll());
                    if (id != null) request.setAttribute("ficha", ganaderos.findById(id));
                }
                if (id != null && request.getAttribute("ficha") == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    request.setAttribute("adminError", "No se ha encontrado el registro.");
                }
            }
        } catch (Exception exception) {
            error(request, response, exception);
        }
        request.getRequestDispatcher(Views.ADMIN).forward(request, response);
    }

    private void saveUser(HttpServletRequest request, boolean delete) throws Exception {
        boolean vet = "veterinario".equals(personType(request));
        Long id = FormValues.id(request, "id");
        if (delete) {
            if (id == null) throw new IllegalArgumentException("Selecciona un usuario.");
            usuarios.delete(id, vet);
            return;
        }
        String nombre = FormValues.field(request, "nombre", "nombre", 60, true);
        String apellidos = FormValues.field(request, "apellidos", "apellidos", 80, true);
        String dni = FormValues.field(request, "dni", "DNI o NIE", 9, true);
        String email = FormValues.field(request, "email", "correo electrónico", 200, true);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Introduce un correo electrónico válido.");
        }
        String password = request.getParameter("password");
        if (password != null && password.isEmpty()) password = null;
        if (password != null && (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y ocupar como máximo 72 bytes.");
        }
        String telefono = FormValues.field(request, "telefono", "teléfono", 20, false);
        String direccion = FormValues.field(request, "direccion", "dirección", 256, false);
        String postal = FormValues.field(request, "codigoPostal", "código postal", 10, false);
        Long municipioId = municipio(request);

        if (vet) {
            Veterinario ficha = new Veterinario();
            ficha.setId(id);
            ficha.setCodigo(FormValues.field(request, "codigo", "código", 20, false));
            ficha.setDni(dni);
            ficha.setNombre(nombre);
            ficha.setApellidos(apellidos);
            ficha.setEmail(email);
            ficha.setTelefono(telefono);
            ficha.setDireccion(direccion);
            ficha.setCodigoPostal(postal);
            ficha.setMunicipioId(municipioId);
            usuarios.save(null, ficha, password);
        } else {
            Ganadero ficha = new Ganadero();
            ficha.setId(id);
            ficha.setDni(dni);
            ficha.setNombre(nombre);
            ficha.setApellidos(apellidos);
            ficha.setEmail(email);
            ficha.setTelefono(telefono);
            ficha.setDireccion(direccion);
            ficha.setCodigoPostal(postal);
            ficha.setMunicipioId(municipioId);
            usuarios.save(ficha, null, password);
        }
    }

    private void saveFarm(HttpServletRequest request, boolean delete) throws Exception {
        Long id = FormValues.id(request, "id");
        GranjaDTO current = id == null ? null : granjas.findById(id);
        if (id != null && current == null) throw new IllegalArgumentException("La granja ya no existe.");
        if (delete) {
            if (id == null) throw new IllegalArgumentException("Selecciona una granja.");
            granjas.delete(id);
            return;
        }
        Granja granja = new Granja();
        granja.setId(id);
        granja.setRega(FormValues.field(request, "rega", "REGA", 20, true));
        granja.setNombre(FormValues.field(request, "nombre", "nombre", 60, true));
        granja.setDireccion(FormValues.field(request, "direccion", "dirección", 256, true));
        granja.setCodigoPostal(FormValues.field(request, "codigoPostal", "código postal", 10, false));
        granja.setMunicipioId(municipio(request));
        if (current == null) granjas.create(granja);
        else {
            granja.setGanaderoId(current.getGanaderoId());
            granjas.update(granja);
        }
    }

    private void prepareRelations(HttpServletRequest request) throws Exception {
        String relation = relation(request);
        Long first = FormValues.id(request, "primero");
        Map<Long, String> left = new LinkedHashMap<>();
        Map<Long, String> right = new LinkedHashMap<>();
        Map<Long, String> linked = new LinkedHashMap<>();

        switch (relation) {
            case "veterinario" -> {
                veterinarios.findAll().forEach(item -> left.put(item.getId(), item.getNombreCompleto()));
                granjas.findAll().forEach(item -> right.put(item.getId(), item.getNombre()));
                if (first != null) veterinarioGranja.findByVeterinarioId(first).forEach(item ->
                        linked.put(item.getGranjaId(), right.get(item.getGranjaId())));
            }
            case "ganadero" -> {
                ganaderos.findAll().forEach(item -> left.put(item.getId(), item.getNombreCompleto()));
                granjas.findAll().forEach(item -> right.put(item.getId(), item.getNombre()));
                if (first != null) granjas.findByGanaderoId(first).forEach(item -> linked.put(item.getId(), item.getNombre()));
            }
            default -> {
                AnimalCriteria criteria = new AnimalCriteria();
                criteria.setIncludeHistory(true);
                animales.findByCriteria(criteria).forEach(item -> left.put(item.getId(), item.getCrotal()
                        + (item.getNombre() == null ? "" : " · " + item.getNombre())));
                semillas.findAll().forEach(item -> right.put(item.getId(), item.getCodigo() + " · " + item.getNombre()));
                if (first != null) animalSemilla.findByAnimalId(first).forEach(item ->
                        linked.put(item.getSemillaId(), right.get(item.getSemillaId())));
            }
        }
        request.setAttribute("primeros", left);
        request.setAttribute("segundos", right);
        request.setAttribute("relaciones", linked);
        request.setAttribute("primero", first);
        request.setAttribute("nombrePrimero", left.get(first));
    }

    private void saveRelation(HttpServletRequest request, boolean delete) throws Exception {
        Long first = FormValues.requiredId(request, "primero", "el primer registro");
        Long second = FormValues.requiredId(request, "segundo", "el segundo registro");

        switch (relation(request)) {
            case "veterinario" -> {
                if (delete) veterinarioGranja.delete(first, second);
                else {
                    VeterinarioGranja value = new VeterinarioGranja();
                    value.setVeterinarioId(first);
                    value.setGranjaId(second);
                    veterinarioGranja.create(value);
                }
            }
            case "ganadero" -> {
                GranjaDTO current = granjas.findById(second);
                if (current == null || ganaderos.findById(first) == null
                        || (delete && !first.equals(current.getGanaderoId()))) {
                    throw new IllegalArgumentException("Selecciona una relación válida.");
                }
                Granja value = new Granja();
                value.setId(current.getId());
                value.setRega(current.getRega());
                value.setNombre(current.getNombre());
                value.setDireccion(current.getDireccion());
                value.setCodigoPostal(current.getCodigoPostal());
                value.setMunicipioId(current.getMunicipioId());
                value.setGanaderoId(delete ? null : first);
                granjas.update(value);
            }
            default -> {
                if (delete) animalSemilla.delete(first, second);
                else {
                    AnimalSemilla value = new AnimalSemilla();
                    value.setAnimalId(first);
                    value.setSemillaId(second);
                    animalSemilla.create(value);
                }
            }
        }
    }

    private Long municipio(HttpServletRequest request) throws Exception {
        Long id = FormValues.requiredId(request, "municipioId", "un municipio");
        if (municipios.findById(id) == null) throw new IllegalArgumentException("Selecciona un municipio válido.");
        return id;
    }

    private void error(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("adminError", exception.getMessage());
        } else if (exception instanceof SQLIntegrityConstraintViolationException) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            request.setAttribute("adminError", "No se pudo guardar el cambio. Revisa los duplicados y registros relacionados.");
        } else {
            getServletContext().log("Error en Administración.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("adminError", "No se pudo completar la operación.");
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        UsuarioLoginDTO user = (UsuarioLoginDTO) SessionManager.getAttribute(request, AttributeNames.USER);
        return user != null && "ADMINISTRADOR".equals(user.getRol());
    }

    private String section(HttpServletRequest request) {
        String value = request.getParameter("panel");
        return "granjas".equals(value) || "relaciones".equals(value) ? value : "usuarios";
    }

    private String personType(HttpServletRequest request) {
        return "veterinario".equals(request.getParameter("tipo")) ? "veterinario" : "ganadero";
    }

    private String relation(HttpServletRequest request) {
        String value = request.getParameter("relacion");
        return "ganadero".equals(value) || "animal".equals(value) ? value : "veterinario";
    }
}
