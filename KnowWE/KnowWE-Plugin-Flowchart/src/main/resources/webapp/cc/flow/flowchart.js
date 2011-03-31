
function Flowchart(parent, id, width, height, idCounter) {
	this.parent = $(parent);
	this.id = id || this.createID('sheet');
	this.width = width;
	this.height = height;
	this.idCounter = idCounter || 0;

	this.nodes = [];
	this.rules = [];
	this.dom = null;
	this.router = new Router(this);
	this.selection = [];
}

Flowchart.imagePath = "cc/image/";

Flowchart.prototype.createID = function(prefix) {
	this.idCounter++;
	var id = '#' + (prefix || "XX") + "_" + this.idCounter;
	while (this.findObject(id)) {
		this.idCounter++;
		id = '#' + (prefix || "XX") + "_" + this.idCounter;
	}
	return id;
}

Flowchart.prototype.setSize = function(width, height) {
	this.width = width;
	this.height = height;
	if (this.dom) {
		var w = Math.ceil(width / 10.0) * 10 + 1;
		var h = Math.ceil(height / 10.0) * 10 + 1;
		var div = this.dom.select('.Flowchart')[0];
		div.style.width = w+'px';
		div.style.height = h+'px';
	}
}

Flowchart.prototype.getContentPane = function() {
	return this.dom.firstChild;
}

Flowchart.prototype.isVisible = function() {
	return (this.dom != null);
}

Flowchart.prototype.focus = function() {
	if (this.isVisible()) {
		this.dom.select('.inputFocus')[0].focus();
	}
}

Flowchart.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		// before showing childs, parent must be visible to enable dragging library
		for (var i=0; i<this.nodes.length; i++) this.nodes[i].setVisible(visible);
		for (var i=0; i<this.rules.length; i++) this.rules[i].setVisible(visible);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.parent.removeChild(this.dom);
		for (var i=0; i<this.nodes.length; i++) this.nodes[i].setVisible(visible);
		for (var i=0; i<this.rules.length; i++) this.rules[i].setVisible(visible);
		this.dom = null;
	}
}

Flowchart.prototype.addNode = function(node) {
	this.nodes.push(node);
}

Flowchart.prototype.removeNode = function(node) {
	this.nodes.remove(node);
}

Flowchart.prototype.findNode = function(id) {
	for (var i=0; i<this.nodes.length; i++) {
		var node = this.nodes[i];
		if (node.getNodeModel().fcid == id) return node;
	}
	return null;
}

Flowchart.prototype.findRule = function(id) {
	for (var i=0; i<this.rules.length; i++) {
		var rule = this.rules[i];
		if (rule.fcid == id) return rule;
	}
	return null;
}


/**
 * Flowchart.isSelected
 * returns if the specified object is currently selected.
 * 
 * @param {Node | Rule} nodeOrRule 
 */
Flowchart.prototype.isSelected = function(nodeOrRule) {
	return this.selection.contains(nodeOrRule);
}

/**
 * Flowchart.setSelection
 * Sets the selection to the specified Node(s) or Rule(s). 
 * This method signals the selected elements to be highlighted. 
 * 
 * @param {Node | Rule | [Node, ..., Rule, ...]} nodeOrRuleOrArray 
 * @param {boolean} addToSelection (default: false)
 */
Flowchart.prototype.setSelection = function(nodeOrRuleOrArray, addToSelection, removeFromSelection) {
	// request Focus
	this.focus();
	// create new and defined selection array 
	// for the items to be selected
	var newSelection;
	if (nodeOrRuleOrArray == null) {
		newSelection = [];
	}
	else if (DiaFluxUtils.isArray(nodeOrRuleOrArray)) {
		newSelection = nodeOrRuleOrArray;
	}
	else {
		newSelection = [nodeOrRuleOrArray];
	}

	if (removeFromSelection) {
		// deselect some nodes
		for (var i=0; i<newSelection.length; i++) {
			this.selection.remove(newSelection[i]);
			newSelection[i].setSelectionVisible(false);
		}
	}
	else {
		// deselect existing selection if a 'total' set action is desired
		if (!addToSelection) {
			for (var i=0; i<this.selection.length; i++) {
				this.selection[i].setSelectionVisible(false);
			}
			this.selection = [];
		}
	
		// otherwise (add or set) select some nodes
		for (var i=0; i<newSelection.length; i++) {
			this.selection.push(newSelection[i]);
			newSelection[i].setSelectionVisible(true);
		}
	}
}

Flowchart.prototype.findObject = function(id) {
	return this.findNode(id) || this.findRule(id);
}

Flowchart.prototype.addRule = function(rule) {
	this.rules.push(rule);
	this.router.rerouteNodes([rule.sourceNode, rule.targetNode]);
}

Flowchart.prototype.removeRule = function(rule) {
	this.rules.remove(rule);
	this.router.rerouteNodes([rule.sourceNode, rule.targetNode]);
}


Flowchart.prototype.render = function() {
	var w = Math.ceil(this.width / 10.0) * 10 + 1;
	var h = Math.ceil(this.height / 10.0) * 10 + 1;

	var contentPane, trashPane;
	var dom = Builder.node('div', {
		id: this.id,
		className: 'FlowchartGroup'
	}, 
	[
		contentPane = Builder.node('div', {
			className: 'Flowchart',
			style: "width: " + w + "px; height:" + h + "px;"
		}),
		Builder.node('div', {style: 'position: absolute; bottom: 0px; left: 0px; width: 0px; height: 0px; overflow: visible;'}, [
			trashPane = Builder.node('div', {className: 'trash'})
		]),
		Builder.node('a', {className: 'inputFocus', href: '#'}) // input focus
	]);
	dom.__flowchart = this;

	this.createDroppables(dom, contentPane, trashPane);

	return dom;
}

// implemented in floweditor.js
Flowchart.prototype.createDroppables = function(dom, contentPane, trashPane) {}



Flowchart.prototype.getContentSize = function() {
	var maxX = this.width;
	var maxY = this.height;
	for (var i=0; i<this.nodes.length; i++) {
		maxX = Math.max(maxX, this.nodes[i].getLeft() + this.nodes[i].getWidth());
		maxY = Math.max(maxY, this.nodes[i].getTop() + this.nodes[i].getHeight());
	}
	return [maxX, maxY];
}

Flowchart.prototype.findRulesForNode = function(node) {
	// TODO: shall be optimized by build an hashtable for each node!!!
	var result = [];
	for (var i=0; i<this.rules.length; i++) {
		var rule = this.rules[i];
		if (rule.sourceNode == node || rule.targetNode == node) {
			result.push(rule);	
		} 
	}
	return result;
}


Flowchart.prototype.addFromXML = function(xmlDom, dx, dy) {
	var pasteOptions = {
		flowchart: this,
		idMap: {},
		allIDs: [],
		translate: {left: dx, top: dy}
	};
	pasteOptions.createID = function(id) {
		if (this.flowchart.findObject(id)) {
			var newID = this.flowchart.createID();
			this.idMap[id] = newID;
			this.allIDs.push(newID);
			return newID;
		}
		else {
			this.allIDs.push(id);
			return id;
		}
	}.bind(pasteOptions);
	pasteOptions.getID = function(id) {
		if(this.idMap[id]) id = this.idMap[id];
		return id;
	}.bind(pasteOptions);

	// nodes
	var nodeDoms = xmlDom.getElementsByTagName('node');
	for (var i=0; i<nodeDoms.length; i++) {
		Node.createFromXML(this, nodeDoms[i], pasteOptions);
	}
	
	// rules
	var ruleDoms = xmlDom.getElementsByTagName('edge');
	for (var i=0; i<ruleDoms.length; i++) {
		Rule.createFromXML(this, ruleDoms[i], pasteOptions);
	}
	
	// select paste objects
	var sel = [];
	for (var i=0; i<pasteOptions.allIDs.length; i++) {
		var item = this.findObject(pasteOptions.allIDs[i]);
		if (item.isVisible()) {
			sel.push(item);
		}
	}
	this.setSelection(sel);
}

Flowchart.createFromXML = function(parent, xmlDom) {
	if (xmlDom.nodeName.toLowerCase() != 'flowchart') {
		return Flowchart.createFromXML(
			parent, 
			xmlDom.getElementsByTagName('flowchart')[0]);
	}
	
	// direkt attributes
	var id = xmlDom.getAttribute('fcid');
	var width = xmlDom.getAttribute('width') || 650;
	var height = xmlDom.getAttribute('height') || 400;
	var name = xmlDom.getAttribute('name');
	var icon = xmlDom.getAttribute('icon');
	var idCounter = xmlDom.getAttribute('idCounter');
	var autostart = xmlDom.getAttribute('autostart');
	
	// create flowchart
	var flowchart = new Flowchart(parent, id, width, height, idCounter);
	flowchart.name = name;
	flowchart.icon = icon;
	
	if (autostart){
		flowchart.autostart = (autostart == "true")
	} else {
		flowchart.autostart = false;
	}
		
	flowchart.addFromXML(xmlDom, 0, 0);
	
	flowchart.setVisible(true);
	return flowchart;
}

Flowchart.prototype.getMaxObjects = function() {
	var maxX = -1;
	var maxY = -1;
	for (var i=0; i<this.nodes.length; i++) {
		maxX = Math.max(maxX, this.nodes[i].getLeft() + this.nodes[i].getWidth());
		maxY = Math.max(maxY, this.nodes[i].getTop() + this.nodes[i].getHeight());
	}
	return [maxX, maxY];
}

Flowchart.prototype.getMinObjects = function() {
	var minX = this.width;
	var minY = this.height;
	for (var i=0; i<this.nodes.length; i++) {
		minX = Math.min(minX, this.nodes[i].getLeft());
		minY = Math.min(minY, this.nodes[i].getTop());
	}
	return [minX, minY];
}




