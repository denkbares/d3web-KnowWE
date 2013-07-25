<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN">
<%@page import="de.knowwe.core.wikiConnector.WikiConnector"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="de.d3web.plugin.Extension"%>
<%@ page import="de.d3web.plugin.JPFPluginManager"%>
<%@ page import="de.knowwe.core.kdom.parsing.Section"%>
<%@ page import="de.knowwe.core.kdom.parsing.Sections"%>
<%@ page import="de.knowwe.core.kdom.Article"%>
<%@ page import="de.knowwe.diaflux.type.DiaFluxType"%>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="de.knowwe.jspwiki.*" %>
<%@ page import="java.util.*" %>
<%@ page import="de.knowwe.core.*" %>
<%@ page import="de.knowwe.core.utils.*" %>
<%@ page import="de.knowwe.core.action.*" %>
<%@ page import="de.knowwe.diaflux.kbinfo.*" %>
<%@ page import="de.knowwe.diaflux.*" %>
<%@ page import="de.d3web.we.utils.*" %>
<%@ page import="de.knowwe.core.user.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%
	//Create wiki context; authorization check not needed
	WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
	WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );
	
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
	
	String kdomID = parameters.get("kdomID");
	Section<DiaFluxType> diafluxSection = Sections.getSection(kdomID, DiaFluxType.class);
	
	if (diafluxSection == null){
		out.println("<h3>Flowchart not found. Please try opening the editor again.</h3>");
		out.println("<script>if (window.opener) window.opener.location.reload();</script>");
		
		return;
	}
	
	// Add topic as containing section of flowchart
	parameters.put(Attributes.TOPIC, diafluxSection.getTitle());
	
	// Add web
	if(!parameters.containsKey(Attributes.WEB)) {
		parameters.put(Attributes.WEB, "default_web");
	}
	
	// Create AuthenticationManager instance
	AuthenticationManager manager = new JSPAuthenticationManager(wikiContext);
	
	// Create action context
	UserActionContext context = new ActionContext(parameters.get("action"), AbstractActionServlet.getActionFollowUpPath(request), parameters, request, response, wiki.getServletContext(), manager);
	
	String topic = context.getTitle();
	String web = context.getWeb();
	Article article = Environment.getInstance().getArticle(web, topic);
	if (article == null){
		// happens if article is no longer available
		out.println("<h3>Article not found: '" + topic + "'.</h3>");
		return;
	}
	
	WikiConnector connector = Environment.getInstance().getWikiConnector();
	boolean canEditPage = connector.userCanEditArticle(topic, context.getRequest());
	
	if (!canEditPage){
		out.println("<h3>Do not have the permission to edit article: '" + topic + "'.</h3>");
		return;
	}
	
	//TODO how to handle leftover pagelocks?
// 	boolean locked = connector.isArticleLocked(topic);
// 	if (locked) {
// 		out.println("<h3>The article is currently being edited.</h3>");
// 		return;
// 	}
	
	
	String title = DiaFluxType.getFlowchartName(diafluxSection);
	JSPHelper jspHelper = new JSPHelper(context);
%>

<html>
<head>
<link rel="shortcut icon" type="image/x-icon" href="/KnowWE/images/favicon.ico" />
<link rel="icon" type="image/x-icon" href="/KnowWE/images/favicon.ico" />
<script>
	var topic = "<%= topic %>";
	var nodeID = "<%= kdomID %>";
</script>

<title>Edit Flowchart: <%= title %></title>
	
	<script src="cc/scriptaculous-js/lib/prototype.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/builder.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/effects.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/dragdrop.js" type="text/javascript"></script>
	
	<script src="cc/kbinfo/kbinfo.js" type="text/javascript"></script>
	<script src="cc/kbinfo/events.js" type="text/javascript"></script>
	<script src="cc/kbinfo/extensions.js" type="text/javascript"></script>
	<script src="cc/kbinfo/dropdownlist.js" type="text/javascript"></script>
	<script src="cc/kbinfo/objectselect.js" type="text/javascript"></script>
	<script src="cc/kbinfo/objecttree.js" type="text/javascript"></script>
	
	<script src="cc/flow/flowchart.js" type="text/javascript"></script>
	<script src="cc/flow/floweditor.js" type="text/javascript"></script>
	<script src="cc/flow/action.js" type="text/javascript"></script>
	<script src="cc/flow/guard.js" type="text/javascript"></script>
	<script src="cc/flow/guardeditor.js" type="text/javascript"></script>
	<script src="cc/flow/node.js" type="text/javascript"></script>
	<script src="cc/flow/rule.js" type="text/javascript"></script>
	<script src="cc/flow/ruleeditor.js" type="text/javascript"></script>
	<script src="cc/flow/nodeeditor.js" type="text/javascript"></script>
	<script src="cc/flow/router.js" type="text/javascript"></script>
	<script src="cc/flow/contextmenu.js" type="text/javascript"></script>
	
<%
	Extension[] extensions = JPFPluginManager.getInstance().getExtensions(DiaFluxEditorEnhancement.PLUGIN_ID, DiaFluxEditorEnhancement.EXTENSION_POINT_ID);
	for (Extension extension : extensions){
		DiaFluxEditorEnhancement enh = (DiaFluxEditorEnhancement) extension.getNewInstance();
		
		for (String script : enh.getScripts()) {
			out.println("<script src='" + script + "' type='text/javascript'></script>");
		}

		for (String style : enh.getStylesheets()) {
			out.println("<link rel='stylesheet' type='text/css' href='" + style + "'></link>");
		}
		
	}

%>	
	
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/dropdownlist.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objectselect.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objecttree.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/floweditor.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/flowchart.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/nodeeditor.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/node.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/rule.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/guard.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/contextmenu.css"></link>
</head>
<body onload="new FlowEditor(<%= jspHelper.getArticleIDsAsArray(kdomID) %>).showEditor();">

<%-- default kbinfo objects delivered from server --%>
<data id="articleKBInfo" style="display:none;">
<%= jspHelper.getArticleInfoObjectsAsXML(kdomID) %>
</data>
<%-- default kbinfo objects delivered from server --%>
<data id="referredKBInfo" style="display:none;">
<%= jspHelper.getReferredInfoObjectsAsXML(kdomID) %>
</data>
<data id="ajaxKBInfo" style="display:none;">
	<kbinfo></kbinfo>
</data>
<data id="flowchartSource" style="display:none;">
<%= jspHelper.loadFlowchart(request.getParameter("kdomID")) %>
</data>
<div id="toolbar"> 
	<ul class="toolbar">
		<li class="icon" id="saveClose" title="Save flowchart & Close editor" style="background-image:url(cc/image/toolbar/saveclose_flowchart_32.png);"></li><!--
	  --><!--li class="icon" id="save" title="Save flowchart" style="background-image:url(cc/image/toolbar/save_flowchart_32.png);"></li--><!--  
	  --><li class="icon" id="refresh" title="Revert changes" style="background-image:url(cc/image/toolbar/reload_32.png);"></li><!--  
	  --><li class="icon" id="close" title="Close editor" style="background-image:url(cc/image/toolbar/cancel_32.png);"></li><!--  
	  --><li class="icon" id="delete" title="Delete flowchart" style="background-image:url(cc/image/toolbar/delete_flowchart_32.png);"></li>
	</ul>
	<div class="propertyArea">
		<div>
			<span class="propertyTitle">Name:</span><input type=text id="properties.editName" class="propertyText long"></input>
			<span class="propertyTitle">Autostart:</span><input type="checkbox" id="properties.autostart" title="Defines if all startnodes of this flowchart are activated on session start."></input>
			<input type=hidden id="properties.editWidth" class="propertyText short"></input>
			<input type=hidden id="properties.editHeight" class="propertyText short"></input>
		</div>	
	</div>
	<ul class="toolbar">
		<li class="icon" id="x_larger" title="Increase width" style="background-image:url(cc/image/toolbar/x_larger.png);"></li><!--
	  --><li class="icon" id="x_smaller" title="Decrease width" style="background-image:url(cc/image/toolbar/x_smaller.png);"></li><!--  
	  --><li class="icon" id="y_larger" title="Increase hight" style="background-image:url(cc/image/toolbar/y_larger.png);"></li><!--  
	  --><li class="icon" id="y_smaller" title="Decrease hight" style="background-image:url(cc/image/toolbar/y_smaller.png);"></li>
	</ul>	
	<ul class="toolbar">
		<li class="icon NodePrototype" id="decision_prototype" title="Action node" style="background-image:url(cc/image/node_decorators/decision_32.png);"></li><!--
	  --><li class="icon NodePrototype" id="start_prototype" title="Start node" style="background-image:url(cc/image/node_decorators/start_32.png);"></li><!--
	  --><li class="icon NodePrototype" id="exit_prototype" title="Exit node" style="background-image:url(cc/image/node_decorators/exit_32.png);"></li><!--
	  --><li class="icon NodePrototype" id="comment_prototype" title="Comment node" style="background-image:url(cc/image/node_decorators/comment_32.png);"></li><!--
	  --><li class="icon NodePrototype" id="snapshot_prototype" title="Snapshot node" style="background-image:url(cc/image/node_decorators/snapshot_32.png);"></li><!--
	  -->
	</ul>
</div>
<div id="changenotebar">
	<table>
    	<tbody>
    		<tr>
   				<td><label for="changenote">Change Note</label></td>
    			<td><input type="text" name="changenote" id="changenote" size="120" value=""></td>
    		</tr>
  		</tbody>
 	</table>
</div>
<div id="leftMenu" class="leftMenu">
	<div id="objectTree"></div>
</div>
<div id="contents"></div>
</body>
</html>