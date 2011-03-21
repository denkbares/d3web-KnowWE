if (!FlowEditor){
	FlowEditor = {};
}

function FlowEditor(articleIDs){
	
	// kbinfo initialization
	KBInfo._updateCache($('ajaxKBInfo'));
	KBInfo._updateCache($('articleKBInfo'));
	KBInfo._updateCache($('referredKBInfo'));
	

	// initialize wiki tree tool
	new ObjectTree('objectTree', null, articleIDs);

	theFlowchart = null;
	
}

FlowEditor.prototype.showEditor = function(){
	theFlowchart = Flowchart.createFromXML('contents', $('flowchartSource'));
	theFlowchart.setVisible(true);
	$('properties.editName').value = theFlowchart.name || theFlowchart.id;
	$('properties.editWidth').value = theFlowchart.width;
	$('properties.editHeight').value = theFlowchart.height;
	$('properties.autostart').checked = theFlowchart.autostart;
	$('properties.editName').onchange = this.updateProperties;
	$('properties.editWidth').onchange = this.updateProperties;
	$('properties.editHeight').onchange = this.updateProperties;
	$('properties.autostart').onchange = this.updateProperties;
	
	$('saveClose').observe('click', function(){this.saveFlowchart(true);}.bind(this));
	$('save').observe('click', function(){this.saveFlowchart(false);}.bind(this));
	$('refresh').observe('click', this.revert);
	
	$('close').observe('click', this.closeEditor);
	$('delete').observe('click', this.deleteFlowchart);
	
	var dragOptions = { ghosting: true, revert: true, reverteffect: ObjectTree.revertEffect};

	new Draggable('decision_prototype', dragOptions);
	new Draggable('start_prototype', dragOptions);
	new Draggable('exit_prototype', dragOptions);
	new Draggable('comment_prototype', dragOptions);
	new Draggable('snapshot_prototype', dragOptions);
	
	$('decision_prototype').createNode = function(flowchart, left, top) { FlowEditor.createActionNode(flowchart, left, top, {action: { markup: 'KnOffice', expression: ''}}); };
	$('start_prototype').createNode = function(flowchart, left, top) { FlowEditor.createActionNode(flowchart, left, top, {start: 'Start'}); };
	$('exit_prototype').createNode = function(flowchart, left, top) { FlowEditor.createActionNode(flowchart, left, top, {exit: 'Exit'}); };
	$('comment_prototype').createNode = function(flowchart, left, top) { FlowEditor.createActionNode(flowchart, left, top, {comment: 'Comment'}); };
	$('snapshot_prototype').createNode = function(flowchart, left, top) { FlowEditor.createActionNode(flowchart, left, top, {snapshot: 'Snapshot'}); };
	
	
	
}

FlowEditor.createActionNode = function(flowchart, left, top, nodeModel) {
	nodeModel.position = {left: left, top: top};
	var node = new Node(flowchart, nodeModel);
	node.select();
	node.edit();
};

FlowEditor.prototype.updateProperties = function(){
	theFlowchart.name = $('properties.editName').value;
	theFlowchart.setSize($('properties.editWidth').value, $('properties.editHeight').value);
	theFlowchart.autostart = $('properties.autostart').checked;
}

FlowEditor.prototype.revert = function(){
	var result = confirm('Do you really want to revert your changes?');
	if (result) {
		window.location.reload();
	}
}

FlowEditor.prototype.closeEditor = function(){
	window.close();
}

FlowEditor.prototype.deleteFlowchart = function() {
	var result = confirm('Do you really want to delete the flowchart?');
	if (result) {
		this._saveFlowchartText('', true);
	}
}

FlowEditor.prototype.saveFlowchart = function(closeOnSuccess) {
	theFlowchart.setSelection(null, false, false);
	var xml = theFlowchart.toXML(true); // include preview for us
	this._saveFlowchartText(xml, closeOnSuccess);
}

// Overrides empty implementation in flowchart.js
Flowchart.prototype.createDroppables = function(dom, contentPane, trashPane) {
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
}



FlowEditor.prototype._saveFlowchartText = function(xml, closeOnSuccess) {
	var url = "KnowWE.jsp";
	new Ajax.Request(url, {
		method: 'post',
		parameters: {
			action: 'SaveFlowchartAction',
			KWiki_Topic: topic,			// article
			TargetNamespace: nodeID,	// KDOM nodeID
			KWikitext: xml				// content
		},
		onSuccess: function(transport) {
			if (window.opener) window.opener.location.reload();
			if (closeOnSuccess) window.close();
		},
		onFailure: function() {
			CCMessage.warn(
				'AJAX Error', 
				'Changes could not be saved.');
		},
		onException: function(transport, exception) {
			CCMessage.warn(
				'AJAX Error, Saving most likely failed.',
				exception
				);
		}
	}); 		

	
}





//Flowchart event handlers
//register select click events for flowchart
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
	else if (DiaFluxUtils.isArray(nodeOrRuleOrArray)) {
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


//TODO remove??
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
			' autostart="'+this.autostart+'"' +
			' idCounter="'+this.idCounter+'">\n\n';
	
	xml += '\t<!-- nodes of the flowchart -->\n';
	for (var i=0; i<this.nodes.length; i++) {
		this.nodes[i].stopEdit();
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
				'\t</preview>\n';
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
	if (node.src){
		var srcVal = node.src;

		if (node.src.startsWith('http://')){
		
			
			var path = self.location.href;
			var pos = path.indexOf('.jsp');
			path = path.substring(0,pos);
			
			pos = path.lastIndexOf('/') + 1;
			path = path.substring(0,pos);
		
			srcVal = node.src.substring(path.length, srcVal.length);
		}
		
		html += ' src="' + srcVal + '"';
	}
	// for nodes we have a problem: padding is not taken nto consideration
	// therefore allow width+padding not to be more than parent's width - 2

	if (node.parentNode.hasClassName('Node') && (node.hasClassName('flowchart') || node.hasClassName('action') || node.hasClassName('question') ||  node.hasClassName('decision'))) {
		size.width = Element.getWidth(node.parentNode)-13;
		size.height += 2; 
	}
	if (node.parentNode.hasClassName('Node') && (node.hasClassName('start') || node.hasClassName('exit') || node.hasClassName('comment') || node.hasClassName('snapshot'))) {
		size.width = Element.getWidth(node.parentNode)-13;
		size.height += 2; 
			
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





