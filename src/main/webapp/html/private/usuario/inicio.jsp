<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.profilePrepared}">
  <c:redirect url="/controller/private/usuario"/>
</c:if>
<c:set var="section" value="perfil"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading">
  <div><h1>Mi perfil</h1><p><c:out value="${sessionScope.user.nombreVisible}"/> · <c:out value="${sessionScope.user.rol}"/></p></div>
</div>
<p><c:out value="${sessionScope.user.email}"/></p>
<section aria-labelledby="profile-photo-title" class="record-panel workspace-form profile-photo">
  <h2 id="profile-photo-title">Foto de perfil</h2>
  <c:if test="${param.photoSaved eq '1'}"><p class="notice" role="status">Tu foto de perfil se ha guardado.</p></c:if>
  <c:if test="${param.photoRemoved eq '1'}"><p class="notice" role="status">Tu foto de perfil se ha quitado.</p></c:if>
  <c:if test="${not empty requestScope.photoError}"><p class="error" role="alert"><c:out value="${requestScope.photoError}"/></p></c:if>
  <c:choose>
    <c:when test="${requestScope.hasProfilePhoto}">
      <img src="${pageContext.request.contextPath}/controller/private/usuario?action=photo" alt="Tu foto de perfil" class="profile-photo-image" width="160" height="160">
    </c:when>
    <c:otherwise><p class="profile-photo-empty">Sin foto de perfil</p></c:otherwise>
  </c:choose>
  <form action="${pageContext.request.contextPath}/controller/private/usuario" method="post" enctype="multipart/form-data" class="access-form">
    <input type="hidden" name="action" value="updatePhoto">
    <div class="field">
      <label for="foto">Seleccionar foto</label>
    <input class="input" type="file" id="foto" name="foto" accept="image/jpeg,image/png,image/gif" required aria-describedby="photo-help">
      <small id="photo-help">JPG, PNG o GIF. Máximo 20 MB y 40 millones de píxeles.</small>
    </div>
    <div class="record-actions"><button class="button button-primary" type="submit">Guardar foto</button></div>
  </form>
  <c:if test="${requestScope.hasStoredProfilePhoto}">
    <form action="${pageContext.request.contextPath}/controller/private/usuario" method="post" class="photo-remove-form">
      <input type="hidden" name="action" value="removePhoto">
      <button class="button button-secondary" type="submit">Quitar foto</button>
    </form>
  </c:if>
</section>
<div class="record-actions"><a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/animal?action=search">Volver a animales</a></div>
<%@ include file="/html/public/common/footer.jsp" %>
