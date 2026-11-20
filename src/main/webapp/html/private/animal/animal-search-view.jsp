<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="section" value="animal"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading">
  <h1>Animales</h1>
  <c:if test="${canEditAnimal and not empty sessionScope.granjaId}">
    <a class="button button-primary new-animal-action" href="${pageContext.request.contextPath}/controller/private/animal-ficha?action=create">Nuevo animal</a>
  </c:if>
</div>
<c:if test="${param.created eq '1'}"><p class="notice" role="status">La granja se ha creado y ya está seleccionada.</p></c:if>
<c:choose>
  <c:when test="${empty sessionScope.granjaId and empty requestScope.granjaError}">
    <p class="notice">Todavía no tienes granjas disponibles.
      <c:if test="${sessionScope.user.rol eq 'GANADERO'}">
        <a href="${pageContext.request.contextPath}/controller/private/granja?action=create">Crea tu primera granja</a> para empezar.
      </c:if>
    </p>
  </c:when>
  <c:when test="${not empty sessionScope.granjaId}">
    <%@ include file="/html/private/animal/animal-search.jsp" %>
  </c:when>
</c:choose>
<c:if test="${not empty requestScope.animalError}">
  <p class="error" role="alert"><c:out value="${requestScope.animalError}"/></p>
</c:if>
<c:set var="results" value="${requestScope.results}"/>
<c:if test="${not empty results and not empty sessionScope.granjaId}">
  <section class="tracking-panel animal-results-panel" aria-labelledby="results-title">
    <header class="tracking-heading">
      <h2 id="results-title">Listado de animales</h2>
        <span class="record-count"><c:out value="${results.total}"/> ${results.total eq 1 ? 'resultado' : 'resultados'}</span>
    </header>
    <c:choose>
      <c:when test="${empty results.pageResults}">
        <p class="table-empty">No se han encontrado animales.</p>
      </c:when>
      <c:otherwise>
        <div class="tracking-table-wrap" role="region" aria-label="Listado de animales" tabindex="0">
          <table class="tracking-table animal-results-table">
            <thead>
              <tr><th scope="col">Crotal</th><th scope="col">Nombre</th><th scope="col">Raza</th><th scope="col">Sexo</th><th scope="col">Granja</th><c:if test="${canEditAnimal}"><th scope="col">Acciones</th></c:if></tr>
            </thead>
            <tbody>
              <c:forEach var="animal" items="${results.pageResults}">
                <c:url var="detailUrl" value="/controller/private/animal-ficha">
                  <c:param name="action" value="detail"/>
                  <c:param name="id" value="${animal.id}"/>
                </c:url>
                <tr>
                  <td class="crotal"><a href="<c:out value='${detailUrl}'/>"><c:out value="${animal.crotal}"/></a></td>
                  <td><a class="animal-detail-link" href="<c:out value='${detailUrl}'/>"><c:out value="${animal.nombre}" default="Sin nombre"/></a></td>
                  <td><c:out value="${animal.razaNombre}"/></td>
                  <td><c:out value="${animal.sexoNombre}"/></td>
                  <td><c:out value="${animal.granjaNombre}"/></td>
                  <c:if test="${canEditAnimal}"><td>
                    <form method="post" action="${pageContext.request.contextPath}/controller/private/animal" data-confirm-delete>
                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="id" value="${animal.id}">
                      <input type="hidden" name="formToken" value="${sessionScope.formToken}">
                      <button class="button button-secondary" type="submit">Eliminar</button>
                    </form>
                  </td></c:if>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
    <c:if test="${paginas gt 1}">
      <c:url var="prevUrl" value="/controller/private/animal">
        <c:param name="action" value="search"/>
        <c:param name="pagina" value="${pagina - 1}"/>
        <c:param name="nombre" value="${param.nombre}"/>
        <c:param name="crotal" value="${param.crotal}"/>
        <c:param name="sexoId" value="${param.sexoId}"/>
        <c:param name="razaId" value="${param.razaId}"/>
        <c:param name="historial" value="${param.historial}"/>
      </c:url>
      <c:url var="nextUrl" value="/controller/private/animal">
        <c:param name="action" value="search"/>
        <c:param name="pagina" value="${pagina + 1}"/>
        <c:param name="nombre" value="${param.nombre}"/>
        <c:param name="crotal" value="${param.crotal}"/>
        <c:param name="sexoId" value="${param.sexoId}"/>
        <c:param name="razaId" value="${param.razaId}"/>
        <c:param name="historial" value="${param.historial}"/>
      </c:url>
      <footer class="list-pagination">
        <span>Mostrando ${(pagina - 1) * 20 + 1}–${(pagina - 1) * 20 + results.pageResults.size()} de ${results.total} resultados</span>
        <nav class="pagination-controls" aria-label="Paginación">
          <c:if test="${pagina gt 1}"><a class="pagination-arrow" href="<c:out value='${prevUrl}'/>" aria-label="Página anterior">‹</a></c:if>
          <span>Página ${pagina} de ${paginas}</span>
          <c:if test="${pagina lt paginas}"><a class="pagination-arrow" href="<c:out value='${nextUrl}'/>" aria-label="Página siguiente">›</a></c:if>
        </nav>
      </footer>
    </c:if>
  </section>
</c:if>
<%@ include file="/html/public/common/footer.jsp" %>
