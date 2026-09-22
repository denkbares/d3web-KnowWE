<%--
   Tab content for the Annotate ("blame") view, included by InfoContent.jsp.
   The annotation is fetched from AnnotatePageAction when the tab is first shown, not while the page info is
   rendered. Computing it reads every version of the page, which is far too much to pay on a view that most
   readers never open. The fetching itself lives in the script below, next to the stylesheet it belongs with.
--%>
<%@ page import="org.apache.wiki.api.core.Context" %>
<%@ page import="org.apache.wiki.api.core.Page" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	Context changeAnnotationsContext = Context.findContext(pageContext);
	Page changeAnnotationsPage = changeAnnotationsContext.getPage();
	pageContext.setAttribute("changeAnnotationsPageName",
			changeAnnotationsPage == null ? null : changeAnnotationsPage.getName());
%>
<c:if test="${changeAnnotationsPageName != null}">
	<div id="changeAnnotations" data-annotate-page="<c:out value='${changeAnnotationsPageName}'/>"></div>
	<script type="text/javascript" src="KnowWEExtension/scripts/KnowWE-Plugin-ChangeAnnotations.js"></script>
</c:if>
