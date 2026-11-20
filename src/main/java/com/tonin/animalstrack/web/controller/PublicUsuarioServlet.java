package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.model.Ganadero;
import com.tonin.animaltrack.model.Municipio;
import com.tonin.animaltrack.model.Provincia;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.AuthService;
import com.tonin.animaltrack.service.MunicipioService;
import com.tonin.animaltrack.service.ProvinciaService;
import com.tonin.animaltrack.service.impl.AuthServiceImpl;
import com.tonin.animaltrack.service.impl.MunicipioServiceImpl;
import com.tonin.animaltrack.service.impl.ProvinciaServiceImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/controller/public/usuario")
public class PublicUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AuthService authService;
    private final MunicipioService municipioService;
    private final ProvinciaService provinciaService;

    public PublicUsuarioServlet() {
        this(new AuthServiceImpl(), new MunicipioServiceImpl(), new ProvinciaServiceImpl());
    }

    PublicUsuarioServlet(AuthService authService, MunicipioService municipioService, ProvinciaService provinciaService) {
        this.authService = authService;
        this.municipioService = municipioService;
        this.provinciaService = provinciaService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter(PNames.ACTION);
        String view = Views.USUARIO_LOGIN;
        // Comprueba los datos del formulario de inicio de sesión.
        if (PValues.ACTION_LOGIN.equalsIgnoreCase(action) && "POST".equals(request.getMethod())) {
            String login = request.getParameter(PNames.LOGIN);
            String password = request.getParameter(PNames.PASSWORD);
            if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("loginError", "Introduce el correo o usuario y la contraseña.");
            } else {
                try {
                    UsuarioLoginDTO user = authService.login(login.trim(), password);
                    if (user == null) {
                        request.setAttribute("loginError", "No se pudo iniciar sesión. Revisa los datos de acceso.");
                    } else {
                        user.setPasswordHash(null);
                        // Abre una sesión nueva para el usuario.
                        if (request.getSession(false) != null) request.getSession(false).invalidate();
                        SessionManager.setAttribute(request, AttributeNames.USER, user);
                        response.sendRedirect(request.getContextPath() + "/controller/private/animal?action=search");
                        return;
                    }
                } catch (Exception exception) {
                    getServletContext().log("No se pudo iniciar sesión en AnimalsTrack.", exception);
                    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    request.setAttribute("loginError", "El inicio de sesión no está disponible temporalmente. Puedes volver al inicio.");
                }
            }
        } else if (PValues.ACTION_REGISTER.equalsIgnoreCase(action)) {
            // Si se envió el formulario, intenta crear la cuenta.
            if ("POST".equals(request.getMethod()) && register(request, response)) return;
            prepareRegistration(request, response);
            view = Views.USUARIO_REGISTRO;
        } else if (PValues.ACTION_LOGOUT.equalsIgnoreCase(action)) {
            // Cierra la sesión y vuelve a la portada.
            if (request.getSession(false) != null) request.getSession(false).invalidate();
            view = Views.HOME;
        }
        request.getRequestDispatcher(view).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private boolean register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String password = request.getParameter(PNames.PASSWORD);
            if (password == null || !password.equals(request.getParameter(PNames.CONFIRM_PASSWORD))) {
                throw new IllegalArgumentException("Las contraseñas no coinciden.");
            }
            // Recoge los datos del nuevo ganadero.
            Ganadero ganadero = new Ganadero();
            ganadero.setNombre(request.getParameter(PNames.NOMBRE));
            ganadero.setApellidos(request.getParameter(PNames.APELLIDOS));
            ganadero.setDni(request.getParameter(PNames.DNI));
            ganadero.setEmail(request.getParameter(PNames.EMAIL));
            try {
                ganadero.setMunicipioId(Long.valueOf(request.getParameter(PNames.MUNICIPIO_ID)));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Selecciona un municipio válido.");
            }
            if (authService.register(ganadero, password) == null) {
                throw new IllegalStateException("No se pudo crear la cuenta.");
            }
            response.sendRedirect(request.getContextPath() + Views.USUARIO_LOGIN + "?registered=1");
            return true;
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("registrationError", exception.getMessage());
        } catch (Exception exception) {
            getServletContext().log("No se pudo completar el registro en AnimalsTrack.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("registrationError", "No se pudo crear la cuenta. Inténtalo más tarde.");
        }
        return false;
    }

    private void prepareRegistration(HttpServletRequest request, HttpServletResponse response) {
        // Carga los municipios y sus provincias para el formulario.
        request.setAttribute("registrationPrepared", true);
        try {
            Map<Long, String> provincias = new HashMap<>();
            for (Provincia provincia : provinciaService.findAll()) {
                provincias.put(provincia.getId(), provincia.getNombre());
            }
            List<Municipio> municipios = municipioService.findAll();
            if (municipios == null || municipios.isEmpty()) {
                throw new IllegalStateException("No hay municipios disponibles para el registro.");
            }
            request.setAttribute("provincias", provincias);
            request.setAttribute("municipios", municipios);
        } catch (Exception exception) {
            getServletContext().log("No se pudieron cargar los municipios para el registro.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("registrationError", "No se pudo cargar el formulario de registro. Inténtalo más tarde.");
        }
    }
}
