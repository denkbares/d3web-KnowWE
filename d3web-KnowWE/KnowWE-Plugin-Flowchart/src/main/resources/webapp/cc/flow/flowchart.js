
function Flowchart(parent, id, width, height, idCounter) {
	this.parent = $(parent);
	this.nodes = [];
	this.rules = [];
	this.id = id || this.createID('sheet');
	this.width = width;
	this.height = height;
	this.dom = null;
	this.router = new Router(this);
	this.selection = [];
	if (idCounter) {
		this.idCounter = idCounter;
	} else {
		this.idCounter = 0;
	}
}
// register select click events for flowchart
CCEvents.addClassListener('click', 'FlowchartGroup', 
	function(event) {
		this.__flowchart.setSelection(null);
	}
);
CCEvents.addClassListener('keydown', 'FlowchartGroup', 
	function(event) {
		this.__flowchart.handleKeyEvent(event);
	}
);

Flowchart.prototype.handleKeyEvent = function(event) {
	var isHandled = false;
	var ctrlKey = event.ctrlKey;
	switch (event.keyCode) {
		case 65: // [ctrl] + a
			if (ctrlKey) {
				this.setSelection(this.nodes, false);
				//this.setSelection(this.rules, true);
				this.focus();
				isHandled = true;
			}
			break;
		case 88: // [ctrl] + x
			if (ctrlKey) {
				this.copySelectionToClipboard();
				this.trashSelection();
				isHandled = true;
			}
			break;
		case 67: // [ctrl] + c
			if (ctrlKey) {
				this.copySelectionToClipboard();
				isHandled = true;
			}
			break;
		case 86: // [ctrl] + v
			if (ctrlKey) {
				this.pasteFromClipboard();
				isHandled = true;
			}
			break;
		case Event.KEY_LEFT:
			isHandled = this.moveSelection(-10, 0);
			break;
		case Event.KEY_RIGHT:
			isHandled = this.moveSelection(+10, 0);
			break;
		case Event.KEY_UP:
			isHandled = this.moveSelection(0, -10);
			break;
		case Event.KEY_DOWN:
			isHandled = this.moveSelection(0, +10);
			break;
		case Event.KEY_DELETE:
		case Event.KEY_BACKSPACE:
			this.trashSelection();
			isHandled = true;
			break;
	}
	// only changes nodes using keys
	if (!isHandled) event.nextHandler();
}

Flowchart.prototype.copySelectionToClipboard = function() {
	var sel = this.selection.clone();
	// add all rinterconnecting rules to clipboard as well
	for (var i=0; i<this.rules.length; i++) {
		var rule = this.rules[i];
		if (sel.contains(rule)) continue;
		if (sel.contains(rule.getSourceNode()) || sel.contains(rule.getTargetNode())) {
			sel.push(rule);
		}
	}
	var result = '';
	for (var i=0; i<sel.length; i++) {
		result += sel[i].toXML();
	}
	CCClipboard.toClipboard(result);
	this.focus();
}

Flowchart.prototype.pasteFromClipboard = function() {
	//alert("paste not implemented yet:\n\n"+CCClipboard.fromClipboard());
	var xmlDom = CCClipboard.fromClipboard().parseXML();
	this.addFromXML(xmlDom, 20, 20);
}

Flowchart.prototype.trashSelection = function() {
	var sel = this.selection.clone();
	for (var i=0; i<sel.length; i++) {
		var item = sel[i];
		item.destroy();
	}
	this.focus();
}

Flowchart.prototype.moveSelection = function(dx, dy) {
	var isHandled = false;
	for (var i=0; i<this.selection.length; i++) {
		var item = this.selection[i];
		if (item.moveBy) {
			item.moveBy(dx, dy);
			isHandled = true;
		}
	}
	this.focus();
	return isHandled;
}

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

	// initialize trash to delete nodes and rules
	Droppables.add(trashPane, { 
		accept: ['Node', 'Rule'],
		hoverclass: 'trash_hover',
		onDrop: function(draggable, droppable, event) {
			if (draggable.__node) draggable.__node.destroy();
			if (draggable.__rule) draggable.__rule.destroy();
		}
	});

	// initialite drag from trees to the pane
	Droppables.add(dom, { 
		accept: ['NodePrototype'],
		hoverclass: 'FlowchartGroup_hover',
		onDrop: function(draggable, droppable, event) {
			var p1 = draggable.cumulativeOffset();
			var p2 = contentPane.cumulativeOffset();
			var x = p1.left - p2.left;
			var y = p1.top  - p2.top;
			draggable.createNode(dom.__flowchart, x, y); // dom.__flowchart is defined above
		}
	});

	return dom;
}


Flowchart.prototype.getHSnaps = function() {
	var result = [];
	for (var i=0; i<this.nodes.length; i++) {
		result.push(this.nodes[i].x + this.nodes[i].getWidth()/2);
	}
	return result;
}

Flowchart.prototype.getVSnaps = function() {
	var result = [];
	for (var i=0; i<this.nodes.length; i++) {
		result.push(this.nodes[i].y + this.nodes[i].getHeight()/2);
	}
	return result;
}

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
	else if (Object.isArray(nodeOrRuleOrArray)) {
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

/**
 * Flowchart.removeFromSelection
 * Removes the specified Node(s) or Rule(s) from the current selection. 
 * This method signals the deselected elements to be de-highlighted.
 * 
 * @param {Node | Rule | [Node, ..., Rule, ...]} nodeOrRuleOrArray 
 */
Flowchart.prototype.removeFromSelection = function(nodeOrRuleOrArray) {
	// create new and defined selection array 
	// for the items to be selected
	var items;
	if (nodeOrRuleOrArray == null) {
		items = [];
	}
	else if (Object.isArray(nodeOrRuleOrArray)) {
		items = nodeOrRuleOrArray;
	}
	else {
		items = [nodeOrRuleOrArray];
	}
	
	for (var i=0; i<items.length; i++) {
		items[i].setSelectionVisible(false);
		this.selection.remove(items[i]);
	}
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

Flowchart.prototype.selectAt = function(x, y, addToSelection) {
	for (var i=0; i<this.nodes.length; i++) {
		if (this.nodes[i].intersects(x, y, x, y)) {
			this.setSelection(this.nodes[i], addToSelection);
			return;
		}
	}
	for (var i=0; i<this.rules.length; i++) {
		if (this.nodes[i].intersects(x-3, y-3, x+3, y+3)) {
			this.setSelection(this.nodes[i], addToSelection);
			return;
		}
	}
	if (!addToSelection) {
		setSelection([], false);
	}
}

Flowchart.prototype.toXML = function(includePreview) {
	var xml = '<flowchart' +
			' fcid="'+this.id+'"' +
			(this.name ? ' name="'+this.name.escapeXML()+'"' : '') +
			(this.icon ?' icon="'+this.icon+'"' : '')  +
			' width="'+this.width+'"' +
			' height="'+this.height+'"' +
			' idCounter="'+this.idCounter+'">\n\n';
	
	xml += '\t<!-- nodes of the flowchart -->\n';
	for (var i=0; i<this.nodes.length; i++) {
		xml += this.nodes[i].toXML() + '\n';
	}
	
	xml += '\n';
	xml += '\t<!-- rules of the flowchart -->\n';
	for (var i=0; i<this.rules.length; i++) {
		xml += this.rules[i].toXML() + '\n';
	}

	if (this.dom && includePreview) {
		xml += '\t<preview mimetype="text/html">\n' +
				'\t\t<![CDATA[\n' +
				this.toPreviewHTML(this.dom.select('.Flowchart')[0]) + '\n' +
				'\t\t]]>\n' +
				'\t</preview>';
	}

	xml += '</flowchart>'
	return xml;
}

Flowchart.prototype.toPreviewHTML = function(node) {
	//return node.innerHTML;
	if (node.nodeName == '#text') return node.data;
	if (node.style.display == 'none') return '';
	if (node.style.visibility == 'hidden') return '';
	var size = Element.getDimensions(node);
	var html = '<' + node.nodeName; 
	if (node.className)	html += ' class="' + node.className + '"';
	if (node.id) html += ' id="' + node.id + '"';
	if (node.src) html += ' src="' + node.src + '"';
	// for nodes we have a problem: padding is not taken nto consideration
	// therefore allow width+padding not to be more than parent's width - 2

	if (node.parentNode.hasClassName('Node') && (node.hasClassName('start') || node.hasClassName('exit') || node.hasClassName('flowchart') || node.hasClassName('action') || node.hasClassName('question'))) {
		size.width = Element.getWidth(node.parentNode)-14;
		size.height += 2; 
	}
	if (node.parentNode.hasClassName('Node') && (node.hasClassName('start') || node.hasClassName('exit'))) {
		size.width += 2;
	}
	// for nodes we want to have the fixed size in addition to the style
	var attributes = ['position', 'display', 'visibility', 'left', 'right', 'top', 'bottom', 'overflow'];
	var style = ''
	for (var i=0; i<attributes.length; i++) {
		var value = node.style[attributes[i]];
		if (value) style += attributes[i] + ': ' + value + ';';
	}
	html += ' style="' + style + ' width: ' + size.width + 'px; height: ' + size.height + 'px;"' + '>';
	var childs = node.childNodes;
	for (var i=0; i<childs.length; i++) {
		html += this.toPreviewHTML(childs[i]);
	}
	html += '</' + node.nodeName + '>';
	return html;
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
	var width = xmlDom.getAttribute('width') | 650;
	var height = xmlDom.getAttribute('height') | 400;
	var name = xmlDom.getAttribute('name');
	var icon = xmlDom.getAttribute('icon');
	var idCounter = xmlDom.getAttribute('idCounter');

	
	// create flowchart
	var flowchart = new Flowchart(parent, id, width, height, idCounter);
	flowchart.name = name;
	flowchart.icon = icon;
	flowchart.idCounter = idCounter;
	
	flowchart.addFromXML(xmlDom, 0, 0);
	
	flowchart.setVisible(true);
	return flowchart;
}
