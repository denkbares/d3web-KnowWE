<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="de.d3web.we.jspwiki.*" %>
<%@ page import="java.util.*" %>
<%@ page import="de.d3web.we.core.*" %>
<%@ page import="de.d3web.we.javaEnv.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="de.d3web.we.action.*" %>
<%@ page contentType="text/xml" %>

<%!
String findParam( PageContext ctx, String key )
    {
        ServletRequest req = ctx.getRequest();
        String val = req.getParameter( key );
        if( val == null )
        {
            val = (String)ctx.findAttribute( key );
        }
        return val;
    }
%><%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context; authorization check not needed
    WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );

    if (!KnowWEEnvironment.isInitialized()) {
		KnowWEEnvironment.initKnowWE(new JSPWikiKnowWEConnector(wiki));
	}
	KnowWEEnvironment env = KnowWEEnvironment.getInstance();
	KnowWEUserContextImpl userContext =  new KnowWEUserContextImpl(wikiContext.getWikiSession().getUserPrincipal().getName(), wikiContext.getHttpRequest().getParameterMap());
	KnowWEParameterMap map = new KnowWEParameterMap(userContext, request, response, wiki.getServletContext(), env);
	
	map.put("KWikiUser",wikiContext.getWikiSession().getUserPrincipal().getName());
	if(!map.containsKey("KWiki_Topic")) {
		//map.put("KWiki_Topic", wikiContext.getPage().getName());
	}
	
	env.getDispatcher().performAction(map);
	
	response.setHeader("Content-Type", "text/xml");

%><!--%=result%--><wiki:Include page="<%=\"\"%>" />
