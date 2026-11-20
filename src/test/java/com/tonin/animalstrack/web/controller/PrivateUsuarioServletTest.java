package com.tonin.animalstrack.web.controller;

import com.tonin.animaltrack.model.ContentData;
import com.tonin.animaltrack.model.ContentException;
import com.tonin.animaltrack.model.ContentMetadata;
import com.tonin.animaltrack.model.dto.UsuarioLoginDTO;
import com.tonin.animaltrack.service.ContentService;
import com.tonin.animaltrack.service.impl.ContentServiceFileImpl;
import com.tonin.animalstrack.web.view.Views;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class PrivateUsuarioServletTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private final Map<String,String> parameters = new HashMap<>();
    private final Map<String,Object> attributes = new HashMap<>();
    private final Map<String,String> headers = new HashMap<>();
    private final UsuarioLoginDTO user = new UsuarioLoginDTO();
    private final ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
    private int status = 200;
    private String forwarded;
    private String redirect;
    private String contentType;
    private byte[] upload;
    private Long uploadSize;
    private boolean anonymous;
    private boolean multipartTooLarge;
    private boolean temporaryDeleted;

    public PrivateUsuarioServletTest() { user.setId(5L); user.setRol("GANADERO"); }

    @Test public void profileWithoutPhotoShowsEmptyState() throws Exception {
        servlet().doGet(request(),response());
        assertEquals(Views.USUARIO_INICIO,forwarded);
        assertEquals(Boolean.FALSE,attributes.get("hasProfilePhoto"));
        assertEquals(Boolean.TRUE,attributes.get("profilePrepared"));
    }

    @Test public void storesUploadedPhotoUnderSessionUserNotSubmittedIdOrFilename() throws Exception {
        update("png"); parameters.put("id","99"); parameters.put("userId","99");
        servlet().doPost(request(),response());
        assertArrayEquals(upload,Files.readAllBytes(root().resolve("usuario/5/foto.png")));
        assertFalse(Files.exists(root().resolve("usuario/99")));
        assertTrue(redirect.endsWith("/controller/private/usuario?photoSaved=1"));
        assertTrue(temporaryDeleted);
    }

    @Test public void storedJpegIsServedWithCorrectTypeAndPrivateCaching() throws Exception {
        byte[] raw=image("jpg"); store(5,"jpg",raw);
        parameters.put("action","photo"); parameters.put("id","99");
        servlet().doGet(request(),response());
        assertEquals("image/jpeg",contentType);
        assertEquals("private, no-store",headers.get("Cache-Control"));
        assertEquals("nosniff",headers.get("X-Content-Type-Options"));
        assertArrayEquals(raw,responseBytes.toByteArray());
    }

    @Test public void gifUploadIsSupportedAsInDesktop() throws Exception {
        update("gif"); servlet().doPost(request(),response());
        assertArrayEquals(upload,Files.readAllBytes(root().resolve("usuario/5/foto.gif")));
    }

    @Test public void changingFormatReplacesPreviousPhotoAndPreservesAnimalFiles() throws Exception {
        byte[] original=image("jpg");store(5,"jpg",original);
        Path animal=root().resolve("animal/47/foto.jpg"); Files.createDirectories(animal.getParent()); Files.write(animal,original);
        update("png");servlet().doPost(request(),response());
        assertFalse(Files.exists(root().resolve("usuario/5/foto.jpg")));
        assertArrayEquals(upload,Files.readAllBytes(root().resolve("usuario/5/foto.png")));
        assertArrayEquals(original,Files.readAllBytes(animal));
    }

    @Test public void rejectedFileKeepsExistingPhoto() throws Exception {
        byte[] original=image("png");store(5,"png",original);
        parameters.put("action","updatePhoto");upload="<html>not an image</html>".getBytes("UTF-8");
        servlet().doPost(request(),response());
        assertEquals(400,status);assertNotNull(attributes.get("photoError"));
        assertArrayEquals(original,Files.readAllBytes(root().resolve("usuario/5/foto.png")));
        assertNull(redirect);
    }

    @Test public void emptyUploadIsRejectedWithoutCreatingFiles() throws Exception {
        parameters.put("action","updatePhoto");upload=new byte[0];
        servlet().doPost(request(),response());
        assertEquals(400,status);assertFalse(Files.exists(root().resolve("usuario/5")));
    }

    @Test public void unsupportedImageFormatIsRejected() throws Exception {
        update("bmp");servlet().doPost(request(),response());
        assertEquals(400,status);assertFalse(Files.exists(root().resolve("usuario/5")));
    }

    @Test public void oversizedFileIsRejectedBeforeReadingItsBytes() throws Exception {
        update("png");uploadSize=20971521L;
        servlet().doPost(request(),response());
        assertEquals(413,status);assertFalse(Files.exists(root().resolve("usuario/5")));
    }

    @Test public void containerMultipartLimitReturnsProfileWithFriendlyError() throws Exception {
        multipartTooLarge=true;
        servlet().doPost(request(),response());
        assertEquals(413,status);assertEquals(Views.USUARIO_INICIO,forwarded);
        assertTrue(attributes.get("photoError").toString().contains("20 MB"));
    }

    @Test public void failureToSaveKeepsOldPhotoAndDoesNotReportSuccess() throws Exception {
        byte[] original=image("png");store(5,"png",original);update("jpg");
        ContentService content=proxy(ContentService.class,(name,args)->{
            if(name.equals("save"))throw new ContentException("Simulated write failure");
            if(name.equals("listMetadata"))return service().listMetadata((String)args[0],(String)args[1]);
            throw new AssertionError("Must not remove the old photo when saving fails");
        });
        servlet(content).doPost(request(),response());
        assertEquals(503,status);assertNull(redirect);
        assertArrayEquals(original,Files.readAllBytes(root().resolve("usuario/5/foto.png")));
        assertFalse(Files.exists(root().resolve("usuario/5/foto.jpg")));
    }

    @Test public void removingPhotoDoesNotRemoveOtherUsersOrAnimalPhotos() throws Exception {
        byte[] raw=image("png");store(5,"png",raw);store(99,"png",raw);
        Path animal=root().resolve("animal/47/foto.png");Files.createDirectories(animal.getParent());Files.write(animal,raw);
        parameters.put("action","removePhoto");parameters.put("id","99");
        servlet().doPost(request(),response());
        assertFalse(Files.exists(root().resolve("usuario/5/foto.png")));
        assertArrayEquals(raw,Files.readAllBytes(root().resolve("usuario/99/foto.png")));
        assertArrayEquals(raw,Files.readAllBytes(animal));
        assertTrue(redirect.endsWith("photoRemoved=1"));
    }

    @Test public void userWithoutPhotoCannotFetchAnotherUsersPhotoById() throws Exception {
        store(99,"png",image("png"));parameters.put("action","photo");parameters.put("id","99");
        servlet().doGet(request(),response());
        assertEquals(404,status);assertEquals(0,responseBytes.size());
    }

    @Test public void photoMutationCannotUseGet() throws Exception {
        byte[] raw=image("png");store(5,"png",raw);parameters.put("action","removePhoto");
        servlet().doGet(request(),response());
        assertEquals(405,status);assertArrayEquals(raw,Files.readAllBytes(root().resolve("usuario/5/foto.png")));
    }

    @Test public void anonymousUserNeverReadsOrWritesContents() throws Exception {
        anonymous=true;parameters.put("action","photo");
        servlet(proxy(ContentService.class,(name,args)->{throw new AssertionError("No contents access expected");})).doGet(request(),response());
        assertTrue(redirect.endsWith(Views.USUARIO_LOGIN));
    }

    @Test public void corruptStoredImageIsNotServedAsAnImage() throws Exception {
        store(5,"png",new byte[]{1,2,3});parameters.put("action","photo");
        servlet().doGet(request(),response());
        assertEquals(404,status);assertEquals(0,responseBytes.size());
    }

    @Test public void profileIndicatesWhenPhotoExists() throws Exception {
        store(5,"png",image("png"));
        ContentServiceFileImpl metadataOnly = new ContentServiceFileImpl(root()) {
            @Override public ContentData get(ContentMetadata metadata) {
                throw new AssertionError("El perfil no debe leer los bytes de la foto");
            }
        };
        servlet(metadataOnly).doGet(request(),response());
        assertEquals(Boolean.TRUE,attributes.get("hasProfilePhoto"));
        assertEquals(Boolean.TRUE,attributes.get("hasStoredProfilePhoto"));
    }

    @Test public void removalOfMissingPhotoIsHarmless() throws Exception {
        parameters.put("action","removePhoto");servlet().doPost(request(),response());
        assertEquals(200,status);assertTrue(redirect.endsWith("photoRemoved=1"));
    }

    @Test public void allExistingAccountRolesCanSaveTheirOwnProfilePhoto() throws Exception {
        for(String role:new String[]{"GANADERO","VETERINARIO","ADMINISTRADOR"}) {
            user.setRol(role);update("png");servlet().doPost(request(),response());
            assertArrayEquals(upload,Files.readAllBytes(root().resolve("usuario/5/foto.png")));
        }
    }

    private Path root() { return temporary.getRoot().toPath().resolve("cms"); }
    private ContentServiceFileImpl service() { return new ContentServiceFileImpl(root()); }
    private void update(String format) throws Exception { parameters.put("action","updatePhoto");upload=image(format); }
    private byte[] image(String format) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(12,10,BufferedImage.TYPE_INT_RGB),format,bytes));
        return bytes.toByteArray();
    }
    private void store(long id,String format,byte[] raw) throws Exception {
        ContentMetadata metadata=new ContentMetadata();metadata.setEntityType("usuario");metadata.setEntityId(Long.toString(id));metadata.setResourceId("foto");metadata.setFormat(format);
        ContentData data=new ContentData();data.setMetadata(metadata);data.setRaw(raw);service().save(data);
    }
    private PrivateUsuarioServlet servlet() throws Exception { return servlet(service()); }
    private PrivateUsuarioServlet servlet(ContentService content) throws Exception {
        PrivateUsuarioServlet servlet=new PrivateUsuarioServlet(content);
        ServletContext context=proxy(ServletContext.class,(name,args)->null);
        servlet.init(proxy(ServletConfig.class,(name,args)->name.equals("getServletContext")?context:null));
        return servlet;
    }
    private HttpServletRequest request() {
        HttpSession session=proxy(HttpSession.class,(name,args)->name.equals("getAttribute") && args[0].equals("user") && !anonymous?user:null);
        return proxy(HttpServletRequest.class,(name,args)->{
            if(name.equals("getSession"))return session;
            if(name.equals("getContentType"))return upload!=null || multipartTooLarge?"multipart/form-data; boundary=test":null;
            if(name.equals("getParts") && multipartTooLarge)throw new IllegalStateException("Multipart too large");
            if(name.equals("getParameter")) { if(multipartTooLarge)throw new IllegalStateException("Multipart too large");return parameters.get(args[0]); }
            if(name.equals("setAttribute"))attributes.put((String)args[0],args[1]);
            if(name.equals("getAttribute"))return attributes.get(args[0]);
            if(name.equals("getContextPath"))return "/AnimalsTrack";
            if(name.equals("getPart"))return upload==null?null:proxy(Part.class,(method,values)->{
                if(method.equals("getSize"))return uploadSize==null?(long)upload.length:uploadSize;
                if(method.equals("getInputStream"))return new ByteArrayInputStream(upload);
                if(method.equals("delete"))temporaryDeleted=true;
                if(method.equals("getSubmittedFileName"))return "../../animal/47/foto.jpg";
                return null;
            });
            if(name.equals("getRequestDispatcher")) { forwarded=(String)args[0];return proxy(RequestDispatcher.class,(n,a)->null); }
            return null;
        });
    }
    private HttpServletResponse response() {
        ServletOutputStream output=new ServletOutputStream() {
            @Override public void write(int b) { responseBytes.write(b); }
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) { }
        };
        return proxy(HttpServletResponse.class,(name,args)->{
            if(name.equals("sendRedirect"))redirect=(String)args[0];
            if(name.equals("sendError") || name.equals("setStatus"))status=(Integer)args[0];
            if(name.equals("setContentType"))contentType=(String)args[0];
            if(name.equals("setHeader"))headers.put((String)args[0],(String)args[1]);
            if(name.equals("getOutputStream"))return output;
            return null;
        });
    }
    private interface Invocation { Object call(String name,Object[] arguments) throws Throwable; }
    private static <T>T proxy(Class<T> type,Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},
            (instance,method,arguments)->invocation.call(method.getName(),arguments)));
    }
}
