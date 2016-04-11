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
