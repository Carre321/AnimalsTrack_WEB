<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.granjaFormPrepared}">
  <c:redirect url="/controller/private/granja?action=create"/>
</c:if>
<c:set var="section" value="granja"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading"><h1>Crear granja</h1></div>
<c:if test="${not empty requestScope.granjaFormError}">
  <p class="error" role="alert"><c:out value="${requestScope.granjaFormError}"/></p>
</c:if>
<section class="record-panel workspace-form" aria-label="Datos de la granja">
  <form method="post" action="${pageContext.request.contextPath}/controller/private/granja" class="auth-form">
    <input type="hidden" name="action" value="create">
    <div class="field">
      <label for="rega">REGA</label>
      <input class="input" id="rega" name="rega" type="text" required maxlength="20" value="<c:out value='${param.rega}'/>">
    </div>
    <div class="field">
      <label for="nombre">Nombre de la granja</label>
      <input class="input" id="nombre" name="nombre" type="text" required maxlength="60" value="<c:out value='${param.nombre}'/>">
    </div>
    <div class="field">
      <label for="direccion">Dirección</label>
      <input class="input" id="direccion" name="direccion" type="text" required maxlength="256" value="<c:out value='${param.direccion}'/>">
    </div>
    <div class="field">
      <label for="codigoPostal">Código postal (opcional)</label>
      <input class="input" id="codigoPostal" name="codigoPostal" type="text" maxlength="10" value="<c:out value='${param.codigoPostal}'/>">
    </div>
    <div class="field">
      <label for="municipioId">Municipio (provincia)</label>
      <select class="input" id="municipioId" name="municipioId" required>
      <option value="">Selecciona el municipio</option>
      <c:forEach var="municipio" items="${requestScope.municipios}">
      <option value="${municipio.id}" <c:if test="${param.municipioId eq municipio.id.toString()}">selected</c:if>><c:out value="${municipio.nombre}"/> (<c:out value="${requestScope.provincias[municipio.provinciaId]}"/>)</option>
      </c:forEach>
      </select>
    </div>
    <button class="button button-primary" type="submit" <c:if test="${empty requestScope.municipios or not empty requestScope.granjaError}">disabled</c:if>>Guardar granja</button>
  </form>
</section>
<div class="record-actions"><a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/animal?action=search">Volver a animales</a></div>
<%@ include file="/html/public/common/footer.jsp" %>
