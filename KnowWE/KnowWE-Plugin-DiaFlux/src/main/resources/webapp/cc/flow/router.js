function Router(flowchart) {
	this.flowchart = flowchart;
	this.delayReroute = false;
	this.allowSingleBendRoute = false;
	this.allowGuardOptimization = false;
}

Router.prototype.withDelayedReroute = function(fun) {
	if (this.delayReroute) return fun();
	try {
		this.delayReroute = true;
		return fun();
	}
	finally {
		this.delayReroute = false;
		this.rerouteAll();
	}
};

Router.prototype.rerouteNodes = function(nodes) {
	if (this.delayReroute) return;
	this.rerouteAll();
};

Router.prototype._createTempRoutingBox = function(rule) {
	var n1 = rule.getSourceNode();
	var n2 = rule.getTargetNode();
	var l1 = n1.getLeft(), t1 = n1.getTop(), w1 = n1.getWidth(), h1 = n1.getHeight(), r1 = l1 + w1, b1 = t1 + h1;
	var l2 = n2.getLeft(), t2 = n2.getTop(), w2 = n2.getWidth(), h2 = n2.getHeight(), r2 = l2 + w2, b2 = t2 + h2;
	var cx1 = n1.getCenterX(), cx2 = n2.getCenterX(), cy1 = n1.getCenterY(), cy2 = n2.getCenterY();
	// if they are overlapping either horizontal or vertical, do nothing
	if (l1 <= r2 && r1 >= l2) return null;
	if (t1 <= b2 && b1 >= t2) return null;
	// check if horizontal left corridor is free
	var flow = rule.flowchart;
	var areaNodes = [].concat(
		flow.getIntersectNodes(Math.min(l1, l2), t1, Math.min(r1, r2), b1),
		flow.getIntersectNodes(l2, Math.min(t1, t2), r2, Math.max(b1, b2))
	);
	var intersects = function(x1, y1, x2, y2) {
		for (var i = 0; i < areaNodes.length; i++) {
			var n = areaNodes[i];
			if (n == n1 || n == n2) continue;
			if (n.intersects(x1, y1, x2, y2)) return true;
		}
		return false;
	};
	var hFree = function(x1, y1, x2, y2) {
		return !intersects(x1, y1 + 2, x2, y1 + 2) || !intersects(x1, y2 - 2, x2, y2 - 2)
			|| !intersects(x1, (y1 + y2) / 2, x2, (y1 + y2) / 2);
	};
	var vFree = function(x1, y1, x2, y2) {
		return !intersects(x1 + 2, y1, x1 + 2, y2) || !intersects(x1 - 2, y1, x2 - 2, y2)
			|| !intersects((x1 + x2) / 2, y1, (x1 + x2) / 2, y2);
	};
	if (hFree(cx1, t1, cx2, b1) && vFree(l2, cy1, r2, cy2)) {
		return RoutingBox.createBendArea(rule, l2, t1, r2, b1);
	}
	return null;
};

Router.prototype.rerouteAll = function() {
	if (this.delayReroute) return;

	// create all boxes and all lines in between
	this.boxes = [];
	this.lines = [];
	for (var i = 0; i < this.flowchart.nodes.length; i++) {
		var node = this.flowchart.nodes[i];
		var box = RoutingBox.createFromNode(node);
		node._boxForNode = box;
		this.boxes.push(box);
	}
	for (var i = 0; i < this.flowchart.rules.length; i++) {
		var rule = this.flowchart.rules[i];
		rule._linesForRule = [];
		var previousBox = rule.getSourceNode()._boxForNode;
		var routingPoints = rule.routingPoints;
		if (this.allowSingleBendRoute && rule.routingPoints.length == 0) {
			// we have no intermediate routing points, so check if we want to create a virtual one
			// to have an edge bended only once (instead of twice)
			var tempBox = this._createTempRoutingBox(rule);
			if (tempBox) {
				this.boxes.push(tempBox);
				// create line
				var line = new RoutingLine(rule, previousBox, tempBox);
				this.lines.push(line);
				rule._linesForRule.push(line);
				previousBox = tempBox;
			}
		}
		else {
			// we have intermediate routing points, so create boxes and lines
			for (var k = 0; k < routingPoints.length; k++) {
				// create box
				var nextBox = RoutingBox.createFromRoutingPoint(routingPoints[k]);
				this.boxes.push(nextBox);
				// create line
				var line = new RoutingLine(rule, previousBox, nextBox);
				this.lines.push(line);
				rule._linesForRule.push(line);
				previousBox = nextBox;
			}
		}
		// and finally create line to target node
		var line = new RoutingLine(rule, previousBox, rule.getTargetNode()._boxForNode);
		this.lines.push(line);
		rule._linesForRule.push(line);
	}

	// update arragements
	var arrangements = [];
	for (var i = 0; i < this.boxes.length; i++) {
		var box = this.boxes[i];
		// for each node we have up to four arrangements 
		// (for each side) of the box
		var linesTop = [], linesBottom = [], linesLeft = [], linesRight = [];
		var lines = box.lines;
		for (var k = 0; k < lines.length; k++) {
			var line = lines[k];
			var otherBox = line.getOtherBox(box);

			// if we are using a routing box
			// test some special cases for routing
			if (otherBox.isRoutingPoint()) {
				// special case #1:
				// which is horizontal beside the incoming source, 
				// but vertically in between its previous and next box
				// force horizontal layout with only one bend
				var left = box.centerX - box.width / 2;
				var right = box.centerX + box.width / 2;
				var top = box.centerY - box.height / 2;
				var bottom = box.centerY + box.height / 2;
				// get range of surrounding boxes
				var xmin = otherBox.lines[0].getOtherBox(otherBox).centerX;
				var xmax = otherBox.lines[0].getOtherBox(otherBox).centerX;
				var ymin = otherBox.lines[0].getOtherBox(otherBox).centerY;
				var ymax = otherBox.lines[0].getOtherBox(otherBox).centerY;
				for (var l = 1; l < otherBox.lines.length; l++) {
					var boxx = otherBox.lines[l].getOtherBox(otherBox).centerX;
					var boxy = otherBox.lines[l].getOtherBox(otherBox).centerY;
					var boxw = otherBox.lines[l].getOtherBox(otherBox).width;
					var boxh = otherBox.lines[l].getOtherBox(otherBox).height;
					xmin = Math.min(xmin, boxx - boxw / 2);
					xmax = Math.max(xmax, boxx + boxw / 2);
					ymin = Math.min(ymin, boxy - boxh / 2);
					ymax = Math.max(ymax, boxy + boxh / 2);
				}
				if (otherBox.centerX < left || otherBox.centerX > right) {
					if (ymin <= otherBox.centerY && otherBox.centerY <= ymax) {
						// here we reached the special case!
						if (otherBox.centerX < left) {
							linesLeft.push(line);
						}
						else {
							linesRight.push(line);
						}
						continue;
					}
				}
				if (otherBox.centerY < top || otherBox.centerY > bottom) {
					if (xmin <= otherBox.centerX && otherBox.centerX <= xmax) {
						// here we reached the special case!
						if (otherBox.centerY < top) {
							linesTop.push(line);
						}
						else {
							linesBottom.push(line);
						}
						continue;
					}
				}
				// special case #2:
				// if the routing point is in one direction 
				// further away than the next target point
				// go to that direction first
				var nextBox;
				for (var l = 0; l < otherBox.lines.length; l++) {
					nextBox = otherBox.lines[l].getOtherBox(otherBox);
					if (nextBox != box) break; // we found it 
				}
				if (otherBox.centerY > bottom && nextBox.centerY <= box.centerY) {
					linesBottom.push(line);
					continue;
				}
				if (otherBox.centerY < top && nextBox.centerY >= box.centerY) {
					linesTop.push(line);
					continue;
				}
				if (otherBox.centerX > right && nextBox.centerX <= box.centerX) {
					linesRight.push(line);
					continue;
				}
				if (otherBox.centerX < left && nextBox.centerX >= box.centerX) {
					linesLeft.push(line);
					continue;
				}
			}

			// calculate horizontal and vertical space between the boxes
			// the element is anchored in a way, 
			// that most available space is used
			var dx = otherBox.centerX - box.centerX;
			var dy = otherBox.centerY - box.centerY;
			if (Math.abs(dx) - (box.width + otherBox.width) / 2 > Math.abs(dy) - (box.height + otherBox.height) / 2) {
				if (dx < 0) {
					linesLeft.push(line);
				}
				else {
					linesRight.push(line);
				}
			}
			else {
				if (dy < 0) {
					linesTop.push(line);
				}
				else {
					linesBottom.push(line);
				}
			}
		}
		// now create the arrangements
		arrangements.push(new RuleArrangement(box, linesTop, 'top'));
		arrangements.push(new RuleArrangement(box, linesBottom, 'bottom'));
		arrangements.push(new RuleArrangement(box, linesLeft, 'left'));
		arrangements.push(new RuleArrangement(box, linesRight, 'right'));
	}

	// rearrange the arrangements
	for (var i = 0; i < arrangements.length; i++) {
		arrangements[i].arrange();
	}

	// add bendHints to Anchors
	for (var i = 0; i < arrangements.length; i++) {
		arrangements[i].addBendHints();
	}

	// add guard position hints to Anchors
	if (this.allowGuardOptimization) {
		for (var i = 0; i < arrangements.length; i++) {
			arrangements[i].addGuardPositionHints();
		}
	}

	for (var i = 0; i < this.flowchart.rules.length; i++) {
		var rule = this.flowchart.rules[i];
		rule.setSourceAnchor(rule._linesForRule[0].sourceAnchor);
		rule.setTargetAnchor(rule._linesForRule[rule._linesForRule.length - 1].targetAnchor);
		this.setRuleCoordinates(rule);
	}
	if (typeof FlowEditor != "undefined" && FlowEditor && FlowEditor.autoResize) {
		FlowEditor.autoResize();
	}
};

Router.prototype.setRuleCoordinates = function(rule) {
	var lines = rule._linesForRule;
	var coordinates = [];

	if (lines.length == 2 && lines[0].targetBox.isBendArea()) {
		// make single bended line
		var a1 = lines[0].sourceAnchor;
		var a2 = lines[1].targetAnchor;
		coordinates.push([a1.x, a1.y]);
		coordinates.push([a2.x, a1.y]);
		coordinates.push([a2.x, a2.y]);
		rule.setCoordinates(coordinates);
		return;
	}

	for (var i = 0; i < lines.length; i++) {
		var line = lines[i];
		var sourceAnchor = line.sourceAnchor;
		var targetAnchor = line.targetAnchor;
		var x1 = sourceAnchor.x;
		var y1 = sourceAnchor.y;
		var s1 = sourceAnchor.slide;
		var x2 = targetAnchor.x;
		var y2 = targetAnchor.y;
		var s2 = targetAnchor.slide;
		var leadingAnchor = line.sourceBox.isRoutingPoint() ? targetAnchor : sourceAnchor;
		var trailingAnchor = line.sourceBox.isRoutingPoint() ? sourceAnchor : targetAnchor;
		var bendOnce = line.sourceBox.isRoutingPoint() || line.targetBox.isRoutingPoint();

		if (leadingAnchor.isHorizontal()) {
			var overlap = Math.min(y1 + s1, y2 + s2) - Math.max(y1 - s1, y2 - s2);
			if (overlap >= 0) {
				// draw straight line, using optimal source position if possible
				// otherwise use overlap
				var yMiddle = (y1 > y2 - s2 && y1 < y2 + s2) ? y1 : Math.max(y1 - s1, y2 - s2) + Math.floor(overlap / 2);
				coordinates.push([x1, yMiddle]);
				coordinates.push([x2, yMiddle]);
			}
			else {
				// draw double bended line
				var xMiddle = Math.floor((x1 + x2) / 2);
				if (bendOnce) xMiddle = trailingAnchor.x;
				else xMiddle = (sourceAnchor.__bendHint || targetAnchor.__bendHint || xMiddle);
				coordinates.push([x1, y1]);
				coordinates.push([xMiddle, y1]);
				coordinates.push([xMiddle, y2]);
				coordinates.push([x2, y2]);
			}
		}
		else {
			var overlap = Math.min(x1 + s1, x2 + s2) - Math.max(x1 - s1, x2 - s2);
			if (overlap >= 0) {
				// draw straight line, using optimal source position if possible
				// otherwise use overlap
				var xMiddle = (x1 > x2 - s2 && x1 < x2 + s2) ? x1 : Math.max(x1 - s1, x2 - s2) + Math.floor(overlap / 2);
				coordinates.push([xMiddle, y1]);
				coordinates.push([xMiddle, y2]);
			}
			else {
				// draw double bended line
				var yMiddle = Math.floor((y1 + y2) / 2);
				if (bendOnce) yMiddle = trailingAnchor.y;
				else yMiddle = (sourceAnchor.__bendHint || targetAnchor.__bendHint || yMiddle);
				coordinates.push([x1, y1]);
				coordinates.push([x1, yMiddle]);
				coordinates.push([x2, yMiddle]);
				coordinates.push([x2, y2]);
			}
		}
	}
	// remove duplicate points
	for (var i = 0; i < coordinates.length - 1; i++) {
		if (coordinates[i][0] == coordinates[i + 1][0] && coordinates[i][1] == coordinates[i + 1][1]) {
			coordinates.splice(i, 1);
			i--;
		}
	}
	rule.setCoordinates(coordinates);
};


//-----
// classes for abstracting from nodes and rules 
// to boxes and and connections. These objects can be
// used for nodes and rules as well as for RoutingPoints 
// and their interconnections
// 
// Based on this, the coordinates are created for the rules.
//-----

function RoutingBox(nodeOrRoutingPoint, centerX, centerY, width, height) {
	this.nodeOrRoutingPoint = nodeOrRoutingPoint;
	this.centerX = centerX;
	this.centerY = centerY;
	this.width = width;
	this.height = height;
	this.lines = [];
}

RoutingBox.createFromNode = function(node) {
	return new RoutingBox(
		node,
		node.getCenterX(),
		node.getCenterY(),
		node.getWidth(),
		node.getHeight()
	);
};

RoutingBox.createFromRoutingPoint = function(routingPoint) {
	return new RoutingBox(
		routingPoint,
		routingPoint.getX(),
		routingPoint.getY(),
		1, 1
	);
};

RoutingBox.createBendArea = function(rule, x1, y1, x2, y2) {
	return new RoutingBox(rule,
		Math.floor((x1 + x2) / 2), Math.floor((y1 + y2) / 2),
		Math.abs(x1 - x2), Math.abs(y1 - y2));
};

RoutingBox.prototype.isBendArea = function() {
	return this.nodeOrRoutingPoint instanceof Rule;
};

RoutingBox.prototype.isRoutingPoint = function() {
	return this.width <= 1 && this.height <= 1;
};

function RoutingLine(rule, sourceBox, targetBox) {
	this.rule = rule;
	this.sourceBox = sourceBox;
	this.targetBox = targetBox;
	sourceBox.lines.push(this);
	targetBox.lines.push(this);
}

RoutingLine.prototype.getOtherBox = function(box) {
	return (box == this.sourceBox) ? this.targetBox : this.sourceBox;
};


// -----
// class for rule routing attributes
// 
// Contains a set of rules for a node that leads into one direction
// and therefore has to be arranged / lined up on the object.
// The directon of the rules (incomung/outgoing) is irrelevant
//
// Anchor must be one of 'top', 'bottom', 'left' or 'right', 
// describing where the rules are aligned at the node 
// -----

function RuleArrangement(box, lines, anchorType) {
	this.box = box;
	this.lines = lines;
	this.anchorType = anchorType;
}

RuleArrangement.prototype.isIncoming = function(line) {
	return line.targetBox == this.box;
};

RuleArrangement.prototype.isOutgoing = function(line) {
	return line.sourceBox == this.box;
};

RuleArrangement.prototype.arrange = function() {
	// first sort the nodes according to the positions of the other objects
	var isHorizontal = this.isHorizontal();
	var box = this.box;
	var anchorType = this.anchorType;
	this.lines.sort(function(l1, l2) {
		var b1 = l1.getOtherBox(box);
		var b2 = l2.getOtherBox(box);

		// special case: when going through bend boxes, only sort by rules itself
		if (box.isBendArea() || (b1.isBendArea() && b2.isBendArea())) {
			// when connecting same nodes, order by id, but for left or down arrows reverse
			if (l1.rule.getSourceNode() == l2.rule.getSourceNode() && l1.rule.getTargetNode() == l2.rule.getTargetNode()) {
				return ((anchorType == 'right' || anchorType == 'bottom') ? -1 : 1) *
					((l1.rule.fcid < l2.rule.fcid) ? -1 : 1);
			}
			// otherwise use target box for sorting (above before below)
			// and only sort for the closer distance in horizontal position
			// (or revert if target is below node)
			var s1 = l1.rule.getSourceNode(), s2 = l2.rule.getSourceNode();
			var t1 = l1.rule.getTargetNode(), t2 = l2.rule.getTargetNode();
			var above1 = t1.getCenterY() < s1.getCenterY(), above2 = t2.getCenterY() < s2.getCenterY();
			if (above1 && !above2) return -1;
			if (!above1 && above2) return 1;
			return (above1 ? 1 : -1) *
				(Math.abs(t1.getCenterX() - s1.getCenterX()) - Math.abs(t2.getCenterX() - s2.getCenterX()));
		}

		var angle1 = Math.atan2(b1.centerY - box.centerY, b1.centerX - box.centerX);
		var angle2 = Math.atan2(b2.centerY - box.centerY, b2.centerX - box.centerX);

		// da auf der linken Seite ein Koordinatensprung von PI ==> -PI statt findet
		// (beim uebertritt  ueber 180°), muss dieser ausgeglichen werden, in dem die Koordinaten
		// in den Bereich [0 .. 2*PI] gebracht werden 
		if (anchorType == 'left') {
			if (angle1 < 0) angle1 += 2 * Math.PI;
			if (angle2 < 0) angle2 += 2 * Math.PI;
		}
		return (anchorType == 'top' || anchorType == 'right' )
			? angle1 - angle2
			: angle2 - angle1;
	});
	var center = isHorizontal ? box.centerY : box.centerX;
	var pixels = isHorizontal ? box.height : box.width;
	var count = this.lines.length;
	var delta = pixels / count;

	var fixedPos = isHorizontal
		? (this.anchorType == 'left' ? Math.ceil(box.centerX - box.width / 2) : Math.floor(box.centerX + box.width / 2))
		: (this.anchorType == 'top' ? Math.ceil(box.centerY - box.height / 2) : Math.floor(box.centerY + box.height / 2));

	for (var i = 0; i < count; i++) {
		var line = this.lines[i];
		var variablePos = Math.floor(center + (i - (count - 1) / 2.0) * delta);
		var coords = isHorizontal ? [fixedPos, variablePos] : [variablePos, fixedPos];
		var node = box.nodeOrRoutingPoint;
		var anchor = new Anchor(node, coords[0], coords[1], this.anchorType, Math.floor(delta / 2));
		if (this.isIncoming(line)) {
			line.targetAnchor = anchor;
		}
		else {
			line.sourceAnchor = anchor;
		}
	}
};

RuleArrangement.prototype.isHorizontal = function() {
	return (this.anchorType == 'left' || this.anchorType == 'right');
};

RuleArrangement.prototype.addGuardPositionHints = function() {
	// place first guard one above / left of the line
	if (!(this.box.nodeOrRoutingPoint instanceof Node)) return;
	var node = this.box.nodeOrRoutingPoint;
	var rule = (this.lines.length > 0) && (this.lines[0].rule);
	if (!rule || rule.getSourceNode() != node) return;

	if (this.isHorizontal()) {
		this.lines[0].sourceAnchor.hints.guardAbove = true;
	}
	else {
		if (this.lines.length > 1) {
			this.lines[0].sourceAnchor.hints.guardLeft = true;
		}
		//noinspection JSDuplicatedDeclaration
		for (var i=0; i<this.lines.length; i++) {
			var line = this.lines[i];
			if (Math.abs(line.sourceAnchor.y - line.targetAnchor.y) > 25) {
				line.sourceAnchor.hints.guardShift = true;
			}
		}
	}

	// place all following horizontal guards (despite the last one)
	// also above if there is enough space between the lines
	if (this.isHorizontal() && this.lines.length >= 3) {
		//noinspection JSDuplicatedDeclaration
		for (var i=1; i<this.lines.length-1; i++) {
			var yBefore = this.lines[i-1].sourceAnchor.y;
			var y = this.lines[i].sourceAnchor.y;
			if (y - yBefore >= 10) {
				this.lines[i].sourceAnchor.hints.guardAbove = true;
				if (y - yBefore < 15) {
					this.lines[i].sourceAnchor.y += 15 - (y - yBefore);
					this.lines[i].targetAnchor.y += 15 - (y - yBefore);
				}
			}
		}
	}
};

RuleArrangement.prototype.addBendHints = function() {
	if (this.lines.length < 2) return;
	if (this.box.isRoutingPoint()) return;
	var isHorizontal = this.isHorizontal();
	var isTopLeft = isHorizontal ? this.anchorType == 'left' : this.anchorType == 'top';

	var minBend = null, maxBend = null;
	var lowLines = [], highLines = [];
	//noinspection JSDuplicatedDeclaration
	for (var i = 0; i < this.lines.length; i++) {
		var line = this.lines[i];
		var anchor1 = line.sourceAnchor;
		var anchor2 = line.targetAnchor;
		var minPos, maxPos;
		if (!isHorizontal) {
			maxPos = Math.max(anchor1.y - 15, anchor2.y - 5);
			minPos = Math.min(anchor1.y + 15, anchor2.y + 5);
		}
		else {
			maxPos = Math.max(anchor1.x - 15, anchor2.x - 5);
			minPos = Math.min(anchor1.x + 15, anchor2.x + 5);
		}
		maxBend = maxBend ? Math.min(maxBend, maxPos) : maxPos;
		minBend = minBend ? Math.max(minBend, minPos) : minPos;
		var other = line.getOtherBox(this.box);
		if (!isHorizontal
			? (isTopLeft
			? this.box.centerX < other.centerX
			: this.box.centerX > other.centerX)
			: (isTopLeft
			? this.box.centerY < other.centerY
			: this.box.centerY > other.centerY)) {
			lowLines.push(line);
		}
		else {
			highLines.push(line);
		}
	}

	var count = Math.max(lowLines.length, highLines.length);
	//comment out to enable low priority hints
	//if (count < 2) return;

	// use available space but not more than 5px
	var bendDelta = Math.min(5, Math.ceil((maxBend - minBend) / count));
	var pos = (maxBend + minBend) / 2 - bendDelta * count * 0.5;
	// and finally apply bends
	var highPrio = (highLines.length >= 2) ? 1.0 : 0.5;
	var lowPrio = (lowLines.length >= 2) ? 1.0 : 0.5;
	//noinspection JSDuplicatedDeclaration
	for (var i = 0; i < count; i++) {
		if (highLines.length - 1 - i >= 0) {
			this.setBendHint(highLines[highLines.length - 1 - i].sourceAnchor, pos, highPrio);
		}
		if (i < lowLines.length) {
			this.setBendHint(lowLines[i].sourceAnchor, pos, lowPrio);
		}
		pos += bendDelta;
	}
};


RuleArrangement.prototype.setBendHint = function(anchor, position, importance) {
	if (!anchor.__bendPriority || anchor.__bendPriority < importance) {
		anchor.__bendHint = position;
		anchor.__bendPriority = importance;
	}
};

