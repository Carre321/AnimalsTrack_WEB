<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="section" value="animal"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading"><h1>Detalle del animal</h1></div>
<c:choose>
  <c:when test="${not empty requestScope.animal}">
    <section class="record-panel" aria-labelledby="animal-info">
      <h2 id="animal-info">Datos del animal</h2>
      <dl class="record-fields animal-data">
        <div><dt>Nombre</dt><dd><c:out value="${requestScope.animal.nombre}" default="—"/></dd></div>
        <div><dt>Crotal</dt><dd><c:out value="${requestScope.animal.crotal}" default="—"/></dd></div>
        <div><dt>Raza</dt><dd><c:out value="${requestScope.animal.razaNombre}" default="—"/></dd></div>
        <div><dt>Sexo</dt><dd><c:out value="${requestScope.animal.sexoNombre}" default="—"/></dd></div>
        <div><dt>Granja</dt><dd><c:out value="${requestScope.animal.granjaNombre}" default="—"/></dd></div>
      </dl>
    </section>
  </c:when>
  <c:otherwise><p class="notice">No existe el animal.</p></c:otherwise>
</c:choose>
<div class="record-actions"><a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/animal?action=search">Volver a animales</a></div>
<%@ include file="/html/public/common/footer.jsp" %>
