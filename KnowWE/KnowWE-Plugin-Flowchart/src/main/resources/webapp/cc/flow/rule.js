
function Rule(id, sourceNode, guard, targetNode) {
	this.flowchart = sourceNode.flowchart;
	this.fcid = id || this.flowchart.createID('rule');
	this.sourceNode = sourceNode;
	this.guard = guard;
	this.guardPane = null;
	this.guardEditor = null;
	this.targetNode = targetNode;
	this.dom = null;
	this.coordinates = [];
	
	// add to parent flowchart
	this.flowchart.addRule(this);
	
	// and inherit the visibility
	this.setVisible(this.flowchart.isVisible());
}


Rule.prototype.getDOM = function() {
	return this.dom;
}

Rule.prototype.isVisible = function() {
	return (this.dom != null);
}

Rule.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		var selected = this.flowchart.isSelected(this);
		this.dom = this.render(selected);
		this.flowchart.getContentPane().appendChild(this.dom);
		this.createDraggable();
		this.setGuardVisible(!selected, selected);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.setGuardVisible(false, false);
		this.destroyDraggable();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
	}
}


Rule.prototype.render = function(selected) {
		var highlightDom, selectorDom;
		
		var ruleDom = Builder.node('div', {
			id: this.fcid,
			className: 'Rule'
		},[highlightDom = Builder.node('div', {className :'rule_highlight'}), selectorDom = Builder.node('div', {className :'rule_selector'})]);
		
		for (var i = 0; i < this.coordinates.length - 1; i++) {
			var x1 = this.coordinates[i][0];
			var y1 = this.coordinates[i][1];
			var x2 = this.coordinates[i+1][0];
			var y2 = this.coordinates[i+1][1];
			var x, y, w, h, clazz, arrow;
			
			if (x1 == x2) { // vertical line
				x = x1;
				y = Math.min(y1, y2);
				w = 1;
				h = Math.abs(y1 - y2)+1;
				clazz = "v_line";
				arrow = (y1 > y2) ? "arrow_up" : "arrow_down";
			}
			else { // horizontal line
				x = Math.min(x1, x2);
				y = y1;
				w = Math.abs(x1 - x2)+1;
				h = 1;
				clazz = "h_line";
				arrow = (x1 > x2) ? "arrow_left" : "arrow_right";
			}
			
			if (i + 2 < this.coordinates.length)
				arrow = "no_arrow"; 
			
			var segmentDom = Builder.node('div', {
				className: clazz,
				style : 'left: ' + x + 'px; top: ' + y + 'px; width: ' + w + 'px; height: ' + h + 'px;'
			}, [
			   
			    Builder.node('div', {className: arrow})
			  ]);
			ruleDom.appendChild(segmentDom);
			
			selectorDom.appendChild( Builder.node('div', {className: 'rule_selector', style: 'position: absolute; overflow:hidden; ' +
				'left: ' + (x-3) + 'px; ' +
				'top: ' + (y-3) + 'px; ' +
				'width: ' + (w+6) + 'px; ' +
				'height: ' + (h+6) + 'px;'})); //.addEvent('click', function(e){this.select(); e.stop();}.bind(this)),
			highlightDom.appendChild(Builder.node('div', {className: clazz + '_highlight', style : '' +
				'left: ' + (x-1) + 'px; ' +
				'top: ' + (y-1) + 'px; ' +
				'width: ' + (w+2) + 'px; ' +
				'height: ' + (h+2) + 'px;'}));
			
			
			
		}
	
		if (this.sourceAnchor && this.coordinates.length > 0) {
			var rect = this.sourceAnchor.getGuardPosition();
			
			var guardStyle = 'position:absolute;';
				
			if (rect.top) guardStyle += ' top: ' + rect.top + 'px; ';
			if (rect.bottom) guardStyle += ' bottom: ' + rect.bottom + 'px; ';
			if (rect.left) guardStyle += ' left: ' + rect.left + 'px; ';
			if (rect.right) guardStyle += ' right: ' + rect.right + 'px; ';
			if (rect.width) guardStyle += ' max-width: ' + rect.width + 'px; ';
			if (rect.height) guardStyle += ' max-height: ' + rect.height + 'px; ';
			
			var guardDom = Builder.node('div', {
				style: 'position:absolute; overflow: visible; ' +
					'left: ' + this.coordinates[0][0] + 'px; top: ' + this.coordinates[0][1] + 'px; width: 0px; height: 0px;'
			}, [
			    this.guardRoot = Builder.node('div', {
			    	className: 'guard', 
			    	style: guardStyle
			    	})
			    ]);
			
			ruleDom.appendChild(guardDom);
			
			
			

		}
		ruleDom.appendChild(highlightDom);
		ruleDom.appendChild(selectorDom);
		ruleDom.__rule = this;
		return ruleDom;
}

// only implemented in editor
Rule.prototype.createDraggable = function() {}

// only implemented in editor
Rule.prototype.destroyDraggable = function() {}



Rule.prototype.intersects = function(x1, y1, x2, y2) {
	var xMin = Math.min(x1, x2);
	var xMax = Math.max(x1, x2);
	var yMin = Math.min(y1, y2);
	var yMax = Math.max(y1, y2);
	for (var i=0; i<this.coordinates.length-1; i++) {
		var lx1 = this.coordinates[i][0];
		var ly1 = this.coordinates[i][1];
		var lx2 = this.coordinates[i+1][0];
		var ly2 = this.coordinates[i+1][1];
		var lxMin = Math.min(lx1, lx2);
		var lxMax = Math.max(lx1, lx2);
		var lyMin = Math.min(ly1, ly2);
		var lyMax = Math.max(ly1, ly2);
		if ((xMin < lxMax) && (yMin < lyMax) && (xMax > lxMin) && (yMax > lyMin)) {
			return true;
		}
	}
}


Rule.prototype.getGuard = function() {
	return this.guard;
}


Rule.prototype.select = function(multipleSelectionMode) {
	var selected = this.flowchart.isSelected(this);
	// select it 
	// (add/remove to selection in multipleSelectionMode otherwise set as only selection)
	this.flowchart.setSelection(this, 
		multipleSelectionMode && !selected, 
		multipleSelectionMode && selected);
}

Rule.prototype.setGuard = function(guard) {
	this.guard = guard;
	if (this.guardPane) {
		this.setGuardVisible(true, false);		
	}
}

Rule.prototype.notifyNodeChanged = function(node) {
	var visible = this.isVisible();
	this.setVisible(false);
	if (this.guardPane) {
		this.guardPane.checkProblems(this);
	}
	if (this.guard && !DiaFluxUtils.isString(this.guard)) {
		this.guard.lookupDisplayHTML(this.sourceNode.getPossibleGuards());
	}
	this.setVisible(visible);
}

Rule.prototype.getGuardRoot = function() {
	return (this.dom ? this.guardRoot : null);
}

Rule.prototype.setSelectionVisible = function(isSelected) {
	if (!this.isVisible()) return;
	var hightlight = this.dom.select('.rule_highlight')[0];
	if (isSelected) {
		// show highlight
		// TODO: avoid using id as dom id(s)
		hightlight.style.visibility = 'visible';
		this.setGuardVisible(false, true);
	}
	else {
		hightlight.style.visibility = 'hidden';
		this.setGuardVisible(true, false);
	}
}

Rule.prototype.setGuardVisible = function(paneVisible, editorVisible) {
	if (this.guardPane) {
		this.guardPane.destroy();
		this.guardPane = null;
	}
	if (this.guardEditor) {
		this.guardEditor.destroy();
		this.guardEditor = null;
	}
	if (paneVisible) {
		this.guardPane = new GuardPane(this.getGuardRoot(), this.guard, this);
	}
	if (editorVisible) {
		this.guardEditor = new GuardEditor(
			this.getGuardRoot(), 
			this.guard, 
			this.sourceNode.getPossibleGuards(),
			this.handleGuardSelected.bind(this));
	}
}

Rule.prototype.handleGuardSelected = function(guard) {
	this.setGuard(guard);
}

Rule.prototype.setSourceAnchor = function(anchor) {
	this.sourceAnchor = anchor;
}

Rule.prototype.getSourceAnchor = function() {
	return this.sourceAnchor;
}

Rule.prototype.getSourceNode = function() {
	return this.sourceNode;
}

Rule.prototype.setTargetAnchor = function(anchor) {
	this.targetAnchor = anchor;
}

Rule.prototype.getTargetAnchor = function() {
	return this.targetAnchor;
}

Rule.prototype.getTargetNode = function() {
	return this.targetNode;
}

Rule.prototype.getAnchor = function(node) {
	return (this.targetNode == node) ? this.targetAnchor : this.sourceAnchor;
}

Rule.prototype.setCoordinates = function(coordinates) {
	// check if coordinates have changed
	if (this.coordinates.equals(coordinates)) return;
	this.coordinates = coordinates;
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
}

Rule.prototype.getOtherNode = function(node) {
	var result = (this.targetNode == node) ? this.sourceNode : this.targetNode;
	return result;
}


Rule.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	// deselect the item (if selected)
	this.flowchart.removeFromSelection(this);
	// this only works if there is no endeffekt in the draggable
	// because the case that the div has been removed is not
	// considered in the drag&drop framework.
	this.draggable.options.endeffekt = null;
	this.setVisible(false);
	this.flowchart.removeRule(this);
}

Rule.createFromXML = function(flowchart, xmlDom, pasteOptions) {
	var id = pasteOptions.createID(xmlDom.getAttribute('fcid'));
	var sourceNodeID = pasteOptions.getID(KBInfo._getNodeValueIfExists(xmlDom, 'origin'));
	var targetNodeID = pasteOptions.getID(KBInfo._getNodeValueIfExists(xmlDom, 'target'));
	var sourceNode = flowchart.findNode(sourceNodeID);
	var targetNode = flowchart.findNode(targetNodeID);
	
	if (!sourceNode) return null;
	if (!targetNode) return null;
	
	var guard = null;
	var guardDoms = xmlDom.getElementsByTagName('guard');
	
	var guard = Guard.createFromXML(flowchart, guardDoms, pasteOptions, sourceNode);
	
	return new Rule(id, sourceNode, guard, targetNode);
}


// ----
// Anchor for having rules anchored to the nodes
// ----

function Anchor(node, x, y, type, slide) {
	this.node = node;
	this.x = x;
	this.y = y;
	this.type = type;
	this.slide = slide;
}


Anchor.prototype.getGuardPosition = function() {
	if (this.type == 'top') {
		return { left: 3, bottom: 4, width: 50 };
	}
	else if (this.type == 'bottom') {
		return { left: 3, top: 2, width: 50 };
	}
	else if (this.type == 'left') {
		return { right: 5, bottom: 0, height: 20 };
	}
	else {
		return { left: 7, bottom: 0, height: 20 };
	}
}
