<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/html/public/common/head.jsp" %>
<body class="workspace-page">
<a class="skip-link" href="#contenido">Ir al contenido</a>
<input class="sidebar-toggle visually-hidden" type="checkbox" id="sidebar-collapsed" aria-label="Contraer menú lateral" aria-controls="desktop-navigation">
<div class="workspace-layout">
  <header class="workspace-topbar">
    <a class="workspace-brand" href="${pageContext.request.contextPath}/controller/private/inicio">
      <img src="${pageContext.request.contextPath}/assets/logo.png" alt="" width="62" height="62">
      <span>AnimalsTrack<small>Gestión ganadera</small></span>
    </a>
    <details class="mobile-navigation">
      <summary aria-label="Abrir menú de navegación">☰ <span>Menú</span></summary>
      <nav aria-label="Secciones en móvil">
        <%@ include file="/html/public/common/workspace-links.jsp" %>
      </nav>
    </details>
    <%@ include file="/html/private/granja/granja-selector.jsp" %>
    <%@ include file="/html/public/usuario/user-menu.jsp" %>
  </header>
  <aside class="workspace-sidebar" aria-label="Navegación principal">
    <label class="sidebar-switch" for="sidebar-collapsed" title="Expandir o contraer menú">
      <span class="hamburger-icon" aria-hidden="true"><i></i><i></i><i></i></span><span class="sidebar-text">Menú</span>
    </label>
    <nav class="workspace-nav" id="desktop-navigation" aria-label="Secciones">
      <%@ include file="/html/public/common/workspace-links.jsp" %>
    </nav>
  </aside>
  <div class="workspace-body">
    <main class="workspace-content" id="contenido">
      <c:if test="${not empty requestScope.granjaError}">
        <p class="error" role="alert"><c:out value="${requestScope.granjaError}"/></p>
      </c:if>
