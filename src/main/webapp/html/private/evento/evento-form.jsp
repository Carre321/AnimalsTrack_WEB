<%@ page pageEncoding="UTF-8" %>
<form class="event-record-form" action="${pageContext.request.contextPath}/controller/private/evento" method="post">
  <input type="hidden" name="action" value="${empty evento ? 'create' : 'update'}">
  <input type="hidden" name="granjaId" value="${sessionScope.granjaId}">
  <c:if test="${not empty evento}"><input type="hidden" name="id" value="${evento.id}"></c:if>
  <section class="record-panel" aria-label="Datos del evento">
    <div class="event-record-fields">
      <div class="field">
        <label for="event-animal">Animal</label>
        <c:set var="selectedValue" value="${eventoSubmitted ? param.animalId : evento.animalId}"/>
        <select class="input" id="event-animal" name="animalId" required>
          <option value="">Selecciona</option>
          <c:forEach var="item" items="${animales}">
            <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}" default="Sin nombre"/> · <c:out value="${item.crotal}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="event-type">Tipo de evento</label>
        <c:set var="selectedValue" value="${eventoSubmitted ? param.tipoEventoId : evento.tipoEventoId}"/>
        <select class="input" id="event-type" name="tipoEventoId" required>
          <option value="">Selecciona</option>
          <c:forEach var="item" items="${tiposEvento}">
            <option value="${item.id}" data-tipo="<c:out value='${item.codigo}'/>" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="event-vet">Veterinario</label>
        <c:set var="selectedValue" value="${eventoSubmitted ? param.veterinarioId : evento.veterinarioId}"/>
        <select class="input" id="event-vet" name="veterinarioId">
          <option value="">Sin especificar</option>
          <c:forEach var="item" items="${veterinarios}">
            <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombreCompleto}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field">
        <label for="event-date">Fecha</label>
        <c:set var="fechaEvento" value="${eventoSubmitted ? param.fecha : evento.fechaHora.toLocalDate()}"/>
        <input class="input" id="event-date" name="fecha" type="date" value="<c:out value='${fechaEvento}'/>" required>
      </div>
      <div class="field">
        <label for="event-time">Hora</label>
        <c:set var="horaEvento" value="${eventoSubmitted ? param.hora : evento.fechaHora.toLocalTime()}"/>
        <input class="input" id="event-time" name="hora" type="time" value="<c:out value='${horaEvento}'/>" step="1">
      </div>
      <div class="field">
        <label for="event-price">Precio (€)</label>
        <input class="input" id="event-price" name="precio" type="number" value="<c:out value='${eventoSubmitted ? param.precio : evento.precioEvento}'/>" min="0" max="99999999.99" step="0.01">
      </div>
      <div class="field event-specific-result">
        <label for="event-result">Resultado</label>
        <input class="input" id="event-result" name="resultado" type="text" value="<c:out value='${eventoSubmitted ? param.resultado : evento.resultado}'/>" maxlength="30">
      </div>
      <div class="field event-specific-seed">
        <label for="event-seed">Semilla</label>
        <c:set var="selectedValue" value="${eventoSubmitted ? param.semillaId : evento.semillaId}"/>
        <select class="input" id="event-seed" name="semillaId">
          <option value="">Sin especificar</option>
          <c:forEach var="item" items="${semillas}">
            <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.codigo}"/> · <c:out value="${item.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field event-specific-treatment">
        <label for="event-treatment">Tratamiento</label>
        <c:set var="selectedValue" value="${eventoSubmitted ? param.tratamientoId : evento.tratamientoId}"/>
        <select class="input" id="event-treatment" name="tratamientoId">
          <option value="">Sin especificar</option>
          <c:forEach var="item" items="${tratamientos}">
            <option value="${item.id}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field event-specific-treatment">
        <label for="event-dose">Dosis</label>
        <c:set var="selectedValue" value="${eventoSubmitted ? param.dosisId : evento.dosisId}"/>
        <select class="input" id="event-dose" name="dosisId">
          <option value="">Sin especificar</option>
          <c:forEach var="item" items="${dosis}">
            <option value="${item.id}" data-tratamiento="${item.tratamientoId}" <c:if test="${selectedValue eq item.id.toString()}">selected</c:if>>Dosis <c:out value="${item.numOrdenDosis}"/></option>
          </c:forEach>
        </select>
      </div>
      <div class="field event-notes">
        <label for="event-notes">Observaciones</label>
        <textarea class="input" id="event-notes" name="observaciones" rows="3" maxlength="500"><c:out value='${eventoSubmitted ? param.observaciones : evento.observaciones}'/></textarea>
      </div>
    </div>
  </section>
  <div class="record-actions">
    <button class="button button-primary" type="submit" <c:if test="${empty animales or empty tiposEvento}">disabled</c:if>>
      <img src="${pageContext.request.contextPath}/assets/iconos/save.png" alt="">${empty evento ? 'Guardar' : 'Actualizar'}
    </button>
    <c:url var="cancelUrl" value="/controller/private/evento">
      <c:param name="action" value="${empty evento ? 'search' : 'detail'}"/>
      <c:if test="${not empty evento}"><c:param name="id" value="${evento.id}"/></c:if>
    </c:url>
    <a class="button button-secondary" href="<c:out value='${cancelUrl}'/>"><img src="${pageContext.request.contextPath}/assets/iconos/cancel.png" alt="">Cancelar</a>
  </div>
</form>
