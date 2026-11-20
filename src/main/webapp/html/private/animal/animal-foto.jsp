<%@ page pageEncoding="UTF-8" %>
<section class="record-panel profile-photo" aria-label="Foto del animal">
  <h2>Foto del animal</h2>
  <c:choose>
    <c:when test="${hasAnimalPhoto}">
      <img class="animal-record-photo" alt="Foto del animal" src="${pageContext.request.contextPath}/controller/private/animal-ficha?action=photo&amp;id=${animal.id}">
    </c:when>
    <c:otherwise><p class="profile-photo-empty">Sin foto</p></c:otherwise>
  </c:choose>
  <c:if test="${canEditAnimal}">
    <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/controller/private/animal-ficha">
    <input type="hidden" name="formToken" value="${sessionScope.formToken}">
    <input type="hidden" name="granjaId" value="${sessionScope.granjaId}">
    <c:if test="${not empty animal}"><input type="hidden" name="id" value="${animal.id}"></c:if>
      <input type="hidden" name="action" value="updatePhoto">
      <div class="field"><label for="animalFoto">Seleccionar foto</label>
        <input class="input" id="animalFoto" name="foto" type="file" accept="image/jpeg,image/png,image/gif" required>
      </div>
      <div class="record-actions"><button class="button button-primary" type="submit">Guardar foto</button></div>
    </form>
    <c:if test="${hasAnimalPhoto}">
      <form method="post" action="${pageContext.request.contextPath}/controller/private/animal-ficha">
    <input type="hidden" name="formToken" value="${sessionScope.formToken}">
    <input type="hidden" name="granjaId" value="${sessionScope.granjaId}">
    <c:if test="${not empty animal}"><input type="hidden" name="id" value="${animal.id}"></c:if>
        <input type="hidden" name="action" value="removePhoto">
        <button class="button button-secondary" type="submit">Quitar foto</button>
      </form>
    </c:if>
  </c:if>
</section>
