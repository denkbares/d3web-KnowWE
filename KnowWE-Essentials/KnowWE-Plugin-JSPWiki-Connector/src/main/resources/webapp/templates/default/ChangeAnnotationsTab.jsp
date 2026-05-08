<%--
   Tab content for the Annotate ("blame") view, included by InfoContent.jsp.
   Renders the <knowwe-page-annotate> server-side via AnnotateRenderHelper — no AJAX.
   Tomcat surfaces any exception directly in the page, which makes diagnostics easier
   than swallowing it in a fetch() error handler.
--%>
<%@ page import="org.apache.wiki.api.core.Context" %>
<%@ page import="org.apache.wiki.api.core.Page" %>
<%@ page import="de.knowwe.jspwiki.changeannotations.AnnotateRenderHelper" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	Context changeAnnotationsContext = Context.findContext(pageContext);
	Page changeAnnotationsPage = changeAnnotationsContext.getPage();
	String changeAnnotationsHtml = null;
	String changeAnnotationsError = null;
	if (changeAnnotationsPage != null) {
		try {
			changeAnnotationsHtml = AnnotateRenderHelper.renderHtml(
					changeAnnotationsContext.getEngine(),
					changeAnnotationsPage.getName(),
					session);
		}
		catch (IllegalArgumentException e) {
			changeAnnotationsError = e.getMessage();
		}
	}
	pageContext.setAttribute("changeAnnotationsHtml", changeAnnotationsHtml);
	pageContext.setAttribute("changeAnnotationsError", changeAnnotationsError);
%>
<c:if test="${changeAnnotationsHtml != null}">
	${changeAnnotationsHtml}
</c:if>
<c:if test="${changeAnnotationsError != null}">
	<p class="warning">No annotation available: <c:out value="${changeAnnotationsError}"/></p>
</c:if>
