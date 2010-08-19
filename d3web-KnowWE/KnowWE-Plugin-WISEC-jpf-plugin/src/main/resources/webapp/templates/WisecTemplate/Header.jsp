<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  String frontpage = c.getEngine().getFrontPage(); 
%>
<div id="header">
	<div class="applicationlogo" > 
		
	</div>
	<%-- start change --%>
	<ul class="wisec_top">
		<li>
			<wiki:UserCheck status="notAuthenticated">
				<wiki:CheckRequestContext context='!login'>
					<wiki:Permission permission="login">
						<a href="<wiki:Link jsp='Login.jsp' format='url'><wiki:Param 
						name='redirect' value='<%=c.getEngine().encodeName(c.getName())%>'/></wiki:Link>" 
						class="login"
						title="<fmt:message key='actions.login.title'/>"><fmt:message key="actions.login"/></a>
					</wiki:Permission>
				</wiki:CheckRequestContext>
			</wiki:UserCheck>
			<wiki:UserCheck status="authenticated">
				<a href="<wiki:Link jsp='Logout.jsp' format='url' />" 
				class="logout"
				title="<fmt:message key='actions.logout.title'/>"><fmt:message key="actions.logout"/></a>
			</wiki:UserCheck>
		</li>
		<li>|</li>
		<li><a href="http://reach-info.de/kontakt.htm">Contact</a></li>
		<li>|</li>
		<li><a href="http://reach-info.de/impressum.htm">Legal notice</a></li>
		<li>|</li>
		<li><a href="<wiki:LinkTo page='<%=frontpage%>' format='url' />"
			title="<fmt:message key='actions.home.title' ><fmt:param><%=frontpage%></fmt:param></fmt:message> "><fmt:message key='actions.home' /></a>
	</ul>
	


	<div id="WISEC" style="width:80;">
		<img src="templates/WisecTemplate/images/wisec_knowwe.png" style="float:none"/>
	</div>

	<div class="clearbox"></div>

</div>