<%@ page pageEncoding="UTF-8" %>
<details class="user-menu">
  <summary>
    <span class="user-avatar" aria-hidden="true">
      <c:out value="${sessionScope.user.rol.substring(0, 1)}"/>
      <img class="user-avatar-photo" src="${pageContext.request.contextPath}/controller/private/usuario?action=photo" alt="" width="36" height="36" hidden>
    </span>
    <span class="user-label"><c:out value="${sessionScope.user.nombreVisible}"/><small><c:out value="${sessionScope.user.rol}"/></small></span>
    <span aria-hidden="true">⌄</span>
  </summary>
  <div class="user-popover">
    <div class="account-menu-heading">
      <div><strong><c:out value="${sessionScope.user.nombreVisible}"/></strong><small><c:out value="${sessionScope.user.rol}"/></small></div>
    </div>
    <div class="account-menu-actions">
      <a class="profile-menu-button" href="${pageContext.request.contextPath}/controller/private/usuario" <c:if test="${section eq 'perfil'}">aria-current="page"</c:if>>
        <span class="account-menu-icon"><img src="${pageContext.request.contextPath}/assets/iconos/login.png" alt=""></span>
        <span>Ver perfil<small>Foto y datos de la cuenta</small></span>
      </a>
      <form method="post" action="${pageContext.request.contextPath}/controller/public/usuario">
        <input type="hidden" name="action" value="logout">
        <button type="submit" class="account-menu-logout">
          <span class="account-menu-icon"><img src="${pageContext.request.contextPath}/assets/iconos/logout.png" alt=""></span>
          Cerrar sesión
        </button>
      </form>
    </div>
  </div>
</details>
