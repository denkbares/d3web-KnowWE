/**
 * --------------
 * Event handling
 * --------------
 * 
 * A word to event handling:
 * Due to havin different dialogs and cklickabe elements nad due to browsers
 * event bublling, it is feasible to improve a new kinf of 'capturing' event
 * handling. This event handling provides to access an event by specifying
 * new event methods insteaf of "onClick", "onMouseDown" or "onKeyDown".
 * The framework takes care of delivering the event ONLY to the most specific
 * element that has an event handler for the specific event.
 * If the event should be propagated upward to the next level (contrary to its
 * default behaviour) use the method 'ccEvent.nextHandler()'.
 * 
 * The event handler attribute can still be either 
 * (a) a string value that will be evaluated when activating the event 
 * or
 * (b) a function that may take the event as an argument
 * 
 * In both cases 'this' is still bound to the element that has the event handler 
 * function. For simpler use, there is an additional distinction. If the evaluation
 * of the string delivers an function, the function will be called immediately with
 * this bound to the element and the event object as the only argument:
 * 
 * example:
 * <div ccClick="function(event) {alert(event);}">click me and see event object!</div>
 * 
 * Supported event methods are:
 * 1. 'ccClick'
 * 2. 'ccMouseDown'
 * 3. 'ccKeyDown'
 * 
 */

var CCEvents = {
	
	_classListeners: {}, // listeners[eventName][className] = function

	addClassListener: function(eventName, className, listener) {
		if (!this._classListeners[eventName]) this._classListeners[eventName] = {};
		if (!this._classListeners[eventName][className]) {
			this._classListeners[eventName][className] = [listener];
		}
		else {
			this._classListeners[eventName][className].push(listener);
		}
	},
	
	removeClassListener: function(eventName, className, listener) {
		if (!this._classListeners[eventName]) return;
		if (!this._classListeners[eventName][className]) return;
		this._classListeners[eventName][className].remove(listener);
	},
	
	addElementListener: function(eventName, element, listener) {
		// for elements, just store the listeners into a element attribute
		element = $(element);
		if (!element._ccListeners) element._ccListeners = {};
		if (!element._ccListeners[eventName]) {
			element._ccListeners[eventName] = [listener];
		}
		else {
			element._ccListeners[eventName].push(listener);
		}
	},

	removeElementListener: function(eventName, element, listener) {
		element = $(element);
		if (!element._ccListeners) return;
		if (!element._ccListeners[eventName]) return;
		element._ccListeners[eventName].remove(listener);
	},
	
	_handleEvent: function(eventName, event) {
		var handlerByClass = this._classListeners[eventName];
		if (!handlerByClass) return;
		
		event.nextHandler = function() {
			// continue selecting next event handler
			this._continue = true;
		};

		event.defaultHandler = function() {
			// ignore following event handlers,
			// but continue with systems default handlers (e.g. input fields)
			this._break = true;
		};

		var isMouseButtonEvent = (eventName == 'click' || eventName == 'dblclick' || eventName == 'mouseup' || eventName == 'mousedown');
		var node = event.element();
		while (node) {
			// do not overwrite click events for default components
			if (isMouseButtonEvent 
					&& (node.nodeName == 'SELECT' 
					|| node.nodeName == 'INPUT'
					|| node.nodeName == 'BUTTON') 
					) {
				return;
			}
			
			// first check if node has its own listener
			if (node._ccListeners) {
				var handlers = node._ccListeners[eventName];
			if (handlers) for (var k=0; k<handlers.length; k++) {
					event._continue = false;
					// eval handler with bound "this=node" and event as an argument
					handlers[k].bind(node)(event);
					// check proceed to default handlers
					if (event._break) {
						return;
					}
					// if event was already handled, we prevent further event bubbling
					if (!event._continue) {
						Event.stop(event);
						//showMessage(node);
						return;
					}
				}
			}
			
			// second check if there are any listeners registered to one of the node's classes
			var classes = $w(node.className);
			for (var i=0; i<classes.length; i++) {
				var handlers = handlerByClass[classes[i]];
				if (handlers) for (var k=0; k<handlers.length; k++) {
					event._continue = false;
					// eval handler with bound "this=node" and event as an argument
					handlers[k].bind(node)(event);
					// check proceed to default handlers
					if (event._break) {
						return;
					}
					// if event was already handled, we prevent further event bubbling
					if (!event._continue) {
						Event.stop(event);
						//showMessage(node);
						return;
					}
				}
			}
			node = node.parentNode;
		}
		//showMessage("unblocked event blocked")
	},
	
	/**
	 * Event handler for ignoring all custom events and use browsers default events
	 * e.g. keys inside an edit field
	 */
	defaultEventHandler: function(event) {
		event.defaultHandler();
	}
};

if (!CCEvents.isInitialized) { 
	document.observe('keydown',		function(event) { CCEvents._handleEvent('keydown', event); });
	document.observe('mousedown',	function(event) { CCEvents._handleEvent('mousedown', event); });
	document.observe('click',		function(event) { CCEvents._handleEvent('click', event); });
	document.observe('dblclick',	function(event) { CCEvents._handleEvent('dblclick', event); });
	CCEvents.isInitialized = true;
}


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

String.prototype.escapeRegExp = function() {
	// place an '\' before each occurence of any of these characters: "/()\[]^$"
	//var result = this.gsub(/([\/\(\)\\\[\]\^\$])/, '\\#{0}'); // geht nicht, warum auch immer?!?
	// replace each occurence of any of these characters "/()\[]^$" 
	// with the wildcard character ('.')
	//var result = this.gsub(/([\/\(\)\\\[\]\^\$])/,'.');
	//return result;
	return RegExp.escape(this);
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
    return this.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
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

	
function createDottedLine(x1, y1, x2, y2, pixelSize, pixelColor, spacing, maxDots) {
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

