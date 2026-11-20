<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty requestScope.registrationPrepared}">
  <c:redirect url="/controller/public/usuario?action=register"/>
</c:if>
<%@ include file="/html/public/common/head.jsp" %>
<body class="auth-page auth-page-register">
  <main class="auth-layout">
    <aside class="auth-intro" aria-labelledby="intro-title">
      <a class="intro-brand" href="${pageContext.request.contextPath}/" aria-label="AnimalsTrack, inicio">
        <img src="${pageContext.request.contextPath}/assets/logo.png?v=2" alt="AnimalsTrack" width="132" height="132">
      </a>
      <div class="intro-content">
        <p class="intro-eyebrow">CERCA DEL CAMPO. CERCA DE TI.</p>
        <h2 id="intro-title">Tu explotación,<br>mejor organizada.</h2>
        <p class="intro-description">AnimalsTrack te ayuda a reunir la información de tus animales y llevar el seguimiento de tu explotación en un mismo lugar.</p>
        <ul class="intro-benefits">
          <li><strong>Conoce a cada animal.</strong><span>Consulta su ficha y mantén sus datos organizados.</span></li>
          <li><strong>Lleva su seguimiento.</strong><span>Ten a mano la información que necesitas en el día a día.</span></li>
          <li><strong>Dedica más tiempo al campo.</strong><span>Encuentra lo que buscas sin perderte entre papeles.</span></li>
        </ul>
      </div>
      <p class="intro-caption">AnimalsTrack · Gestión y seguimiento de animales</p>
    </aside>
    <div class="auth-form-panel">
      <section class="auth-card auth-card-wide" aria-labelledby="register-title">
        <header class="auth-header">
          <h1 id="register-title">Crear cuenta de ganadero</h1>
        </header>
        <c:if test="${not empty requestScope.registrationError}">
          <p class="error" role="alert"><c:out value="${requestScope.registrationError}"/></p>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/controller/public/usuario" class="auth-form">
          <input type="hidden" name="action" value="register">
          <div class="field-row">
          <div class="field">
            <label for="nombre">Nombre</label>
            <input class="input" id="nombre" name="nombre" type="text" required maxlength="60" autocomplete="given-name" value="<c:out value='${param.nombre}'/>">
          </div>
          <div class="field">
            <label for="apellidos">Apellidos</label>
            <input class="input" id="apellidos" name="apellidos" type="text" required maxlength="80" autocomplete="family-name" value="<c:out value='${param.apellidos}'/>">
          </div>
          </div>
          <div class="field">
            <label for="dni">DNI o NIE</label>
            <input class="input" id="dni" name="dni" type="text" required maxlength="9" value="<c:out value='${param.dni}'/>">
          </div>
          <div class="field">
          <label for="municipioId">Municipio (provincia)</label>
          <select class="input" id="municipioId" name="municipioId" required>
            <option value="">Selecciona tu municipio</option>
            <c:forEach var="municipio" items="${requestScope.municipios}">
              <option value="${municipio.id}" <c:if test="${param.municipioId eq municipio.id.toString()}">selected</c:if>><c:out value="${municipio.nombre}"/> (<c:out value="${requestScope.provincias[municipio.provinciaId]}"/>)</option>
            </c:forEach>
          </select>
        </div>
          <div class="field">
            <label for="reg-email">Correo electrónico</label>
            <input class="input" id="reg-email" name="email" type="email" required maxlength="200" autocomplete="email" value="<c:out value='${param.email}'/>">
          </div>
          <div class="field-row">
            <div class="field">
              <label for="reg-password">Contraseña</label>
              <input class="input" id="reg-password" type="password" name="password" required minlength="8" maxlength="72" autocomplete="new-password" aria-describedby="password-help">
              <small id="password-help">Mínimo 8 caracteres.</small>
            </div>
            <div class="field">
              <label for="confirmPassword">Repetir contraseña</label>
              <input class="input" id="confirmPassword" type="password" name="confirmPassword" required minlength="8" maxlength="72" autocomplete="new-password">
            </div>
          </div>
          <div class="form-actions">
            <button class="button button-primary" type="submit" <c:if test="${empty requestScope.municipios}">disabled</c:if>>Crear cuenta</button>
          </div>
        </form>
        <p class="auth-switch">¿Ya tienes cuenta? <a href="${pageContext.request.contextPath}/html/public/usuario/login.jsp">Iniciar sesión</a></p>
      </section>
    <%@ include file="/html/public/common/auth-footer.jsp" %>
