function ToolMenu() {
	this.cache = {};
}

ToolMenu.prototype.showToolPopupMenu = function(nodeID) {
	this.hideToolsPopupMenu();
	var node = $(nodeID);
	var pos = node.getPosition();
	var w = node.offsetWidth, h = node.offsetHeight;
	var par = new Element('div', {
		'id' : 'toolPopupMenuID',
		'styles' : {
			'top' : pos.y + 'px',
			'left' : pos.x + 'px',
			'position' : 'absolute'
		},
		'events' : {
			'mouseleave' : ToolMenu.hideToolsPopupMenu
		}
	});
	document.body.appendChild(par);

	par.innerHTML = "<div class='toolMenuFrame'>" + "<div style='width:" + w
			+ "px;height:" + h + "px;' onclick='ToolMenu.hideToolsPopupMenu();'></div>"
			+ this.getToolMenuHtml(jq$('#' + nodeID).attr('sectionID')) + "</div>";
}

ToolMenu.prototype.getToolMenuHtml = function(id) {

	if (!this.cache[id]) {
		var params = {
			action : 'GetToolMenuAction',
			sectionID : id
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
		this.cache[id] = parsedResponse.menuHTML;
	}
	return this.cache[id];
}

ToolMenu.prototype.hideToolsPopupMenu = function() {
	var old = $('toolPopupMenuID');
	if (old) {
		old.remove();
	}
}

ToolMenu.prototype.decorateToolMenus = function() {
	jq$('.toolsMenuDecorator').each(function() {
		var a = jq$(this);
		a.parent().mouseenter(function() {
			a.css('visibility', 'visible');
		});
		a.parent().mouseleave(function() {
			a.css('visibility', 'hidden');
		});
		a.click(function() {
			ToolMenu.showToolPopupMenu(a.attr('id'));
		});
	});
}
var ToolMenu = new ToolMenu();

jq$(document).ready(function() {
	ToolMenu.decorateToolMenus();
});