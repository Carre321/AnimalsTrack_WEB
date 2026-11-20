package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.criteria.GranjaCriteria;
import com.tonin.animaltrack.model.Granja;
import com.tonin.animaltrack.model.Municipio;
import com.tonin.animaltrack.model.Provincia;
import com.tonin.animaltrack.model.VeterinarioGranja;
import com.tonin.animaltrack.model.dto.GranjaDTO;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.GranjaService;
import com.tonin.animaltrack.service.MunicipioService;
import com.tonin.animaltrack.service.ProvinciaService;
import com.tonin.animaltrack.service.VeterinarioGranjaService;
import com.tonin.animalstrack.web.filter.AuthenticationFilter;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class FarmWorkflowTest {
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, Object> sessionAttributes = new HashMap<>();
    private final UsuarioLoginDTO user = new UsuarioLoginDTO();
    private List<GranjaDTO> farms = new ArrayList<>(Arrays.asList(farm(10), farm(20)));
    private String method = "POST";
    private int status = 200;
    private String redirect;
    private String forwarded;
    private int creates;
    private int reads;
    private boolean unavailable;
    private boolean duplicate;
    private boolean unknownMunicipio;
    private Exception creationFailure;
    private Granja saved;
    private String lookup;

    public FarmWorkflowTest() {
        user.setRol("GANADERO");
        user.setGanaderoId(5L);
        sessionAttributes.put("user", user);
    }

    @Test
    public void farmerLoadsOnlyOwnFarmsAndSelectsFirst() throws Exception {
        filter();
        assertEquals("findByGanaderoId", lookup);
        assertEquals(10L, sessionAttributes.get("granjaId"));
        assertEquals(farms, sessionAttributes.get("granjas"));
    }

    @Test
    public void selectionIsKeptAcrossPrivateRequests() throws Exception {
        sessionAttributes.put("granjaId", 20L);
        filter();
        assertEquals(20L, sessionAttributes.get("granjaId"));
    }

    @Test
    public void revokedSelectionIsReplacedByAnAvailableFarm() throws Exception {
        sessionAttributes.put("granjaId", 99L);
        filter();
        assertEquals(10L, sessionAttributes.get("granjaId"));
    }

    @Test
    public void emptyFarmListClearsThePreviousSelection() throws Exception {
        farms.clear();
        sessionAttributes.put("granjaId", 99L);
        filter();
        assertNull(sessionAttributes.get("granjaId"));
    }

    @Test
    public void administratorUsesTheSameAllFarmsLookupAsDesktop() throws Exception {
        user.setRol("ADMINISTRADOR");
        filter();
        assertEquals("findAll", lookup);
    }

    @Test
    public void veterinarianLoadsOnlyAssignedFarms() throws Exception {
        user.setRol("VETERINARIO");
        user.setGanaderoId(null);
        user.setVeterinarioId(8L);
        filter();
        assertEquals("findById", lookup);
        List<?> visible = (List<?>) sessionAttributes.get("granjas");
        assertEquals(1, visible.size());
        assertEquals(20L, sessionAttributes.get("granjaId"));
    }

    @Test
    public void farmLoadFailureClearsStaleAccessAndReportsAnError() throws Exception {
        unavailable = true;
        sessionAttributes.put("granjaId", 10L);
        filter();
        assertEquals(503, status);
        assertNull(sessionAttributes.get("granjaId"));
        assertNotNull(attributes.get("granjaError"));
    }

    @Test
    public void forwardDoesNotLoadFarmListTwice() throws Exception {
        filter();
        filter();
        assertEquals(1, reads);
    }

    @Test
    public void selectorAcceptsAnAvailableFarm() throws Exception {
        filter();
        parameters.put("action", "select");
        parameters.put("granjaId", "20");
        servlet().doPost(request(), response());
        assertEquals(20L, sessionAttributes.get("granjaId"));
        assertEquals("/AnimalsTrack/controller/private/animal?action=search", redirect);
    }

    @Test
    public void selectorRejectsAnotherOwnersFarm() throws Exception {
        filter();
        parameters.put("action", "select");
        parameters.put("granjaId", "99");
        servlet().doPost(request(), response());
        assertEquals(403, status);
        assertEquals(10L, sessionAttributes.get("granjaId"));
    }

    @Test
    public void selectorRejectsInvalidInputAndDoesNotChangeSelection() throws Exception {
        filter();
        parameters.put("action", "select");
        parameters.put("granjaId", "invalid");
        servlet().doPost(request(), response());
        assertEquals(400, status);
        assertEquals(10L, sessionAttributes.get("granjaId"));
    }

    @Test
    public void openingFormLoadsLocationsWithoutCreatingAnything() throws Exception {
        method = "GET";
        servlet().doGet(request(), response());
        assertEquals(0, creates);
        assertNotNull(attributes.get("municipios"));
        assertEquals(Views.GRANJA_CREATE, forwarded);
    }

    @Test
    public void creationUsesSessionOwnerAndSelectsTheNewFarm() throws Exception {
        fields();
        parameters.put("ganaderoId", "999");
        servlet().doPost(request(), response());
        assertEquals(1, creates);
        assertEquals(Long.valueOf(5), saved.getGanaderoId());
        assertEquals("Granja de María", saved.getNombre());
        assertNull(saved.getCodigoPostal());
        assertEquals(30L, sessionAttributes.get("granjaId"));
        assertTrue(redirect.endsWith("action=search&created=1"));
    }

    @Test
    public void requiredFieldsAreValidatedBeforeCreation() throws Exception {
        fields();
        parameters.put("rega", " ");
        servlet().doPost(request(), response());
        assertEquals(0, creates);
        assertEquals(400, status);
        assertEquals(Views.GRANJA_CREATE, forwarded);
    }

    @Test
    public void invalidAndNonexistentMunicipalitiesCannotBeSaved() throws Exception {
        fields();
        parameters.put("municipioId", "not-a-number");
        servlet().doPost(request(), response());
        assertEquals(400, status);
        assertEquals(0, creates);
        parameters.put("municipioId", "9999");
        unknownMunicipio = true;
        servlet().doPost(request(), response());
        assertEquals(400, status);
        assertEquals(0, creates);
    }

    @Test
    public void duplicateRegaCannotBeSaved() throws Exception {
        fields();
        duplicate = true;
        servlet().doPost(request(), response());
        assertEquals(400, status);
        assertEquals(0, creates);
        assertEquals("Ya existe una granja con ese REGA.", attributes.get("granjaFormError"));
    }

    @Test
    public void concurrentDuplicateInsertShowsTheSameValidationMessage() throws Exception {
        fields();
        creationFailure = new SQLException("Duplicate", "23000", 1062);
        servlet().doPost(request(), response());
        assertEquals(400, status);
        assertEquals("Ya existe una granja con ese REGA.", attributes.get("granjaFormError"));
        assertNull(redirect);
    }

    @Test
    public void failedCreationDoesNotSelectOrReportANewFarm() throws Exception {
        fields();
        sessionAttributes.put("granjaId", 10L);
        creationFailure = new SQLException("Fixture failure");
        servlet().doPost(request(), response());
        assertEquals(503, status);
        assertEquals(10L, sessionAttributes.get("granjaId"));
        assertNull(redirect);
    }

    @Test
    public void veterinarianCannotCreateAFarmByPostingTheForm() throws Exception {
        fields();
        user.setRol("VETERINARIO");
        servlet().doPost(request(), response());
        assertEquals(403, status);
        assertEquals(0, creates);
    }

    @Test
    public void getRequestDoesNotChangeSelection() throws Exception {
        parameters.put("action", "select");
        parameters.put("granjaId", "20");
        method = "GET";
        servlet().doGet(request(), response());
        assertEquals(405, status);
        assertNull(sessionAttributes.get("granjaId"));
    }

    private void fields() {
        parameters.put("action", "create");
        parameters.put("rega", "TEST-REGA");
        parameters.put("nombre", " Granja de María ");
        parameters.put("direccion", " Camino de pruebas ");
        parameters.put("codigoPostal", " ");
        parameters.put("municipioId", "900001");
    }

    private void filter() throws Exception {
        new AuthenticationFilter(farmService(), proxy(VeterinarioGranjaService.class, (name, arguments) -> {
            assertEquals(8L, arguments[0]);
            VeterinarioGranja relacion = new VeterinarioGranja();
            relacion.setGranjaId(20L);
            return Collections.singletonList(relacion);
        })).doFilter(request(), response(), (req, res) -> { });
    }

    private GranjaService farmService() {
        return proxy(GranjaService.class, (name, arguments) -> {
            if ("create".equals(name)) {
                creates++;
                if (creationFailure != null) throw creationFailure;
                saved = (Granja) arguments[0];
                return farm(30);
            }
            if ("findByCriteria".equals(name)) {
                assertEquals("TEST-REGA", ((GranjaCriteria) arguments[0]).getRega());
                return duplicate ? Collections.singletonList(farm(30)) : Collections.emptyList();
            }
            reads++;
            lookup = name;
            if (unavailable) throw new SQLException("Fixture unavailable");
            if ("findByGanaderoId".equals(name)) assertEquals(5L, arguments[0]);
            if ("findById".equals(name)) return farm((Long) arguments[0]);
            return farms;
        });
    }

    private GranjaServlet servlet() throws Exception {
        GranjaServlet servlet = new GranjaServlet(farmService(), proxy(MunicipioService.class, (name, arguments) -> {
            Municipio municipio = new Municipio();
            municipio.setId(900001L);
            municipio.setProvinciaId(900001L);
            return "findById".equals(name) ? (unknownMunicipio ? null : municipio) : Collections.singletonList(municipio);
        }), proxy(ProvinciaService.class, (name, arguments) -> Collections.singletonList(new Provincia())));
        ServletContext context = proxy(ServletContext.class, (name, arguments) -> null);
        servlet.init(proxy(ServletConfig.class, (name, arguments) -> "getServletContext".equals(name) ? context : null));
        return servlet;
    }

    private static GranjaDTO farm(long id) {
        GranjaDTO farm = new GranjaDTO();
        farm.setId(id);
        return farm;
    }

    private HttpServletRequest request() {
        HttpSession session = proxy(HttpSession.class, (name, arguments) -> {
            if ("getAttribute".equals(name)) return sessionAttributes.get(arguments[0]);
            if ("setAttribute".equals(name)) sessionAttributes.put((String) arguments[0], arguments[1]);
            return null;
        });
        return proxy(HttpServletRequest.class, (name, arguments) -> {
            if ("getMethod".equals(name)) return method;
            if ("getParameter".equals(name)) return parameters.get(arguments[0]);
            if ("getAttribute".equals(name)) return attributes.get(arguments[0]);
            if ("setAttribute".equals(name)) attributes.put((String) arguments[0], arguments[1]);
            if ("getSession".equals(name)) return session;
            if ("getServletContext".equals(name)) return proxy(ServletContext.class, (n, a) -> null);
            if ("getContextPath".equals(name)) return "/AnimalsTrack";
            if ("getRequestDispatcher".equals(name)) {
                forwarded = (String) arguments[0];
                return proxy(RequestDispatcher.class, (n, a) -> null);
            }
            return null;
        });
    }

    private HttpServletResponse response() {
        return proxy(HttpServletResponse.class, (name, arguments) -> {
            if ("setStatus".equals(name) || "sendError".equals(name)) status = (Integer) arguments[0];
            if ("sendRedirect".equals(name)) redirect = (String) arguments[0];
            return null;
        });
    }

    private interface Invocation {
        Object call(String name, Object[] arguments) throws Throwable;
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
            (instance, method, arguments) -> invocation.call(method.getName(), arguments)));
    }
}
