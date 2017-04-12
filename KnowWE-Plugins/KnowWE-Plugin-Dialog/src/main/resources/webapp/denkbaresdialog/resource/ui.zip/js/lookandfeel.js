var LookAndFeel = {imageIDCounter : 0};

LookAndFeel.renderImage = function(src, maxWidth, maxHeight, optionalMap, optionalClass) {
	// if we use an imagemap we have to deactivate the
	// on-float-show-full-size option.
	// instead we show it full-size from the beginning
	var imageID = "_imageID" + (LookAndFeel.imageIDCounter++);
	if (!src) return "";
	var style = "float: left; padding-right: 10px;";
	if (maxWidth && !optionalMap) {
		style += " max-width: " + maxWidth;
		if (typeof maxWidth == "number") style += "px";
		style += ";";
	}
	if (maxHeight && !optionalMap) {
		style += " max-height: " + maxHeight;
		if (typeof maxHeight == "number") style += "px";
		style += ";";
	}
	if (!optionalClass) optionalClass = "";
	return "<img id=" + imageID +
		" src='" + src + "'" +
		" class='" + optionalClass + "'" +
		" style='" + style + "'" +
		(optionalMap
				? " usemap='#" + optionalMap + "'"
				: " onclick='LookAndFeel._showFullImage(\"" + imageID + "\");'"
			// + " onmouseout='LookAndFeel._hideFullImage(\""+imageID+"\");'"
		) +
		"></img>\n";
};

LookAndFeel._showFullImage = function(imgID) {
	var floatID = imgID + "_floating";
	var img = $(imgID);
	var pos = Element.cumulativeOffset(img);
	var node = Builder.node(
		"div", {
			id : floatID,
			style : "position:absolute; top: " + pos.top + "px; left: " + pos.left + "px;",
			onclick : "LookAndFeel._removeElement('" + floatID + "');",
			onmouseout : "LookAndFeel._removeElement('" + floatID + "');"
		},
		["dies ist ein Test"]);
	node.innerHTML = "<img src='" + img.src + "' usemap='" + img.usemap + "'></img>";
	document.body.appendChild(node);
}

LookAndFeel._removeElement = function(floatID) {
	element = $(floatID);
	if (element) {
		Element.remove(floatID);
	}
}

LookAndFeel._hideFullImage = function(imgID) {
	var floatID = imgID + "_floating";
	$(floatID).remove();
}

LookAndFeel._renderInnerBoxDIVs = function(innerHTML) {
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

LookAndFeel.renderBox = function(innerHTML, classNames) {
	if (classNames) classNames += " cb";
	else classNames = "cb";
	return "<div class='" + classNames + "'>" + LookAndFeel._renderInnerBoxDIVs(innerHTML) + "</div>";
};

LookAndFeel.renderButton = function(image, text, action) {
	var html =
		"<button onclick='" + action + "'" +
		(action ? "" : " disabled") +
		" style='margin-top: 4px;'" +
		">";
	if (image) {
		html +=
			"<img src='" + image + "'" +
			" style='vertical-align: middle; margin-right: 5px;'" +
			">";
	}
	html +=
		text +
		"</img>" +
		"</button>";
	return html;
};

LookAndFeel.renderSeparator = function(title, decoration) {
	if (!decoration) decoration = ""; // avoid "undefined" in output
	var languageSelector = "";
	var langs = Translate.getAvailableLanguages();
	for (var i = 0; i < langs.length; i++) {
		languageSelector += "<a href='" + window.location.pathname + "?lang=" + langs[i] + "'><img border=0 style='width:24px;' src='../image/flag_" + langs[i] + ".gif'></img></a> ";
	}
	return "<div class='separator-" + title.replace(/[^a-zA-Z0-9-_]+/g, "_") + "'>" +
		"<p style='position: relative;'>" +
		"<div style='position: absolute; right: 5px; top: -1px'>" +
		decoration +
		"</div>" +
		"<div style='margin-left:5px;'>" +
		"<span class='cb button'>" + LookAndFeel._renderInnerBoxDIVs(title) + "</span>" +
		"</div>" +
			//"<div style='position: absolute; margin-top:13px; margin-left:5px;'>" +
			//languageSelector +
			//"&nbsp;" +
			//"</div>" +
		"</p>" +
		"<br><hr>" +
		"</div>";
};

LookAndFeel.renderSection = function(innerHTML, clickAction, isActive, classNames) {
	var onclick = (!clickAction) ? "" :
	" onclick='" + clickAction + ";' style='cursor:pointer;'";
	var className = isActive ? "cb section sectionactive" : "cb section";
	if (classNames) className += " " + classNames;
	return "<div class='" + className + "' onmouseover='Element.addClassName(this, \"sectionhover\");' onmouseout='Element.removeClassName(this, \"sectionhover\");'" + onclick + ">" + LookAndFeel._renderInnerBoxDIVs(innerHTML) + "</div>";
};

LookAndFeel.deactivateSection = function(itemContained) {
	var node = Element.up(itemContained, ".section");
	Element.removeClassName(node, "sectionactive");
};

LookAndFeel.activateSection = function(itemContained) {
	var node = Element.up(itemContained, ".section");
	Element.addClassName(node, "sectionactive");
};

LookAndFeel.deactivateAllSections = function(parent) {
	var nodes = Element.select(parent, ".sectionactive");
	for (var i = 0; i < nodes.length; i++) {
		var node = nodes[i];
		Element.removeClassName(node, "sectionactive");
	}
};

LookAndFeel.appendChildDIV = function(parent, html) {
	parent = $(parent);
	var node = Builder.node("div", {className : "appendedChildDIV"});
	parent.appendChild(node);
	node.innerHTML = html;
	return node;
	;
}

LookAndFeel.removeChildDIV = function(itemContained) {
	var node = Element.up(itemContained, ".appendedChildDIV");
	node.remove();
};