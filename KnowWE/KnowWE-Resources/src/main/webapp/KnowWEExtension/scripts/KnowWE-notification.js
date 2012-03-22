/**
 * Title: KnowWE-notification 
 * Notification mechanism used in KnowWE and denkbares-Dialog (MobileApplication).
 * This file uses the jQuery library and is separated from KnowWE-helper.js in order
 * to avoid conflicts with JSPWiki's mootools.js and denkbares-Dialogs's scriptacouls.js
 */

if (typeof KNOWWE == "undefined" || !KNOWWE) {
    /**
	 * The KNOWWE global namespace object. If KNOWWE is already defined, the
	 * existing KNOWWE object will not be overwritten so that defined namespaces
	 * are preserved.
	 */
    var KNOWWE = {};
}

/**
 * Class: KNOWWE.notification 
 * functions and variables for the notification mechanism.
 */
KNOWWE.notification = {
	
	messages : [],
	activeIndex : -1,
	
	error : function(title, details) {
		KNOWWE.notification._add('error', title, details);
	},
		
	warn : function(title, details) {
		KNOWWE.notification._add('warn', title, details);
	},
		
	_getDom : function() {
		if (!document.getElementById('KnowWENotificationDom')) {
			// title
			title = jq$('<div></div>').attr({
				id: 'KnowWENotificationTitle',
				style: 'float:left'
			});
			
			// message
			message = jq$('<div></div>').attr({
				id: 'KnowWENotificationMessage'
			});
			
			// title + message -> wrapper
			wrapper = jq$('<div></div>');
			title.appendTo(wrapper);
			message.appendTo(wrapper);
			
			// counter
			counter = jq$('<div></div>').attr({
					id: 'KnowWENotificationCounter',
					style: 	'padding: 5px; ' + 
							'position: fixed; ' +
							'top: 0px; ' +
							'right: 25px;'
			});
			
			// quit
			quit = jq$('<div></div>').attr({
				id: 'KnowWENotificationQuit',
				style: 	'padding: 5px; ' + 
						'position: fixed; ' +
						'top: 0px; ' +
						'right: 0px;'
			});
			
			// dom
			dom = jq$('<div></div>').attr({
				id: 'KnowWENotificationDom'
				});
			wrapper.appendTo(dom);
			counter.appendTo(dom);
			quit.appendTo(dom);
			dom.appendTo('body');
		}
		return jq$('#KnowWENotificationDom');
	},
	
	_add : function(severity, title, details) {	
		KNOWWE.notification.messages.push({severity: severity, title: title, details: details});
		KNOWWE.notification._select(KNOWWE.notification.messages.length - 1);
	},
	
	_select : function(index) {
		
		KNOWWE.notification.activeIndex = index;
		var message = KNOWWE.notification.messages[index];
		var dom = KNOWWE.notification._getDom();
		
		// set colors
		var startColor = '#fff6c3';
		var endColor = '#f9eba5';
		if (message.severity == 'error') {
			startColor = '#efd5d3';
			endColor = '#e6bbb8';
		}
		
		// css
		dom.attr({ style :  'opacity:0.8;' +
                            'position:fixed;' +
                            'z-index:2000;' +
                            'border-bottom:1px solid #7a7a7a;' +
                            'top:0px;' +
                            'left:0px;' + 
                            'right:0px;' +
                            'width:100%;' +
                            'padding:5px;' +
                            'background-color: #f9eba5;' +
                            'background-image: -webkit-gradient(linear, 0% 0%, 0% 100%, from(' + startColor + '), to(' + endColor + '));' +
                            'background-image: -webkit-linear-gradient(top, ' + startColor + ',' + endColor + ');' +
                            'background-image: -moz-linear-gradient(top, ' + startColor + ',' + endColor + ');' +
                            'background-image: -ms-linear-gradient(top, ' + startColor + ',' + endColor + ');' +
                            'background-image: -o-linear-gradient(top, ' + startColor + ',' + endColor + ');'
				});
		
		// title
		if (message.title) {
			var titleHTML = '<strong>' + message.title + ':</strong>&nbsp;';
			jq$('#KnowWENotificationTitle').html(titleHTML);
		}
		
		// message
		jq$('#KnowWENotificationMessage').html(message.details);
		
		// counter
		if (KNOWWE.notification.messages.length > 1) {
			var counterHTML = "";
			var counter = '&nbsp;<span>(' + (index+1) + '/' +KNOWWE.notification.messages.length + ')</span>';
			var next = '&nbsp;<a href="#" onclick="KNOWWE.notification._select(' + (index+1) + ');">&gt;</a>';
			var prev = '&nbsp;<a href="#" onclick="KNOWWE.notification._select(' + (index-1) + ');">&lt;</a>';
		    if (index == 0) {
		        counterHTML += counter;
		        counterHTML += next;
		    } else if (index >= 0 && index < KNOWWE.notification.messages.length-1) {
			    counterHTML += prev;
			    counterHTML += counter;
			    counterHTML += next;
		    } else if (index == KNOWWE.notification.messages.length-1) {
		        counterHTML += prev;
			    counterHTML += counter;
		    }
		    jq$('#KnowWENotificationCounter').html(counterHTML);
		} else {
		    jq$('#KnowWENotificationCounter').html("");
		}
		
        var jsAction = KNOWWE.notification.getQuitActions();
		jq$('#KnowWENotificationQuit').html('<span><a href="#" onclick="javascript:' + jsAction + '">X</a></span>');
		
		// show notification bar
		jq$('#KnowWENotificationDom').show();
	},
	
	getQuitActions : function() {
	    var index = KNOWWE.notification.activeIndex;
	    // quit: remove current message from stack
		var jsAction = 'KNOWWE.notification.messages.splice(' + index + ', 1);';
		// quit: other notifications? show them!
		if (index > 0) { 
			jsAction += 'KNOWWE.notification._select(' + (index-1) + ');';
		// quit: no other notifications? hide notification bar!
		} else if (index == 0 && KNOWWE.notification.messages.length > 1) {
		    jsAction += 'KNOWWE.notification._select(' + (index) + ');';
		} else { 
			jsAction += 'jq$(\'#KnowWENotificationDom\').hide();';
		}
		return jsAction;
	}
		    
};