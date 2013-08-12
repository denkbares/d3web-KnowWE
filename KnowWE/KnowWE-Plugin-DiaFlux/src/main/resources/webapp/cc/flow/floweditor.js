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
	$('properties.autostart').checked = theFlowchart.autostart;
	$('properties.editName').onchange = this.updateProperties;
	$('properties.autostart').onchange = this.updateProperties;
	
	$('saveClose').observe('click', function(){this.saveFlowchart(true);}.bind(this));
	//$('save').observe('click', function(){this.saveFlowchart(false);}.bind(this));
	$('refresh').observe('click', this.revert);
	
	$('close').observe('click', this.closeEditor);
	$('delete').observe('click', this.deleteFlowchart);
	
		
//	theFlowchart.getContentPane().observe('mousedown', function(event) {FlowEditor.massSelectDown(event);})
//	theFlowchart.getContentPane().observe('mouseup', function(event) {FlowEditor.massSelectUp(event);})
//	theFlowchart.getContentPane().observe('mousemove', function(event) {FlowEditor.massSelectMove(event);})
	Event.observe(window, "resize", function (event) {
		FlowEditor.autoResize();
	});
	
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

	// enable ghost sheet around the existing one
	FlowEditor.borderSpacing = 350;	
	FlowEditor.autoResize();
	theFlowchart.incScroll(FlowEditor.borderSpacing-19, FlowEditor.borderSpacing-19);
}

FlowEditor.arrowMinSpacing = 50;

FlowEditor.autoResize = function() {
	if (!theFlowchart) return;
	if (FlowEditor.avoidAutoResize) return;
	var spacing = FlowEditor.borderSpacing;
	
	// move objects to upper left corner with some spacing left/above
	var area = theFlowchart.getUsedArea();
	var dx = spacing - area.left;
	var dy = spacing - area.top;
	var scroll = theFlowchart.getScroll();
	if (-dx > scroll.x) dx = -scroll.x;
	if (-dy > scroll.y) dy = -scroll.y;
	dx = Math.round(dx / 10.0) * 10;
	dy = Math.round(dy / 10.0) * 10;		
	var moveNodes = Math.abs(dx) > 1 || Math.abs(dy) > 1;
	if (moveNodes) {
		for (var i=0; i<theFlowchart.nodes.length; i++) {
			theFlowchart.nodes[i].moveBy(dx, dy, true);
		}
		theFlowchart.incScroll(dx, dy);
	}
	
	// resize flowchart to add some spacing on bottom right
	var width = $('contents').offsetWidth - 3;
	var height = $('contents').offsetHeight - 3;
	width = Math.max(area.right + spacing + dx, width);
	height = Math.max(area.bottom + spacing + dy, height);
	theFlowchart.setSize(width, height, true);

	if (moveNodes) {
		try {
			FlowEditor.avoidAutoResize = true;
			theFlowchart.router.rerouteAll();			
		} finally {
			FlowEditor.avoidAutoResize = false;
		}
	}
}


FlowEditor.massSelectDown = function(event) {
	
	// prevent this event from taking over newly dragged in nodes
	if (event.target.nodeName === 'INPUT'
			|| event.target.nodeName === 'TEXTAREA' 
				|| event.target.id === 'resizeHandle') {
		return;
	}
	
	FlowEditor.SelectX = event.layerX;
	FlowEditor.SelectY = event.layerY;
	FlowEditor.moveStarted = true;
	FlowEditor.difX = event.clientX - event.layerX;
	FlowEditor.difY = event.clientY - event.layerY;

	event.stop(); 
}

FlowEditor.massSelectMove = function(event) {
	var selectTool = $('select_tool');
	if (selectTool) {
		selectTool.parentNode.removeChild(selectTool);
	}
	if (FlowEditor.moveStarted) {
		var startX = FlowEditor.SelectX;
		var startY = FlowEditor.SelectY;
		var endX = event.clientX - FlowEditor.difX;
		var endY = event.clientY - FlowEditor.difY;
		
		var lineDOM = SelectTool.createSelectionBox(startX, startY, endX, endY, 1, 'grey', 0, 1000);
		theFlowchart.getContentPane().appendChild(lineDOM);
		
		var newSelection = [];
		for ( var i = 0; i < theFlowchart.nodes.length; i++) {
			if (theFlowchart.nodes[i].intersects(startX, startY, endX, endY)) {
				newSelection.push(theFlowchart.nodes[i]);
			}
		}

		var currentSelection = theFlowchart.selection[0];
		if ((currentSelection && !currentSelection.guardEditor) || newSelection.size() !== 0) {
			theFlowchart.setSelection(newSelection, false, false);
		} 
	}
}

FlowEditor.massSelectUp = function(event) {
	
	// prevent this event from taking over newly dragged in nodes
	if (event.target.nodeName === 'INPUT'
		|| event.target.nodeName === 'TEXTAREA') {
		return;
	}
	
	var startX = FlowEditor.SelectX;
	var startY = FlowEditor.SelectY;
	var endX = event.clientX - FlowEditor.difX;
	var endY = event.clientY - FlowEditor.difY;
	
	
	var newSelection = [];
	for ( var i = 0; i < theFlowchart.nodes.length; i++) {
		if (theFlowchart.nodes[i].intersects(startX, startY, endX, endY)) {
			newSelection.push(theFlowchart.nodes[i]);
		}
	}

	var currentSelection = theFlowchart.selection[0];
	if ((currentSelection && !currentSelection.guardEditor) || newSelection.size() !== 0) {
		theFlowchart.setSelection(newSelection, false, false);
	} 
	FlowEditor.moveStarted = false;
	var selectTool = $('select_tool');
	if (selectTool) {
		selectTool.parentNode.removeChild(selectTool);
	}
}

FlowEditor.createActionNode = function(flowchart, left, top, nodeModel) {
	nodeModel.position = {left: left, top: top};
	var node = new Node(flowchart, nodeModel);
	node.select();
	node.edit();
};

FlowEditor.prototype.updateProperties = function(){
	theFlowchart.name = $('properties.editName').value;
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
	//TODO
		FlowEditor.prototype._saveFlowchartText('', true);
	}
}

FlowEditor.prototype.saveFlowchart = function(closeOnSuccess) {
	theFlowchart.setSelection(null, false, false);
	var xml = theFlowchart.toXML(); 
	this._saveFlowchartText(xml, closeOnSuccess);
}

// Overrides empty implementation in flowchart.js
Flowchart.prototype.createDroppables = function(dom, contentPane, trashPane) {

	var trashPane, trashParent =
		Builder.node('div', {className: 'trashParent'}, 
				[
				 trashPane = Builder.node('div', {className: 'trash'})
				 ]);
	$(window.document.body).appendChild(trashParent); 
				 
	// initialize trash to delete nodes and rules
	Droppables.add(trashPane, { 
		accept: ['Node', 'Rule', 'RoutingTool'],
		hoverclass: 'trash_hover',
		onDrop: function(draggable, droppable, event) {
			if (draggable.__node) draggable.__node.destroy();
			if (draggable.__rule) draggable.__rule.destroy();
			if (draggable.__routingTool) draggable.__routingTool.routingPoint.destroy();
		}
	});

	// initialite drag from trees to the pane
	Droppables.add(dom, { 
		accept: ['NodePrototype'],
		hoverclass: 'FlowchartGroup_hover',
		onDrop: function(draggable, droppable, event) {
			var p1 = draggable.cumulativeOffset();
			var p2 = contentPane.cumulativeOffset();
			var scroll = dom.__flowchart.getScroll();
			var x = p1.left - p2.left + scroll.x;
			var y = p1.top  - p2.top + scroll.y;
			draggable.createNode(dom.__flowchart, x, y); // dom.__flowchart is defined above
		}
	});
}

Flowchart.prototype.getScroll = function() {
	var node = this.getContentPane().parentNode.parentNode;
	return { x: node.scrollLeft, y: node.scrollTop };
}

Flowchart.prototype.incScroll = function(sx, sy) {
	var node = this.getContentPane().parentNode.parentNode;
	var x = node.scrollLeft + sx;
	var y = node.scrollTop  + sy;
	node.scrollLeft = x < 0 ? 0 : x;
	node.scrollTop  = y < 0 ? 0 : y;
}



FlowEditor.prototype._saveFlowchartText = function(xml, closeOnSuccess) {
	var changeNote = $('changenote').value;
	
	var url = "KnowWE.jsp";
	new Ajax.Request(url, {
		method: 'post',
		parameters: {
			action: 'SaveFlowchartAction',
			KWiki_Topic: topic,			// article
			TargetNamespace: nodeID,	// KDOM nodeID
			KWikitext: xml,				// content
			KWikiChangeNote : changeNote //change note for new page version
		},
		onSuccess: function(transport) {
			if (window.opener) window.opener.location.reload();
			if (closeOnSuccess) window.close();
			//set Url according to new section id
			if (transport.responseText) {
				var loc = window.location.href;
				loc = loc.replace(/kdomID=[^&]*/,'kdomID=' + transport.responseText);
				window.location.replace(loc);
			}
				
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
		this.__flowchart.setSelection(null, DiaFluxUtils.isControlKey(event));
		if (!event.isRightClick()) {
			contextMenuFlowchart.close();
			contextMenuNode.close();
			contextMenuRule.close();
		}
		// this code circumvents browser bugs
		else {
			contextMenuNode.close();
			contextMenuRule.close();
			contextMenuFlowchart.show(event, this.__flowchart.getSelection());
		}

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

	// BUGFIX: when clipboard is empty CCClipboard is null
	//         therefore this has to be checked to avoid NPE
	if (CCClipboard.fromClipboard() == null)
		return;
	
	var xmlDom = CCClipboard.fromClipboard().parseXML();
	this.addFromXML(xmlDom, 20, 20);
}

Flowchart.prototype.cut = function() {
	this.copySelectionToClipboard();
	this.trashSelection();
}

Flowchart.prototype.trashSelection = function() {
	var sel = this.selection.clone();
	for (var i=0; i<sel.length; i++) {
		var item = sel[i];
		item.destroy();
	}
	this.focus();
	FlowEditor.autoResize();
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
 * Getter for selected Nodes/Rules.
 * @return selected Nodes/Rules
 */
Flowchart.prototype.getSelection = function() {	
	return this.selection;
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

Flowchart.prototype.toXML = function() {
	var area = this.getUsedArea();
	var dx = - area.left + 20;
	var dy = - area.top + 20;
	var xml = '<flowchart' +
			' fcid="'+this.id+'"' +
			(this.name ? ' name="'+this.name.escapeXML()+'"' : '') +
			(this.icon ?' icon="'+this.icon+'"' : '')  +
			' width="'+(area.width + 30)+'"' +
			' height="'+(area.height + 30)+'"' +
			' autostart="'+this.autostart+'"' +
			' idCounter="'+this.idCounter+'">\n\n';
	
	xml += '\t<!-- nodes of the flowchart -->\n';
	for (var i=0; i<this.nodes.length; i++) {
		this.nodes[i].stopEdit();
		xml += this.nodes[i].toXML(dx, dy) + '\n';
	}
	
	xml += '\n';
	xml += '\t<!-- rules of the flowchart -->\n';
	for (var i=0; i<this.rules.length; i++) {
		xml += this.rules[i].toXML(dx, dy) + '\n';
	}

	xml += '</flowchart>'
	return xml;
}
var SelectTool = {}

SelectTool.createSelectionBox = function(x1, y1, x2, y2, pixelSize, pixelColor, spacing, maxDots) {
	var temp;
	if (x2 < x1) {
		temp = x1;
		x1 = x2;
		x2 = temp;
	}
	
	if (y2 < y1) {
		temp = y1;
		y1 = y2;
		y2 = temp;
	}
	
	var cx = x2 - x1;
	var cy = y2 - y1;

	var dotCountX = cx / (spacing + pixelSize);
	var dotCountY = cy / (spacing + pixelSize);
	if (maxDots && dotCountX > maxDots) dotCountX = maxDots;
	if (maxDots && dotCountY > maxDots) dotCountY = maxDots;
	var dx = cx / dotCountX;
	var dy = cy / dotCountY;

	var x = x1;
	var y = y1;
	var dotsHTML = '';
	for (var i = 0; i < dotCountX; i++) {
		dotsHTML += '<div style="position:absolute; overflow:hidden; ' +
		'left:' + Math.ceil(x-pixelSize/2) + 'px; ' +
		'top:' + y + 'px; ' +
		'width:' + pixelSize + 'px; ' +
		'height:' + pixelSize + 'px; ' +
		'background-color: ' + pixelColor + ';"></div>';
		
		x += dx;
	}
	x = x1;
	for (var i = 0; i < dotCountX; i++) {
		dotsHTML += '<div style="position:absolute; overflow:hidden; ' +
		'left:' + Math.ceil(x-pixelSize/2) + 'px; ' +
		'top:' + y2 + 'px; ' +
		'width:' + pixelSize + 'px; ' +
		'height:' + pixelSize + 'px; ' +
		'background-color: ' + pixelColor + ';"></div>';
		
		x += dx;
	}
	
	for (var i = 0; i < dotCountY; i++) {
		dotsHTML += '<div style="position:absolute; overflow:hidden; ' +
		'left:' + x1 + 'px; ' +
		'top:' + Math.ceil(y-pixelSize/2) + 'px; ' +
		'width:' + pixelSize + 'px; ' +
		'height:' + pixelSize + 'px; ' +
		'background-color: ' + pixelColor + ';"></div>';
		
		y += dy;
	}
	y = y1;
	
	for (var i = 0; i < dotCountY; i++) {
		dotsHTML += '<div style="position:absolute; overflow:hidden; ' +
		'left:' + x2 + 'px; ' +
		'top:' + Math.ceil(y-pixelSize/2) + 'px; ' +
		'width:' + pixelSize + 'px; ' +
		'height:' + pixelSize + 'px; ' +
		'background-color: ' + pixelColor + ';"></div>';
		
		y += dy;
	}
	
	
	var div = Builder.node('div', {
		id: 'select_tool',
		style: 'position:absolute; overflow:visible; ' +
		 		'top: 0px; left: 0px; width:1px; height:1px;'
	});
	div.innerHTML = dotsHTML;
	return div;
}



/**
 * Create a new node at the given position in the flowchart.
 * @param flowchart flowchart in which node is inserted
 * @param x x coordinate of node position (relative to flowchart)
 * @param y y coordinate of node position (relative to flowchart)
 */
FlowEditor.newNode = function(flowchart, x, y, type) {
	
	var nodeProt = $(type + '_prototype');
	
	if (!nodeProt) return;
	
	nodeProt.createNode(flowchart, x - flowchart.getLeft(), y - flowchart.getTop());
}


