<%@ page isErrorPage="true" %><%@ page import="org.apache.log4j.*" %><%@ page import="com.ecyrd.jspwiki.*" %><%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" 
     prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    WikiContext wikiContext = wiki.createContext( request, 
                                                  WikiContext.ERROR );
    String pagereq = wikiContext.getName();
    String xRequestedWith = request.getHeader("X-Requested-With");
    
    String message = "";
    Object messageObj = request.getAttribute("javax.servlet.error.message");
    if (messageObj != null) {
    	message = messageObj.toString();
    }
    
    // If the request was sent by AJAX, just return a plain error message
    if (xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest")) {
        response.setContentType("text/plain; charset="+wiki.getContentEncoding() );
        response.getWriter().append(message);
    } else {
        response.setContentType("text/html; charset="+wiki.getContentEncoding() );%>
<html>
  <body>
    <h3>Forbidden</h3> 
    <p> 
      <strong>Sorry, but you are not allowed to do that.</strong> 
    </p>
	<% if (!message.equals("")) { %>
		<c:set var="message" value="<%= message %>" />
		<p>
	  		Additional error message: <strong>${fn:escapeXml(message) }</strong>
		</p>
	<% } %> 
    <p> 
      Usually we block access to
      something because you do not have the correct privileges (<em>e.g.</em>,
      read, edit, comment) for the page you are looking for. In this particular case,
      it is likely that you are not listed in the page&rsquo;s access control list
      or that your privileges aren&rsquo;t high enough (you want
      to edit, but ACL only allows &lsquo;read&rsquo;).
    </p> 
    <p> 
      It is also possible that JSPWiki cannot find its security policy, or that
      the policy is not configured correctly. Either of these cases would cause
      JSPWiki to block access, too.
    </p> 
    <p><a href=".">Better luck next time.</a></p>
  </body> 
</html> 
<%    	
    }
%>