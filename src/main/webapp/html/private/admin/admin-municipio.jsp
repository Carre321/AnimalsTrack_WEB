<%@ page pageEncoding="UTF-8" %>
<div class="field">
  <label for="f-municipioId">Municipio (provincia)</label>
  <c:set var="municipioElegido" value="${adminSubmitted ? param.municipioId : ficha.municipioId}"/>
  <select class="input" id="f-municipioId" name="municipioId" required>
    <option value="">Selecciona</option>
    <c:forEach var="item" items="${municipios}">
      <option value="${item.id}" <c:if test="${municipioElegido eq item.id.toString()}">selected</c:if>><c:out value="${item.nombre}"/> (<c:out value="${provincias[item.provinciaId]}"/>)</option>
    </c:forEach>
  </select>
</div>
