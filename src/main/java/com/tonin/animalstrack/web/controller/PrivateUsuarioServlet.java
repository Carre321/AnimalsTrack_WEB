package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentException;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.ContentService;
import com.tonin.animaltrack.service.impl.ContentServiceFileImpl;
import com.tonin.animaltrack.utils.PhotoUtils;
import com.tonin.animalstrack.web.util.AttributeNames;
import com.tonin.animalstrack.web.util.PNames;
import com.tonin.animalstrack.web.util.PValues;
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
import java.util.List;
import java.util.Locale;

@WebServlet("/controller/private/usuario")
@MultipartConfig(fileSizeThreshold = 1048576, maxFileSize = PhotoUtils.MAX_BYTES,
        maxRequestSize = PhotoUtils.MAX_BYTES + 1048576)
public class PrivateUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ContentService contentService;

    public PrivateUsuarioServlet() {
        this(new ContentServiceFileImpl());
    }

    PrivateUsuarioServlet(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UsuarioLoginDTO user = currentUser(request, response);
        if (user == null) return;
        String action = request.getParameter(PNames.ACTION);
        response.setHeader("Cache-Control", "private, no-store");
        if (PValues.ACTION_PHOTO.equalsIgnoreCase(action)) {
            // Busca la foto con sus metadatos y la muestra al usuario.
            try {
                List<ContentMetadata> photos = photos(user);
                ContentData photo = photos.size() == 1 ? contentService.get(photos.get(0)) : null;
                if (photo == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                byte[] raw = photo.getRaw();
                String format = PhotoUtils.format(raw);
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setContentType("jpg".equals(format) ? "image/jpeg" : "image/" + format);
                response.setContentLength(raw.length);
                response.getOutputStream().write(raw);
            } catch (IllegalArgumentException exception) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (Exception exception) {
                getServletContext().log("No se pudo leer la foto del usuario.", exception);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        } else if (action == null || action.isEmpty()) {
            showProfile(request, response, user);
        } else {
            response.sendError(PValues.ACTION_UPDATE_PHOTO.equalsIgnoreCase(action)
                    || PValues.ACTION_REMOVE_PHOTO.equalsIgnoreCase(action)
                    ? HttpServletResponse.SC_METHOD_NOT_ALLOWED : HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "private, no-store");
        UsuarioLoginDTO user = currentUser(request, response);
        if (user == null) return;
        Part part = null;
        try {
            String contentType = request.getContentType();
            if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
                // Lee el formulario para detectar si la foto supera el límite.
                request.getParts();
            }
            String action = request.getParameter(PNames.ACTION);
            if (PValues.ACTION_UPDATE_PHOTO.equalsIgnoreCase(action)) {
                // Recoge la foto que se envió desde el perfil.
                part = request.getPart(PNames.FOTO);
                if (part == null || part.getSize() == 0) throw new IllegalArgumentException("Selecciona una foto.");
                if (part.getSize() > PhotoUtils.MAX_BYTES) throw new IllegalStateException("La foto es demasiado grande.");
                byte[] raw;
                try (InputStream input = part.getInputStream()) {
                    raw = input.readNBytes(PhotoUtils.MAX_BYTES + 1);
                }
                if (raw.length > PhotoUtils.MAX_BYTES) throw new IllegalStateException("La foto es demasiado grande.");
                String format = PhotoUtils.format(raw);
                List<ContentMetadata> previous = photos(user);
                ContentData photo = new ContentData();
                photo.setRaw(raw);
                photo.setMetadata(metadata(user, format));
                // Guarda la foto nueva antes de borrar la anterior.
                contentService.save(photo);
                for (ContentMetadata old : previous) {
                    if (!format.equalsIgnoreCase(old.getFormat())) {
                        contentService.remove(old);
                    }
                }
                response.sendRedirect(request.getContextPath() + "/controller/private/usuario?photoSaved=1");
                return;
            } else if (PValues.ACTION_REMOVE_PHOTO.equalsIgnoreCase(action)) {
                // Quita la foto del usuario conectado.
                for (ContentMetadata photo : photos(user)) contentService.remove(photo);
                response.sendRedirect(request.getContextPath() + "/controller/private/usuario?photoRemoved=1");
                return;
            } else {
                throw new IllegalArgumentException("La operación solicitada no es válida.");
            }
        } catch (IllegalStateException exception) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            request.setAttribute("photoError", "La foto supera el tamaño permitido: máximo 20 MB.");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("photoError", exception.getMessage());
        } catch (ServletException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("photoError", "No se pudo recibir la foto. Selecciona el archivo y vuelve a intentarlo.");
        } catch (Exception exception) {
            getServletContext().log("No se pudo guardar el cambio de foto del usuario.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("photoError", "No se pudo completar el cambio de foto. Inténtalo de nuevo.");
        } finally {
            if (part != null) {
                try { part.delete(); }
                catch (IOException exception) { getServletContext().log("No se pudo retirar el archivo temporal de subida.", exception); }
            }
        }
        showProfile(request, response, user);
    }

    private UsuarioLoginDTO currentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Saca el usuario de la sesión.
        UsuarioLoginDTO user = (UsuarioLoginDTO) SessionManager.getAttribute(request, AttributeNames.USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + Views.USUARIO_LOGIN);
            return null;
        }
        if (user.getId() == null || user.getId() <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private void showProfile(HttpServletRequest request, HttpServletResponse response, UsuarioLoginDTO user)
            throws ServletException, IOException {
        request.setAttribute("profilePrepared", true);
        try {
            // Comprueba si hay foto sin cargar el archivo.
            int photoCount = photos(user).size();
            request.setAttribute("hasProfilePhoto", photoCount == 1);
            request.setAttribute("hasStoredProfilePhoto", photoCount > 0);
            if (photoCount > 1) {
                request.setAttribute("photoError", "Hay varias versiones de la foto. Guarda una nueva o quítalas para corregirlo.");
            }
        } catch (Exception exception) {
            getServletContext().log("No se pudo consultar la foto del usuario.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("photoError", "No se pudo acceder a la foto de perfil. Inténtalo más tarde.");
        }
        request.getRequestDispatcher(Views.USUARIO_INICIO).forward(request, response);
    }

    private List<ContentMetadata> photos(UsuarioLoginDTO user) throws ContentException {
        return contentService.listMetadata("usuario", user.getId().toString()).stream()
                .filter(metadata -> "foto".equals(metadata.getResourceId()))
                .toList();
    }

    private ContentMetadata metadata(UsuarioLoginDTO user, String format) {
        ContentMetadata metadata = new ContentMetadata();
        metadata.setEntityType("usuario");
        metadata.setEntityId(user.getId().toString());
        metadata.setResourceId("foto");
        metadata.setFormat(format);
        return metadata;
    }

}
