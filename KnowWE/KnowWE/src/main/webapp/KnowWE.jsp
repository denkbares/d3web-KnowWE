<%@ page import="com.ecyrd.jspwiki.*" %><%@ page import="de.d3web.we.jspwiki.*" %><%@ page import="de.d3web.we.core.*" %><%@ page import="de.d3web.we.utils.*" %><%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %><%!
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
	JSPWikiUserContext context = new JSPWikiUserContext(wikiContext);
	KnowWEParameterMap map = new KnowWEParameterMap(context,request,response,wiki.getServletContext(),env);
	context.setUrlParameter(map);
	
	map.put("KWikiUser",wikiContext.getWikiSession().getUserPrincipal().getName());
	if(!map.containsKey("KWiki_Topic")) {
		//map.put("KWiki_Topic", wikiContext.getPage().getName());
	}
	
	env.getDispatcher().performAction(map);
	
%><wiki:Include page="<%=\"\"%>"/>