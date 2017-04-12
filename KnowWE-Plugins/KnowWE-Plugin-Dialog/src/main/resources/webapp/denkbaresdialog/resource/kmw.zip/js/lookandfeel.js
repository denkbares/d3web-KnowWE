
var LookAndFeel = { imageIDCounter: 0 };

LookAndFeel.renderImage = function (src, maxWidth, maxHeight) {
	var imageID = "_imageID"+(LookAndFeel.imageIDCounter++);
	if (!src) return "";
	var style = "float: left; padding-right: 10px; cursor: pointer;";
	if (maxWidth) {
		style += " max-width: " + maxWidth;
		if (typeof maxWidth == "number") style + "px";
		style += ";";
	}
	if (maxHeight) {
		style += " max-height: " + maxHeight;
		if (typeof maxHeight == "number") style + "px";
		style += ";";
	}
	var result = 
		"<img id=" + imageID + 
		" src='" + src + "'" +
		" style='" + style + "'" + 
		" onclick='LookAndFeel._showFullImage(\""+imageID+"\");'" + 
		//" onmouseout='LookAndFeel._hideFullImage(\""+imageID+"\");'" + 
		"></img>\n";
	return result;
};

LookAndFeel._showFullImage = function (imgID) {
	var floatID = imgID + "_floating";
	var img = $(imgID);
	var pos = Element.cumulativeOffset(img);
	var node = Builder.node(
		"div", { 
			id: floatID,
			style: "position:absolute; top: "+pos.top+"px; left: "+pos.left+"px;",
			onmouseout: "Element.remove('"+floatID+"');",
			onclick: "Element.remove('"+floatID+"');"
		},
		["dies ist ein Test"]);
	node.innerHTML = "<img src='"+img.src+"'></img>";
	document.body.appendChild(node);
	Effect.ScrollTo(node);
}

LookAndFeel._hideFullImage = function (imgID) {
	var floatID = imgID + "_floating";
	$(floatID).remove();
}

LookAndFeel._renderInnerBoxDIVs = function (innerHTML) {
	var html = 
		"	<div class=bt>" +
		"		<div></div>" +
		"	</div>" +
		"	<div class=i1>" +
		"		<div class=i2>" +
		"			<div class=i3>" +
		innerHTML +
		"			</div>" +
		"		</div>" +
		"	</div>" +
		"	<div class=bb>" +
		"		<div></div>" +
		"	</div>";
	return html;
};

LookAndFeel.renderBox = function (innerHTML) {
	return "<div class=cb>"+LookAndFeel._renderInnerBoxDIVs(innerHTML)+"</div>";
};

LookAndFeel.renderButton = function (image, text, action) {
	var html =
		"<p onclick='" + action + "'" +
		(action ? "" : " disabled") + ">";
	if (image) {
		html += 
		"<img src='" + image + "'>";
	}
	html += 
		text +
		"</img>" +
		"</p>";
	return html;
};

LookAndFeel.renderLanguages = function() {
	var languageSelector = "";
	var langs = Translate.getAvailableLanguages();
	for (var i=0; i<langs.length; i++) {
		languageSelector += "<a href='"+window.location.pathname+"?lang="+langs[i]+"'><img border=0 style='width:48px;' src='../image/flag_" + langs[i] + ".png'></img></a>";
	}
	return languageSelector;
}

LookAndFeel.renderSolutionSeparator = function() {
	var html = "<div id='separator-button-interview'><p>" + unescape(Translate.get("result_title")) + "</p></div>";
	return html;
}

LookAndFeel.renderSeparator = function (title, decoration) {
	if (!decoration) decoration = ""; // avoid "undefined" in output
	var languageSelector = "";
	var langs = Translate.getAvailableLanguages();
	for (var i=0; i<langs.length; i++) {
		languageSelector += "<a href='"+window.location.pathname+"?lang="+langs[i]+"'><img border=0 style='width:24px;' src='../image/flag_" + langs[i] + ".gif'></img></a> ";
	}
	var result = 
		"<div style='position: fixed;'>" +
		"<div style='position: absolute; right: 5px;'>" + 
		decoration + 
		"</div>" +
		"<div style='margin-left:5px;'>" +
		"<span class='cb button'>"+LookAndFeel._renderInnerBoxDIVs(title)+"</span>" +
		"</div>" +
		"<div style='position: absolute; margin-top:13px; margin-left:5px;'>" + 
		languageSelector + 
		"&nbsp;" +
		"</div>" +
		"<br><hr>" + 
		"</div>";
	return result;
}

LookAndFeel.renderSection = function (innerHTML, clickAction, isActive) {
	var onclick = (!clickAction) ? "" :
		" onclick='"+clickAction+";' style='cursor:pointer;'";
	var className = isActive ? "cb section sectionactive" : "cb section";
	return "<div class='"+className+"' onmouseover='//Element.addClassName(this, \"sectionhover\");' onmouseout='//Element.removeClassName(this, \"sectionhover\");'"+onclick+">"+LookAndFeel._renderInnerBoxDIVs(innerHTML)+"</div>";
};

LookAndFeel.renderSection2Elements = function (innerHTML, clickAction, isActive) {
	var onclick = (!clickAction) ? "" :
		" onclick='"+clickAction+";' style='cursor:pointer;'";
	var className = isActive ? "cb section sectionactive section2elements cb2elements" : "cb section section2elements cb2elements";
	return "<div class='"+className+"' onmouseover='//Element.addClassName(this, \"sectionhover\");' onmouseout='//Element.removeClassName(this, \"sectionhover\");'"+onclick+">"+LookAndFeel._renderInnerBoxDIVs(innerHTML)+"</div>";
};

LookAndFeel.deactivateSection = function (itemContained) {
	var node = Element.up(itemContained, ".section");
	Element.removeClassName(node, "sectionactive");
};

LookAndFeel.activateSection = function (itemContained) {
	var node = Element.up(itemContained, ".section");
	Element.addClassName(node, "sectionactive");
};

LookAndFeel.deactivateAllSections = function (parent) {
	var nodes = Element.select(parent, ".sectionactive");
	for (var i=0; i<nodes.length; i++) {
		var node = nodes[i];
		Element.removeClassName(node, "sectionactive");
	}
};

LookAndFeel.appendChildDIV = function (parent, html) {
	parent = $(parent);
	var node = Builder.node("div", { className: "appendedChildDIV" });
	parent.appendChild(node);
	node.innerHTML = html;
	return node;
;}

LookAndFeel.removeChildDIV = function (itemContained) {
	var node = Element.up(itemContained, ".appendedChildDIV");
	node.remove();
};