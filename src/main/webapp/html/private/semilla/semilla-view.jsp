<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.semillaPrepared}">
  <c:redirect url="/controller/private/semilla"/>
</c:if>
<c:set var="section" value="semilla"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading"><h1>Semillas</h1><a class="button button-primary" href="${pageContext.request.contextPath}/controller/private/semilla?action=create">Nueva semilla</a></div>
<c:if test="${not empty semillaError}"><p class="error" role="alert"><c:out value="${semillaError}"/></p></c:if>
<c:if test="${param.saved eq '1'}"><p class="notice" role="status">El cambio se ha guardado.</p></c:if>
<section class="animal-search-panel" aria-label="Buscar semillas">
  <form class="seed-search-fields" method="get" action="${pageContext.request.contextPath}/controller/private/semilla">
    <div class="field">
  <label for="f-buscarCodigo">Código</label>
  <input class="input" id="f-buscarCodigo" name="buscarCodigo" type="search" value="<c:out value='${param.buscarCodigo}'/>">
</div>
    <div class="field">
  <label for="f-buscarNombre">Nombre</label>
  <input class="input" id="f-buscarNombre" name="buscarNombre" type="search" value="<c:out value='${param.buscarNombre}'/>">
</div>
    <div class="field">
  <label for="f-buscarRaza">Raza</label>
  <c:set var="selectedValue" value="${param.buscarRaza}"/>
  <select class="input" id="f-buscarRaza" name="buscarRaza">
    <option value="">Todas</option>
    <c:forEach var="item" items="${razas}">
      <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
    </c:forEach>
  </select>
</div>
    <button class="button button-primary" type="submit"><img src="${pageContext.request.contextPath}/assets/iconos/search.png" alt="">Buscar</button>
  </form>
</section>
<c:if test="${semillaForm}">
  <section class="record-panel seed-editor" aria-label="Ficha de semilla">
    <h2>${empty semilla ? 'Nueva semilla' : 'Editar semilla'}</h2>
    <form method="post" action="${pageContext.request.contextPath}/controller/private/semilla" data-confirm-delete="¿Borrar esta semilla?">
      <input type="hidden" name="formToken" value="${sessionScope.formToken}">
      <c:if test="${not empty semilla}"><input type="hidden" name="id" value="${semilla.id}"></c:if>
      <div class="seed-edit-fields">
        <div class="field">
  <label for="f-codigo">Código</label>
  <input class="input" id="f-codigo" name="codigo" type="text" required maxlength="40" value="<c:out value='${semillaSubmitted ? param.codigo : (empty semilla ? param.buscarCodigo : semilla.codigo)}'/>">
</div>
        <div class="field">
  <label for="f-nombre">Nombre</label>
  <input class="input" id="f-nombre" name="nombre" type="text" required maxlength="100" value="<c:out value='${semillaSubmitted ? param.nombre : (empty semilla ? param.buscarNombre : semilla.nombre)}'/>">
</div>
        <div class="field">
  <label for="f-razaId">Raza</label>
  <c:set var="selectedValue" value="${semillaSubmitted ? param.razaId : (empty semilla ? param.buscarRaza : semilla.razaId)}"/>
  <select class="input" id="f-razaId" name="razaId">
    <option value="">Sin especificar</option>
    <c:forEach var="item" items="${razas}">
      <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
    </c:forEach>
  </select>
</div>
        <div class="field seed-description"><label for="seed-description">Descripción</label>
          <textarea class="input" id="seed-description" name="descripcion" rows="3" maxlength="256"><c:out value="${semillaSubmitted ? param.descripcion : semilla.descripcion}"/></textarea>
        </div>
      </div>
      <div class="record-actions">
        <button class="button button-primary" name="action" value="${empty semilla ? 'create' : 'update'}" type="submit"><img src="${pageContext.request.contextPath}/assets/iconos/save.png" alt="">Guardar</button>
        <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/semilla">Cancelar</a>
        <c:if test="${not empty semilla}"><button class="button button-danger" name="action" value="delete" type="submit" formnovalidate><img src="${pageContext.request.contextPath}/assets/iconos/delete.png" alt="">Borrar</button></c:if>
      </div>
    </form>
  </section>
</c:if>
<section class="tracking-panel" aria-label="Catálogo de semillas">
  <header class="tracking-heading"><h2>Catálogo de semillas</h2></header>
  <c:choose>
    <c:when test="${empty semillas}"><p class="table-empty">No se han encontrado semillas.</p></c:when>
    <c:otherwise>
      <div class="tracking-table-wrap" role="region" aria-label="Semillas" tabindex="0">
        <table class="tracking-table seed-table" data-open-row>
          <thead><tr><th>Código</th><th>Nombre</th><th>Raza</th><th>Descripción</th></tr></thead>
          <tbody>
            <c:forEach var="item" items="${semillas}">
              <c:url var="semillaUrl" value="/controller/private/semilla"><c:param name="action" value="edit"/><c:param name="id" value="${item.id}"/></c:url>
              <tr><td><a href="<c:out value='${semillaUrl}'/>"><c:out value="${item.codigo}"/></a></td><td><c:out value="${item.nombre}"/></td><td><c:out value="${item.razaNombre}" default="—"/></td><td><c:out value="${item.descripcion}" default="—"/></td></tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</section>
<%@ include file="/html/public/common/footer.jsp" %>
