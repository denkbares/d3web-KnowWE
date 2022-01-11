
<%@ page import="org.apache.wiki.*" %>
<%@ page import="de.knowwe.jspwiki.*" %>
<%@ page import="de.knowwe.core.user.*" %>
<%@ page import="de.knowwe.core.action.*" %>
<%@ page import="de.knowwe.core.utils.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="de.knowwe.core.*" %>
<%@ page import="com.denkbares.strings.Strings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%//Create wiki context; authorization check not needed
	WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    WikiContext wikiContext = new WikiContext( wiki, request, WikiContext.VIEW );

    // Check if KnowWE is initialized
    if (!Environment.isInitialized()) {
    	Environment.initInstance(new JSPWikiConnector(wiki));
	}

	// We need to do this, because the paramterMap is locked!
	Map<String, String> parameters = UserContextUtil.getParameters(request);
	
	// Add user
	if (!parameters.containsKey(Attributes.USER)) {
		parameters.put(Attributes.USER, wikiContext.getWikiSession().getUserPrincipal().getName());
	}
	
	// Add topic
	if (!parameters.containsKey(Attributes.TOPIC)) {
		String topic = parameters.get("page");
		if (topic == null) {
			topic = Strings.decodeURL(wikiContext.getPage().getName());
		}
		parameters.put(Attributes.TOPIC, topic);
	}
	
	// Add web
	if(!parameters.containsKey(Attributes.WEB)) {
		parameters.put(Attributes.WEB, "default_web");
	}
	
	// Create AuthenticationManager instance
	AuthenticationManager manager = new JSPAuthenticationManager(wikiContext);
	
	// Create action context
	UserActionContext context = new ActionContext(parameters.get("action"), AbstractActionServlet.getActionFollowUpPath(request), parameters, request, response, wiki.getServletContext(), manager);
	
	// Perform action
	JSPActionDispatcher.getInstance().performAction(context);%>
