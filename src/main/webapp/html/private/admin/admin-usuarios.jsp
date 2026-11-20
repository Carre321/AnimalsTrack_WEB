<%@ page pageEncoding="UTF-8" %>
<nav class="record-actions admin-subtabs" aria-label="Tipo de usuario">
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=usuarios&amp;tipo=ganadero" <c:if test="${tipoPersona eq 'ganadero'}">aria-current="page"</c:if>>Ganaderos</a>
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=usuarios&amp;tipo=veterinario" <c:if test="${tipoPersona eq 'veterinario'}">aria-current="page"</c:if>>Veterinarios</a>
</nav>
<section class="record-panel admin-section">
  <h2>${empty ficha ? 'Nuevo usuario' : 'Editar usuario'}</h2>
  <form method="post" action="${pageContext.request.contextPath}/controller/private/admin" data-confirm-delete="¿Borrar este usuario y su acceso?">
    <input type="hidden" name="panel" value="usuarios">
    <input type="hidden" name="tipo" value="${tipoPersona}">
    <input type="hidden" name="formToken" value="${sessionScope.formToken}">
    <c:if test="${not empty ficha}"><input type="hidden" name="id" value="${ficha.id}"></c:if>
    <div class="admin-fields">
      <c:if test="${tipoPersona eq 'veterinario'}">
        <div class="field"><label for="f-codigo">Código</label>
          <input class="input" id="f-codigo" name="codigo" type="text" maxlength="20" value="<c:out value='${adminSubmitted ? param.codigo : ficha.codigo}'/>">
        </div>
      </c:if>
        <div class="field"><label for="f-dni">DNI o NIE</label>
          <input class="input" id="f-dni" name="dni" type="text" maxlength="9" required value="<c:out value='${adminSubmitted ? param.dni : ficha.dni}'/>">
        </div>
        <div class="field"><label for="f-nombre">Nombre</label>
          <input class="input" id="f-nombre" name="nombre" type="text" maxlength="60" required value="<c:out value='${adminSubmitted ? param.nombre : ficha.nombre}'/>">
        </div>
        <div class="field"><label for="f-apellidos">Apellidos</label>
          <input class="input" id="f-apellidos" name="apellidos" type="text" maxlength="80" required value="<c:out value='${adminSubmitted ? param.apellidos : ficha.apellidos}'/>">
        </div>
        <div class="field"><label for="f-telefono">Teléfono</label>
          <input class="input" id="f-telefono" name="telefono" type="tel" maxlength="20" value="<c:out value='${adminSubmitted ? param.telefono : ficha.telefono}'/>">
        </div>
        <div class="field"><label for="f-email">Correo electrónico</label>
          <input class="input" id="f-email" name="email" type="email" maxlength="200" required value="<c:out value='${adminSubmitted ? param.email : ficha.email}'/>">
        </div>
      <div class="field">
        <label for="f-password">Contraseña</label>
        <input class="input" id="f-password" type="password" name="password" minlength="8" maxlength="72" autocomplete="new-password" <c:if test="${empty ficha}">required</c:if>>
        <c:if test="${not empty ficha}"><small>Déjala vacía para conservar la actual.</small></c:if>
      </div>
        <div class="field"><label for="f-direccion">Dirección</label>
          <input class="input" id="f-direccion" name="direccion" type="text" maxlength="256" value="<c:out value='${adminSubmitted ? param.direccion : ficha.direccion}'/>">
        </div>
        <div class="field"><label for="f-codigoPostal">Código postal</label>
          <input class="input" id="f-codigoPostal" name="codigoPostal" type="text" maxlength="10" value="<c:out value='${adminSubmitted ? param.codigoPostal : ficha.codigoPostal}'/>">
        </div>
      <%@ include file="/html/private/admin/admin-municipio.jsp" %>
    </div>
    <div class="record-actions">
      <button type="submit" class="button button-primary" name="action" value="save">Guardar</button>
      <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=usuarios&amp;tipo=${tipoPersona}">Nuevo</a>
      <c:if test="${not empty ficha}"><button class="button button-danger" type="submit" name="action" value="delete" formnovalidate>Borrar</button></c:if>
    </div>
  </form>
</section>
<section class="tracking-panel admin-section">
  <header class="tracking-heading"><h2>Usuarios</h2><input class="input" type="search" aria-label="Filtrar usuarios" placeholder="Filtrar usuarios" data-filter-table="admin-usuarios"></header>
  <c:choose>
    <c:when test="${empty filas}"><p class="table-empty">No hay usuarios de este tipo.</p></c:when>
    <c:otherwise>
      <div class="tracking-table-wrap" role="region" aria-label="Usuarios" tabindex="0">
        <table class="tracking-table admin-table" id="admin-usuarios" data-open-row>
          <thead><tr><th>DNI</th><th>Nombre</th><th>Apellidos</th><th>Teléfono</th><th>Correo</th><th>Dirección</th><th>CP</th><th>Municipio</th></tr></thead>
          <tbody>
            <c:forEach var="item" items="${filas}">
              <c:url var="editUrl" value="/controller/private/admin"><c:param name="panel" value="usuarios"/><c:param name="tipo" value="${tipoPersona}"/><c:param name="id" value="${item.id}"/></c:url>
              <tr>
                <td><a href="<c:out value='${editUrl}'/>"><c:out value="${item.dni}" default="—"/></a></td>
                <td><c:out value="${item.nombre}" default="—"/></td><td><c:out value="${item.apellidos}" default="—"/></td><td><c:out value="${item.telefono}" default="—"/></td><td><c:out value="${item.email}" default="—"/></td><td><c:out value="${item.direccion}" default="—"/></td><td><c:out value="${item.codigoPostal}" default="—"/></td><td><c:out value="${item.municipioNombre}" default="—"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
</section>
