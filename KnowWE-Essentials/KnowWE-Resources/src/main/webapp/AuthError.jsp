<%@ page import="org.apache.wiki.*" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
	WikiEngine wiki = WikiEngine.getInstance(getServletConfig());
	WikiContext wikiContext = new WikiContext(wiki, request, WikiContext.ERROR);
%>
<c:set var="messages" value="<%= wikiContext.getWikiSession().getMessages() %>" />
<%
	wikiContext.getWikiSession().clearMessages();
%>
<!doctype html>
<html lang="en" name="top">
<head>
	<title>Forbidden</title>
	<style>
		body {
			margin: 3em auto;
			width: 50%;
			font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
		}
		h3 {
			font-size: 2em;
			font-weight: 400;
			border-bottom: 2px solid black;
		}
	</style>
</head>
<body>
<h3>Forbidden</h3>
<p>
	<strong>Sorry, but you are not allowed to do that.</strong>
</p>
<c:forEach var="message" items="${messages}">
	<p>
		<c:out value="${message}"/>
	</p>
</c:forEach>

<p>
	You can go back to the main page, back to the previous page or log out to
	switch to a user who might be able to access this resource:
</p>
<p><a href=".">Return to main page</a><br><a href="javascript:history.back();">Go back</a><br><a href="Logout.jsp">Log out</a></p>
</body>
</html>
