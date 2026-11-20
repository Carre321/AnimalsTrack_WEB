package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.dao.Results;
import com.tonin.animaltrack.dao.criteria.AnimalCriteria;
import com.tonin.animaltrack.model.dto.AnimalDTO;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.AnimalService;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class AnimalServletTest {
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private String forwarded;
    private String redirect;
    private int status = 200;
    private int calls;
    private Long granjaId = 42L;
    private boolean writeRequest;
    private String role = "GANADERO";

    @Test
    public void searchesUsingBothDemoWebFiltersAndTheSamePageSize() throws Exception {
        parameters.put("action", "search");
        parameters.put("nombre", " Alba ");
        parameters.put("crotal", " ES270 ");
        Results<AnimalDTO> results = new Results<>();
        servlet((method, values) -> {
            assertEquals("findByCriteria", method);
            AnimalCriteria criteria = (AnimalCriteria) values[0];
            assertEquals("Alba", criteria.getNombreLike());
            assertEquals("ES270", criteria.getCrotalLike());
            assertEquals(Long.valueOf(42), criteria.getGranjaId());
            assertEquals(1, values[1]);
            assertEquals(20, values[2]);
            return results;
        }).doPost(request(), response());
        assertSame(results, attributes.get("results"));
        assertEquals(Views.ANIMAL_SEARCH, forwarded);
    }

    @Test
    public void detailsLoadTheSelectedAnimal() throws Exception {
        parameters.put("action", "detail");
        parameters.put("id", "7");
        AnimalDTO animal = new AnimalDTO();
        animal.setGranjaId(42L);
        servlet((method, values) -> {
            assertEquals("findById", method);
            assertEquals(7L, values[0]);
            return animal;
        }).doGet(request(), response());
        assertSame(animal, attributes.get("animal"));
        assertEquals(Views.ANIMAL_DETAIL, forwarded);
    }

    @Test
    public void deletionCallsTheExistingServiceAndReturnsToTheList() throws Exception {
        writeRequest = true;
        parameters.put("action", "delete");
        parameters.put("id", "7");
        servlet((method, values) -> {
            assertEquals(7L, values[0]);
            if ("findById".equals(method)) {
                AnimalDTO animal = new AnimalDTO();
                animal.setGranjaId(42L);
                return animal;
            }
            assertEquals("delete", method);
            return true;
        }).doGet(request(), response());
        assertEquals(2, calls);
        assertEquals("/AnimalsTrack/controller/private/animal?action=search", redirect);
        assertNull(forwarded);
    }

    @Test
    public void missingAnimalCannotBeReportedAsSuccessfullyDeleted() throws Exception {
        writeRequest = true;
        parameters.put("action", "delete");
        parameters.put("id", "7");
        servlet((method, values) -> null).doGet(request(), response());
        assertEquals(404, status);
        assertNull(redirect);
        assertEquals(1, calls);
    }

    @Test
    public void invalidIdentifiersNeverReachTheService() throws Exception {
        writeRequest = true;
        parameters.put("action", "delete");
        parameters.put("id", "invalid");
        servlet((method, values) -> { throw new AssertionError("No service call expected"); }).doGet(request(), response());
        assertEquals(400, status);
        assertEquals(0, calls);
        assertNotNull(attributes.get("animalError"));
    }

    @Test
    public void databaseErrorsRenderTheSearchPageWithAnError() throws Exception {
        servlet((method, values) -> { throw new Exception("Simulated database failure"); }).doGet(request(), response());
        assertEquals(503, status);
        assertEquals(Views.ANIMAL_SEARCH, forwarded);
        assertNotNull(attributes.get("animalError"));
    }

    @Test
    public void usersWithoutFarmsNeverSearchAllAnimals() throws Exception {
        granjaId = null;
        servlet((method, values) -> { throw new AssertionError("No service call expected"); }).doGet(request(), response());
        assertEquals(0, calls);
        assertEquals(0, ((Results<?>) attributes.get("results")).getTotal());
    }

    @Test
    public void cannotDeleteAnAnimalOutsideTheSelectedFarm() throws Exception {
        writeRequest = true;
        parameters.put("action", "delete");
        parameters.put("id", "7");
        servlet((method, values) -> {
            assertEquals("findById", method);
            AnimalDTO animal = new AnimalDTO();
            animal.setGranjaId(99L);
            return animal;
        }).doGet(request(), response());
        assertEquals(404, status);
        assertEquals(1, calls);
        assertNull(redirect);
    }

    @Test
    public void cannotReadAnAnimalOutsideTheSelectedFarm() throws Exception {
        parameters.put("action", "detail");
        parameters.put("id", "7");
        servlet((method, values) -> {
            AnimalDTO animal = new AnimalDTO();
            animal.setGranjaId(99L);
            return animal;
        }).doGet(request(), response());
        assertEquals(404, status);
        assertNull(attributes.get("animal"));
    }

    @Test
    public void unavailableFarmListDoesNotFallBackToAllAnimals() throws Exception {
        attributes.put("granjaError", "Unavailable");
        servlet((method, values) -> { throw new AssertionError("No service call expected"); }).doGet(request(), response());
        assertEquals(503, status);
        assertEquals(0, calls);
    }

    @Test
    public void deletionRequiresPostAndAWriterRole() throws Exception {
        parameters.put("action", "delete");
        AnimalServlet servlet = servlet((method, values) -> { throw new AssertionError("No service call expected"); });
        servlet.doGet(request(), response());
        assertEquals(405, status);
        writeRequest = true;
        role = "VETERINARIO";
        servlet.doGet(request(), response());
        assertEquals(403, status);
    }

    private AnimalServlet servlet(Invocation service) throws Exception {
        AnimalService fake = proxy(AnimalService.class, (method, arguments) -> {
            calls++;
            return service.call(method, arguments);
        });
        AnimalServlet servlet = new AnimalServlet(fake);
        ServletContext context = proxy(ServletContext.class, (method, arguments) -> null);
        servlet.init(proxy(ServletConfig.class, (method, arguments) -> "getServletContext".equals(method) ? context : null));
        return servlet;
    }

    private HttpServletRequest request() {
        UsuarioLoginDTO user = new UsuarioLoginDTO();
        user.setRol(role);
        HttpSession session = proxy(HttpSession.class, (method, arguments) -> {
            if (!"getAttribute".equals(method)) return null;
            if ("granjaId".equals(arguments[0])) return granjaId;
            if ("user".equals(arguments[0]) && writeRequest) return user;
            if ("formToken".equals(arguments[0])) return "test";
            return null;
        });
        return proxy(HttpServletRequest.class, (method, arguments) -> {
            if ("getMethod".equals(method)) return writeRequest ? "POST" : "GET";
            if ("getSession".equals(method)) return session;
            if ("getAttribute".equals(method)) return attributes.get(arguments[0]);
            if ("getParameter".equals(method)) return "formToken".equals(arguments[0]) ? "test" : parameters.get(arguments[0]);
            if ("setAttribute".equals(method)) attributes.put((String) arguments[0], arguments[1]);
            if ("getContextPath".equals(method)) return "/AnimalsTrack";
            if ("getRequestDispatcher".equals(method)) {
                forwarded = (String) arguments[0];
                return proxy(RequestDispatcher.class, (name, values) -> null);
            }
            return null;
        });
    }

    private HttpServletResponse response() {
        return proxy(HttpServletResponse.class, (method, arguments) -> {
            if ("setStatus".equals(method)) status = (Integer) arguments[0];
            if ("sendError".equals(method)) status = (Integer) arguments[0];
            if ("sendRedirect".equals(method)) redirect = (String) arguments[0];
            return null;
        });
    }

    private interface Invocation {
        Object call(String method, Object[] arguments) throws Throwable;
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
            (instance, method, arguments) -> invocation.call(method.getName(), arguments)));
    }
}
