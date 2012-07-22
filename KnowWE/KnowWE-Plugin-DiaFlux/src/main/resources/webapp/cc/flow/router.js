
function Router(flowchart) {
	this.flowchart = flowchart;
}

Router.prototype.rerouteNodes = function(nodes) {
	this.rerouteAll();
}

Router.prototype.rerouteAll = function() {
	// create all boxes and all lines in between
	this.boxes = [];
	this.lines = [];
	for (var i=0; i<this.flowchart.nodes.length; i++) {
		var node = this.flowchart.nodes[i];
		var box = RoutingBox.createFromNode(node);
		node._boxForNode = box;
		this.boxes.push(box);
	}	
	for (var i=0; i<this.flowchart.rules.length; i++) {
		var rule = this.flowchart.rules[i];
		rule._linesForRule = [];
		var previousBox = rule.getSourceNode()._boxForNode;
		// we have intermediate routing points, so create boxes and lines
		for (var k=0; k<rule.routingPoints.length; k++) {
			var routingPoint = rule.routingPoints[k];
			// create box
			var nextBox = RoutingBox.createFromRoutingPoint(routingPoint);
			this.boxes.push(nextBox);
			// create line
			var line = new RoutingLine(previousBox, nextBox);
			this.lines.push(line);
			rule._linesForRule.push(line);
			previousBox = nextBox;
		}
		// and finally create line to target node
		var nextBox = rule.getTargetNode()._boxForNode;
		var line = new RoutingLine(previousBox, nextBox);
		this.lines.push(line);
		rule._linesForRule.push(line);
	}
	
	// update arragements
	var arrangements = [];
	for (var i=0; i<this.boxes.length; i++) {
		var box = this.boxes[i];
		// for each node we have up to four arrangements 
		// (for each side) of the box
		var linesTop = [], linesBottom = [], linesLeft = [], linesRight = [];
		var lines = box.lines;
		for (var k=0; k<lines.length; k++) {
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
				for (var l=1; l<otherBox.lines.length; l++) {
					var boxx = otherBox.lines[l].getOtherBox(otherBox).centerX;
					var boxy = otherBox.lines[l].getOtherBox(otherBox).centerY;
					var boxw = otherBox.lines[l].getOtherBox(otherBox).width;
					var boxh = otherBox.lines[l].getOtherBox(otherBox).height;
					xmin = Math.min(xmin, boxx - boxw/2);
					xmax = Math.max(xmax, boxx + boxw/2);
					ymin = Math.min(ymin, boxy - boxh/2);
					ymax = Math.max(ymax, boxy + boxh/2);
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
				for (var l=0; l<otherBox.lines.length; l++) {
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
			if (Math.abs(dx)-(box.width+otherBox.width)/2 > Math.abs(dy)-(box.height+otherBox.height)/2) {
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
	for (var i=0; i<arrangements.length; i++) {
		arrangements[i].arrange();
	}
	
	// add bendHints to Anchors
	for (var i=0; i<arrangements.length; i++) {
		arrangements[i].addBendHints();
	}
	
	for (var i=0; i<this.flowchart.rules.length; i++) {
		var rule = this.flowchart.rules[i];
		this.setRuleCoordinates(rule);
		rule.setSourceAnchor(rule._linesForRule[0].sourceAnchor);
		rule.setTargetAnchor(rule._linesForRule[rule._linesForRule.length-1].targetAnchor);
	} 
}

Router.prototype.setRuleCoordinates = function(rule) {
	var lines = rule._linesForRule;
	var coordinates = [];
	
	for (var i=0; i<lines.length; i++) {
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
			var overlap = Math.min(y1+s1, y2+s2) - Math.max(y1-s1, y2-s2);
			if (overlap >= 0) {
				// draw straight line, using optimal source position if possible
				// otherwise use overlap
				var yMiddle = (y1 > y2-s2 && y1 < y2+s2) ? y1 : Math.max(y1-s1, y2-s2) + Math.floor(overlap / 2);
				coordinates.push([x1, yMiddle]);
				coordinates.push([x2, yMiddle]);
			}
			else {
				// draw double bended line
				var xMiddle = Math.floor((x1+x2)/2);
				if (bendOnce) xMiddle = trailingAnchor.x;
				else xMiddle = (sourceAnchor.__bendHint || targetAnchor.__bendHint || xMiddle); 
				coordinates.push([x1, y1]);
				coordinates.push([xMiddle, y1]);
				coordinates.push([xMiddle, y2]);
				coordinates.push([x2, y2]);
			}
		}
		else {
			var overlap = Math.min(x1+s1, x2+s2) - Math.max(x1-s1, x2-s2);
			if (overlap >= 0) {
				// draw straight line, using optimal source position if possible
				// otherwise use overlap
				var xMiddle = (x1 > x2-s2 && x1 < x2+s2) ? x1 : Math.max(x1-s1, x2-s2) + Math.floor(overlap / 2);
				coordinates.push([xMiddle, y1]);
				coordinates.push([xMiddle, y2]);
			}
			else {
				// draw double bended line
				var yMiddle = Math.floor((y1+y2)/2); 
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
	for (var i=0; i<coordinates.length-1; i++) {
		if (coordinates[i][0] == coordinates[i+1][0] && coordinates[i][1] == coordinates[i+1][1]) {
			coordinates.splice(i, 1);
		}
	}
	rule.setCoordinates(coordinates);
}


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
}

RoutingBox.createFromRoutingPoint = function (routingPoint) {
	return new RoutingBox(
			routingPoint,
			routingPoint.getX(),
			routingPoint.getY(),
			1 , 1
			);
}

RoutingBox.prototype.isRoutingPoint = function () {
	return this.width <= 1 && this.height <= 1;
}

function RoutingLine (sourceBox, targetBox) {
	this.sourceBox = sourceBox;
	this.targetBox = targetBox;
	sourceBox.lines.push(this);
	targetBox.lines.push(this);
}

RoutingLine.prototype.getOtherBox = function (box) {
	return (box == this.sourceBox) ? this.targetBox : this.sourceBox;
}


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
	this.box = box
	this.lines = lines;
	this.anchorType = anchorType;
}

RuleArrangement.prototype.isIncoming = function (line) {
	return line.targetBox == this.box;
}

RuleArrangement.prototype.isOutgoing = function (line) {
	return line.sourceBox == this.box;
}

RuleArrangement.prototype.arrange = function() {
	// first sort the nodes according to the positions of the other objects
	var isHorizontal = this.isHorizontal();
	var box = this.box;
	var anchorType = this.anchorType;
	this.lines.sort(function(l1, l2) {
		var b1 = l1.getOtherBox(box);
		var b2 = l2.getOtherBox(box);
		var angle1 = Math.atan2(b1.centerY - box.centerY, b1.centerX - box.centerX);
		var angle2 = Math.atan2(b2.centerY - box.centerY, b2.centerX - box.centerX);
		// da auf der linken Seite ein Koordinatensprung von PI ==> -PI statt findet 
		// (beim uebertritt  ueber 180°), muss dieser ausgeglichen werden, in dem die Koordinaten
		// in den Bereich [0 .. 2*PI] gebracht werden 
		if (anchorType == 'left') {
			if (angle1 < 0) angle1 += 2*Math.PI
			if (angle2 < 0) angle2 += 2*Math.PI
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
		? (this.anchorType=='left' ? Math.ceil(box.centerX - box.width/2) : Math.floor(box.centerX + box.width/2)) 
		: (this.anchorType=='top' ? Math.ceil(box.centerY - box.height/2) : Math.floor(box.centerY + box.height/2));

	for (var i=0; i<count; i++) {
		var line = this.lines[i];
		var variablePos = Math.floor(center + (i - (count-1)/2.0) * delta);
		var coords = isHorizontal ? [fixedPos, variablePos] : [variablePos, fixedPos];
		var node = box.nodeOrRoutingPoint;
		var anchor = new Anchor(node, coords[0], coords[1], this.anchorType, Math.floor(delta/2));
		if (this.isIncoming(line)) {
			line.targetAnchor = anchor;
		}
		else {
			line.sourceAnchor = anchor;
		}
	}
} 

RuleArrangement.prototype.isHorizontal = function() {
	return (this.anchorType == 'left' || this.anchorType == 'right');
}

RuleArrangement.prototype.addBendHints = function() {
	if (this.lines.length < 2) return;
	if (this.box.isRoutingPoint()) return;
	var isHorizontal = this.isHorizontal();
	var isTopLeft = isHorizontal ? this.anchorType == 'left' : this.anchorType == 'top';

	var minBend = null, maxBend = null;
	var lowLines = [], highLines = [];
	for (var i=0; i<this.lines.length; i++) {
		var line = this.lines[i];
		var anchor1 = line.sourceAnchor;
		var anchor2 = line.targetAnchor;
		var minPos, maxPos;
		if (!isHorizontal) {
			maxPos = Math.max(anchor1.y-15, anchor2.y-5);
			minPos = Math.min(anchor1.y+15, anchor2.y+5);
		}
		else {
			maxPos = Math.max(anchor1.x-15, anchor2.x-5);
			minPos = Math.min(anchor1.x+15, anchor2.x+5);
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
	var bendPos = (maxBend + minBend) / 2 - bendDelta*count*0.5; 
	// and finally apply bends
	var pos = bendPos;
	var highPrio = (highLines.length >= 2) ? 1.0 : 0.5;
	var lowPrio = (lowLines.length >= 2) ? 1.0 : 0.5;
	for (var i=0; i<count; i++) {
		if (highLines.length-1-i>=0) {
			this.setBendHint(highLines[highLines.length-1-i].sourceAnchor, pos, highPrio);
		} 
		if (i<lowLines.length) {
			this.setBendHint(lowLines[i].sourceAnchor, pos, lowPrio);
		}
		pos += bendDelta;
	}
}


RuleArrangement.prototype.setBendHint = function(anchor, position, importance) {
	if (!anchor.__bendPriority || anchor.__bendPriority < importance) {
		anchor.__bendHint = position;
		anchor.__bendPriority = importance;
	}
}

