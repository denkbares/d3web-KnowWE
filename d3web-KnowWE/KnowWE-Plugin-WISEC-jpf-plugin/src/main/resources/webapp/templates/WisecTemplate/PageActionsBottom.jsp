<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  WikiPage wikipage = c.getPage();
%>
<wiki:CheckRequestContext context='view|diff|edit|upload|info'>
<div id='actionsBottom' class="pageactions"> 
  <wiki:PageExists>  

    <a href="#top" 
      class="action quick2top" 
      title="<fmt:message key='actions.gototop'/>" >&laquo;</a>

    <wiki:CheckVersion mode="latest">
       <fmt:message key="info.lastmodified">
          <fmt:param><wiki:PageVersion /></fmt:param>
          <fmt:param><wiki:DiffLink version="latest" newVersion="previous"><wiki:PageDate format='${prefs["DateFormat"]}'/></wiki:DiffLink></fmt:param>
          <fmt:param><wiki:Author /></fmt:param>
       </fmt:message>
    </wiki:CheckVersion>

    <wiki:CheckVersion mode="notlatest">
      <fmt:message key="actions.publishedon">
         <fmt:param><wiki:PageDate format='${prefs["DateFormat"]}'/></fmt:param>
         <fmt:param><wiki:Author /></fmt:param>
      </fmt:message>
    </wiki:CheckVersion>
   
   <wiki:Permission permission="edit">
	<wiki:UserCheck exists="true">
		<wiki:EditLink>Edit this page</wiki:EditLink>
	</UserCheck>
	<wiki:UserCheck exists="false">
		<wiki:LinkTo page="UserPreferences">Set your User Preferences to edit this page</wiki:LinkTo>
	</UserCheck>
	
   </wiki:Permission>	
   
   <wiki:RSSImageLink mode="wiki" />

  </wiki:PageExists>

  <wiki:NoSuchPage><fmt:message key="actions.notcreated"/></wiki:NoSuchPage> 
</div>
</wiki:CheckRequestContext>