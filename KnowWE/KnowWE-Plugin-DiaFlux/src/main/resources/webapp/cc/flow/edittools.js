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

	/**
	 * Some helper functions
	 */
	var getAction = function(node) {
		var nodeModel = node.nodeModel;
		if (!nodeModel) return null;
		if (!nodeModel.action) return null;
		return new Action(nodeModel.action.markup, nodeModel.action.expression);
	};

	var getInfoObject = function(node) {
		var action = getAction(node);
		if (!action) return null;
		var objectName = action.getInfoObjectName();
		if (!objectName) return null;
		return KBInfo.lookupInfoObject(objectName);
	};

	/**
	 * Predicates to specify when a menu item is active
	 */
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
		return getInfoObject(flow.selection[0]) instanceof KBInfo.Flowchart;
	};
	var disabled = function() {
		return false;
	};

	/**
	 * Selecting actions
	 */
	var selectPath = function(flow, backwards, includeRules) {
		if (flow instanceof FlowEditor) {
			flow.snapshot();
			flow = flow.getFlowchart();
		}
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

	var selectEdges = function(flowEditor) {
		flowEditor.snapshot();
		var flow = flowEditor.getFlowchart();
		var nodes = flow.getSelectedNodes();
		flow.setSelection(
			jq$.grep(flow.rules, function(rule) {
				return nodes.contains(rule.sourceNode) && nodes.contains(rule.targetNode);
			}),
			true, false);
	};

	/**
	 * Align actions
	 */
	var align = function(flowEditor, horizontal, useMinMiddleMax) {
		flowEditor.snapshot();
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

	/**
	 * balance & spread actions
	 */
	var spread = function(flowEditor, horizontal, center) {
		var getPos = function(node) {
			return horizontal ? node.getCenterX() : node.getCenterY();
		};
		var getSize = function(node) {
			return horizontal ? node.getWidth() : node.getHeight();
		};
		var getDistance = function(node1, node2) {
			var p1 = getPos(node1), p2 = getPos(node2);
			if (center) return Math.abs(p1 - p2);
			var s1 = getSize(node1) / 2, s2 = getSize(node2) / 2;
			var l1 = p1 - s1, r1 = p1 + s1, l2 = p2 - s2, r2 = p2 + s2;
			return (p1 < p2) ? l2 - r1 : l1 - r2;
		};
		var setPos = function(node, pos) {
			if (horizontal) {
				node.moveTo(pos, node.getTop());
			}
			else {
				node.moveTo(node.getLeft(), pos);
			}
		};
		var nodes = flowEditor.getFlowchart().getSelectedNodes();
		nodes.sort(function(n1, n2) {
			return getPos(n1) - getPos(n2);
		});
		var min = getPos(nodes[0]);
		var space = 0, previous = null;
		jq$.each(nodes, function(index, node) {
			if (previous) space += getDistance(previous, node);
			previous = node;
		});
		var pos = min, delta = space / (nodes.length - 1);
		jq$.each(nodes, function(index, node) {
			var size = getSize(node);
			if (!center && node != nodes[0]) pos += size / 2;
			setPos(node, pos - size / 2);
			pos += delta;
			if (!center) pos += size / 2;
		});
	};

	var spreadDistanceX = function(flowEditor) {
		spread(flowEditor, true, false)
	};
	var spreadMiddleX = function(flowEditor) {
		spread(flowEditor, true, true)
	};

	var spreadDistanceY = function(flowEditor) {
		spread(flowEditor, false, false)
	};
	var spreadMiddleY = function(flowEditor) {
		spread(flowEditor, false, true)
	};

	/**
	 * Cleaning up
	 */
	var arrange = function(flowEditor, horizontal) {
		var getPos = function(node) {
			return horizontal ? node.getCenterX() : node.getCenterY();
		};
		var getOtherPos = function(node) {
			return horizontal ? node.getCenterY() : node.getCenterX();
		};
		var getSize = function(node) {
			return horizontal ? node.getWidth() : node.getHeight();
		};
		var setPos = function(node, pos) {
			var size = getSize(node);
			if (horizontal) {
				node.moveTo(pos - size / 2, node.getTop());
			}
			else {
				node.moveTo(node.getLeft(), pos - size / 2);
			}
		};
		var canGroup = function(node1, node2) {
			// we can group two nodes if they do not intersect
			// or if they intersect, our used position (x or y)
			// is closer than the other position
			if (!node1.intersects(node2)) return true;
			var dOther = Math.abs(getOtherPos(node1) - getOtherPos(node2));
			return Math.abs(getPos(node1) - getPos(node2)) < dOther;
		};
		var groups = [];
		var divideAndConquer = function(nodes) {
			if (nodes.length == 0) return;
			var pivot = nodes[Math.floor(nodes.length / 2)];
			var ppos = getPos(pivot), psize = getSize(pivot);
			var group = [], above = [], below = [];
			jq$.each(nodes, function(index, node) {
				// take the middle one and group all non-overlapping nodes
				// that has roughly same position
				var npos = getPos(node);
				if (node == pivot || (canGroup(pivot, node) && Math.abs(ppos - npos) < psize / 2)) {
					group.push(node);
				}
				else if (ppos > npos) {
					above.push(node);
				}
				else {
					below.push(node);
				}
			});
			// group other ones recursively (and keep groups ordered
			divideAndConquer(above);
			groups.push(group.reverse());
			divideAndConquer(below);
		};

		// sort nodes and split into groups
		var nodes = flowEditor.getFlowchart().getSelectedNodes();
		if (nodes.length == 0) {
			// if no nodes selected, update all nodes (make a copy of the list)
			nodes = jq$.map(flowEditor.getFlowchart().nodes, function(node) {
				return node
			});
		}
		nodes.sort(function(n1, n2) {
			return getPos(n1) - getPos(n2);
		});
		divideAndConquer(nodes);

		// iterate the groups (which are sill ordered ascending),
		// rearrange all items of one group into the same position
		// and spread the positions if required
		var minPos = Number.MIN_VALUE;
		jq$.each(groups, function(index, group) {
			var pos = 0, size = 0;
			jq$.each(group, function(index, node) {
				pos += getPos(node);
				size = Math.max(size, getSize(node));
			});
			pos = pos / group.length; // average pos
			// make sure that we not intersect the previous line
			if (pos < minPos + size / 2) pos = minPos + size / 2;
			// align the nodes and update the minPos for the next group, including some spacing
			jq$.each(group, function(index, node) {
				setPos(node, pos);
			});
			minPos = pos + size / 2 + (horizontal ? 40 : 20);
		});
	};

	var cleanup = function(flowEditor) {
		flowEditor.snapshot();
		arrange(flowEditor, false);
		arrange(flowEditor, true);
	};

	/**
	 * Refactor: inline sub-flow
	 */
	var inline = function(flowEditor) {
		flowEditor.snapshot();
		var flow = flowEditor.getFlowchart();
		var composedNode = flowEditor.getFlowchart().selection[0];
		var flowInfo = getInfoObject(composedNode);
		var getExitName = function(rule) {
			if (!rule.guard) return null;
			var condition = rule.guard.conditionString;
			if (!condition.startsWith("IS_ACTIVE[")) return null;
			var matches = condition.match(new RegExp(
					"\\(" + IdentifierUtils.IDENTIFIER_STRING + "\\)\\]$"));
			if (!matches || matches.length == 0) return null;
			var match = matches[matches.length - 1];
			return IdentifierUtils.unquote(match.substring(1, match.length - 2));
		};

		// first check if we have only named, positive exits
		var missingExit = "";
		jq$.each(composedNode.getOutgoingRules(), function(index, rule) {
			if (!getExitName(rule)) missingExit += "<li>" + rule.getGuard().getDisplayHTML() + "</li>";
		});
		if (missingExit.length > 0) {
			jq$('<div id="dialog-confirm" title="Invalid Exit Conditions">' +
				'<p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>' +
				'The selected composed node uses exit edge(s) that not explicit referencing a single ' +
				'exit node, or is a negation. Before continue, please correct these edges first.</p>' +
				'<p>The following condition(s) are invalid: <ul>' + missingExit + '</ul></p>' +
				'</div>')
				.appendTo(document.body)
				.dialog({
					resizable : false,
					dialogClass : "no-close",
					width : 440,
					modal : true,
					buttons : {
						Close : function() {
							jq$(this).dialog("close");
						}
					}
				});
			return;
		}

		var doIt = function(xml) {
			// TODO: allow multiple composed nodes with the same flowchart, but multiple start nodes
			// now we start to modify the inline flow so that is can replace the calling flow
			// also remove all unreachable nodes (based on the called start node)
			var inlineFlow = Flowchart.createFromXML(null, xml);

			// first we replace start and exit nodes by comment nodes
			var interfaceNodes = {};
			jq$.each(inlineFlow.nodes, function(index, node) {
				var model = node.getNodeModel();
				if (model.start) {
					interfaceNodes["start:" + model.start] = node;
					model.comment = "Entering '" + model.start + "' of\n'" + flowInfo.getName() + "'";
					model.start = null;
				}
				else if (model.exit) {
					interfaceNodes["exit:" + model.exit] = node;
					model.comment = "Exiting '" + model.exit + "' of\n'" + flowInfo.getName() + "'";
					model.exit = null;
				}
			});

			// then select start node and its follow-on-path
			var startName = getAction(composedNode).getValueString();
			var startNode = interfaceNodes["start:" + startName];
			if (!startNode) {
				throw "The requested start node has not been found in the called flowchart. Maybe this flowchart is outdated or the flowchart to be inlined has been changed during your edit, please save/reload and try again.";
			}
			inlineFlow.setSelection(startNode, false, false);
			selectPathAfter(inlineFlow);

			// copy the selection to an xml string and paste it to this flow
			// with a translation that the called start node is at the same location as composed node
			// (not knowing the size of the start node, we can only align the top/left edge)
			var dx = composedNode.getLeft() - startNode.getLeft();
			var dy = composedNode.getTop() - startNode.getTop();
			var toAdd = inlineFlow.getSelectionAsXML();
			var idMapper = flow.pasteXML(toAdd, dx, dy);

			// remove the composed node
			// #1: link its incoming edges to the called start node
			// #2: link its outgoing edges to the exit nodes
			jq$.each(composedNode.getIncomingRules(), function(index, rule) {
				var pastedStart = flow.findNode(idMapper.getID(startNode.getNodeModel().fcid));
				var newRule = new Rule(null, rule.getSourceNode(), rule.getGuard(), pastedStart);
				newRule.routingPoints = rule.routingPoints;
				rule.destroy();
			});
			jq$.each(composedNode.getOutgoingRules(), function(index, rule) {
				var exitName = getExitName(rule);
				var exitNode = interfaceNodes["exit:" + exitName];
				if (!exitName || !exitNode) {
					throw "The requested exit node '" + rule.getGuard().getDisplayHTML() +
						"' has not been found in the called flowchart. " +
						"Maybe this flowchart is outdated or the flowchart to be inlined " +
						"has been changed during your edit, please save/reload and try again.";
				}
				var pastedExit = flow.findNode(idMapper.getID(exitNode.getNodeModel().fcid));
				var newRule = new Rule(null, pastedExit, null, rule.getTargetNode());
				newRule.routingPoints = rule.routingPoints;
				rule.destroy();
			});
			composedNode.destroy();
			flow.focus();
			FlowEditor.autoResize();
		};
		jq$.ajax({
			cache : false,
			url : "action/LoadFlowchartAction",
			data : {KWikiWeb : "default_web", FlowIdentifier : flowInfo.getID() },
			success : function(data, textStatus, jqXHR) {
				try {
					doIt(jqXHR.responseXML);
				}
				catch (e) {
					CCMessage.warn("Refactoring aborted", e);
					flowEditor.undo();
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				CCMessage.warn(
					'AJAX error while requesting flowchart to inline.',
					'Cannot perform flowchart inline. Maybe this flowchart is outdated or the flowchart to be inlined has been changed during your edit, please save/reload and try again.')
			}
		});
	};

	/**
	 * Finally:
	 * define the edit tools and tool sub-menus
	 */
	this.editTools = [
		new FlowEditor.EditToolGroup("Select", [
			new FlowEditor.EditTool("Path Beyond Node", selectPathAfter, oneOrMore, 'path-after-node'),
			new FlowEditor.EditTool("Path To Node", selectPathBefore, oneOrMore, 'path-to-node'),
			FlowEditor.EditTool.SEPARATOR,
			new FlowEditor.EditTool("Edges Between", selectEdges, twoOrMoreNodes, 'edges-between')
		]),
		new FlowEditor.EditToolGroup("Align", [
			new FlowEditor.EditTool("Left", alignLeft, twoOrMoreNodes, 'align-left'),
			new FlowEditor.EditTool("Middle", alignCenter, twoOrMoreNodes, 'align-center'),
			new FlowEditor.EditTool("Right", alignRight, twoOrMoreNodes, 'align-right'),
			FlowEditor.EditTool.SEPARATOR,
			new FlowEditor.EditTool("Top", alignTop, twoOrMoreNodes, 'align-top'),
			new FlowEditor.EditTool("Middle", alignMiddle, twoOrMoreNodes, 'align-middle'),
			new FlowEditor.EditTool("Bottom", alignBottom, twoOrMoreNodes, 'align-bottom'),
			FlowEditor.EditTool.SEPARATOR,
			new FlowEditor.EditTool("Balance Horizontal", spreadDistanceX, twoOrMoreNodes, 'spread-distance-x'),
			new FlowEditor.EditTool("Spread Horizontal", spreadMiddleX, twoOrMoreNodes, 'spread-middle-x'),
			FlowEditor.EditTool.SEPARATOR,
			new FlowEditor.EditTool("Balance Vertical", spreadDistanceY, twoOrMoreNodes, 'spread-distance-y'),
			new FlowEditor.EditTool("Spread Vertical", spreadMiddleY, twoOrMoreNodes, 'spread-middle-y')
		]),
		new FlowEditor.EditTool("Clean Up", cleanup, null, 'clean-up'),
		FlowEditor.EditTool.SEPARATOR,
		new FlowEditor.EditToolGroup("Refactor", [
			new FlowEditor.EditTool("Inline Subflow", inline, oneComposed, 'unfold-subflow'),
			new FlowEditor.EditTool("Extract New Subflow", null, disabled, 'extract-subflow')
		])
	];
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


