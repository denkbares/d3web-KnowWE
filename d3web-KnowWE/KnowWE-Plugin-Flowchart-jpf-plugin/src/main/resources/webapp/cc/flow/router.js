
function Router(flowchart) {
	this.flowchart = flowchart;
}

Router.prototype.rerouteNodes = function(nodes) {
	this.rerouteAll();
}

Router.prototype.rerouteAll = function() {
	// update arragements
	var arrangements = [];
	for (var i=0; i<this.flowchart.nodes.length; i++) {
		var node = this.flowchart.nodes[i];
		// for each node we have up to four arrangements 
		// (for each side) of the box
		var rulesTop = [], rulesBottom = [], rulesLeft = [], rulesRight = []; 
		var rules = this.flowchart.findRulesForNode(node);
		for (var k=0; k<rules.length; k++) {
			var rule = rules[k];
			var otherNode = rule.getOtherNode(node); 
			// calculate horizontal and vertical space between the boxes
			// the element is anchored in a way, 
			// that most available space is used
			var dx = otherNode.getCenterX() - node.getCenterX();
			var dy = otherNode.getCenterY() - node.getCenterY();
			if (Math.abs(dx)-(node.width+otherNode.width)/2 > Math.abs(dy)-(node.height+otherNode.height)/2) {
				if (dx < 0) {
					rulesLeft.push(rule);
				}
				else {
					rulesRight.push(rule);
				}
			}
			else {
				if (dy < 0) {
					rulesTop.push(rule);
				}
				else {
					rulesBottom.push(rule);
				}
			}
		}
		// now create the arrangements
		arrangements.push(new RuleArrangement(node, rulesTop, 'top'));
		arrangements.push(new RuleArrangement(node, rulesBottom, 'bottom'));
		arrangements.push(new RuleArrangement(node, rulesLeft, 'left'));
		arrangements.push(new RuleArrangement(node, rulesRight, 'right'));
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
	} 
}

Router.prototype.setRuleCoordinates = function(rule) {
	var sourceAnchor = rule.getSourceAnchor();
	var targetAnchor = rule.getTargetAnchor();
	var x1 = sourceAnchor.x;
	var y1 = sourceAnchor.y;
	var s1 = sourceAnchor.slide;
	var x2 = targetAnchor.x;
	var y2 = targetAnchor.y;
	var s2 = targetAnchor.slide;
	var isHorizontal = (sourceAnchor.type == 'left' || sourceAnchor.type == 'right'); 
	if (isHorizontal) {
		var overlap = Math.min(y1+s1, y2+s2) - Math.max(y1-s1, y2-s2);
		if (overlap >= 0) {
			// draw straight line, using optimal source position if possible
			// otherwise use overlap
			var yMiddle = (y1 > y2-s2 && y1 < y2+s2) ? y1 : Math.max(y1-s1, y2-s2) + Math.floor(overlap / 2);
			rule.setCoordinates([[x1, yMiddle], [x2, yMiddle]]);
		}
		else {
			// draw double bended line
			var xMiddle = (sourceAnchor.__bendHint || targetAnchor.__bendHint || Math.floor((x1+x2)/2)); 
			rule.setCoordinates([
				[x1, y1], 
				[xMiddle, y1],
				[xMiddle, y2],
				[x2, y2]
			]);
		}
	}
	else {
		var overlap = Math.min(x1+s1, x2+s2) - Math.max(x1-s1, x2-s2);
		if (overlap >= 0) {
			// draw straight line, using optimal source position if possible
			// otherwise use overlap
			var xMiddle = (x1 > x2-s2 && x1 < x2+s2) ? x1 : Math.max(x1-s1, x2-s2) + Math.floor(overlap / 2);
			rule.setCoordinates([[xMiddle, y1], [xMiddle, y2]]);
		}
		else {
			// draw double bended line
			var yMiddle = (sourceAnchor.__bendHint || targetAnchor.__bendHint || Math.floor((y1+y2)/2)); 
			rule.setCoordinates([
				[x1, y1], 
				[x1, yMiddle],
				[x2, yMiddle], 
				[x2, y2]
			]);
		}
	}
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

function RuleArrangement(node, rules, anchorType) {
	this.node = node;
	this.rules = rules;
	this.anchorType = anchorType;
}

RuleArrangement.prototype.isIncoming = function(rule) {
	return rule.targetNode == this.node;
}

RuleArrangement.prototype.isOutgoing = function(rule) {
	return rule.sourceNode == this.node;
}

RuleArrangement.prototype.arrange = function() {
	// first sort the nodes according to the positions of the other objects
	var isHorizontal = this.isHorizontal();
	var node = this.node;
	var anchorType = this.anchorType;
	this.rules.sort(function(r1, r2) {
		var n1 = r1.getOtherNode(node);
		var n2 = r2.getOtherNode(node);
//		return isHorizontal
//			? (n1.getCenterY() - n2.getCenterY())
//			: (n1.getCenterX() - n2.getCenterX());
		var angle1 = Math.atan2(n1.getCenterY() - node.getCenterY(), n1.getCenterX() - node.getCenterX());
		var angle2 = Math.atan2(n2.getCenterY() - node.getCenterY(), n2.getCenterX() - node.getCenterX());
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
	var center = isHorizontal ? this.node.getCenterY() : this.node.getCenterX();  
	var pixels = isHorizontal ? this.node.getHeight() : this.node.getWidth();
	var count = this.rules.length;
	var delta = pixels / count;

	var fixedPos = isHorizontal 
		? (this.anchorType=='left' ? this.node.getLeft()-1 : this.node.getLeft()+this.node.getWidth()) 
		: (this.anchorType=='top' ? this.node.getTop()-1 : this.node.getTop()+this.node.getHeight());

	for (var i=0; i<count; i++) {
		var rule = this.rules[i];
		var variablePos = Math.floor(center + (i - (count-1)/2.0) * delta);
		var coords = isHorizontal ? [fixedPos, variablePos] : [variablePos, fixedPos];
		var anchor = new Anchor(this.node, coords[0], coords[1], this.anchorType, Math.floor(delta/2));
		if (this.isIncoming(rule)) {
			rule.setTargetAnchor(anchor);
		}
		else {
			rule.setSourceAnchor(anchor);
		}
	}
} 

RuleArrangement.prototype.isHorizontal = function() {
	return (this.anchorType == 'left' || this.anchorType == 'right');
}

RuleArrangement.prototype.addBendHints = function() {
	if (this.rules.length < 2) return;
	var isHorizontal = this.isHorizontal();
	var isTopLeft = isHorizontal ? this.anchorType == 'left' : this.anchorType == 'top';

	var minBend = null, maxBend = null;
	var lowRules = [], highRules = [];
	for (var i=0; i<this.rules.length; i++) {
		var rule = this.rules[i];
		var anchor1 = rule.getSourceAnchor();
		var anchor2 = rule.getTargetAnchor();
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
		var other = rule.getOtherNode(this.node);
		if (!isHorizontal 
				? (isTopLeft
						? this.node.getCenterX() < other.getCenterX()
						: this.node.getCenterX() > other.getCenterX()) 
				: (isTopLeft
						? this.node.getCenterY() < other.getCenterY()
						: this.node.getCenterY() > other.getCenterY())) {
			lowRules.push(rule);
		}
		else {
			highRules.push(rule);
		}
	}
	
	var count = Math.max(lowRules.length, highRules.length);
	//comment out to enable low priority hints
	//if (count < 2) return;
	
	// use available space but not more than 5px
	var bendDelta = Math.min(5, Math.ceil((maxBend - minBend) / count));
	var bendPos = (maxBend + minBend) / 2 - bendDelta*count*0.5; 
	// and finally apply bends
	var pos = bendPos;
	var highPrio = (highRules.length >= 2) ? 1.0 : 0.5;
	var lowPrio = (lowRules.length >= 2) ? 1.0 : 0.5;
	for (var i=0; i<count; i++) {
		if (highRules.length-1-i>=0) {
			this.setBendHint(highRules[highRules.length-1-i].getSourceAnchor(), pos, highPrio);
		} 
		if (i<lowRules.length) {
			this.setBendHint(lowRules[i].getSourceAnchor(), pos, lowPrio);
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

