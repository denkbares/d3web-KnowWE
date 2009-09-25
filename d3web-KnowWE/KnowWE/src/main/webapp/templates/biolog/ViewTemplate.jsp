<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setBundle basename="templates.default"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html id="top" xmlns="http://www.w3.org/1999/xhtml">

<head>
  <title>
    <fmt:message key="view.title.view">
      <fmt:param><wiki:Variable var="ApplicationName" /></fmt:param>
      <fmt:param><wiki:PageName /></fmt:param>
    </fmt:message>
  </title>
  <wiki:Include page="commonheader.jsp"/>
  <wiki:CheckVersion mode="notlatest">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckVersion>
  <wiki:CheckRequestContext context="diff|info">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckRequestContext>
  <wiki:CheckRequestContext context="!view">
    <meta name="robots" content="noindex,follow" />
  </wiki:CheckRequestContext>
</head>

<body class="view">
                <%--start change --%>
				<table id="biolog">
						<tbody>
						<!-- <tr>
	                        <td><img alt="" src="templates/biolog/images/oben_links.jpg"/></td>
							<td><img alt="" src="templates/biolog/images/oben_mitte.jpg"/></td>
							<td><img alt="" src="templates/biolog/images/oben_rechts.jpg"/></td>
						</tr>-->
						<tr>
							<td background="templates/biolog/images/mitte_links.jpg"> </td>
							<td width="780">
                <%--end change --%>

	<div id="wikibody" class="${prefs.Orientation}">
	  <wiki:Include page="Header.jsp" />
	  <div id="content">
	    <div id="page">
		      <wiki:Include page="PageActionsTop.jsp"/>
		      <wiki:Content/>
		      <%--start change --%><%--<wiki:Include page="PageActionsBottom.jsp"/>--%><%--end change --%>
	    </div>
	    <wiki:Include page="Favorites.jsp"/>
		<div class="clearbox"></div>
	  </div>
	  <wiki:Include page="Footer.jsp" />
	</div>
								
					<%--start change --%>
							</td>
							<td width="10" background="templates/biolog/images/mitte_rechts.jpg"> </td>
						</tr>
						<!-- <tr>
							<td width="10" height="10"><img width="10" height="10" border="0" alt="" src="templates/biolog/images/unten_links.jpg"/></td>
							<td width="780" height="10"><img width="780" height="10" border="0" alt="" src="templates/biolog/images/unten_mitte.jpg"/></td>
							<td width="10" height="10"><img width="10" height="10" border="0" alt="" src="templates/biolog/images/unten_rechts.jpg"/></td>
						</tr>-->
					</tbody></table>
					<%--end change --%>				
</body>
</html>