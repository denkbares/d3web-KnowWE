/*==================================================
 *  DOM Utility Functions
 *==================================================
 */

SimileAjax.DOM = new Object();

SimileAjax.DOM.registerEventWithObject = function(elmt, eventName, obj, handlerName) {
    SimileAjax.DOM.registerEvent(elmt, eventName,  function(elmt2, evt, target) {
    	jq$.proxy(obj, handlerName, elmt2, evt, target);
    });
};

SimileAjax.DOM.registerEvent = function(elmt, eventName, handler) {
	jq$(elmt).bind(eventName, function(event) {
		return handler(elmt, event, event.target);
	});
};

SimileAjax.DOM.getEventRelativeCoordinates = function(evt, elmt) {
	var coords = jq$(elmt).offset();
    if (jq$.browser.msie) {
      if (evt.type == "mousewheel") {
        return {
          x: evt.clientX - coords.left, 
          y: evt.clientY - coords.top
        };        
      } else {
        return {
          x: evt.offsetX,
          y: evt.offsetY
        };
      }
    } else {
          return {
              x: evt.pageX - coords.left,
              y: evt.pageY - coords.top
          };
    }
};

SimileAjax.DOM.cancelEvent = function(evt) {
    evt.returnValue = false;
    evt.cancelBubble = true;
    if ("preventDefault" in evt) {
        evt.preventDefault();
    }
};


