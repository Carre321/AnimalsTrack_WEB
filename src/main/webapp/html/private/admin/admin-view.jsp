<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty adminPrepared}"><c:redirect url="/controller/private/admin"/></c:if>
<c:set var="section" value="admin"/>
<%@ include file="/html/public/common/header.jsp" %>
<div class="page-heading"><h1>Administración</h1></div>
<c:if test="${not empty adminError}"><p class="error" role="alert"><c:out value="${adminError}"/></p></c:if>
<c:if test="${param.saved eq '1'}"><p class="notice" role="status">El cambio se ha guardado.</p></c:if>
<nav class="record-actions admin-subtabs" aria-label="Administración">
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=usuarios" <c:if test="${panel eq 'usuarios'}">aria-current="page"</c:if>>Usuarios</a>
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=granjas" <c:if test="${panel eq 'granjas'}">aria-current="page"</c:if>>Granjas</a>
  <a class="button button-secondary" href="${pageContext.request.contextPath}/controller/private/admin?panel=relaciones" <c:if test="${panel eq 'relaciones'}">aria-current="page"</c:if>>Relaciones</a>
</nav>
<c:choose>
  <c:when test="${panel eq 'granjas'}"><%@ include file="/html/private/admin/admin-granjas.jsp" %></c:when>
  <c:when test="${panel eq 'relaciones'}"><%@ include file="/html/private/admin/admin-relaciones.jsp" %></c:when>
  <c:otherwise><%@ include file="/html/private/admin/admin-usuarios.jsp" %></c:otherwise>
</c:choose>
<%@ include file="/html/public/common/footer.jsp" %>
