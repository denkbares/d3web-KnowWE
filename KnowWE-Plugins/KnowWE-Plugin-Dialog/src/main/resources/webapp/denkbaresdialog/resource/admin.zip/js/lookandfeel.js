
var LookAndFeel = { imageIDCounter: 0 };

LookAndFeel.dateToString = function (date) {
	if (!date) return "---";
	var result = "";
	result += date.getYear() + "-" + LookAndFeel._extendLeft(date.getMonth()+1, "0", 2) + "-" + LookAndFeel._extendLeft(date.getDate(), "0", 2);
	result += " ";
	result += LookAndFeel._extendLeft(date.getHours(), "0", 2) + ":" + LookAndFeel._extendLeft(date.getMinutes(), "0", 2);
	return result;
};

LookAndFeel._extendLeft = function(text, character, minLength) {
	if (!text) text = "";
	text = "" + text;
	while (text.length < minLength) text = character + text;
	return text;
};

LookAndFeel.renderImage = function (src, maxWidth, maxHeight) {
	var imageID = "_imageID"+(LookAndFeel.imageIDCounter++);
	if (!src) return "";
	var style = "float: left; padding-right: 10px;";
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
		" onmouseover='LookAndFeel._showFullImage(\""+imageID+"\");'" + 
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
			onmouseout: "Element.remove('"+floatID+"');"
		},
		["dies ist ein Test"]);
	node.innerHTML = "<img src='"+img.src+"'></img>";
	document.body.appendChild(node);
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

LookAndFeel.renderSeparator = function (title, decoration) {
	if (!decoration) decoration = ""; // avoid "undefined" in output
	var result = 
		"<p style='position: relative;'>" +
		"<div style='position: absolute; right: 5px;'>" + 
		decoration + 
		"</div>" +
		"<div style='margin-left:5px;'>" +
		"<span class='cb button'>"+LookAndFeel._renderInnerBoxDIVs(title)+"</span>" +
		"</div>" +
		"</p>" +
		"<br><hr>";
	return result;
}

LookAndFeel.renderSection = function (innerHTML, clickAction, isActive) {
	return "<div class='cb section'>"+LookAndFeel._renderInnerBoxDIVs(innerHTML)+"</div>";
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