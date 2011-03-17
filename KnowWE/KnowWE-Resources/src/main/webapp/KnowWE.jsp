<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="de.d3web.we.jspwiki.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="de.d3web.we.action.*" %>
<%@ page import="de.d3web.we.core.*" %>
<%@ page import="de.d3web.we.utils.*" %>
<%@ page import="de.d3web.we.user.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %><%!
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
	//Create wiki context; authorization check not needed
	WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );

    // Check if KnowWE is initialized
    if (!KnowWEEnvironment.isInitialized()) {
		KnowWEEnvironment.initKnowWE(new JSPWikiKnowWEConnector(wiki));
	}

	// We need to do this, because the paramterMap is locked!
	Map<String, String> parameters = UserContextUtil.getParameters(request);
	
	// Add user
	if (!parameters.containsKey(KnowWEAttributes.USER)) {
		parameters.put(KnowWEAttributes.USER, wikiContext.getWikiSession().getUserPrincipal().getName());
	}
	
	// Add topic
	if (!parameters.containsKey(KnowWEAttributes.TOPIC)) {
		String topic = parameters.get("page");
		if (topic == null) {
			topic = KnowWEUtils.urldecode(wikiContext.getPage().getName());
		}
		parameters.put(KnowWEAttributes.TOPIC, topic);
	}
	
	// Add web
	if(!parameters.containsKey(KnowWEAttributes.WEB)) {
		parameters.put(KnowWEAttributes.WEB, "default_web");
	}
	
	// Create AuthenticationManager instance
	AuthenticationManager manager = new JSPAuthenticationManager(wikiContext);
	
	// Create action context
	UserActionContext context = new ActionContext(parameters.get("action"), AbstractActionServlet.getActionFollowUpPath(request), parameters, request, response, wiki.getServletContext(), manager);
	
	// Perform action
	KnowWEEnvironment.getInstance().getDispatcher().performAction(context);
	
%><wiki:Include page="<%=\"\"%>"/>