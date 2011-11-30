//Rule event handlers
//register click-listener for that class
CCEvents.addClassListener('click', 'ArrowTool', function(event) {/*NOP, but avoid bubbling*/});


//register select click events for rule
CCEvents.addClassListener('click', 'Rule', 
	function(event) {
		if (!this.__rule.flowchart.isSelected(this.__rule)) {
			if (this.__rule) this.__rule.select(DiaFluxUtils.isControlKey(event));
		}
		
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
);

//overrides empty implementation in rule.js
Rule.prototype.createDraggable = function() {
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
Rule.prototype.destroyDraggable = function() {
	this.draggable.destroy();
	this.draggable = null;
}


Rule.prototype.toXML = function() {
	var xml = '\t<edge' +
			(this.fcid ? ' fcid="'+this.fcid+'"' : '')+
			'>\n';
	xml += '\t\t<origin>'+this.sourceNode.getNodeModel().fcid+'</origin>\n';
	xml += '\t\t<target>'+this.targetNode.getNodeModel().fcid+'</target>\n';
	if (this.guard && this.guard.getMarkup() != 'NOP') {
		if (DiaFluxUtils.isString(this.guard)) {
			xml += '\t\t<guard>' + this.guard + '</guard>\n';
		}
		else {
			xml += '\t\t<guard markup="'+this.guard.getMarkup()+'">' +
					this.guard.toXML(this) +
					'</guard>\n';
		}
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

RuleArrowTool.prototype.getDOM = function() {
	return this.dom;
}

RuleArrowTool.prototype.isVisible = function() {
	return (this.dom != null);
}

RuleArrowTool.prototype.setVisible = function(visible) {
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

RuleArrowTool.prototype.destroy = function() {
	this.setVisible(false);
}

RuleArrowTool.prototype.render = function() {
	var x, y, dom, arrow;
	x = this.rule.coordinates[this.rule.coordinates.length - 1][0] - 13;
	y = this.rule.coordinates[this.rule.coordinates.length - 1][1] - 13;
	
	var ruleDOM = this.rule.getDOM();
	var arrowDir = ruleDOM.select('.arrow')[0].className.match(/_(\w+)/)[1];
		
	dom = Builder.node('div', {
		id: this.rule.fcid+'_arrow_tool',
		className: 'ArrowTool ArrowTool_' + arrowDir,
		style: 	"left: " + x + "px; top:" + y + "px;"
	});
	dom.__arrowTool = this;
	return dom;
}

RuleArrowTool.prototype.createDraggable = function() {
	var newDrag = new Draggable(this.getDOM(), {
		ghosting: false,
		revert: true, 
		starteffect: null,
		endeffect: null,
		onStart: function(draggable, event) {
		},
		onEnd: function(draggable, event) {
			draggable.__arrowTool.showLine(null, null);
		},
		snap: function(x, y, draggable) {
			draggable.__arrowTool.showLine(x, y);
			return [x, y];
		},
		scroll: this.flowchart.fcid
	});
	newDrag.__arrowTool = this;
	return newDrag;	
}

RuleArrowTool.prototype.createRule = function(targetNode) {
	if (this.rule.getTargetNode() != targetNode) {
		var rule = new Rule(this.rule.fcid, this.rule.getSourceNode(), this.rule.getGuard(), targetNode);
		this.rule.destroy();
		rule.select();
	}
}

RuleArrowTool.prototype.showLine = function(x, y) {
	if (this.lineDOM) {
		this.flowchart.getContentPane().removeChild(this.lineDOM);
		this.lineDOM = null;
	}
	if (x && y && this.dom) {
		var x1 = this.rule.getSourceNode().getCenterX();
		var y1 = this.rule.getSourceNode().getCenterY();
		this.lineDOM = DiaFluxUtils.createDottedLine(x1, y1, x+13, y+13, 2, 'red', 5, 100);
		this.flowchart.getContentPane().appendChild(this.lineDOM);
	}
}


