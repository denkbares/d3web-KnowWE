var kwikiEvent;

function kwikiOnLoad() {
	//getSymptomGT('null');
	//getDiagnosisGT('null');
	//updateSolutions();
	//updateDialogs();
	//window.setTimeout("getSymptomGT('null')", 500);
	//window.setTimeout("getDiagnosisGT('null')", 500);
	//window.setTimeout("kwiki_poll()", 500);
}
function updateSolutions() {
	var url = 'KnowWE.jsp?renderer=KWiki_dpsSolutions&KWikiWeb=default_web';

	KnowWEAjax.id = 'KnowWESolutions;-;solution-panel';
	KnowWEAjax.send(url, KnowWEAjax.insert, true);
}
function doKbGenerating(jarfile) {
	try {
		var newtopicname = document.getElementById(jarfile).value;

		kBUpload_xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		kBUpload_xmlhttp.onreadystatechange = gotGeneratedKB;
		var kBUpload_poll_url = "KnowWE.jsp?renderer=GenerateKBRenderer&NewKBName=" + newtopicname + "&AttachmentName=" + jarfile;
		kBUpload_xmlhttp.open("Get", kBUpload_poll_url);
		kBUpload_xmlhttp.send(null);
	} catch (e) {
		alert(e);
	}
}

function gotGeneratedKB() {
	if ((kBUpload_xmlhttp.readyState == 4) && (kBUpload_xmlhttp.status == 200)) {
		if (document.getElementById("GeneratingInfo") != null) {
			document.getElementById("GeneratingInfo").innerHTML = kBUpload_xmlhttp.responseText;
		}
	}
}

function doTiRexToXCL(topicname) {
	try {
		var newtopicname = "" + topicname;
		tiRexToXCL_xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		tiRexToXCL_xmlhttp.onreadystatechange = gotTiRexToXCL;
		var tiRexToXCL_poll_url = "KnowWE.jsp?renderer=TirexToXCLRenderer&TopicForXCL=" + newtopicname;
		tiRexToXCL_xmlhttp.open("Get", tiRexToXCL_poll_url);
		tiRexToXCL_xmlhttp.send(null);
	} catch (e) {
		alert(e);
	}
}

function gotTiRexToXCL() {
	if ((tiRexToXCL_xmlhttp.readyState == 4) && (tiRexToXCL_xmlhttp.status == 200)) {
		if (document.getElementById("GeneratingTiRexToXCLInfo") != null) {
			document.getElementById("GeneratingTiRexToXCLInfo").innerHTML = tiRexToXCL_xmlhttp.responseText;
		}
	}
}

function showSolutions() {
	document.getElementById('KnowWESolutions').style.visibility = 'visible';
	document.getElementById('KnowWEDialogs').style.visibility = 'hidden';
	//document.getElementById('solutionsButton').style.backgroundColor = '#E2DCC8';
	//document.getElementById('dialogsButton').style.backgroundColor = '#FFFFFF';
}

function showDialogs() {
	document.getElementById('KnowWESolutions').style.visibility = 'hidden';
	document.getElementById('KnowWEDialogs').style.visibility = 'visible';
	//document.getElementById('solutionsButton').style.backgroundColor = '#FFFFFF';
	//document.getElementById('dialogsButton').style.backgroundColor = '#E2DCC8';
}

function doShowHideOptionsMenu() {
	if (document.getElementById("kwikiOptionsMenu").style.visibility == "visible") {
		doHideOptionsMenu();
	} else if (document.getElementById("kwikiOptionsMenu").style.visibility == "hidden") {
		doShowOptionsMenu();
	}
}

function kwiki_window(url) {
	// 420 x 500
	var screenWidth = (window.screen.width / 2) - (210 + 10);
	var screenHeight = (window.screen.height / 2) - (250 + 50);
	newWindow = window.open(url, url, "height=420,width=520,left=" + screenWidth + ",top=" + screenHeight + ",screenX=" + screenWidth + ", screenY=" + screenHeight + ",resizable=yes,toolbar=no,menubar=no,scrollbars=yes,location=no,status=yes,dependent=yes");
	newWindow.focus();
}

function kwiki_call(url) {
	try {
		kwiki_xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		kwiki_xmlhttp.open("GET", url);
		kwiki_xmlhttp.send(null);
		updateSolutions();
		updateDialogs();
	} catch (e) {
	}
}

function doHideOptionsMenu() {
	document.getElementById("kwikiOptionsMenu").style.visibility = "hidden";
}

function doShowOptionsMenu() {
	document.getElementById("kwikiOptionsMenu").style.visibility = "visible";
}

function updateDialogs() {
	var url = 'KnowWE.jsp?renderer=KWiki_dpsDialogs&KWikiWeb=default_web';
	KnowWEAjax.id = 'KnowWEDialogs';
	KnowWEAjax.send(url, KnowWEAjax.insert, true);
}

function kwiki_sessions(linkAction, event) {
	if (!event) {
		event = window.event;
	}
	try {
		kwikiEvent = event;
		kwiki_xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		kwiki_xmlhttp.onreadystatechange = kwiki_sessions_triggered;
		var kwiki_poll_url = "KnowWE.jsp?renderer=KWiki_sessionChooser&KWikiLinkAction=" + linkAction + "&KWikiWeb=default_web";
		kwiki_xmlhttp.open("GET", kwiki_poll_url);
		kwiki_xmlhttp.send(null);
	} catch (e) {
		alert(e);
	}
}

function kwiki_sessions_triggered() {
	if ((kwiki_xmlhttp.readyState == 4) && (kwiki_xmlhttp.status == 200)) {
		dummy = document.getElementById("kwikiSessions");
		dummy.innerHTML = kwiki_xmlhttp.responseText;
		setCorrectPositionAndShow(dummy, kwikiEvent);
		setTimeout("hideSessionChooser()", 5000);
	}
}

// Ajax events admin panel
function doReInit() {

	try {
		xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		xmlhttp.onreadystatechange = reInitTrigger;
		var kwiki_poll_url = "KnowWE.jsp?renderer=KWiki_ReInitWebTermsRenderer&KWikiWeb=default_web";
		xmlhttp.open("GET", kwiki_poll_url);
		xmlhttp.send(null);
	} catch (e) {
		alert(e);
	}
}
function reInitTrigger() {
	insertAjaxResponse("reInit");
}
function doParseWeb() {
	//Offline parse-Aktion - DPSEnvironment/Terminologien nicht aktualisiert
	try {
		xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		xmlhttp.onreadystatechange = parseWebTrigger;
		var kwiki_poll_url = "KnowWE.jsp?renderer=ParseWebOffline&KWikiWeb=default_web";
		xmlhttp.open("GET", kwiki_poll_url);
		xmlhttp.send(null);
	} catch (e) {
		alert(e);
	}
}

function parseWebTrigger() {
	insertAjaxResponse("parseWeb");
}
function doSumAll() {

	try {
		xmlhttp = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		xmlhttp.onreadystatechange = sumAllTrigger;
		var kwiki_poll_url = "KnowWE.jsp?renderer=KWikiSummarizer&KWikiWeb=default_web";
		xmlhttp.open("GET", kwiki_poll_url);
		xmlhttp.send(null);
	} catch (e) {
		alert(e);
	}
}
function sumAllTrigger() {
	insertAjaxResponse("sumAll");
}
/*
 Clears a certain area of the HTML document per id.
 Is used in the admin panel to hide the generated information for:
 - parseWeb | sumAll | reInit
 */
function clearInnerHTML(id) {
	if (document.getElementById(id) != null) {
		document.getElementById(id).innerHTML = "";
	}
}
/* update response text sollte auch zusammenzufassen gehen, ...*/
function insertAjaxResponse(id) {
	if ((xmlhttp.readyState == 4) && (xmlhttp.status == 200)) {
		if (document.getElementById(id) != null) {
			document.getElementById(id).innerHTML = xmlhttp.responseText;
		}
		document.getElementById(id).scrollIntoView(true);
	}
}


function encodeUmlauts(text) {
	text = text.replace(/�/g, "&auml;");
	text = text.replace(/�/g, "&Auml;");
	text = text.replace(/�/g, "&ouml;");
	text = text.replace(/�/g, "&Ouml;");
	text = text.replace(/�/g, "&uuml;");
	text = text.replace(/�/g, "&Uuml;");
	text = text.replace(/�/g, "&szlig;");
	return text;
}

function hideSessionChooser() {
	document.getElementById("kwikiSessions").style.visibility = 'hidden';
}

function showPopupButtons(usagePrefix, event) {
	if (!event) {
		event = window.event;
	}
	hideAllPopupDivs();
	var dummy = document.getElementById(usagePrefix + 'Popup');
	setCorrectPositionAndShow(dummy, event);
	planToHide(dummy, event);
}

function planToHide(node, event) {
	replanToHide(node, event);
	node.kwikiTimer = window.setTimeout("hideNow('" + node.id + "')", 2000);
}

function hideNow(id, event) {
	node = document.getElementById(id);
	node.style.visibility = 'hidden';
}

function replanToHide(node, event) {
	if (node.kwikiTimer != null) {
		window.clearTimeout(node.kwikiTimer);
	}
}

function hideAllPopupDivs() {
	for (i = 0; i < document.getElementsByTagName("div").length; i++) {
		div = document.getElementsByTagName("div")[i];
		if (div.getAttribute("KWikiPopupLinksDiv") != null) {
			div.style.visibility = 'hidden';
		}
	}
}

function doNothing() {
}


function hidePopupButtons(usagePrefix) {
	document.getElementById(usagePrefix + 'Popup').style.visibility = 'hidden';
}


var ie4 = document.all && navigator.userAgent.indexOf("Opera") == -1;
var ns6 = document.getElementById && !document.all;
var ns4 = document.layers;
var canhide = false;

function setCorrectPositionAndShow(menuobj, e) {
	menuobj.thestyle = (ie4 || ns6) ? menuobj.style : menuobj;
	eventX = e.layerX;
	eventY = e.layerY;
	menuobj.thestyle.left = (eventX - menuobj.contentwidth) + "px";
	menuobj.thestyle.top = eventY + "px";
	menuobj.thestyle.visibility = "visible";
}

function sendRenameRequest() {
	try {
		kwiki_xmlhttp2 = window.XMLHttpRequest ? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		kwiki_xmlhttp2.onreadystatechange = insertRenamingMask;

		var renameText = document.getElementById("renameInputField").value;
		var replacementText = document.getElementById("replaceInputField").value;
		var renameContextPrevious = document.getElementById("renamePreviousInputContext").value;
		var renameContextAfter = document.getElementById("renameAfterInputContext").value;
		var caseSensitive = document.getElementById("search-sensitive").checked;

		var kwiki_poll_url = "KnowWE.jsp";
		var params = 'TargetNamespace=' + encodeURIComponent(encodeUmlauts(renameText))
			+ '&action=RenamingRenderer&KWikiFocusedTerm=' + replacementText
			+ '&ContextPrevious=' + encodeURIComponent(encodeUmlauts(renameContextPrevious))
			+ '&ContextAfter=' + encodeURIComponent(encodeUmlauts(renameContextAfter))
			+ '&CaseSensitive=' + caseSensitive;

		kwiki_xmlhttp2.open("POST", kwiki_poll_url, false);
		kwiki_xmlhttp2.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
		kwiki_xmlhttp2.setRequestHeader("Connection", "close");
		kwiki_xmlhttp2.send(params);
		//kwiki_xmlhttp2.send(encodeURI('KWikitext='+kopictext.substring(0,20)));

	} catch (e) {
		alert(e);
	}
}

// gets the additional text, displayed in the match column of the renaming tool. 
function getAdditionalMatchText(atmUrl) {
	try {
		kwiki_xmlhttp2 = window.XMLHttpRequest ? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");

		var renameText = document.getElementById("renameInputField").value;
		var replacementText = document.getElementById("replaceInputField").value;
		var renameContextPrevious = document.getElementById("renamePreviousInputContext").value;
		var renameContextAfter = document.getElementById("renameAfterInputContext").value;
		var caseSensitive = document.getElementById("search-sensitive").checked;

		var kwiki_poll_url = "KnowWE.jsp";
		var params = 'TargetNamespace=' + encodeURIComponent(encodeUmlauts(renameText))
			+ '&action=RenamingRenderer&KWikiFocusedTerm=' + replacementText
			+ '&ContextPrevious=' + encodeURIComponent(encodeUmlauts(renameContextPrevious))
			+ '&ContextAfter=' + encodeURIComponent(encodeUmlauts(renameContextAfter))
			+ '&CaseSensitive=' + caseSensitive
			+ '&ATMUrl=' + atmUrl;

		kwiki_xmlhttp2.open("POST", kwiki_poll_url, false);
		kwiki_xmlhttp2.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
		kwiki_xmlhttp2.setRequestHeader("Connection", "close");
		kwiki_xmlhttp2.send(params);

		kwiki_xmlhttp2.onreadystatechange = insertAdditionalMatchText(atmUrl);
	} catch (e) {
		alert(e);
	}
}

//inserts an additional text into a certain preview column. 
function insertAdditionalMatchText(atmUrl) {

	var id = atmUrl.split("#")[2];
	var direction = atmUrl.split("#")[4];
	var text = "";

	id = direction + id;

	if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
		if (document.getElementById(id) != null) {
			text = kwiki_xmlhttp2.responseText;
			document.getElementById(id).innerHTML = text;
		}
	}
	document.getElementById("rename-result").scrollIntoView(true);
}

function insertRenamingMask() {
	if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
		if (document.getElementById("rename-result") != null) {
			//alert("hab was bekommen!");
			var text = kwiki_xmlhttp2.responseText;
			if (text.length == 0) {
				text = "no response for request";
			}

			document.getElementById("rename-result").innerHTML = text;
		}
		document.getElementById("rename-result").scrollIntoView(true);
		addLoadEvent(init_sortable(myColumns, 'sortable1'));
	}
}


function replaceAll() {
	try {
		kwiki_xmlhttp2 = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		kwiki_xmlhttp2.onreadystatechange = showReplaceReport;
		var renameText = document.getElementById("renameInputField").value;
		var replacementText = document.getElementById("replaceInputField").value;
		var codedReplacements = '';
		var inputs = document.getElementsByTagName("input");
		for (var i = 0; i < inputs.length; i++) {
			var inputID = inputs[i].id;
			if (inputID.substring(0, 11) == 'replaceBox_') {
				if (inputs[i].checked) {
					var code = inputID.substring(11);
					codedReplacements += "__" + code;
				}
			}
		}
		var kwiki_poll_url = "KnowWE.jsp";
		var params = 'TargetNamespace=' + encodeURIComponent(encodeUmlauts(renameText)) + '&action=GlobalReplaceAction&KWikitext=' + codedReplacements + '&KWikiFocusedTerm=' + replacementText + '&page=blubb';
		kwiki_xmlhttp2.open("POST", kwiki_poll_url, false);
		kwiki_xmlhttp2.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
		kwiki_xmlhttp2.setRequestHeader("Connection", "close");
		kwiki_xmlhttp2.send(params);
		//kwiki_xmlhttp2.send(encodeURI('KWikitext='+kopictext.substring(0,20)));

	} catch (e) {
		alert(e);
	}
}

function showReplaceReport() {
	if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
		if (document.getElementById("rename-result") != null) {
			//alert("hab was bekommen!");
			var text = kwiki_xmlhttp2.responseText;
			if (text.length == 0) {
				text = "no response for request";
			}

			document.location.reload();
			document.getElementById("rename-result").innerHTML = text;
		}
		document.getElementById("rename-result").scrollIntoView(true);

		var els = document.getElementById("rename-panel").getElementsByTagName("input");
		for (var i in els) {
			if (els[i].type == "text")
				els[i].value = "";
		}

	}
}

function kwiki_pollSyntax(article) {
	try {
		kwiki_xmlhttp2 = window.XMLHttpRequest
			? new XMLHttpRequest()
			: new ActiveXObject("Microsoft.XMLHTTP");
		kwiki_xmlhttp2.onreadystatechange = kwiki_triggeredSyntax;
		var semi = new RegExp("\;", "g");
// var kopictext = "<topic>Swimming</topic>"+(document.main.text.value.replace(semi,"\;"));
		var kopictext = document.getElementById("editorarea").value;
		var kwiki_poll_url = "KnowWE.jsp";
		var params = 'KWikitext=' + encodeURIComponent(encodeUmlauts(kopictext)) + '&KWiki_Topic=' + article + '&renderer=KWikiParseRenderer' + '&action=KWiki_parseTextToKnowledgebase';
		kwiki_xmlhttp2.open("POST", kwiki_poll_url, false);
		kwiki_xmlhttp2.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
		kwiki_xmlhttp2.setRequestHeader("Connection", "close");
		kwiki_xmlhttp2.send(params);
		//kwiki_xmlhttp2.send(encodeURI('KWikitext='+kopictext.substring(0,20)));

	} catch (e) {
		alert(e);
	}
}

function kwiki_triggeredSyntax() {
	if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
		if (document.getElementById("kwikiSyntaxReportArea") != null) {
			//alert("hab was bekommen!");
			var text = kwiki_xmlhttp2.responseText;
			if (text.length == 0) {
				text = "no response for request";
			}

			document.getElementById("kwikiSyntaxReportArea").innerHTML = text;
		}
		document.getElementById("kwikiSyntaxReportArea").scrollIntoView(true);
	}
}

var autoCompleteEvent;
function handleKeyDown(event) {
	var myDiv = document.getElementById("autoCompleteMenu");
	myDiv.style.visibility = "hidden";
	if (event.ctrlKey) {
		if (event.keyCode == 32) {
			autoCompleteEvent = event;
//textArea = document.getElementById("editorarea");
			textArea = document.getElementsByTagName("textarea")[0];
			var endPos = textArea.selectionEnd;

			var startPos = endPos - 20;
			if (endPos - 20 < 0) {
				startPos = 0;
			}
			var data = textArea.value.substring(startPos, endPos);
			var encodedData = encodeURIComponent(data);
			var request = "KnowWE.jsp?renderer=KWiki_codeCompletion&KWikiWeb=default_web&CompletionText=" + encodedData;
			codeCompletionRequest = window.XMLHttpRequest
				? new XMLHttpRequest()
				: new ActiveXObject("Microsoft.XMLHTTP");
			codeCompletionRequest.onreadystatechange = presentCompletionPropositions;
			codeCompletionRequest.open("GET", request);
			codeCompletionRequest.send(null);
		} else {
			autoCompleteEvent = null;
		}
	} else {
		autoCompleteEvent = null;
	}
}

function presentCompletionPropositions() {
	if (codeCompletionRequest.readyState == 4 && codeCompletionRequest.status == 200 && autoCompleteEvent) {
		var result = codeCompletionRequest.responseText;
		var myDiv = document.getElementById("autoCompleteMenu");
		myDiv.innerHTML = result;

		myDiv.style.top = "400px";
		myDiv.style.left = "200px";
		myDiv.style.visibility = "visible";
		var popup = document.getElementById("findings");
		if (popup && result.length > 0) {
			popup.focus();
		} else {
			textArea = document.getElementsByTagName("textarea")[0];
			if (textArea) {
				textArea.focus();
			}
		}
	}
}

function selectionKey(select, event) {
	if (event.keyCode == 13) {
		//Enter pressed
		var wert = select.options[select.options.selectedIndex].getAttribute('replacestring');
		insertAtCursor(textArea = document.getElementsByTagName("textarea")[0], wert);
		//var myDiv = document.getElementById("autoCompleteMenu");
		//select.style.visibility = "hidden";
	}
}

function insertAtCursor(myField, myValue) {
	twikismartTextareaScrollFirefox = myField.scrollTop;
	//IE support
	if (document.selection) {
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
	}

	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;

		var endPos = myField.selectionEnd;
		myField.value = myField.value.substring(0, startPos)
			+ myValue
			+ myField.value.substring(endPos, myField.value.length);
		myField.setSelectionRange(endPos + myValue.length, endPos + myValue.length);
		//twikismartInitializeAllAttributes();
		myField.scrollTop = twikismartTextareaScrollFirefox;

	} else {
		myField.value += myValue;
	}
}

/* ############################################################### */
/* ############################################################### */
/*
 * Transforms the headings of the KnowWE plugin panels to be collabsible.
 */
function addPanelToggle() {
	var panels = new Array("admin-panel", "rename-panel", "knoffice-panel",
		"knowledge-panel", "questionsheet-panel", "KnowledgeBasesGenerator");

	for (var i = 0; i < panels.length; i++) {
		if (jq$('#' + panels[i])[0]) {
			var current = $tag(panels[i], 'h3');
			current.innerHTML = "- " + current.innerHTML;
			WikiEvent.add(current, 'click', togglePanel);
		}
	}
}
/*
 * Collapses or expands the KnowWE plugin panels.
 */
function togglePanel(event) {
	var evt = event || window.event;
	var el = evt.target || evt.srcElement;

	var state = el.nextSibling.style['display'];
	var elements = el.parentNode.childNodes;
	var visible = (state == 'block') ? 'none' : ((state == '') ? 'none' : 'block');

	for (var i = 0; i < elements.length; i++) {
		if (elements[i].tagName.toLowerCase() == "h3") {
			var sign = (visible == 'block') ? '- ' : '+ ';
			elements[i].innerHTML = sign + elements[i].innerHTML.substring(2);
		} else {
			elements[i].style['display'] = visible;
		}
	}
}
/*
 * Shows a panel in certain plugin with additional options.
 */
function showExtendedPanel(event) {
	var evt = event || window.event;
	var el = evt.target || evt.srcElement;

	var style = el.nextSibling.style;
	el.removeAttribute('class');

	if (style['display'] == 'inline') {
		style['display'] = 'none';
		el.setAttribute('class', 'pointer extend-panel-down');
	} else {
		style['display'] = 'inline';
		el.setAttribute('class', 'pointer extend-panel-up');
	}
}
/**
 * Returns the first elements with given tag within a certain id element.
 * Usage: $tag(dialog-panel, table)
 */
function $tag(id, tag) {
	if (!id || !tag) return;
	if (typeof id != 'string' || typeof tag != 'string') return;

	return jq$('#' + id)[0].getElementsByTagName(tag)[0];
}
/*
 * Checks if the current page allows certain onload events.
 * If the page list contains the current page, the onload event is triggered,
 * otherwise none. Prevents errors due incorrect context for onload events.
 */
function loadCheck(pages) {
	var path = window.location.pathname.split('/');
	var page = path[path.length - 1];

	for (var i = 0; i < pages.length; i++) {
		if (page === pages[i])
			return true;
	}
	return false;
}
function formHint(event) {
	if (!$('knoffice-panel')) return;

	var els = jq$('#knoffice-extend-panel')[0].getElementsByTagName("input");

	for (var i = 0; i < els.length; i++) {
		if (els[i].nextSibling.tagName.toLowerCase() == 'span') {
			WikiEvent.add(els[i], 'focus', function(event) {
				var evt = event || window.event;
				var el = evt.target || evt.srcElement;
				el.nextSibling.style.display = "inline";
			});
			WikiEvent.add(els[i], 'blur', function(event) {
				var evt = event || window.event;
				var el = evt.target || evt.srcElement;
				el.nextSibling.style.display = "none";
			});
		}
	}
}
/**
 * Returns the value of a URL parameter. Which paramater is specified through
 * name.
 */
function gup(name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(window.location.href);
	if (results === null)
		return "";
	else
		return results[1];
}
/* ############################################################### */
/* KnowWEAjax - common ajax ability.                               */
/* ############################################################### */
var http;
var KnowWEAjax = {
	/* stores current request */
	send : function(url, fn, post) {
		http = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
		if (!http) return;

		var method = (post) ? 'POST' : 'GET';

		http.open(method, url, true);
		http.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		http.setRequestHeader('Content-length', url.length);
		http.setRequestHeader('Connection', 'close');
		http.onreadystatechange = fn;
		http.send(method);
	},
	/* inserts the repsonse. mainly text or html result that changed*/
	insert : function() {
		if ((http.readyState == 4) && (http.status == 200)) {
			if (!KnowWEAjax.id) return;
			if (KnowWEAjax.id.contains(';-;')) {
				var token = KnowWEAjax.id.split(';-;');
				for (var i = 0; i < token.length; i++) {
					if (!$jq('#' + token[i])[0]) return;
					jq$('#' + token[i])[0].innerHTML = http.responseText;
				}
			} else {
				jq$('#' + KnowWEAjax.id)[0].innerHTML = http.responseText;
			}
		}
	},
	/* inserts an extendend response. user can specify additional information to execute tasks.*/
	insertExtended : function() {
		if ((http.readyState == 4) && (http.status == 200)) {
			if (!KnowWEAjax.ext || !KnowWEAjax.ext.id) return;

			if (!KnowWEAjax.ext.div || KnowWEAjax.ext.div === '') {
				jq$('#' + KnowWEAjax.ext.id)[0].innerHTML = http.responseText;
			} else {
				var div = document.createElement('div');
				div.setAttribute('id', KnowWEAjax.ext.div);
				div.innerHTML = http.responseText;
				jq$('#' + KnowWEAjax.ext.id)[0].insertBefore(div, jq$('#' + KnowWEAjax.ext.id)[0].getChildren()[0]);
				;
			}
			if (!KnowWEAjax.ext.fn) return;
			KnowWEAjax.ext.fn.call();
		}
	}
};
/* ############################################################### */
/* ############################################################### */
var EventCache = [];
var WikiEvent = {
	/*
	 * Cross-Browser Ereignis-Registrar v.
	 * Originally by Scott Andrew
	 */
	add : function(obj, evType, fn) {
		if (obj.addEventListener) {
			obj.addEventListener(evType, fn, false);
			EventCache.push({'el' : obj, 'type' : evType, 'fn' : fn});
			return true;
		} else if (obj.attachEvent) {
			var retVal = obj.attachEvent("on" + evType, fn);
			EventCache.push({'el' : obj, 'type' : evType, 'fn' : fn});
			return retVal;
		} else {
			return false;
		}
	},
	remove : function(obj, evType, fn) {
		if (obj.removeEventListener) {
			obj.removeEventListener(evType, fn, false);
			return true;
		} else if (obj.detachEvent) {
			var r = obj.detachEvent("on" + evType, fn);
			return r;
		} else {
			alert("Handler could not be removed");
		}
	},
	purge : function() {
		for (var i = 0; i < EventCache.length; i++) {
			alert(EventCache[i].el + "\n" + EventCache[i].type + " " + EventCache[i].fn);
		}
	}
};
/* ############################################################### */
/* ------------- Events  ----------------------------------------- */
/* ############################################################### */
function onloadActions() {
	if (jq$('#rename-show-extend')[0]) {
		WikiEvent.add($('rename-show-extend'), 'click', showExtendedPanel);
	}
	if (jq$('#knoffice-show-extend')[0]) {
		WikiEvent.add($('knoffice-show-extend'), 'click', showExtendedPanel);
	}
}
if (loadCheck(['Wiki.jsp'])) {
	WikiEvent.add(window, 'load', addPanelToggle);
	WikiEvent.add(window, 'load', onloadActions);
	WikiEvent.add(window, 'load', formHint);
	WikiEvent.add(window, 'load', updateSolutions);
	WikiEvent.add(window, 'load', updateDialogs);
}


/* ############################################################### */
/* ------------- Biolog  ----------------------------------------- */
/* ############################################################### */
WikiEvent.add(window, 'load', addLayout);
function addLayout() {

	var pos = $('header').getFirst('titlebox').getNext('applicationlogo');

	//header
	var div = new Element('div', {'id' : 'biolog-header'});
	div.inject(pos, 'after');

	var wissen = new Element('div', {'id' : 'biolog-wissen'});
	wissen.inject($('biolog-header'));

	var box = new Element('div', {'id' : 'biolog-box'});
	box.inject($('biolog-header'));

	var left = new Element('div', {'id' : 'biolog-header-left'});
	var middle = new Element('div', {'id' : 'biolog-header-middle'});
	var right = new Element('div', {'id' : 'biolog-header-right'});

	left.inject($('biolog-box'));
	right.inject($('biolog-box'));
	middle.inject($('biolog-box'));

	//clear head
	//div.getNext('titlebox').getNext('applicationlogo').setStyle('clear', 'both');
	$('biolog-header').setStyle('clear', 'both');
	//footer
	var left = new Element('div', {'id' : 'biolog-footer-left'});
	var middle = new Element('div', {'id' : 'biolog-footer-middle'});
	var right = new Element('div', {'id' : 'biolog-footer-right'});
	middle.inject($('footer'), 'top');
	right.inject($('footer'), 'top');
	left.inject($('footer'), 'top');
}