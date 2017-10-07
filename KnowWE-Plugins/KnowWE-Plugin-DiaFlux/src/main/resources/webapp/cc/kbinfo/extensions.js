/**
 * Error handling towards the user interface
 */

var CCMessage = {
	dom: null,
	messages: [],
	
	error: function(title, details) {
		CCMessage._add('error', title, details);
	},
	
	warn: function(title, details) {
		CCMessage._add('warn', title, details);
	},
	
	_getDom: function() {
		if (!CCMessage.dom) {
			CCMessage.dom = Builder.node('div', {
					style: 'border: 2px solid red; ' +
							'position: fixed; ' +
							'top:0px; left:0px;' +
							'z-index: 2000;' +
							'max-width: 300px;'
					});
			document.body.appendChild(CCMessage.dom);
			Element.setOpacity(CCMessage.dom, 0.80);
		}
		return CCMessage.dom;
	},
	
	_add: function(severity, title, details) {
		if (details && !DiaFluxUtils.isString(details)) {	
			details = Object.toHTML(details);
		}		
		CCMessage.messages.push({severity: severity, title: title, details: details});
		CCMessage._select(CCMessage.messages.length - 1);
	},
	
	_select: function(index) {
		var message = CCMessage.messages[index];
		var color = (message.severity == 'warn' ? 'yellow' : '#f88');
		CCMessage._getDom().innerHTML = 
			'<div style="padding: 10px; background-color: '+color+';">' +
			'<span><a href="#" ' +
			(message.details ? 'onclick="javascript:Element.toggle(\'CCMessageDetails\');"' : '') +
			'>' +
			message.title +
			'</a></span>' +
			'&nbsp;&nbsp;<span>('+(index+1)+'/'+CCMessage.messages.length +
			(index > 0 ? '&nbsp;<a href="#" onclick="CCMessage._select('+(index-1)+');">&lt;prev</a>' : '') + 
			(index < CCMessage.messages.length-1 ? '&nbsp;<a href="#" onclick="CCMessage._select('+(index+1)+');">next&gt;</a>' : '') + 
			')</span>' +
			(message.details ? '<div id="CCMessageDetails" style="font-size: 8pt; display:none;">' + message.details + '</div>' : '') +
			'</div>';
	}
};

/**
 * Browser independent clipboard handling
 */
var CCClipboard = {
	_localClipboard: null,
	
	toClipboard: function(text) {
		if (window.clipboardData) {
			// use system clipboard if available (IE)
			window.clipboardData.setData('Text', text);
		}
		else {
			// use own local clipboard
			CCClipboard._localClipboard = text;
		}
	},
	
	fromClipboard: function() {
		if (window.clipboardData) {
			// use system clipboard if available (IE)
			return window.clipboardData.getData('Text');
		}
		else {
			// use own local clipboard
			return CCClipboard._localClipboard;
		}
	}
};

// ----
// Array utils
// ----

Array.prototype.remove = function(item) {
	for (var i=0; i<this.length; i++) {
		if (this[i] == item) {
			this.splice(i, 1);
			return;
		}
	}
}

Array.prototype.contains = function(item) {
	for (var i=0; i<this.length; i++) {
		if (this[i] == item) {
			return true;
		}
	}
	return false;
}

Array.prototype.equals = function(other) {
	if (!other) return false;
	if (!DiaFluxUtils.isArray(other)) return false;
	if (this.length != other.length) return false;
	for (var i=0; i<this.length; i++) {
		var item1 = this[i];
		var item2 = other[i];
		if (item1 && item1.equals && DiaFluxUtils.isFunction(item1.equals)) {
			if (!item1.equals(item2)) return false;
		}
		else {
			if (this[i] != other[i]) return false;
		}
	}
	return true;
}

// ----
// String Utils
// ----

var IdentifierUtils = {};
// ()[]{}<>"'#=&|\*+-,.
IdentifierUtils.CONTROLCHARS_STRING = "\\(\\)\\[\\]\\{\\}<>\"\'#=&|\\*\\+\\-,\\.\t\\n\\r";
IdentifierUtils.QUOTED_STRING = "(?:\"(?:[^\"\\\\]|(?:(?:\\\\\\\\)*(?:\\\\\")*))*\")";
IdentifierUtils.UNQUOTED_STRING = "(?:[^"+IdentifierUtils.CONTROLCHARS_STRING+"]+?)";
IdentifierUtils.IDENTIFIER_STRING = "(?:" + IdentifierUtils.QUOTED_STRING + "|" + IdentifierUtils.UNQUOTED_STRING + ")"

IdentifierUtils.QUOTED = new RegExp(IdentifierUtils.QUOTED_STRING);
IdentifierUtils.UNQUOTED = new RegExp(IdentifierUtils.UNQUOTED_STRING);
IdentifierUtils.IDENTIFIER = new RegExp(IdentifierUtils.IDENTIFIER_STRING);

IdentifierUtils.isIdentifier = function(text) {
	if (!text) return false;
	return text.match(new RegExp("^\\s*"+IdentifierUtils.IDENTIFIER_STRING+"\\s*$"));
}

IdentifierUtils.needQuotes = function(text) {
	if (!text) return false;
	return text.match(new RegExp("["+IdentifierUtils.CONTROLCHARS_STRING+"]"));
}

IdentifierUtils.quote = function(text) {
	if (!text) return text;
	return '"' + text.replace(/\\/g, '\\\\').replace(/\"/g, '\\"') + '"';
}

IdentifierUtils.quoteIfNeeded = function(text) {
	if (IdentifierUtils.needQuotes(text))  {
		text = IdentifierUtils.quote(text);
	}
	return text;
}

IdentifierUtils.unquote = function(text) {
	if (!text) return text;
	if (text.length <= 1) return text;
	if (text.charAt(0) != '"') return text;
	if (text.charAt(text.length-1) != '"') return text;
	return text.substring(1,text.length-1).replace(/\\"/g, '"').replace(/\\\\/g, '\\');
}

String.prototype.escapeQuote = function() {
	var result = this.gsub(/\"/,'\\"');
	return result;
}

String.prototype.escapeXML = function() {
	//TODO: handle escaping well without using html entities!!!
	var result = this.escapeHTML();
	return result;
}

String.prototype.escapeHTML = function() {
    return this.replace(/&/g,'&amp;')
    	.replace(/</g,'&lt;').replace(/>/g,'&gt;')
    	.replace(/"/g,'&quot;').replace(/'/g,'&apos;');
}


//----
//Element Utils
//----

Element.scrollVisible = function(scrollPane, element) {
	var posElement = $(element).viewportOffset();
	var x1 = posElement.left;
	var x2 = posElement.left + element.offsetWidth;
	var y1 = posElement.top;
	var y2 = posElement.top + element.offsetHeight;

	var posScroll = $(scrollPane).cumulativeOffset();
	var sx1 = posScroll.left;
	var sx2 = posScroll.left + scrollPane.offsetWidth;
	var sy1 = posScroll.top;
	var sy2 = posScroll.top + scrollPane.offsetHeight;
	if (x1 < sx1) {
		scrollPane.scrollLeft -= (sx1 - x1); 
	}
	else if (x2 > sx2) {
		scrollPane.scrollLeft += (x2 - sx2); 
	}
	if (y1 < sy1) {
		scrollPane.scrollTop -= (sy1 - y1); 
	}
	else if (y2 > sy2) {
		scrollPane.scrollTop += (y2 - sy2); 
	}
}

String.prototype.parseXML = function() {
	//for IE
	if (window.ActiveXObject) {
		var xmlDoc = new ActiveXObject('Microsoft.XMLDOM');
		xmlDoc.async = 'false';
		xmlDoc.loadXML('<xml>'+this+'</xml>');
		return xmlDoc;
	}
	//for Mozilla, Firefox, Opera, etc.
	else if (document.implementation && document.implementation.createDocument) {
		var parser = new DOMParser();
		return parser.parseFromString('<xml>'+this+'</xml>', 'text/xml');
	}
	else {
		var node = Builder.node('xml');
		node.innerHTML = this;
		return node;
	}
}

var DiaFluxUtils;

if (!DiaFluxUtils){
	
	DiaFluxUtils = {};
}

DiaFluxUtils.isArray = function(obj) {
   if (obj.constructor.toString().indexOf("Array") == -1)
      return false;
   else
      return true;
}

DiaFluxUtils.isString = function(obj){
	return typeof obj === "string";
	
}

DiaFluxUtils.isFunction = function(object) {
    return typeof object === "function";
}

DiaFluxUtils.escapeRegex = function(regexString) {
	return regexString.replace(/([.*+?^=!:${}()|[\]\/\\])/g, '\\$1');
}

DiaFluxUtils.isControlKey = function(event) {
	var altKey = event.altKey;
	var metaKey = event.metaKey;
	var ctrlKey = event.ctrlKey;

	return ctrlKey | altKey | metaKey; 
}

DiaFluxUtils.createDottedLine = function(x1, y1, x2, y2, pixelSize, pixelColor, spacing, maxDots) {
	var cx = x2 - x1;
	var cy = y2 - y1;
	var len = Math.sqrt(cx*cx + cy*cy);
	var dotCount = len / (spacing + pixelSize);
	if (maxDots && dotCount > maxDots) dotCount = maxDots;
	var dx = cx / dotCount;
	var dy = cy / dotCount;

	var x = x1;
	var y = y1;
	var dotsHTML = '';
	for (var i=0; i<dotCount; i++) {
		// make Dot
		dotsHTML += '<div style="position:absolute; overflow:hidden; ' +
					'left:' + Math.ceil(x-pixelSize/2) + 'px; ' +
					'top:' + Math.ceil(y-pixelSize/2) + 'px; ' +
					'width:' + pixelSize + 'px; ' +
					'height:' + pixelSize + 'px; ' +
					'background-color: ' + pixelColor + ';"></div>';
		//parentDIV.appendChild(dot);
		// proceed to next dot
		x += dx;
		y += dy;
	}
	
	var div = Builder.node('div', {
		 style: 'position:absolute; overflow:visible; ' +
		 		'top: 0px; left: 0px; width:1px; height:1px;'
	});
	div.innerHTML = dotsHTML;
	return div;
}


