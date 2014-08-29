/*
 * Copyright (C) 2013 denkbares GmbH
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

/**
 * Class for managing the editor tool menu(s)
 *
 * @param flowEditor the flow editor to apply the editor tools for
 * @param menuButton the html element used to display the menu for
 * @constructor
 */
FlowEditor.EditorToolMenu = function(flowEditor, menuButton) {
	this.flowEditor = flowEditor;
	this.menuButton = menuButton;
	this.editTools = [];
	this.initTools();
};

FlowEditor.EditorToolMenu.prototype.isMenuShown = function() {
	return jq$(this.menuButton).hasClass('active');
};

FlowEditor.EditorToolMenu.prototype.hideMenu = function() {
	jq$('.EditorToolMenu').remove();
	jq$(this.menuButton).removeClass('active');
};

FlowEditor.EditorToolMenu.prototype.showMenu = function() {
	var editor = this.flowEditor;
//	var html = "";
//	for (var i = 0; i < this.editTools.length; i++) {
//		var title = this.editTools[i].getTitle(editor);
//		if (!title) html += "<li>-</li>";
//		else {
//			var active = this.editTools[i].isActive(editor);
//			html += "<li" + (active ? "" : " class='ui-state-disabled'") +
//				" data-menuitem='" + this.editTools[i].getId() + "'" +
//				"><a href='#'>" +
//				title + "</a></li>";
//		}
//	}
	var html = jq$("<div class='EditorToolMenu' style='position;absolute'>" +
		"<div class='closearea'></div>" +
		"<div class='menuarea'><ul></ul></div></div>");
	var ul = html.find("ul");
	var self = this;
	jq$.each(this.editTools, function(index, item) {
		ul.append(item.render(self, editor));
	});
	jq$(this.menuButton).addClass('active');
	jq$(this.menuButton).closest('body').append(html);
	jq$('.EditorToolMenu').offset(jq$(this.menuButton).offset());
	jq$('.EditorToolMenu .menuarea ul').menu({role : 'menuitem'});
	jq$('.EditorToolMenu .closearea').click(function() {
		self.hideMenu();
	});
};

FlowEditor.EditorToolMenu.prototype.initTools = function() {

	var oneOrMore = function(flowEditor) {
		var flow = flowEditor.getFlowchart();
		return flow.selection.length > 0;
	};
	var oneComposed = function(flowEditor) {
		var flow = flowEditor.getFlowchart();
		if (flow.selection.length != 1) return false;
		var nodeModel = flow.selection[0].nodeModel;
		if (!nodeModel) return false;
		if (!nodeModel.action) return false;
		var action = new Action(nodeModel.action.markup, nodeModel.action.expression);
		var objectName = action.getInfoObjectName();
		if (!objectName) return false;
		var infoObject = KBInfo.lookupInfoObject(objectName);
		if (!infoObject) return false;
		return infoObject instanceof KBInfo.Flowchart;
	};

	var selectPath = function(flowEditor, backwards, includeRules) {
		var flow = flowEditor.getFlowchart();
		var open = [];
		jq$.each(flow.selection, function(index, item) {
			if (item instanceof Node) {
				open.push(item);
			}
			else {
				open.push(item.getTargetNode());
				flow.setSelection(item.getTargetNode(), true, false);
			}
		});
		while (open.length > 0) {
			var rules = backwards ? open.pop().getIncomingRules() : open.pop().getOutgoingRules();
			jq$.each(rules, function(index, rule) {
				// select rule
				if (includeRules && !flow.isSelected(rule)) {
					flow.setSelection(rule, true, false);
				}
				// select next node
				var next = backwards ? rule.getSourceNode() : rule.getTargetNode();
				if (!flow.isSelected(next)) {
					flow.setSelection(next, true, false);
					open.push(next);
				}
			});
		}
	};

	var selectPathAfter = function(flowEditor) {
		selectPath(flowEditor, false, false);
	};

	var selectPathBefore = function(flowEditor) {
		selectPath(flowEditor, true, false);
	};

	this.editTools = [];
	this.editTools.push(new FlowEditor.EditToolGroup("Select", [
		new FlowEditor.EditTool("Path after node", selectPathAfter, oneOrMore),
		new FlowEditor.EditTool("Path to node", selectPathBefore, oneOrMore)
	]));
	this.editTools.push(new FlowEditor.EditTool("Cleanup Flow", null, function() {
		return false;
	}));
	this.editTools.push(FlowEditor.EditTool.SEPARATOR);
	this.editTools.push(new FlowEditor.EditTool("Unfold Subflow", null, oneComposed));
	this.editTools.push(new FlowEditor.EditTool("Create Subflow", null, oneComposed));
};


/**
 * Class for particular editor tools
 */

FlowEditor.EditTool = function(title, actionFun, isActiveFun) {
	this.title = title;
	this.actionFun = actionFun;
	this.isActiveFun = isActiveFun;
};

FlowEditor.EditTool.SEPARATOR = new FlowEditor.EditTool();

FlowEditor.EditTool.prototype.getTitle = function() {
	return this.title;
};

FlowEditor.EditTool.prototype.render = function(toolMenu, flowEditor) {
	var title = this.getTitle();
	if (!title) return jq$("<li>-</li>");

	var self = this;
	var li = jq$(document.createElement("li"))
		.append(jq$(document.createElement("a")).text(title))
		.click(function() {
			self.execute(flowEditor);
			toolMenu.hideMenu();
		});
	if (!this.isActive(flowEditor)) {
		li.addClass("ui-state-disabled");
	}
	return li;
};

FlowEditor.EditTool.prototype.isActive = function(flowEditor) {
	return this.isActiveFun ? this.isActiveFun(flowEditor) : true;
};

FlowEditor.EditTool.prototype.execute = function(flowEditor) {
	if (this.actionFun) this.actionFun(flowEditor);
};

/**
 * Class for sub-menus of EditTools
 */

FlowEditor.EditToolGroup = function(title, menuItems) {
	this.title = title;
	this.menuItems = menuItems;
};

FlowEditor.EditToolGroup.prototype.getTitle = function() {
	return this.title;
};

FlowEditor.EditToolGroup.prototype.render = function(toolMenu, flowEditor) {
	var li = jq$(document.createElement("li"))
		.append(jq$(document.createElement("a")).text(this.getTitle()))
		.click(function() {
			window.event.stop()
		});
	if (!this.isActive(flowEditor)) {
		li.addClass("ui-state-disabled");
	}
	var subList = jq$(document.createElement("ul")).appendTo(li);
	jq$.each(this.menuItems, function(index, item) {
		subList.append(item.render(toolMenu, flowEditor));
	});
	return li;
};

FlowEditor.EditToolGroup.prototype.isActive = function(flowEditor) {
	var anyActive = false;
	jq$.each(this.menuItems, function(index, item) {
		anyActive |= item.isActive(flowEditor);
		return !anyActive;
	});
	return anyActive;
};


