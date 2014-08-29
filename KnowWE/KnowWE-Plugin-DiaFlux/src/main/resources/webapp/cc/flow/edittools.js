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
	var html = "";
	for (var i = 0; i < this.editTools.length; i++) {
		var title = this.editTools[i].getTitle(editor);
		if (!title) html += "<li>-</li>";
		else {
			var active = this.editTools[i].isActive(editor);
			html += "<li" + (active ? "" : " class='ui-state-disabled'") +
				" data-menuitem='" + this.editTools[i].getId() + "'"+
				"><a href='#'>" +
				title + "</a></li>";
		}
	}
	html = "<div class='EditorToolMenu' style='position;absolute'>" +
		"<div class='closearea'></div>" +
		"<div class='menuarea'><ul>" + html +
		"</ul></div></div>";
	jq$(this.menuButton).addClass('active');
	jq$(this.menuButton).closest('body').append(html);
	jq$('.EditorToolMenu').offset(jq$(this.menuButton).offset());
	jq$('.EditorToolMenu .menuarea ul').menu({role : 'menuitem'});
	var self = this;

	jq$('.EditorToolMenu .closearea').click(function() {
		self.hideMenu()
	});
	jq$('.EditorToolMenu .menuarea ul li').click(function(event) {
		var id = jq$(event.currentTarget).data("menuitem");
		self._selectMenu(id);
	});
};

FlowEditor.EditorToolMenu.prototype._selectMenu = function(menuItemId) {
	for (var i = 0; i < this.editTools.length; i++) {
		if (this.editTools[i].getId() == menuItemId) {
			this.editTools[i].execute(this.flowEditor);
			return;
		}
	}
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

	var actionSelectPath = function(flowEditor) {
		var flow = flowEditor.getFlowchart();
		var open = [];
		for (var i = 0; i < flow.selection.length; i++) {
			var item = flow.selection[i];
			if (item instanceof Node) {
				open.push(item);
			}
			else {
				open.push(item.getTargetNode());
				flow.setSelection(item.getTargetNode(), true, false);
			}
		}
		while (open.length > 0) {
			var rules = open.pop().getOutgoingRules();
			for (var i = 0; i < rules.length; i++) {
				// select rule
//				if (!flow.isSelected(rules[i])) {
//					flow.setSelection(rules[i], true, false);
//				}
				// select target node
				var next = rules[i].getTargetNode();
				if (!flow.isSelected(next)) {
					flow.setSelection(next, true, false);
					open.push(next);
				}
			}
		}
	};

	this.editTools = [];
	this.editTools.push(new FlowEditor.EditTool("Select Path", actionSelectPath, oneOrMore));
	this.editTools.push(new FlowEditor.EditTool("Cleanup Flow", null, function() {return false;}));
	this.editTools.push(new FlowEditor.EditTool(null, null, null));
	this.editTools.push(new FlowEditor.EditTool("Unfold Subflow", null, oneComposed));
	this.editTools.push(new FlowEditor.EditTool("Create Subflow", null, oneComposed));
};


/**
 * Class for particular editor tools
 */

FlowEditor.EditTool = function(title, actionFun, isActiveFun) {
	this.id = title ? (FlowEditor.EditTool.idCounter++) + "." + title.replace(/[^\w]+/g, "") : null;
	this.title = title;
	this.actionFun = actionFun;
	this.isActiveFun = isActiveFun;
};

FlowEditor.EditTool.idCounter = 1;

FlowEditor.EditTool.prototype.getId = function() {
	return this.id;
};

FlowEditor.EditTool.prototype.getTitle = function(flowEditor) {
	return this.title;
};

FlowEditor.EditTool.prototype.isActive = function(flowEditor) {
	return this.isActiveFun ? this.isActiveFun(flowEditor) : true;
};

FlowEditor.EditTool.prototype.execute = function(flowEditor) {
	if (this.actionFun) this.actionFun(flowEditor);
};

