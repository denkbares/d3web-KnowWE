<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN">
<%@page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.apache.wiki.WikiContext" %>
<%@ page import="org.apache.wiki.WikiEngine" %>
<%@ page import="de.d3web.plugin.Extension" %>
<%@ page import="de.d3web.plugin.JPFPluginManager" %>
<%@ page import="de.knowwe.core.Attributes" %>
<%@ page import="de.knowwe.core.Environment" %>
<%@ page import="de.knowwe.core.action.AbstractActionServlet" %>
<%@ page import="de.knowwe.core.action.ActionContext" %>
<%@ page import="de.knowwe.core.action.UserActionContext" %>
<%@ page import="de.knowwe.core.kdom.Article" %>
<%@ page import="de.knowwe.core.kdom.parsing.Section" %>
<%@ page import="de.knowwe.core.kdom.parsing.Sections" %>
<%@ page import="de.knowwe.core.user.AuthenticationManager" %>
<%@ page import="de.knowwe.core.user.UserContextUtil" %>
<%@ page import="de.knowwe.core.wikiConnector.WikiConnector" %>
<%@ page import="de.knowwe.diaflux.DiaFluxEditorEnhancement" %>
<%@ page import="de.knowwe.diaflux.kbinfo.JSPHelper" %>
<%@ page import="de.knowwe.diaflux.type.DiaFluxType" %>
<%@ page import="de.knowwe.jspwiki.JSPAuthenticationManager" %>
<%@ page import="de.knowwe.jspwiki.JSPWikiConnector" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
	//Create wiki context; authorization check not needed
	WikiEngine wiki = WikiEngine.getInstance(getServletConfig());
	WikiContext wikiContext = wiki.createContext(request, WikiContext.VIEW);

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
	Section<DiaFluxType> diafluxSection = Sections.get(kdomID, DiaFluxType.class);

	if (diafluxSection == null) {
		out.println("<h3>Flowchart not found. Please try opening the editor again.</h3>");
		out.println("<script>if (window.opener) window.opener.location.reload();</script>");

		return;
	}

	// Add topic as containing section of flowchart
	parameters.put(Attributes.TOPIC, diafluxSection.getTitle());

	// Add web
	if (!parameters.containsKey(Attributes.WEB)) {
		parameters.put(Attributes.WEB, "default_web");
	}

	// Create AuthenticationManager instance
	AuthenticationManager manager = new JSPAuthenticationManager(wikiContext);

	// Create action context
	UserActionContext context = new ActionContext(parameters.get("action"), AbstractActionServlet.getActionFollowUpPath(request), parameters, request, response, wiki
			.getServletContext(), manager);

	String topic = context.getTitle();
	String web = context.getWeb();
	Article article = Environment.getInstance().getArticle(web, topic);
	if (article == null) {
		// happens if article is no longer available
		out.println("<h3>Article not found: '" + topic + "'.</h3>");
		return;
	}

	WikiConnector connector = Environment.getInstance().getWikiConnector();
	boolean canEditPage = connector.userCanEditArticle(topic, context.getRequest());

	if (!canEditPage) {
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
	<link rel="icon" type="image/x-icon" href="/KnowWE/images/favicon.ico">
	<script>
		var topic = "<%= topic %>";
		var nodeID = "<%= kdomID %>";
	</script>

	<title>Edit Flowchart: <%= title %>
	</title>

	<script src="cc/scriptaculous-js/lib/prototype.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/builder.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/effects.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/dragdrop.js" type="text/javascript"></script>

	<script type='text/javascript' src='KnowWEExtension/scripts/jquery-2.1.0.min.js'></script>
	<script type='text/javascript'
			src='KnowWEExtension/scripts/jquery-ui.min.js'></script>
	<script type='text/javascript' src='KnowWEExtension/scripts/jquery-treeTable.js'></script>
	<script type='text/javascript' src='KnowWEExtension/scripts/jquery-tooltipster.js'></script>
	<script type='text/javascript'
			src='KnowWEExtension/scripts/jquery-plugin-collection.js'></script>
	<script type='text/javascript' src='KnowWEExtension/scripts/jquery-compatibility.js'></script>

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
	<script src="cc/flow/edittools.js" type="text/javascript"></script>

	<script type='text/javascript' src='KnowWEExtension/scripts/TextArea.js'></script>
	<script type='text/javascript' src='KnowWEExtension/scripts/KnowWE-EditCommons.js'></script>
	<script type='text/javascript'
			src='KnowWEExtension/scripts/KnowWE-Plugin-AutoComplete.js'></script>

	<%
		Extension[] extensions = JPFPluginManager.getInstance()
				.getExtensions(DiaFluxEditorEnhancement.PLUGIN_ID, DiaFluxEditorEnhancement.EXTENSION_POINT_ID);
		for (Extension extension : extensions) {
			DiaFluxEditorEnhancement enh = (DiaFluxEditorEnhancement) extension.getNewInstance();

			for (String script : enh.getScripts()) {
				out.println("<script src='" + script + "' type='text/javascript'></script>");
			}

			for (String style : enh.getStylesheets()) {
				out.println("<link rel='stylesheet' type='text/css' href='" + style + "'></link>");
			}
		}
	%>

	<link rel="stylesheet" type="text/css" href="KnowWEExtension/css/jquery-ui.min.css"/>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/dropdownlist.css"/>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objectselect.css"/>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objecttree.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/floweditor.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/flowchart.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/nodeeditor.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/node.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/rule.css">
	<link rel="stylesheet" type="text/css" href="cc/flow/guard.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/contextmenu.css"/>
	<link rel="stylesheet" type="text/css" href="cc/flow/edittools.css"/>
	<link rel='stylesheet' type='text/css' href='KnowWEExtension/css/jquery-treeTable.css'/>
	<link rel='stylesheet' type='text/css' href='KnowWEExtension/css/KnowWE-Plugin-AutoComplete.css'/>
</head>
<body onload="new FlowEditor(<%= jspHelper.getArticleIDsAsArray(Sections.get(kdomID)).replace("\"", "&quot;") %>).showEditor();">

<%-- default kbinfo objects delivered from server --%>
<data id="articleKBInfo" style="display:none;">
	<%= jspHelper.getArticleInfoObjectsAsXML(Sections.get(kdomID)) %>
</data>
<%-- default kbinfo objects delivered from server --%>
<data id="referredKBInfo" style="display:none;">
	<%= JSPHelper.getReferredInfoObjectsAsXML(Sections.get(kdomID)) %>
</data>
<data id="ajaxKBInfo" style="display:none;">
	<kbinfo></kbinfo>
</data>
<data id="flowchartSource" style="display:none;">
	<%= jspHelper.loadFlowchart(request.getParameter("kdomID")) %>
</data>
<div id="toolbar">
	<ul class="toolbar">
		<li class="icon" id="saveClose" title="Save and Close Editor"
			style="position:relative; background-image:url(cc/image/toolbar/saveclose.png);width:80px"><span
				style="position:absolute;left: 45px;top:12px;">Save</span>
			<!--li class="icon" id="save" title="Save flowchart" style="background-image:url(cc/image/toolbar/save_flowchart_32.png);"></li-->
		<li class="icon" id="cancel" title="Cancel"
			style="position:relative; background-image:url(cc/image/toolbar/cancel.png);;width:80px"><span
				style="position:absolute;left:38px;top:12px;">Cancel</span>
	</ul>
	<div class="propertyArea" id="editNameArea">
		<span class="propertyTitle">Name </span><input type=text id="properties.editName"
													   class="propertyText long"></input>
		<input type="checkbox" class="pointer" id="properties.autostart"
			   title="Defines if all startnodes of this flowchart are activated on session start."></input>
		<label class="propertyTitle pointer" for="properties.autostart">Autostart</label>
	</div>
	<ul class="toolbar">
		<li class="icon disabled" id="undo" title="Undo">
		<li class="icon disabled" id="redo" title="Redo">
		<li class="icon" id="tools" title="More Tools..."
			style="background-image:url(cc/image/toolbar/tools.png);">
	</ul>
	<ul class="toolbar">
		<li class="icon NodePrototype" id="decision_prototype" title="Action node"
			style="background-image:url(cc/image/node_decorators/decision_32.png);">
		<li class="icon NodePrototype" id="start_prototype" title="Start node"
			style="background-image:url(cc/image/node_decorators/start_32.png);">
		<li class="icon NodePrototype" id="exit_prototype" title="Exit node"
			style="background-image:url(cc/image/node_decorators/exit_32.png);">
		<li class="icon NodePrototype" id="comment_prototype" title="Comment node"
			style="background-image:url(cc/image/node_decorators/comment_32.png);">
		<li class="icon NodePrototype" id="snapshot_prototype" title="Snapshot node"
			style="background-image:url(cc/image/node_decorators/snapshot_32.png);">
	</ul>
	<div class="propertyArea" id="changeNoteArea">
		<span class="propertyTitle">Change Note </span><input type=text id="changenote"
															  class="propertyText medium"></input>
	</div>
</div>
<div id="leftMenu" class="leftMenu">
	<div id="favorites"></div>
	<div id="objectTree"></div>
</div>
<div id="contents"></div>
</body>
</html>