<%@ page pageEncoding="UTF-8" %>
<form method="post" action="${pageContext.request.contextPath}/controller/private/animal-ficha">
    <input type="hidden" name="formToken" value="${sessionScope.formToken}">
    <input type="hidden" name="granjaId" value="${sessionScope.granjaId}">
    <c:if test="${not empty animal}"><input type="hidden" name="id" value="${animal.id}"></c:if>
  <input type="hidden" name="action" value="${empty animal ? 'create' : 'update'}">
  <section class="record-panel" aria-label="Datos del animal">
  <div class="record-fields">
    <div class="field">
      <label for="a-crotal">Crotal</label>
      <input class="input" id="a-crotal" name="crotal" type="text" required maxlength="30" value="<c:out value='${animalSubmitted ? param.crotal : animal.crotal}'/>">
    </div>
    <div class="field">
      <label for="a-nombre">Nombre</label>
      <input class="input" id="a-nombre" name="nombre" type="text" maxlength="80" value="<c:out value='${animalSubmitted ? param.nombre : animal.nombre}'/>">
    </div>
    <div class="field">
      <label for="a-sexoId">Sexo</label>
      <c:set var="selectedValue" value="${animalSubmitted ? param.sexoId : animal.sexoId}"/>
      <select class="input" id="a-sexoId" name="sexoId" required>
        <option value="">Selecciona</option>
        <c:forEach var="item" items="${sexos}">
          <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
        </c:forEach>
      </select>
    </div>
    <div class="field">
      <label for="a-razaId">Raza</label>
      <c:set var="selectedValue" value="${animalSubmitted ? param.razaId : animal.razaId}"/>
      <select class="input" id="a-razaId" name="razaId" >
        <option value="">Sin especificar</option>
        <c:forEach var="item" items="${razas}">
          <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
        </c:forEach>
      </select>
    </div>
    <div class="field">
      <label for="a-fechaNacimiento">Fecha de nacimiento</label>
      <input class="input" id="a-fechaNacimiento" name="fechaNacimiento" type="date"  value="<c:out value='${animalSubmitted ? param.fechaNacimiento : animal.fechaNacimiento}'/>">
    </div>
    <c:if test="${not empty animal}">
    <div class="field">
      <label for="a-fechaBaja">Fecha de baja</label>
      <input class="input" id="a-fechaBaja" name="fechaBaja" type="date"  value="<c:out value='${animalSubmitted ? param.fechaBaja : animal.fechaBaja}'/>">
    </div>
    </c:if>
    <div class="field">
      <label for="a-madreInternaId">Madre interna</label>
      <c:set var="selectedValue" value="${animalSubmitted ? param.madreInternaId : animal.madreInternaId}"/>
      <select class="input" id="a-madreInternaId" name="madreInternaId" >
        <option value="">Sin especificar</option>
        <c:forEach var="item" items="${progenitores}">
          <c:if test="${item.id ne animal.id}">
            <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.crotal}"/> · <c:out value="${item.nombre}"/></option>
          </c:if>
        </c:forEach>
      </select>
    </div>
    <div class="field">
      <label for="a-madreExternaCrotal">Crotal de madre externa</label>
      <input class="input" id="a-madreExternaCrotal" name="madreExternaCrotal" type="text" maxlength="45" value="<c:out value='${animalSubmitted ? param.madreExternaCrotal : animal.madreExternaCrotal}'/>">
    </div>
    <div class="field">
      <label for="a-padreInternoId">Padre interno</label>
      <c:set var="selectedValue" value="${animalSubmitted ? param.padreInternoId : animal.padreInternoId}"/>
      <select class="input" id="a-padreInternoId" name="padreInternoId" >
        <option value="">Sin especificar</option>
        <c:forEach var="item" items="${progenitores}">
          <c:if test="${item.id ne animal.id}">
            <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.crotal}"/> · <c:out value="${item.nombre}"/></option>
          </c:if>
        </c:forEach>
      </select>
    </div>
  </div>
  </section>
  <div class="record-actions">
    <button class="button button-primary" type="submit">${empty animal ? 'Guardar' : 'Actualizar'}</button>
    <c:choose>
      <c:when test="${empty animal}"><a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/animal?action=search">Cancelar</a></c:when>
      <c:otherwise><a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/animal-ficha?action=detail&amp;id=${animal.id}">Cancelar</a></c:otherwise>
    </c:choose>
  </div>
</form>
