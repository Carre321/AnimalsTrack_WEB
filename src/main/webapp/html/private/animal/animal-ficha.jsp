<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty fichaPrepared}"><c:redirect url="/controller/private/animal?action=search"/></c:if>
<c:set var="section" value="animal"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="record-heading">
  <div><a class="record-back" href="${pageContext.request.contextPath}/controller/private/animal?action=search">← Animales</a>
    <h1>${animalEdit ? (empty animal ? 'Nuevo animal' : 'Editar animal') : 'Detalle del animal'}</h1>
  </div>
  <c:if test="${not empty animal}"><span class="record-identity"><c:out value="${animal.crotal}"/></span></c:if>
</div>
<c:if test="${not empty animalError}"><p class="error" role="alert"><c:out value="${animalError}"/></p></c:if>
<c:if test="${param.saved eq '1'}"><p class="notice" role="status">El cambio se ha guardado.</p></c:if>
<c:choose>
  <c:when test="${animalEdit}"><%@ include file="/html/private/animal/animal-form.jsp" %></c:when>
  <c:otherwise>
    <div class="record-top">
      <%@ include file="/html/private/animal/animal-foto.jsp" %>
      <section class="record-panel" aria-label="Datos del animal">
        <h2>Datos del animal</h2>
        <dl class="record-fields animal-data">
          <div><dt>Nombre</dt><dd><c:out value="${animal.nombre}" default="—"/></dd></div>
          <div><dt>Crotal</dt><dd><c:out value="${animal.crotal}" default="—"/></dd></div>
          <div><dt>Sexo</dt><dd><c:out value="${animal.sexoNombre}" default="—"/></dd></div>
          <div><dt>Raza</dt><dd><c:out value="${animal.razaNombre}" default="—"/></dd></div>
          <div><dt>Fecha de nacimiento</dt><dd><c:out value="${animal.fechaNacimiento}" default="—"/></dd></div>
          <div><dt>Fecha de baja</dt><dd><c:out value="${animal.fechaBaja}" default="—"/></dd></div>
          <div><dt>Granja</dt><dd><c:out value="${animal.granjaNombre}" default="—"/></dd></div>
          <div><dt>Madre interna</dt><dd><c:out value="${animal.madreInternaId}" default="—"/></dd></div>
          <div><dt>Madre externa</dt><dd><c:out value="${animal.madreExternaCrotal}" default="—"/></dd></div>
          <div><dt>Padre interno</dt><dd><c:out value="${animal.padreInternoId}" default="—"/></dd></div>
        </dl>
      </section>
    </div>
    <c:if test="${canEditAnimal}">
      <div class="record-actions">
        <a class="button button-primary" href="${pageContext.request.contextPath}/controller/private/animal-ficha?action=edit&amp;id=${animal.id}">Editar</a>
        <c:if test="${empty animal.fechaBaja}">
          <form method="post" action="${pageContext.request.contextPath}/controller/private/animal-ficha" data-confirm-delete="¿Dar de baja a este animal?">
    <input type="hidden" name="formToken" value="${sessionScope.formToken}">
    <input type="hidden" name="granjaId" value="${sessionScope.granjaId}">
    <c:if test="${not empty animal}"><input type="hidden" name="id" value="${animal.id}"></c:if>
            <input type="hidden" name="action" value="baja">
            <button class="button button-secondary" type="submit">Dar de baja</button>
          </form>
        </c:if>
      </div>
    </c:if>
    <section class="tracking-panel animal-events" aria-label="Últimos eventos">
      <header class="tracking-heading"><h2>Últimos eventos</h2></header>
      <c:choose>
        <c:when test="${empty eventos}"><p class="table-empty">Este animal todavía no tiene eventos.</p></c:when>
        <c:otherwise>
          <div class="tracking-table-wrap" role="region" aria-label="Últimos eventos" tabindex="0">
            <table class="tracking-table">
              <thead><tr><th>Tipo</th><th>Fecha</th><th>Resultado</th></tr></thead>
              <tbody>
                <c:forEach var="evento" items="${eventos}">
                  <tr><td><a href="${pageContext.request.contextPath}/controller/private/evento?action=detail&amp;id=${evento.id}"><c:out value="${evento.tipoEventoNombre}"/></a></td><td><c:out value="${evento.fechaHora.toLocalDate()}"/></td><td><c:out value="${evento.resultado}" default="—"/></td></tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </section>
  </c:otherwise>
</c:choose>
<%@ include file="/html/public/common/footer.jsp" %>
