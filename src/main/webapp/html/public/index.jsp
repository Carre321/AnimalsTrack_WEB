<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/html/public/common/head.jsp" %>
<body class="auth-page auth-page-login">
  <main class="auth-layout">
    <%@ include file="/html/public/common/auth-intro.jsp" %>
    <div class="auth-form-panel">
      <section class="auth-card" aria-labelledby="home-title">
        <header class="auth-header">
          <h1 id="home-title">AnimalsTrack</h1>
          <p>Gestiona los animales y las granjas de tu explotación.</p>
        </header>
        <div class="form-actions">
          <c:choose>
            <c:when test="${empty sessionScope.user}">
              <a class="button button-primary" href="${pageContext.request.contextPath}/html/public/usuario/login.jsp">Iniciar sesión</a>
              <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/public/usuario?action=register">Crear cuenta</a>
            </c:when>
            <c:otherwise>
              <a class="button button-primary" href="${pageContext.request.contextPath}/controller/private/animal?action=search">Ver animales</a>
            </c:otherwise>
          </c:choose>
        </div>
      </section>
    <%@ include file="/html/public/common/auth-footer.jsp" %>
