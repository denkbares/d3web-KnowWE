
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
							'top:0px; right:0px;' +
							'z-index: 2000;' +
							'max-width: 300px;'
					});
			document.body.appendChild(CCMessage.dom);
			Element.setOpacity(CCMessage.dom, 0.80);
		}
		return CCMessage.dom;
	},
	
	_add: function(severity, title, details) {
		if (details && !Object.isString(details)) {	
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

String.escapeQuotes = function(text) {
	if (!text) return text;
	return text.replace(/'/g, "\\'").replace(/"/g, "\\\"");
};

