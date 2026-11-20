<%@ page pageEncoding="UTF-8" %>
<a href="${pageContext.request.contextPath}/controller/private/inicio" aria-label="Inicio" title="Inicio" <c:if test="${section eq 'inicio'}">aria-current="page"</c:if>>
  <img src="${pageContext.request.contextPath}/assets/iconos/dashboard.png" alt=""><span class="sidebar-text">Inicio</span>
</a>
<a href="${pageContext.request.contextPath}/controller/private/evento?action=search" aria-label="Eventos" title="Eventos" <c:if test="${section eq 'evento'}">aria-current="page"</c:if>>
  <img src="${pageContext.request.contextPath}/assets/iconos/calendar.png" alt=""><span class="sidebar-text">Eventos</span>
</a>
<a href="${pageContext.request.contextPath}/controller/private/animal?action=search" aria-label="Animales" title="Animales" <c:if test="${section eq 'animal'}">aria-current="page"</c:if>>
  <img src="${pageContext.request.contextPath}/assets/iconos/animal.png" alt=""><span class="sidebar-text">Animales</span>
</a>
<c:if test="${sessionScope.user.rol eq 'GANADERO' and not empty sessionScope.user.ganaderoId}">
  <a href="${pageContext.request.contextPath}/controller/private/granja?action=create" aria-label="Crear granja" title="Crear granja" <c:if test="${section eq 'granja'}">aria-current="page"</c:if>>
    <img src="${pageContext.request.contextPath}/assets/iconos/add-new.png" alt=""><span class="sidebar-text">Crear granja</span>
  </a>
</c:if>
<c:if test="${sessionScope.user.rol eq 'ADMINISTRADOR' or sessionScope.user.rol eq 'VETERINARIO'}">
  <a href="${pageContext.request.contextPath}/controller/private/semilla" aria-label="Semillas" title="Semillas" <c:if test="${section eq 'semilla'}">aria-current="page"</c:if>>
    <img src="${pageContext.request.contextPath}/assets/iconos/vaccine.png" alt=""><span class="sidebar-text">Semillas</span>
  </a>
</c:if>
<c:if test="${sessionScope.user.rol eq 'ADMINISTRADOR'}">
  <a href="${pageContext.request.contextPath}/controller/private/admin" aria-label="Administración" title="Administración" <c:if test="${section eq 'admin'}">aria-current="page"</c:if>>
    <img src="${pageContext.request.contextPath}/assets/iconos/admin.png" alt=""><span class="sidebar-text">Administración</span>
  </a>
</c:if>
