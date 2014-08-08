/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

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
			_TM.showToolPopupMenu(a);
		});
		a.data('toolMenuDecorated', 'true');
		//prevent "click through" in composite edit
		a.click(function(e) {
			e.stopPropagation();
		});
	});
};

ToolMenu.prototype.showToolPopupMenu = function(node) {
	this.hideToolsPopupMenu();
	node = node[0];
	var par = new Element('div', {
		'id' : 'toolPopupMenuID',
		'styles' : {
			'top' : jq$(node).offset().top + 'px',
			'left' : jq$(node).offset().left + 'px',
			'z-index' : '10000',
			'position' : 'absolute'
		},
		'events' : {
			'mouseleave' : _TM.hideToolsPopupMenu
		}
	});
	var scale = 1;
	jq$(node).parentsUntil('#pagecontent').each(function() {
		if (scale == 1) scale = jq$(this).scale();
	});
	document.body.appendChild(par);
	par.innerHTML = "<div class='toolMenuFrame'>" + "<div style='width:" + node.offsetWidth * scale
		+ "px;height:" + node.offsetHeight * scale + "px;' onclick='_TM.hideToolsPopupMenu();'></div>"
		+ this.getToolMenuHtml(node) + "</div>";
};

ToolMenu.prototype.getToolMenuHtml = function(node) {


	var toolMenuIdentifier = jq$(node).attr('toolMenuIdentifier');
	if (!this.cache[toolMenuIdentifier]) {
		var toolMenuAction = 'GetToolMenuAction';
		var specialAction = jq$(node).attr('toolMenuAction');
		if (specialAction) {
			toolMenuAction = specialAction;
		}

		var params = {
			action : toolMenuAction,
			identifier : toolMenuIdentifier
		};

		var options = {
			url : KNOWWE.core.util.getURL(params),
			async : false,
			response : {
				onError : _EC.onErrorBehavior
			}
		};
		var ajaxCall = new _KA(options);
		ajaxCall.send();
		var parsedResponse = JSON.parse(ajaxCall.getResponse());
		this.cache[ parsedResponse.sectionId] = parsedResponse.menuHTML;
		if (specialAction) {
			jq$(node).removeAttr('toolMenuAction');
			jq$(node).attr('toolMenuIdentifier', parsedResponse.sectionId);
			toolMenuIdentifier = parsedResponse.sectionId;
		}
	}
	return this.cache[toolMenuIdentifier];
};

ToolMenu.prototype.hideToolsPopupMenu = function() {
	var old = $('toolPopupMenuID');
	if (old) {
		old.remove();
	}
};

var _TM = new ToolMenu();

jq$(document).ready(function() {
	_TM.decorateToolMenus();
});

KNOWWE.helper.observer.subscribe("flowchartrendered", function() {
	_TM.decorateToolMenus(jq$('.Flowchart'))
});

KNOWWE.helper.observer.subscribe("afterRerender", function() {
	var replacedElement = this;
	KNOWWE.core.rerendercontent.animateDefaultMarkupMenu(jq$(replacedElement));
});

