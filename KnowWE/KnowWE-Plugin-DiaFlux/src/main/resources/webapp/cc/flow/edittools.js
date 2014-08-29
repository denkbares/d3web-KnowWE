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
	jq$('.EditorToolMenu .menuarea ul').menu();
	jq$('.EditorToolMenu .closearea').click(function() {
		self.hideMenu();
	});
};

FlowEditor.EditorToolMenu.prototype.initTools = function() {

	var oneOrMore = function(flowEditor) {
		var flow = flowEditor.getFlowchart();
		return flow.selection.length >= 1;
	};
	var twoOrMoreNodes = function(flowEditor) {
		return flowEditor.getFlowchart().getSelectedNodes().length >= 2;
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

	var align = function(flowEditor, horizontal, useMinMiddleMax) {
		var getPos = function(node) {
			var pos = horizontal ? node.getLeft() : node.getTop();
			var size = horizontal ? node.getWidth() : node.getHeight();
			return (useMinMiddleMax == "min") ? pos :
				(useMinMiddleMax == "middle") ? pos + size / 2 : pos + size;
		};
		var setPos = function(node, pos) {
			var size = horizontal ? node.getWidth() : node.getHeight();
			if (useMinMiddleMax == "max")  pos -= size;
			if (useMinMiddleMax == "middle")  pos -= size / 2;
			if (horizontal) {
				node.moveTo(pos, node.getTop());
			}
			else {
				node.moveTo(node.getLeft(), pos);
			}
		};
		var nodes = flowEditor.getFlowchart().getSelectedNodes();
		var total = 0;
		jq$.each(nodes, function(index, node) {
			total += getPos(node);
		});
		jq$.each(nodes, function(index, node) {
			setPos(node, total / nodes.length);
		});
	};

	var alignTop = function(flowEditor) {
		align(flowEditor, false, "min");
	};
	var alignMiddle = function(flowEditor) {
		align(flowEditor, false, "middle");
	};
	var alignBottom = function(flowEditor) {
		align(flowEditor, false, "max");
	};

	var alignLeft = function(flowEditor) {
		align(flowEditor, true, "min");
	};
	var alignCenter = function(flowEditor) {
		align(flowEditor, true, "middle");
	};
	var alignRight = function(flowEditor) {
		align(flowEditor, true, "max");
	};

	this.editTools = [];
	this.editTools.push(new FlowEditor.EditToolGroup("Select", [
		new FlowEditor.EditTool("Path after node", selectPathAfter, oneOrMore, 'path-after-node'),
		new FlowEditor.EditTool("Path to node", selectPathBefore, oneOrMore, 'path-to-node')
	]));
	this.editTools.push(new FlowEditor.EditToolGroup("Align", [
		new FlowEditor.EditTool("Top", alignTop, twoOrMoreNodes, 'align-top'),
		new FlowEditor.EditTool("Middle", alignMiddle, twoOrMoreNodes, 'align-middle'),
		new FlowEditor.EditTool("Bottom", alignBottom, twoOrMoreNodes, 'align-bottom'),
		FlowEditor.EditTool.SEPARATOR,
		new FlowEditor.EditTool("Left", alignLeft, twoOrMoreNodes, 'align-left'),
		new FlowEditor.EditTool("Middle", alignCenter, twoOrMoreNodes, 'align-center'),
		new FlowEditor.EditTool("Right", alignRight, twoOrMoreNodes, 'align-right')
	]));
	this.editTools.push(new FlowEditor.EditTool("Cleanup Flow", null, function() {
		return false;
	}));
	this.editTools.push(FlowEditor.EditTool.SEPARATOR);
	this.editTools.push(new FlowEditor.EditTool("Unfold Subflow", null, oneComposed, 'unfold-subflow'));
	this.editTools.push(new FlowEditor.EditTool("Create Subflow", null, oneComposed, 'extract-subflow'));
};


/**
 * Class for particular editor tools
 */

FlowEditor.EditTool = function(title, actionFun, isActiveFun, icon) {
	this.title = title;
	this.actionFun = actionFun;
	this.isActiveFun = isActiveFun;
	this.icon = icon;
};

FlowEditor.EditTool.SEPARATOR = new FlowEditor.EditTool();

FlowEditor.EditTool.prototype.getTitle = function() {
	return this.title;
};

FlowEditor.EditTool.prototype.getIcon = function() {
	return this.icon;
};

FlowEditor.EditTool.prototype.render = function(toolMenu, flowEditor) {
	var title = this.getTitle();
	if (!title) return jq$("<li>-</li>");

	var self = this;
	var a = jq$(document.createElement("a")).text(title);
	if (this.getIcon()) {
		a.prepend('<span class="ui-icon ui-icon-' + this.getIcon() + '"></span>');
	}
	var li = jq$(document.createElement("li"))
		.append(a)
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

FlowEditor.EditToolGroup = function(title, menuItems, icon) {
	this.title = title;
	this.menuItems = menuItems;
	this.icon = icon;
};

FlowEditor.EditToolGroup.prototype.getIcon = function() {
	return this.icon;
};

FlowEditor.EditToolGroup.prototype.getTitle = function() {
	return this.title;
};

FlowEditor.EditToolGroup.prototype.render = function(toolMenu, flowEditor) {
	var li = jq$(document.createElement("li"))
		.append(jq$(document.createElement("a")).text(this.getTitle()))
		.click(function(event) {
			event.stopPropagation()
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
		if (item.getTitle()) anyActive |= item.isActive(flowEditor);
		return !anyActive;
	});
	return anyActive;
};


