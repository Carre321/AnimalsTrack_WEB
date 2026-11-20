<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/html/public/common/head.jsp" %>
<body class="auth-page auth-page-login">
  <main class="auth-layout">
    <%@ include file="/html/public/common/auth-intro.jsp" %>
    <div class="auth-form-panel">
    <section class="auth-card" aria-labelledby="login-title">
      <header class="auth-header">
        <h1 id="login-title">Iniciar sesión</h1>
        <p>Accede a tu espacio de gestión ganadera.</p>
      </header>

      <c:if test="${param.registered eq '1'}">
        <p class="notice" role="status">Tu cuenta se ha creado. Ya puedes iniciar sesión.</p>
      </c:if>
      <c:if test="${not empty requestScope.loginError}">
        <p class="error" role="alert"><c:out value="${requestScope.loginError}"/></p>
      </c:if>
      <form class="auth-form" action="${pageContext.request.contextPath}/controller/public/usuario" method="post">
        <input type="hidden" name="action" value="login">
        <div class="field">
          <label for="email">Correo electrónico o usuario</label>
          <input class="input" id="email" name="login" type="text" maxlength="254" placeholder="Correo electrónico o usuario" autocomplete="username" spellcheck="false" autocapitalize="none" required>
        </div>

        <div class="field">
          <label for="password">Contraseña</label>
          <input class="input" id="password" name="password" type="password" maxlength="256" placeholder="Tu contraseña" autocomplete="current-password" required>
        </div>

        <div class="form-actions">
          <button class="button button-primary" type="submit">Entrar</button>
          <a class="button button-secondary" href="${pageContext.request.contextPath}/">Salir</a>
        </div>
      </form>
      <p class="auth-switch">¿Todavía no tienes cuenta? <a href="${pageContext.request.contextPath}/controller/public/usuario?action=register">Crear cuenta</a></p>
    </section>

    <%@ include file="/html/public/common/auth-footer.jsp" %>
