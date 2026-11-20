<%@ page pageEncoding="UTF-8" %>
<section class="animal-search-panel" aria-label="Buscar animal">
  <form class="animal-filter-form" action="${pageContext.request.contextPath}/controller/private/animal" method="post">
    <input type="hidden" name="action" value="search">
    <div class="animal-filter-fields">
      <div class="field">
        <label for="nombre">Nombre</label>
        <input class="input" type="search" id="nombre" name="nombre" placeholder="Nombre del animal" value="<c:out value='${param.nombre}'/>">
      </div>
      <div class="field">
        <label for="crotal">Crotal</label>
        <input class="input" type="search" id="crotal" name="crotal" placeholder="Número de identificación" value="<c:out value='${param.crotal}'/>">
      </div>
      <div class="field">
        <label for="sexoId">Sexo</label>
        <select class="input" id="sexoId" name="sexoId">
          <option value="">Todos</option>
          <c:forEach var="item" items="${sexos}">
            <option value="${item.id}" <c:if test="${param.sexoId eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="razaId">Raza</label>
        <select class="input" id="razaId" name="razaId">
          <option value="">Todas</option>
          <c:forEach var="item" items="${razas}">
            <option value="${item.id}" <c:if test="${param.razaId eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
    </div>
    <div class="animal-filter-actions">
      <label class="history-filter"><input name="historial" type="checkbox" value="1" <c:if test="${param.historial eq '1'}">checked</c:if>> Incluir animales dados de baja</label>
      <div class="search-buttons">
        <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/animal?action=search">Limpiar filtros</a>
        <button class="button button-primary" type="submit"><img src="${pageContext.request.contextPath}/assets/iconos/search.png" alt="">Buscar</button>
      </div>
    </div>
  </form>
</section>
