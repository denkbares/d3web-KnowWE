<%@ page import="com.ecyrd.jspwiki.*" %><%@ page import="de.d3web.we.jspwiki.*" %><%@ page import="de.d3web.we.javaEnv.*" %><%@ page import="de.d3web.we.core.*" %><%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %><%!
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
	KnowWEParameterMap map = new KnowWEParameterMap(context,request,wiki.getServletContext(),env);
	context.setUrlParameter(map);
	
	map.put("KWikiUser",wikiContext.getWikiSession().getUserPrincipal().getName());
	if(!map.containsKey("KWiki_Topic")) {
		//map.put("KWiki_Topic", wikiContext.getPage().getName());
	}
	String result = env.getDispatcher().performAction(map);

%><%=result%><wiki:Include page="<%=\"\"%>" />
