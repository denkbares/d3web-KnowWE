<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN">

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="de.d3web.we.kdom.Section"%>
<%@ page import="de.d3web.we.kdom.KnowWEArticle"%>
<%@ page import="de.d3web.we.flow.type.DiaFluxType"%>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="de.d3web.we.jspwiki.*" %>
<%@ page import="java.util.*" %>
<%@ page import="de.d3web.we.core.*" %>
<%@ page import="de.d3web.we.javaEnv.*" %>
<%@ page import="de.d3web.we.wikiConnector.*" %>
<%@ page import="de.d3web.we.action.*" %>
<%@ page import="de.d3web.we.flow.kbinfo.*" %>
<%@ page import="de.d3web.we.flow.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context; authorization check not needed
    WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );

    if (!KnowWEEnvironment.isInitialized()) {
		KnowWEEnvironment.initKnowWE(new JSPWikiKnowWEConnector(wiki));
	}
	KnowWEEnvironment env = KnowWEEnvironment.getInstance();
	KnowWEUserContextImpl userContext =  new KnowWEUserContextImpl(wikiContext.getWikiSession().getUserPrincipal().getName(), wikiContext.getHttpRequest().getParameterMap());
	KnowWEParameterMap map = new KnowWEParameterMap(userContext, request, response, wiki.getServletContext(), env);
	
	map.put(KnowWEAttributes.USER, wikiContext.getWikiSession().getUserPrincipal().getName());
	if(!map.containsKey(KnowWEAttributes.WEB)) {
		map.put(KnowWEAttributes.WEB, "default_web");
	}
	String topic = map.getTopic();
	String web = map.getWeb();
	KnowWEArticle article = env.getArticle(web, topic);
	
	if (article == null){
		//TODO happens if article is no longer available
		out.println("<h3>Article not found: '" + topic + "'.</h3>");
		return;
	}
	
	JSPHelper jspHelper = new JSPHelper(map);
	String kdomID = map.get("kdomID");
	Section diafluxSection = article.findSection(kdomID);
	String title = DiaFluxType.getFlowchartName(diafluxSection);
%>

<script>
	var topic = "<%= topic %>";
	var nodeID = "<%= kdomID %>";
</script>

<html>
<head>
<title>Edit Flowchart: <%= title %></title>
	
	<script src="cc/scriptaculous-js/lib/prototype.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/builder.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/effects.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/dragdrop.js" type="text/javascript"></script>
	<!-- script src="cc/scriptaculous-js/src/controls.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/slider.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/sound.js" type="text/javascript"></script>
	<script src="cc/scriptaculous-js/src/scriptaculous.js" type="text/javascript"></script-->
	
	<script src="cc/kbinfo/kbinfo.js" type="text/javascript"></script>
	<script src="cc/kbinfo/extensions.js" type="text/javascript"></script>
	<script src="cc/kbinfo/dropdownlist.js" type="text/javascript"></script>
	<script src="cc/kbinfo/objectselect.js" type="text/javascript"></script>
	<script src="cc/kbinfo/objecttree.js" type="text/javascript"></script>
	
	<script src="cc/flow/floweditor.js" type="text/javascript"></script>
	<script src="cc/flow/flowchart.js" type="text/javascript"></script>
	<script src="cc/flow/action.js" type="text/javascript"></script>
	<script src="cc/flow/guard.js" type="text/javascript"></script>
	<script src="cc/flow/node.js" type="text/javascript"></script>
	<script src="cc/flow/rule.js" type="text/javascript"></script>
	<script src="cc/flow/nodeeditor.js" type="text/javascript"></script>
	<script src="cc/flow/router.js" type="text/javascript"></script>
	<script src="cc/flow/rollup.js" type="text/javascript"></script>
	
	
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/dropdownlist.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objectselect.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/kbinfo/objecttree.css"></link>


	<link rel="stylesheet" type="text/css" href="cc/flow/floweditor.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/flowchart.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/nodeeditor.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/node.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/rule.css"></link>
	<link rel="stylesheet" type="text/css" href="cc/flow/guard.css"></link>
	
</head>


<body onload="KBInfo._updateCache($('ajaxKBInfo'));showEditor();">

<%-- default kbinfo objects delivered from server --%>
<xml id="articleKBInfo" style="display:none;">
<%= jspHelper.getArticleInfoObjectsAsXML() %>
</xml>

<%-- default kbinfo objects delivered from server --%>
<xml id="referredKBInfo" style="display:none;">
<%= jspHelper.getReferredInfoObjectsAsXML() %>
</xml>


<xml id="ajaxKBInfo" style="display:none;">
	<kbinfo>

	</kbinfo>
</xml>


<xml id="flowchartSource" style="display:none;">
<%= jspHelper.loadFlowchart(request.getParameter("kdomID")) %>
</xml>

<div> 
	<ul class="toolbar" tyle="float: left; width: 40%;">
		<li class="icon" id="saveClose" title="Save & Close" style="background-image:url(cc/image/toolbar/saveclose_flowchart_32.png);"></li><!--
	  --><li class="icon" id="save" title="Save" style="background-image:url(cc/image/toolbar/save_flowchart_32.png);"></li><!--  
	  --><li class="icon" id="refresh" title="Reload" style="background-image:url(cc/image/toolbar/reload_32.png);"></li><!--  
	  --><li class="icon" id="close" title="Close" style="background-image:url(cc/image/toolbar/cancel_32.png);"></li><!--  
	  --><li class="icon" id="delete" title="Delete" style="background-image:url(cc/image/toolbar/delete_flowchart_32.png);"></li>
	</ul>
	<div class="propertyArea">
		<div>
			<span class="propertyTitle">Name:</span><input type=text id="properties.editName" class="propertyText long"></input>
			<span class="propertyTitle">Autostart:</span><input type="checkbox" id="properties.autostart" title="Defines if all startnodes of this flowchart are activated on session start."></input>
			<span class=propertyTitle>Width:</span><input type=text id="properties.editWidth" class="propertyText short"></input>
			<span class=propertyTitle>Height:</span><input type=text id="properties.editHeight" class="propertyText short"></input>
		</div>	
	</div>	
	<ul class="toolbar" tyle="float: left; width: 60%;">
		<li class="icon NodePrototype" id="decision_prototype" title="Action node" style="background-image:url(cc/image/node_decorators/decision.png);"></li><!--
	  --><li class="icon NodePrototype" id="start_prototype" title="Start node" style="background-image:url(cc/image/node_decorators/start.png);"></li><!--
	  --><li class="icon NodePrototype" id="exit_prototype" title="Exit node" style="background-image:url(cc/image/node_decorators/exit.png);"></li><!--
	  --><li class="icon NodePrototype" id="comment_prototype" title="Comment node" style="background-image:url(cc/image/node_decorators/comment.png);"></li><!--
	  --><li class="icon NodePrototype" id="snapshot_prototype" title="Snapshot node" style="background-image:url(cc/image/node_decorators/snapshot.png);"></li><!--
	  -->
	</ul>
</div>

<div id="leftMenu" class="leftMenu">
	<div id="objectTree"></div>
</div>

<div id="contents" style="position:relative"></div>

<script>
	// kbinfo initialization
	KBInfo._updateCache($('articleKBInfo'));
	KBInfo._updateCache($('referredKBInfo'));
	

	// initialize wiki tree tool
	new ObjectTree('objectTree', null, 
		<%= jspHelper.getArticleIDsAsArray() %>
	);

	var theFlowchart = null;

	// create example flowchart by delivered xml
	function showEditor() {
		theFlowchart = Flowchart.createFromXML('contents', $('flowchartSource'));
		theFlowchart.setVisible(true);
		$('properties.editName').value = theFlowchart.name || theFlowchart.id;
		$('properties.editWidth').value = theFlowchart.width;
		$('properties.editHeight').value = theFlowchart.height;
		$('properties.autostart').checked = theFlowchart.autostart;
		$('properties.editName').onchange = updateProperties;
		$('properties.editWidth').onchange = updateProperties;
		$('properties.editHeight').onchange = updateProperties;
		$('properties.autostart').onchange = updateProperties;
		
		$('saveClose').observe('click', function(){saveFlowchart(true);});
		$('save').observe('click', function(){saveFlowchart(false);});
		$('refresh').observe('click', refresh);
		
		$('close').observe('click', closeEditor);
		$('delete').observe('click', deleteFlowchart);
		
	}
	
	function updateProperties() {
		theFlowchart.name = $('properties.editName').value;
		theFlowchart.setSize($('properties.editWidth').value, $('properties.editHeight').value);
		theFlowchart.autostart = $('properties.autostart').checked;
	}
	
	function refresh(){
		window.location.reload();
	}
	
	function closeEditor(){
		window.close();
	}
	
	function deleteFlowchart() {
		var result = confirm('Do you really want to delete the flowchart?');
		if (result) {
			_saveFlowchartText('', true);
		}
	}
	
	function saveFlowchart(closeOnSuccess) {
		theFlowchart.setSelection(null, false, false);
		var xml = theFlowchart.toXML(true); // include preview for us
		_saveFlowchartText(xml, closeOnSuccess);
	}
	
	function _saveFlowchartText(xml, closeOnSuccess) {
		var url = "KnowWE.jsp";
		new Ajax.Request(url, {
			method: 'post',
			parameters: {
				action: 'SaveFlowchartAction',
				KWiki_Topic: topic,			// article
				TargetNamespace: nodeID,	// KDOM nodeID
				KWikitext: xml				// content
			},
			onSuccess: function(transport) {
				if (window.opener) window.opener.location.reload();
				if (closeOnSuccess) window.close();
			},
			onFailure: function() {
				CCMessage.warn(
					'AJAX Error', 
					'Changes could not be saved.');
			},
			onException: function(transport, exception) {
				CCMessage.warn(
					'AJAX Error, Saving most likely failed.',
					exception
					);
			}
		}); 		

		
	}

	var dragOptions = { ghosting: true, revert: true, reverteffect: ObjectTree.revertEffect};

	new Draggable('decision_prototype', dragOptions);
	new Draggable('start_prototype', dragOptions);
	new Draggable('exit_prototype', dragOptions);
	new Draggable('comment_prototype', dragOptions);
	new Draggable('snapshot_prototype', dragOptions);
	
	$('decision_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {action: { markup: 'KnOffice', expression: ''}}); };
	$('start_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {start: 'Start'}); };
	$('exit_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {exit: 'Exit'}); };
	$('comment_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {comment: 'Comment'}); };
	$('snapshot_prototype').createNode = function(flowchart, left, top) { createActionNode(flowchart, left, top, {snapshot: 'Snapshot'}); };
	
	function createActionNode(flowchart, left, top, nodeModel) {
		nodeModel.position = {left: left, top: top};
		var node = new Node(flowchart, nodeModel);
		node.select();
		node.edit();
	};
  
</script>

<wiki:Include page="<%=\"\"%>" />

</body>
</html>