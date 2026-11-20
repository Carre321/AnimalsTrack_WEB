<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.eventoPrepared}">
  <c:redirect url="/controller/private/evento?action=search"/>
</c:if>
<c:set var="section" value="evento"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="record-heading">
  <div>
    <a class="record-back" href="${pageContext.request.contextPath}/controller/private/evento?action=search">← Eventos</a>
    <h1>${eventoForm ? (empty evento ? 'Nuevo evento' : 'Editar evento') : 'Detalle del evento'}</h1>
  </div>
  <c:if test="${not empty evento}">
    <span class="record-identity event-identity"><c:out value="${evento.animalNombre}" default="Sin nombre"/> · <c:out value="${evento.tipoEventoNombre}"/></span>
  </c:if>
</div>
<c:if test="${not empty requestScope.eventoError}">
  <p class="error" role="alert"><c:out value="${requestScope.eventoError}"/></p>
</c:if>
<c:if test="${param.saved eq '1' and not eventoForm and empty requestScope.eventoError}">
  <p class="notice" role="status">El evento se ha guardado.</p>
</c:if>
<c:choose>
  <c:when test="${eventoForm}">
    <%@ include file="/html/private/evento/evento-form.jsp" %>
  </c:when>
  <c:when test="${not empty evento}">
    <section class="record-panel" aria-label="Datos del evento">
      <dl class="event-record-fields animal-data">
        <div><dt>Animal</dt><dd><c:out value="${evento.animalNombre}" default="—"/> · <c:out value="${evento.animalCrotal}"/></dd></div>
        <div><dt>Tipo de evento</dt><dd><c:out value="${evento.tipoEventoNombre}" default="—"/></dd></div>
        <div><dt>Veterinario</dt><dd><c:out value="${empty evento.veterinarioNombreCompleto ? '—' : evento.veterinarioNombreCompleto}" default="—"/></dd></div>
        <div><dt>Fecha</dt><dd><c:out value="${evento.fechaHora.toLocalDate()}" default="—"/></dd></div>
        <div><dt>Hora</dt><dd><c:out value="${evento.fechaHora.toLocalTime()}" default="—"/></dd></div>
        <div><dt>Precio (€)</dt><dd><c:out value="${evento.precioEvento}" default="—"/></dd></div>
        <c:if test="${evento.tipoEventoCodigo eq 'REVISION' or evento.tipoEventoCodigo eq 'PRENEZ' or not empty evento.resultado}">
        <div><dt>Resultado</dt><dd><c:out value="${evento.resultado}" default="—"/></dd></div>
        </c:if>
        <c:if test="${evento.tipoEventoCodigo eq 'IA' or not empty evento.semillaId}">
        <div><dt>Semilla</dt><dd><c:out value="${evento.semillaCodigo}" default="—"/></dd></div>
        </c:if>
        <c:if test="${evento.tipoEventoCodigo eq 'TRATAMIENTO' or not empty evento.tratamientoId or not empty evento.dosisId}">
        <div><dt>Tratamiento</dt><dd><c:out value="${evento.tratamientoNombre}" default="—"/></dd></div>
        <div><dt>Dosis</dt><dd><c:out value="${evento.dosisNumOrden}" default="—"/></dd></div>
        </c:if>
        <div class="event-notes"><dt>Observaciones</dt><dd><c:out value="${evento.observaciones}" default="—"/></dd></div>
      </dl>
    </section>
    <div class="record-actions">
      <a class="button button-primary" href="${pageContext.request.contextPath}/controller/private/evento?action=edit&amp;id=${evento.id}"><img src="${pageContext.request.contextPath}/assets/iconos/edit.png" alt="">Editar</a>
      <form method="post" action="${pageContext.request.contextPath}/controller/private/evento" data-confirm-delete="¿Borrar este evento?">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" value="${evento.id}">
        <input type="hidden" name="granjaId" value="${sessionScope.granjaId}">
        <button class="button button-danger" type="submit"><img src="${pageContext.request.contextPath}/assets/iconos/delete.png" alt="">Borrar</button>
      </form>
    </div>
  </c:when>
</c:choose>
<script src="${pageContext.request.contextPath}/js/eventos.js" defer></script>
<%@ include file="/html/public/common/footer.jsp" %>
