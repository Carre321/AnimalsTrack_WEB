<%@ page pageEncoding="UTF-8" %>
<nav class="record-actions admin-subtabs" aria-label="Tipo de relación">
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=relaciones&amp;relacion=veterinario" <c:if test="${relacion eq 'veterinario'}">aria-current="page"</c:if>>Veterinario–Granja</a>
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=relaciones&amp;relacion=ganadero" <c:if test="${relacion eq 'ganadero'}">aria-current="page"</c:if>>Ganadero–Granja</a>
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=relaciones&amp;relacion=animal" <c:if test="${relacion eq 'animal'}">aria-current="page"</c:if>>Animal–Semilla</a>
</nav>
<c:set var="firstLabel" value="${relacion eq 'veterinario' ? 'Veterinario' : (relacion eq 'ganadero' ? 'Ganadero' : 'Animal')}"/>
<c:set var="secondLabel" value="${relacion eq 'animal' ? 'Semilla' : 'Granja'}"/>
<section class="record-panel admin-section">
  <form action="${pageContext.request.contextPath}/controller/private/admin" method="get" class="admin-fields">
    <input type="hidden" name="panel" value="relaciones"><input type="hidden" name="relacion" value="${relacion}">
    <div class="field">
      <label for="r-primero">${firstLabel}</label>
      <select class="input" name="primero" id="r-primero" required>
        <option value="">Selecciona</option>
        <c:forEach var="item" items="${primeros}"><option value="${item.key}" <c:if test="${primero eq item.key}">selected</c:if>><c:out value="${item.value}"/></option></c:forEach>
      </select>
    </div>
    <div class="record-actions"><button class="button button-secondary" type="submit">Buscar relaciones</button></div>
  </form>
  <c:if test="${not empty nombrePrimero}">
    <form method="post" action="${pageContext.request.contextPath}/controller/private/admin" class="admin-fields">
      <input type="hidden" name="panel" value="relaciones"><input type="hidden" name="relacion" value="${relacion}"><input type="hidden" name="primero" value="${primero}"><input type="hidden" name="formToken" value="${sessionScope.formToken}">
      <div class="field">
        <label for="r-segundo">${secondLabel}</label>
        <select class="input" id="r-segundo" name="segundo" required>
          <option value="">Selecciona</option>
          <c:forEach var="item" items="${segundos}">
            <option value="${item.key}" <c:if test="${param.segundo eq item.key.toString()}">selected</c:if>><c:out value="${item.value}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="record-actions"><button class="button button-primary" type="submit" name="action" value="save">Asignar</button></div>
    </form>
  </c:if>
</section>
<c:if test="${not empty nombrePrimero}">
  <section class="tracking-panel admin-section">
    <header class="tracking-heading"><h2>Relaciones de <c:out value="${nombrePrimero}"/></h2></header>
    <c:choose>
      <c:when test="${empty relaciones}"><p class="table-empty">No hay relaciones para este registro.</p></c:when>
      <c:otherwise>
        <div class="tracking-table-wrap" role="region" aria-label="Relaciones" tabindex="0">
          <table class="tracking-table admin-table">
            <thead><tr><th>${firstLabel}</th><th>${secondLabel}</th><th>Acciones</th></tr></thead>
            <tbody>
              <c:forEach var="item" items="${relaciones}">
                <tr><td><c:out value="${nombrePrimero}"/></td><td><c:out value="${item.value}" default="—"/></td><td>
                  <form method="post" action="${pageContext.request.contextPath}/controller/private/admin" data-confirm-delete="¿Quitar esta relación?">
                    <input type="hidden" name="panel" value="relaciones"><input type="hidden" name="relacion" value="${relacion}"><input type="hidden" name="primero" value="${primero}"><input type="hidden" name="segundo" value="${item.key}"><input type="hidden" name="formToken" value="${sessionScope.formToken}">
                    <button class="button button-secondary" type="submit" name="action" value="delete">Quitar</button>
                  </form>
                </td></tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </section>
</c:if>
