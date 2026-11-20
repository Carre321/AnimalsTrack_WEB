<%@ page pageEncoding="UTF-8" %>
<section class="animal-search-panel" aria-label="Buscar evento">
  <form class="animal-filter-form events-filter-form" action="${pageContext.request.contextPath}/controller/private/evento" method="get">
    <input type="hidden" name="action" value="search">
    <div class="animal-filter-fields event-filter-fields">
      <div class="field">
        <label for="filter-nombre">Animal</label>
        <input class="input" id="filter-nombre" name="nombre" type="search" value="<c:out value='${param.nombre}'/>" placeholder="Nombre del animal">
      </div>
      <div class="field">
        <label for="filter-crotal">Crotal</label>
        <input class="input" id="filter-crotal" name="crotal" type="search" value="<c:out value='${param.crotal}'/>" placeholder="Número de identificación">
      </div>
      <div class="field">
        <label for="filter-tipo">Tipo de evento</label>
        <select class="input" id="filter-tipo" name="tipoEventoId">
          <option value="">Todos</option>
          <c:forEach var="item" items="${requestScope.tiposEvento}">
            <option value="${item.id}" <c:if test="${param.tipoEventoId eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="filter-vet">Veterinario</label>
        <select class="input" id="filter-vet" name="veterinarioId">
          <option value="">Todos</option>
          <c:forEach var="item" items="${requestScope.veterinarios}">
            <option value="${item.id}" <c:if test="${param.veterinarioId eq item.id.toString()}">selected</c:if>><c:out value="${item.nombreCompleto}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="filter-desde">Fecha desde</label>
        <input class="input" id="filter-desde" name="desde" type="date" value="<c:out value='${param.desde}'/>">
      </div>
      <div class="field">
        <label for="filter-hasta">Hasta</label>
        <input class="input" id="filter-hasta" name="hasta" type="date" value="<c:out value='${param.hasta}'/>">
      </div>
    </div>
    <div class="animal-filter-actions event-filter-actions">
      <div class="search-buttons">
        <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/evento?action=search">Limpiar filtros</a>
        <button class="button button-primary" type="submit"><img src="${pageContext.request.contextPath}/assets/iconos/search.png" alt="">Buscar</button>
      </div>
    </div>
  </form>
</section>
