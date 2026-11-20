<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.inicioPrepared}">
  <c:redirect url="/controller/private/inicio"/>
</c:if>
<c:set var="section" value="inicio"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading">
  <h1>Vacas próximas a cumplir preñez</h1>
  <a class="button button-secondary refresh-action" href="${pageContext.request.contextPath}/controller/private/inicio"><img src="${pageContext.request.contextPath}/assets/iconos/refresh.png" alt="">Actualizar</a>
</div>
<c:if test="${not empty inicioError}"><p class="error" role="alert"><c:out value="${inicioError}"/></p></c:if>
<c:choose>
  <c:when test="${empty sessionScope.granjaId and empty granjaError}">
    <p class="notice">Selecciona una granja para ver su seguimiento.</p>
  </c:when>
  <c:when test="${not empty sessionScope.granjaId and empty inicioError}">
    <section class="reproduction-summary" aria-label="Resumen del seguimiento">
      <article class="stat-card"><span>En la lista</span><div><strong>${partos.size()}</strong><img src="${pageContext.request.contextPath}/assets/iconos/animal.png" alt="" width="36" height="36"></div><p>Animales en seguimiento</p></article>
      <article class="stat-card stat-warning"><span>Fuera de fecha</span><div><strong>${atrasados}</strong><span class="stat-symbol" aria-hidden="true">!</span></div><p>Han superado la fecha prevista</p></article>
      <article class="stat-card stat-upcoming"><span>Próximos 15 días</span><div><strong>${proximos}</strong><img src="${pageContext.request.contextPath}/assets/iconos/calendar.png" alt="" width="36" height="36"></div><p>Incluye los previstos para hoy</p></article>
    </section>
    <section class="tracking-panel" aria-labelledby="tracking-title">
      <header class="tracking-heading"><h2 id="tracking-title">Seguimiento reproductivo</h2></header>
      <c:choose>
        <c:when test="${empty partos}"><p class="table-empty">No hay partos previstos en este periodo.</p></c:when>
        <c:otherwise>
          <div class="tracking-table-wrap" role="region" aria-label="Seguimiento reproductivo" tabindex="0">
            <table class="tracking-table">
              <thead><tr><th>Estado</th><th>Crotal</th><th>Animal</th><th>Inseminación</th><th>Confirmación</th><th>Fin de preñez</th><th>Días</th></tr></thead>
              <tbody>
                <c:forEach var="parto" items="${partos}">
                  <c:url var="fichaUrl" value="/controller/private/animal-ficha">
                    <c:param name="action" value="detail"/><c:param name="id" value="${parto.evento.animalId}"/>
                  </c:url>
                  <tr class="${parto.dias lt 0 ? 'row-warning' : (parto.dias eq 0 ? 'row-today' : '')}">
                    <td>${parto.dias lt 0 ? 'Fuera de fecha' : (parto.dias eq 0 ? 'Hoy' : 'Próximo')}</td>
                    <td class="crotal"><c:out value="${parto.evento.animalCrotal}"/></td>
                    <td><a href="<c:out value='${fichaUrl}'/>"><c:out value="${parto.evento.animalNombre}" default="Sin nombre"/></a></td>
                    <td><c:out value="${parto.evento.fechaHora.toLocalDate()}"/></td>
                    <td><c:out value="${parto.confirmacion.fechaHora.toLocalDate()}"/></td>
                    <td><c:out value="${parto.fin}"/></td><td>${parto.dias}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </section>
  </c:when>
</c:choose>
<%@ include file="/html/public/common/footer.jsp" %>
