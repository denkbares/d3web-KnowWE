function ToolMenu() {
	this.cache = {};
}

ToolMenu.prototype.decorateToolMenus = function(parent) {
	parent = jq$(parent);
	var decorators = parent ? parent.find('.toolsMenuDecorator') : jq$('.toolsMenuDecorator');
	jq$('.toolsMenuDecorator').each(function() {
		var a = jq$(this);
		if (a.data('toolMenuDecorated') === 'true') return;
		a.parent().mouseenter(function() {
			a.css('visibility', 'visible');
		});
		a.parent().mouseleave(function() {
			a.css('visibility', 'hidden');
		});
		a.click(function() {
			ToolMenu.showToolPopupMenu(a);
		});
		a.data('toolMenuDecorated', 'true');
	});
}

ToolMenu.prototype.showToolPopupMenu = function($node) {
	this.hideToolsPopupMenu();
	var node = $node[0];
	var pos = node.getPosition();
	var w = node.offsetWidth, h = node.offsetHeight;
	var par = new Element('div', {
		'id' : 'toolPopupMenuID',
		'styles' : {
			'top' : pos.y + 'px',
			'left' : pos.x + 'px',
			'z-index' : '10000',
			'position' : 'absolute'
		},
		'events' : {
			'mouseleave' : ToolMenu.hideToolsPopupMenu
		}
	});
	document.body.appendChild(par);
	par.innerHTML = "<div class='toolMenuFrame'>" + "<div style='width:" + w
			+ "px;height:" + h + "px;' onclick='ToolMenu.hideToolsPopupMenu();'></div>"
			+ this.getToolMenuHtml($node) + "</div>";
}

ToolMenu.prototype.getToolMenuHtml = function($node) {
	
	var toolMenuIdentifier = $node.attr('toolMenuIdentifier');
	
	if (!this.cache[toolMenuIdentifier]) {
		var toolMenuAction = 'GetToolMenuAction';
		var specialAction = $node.attr('toolMenuAction');
		if (specialAction) {
			toolMenuAction = specialAction;
		}

		var params = {
			action : toolMenuAction,
			identifier : toolMenuIdentifier
		}

		var options = {
			url : KNOWWE.core.util.getURL(params),
			async : false,
			response : {
				onError : _EC.onErrorBehavior,
			}
		}
		var ajaxCall = new _KA(options);
		ajaxCall.send();
		var parsedResponse = JSON.parse(ajaxCall.getResponse());
		this.cache[toolMenuIdentifier] = parsedResponse.menuHTML;
	}
	return this.cache[toolMenuIdentifier];
}

ToolMenu.prototype.hideToolsPopupMenu = function() {
	var old = $('toolPopupMenuID');
	if (old) {
		old.remove();
	}
}

var ToolMenu = new ToolMenu();

jq$(document).ready(function() {
	ToolMenu.decorateToolMenus();
});

KNOWWE.helper.observer.subscribe("flowchartrendered", function() {ToolMenu.decorateToolMenus(jq$('.Flowchart'))});