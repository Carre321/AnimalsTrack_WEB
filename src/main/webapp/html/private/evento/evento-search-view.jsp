<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.eventoPrepared}">
  <c:redirect url="/controller/private/evento?action=search"/>
</c:if>
<c:set var="section" value="evento"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading">
  <h1>Eventos</h1>
  <c:if test="${not empty sessionScope.granjaId}">
    <a class="button button-primary new-animal-action" href="${pageContext.request.contextPath}/controller/private/evento?action=create"><img src="${pageContext.request.contextPath}/assets/iconos/add-new.png" alt="">Nuevo evento</a>
  </c:if>
</div>
<c:if test="${not empty requestScope.eventoError}">
  <p class="error" role="alert"><c:out value="${requestScope.eventoError}"/></p>
</c:if>
<c:choose>
  <c:when test="${not empty sessionScope.granjaId and empty requestScope.granjaError}">
    <%@ include file="/html/private/evento/evento-search.jsp" %>
  </c:when>
  <c:when test="${empty requestScope.granjaError}">
    <p class="notice">Selecciona una granja para ver sus eventos.</p>
  </c:when>
</c:choose>
<c:set var="results" value="${requestScope.results}"/>
<c:if test="${not empty results}">
  <section class="tracking-panel" aria-labelledby="results-title">
    <header class="tracking-heading">
      <h2 id="results-title">Listado de eventos</h2>
      <span class="record-count"><c:out value="${results.total}"/> resultados</span>
    </header>
    <c:choose>
      <c:when test="${empty results.pageResults}"><p class="table-empty">No se han encontrado eventos.</p></c:when>
      <c:otherwise>
        <div class="tracking-table-wrap" role="region" aria-label="Listado de eventos" tabindex="0">
          <table class="tracking-table event-results-table">
            <thead><tr>
              <th scope="col">Animal</th><th scope="col">Crotal</th><th scope="col">Tipo de evento</th>
              <th scope="col">Resultado</th><th scope="col">Veterinario</th><th scope="col">Fecha</th><th scope="col">Precio</th>
            </tr></thead>
            <tbody>
              <c:forEach var="evento" items="${results.pageResults}">
                <c:url var="detailUrl" value="/controller/private/evento">
                  <c:param name="action" value="detail"/>
                  <c:param name="id" value="${evento.id}"/>
                </c:url>
                <tr>
                  <td><a class="animal-detail-link" href="<c:out value='${detailUrl}'/>"><c:out value="${evento.animalNombre}" default="Sin nombre"/></a></td>
                  <td class="crotal"><c:out value="${evento.animalCrotal}"/></td>
                  <td><c:out value="${evento.tipoEventoNombre}"/></td>
                  <td><c:out value="${evento.resultado}" default="—"/></td>
                  <td><c:choose><c:when test="${empty evento.veterinarioNombreCompleto}">—</c:when><c:otherwise><c:out value="${evento.veterinarioNombreCompleto}"/></c:otherwise></c:choose></td>
                  <td><c:out value="${evento.fechaHora.toLocalDate()}"/> <c:out value="${evento.fechaHora.toLocalTime()}"/></td>
                  <td><c:if test="${not empty evento.precioEvento}"><c:out value="${evento.precioEvento}"/> €</c:if></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
    <c:if test="${paginas gt 1}">
    <c:url var="prevUrl" value="/controller/private/evento">
      <c:param name="action" value="search"/>
      <c:param name="pagina" value="${pagina - 1}"/>
      <c:param name="nombre" value="${param.nombre}"/>
      <c:param name="crotal" value="${param.crotal}"/>
      <c:param name="tipoEventoId" value="${param.tipoEventoId}"/>
      <c:param name="veterinarioId" value="${param.veterinarioId}"/>
      <c:param name="desde" value="${param.desde}"/>
      <c:param name="hasta" value="${param.hasta}"/>
    </c:url>
    <c:url var="nextUrl" value="/controller/private/evento">
      <c:param name="action" value="search"/>
      <c:param name="pagina" value="${pagina + 1}"/>
      <c:param name="nombre" value="${param.nombre}"/>
      <c:param name="crotal" value="${param.crotal}"/>
      <c:param name="tipoEventoId" value="${param.tipoEventoId}"/>
      <c:param name="veterinarioId" value="${param.veterinarioId}"/>
      <c:param name="desde" value="${param.desde}"/>
      <c:param name="hasta" value="${param.hasta}"/>
    </c:url>
      <footer class="list-pagination">
        <span>Mostrando ${(pagina - 1) * 20 + 1}–${(pagina - 1) * 20 + results.pageResults.size()} de ${results.total} resultados</span>
        <nav class="pagination-controls" aria-label="Paginación del listado">
          <c:choose>
            <c:when test="${pagina gt 1}"><a class="pagination-arrow" href="<c:out value='${prevUrl}'/>" aria-label="Página anterior">‹</a></c:when>
            <c:otherwise><span class="pagination-arrow" aria-disabled="true">‹</span></c:otherwise>
          </c:choose>
          <span class="pagination-label" aria-current="page">Página <strong>${pagina}</strong> de ${paginas}</span>
          <c:choose>
            <c:when test="${pagina lt paginas}"><a class="pagination-arrow" href="<c:out value='${nextUrl}'/>" aria-label="Página siguiente">›</a></c:when>
            <c:otherwise><span class="pagination-arrow" aria-disabled="true">›</span></c:otherwise>
          </c:choose>
        </nav>
      </footer>
    </c:if>
  </section>
</c:if>
<script src="${pageContext.request.contextPath}/js/eventos.js" defer></script>
<%@ include file="/html/public/common/footer.jsp" %>
