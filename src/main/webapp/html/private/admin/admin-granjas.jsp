<%@ page pageEncoding="UTF-8" %>
<section class="record-panel admin-section">
  <h2>${empty ficha ? 'Nueva granja' : 'Editar granja'}</h2>
  <form method="post" action="${pageContext.request.contextPath}/controller/private/admin" data-confirm-delete="¿Borrar esta granja?">
    <input type="hidden" name="panel" value="granjas">
    <input type="hidden" name="tipo" value="${tipoPersona}">
    <input type="hidden" name="formToken" value="${sessionScope.formToken}">
    <c:if test="${not empty ficha}"><input type="hidden" name="id" value="${ficha.id}"></c:if>
    <div class="admin-fields">
        <div class="field"><label for="f-rega">REGA</label>
          <input class="input" id="f-rega" name="rega" type="text" maxlength="20" required value="<c:out value='${adminSubmitted ? param.rega : ficha.rega}'/>">
        </div>
        <div class="field"><label for="f-nombre">Nombre</label>
          <input class="input" id="f-nombre" name="nombre" type="text" maxlength="60" required value="<c:out value='${adminSubmitted ? param.nombre : ficha.nombre}'/>">
        </div>
        <div class="field"><label for="f-direccion">Dirección</label>
          <input class="input" id="f-direccion" name="direccion" type="text" maxlength="256" required value="<c:out value='${adminSubmitted ? param.direccion : ficha.direccion}'/>">
        </div>
        <div class="field"><label for="f-codigoPostal">Código postal</label>
          <input class="input" id="f-codigoPostal" name="codigoPostal" type="text" maxlength="10" value="<c:out value='${adminSubmitted ? param.codigoPostal : ficha.codigoPostal}'/>">
        </div>
      <%@ include file="/html/private/admin/admin-municipio.jsp" %>
    </div>
    <div class="record-actions">
      <button type="submit" class="button button-primary" name="action" value="save">Guardar</button>
      <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=granjas&amp;tipo=${tipoPersona}">Nuevo</a>
      <c:if test="${not empty ficha}"><button class="button button-danger" type="submit" name="action" value="delete" formnovalidate>Borrar</button></c:if>
    </div>
  </form>
</section>
<section class="tracking-panel admin-section">
  <header class="tracking-heading"><h2>Granjas</h2><input class="input" type="search" aria-label="Filtrar granjas" placeholder="Filtrar granjas" data-filter-table="admin-granjas"></header>
  <c:choose>
    <c:when test="${empty filas}"><p class="table-empty">No hay granjas.</p></c:when>
    <c:otherwise>
      <div class="tracking-table-wrap" role="region" aria-label="Granjas" tabindex="0">
        <table class="tracking-table admin-table" id="admin-granjas" data-open-row>
          <thead><tr><th>REGA</th><th>Nombre</th><th>Dirección</th><th>CP</th><th>Municipio</th></tr></thead>
          <tbody>
            <c:forEach var="item" items="${filas}">
              <c:url var="editUrl" value="/controller/private/admin"><c:param name="panel" value="granjas"/><c:param name="id" value="${item.id}"/></c:url>
              <tr><td><a href="<c:out value='${editUrl}'/>"><c:out value="${item.rega}"/></a></td><td><c:out value="${item.nombre}" default="—"/></td><td><c:out value="${item.direccion}" default="—"/></td><td><c:out value="${item.codigoPostal}" default="—"/></td><td><c:out value="${item.municipioNombre}" default="—"/></td></tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</section>
