//Node event handlers
//register select and edit click events for node
CCEvents.addClassListener('click', 'Node',
	function(event) {

		if (!this.__node) return;
		if (this.__dragging) return;

		var multiple = DiaFluxUtils.isControlKey(event);

		if (!event.isRightClick()) {
			this.__node.select(multiple);
		}

		if (event.isRightClick()) {
			contextMenuFlowchart.close();
			contextMenuRule.close();
			contextMenuNode.show(event, this.__node);
		} else {
			contextMenuNode.close();
			contextMenuFlowchart.close();
			contextMenuRule.close();
		}
	}
);

CCEvents.addClassListener('dblclick', 'Node',
	function(event) {
		//avoids error when dblclick on prototype
		if (this.__node)
			this.__node.edit();
	}
);


Node.prototype.edit = function() {
	if (this.nodeEditor && this.nodeEditor.isVisible()) {
		// wenn editor bereits sichtbar, dann nichts machen
		return;
	}
	// eventuell vorhandene Artefakte (nach cancel) aufraeumen
	this.stopEdit();
	this.nodeEditor = new NodeEditor(
		this.flowchart.getContentPane(),
		this.nodeModel,
			'position:absolute; ' +
//		'position:fixed; opacity: 1.0; filter: alpha(opacity=100);' +
			'left: ' + (this.getLeft()) + 'px; ' +
			'top: ' + (this.getTop()) + 'px;',
		function(nodeEditor) {
			this.setNodeModel(nodeEditor.getNodeModel());
		}.bind(this)
	);
}

Node.prototype.stopEdit = function() {
	if (this.nodeEditor) {
		this.nodeEditor.destroy();
		this.nodeEditor = null;
	}
}

Node.prototype.moveBy = function(dLeft, dTop, noRouting) {
	this.moveTo(this.getLeft() + dLeft, this.getTop() + dTop, noRouting);
}


//overrides empty implementation node.js
Node.prototype.destroyDraggable = function() {
	Droppables.remove(this.nodeModel.id);
	this.draggable.destroy();
	this.draggable = null;

};


//overrides empty implementation node.js
Node.prototype.createDraggable = function() {

	if (!this.snapManager) {
		this.snapManager = new SnapManager(this.flowchart);
	}

	this.draggable = new Draggable(this.getDOM(), {
		ghosting : true,
		starteffect : function(element) {
			element.__dragging = true;
		},
		endeffect : function(element, left, top) {
			window.setTimeout(function() {
				element.__dragging = false;
			}, 100);
			jq$(element).find('.badge').remove();
		},
		onStart : function(draggable, event) {
			draggable.__snapManager.initializeSnapsForNode(draggable.__node);
			draggable.__node.__draggable = draggable;
			var node = draggable.__node;
			// if we drag a node of a multiple selection drag all nodes
			if (node.flowchart.isSelected(node)) {
				draggable.draggedNodes = [];
				node.flowchart.selection.each(function(item) {
					if (item.nodeModel) {
						draggable.draggedNodes.push(item);
					}
				});
			}
			else {
				draggable.draggedNodes = [node];
			}
			// add badge for multiple drag
			var count = draggable.draggedNodes.length;
			if (count > 1) {
				var element = jq$(draggable.__node.getDOM());
				element.append("<div class='badge'>" + count + "</div>");
			}
		},
		onEnd : function(draggable, event) {
			var node = draggable.__node;
			node.flowchart.setSelection(draggable.draggedNodes);
			EditorInstance.withUndo("Drag Nodes", function() {
				draggable.__snapManager.showSnapLines(null, null);
				// check if the node has been deleted
				if (!node.getDOM()) return;
				// if we moved a node of a multiple selection move all nodes
				var dx = node.getDOM().offsetLeft - node.getLeft();
				var dy = node.getDOM().offsetTop - node.getTop();
				for (var i = 0; i < draggable.draggedNodes.length; i++) {
					node = draggable.draggedNodes[i];
					node.moveTo(node.getLeft() + dx, node.getTop() + dy, true);
				}
				node.flowchart.router.rerouteNodes(draggable.draggedNodes);
				node.flowchart.setSelection(draggable.draggedNodes);
			});
		},
		snap : function(x, y, draggable) {
			return draggable.__snapManager.snapIt(x, y);
		},
		scroll : null // $("contents")
	});
	this.draggable.__snapManager = this.snapManager;
	this.draggable.__node = this;

	// make this node a possible target for the arrow tool
	Droppables.add(this.getDOM(), {
		accept : 'ArrowTool',
		hoverclass : 'Node_hover',
		onDrop : function(draggable, droppable, event) {
			draggable.__arrowTool.createRule(droppable.__node);
		}
	});
}


Node.prototype.moveTo = function(left, top, noRouting) {
	this.nodeModel.position.left = left;
	this.nodeModel.position.top = top;
	if (this.dom != null) {
		this.dom.style.left = left + "px";
		this.dom.style.top = top + "px";
	}
	if (this.isVisible() && this.arrowTool != null && this.arrowTool.isVisible()) {
		this.arrowTool.setVisible(false);
		this.arrowTool.setVisible(true);
	}
	if (!noRouting) {
		// TODO: + alle mit Regeln verbundenen Knoten!!!
		this.flowchart.router.rerouteNodes([this]);
	}
}


Node.prototype.toXML = function(dx, dy) {
	if (!dx) dx = 0;
	if (!dy) dy = 0;
	var xml = '\t<node fcid="' + this.nodeModel.fcid + '">\n';
	xml += '\t\t<position left="' + (this.getLeft() + dx) + '" top="' + (this.getTop() + dy) + '"></position>\n';
	if (this.nodeModel.start) {
		xml += '\t\t<start>' + this.nodeModel.start.escapeXML() + '</start>\n';
	}
	else if (this.nodeModel.exit) {
		xml += '\t\t<exit>' + this.nodeModel.exit.escapeXML() + '</exit>\n';
	}
	else if (this.nodeModel.comment) {
		xml += '\t\t<comment>' + this.nodeModel.comment.escapeXML() + '</comment>\n';
	}
	else if (this.nodeModel.snapshot) {
		xml += '\t\t<snapshot>' + this.nodeModel.snapshot.escapeXML() + '</snapshot>\n';
	}
	else if (this.nodeModel.action) {
		var action = this.nodeModel.action;

		if (action.markup == 'NOP') {
			xml += '\t\t<decision>' +
				(action.expression ? '<![CDATA[' + action.expression + ']]>' : '') +
				'</decision>\n';

		} else {
			xml += '\t\t<action markup="' + action.markup + '">' +
				(action.expression ? '<![CDATA[' + action.expression + ']]>' : '') +
				'</action>\n';
		}
	}
	xml += '\t</node>\n';
	return xml;
}

function NodeEditor(parent, nodeModel, style, onSuccess) {
	this.parent = $(parent);
	this.dom = null;
	this.style = style;
	this.nodeModel = nodeModel;
	this.onSuccess = onSuccess;
	this.actionEditor = null;
	this.tabPanes = null;
	this.tabItems = null;

	this.setVisible(true);
	this.selectTab(nodeModel.start ? 1 : nodeModel.exit ? 2 : nodeModel.comment ? 3 : nodeModel.snapshot ? 4 : 0);
}

// register key listener to handle ok / cancel hot keys 
// (only if not handled by contained components!!!)
CCEvents.addClassListener('keydown', 'NodeEditor',
	function(event) {
		this.__nodeEditor.handleKeyEvent(event);
	}
);

// stop custom mouse click events on this pane
CCEvents.addClassListener('click', 'NodeEditor', function(event) {
	event.defaultHandler();
});
CCEvents.addClassListener('dblclick', 'NodeEditor', function(event) {
	event.defaultHandler();
});
CCEvents.addClassListener('mousedown', 'NodeEditor', function(event) {
	event.defaultHandler();
});
CCEvents.addClassListener('mouseup', 'NodeEditor', function(event) {
	event.defaultHandler();
});


NodeEditor.prototype.getDOM = function() {
	return this.dom;
}

NodeEditor.prototype.isVisible = function() {
	return (this.dom != null);
}

NodeEditor.prototype.getNodeModel = function() {
	return this.nodeModel;
}


NodeEditor.prototype.handleOk = function() {
	EditorInstance.withUndo("Edit Node", function() {
		// update node model before closing
		this.nodeModel = {
			fcid : this.nodeModel.fcid,
			position : this.nodeModel.position
		};
		if (this.tabItems[0].className == 'actionTab_selected') {
			// vor dem ok noch sicherstellen, dass
			// das Selektionsfeld uebernommen wurde (blur() hilft hier)
			var select = this.dom.select;
			var action = this.actionEditor.getAction();
			this.nodeModel.action = {
				markup : action ? action.getMarkup() : 'NOP',
				expression : action ? action.getExpression() : this.actionEditor.objectSelect.getValue()
			};
		}
		else if (this.tabItems[1].className == 'startTab_selected') {
			var value = this.tabPanes[1].childNodes[2].value;
			this.nodeModel.start = value || " ";
		}
		else if (this.tabItems[2].className == 'exitTab_selected') {
			var value = this.tabPanes[2].childNodes[2].value;
			this.nodeModel.exit = value || " ";
		}
		else if (this.tabItems[3].className == 'commentTab_selected') {
			var value = this.tabPanes[3].childNodes[2].value;
			this.nodeModel.comment = value || " ";
		}
		else if (this.tabItems[4].className == 'snapshotTab_selected') {
			var value = this.tabPanes[4].childNodes[2].value;
			this.nodeModel.snapshot = value || " ";
		}
		else {
			throw "invalid/unexpected tab pane layout";
		}
		this.setVisible(false);
		FlowEditor.checkFocus();
		if (this.onSuccess) {
			this.onSuccess(this);
		}
	}.bind(this));
};

NodeEditor.prototype.handleCancel = function() {
	this.setVisible(false);
	FlowEditor.checkFocus();
}

NodeEditor.prototype.handleKeyEvent = function(e) {
	// if not, we can use the keys to commit/cancel the modal dialog
	switch (e.keyCode) {
		case Event.KEY_ESC:
			this.handleCancel();
			return;
		case Event.KEY_RETURN:
			this.handleOk();
			return;
	}
	//default handling for cursor events
	e.defaultHandler();
}

NodeEditor.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		this.actionEditor = new ActionEditor(
			this.tabPanes[0],
			(this.nodeModel.action) ? new Action(this.nodeModel.action.markup, this.nodeModel.action.expression) : null);
		//this.keyFx = this.handleKeyEvent.bindAsEventListener(this);
		//document.observe('keydown', this.keyFx);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		//document.stopObserving('keydown',  this.keyFx);
		this.keyFx = null;
		this.actionEditor.destroy();
		this.actionEditor = null;
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

NodeEditor.prototype.selectTab = function(index) {
	if (!this.tabItems) return;
	if (!this.tabPanes) return;
	for (var i = 0; i < this.tabItems.length; i++) {
		var itemBaseClass = this.tabItems[i].className.replace(/_.*$/, '');
		if (i == index) {
			this.tabItems[i].className = itemBaseClass + '_selected';
			this.tabPanes[i].show();
			if (i == 0) {
				var selectors = this.tabPanes[i].select('.ActionEditor');
				if (selectors && selectors.length >= 1) {
					selectors[0].__ActionEditor.focus();
				}
			}
			else {
				var inputs = this.tabPanes[i].select('input');
				if (inputs && inputs.length >= 1) {
					inputs[0].focus();
					inputs[0].select();
					continue;
				}

				inputs = this.tabPanes[i].select('textarea');
				if (inputs && inputs.length >= 1) {
					inputs[0].focus();
					inputs[0].select();
				}
			}
		}
		else {
			this.tabItems[i].className = itemBaseClass;
			this.tabPanes[i].hide();
		}
	}
}

NodeEditor.prototype.render = function() {
	this.tabItems = [
		Builder.node('span', {className : 'actionTab', onclick : 'this.parentNode.parentNode.__nodeEditor.selectTab(0);'}),
		Builder.node('span', {className : 'startTab', onclick : 'this.parentNode.parentNode.__nodeEditor.selectTab(1);'}),
		Builder.node('span', {className : 'exitTab', onclick : 'this.parentNode.parentNode.__nodeEditor.selectTab(2);'}),
		Builder.node('span', {className : 'commentTab', onclick : 'this.parentNode.parentNode.__nodeEditor.selectTab(3);'}),
		Builder.node('span', {className : 'snapshotTab', onclick : 'this.parentNode.parentNode.__nodeEditor.selectTab(4);'})];
	this.tabPanes = [
		this.renderActionPane(),
		this.renderStartPane(),
		this.renderExitPane(),
		this.renderCommentPane(),
		this.renderSnapshotPane()];

	var dom = Builder.node('div', {
			className : 'NodeEditor',
			style : (this.style) ? this.style : ''
		},
		[
			Builder.node('div', {className : 'background'}),
			Builder.node('div', {className : 'paneGroup'}, this.tabPanes),
			Builder.node('div', {className : 'buttonGroup'}, [
				Builder.node('button', {className : 'ok', onclick : 'this.parentNode.parentNode.__nodeEditor.handleOk();'}, ['Ok']),
				Builder.node('button', {className : 'cancel', onclick : 'this.parentNode.parentNode.__nodeEditor.handleCancel();'}, ['Cancel'])
			])
		]);
	dom.__nodeEditor = this;
	return dom;
}

NodeEditor.prototype.renderActionPane = function() {
	var dom = Builder.node('div', {
			className : 'actionPane',
			style : 'display: none;'
		},
		[
			"Use object:"
		]);
	return dom;
}

NodeEditor.prototype.renderStartPane = function() {
	var dom = Builder.node('div', {
			className : 'startPane',
			style : 'display: none;'
		},
		[
			'Start node:',
			Builder.node('br'),
			Builder.node('input', {
				className : 'value',
				type : 'text',
				value : this.nodeModel.start ? this.nodeModel.start : ''
			})
		]);
	return dom;
}

NodeEditor.prototype.renderCommentPane = function() {
	var dom = Builder.node('div', {
			className : 'commentPane',
			style : 'display: none;'
		},
		[
			'Comment:',
			Builder.node('br'),
			Builder.node('textarea',
				this.nodeModel.comment ? this.nodeModel.comment : ''
			)
		]);
	return dom;
}

NodeEditor.prototype.renderExitPane = function() {
	var dom = Builder.node('div', {
			className : 'exitPane',
			style : 'display: none;'
		},
		[
			'Exit node:',
			Builder.node('br'),
			Builder.node('input', {
				className : 'value',
				type : 'text',
				value : this.nodeModel.exit ? this.nodeModel.exit : ''
			})
		]);
	return dom;
}

NodeEditor.prototype.renderSnapshotPane = function() {
	var dom = Builder.node('div', {
			className : 'snapshotPane',
			style : 'display: none;'
		},
		[
			'Snapshot:',
			Builder.node('br'),
			Builder.node('input', {
				className : 'value',
				type : 'text',
				value : this.nodeModel.snapshot ? this.nodeModel.snapshot : ''
			})
		]);
	return dom;
}

NodeEditor.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	this.setVisible(false);
}


/**
 * ArrowTool
 * creates a new arrow tool for the node.
 * @param {Node} node
 */
function ArrowTool(node) {
	this.node = node;
	this.flowchart = node.flowchart;
	this.dom = null;
	this.draggable = null;
}


ArrowTool.prototype.getDOM = function() {
	return this.dom;
}

ArrowTool.prototype.isVisible = function() {
	return (this.dom != null);
}

ArrowTool.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.flowchart.getContentPane().appendChild(this.dom);
		this.draggable = this.createDraggable();
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.showLine(null, null);
		this.draggable.destroy();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
		this.draggable = null;
	}
}

ArrowTool.prototype.destroy = function() {
	this.setVisible(false);
}

ArrowTool.prototype.render = function() {
	var dom = Builder.node('div', {
		id : this.node.nodeModel.fcid + '_arrow_tool',
		className : 'ArrowTool ArrowTool_right',
		style : "left: " + (this.node.getLeft() + this.node.getWidth() - 13) + "px; " +
			"top:" + (this.node.getTop() + this.node.getHeight() - 13) + "px;"});
	dom.__arrowTool = this;
	return dom;
}

ArrowTool.prototype.createDraggable = function() {
	var newDrag = new Draggable(this.getDOM(), {
		ghosting : false,
		revert : true,
		starteffect : null,
		endeffect : null,
		onStart : function(draggable, event) {
		},
		onEnd : function(draggable, event) {
			draggable.__arrowTool.showLine(null, null);
		},
		snap : function(x, y, draggable) {
			draggable.__arrowTool.showLine(x, y);
			return [x, y];
		},
		scroll : this.node.flowchart.fcid
	});
	newDrag.__arrowTool = this;
	return newDrag;
}

ArrowTool.prototype.createRule = function(targetNode) {
	if (this.node != targetNode) {
		var rule = new Rule(
			/*this.flowchart.createID('rule')*/ null,
			this.node, null, targetNode);
		rule.select();
	}
}

ArrowTool.prototype.showLine = function(x, y) {
	if (this.lineDOM) {
		this.flowchart.getContentPane().removeChild(this.lineDOM);
		this.lineDOM = null;
	}
	if (x && y && this.dom) {
		var x1 = this.node.getCenterX();
		var y1 = this.node.getCenterY();
		this.lineDOM = DiaFluxUtils.createDottedLine(x1, y1, x + 13, y + 13, 2, 'red', 5, 100);
		this.flowchart.getContentPane().appendChild(this.lineDOM);
	}
}

// -----
// handle dragging
// -----

function SnapManager(flowchart) {
	this.flowchart = flowchart;
	this.hSnaps = [];
	this.vSnaps = [];
	this.snapDistance = 5;
}

SnapManager.prototype.initializeSnapsForRoutingPoint = function(routingPoint) {
	// empty existing ones
	this.hSnaps = [];
	this.vSnaps = [];
	var x = routingPoint.getX();
	var y = routingPoint.getY();
	var offsetX = 7;
	var offsetY = 7;

	// add snaps for itself (to have the highest priority)
	this.hSnaps.push(new Snap(offsetX, x));
	this.vSnaps.push(new Snap(offsetY, y));

	// add snaps for other routing points
	for (var i = 0; i < routingPoint.rule.routingPoints.length; i++) {
		var other = routingPoint.rule.routingPoints[i];
		if (routingPoint == other) continue;
		this.hSnaps.push(new Snap(offsetX, other.getX()));
		this.vSnaps.push(new Snap(offsetY, other.getY()));
	}

	// add snaps for connected node centers to this node's center
	var n1 = routingPoint.rule.getSourceNode();
	var n2 = routingPoint.rule.getTargetNode();
	this.hSnaps.push(new Snap(offsetX, n1.getCenterX()));
	this.vSnaps.push(new Snap(offsetY, n1.getCenterY()));
	this.hSnaps.push(new Snap(offsetX, n2.getCenterX()));
	this.vSnaps.push(new Snap(offsetY, n2.getCenterY()));
}

SnapManager.prototype.initializeSnapsForNode = function(node) {
	// empty existing ones
	this.hSnaps = [];
	this.vSnaps = [];
	var x = node.getCenterX();
	var y = node.getCenterY();
	var offsetX = node.getWidth() / 2;
	var offsetY = node.getHeight() / 2;

	// add snaps for itself (to have the highest priority
	this.hSnaps.push(new Snap(offsetX, x));
	this.vSnaps.push(new Snap(offsetY, y));

	// add snaps for all rules to have a single straight line
	var rules = this.flowchart.findRulesForNode(node);
	for (var i = 0; i < rules.length; i++) {
		var rule = rules[i];
		var myAnchor = rule.getSourceAnchor();
		var otherAnchor = rule.getTargetAnchor();
		if (node == otherAnchor.node) {
			otherAnchor = rule.getSourceAnchor();
			myAnchor = rule.getTargetAnchor();
		}
		if (otherAnchor.type == 'top' || otherAnchor.type == 'bottom') {
			// horizontal snap
			this.hSnaps.push(new Snap(myAnchor.x - node.getLeft(), otherAnchor.x));
		}
		else {
			// vertical snap
			this.vSnaps.push(new Snap(myAnchor.y - node.getTop(), otherAnchor.y));
		}
	}

	// add snaps for all node centers to this node's center
	for (var i = 0; i < this.flowchart.nodes.length; i++) {
		var node = this.flowchart.nodes[i];
		this.hSnaps.push(new Snap(offsetX, node.getCenterX()));
		this.vSnaps.push(new Snap(offsetY, node.getCenterY()));
	}
}

SnapManager.prototype.snapIt = function(x, y) {
	// snap to the middle of the object
	var hSnap = this.findSnap(this.hSnaps, x);
	var vSnap = this.findSnap(this.vSnaps, y);
	this.showSnapLines(hSnap, vSnap);
	return [
		hSnap ? hSnap.getNodePosition() : x,
		vSnap ? vSnap.getNodePosition() : y];

}

SnapManager.prototype.findSnap = function(snaps, position) {
	// iterate to find optimal snap with less than 5 pixels (= snapDistance pixels) away
	var bestDist = this.snapDistance + 1;
	var bestSnap = null;
	for (var i = 0; i < snaps.length; i++) {
		var snap = snaps[i];
		var d = snap.getDistance(position);
		if (d < bestDist) {
			bestDist = d;
			bestSnap = snap;
		}
	}
	return bestSnap;
}

SnapManager.prototype.showSnapLines = function(hSnap, vSnap) {
	var hid = "dragHelpLine_" + this.flowchart.fcid + "_h";
	var vid = "dragHelpLine_" + this.flowchart.fcid + "_v";
	// remove horizontal line if available and changed
	if (this.vSnapLinePos && (vSnap == null || this.vSnapLinePos != vSnap.snapPosition)) {
		var div = document.getElementById(hid);
		this.flowchart.getContentPane().removeChild(div);
		this.vSnapLinePos = null;
	}
	// remove vertical line if available and changed
	if (this.hSnapLinePos && (hSnap == null || this.hSnapLinePos != hSnap.snapPosition)) {
		var div = document.getElementById(vid);
		this.flowchart.getContentPane().removeChild(div);
		this.hSnapLinePos = null;
	}

	var size = this.flowchart.getContentSize();
	// add horizontal line if desired
	if (vSnap && this.vSnapLinePos != vSnap.snapPosition) {
		var line = Builder.node('div', {
			id : hid,
			className : 'h_snapline',
			style : "left: 0px; top: " + vSnap.snapPosition + "px; width: " + (size[0] - 2) + "px; height: 1px;"
		});
		this.flowchart.getContentPane().appendChild(line);
		this.vSnapLinePos = vSnap.snapPosition;
	}
	// add vertical line if desired
	if (hSnap && this.hSnapLinePos != hSnap.snapPosition) {
		var line = Builder.node('div', {
			id : vid,
			className : 'v_snapline',
			style : "left: " + hSnap.snapPosition + "px; top: 0px; width: 1px; height: " + (size[1] - 2) + "px"
		});
		this.flowchart.getContentPane().appendChild(line);
		this.hSnapLinePos = hSnap.snapPosition;
	}
}


function Snap(offset, snapPosition) {
	this.offset = Math.floor(offset);
	this.snapPosition = Math.floor(snapPosition);
}

Snap.prototype.getDistance = function(position) {
	return Math.abs((position + this.offset) - this.snapPosition);
}

Snap.prototype.getNodePosition = function() {
	return this.snapPosition - this.offset;
}
