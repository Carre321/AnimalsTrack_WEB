package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.model.Ganadero;
import com.tonin.animaltrack.model.Municipio;
import com.tonin.animaltrack.model.Provincia;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.AuthService;
import com.tonin.animaltrack.service.MunicipioService;
import com.tonin.animaltrack.service.ProvinciaService;
import com.tonin.animalstrack.web.filter.AuthenticationFilter;
import com.tonin.animalstrack.web.util.AttributeNames;
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
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class DemoWebTest {
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, Object> sessionAttributes = new HashMap<>();
    private String method = "POST";
    private String forwardedPath;
    private String redirect;
    private int status = 200;
    private int loginCalls;
    private boolean sessionInvalidated;
    private UsuarioLoginDTO loginResult;
    private boolean serviceUnavailable;
    private int registrationCalls;
    private boolean duplicateRegistration;
    private boolean locationsUnavailable;

    @Test
    public void openingLoginDoesNotCallTheDatabaseService() throws Exception {
        method = "GET";
        parameters.put("action", "login");
        servlet().doGet(request(), response());
        assertEquals(0, loginCalls);
        assertEquals(Views.USUARIO_LOGIN, forwardedPath);
    }

    @Test
    public void successfulLoginStoresUserAndRedirectsToTheAnimals() throws Exception {
        credentials();
        loginResult = new UsuarioLoginDTO();
        loginResult.setId(2L);
        loginResult.setPasswordHash("must-not-remain-in-session");
        servlet().doPost(request(), response());
        assertEquals(1, loginCalls);
        assertTrue(sessionInvalidated);
        assertSame(loginResult, sessionAttributes.get(AttributeNames.USER));
        assertNull(loginResult.getPasswordHash());
        assertEquals("/AnimalsTrack/controller/private/animal?action=search", redirect);
        assertNull(forwardedPath);
    }

    @Test
    public void invalidLoginDoesNotCreateAnAuthenticatedSession() throws Exception {
        credentials();
        servlet().doPost(request(), response());
        assertEquals(1, loginCalls);
        assertNull(sessionAttributes.get(AttributeNames.USER));
        assertNotNull(attributes.get("loginError"));
        assertEquals(Views.USUARIO_LOGIN, forwardedPath);
    }

    @Test
    public void unavailableDatabaseReturnsAnErrorWithoutBreakingPublicPages() throws Exception {
        credentials();
        serviceUnavailable = true;
        servlet().doPost(request(), response());
        assertEquals(503, status);
        assertNotNull(attributes.get("loginError"));
        assertNull(sessionAttributes.get(AttributeNames.USER));
        assertEquals(Views.USUARIO_LOGIN, forwardedPath);
    }

    @Test
    public void logoutClearsTheSessionAndReturnsToTheHomepage() throws Exception {
        parameters.put("action", "logout");
        sessionAttributes.put(AttributeNames.USER, new UsuarioLoginDTO());
        servlet().doPost(request(), response());
        assertTrue(sessionInvalidated);
        assertTrue(sessionAttributes.isEmpty());
        assertEquals(0, loginCalls);
        assertEquals(Views.HOME, forwardedPath);
    }

    @Test
    public void privateRoutesRedirectAnonymousUsersUsingTheDeploymentContext() throws Exception {
        new AuthenticationFilter().doFilter(request(), response(), (request, response) -> fail("Private access must be denied"));
        assertEquals("/AnimalsTrack" + Views.USUARIO_LOGIN, redirect);
    }

    @Test
    public void privateRoutesAllowAuthenticatedUsers() throws Exception {
        sessionAttributes.put(AttributeNames.USER, new UsuarioLoginDTO());
        boolean[] allowed = {false};
        new AuthenticationFilter().doFilter(request(), response(), (request, response) -> allowed[0] = true);
        assertTrue(allowed[0]);
        assertNull(redirect);
    }

    @Test
    public void registrationPageLoadsLocationsWithoutCreatingAnAccount() throws Exception {
        method = "GET";
        parameters.put("action", "register");
        servlet().doGet(request(), response());
        assertEquals(0, registrationCalls);
        assertEquals(Boolean.TRUE, attributes.get("registrationPrepared"));
        assertNotNull(attributes.get("municipios"));
        assertEquals(Views.USUARIO_REGISTRO, forwardedPath);
    }

    @Test
    public void registrationSuccessReturnsToLoginWithoutAuthenticating() throws Exception {
        registration();
        servlet().doPost(request(), response());
        assertEquals(1, registrationCalls);
        assertEquals("/AnimalsTrack" + Views.USUARIO_LOGIN + "?registered=1", redirect);
        assertNull(sessionAttributes.get(AttributeNames.USER));
        assertNull(forwardedPath);
    }

    @Test
    public void mismatchedPasswordsDoNotReachTheRegistrationService() throws Exception {
        registration();
        parameters.put("confirmPassword", "different-password");
        servlet().doPost(request(), response());
        assertEquals(0, registrationCalls);
        assertEquals(400, status);
        assertEquals("Las contraseñas no coinciden.", attributes.get("registrationError"));
        assertEquals(Views.USUARIO_REGISTRO, forwardedPath);
    }

    @Test
    public void invalidMunicipalityKeepsTheUserOnTheForm() throws Exception {
        registration();
        parameters.put("municipioId", "invalid");
        servlet().doPost(request(), response());
        assertEquals(0, registrationCalls);
        assertEquals(400, status);
        assertNotNull(attributes.get("registrationError"));
        assertNotNull(attributes.get("municipios"));
    }

    @Test
    public void duplicateRegistrationDisplaysTheServiceValidationMessage() throws Exception {
        registration();
        duplicateRegistration = true;
        servlet().doPost(request(), response());
        assertEquals(400, status);
        assertEquals("Ya existe una cuenta con ese correo electrónico.", attributes.get("registrationError"));
        assertNull(redirect);
    }

    @Test
    public void databaseRegistrationFailureDoesNotShowSuccess() throws Exception {
        registration();
        serviceUnavailable = true;
        servlet().doPost(request(), response());
        assertEquals(503, status);
        assertNotNull(attributes.get("registrationError"));
        assertNull(redirect);
        assertNull(sessionAttributes.get(AttributeNames.USER));
    }

    @Test
    public void unavailableLocationsDisplayAnErrorWithoutCreatingAnAccount() throws Exception {
        method = "GET";
        parameters.put("action", "register");
        locationsUnavailable = true;
        servlet().doGet(request(), response());
        assertEquals(503, status);
        assertEquals(0, registrationCalls);
        assertNotNull(attributes.get("registrationError"));
        assertEquals(Views.USUARIO_REGISTRO, forwardedPath);
    }

    private void registration() {
        parameters.put("action", "register");
        parameters.put("nombre", "Persona");
        parameters.put("apellidos", "De prueba");
        parameters.put("dni", "12345678Z");
        parameters.put("email", "persona@example.test");
        parameters.put("municipioId", "900001");
        parameters.put("password", "test-only-password");
        parameters.put("confirmPassword", "test-only-password");
    }

    private void credentials() {
        parameters.put("action", "login");
        parameters.put("login", " persona@example.test ");
        parameters.put("password", "test-only-password");
    }

    private PublicUsuarioServlet servlet() throws Exception {
        PublicUsuarioServlet servlet = new PublicUsuarioServlet(new AuthService() {
            @Override
            public UsuarioLoginDTO login(String login, String password) throws Exception {
                loginCalls++;
                assertEquals("persona@example.test", login);
                if (serviceUnavailable) throw new IllegalStateException("Fictitious database unavailable");
                return loginResult;
            }

            @Override
            public UsuarioLoginDTO findByEmail(String login) {
                throw new AssertionError("Unexpected lookup");
            }

            @Override
            public UsuarioLoginDTO register(Ganadero ganadero, String password) throws Exception {
                registrationCalls++;
                assertEquals("persona@example.test", ganadero.getEmail());
                assertEquals(Long.valueOf(900001), ganadero.getMunicipioId());
                if (duplicateRegistration) throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
                if (serviceUnavailable) throw new IllegalStateException("Fictitious database unavailable");
                return new UsuarioLoginDTO();
            }
        }, proxy(MunicipioService.class, (method, arguments) -> {
            Municipio municipio = new Municipio();
            municipio.setId(900001L);
            municipio.setNombre("Municipio de pruebas");
            municipio.setProvinciaId(900001L);
            return Collections.singletonList(municipio);
        }), proxy(ProvinciaService.class, (method, arguments) -> {
            if (locationsUnavailable) throw new IllegalStateException("Fictitious locations unavailable");
            Provincia provincia = new Provincia();
            provincia.setId(900001L);
            provincia.setNombre("Provincia de pruebas");
            return Collections.singletonList(provincia);
        }));
        ServletContext context = proxy(ServletContext.class, (method, arguments) -> null);
        servlet.init(proxy(ServletConfig.class, (method, arguments) -> "getServletContext".equals(method) ? context : null));
        return servlet;
    }

    private HttpServletRequest request() {
        HttpSession session = proxy(HttpSession.class, (method, arguments) -> {
            if ("getAttribute".equals(method)) return sessionAttributes.get(arguments[0]);
            if ("setAttribute".equals(method)) sessionAttributes.put((String) arguments[0], arguments[1]);
            if ("invalidate".equals(method)) {
                sessionAttributes.clear();
                sessionInvalidated = true;
            }
            return null;
        });
        return proxy(HttpServletRequest.class, (name, arguments) -> {
            if ("getMethod".equals(name)) return method;
            if ("getParameter".equals(name)) return parameters.get(arguments[0]);
            if ("getAttribute".equals(name)) return attributes.get(arguments[0]);
            if ("setAttribute".equals(name)) attributes.put((String) arguments[0], arguments[1]);
            if ("getSession".equals(name)) return session;
            if ("getContextPath".equals(name)) return "/AnimalsTrack";
            if ("getRequestDispatcher".equals(name)) {
                forwardedPath = (String) arguments[0];
                return proxy(RequestDispatcher.class, (dispatcherMethod, values) -> null);
            }
            return null;
        });
    }

    private HttpServletResponse response() {
        return proxy(HttpServletResponse.class, (method, arguments) -> {
            if ("setStatus".equals(method)) status = (Integer) arguments[0];
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
