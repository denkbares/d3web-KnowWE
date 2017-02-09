//Rule event handlers
//register click-listener for that class
CCEvents.addClassListener('click', 'ArrowTool', function (event) {/*NOP, but avoid bubbling*/
});
CCEvents.addClassListener('click', 'RoutingTool', function (event) {/*NOP, but avoid bubbling*/
});
CCEvents.addClassListener('click', 'RoutingToolGray', function (event) {/*NOP, but avoid bubbling*/
});

CCEvents.addClassListener('dblclick', 'RoutingTool', function (event) {
	this.__routingTool.routingPoint.destroy();
});


//register select click events for rule
CCEvents.addClassListener('click', 'Rule',
	function (event) {
		if (this.__rule) this.__rule.select(DiaFluxUtils.isControlKey(event));

		if (typeof contextMenuNode != "undefined") {
			if (event.isRightClick()) {
				contextMenuFlowchart.close();
				contextMenuNode.close();
				contextMenuRule.show(event, this.__rule);
			} else {
				contextMenuNode.close();
				contextMenuFlowchart.close();
				contextMenuRule.close();

			}
		}
	}
);

//overrides empty implementation in rule.js
Rule.prototype.createDraggable = function () {
	var newDrag = new Draggable(this.getDOM(), {
		ghosting: false,
		revert: true,
		starteffect: null,
		endeffect: null
	});
	newDrag.__rule = this;
	this.draggable = newDrag;
}

//overrides empty implementation in rule.js
Rule.prototype.destroyDraggable = function () {
	this.draggable.destroy();
	this.draggable = null;
}


Rule.prototype.toXML = function (dx, dy) {
	var xml = '\t<edge' +
		(this.fcid ? ' fcid="' + this.fcid + '"' : '') +
		'>\n';
	xml += '\t\t<origin>' + this.sourceNode.getNodeModel().fcid + '</origin>\n';
	xml += '\t\t<target>' + this.targetNode.getNodeModel().fcid + '</target>\n';
	if (this.guard && this.guard.getMarkup() != 'NOP') {
		if (DiaFluxUtils.isString(this.guard)) {
			xml += '\t\t<guard><![CDATA[' + this.guard + ']]></guard>\n';
		}
		else {
			xml += '\t\t<guard markup="' + this.guard.getMarkup() + '"><![CDATA[' +
				this.guard.toXML(this) +
				']]></guard>\n';
		}
	}
	for (var i = 0; i < this.routingPoints.length; i++) {
		var routingPoint = this.routingPoints[i];
		xml += '\t\t<routingPoint x="' + routingPoint.percentX + '" y="' + routingPoint.percentY + '" />\n';
	}
	xml += '\t</edge>\n';
	return xml;
}


/**
 * RuleArrowTool
 * creates a new arrow tool for a rule.
 * @param {Node} node
 */
function RuleArrowTool(rule) {
	this.rule = rule;
	this.flowchart = this.rule.flowchart;
	this.dom = null;
	this.draggable = null;
}

RuleArrowTool.prototype.getDOM = function () {
	return this.dom;
}

RuleArrowTool.prototype.isVisible = function () {
	return (this.dom != null);
}

RuleArrowTool.prototype.setVisible = function (visible) {
	if (!this.isVisible() && visible) {
		this.dom = this.render();
		this.flowchart.getContentPane().appendChild(this.dom);
		this.draggable = this.createDraggable();
	}
	else if (this.isVisible() && !visible) {
		this.showLine(null, null);
		this.draggable.destroy();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
		this.draggable = null;
	}
}

RuleArrowTool.prototype.destroy = function () {
	this.setVisible(false);
}

RuleArrowTool.prototype.render = function () {
	var x, y, dom, arrow;
	x = this.rule.coordinates[this.rule.coordinates.length - 1][0] - 13;
	y = this.rule.coordinates[this.rule.coordinates.length - 1][1] - 13;

	var ruleDOM = this.rule.getDOM();
	var arrowDir = ruleDOM.select('.arrow')[0].className.match(/_(\w+)/)[1];

	dom = Builder.node('div', {
		id: this.rule.fcid + '_arrow_tool',
		className: 'ArrowTool ArrowTool_' + arrowDir,
		style: "left: " + x + "px; top:" + y + "px;"
	});
	dom.__arrowTool = this;
	return dom;
}

RuleArrowTool.prototype.createDraggable = function () {
	var newDrag = new Draggable(this.getDOM(), {
		ghosting: false,
		revert: true,
		starteffect: null,
		endeffect: null,
		onStart: function (draggable, event) {
		},
		onEnd: function (draggable, event) {
			draggable.__arrowTool.showLine(null, null);
		},
		snap: function (x, y, draggable) {
			draggable.__arrowTool.showLine(x, y);
			return [x, y];
		},
		scroll: this.flowchart.fcid
	});
	newDrag.__arrowTool = this;
	return newDrag;
}

RuleArrowTool.prototype.createRule = function (targetNode) {
	EditorInstance.withUndo("Relocate Edge", function () {
		if (this.rule.getTargetNode() != targetNode && this.rule.getSourceNode() != targetNode) {
			var rule = new Rule(this.rule.fcid, this.rule.getSourceNode(), this.rule.getGuard(), targetNode);
			this.rule.destroy();
			rule.select();
		}
	}.bind(this));
};

RuleArrowTool.prototype.showLine = function (x, y) {
	if (this.lineDOM) {
		this.flowchart.getContentPane().removeChild(this.lineDOM);
		this.lineDOM = null;
	}
	if (x && y && this.dom) {
		var x1 = this.rule.getSourceNode().getCenterX();
		var y1 = this.rule.getSourceNode().getCenterY();
		this.lineDOM = DiaFluxUtils.createDottedLine(x1, y1, x + 13, y + 13, 2, 'red', 5, 100);
		this.flowchart.getContentPane().appendChild(this.lineDOM);
	}
}


/**
 * RoutingPointTool
 * creates a new arrow tool for a rule.
 * @param {RoutingPoint} routingPoint the routing point to be added
 * @param {int} insertIndex the index where to add this routingPoint when it is dragged (created)
 *                or 'undefined' if the routing point is an already added (created) routing point
 */
function RoutingTool(routingPoint, insertIndex) {
	this.routingPoint = routingPoint;
	this.insertIndex = insertIndex;
	this.flowchart = this.routingPoint.rule.flowchart;
	this.dom = null;
	this.draggable = null;
}

RoutingTool.prototype.getDOM = function () {
	return this.dom;
}

RoutingTool.prototype.isInsert = function () {
	return typeof this.insertIndex === 'number';
}

RoutingTool.prototype.isVisible = function () {
	return (this.dom != null);
}

RoutingTool.prototype.setVisible = function (visible) {
	if (!this.isVisible() && visible) {
		this.dom = this.render();
		this.flowchart.getContentPane().appendChild(this.dom);
		this.draggable = this.createDraggable();
	}
	else if (this.isVisible() && !visible) {
		this.showLine(null, null);
		this.draggable.destroy();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
		this.draggable = null;
	}
}

RoutingTool.prototype.destroy = function () {
	this.setVisible(false);
}

RoutingTool.prototype.render = function () {
	var x = this.routingPoint.getX() - 7;
	var y = this.routingPoint.getY() - 7;
	var className = this.isInsert() ? 'RoutingToolGray' : 'RoutingTool';

	this.dom = Builder.node('div', {
		id: this.routingPoint.rule.fcid + '_routing_tool', // TODO: remove id if possible or make unique
		className: className,
		style: "left: " + x + "px; top:" + y + "px;"
	});
	this.dom.__routingTool = this;
	return this.dom;
}

RoutingTool.prototype.createDraggable = function () {

	if (!this.snapManager) {
		this.snapManager = new SnapManager(this.flowchart);
	}

	var lastX = this.routingPoint.getX();
	var lastY = this.routingPoint.getY();
	var newDrag = new Draggable(this.getDOM(), {
		ghosting: false,
		revert: true,
		starteffect: null,
		endeffect: null,
		onStart: function (draggable, event) {
			draggable.__snapManager.initializeSnapsForRoutingPoint(newDrag.__routingTool.routingPoint);
		},
		onEnd: function (draggable, event) {
			EditorInstance.withUndo("Edge Routing", function () {
				draggable.__snapManager.showSnapLines(null, null);
				draggable.__routingTool.showLine(null, null);
				var routingPoint = draggable.__routingTool.routingPoint;
				var rule = routingPoint.rule;
				routingPoint.setCoordinates(lastX, lastY);
				if (draggable.__routingTool.isInsert()) {
					rule.routingPoints.splice(draggable.__routingTool.insertIndex, 0, routingPoint);
				}
				var flowchart = routingPoint.rule.flowchart;
				flowchart.router.rerouteNodes([rule.getSourceNode(), rule.getTargetNode()]);
				rule.select(false, true);
			});
		},
		snap: function (x, y, draggable) {
			draggable.__routingTool.showLine(x, y);
			// TODO: handle snap to source / target / other routing points
			var coords = draggable.__snapManager.snapIt(x, y);
			lastX = coords[0] + 7;
			lastY = coords[1] + 7;
			return coords;
		},
		scroll: this.flowchart.fcid
	});
	newDrag.__snapManager = this.snapManager;
	newDrag.__routingTool = this;
	return newDrag;
}

RoutingTool.prototype.showLine = function (x, y) {
	if (this.lineDOM1) {
		this.flowchart.getContentPane().removeChild(this.lineDOM1);
		this.lineDOM1 = null;
	}
	if (this.lineDOM2) {
		this.flowchart.getContentPane().removeChild(this.lineDOM2);
		this.lineDOM2 = null;
	}
	if (x && y && this.dom) {
		var rule = this.routingPoint.rule;
		var x1, y1, x2, y2;
		var x1 = rule.getSourceNode().getCenterX();
		var y1 = rule.getSourceNode().getCenterY();
		var x2 = rule.getTargetNode().getCenterX();
		var y2 = rule.getTargetNode().getCenterY();

		// handle multiple routing points for dotted lines
		var routingPoints = this.routingPoint.rule.routingPoints;
		var prevIndex, nextIndex;
		if (this.isInsert()) {
			prevIndex = this.insertIndex - 1;
			nextIndex = this.insertIndex;
		}
		else {
			for (var i = 0; i < routingPoints.length; i++) {
				if (routingPoints[i] == this.routingPoint) {
					prevIndex = i - 1;
					nextIndex = i + 1;
					break;
				}
			}
		}
		// we have a routing point before this one
		if (prevIndex >= 0) {
			x1 = routingPoints[prevIndex].getX();
			y1 = routingPoints[prevIndex].getY();
		}
		// we have a routing point after this one
		if (nextIndex < routingPoints.length) {
			x2 = routingPoints[nextIndex].getX();
			y2 = routingPoints[nextIndex].getY();
		}

		this.lineDOM1 = DiaFluxUtils.createDottedLine(x1, y1, x + 7, y + 7, 2, 'red', 5, 100);
		this.lineDOM2 = DiaFluxUtils.createDottedLine(x2, y2, x + 7, y + 7, 2, 'red', 5, 100);
		this.flowchart.getContentPane().appendChild(this.lineDOM1);
		this.flowchart.getContentPane().appendChild(this.lineDOM2);
	}
}


