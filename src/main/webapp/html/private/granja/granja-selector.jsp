<%@ page pageEncoding="UTF-8" %>
<form method="post" action="${pageContext.request.contextPath}/controller/private/granja" class="farm-picker" data-farm-picker>
  <input type="hidden" name="action" value="select">
  <c:if test="${section eq 'evento'}"><input type="hidden" name="seccion" value="evento"></c:if>
  <c:if test="${section eq 'inicio'}"><input type="hidden" name="seccion" value="inicio"></c:if>
  <label for="granjaId">Granja</label>
  <select id="granjaId" name="granjaId" <c:if test="${empty sessionScope.granjas}">disabled</c:if>>
    <c:if test="${empty sessionScope.granjas}"><option value="">Sin granjas disponibles</option></c:if>
    <c:forEach var="granja" items="${sessionScope.granjas}">
      <option value="${granja.id}" <c:if test="${sessionScope.granjaId eq granja.id}">selected</c:if>><c:out value="${granja.rega}"/> — <c:out value="${granja.nombre}"/></option>
    </c:forEach>
  </select>
  <noscript><button type="submit" class="button button-secondary" <c:if test="${empty sessionScope.granjas}">disabled</c:if>>Cambiar granja</button></noscript>
</form>
