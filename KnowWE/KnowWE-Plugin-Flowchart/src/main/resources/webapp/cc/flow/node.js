
/** 
 * test
 * Node is the graphical representation of an node of the flowchart.
 * 
 * It works on an node model, containing the essential persistance data 
 * for the flowchart. For now the node model is a simple structure instead 
 * of a class. The node model is the same structure as in teh flowchart's
 * JSON data structure.
 * 
 * Therefore the node model hat the following outine:
 * {
 * 		id: <string>,
 * 		position: {
 * 			top: <number>,
 * 			left: <number>
 * 		},
 * 		start: <string>,		// only if it is a start node
 * 		exit: <string>,			// only if it is an exit node
 * 		action: {				// only if it is an action node
 * 			markup: <string>,
 * 			expression: <string>,
 * 		}
 * }
 * 
 * Currently only 'KnOffice' ist supported as markup language.
 * There can be at least only one of "start", "exit" or "action",
 * making the node's type. Later there might be added additional
 * optional node types such as "freeze". It is possible that there
 * is no of these attributes available. Then the node is a decision
 * node and the contained (displayed) node content is extracted from
 * the outgoing rules (maybe later we add an "decision" section). 
 * 
 * For comparision here an example of the same data structure in KnOffice's 
 * xml representation: 
 * <node id=no001> 
 * 		<position top=100 left=50/> 
 * 		<start>2 min</start> 
 * 		<exit>done</exit> 
 * 		<action markup=KnOffice>T_next = now + 2 min</action>
 * </node>
 * 
 * 
 * The Node stores additional (non-persiatant) information outside the node model
 * and keeps them maintained when the node model is changed. 
 */
function Node(flowchart, nodeModel /*id, type, title, x, y*/) {
	this.flowchart = flowchart;
	this.nodeModel = null; // see below, set function is used
	this.dom = null;
	this.draggable = null;
	this.nodeEditor = null;
	this.actionPane = null;
	this.width = 0;
	this.height = 0;
	this.text = '';
	this.setNodeModel(nodeModel);
	this.snapManager = new SnapManager(this);
	// add to parent flowchart
	this.flowchart.addNode(this);
	// and inherit the visibility
	this.setVisible(this.flowchart.isVisible());
}

// register select and edit click events for node
CCEvents.addClassListener('click', 'Node', 
	function(event) {
		var ctrlKey = event.ctrlKey;
		var altKey = event.altKey;
		var metaKey = event.metaKey;
		if (this.__node) this.__node.select(ctrlKey | altKey | metaKey);
	}
);
CCEvents.addClassListener('dblclick', 'Node', 
	function(event) {
	//avoids error when dblclick on prototype
		if (this.__node)
			this.__node.edit();
	}
);


Node.prototype.getDOM = function() {
	return this.dom;
}

Node.prototype.isVisible = function() {
	return (this.dom != null);
}

Node.prototype.getLeft = function() {
	return this.nodeModel.position.left;
}

Node.prototype.getTop = function() {
	return this.nodeModel.position.top;
}

Node.prototype.getWidth = function() {
	return this.width;
}

Node.prototype.getHeight = function() {
	return this.height;
}

Node.prototype.getNodeModel = function() {
	return this.nodeModel;
}

Node.prototype.setNodeModel = function(nodeModel) {
	if (!nodeModel.fcid) {
		nodeModel.fcid = this.flowchart.createID('node');
	}
	this.nodeModel = nodeModel;
	this._possibleGuardCache = null;
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
	var rules = this.flowchart.findRulesForNode(this);
	for (var i=0; i<rules.length; i++) {
		if (rules[i].getSourceNode() == this) {
			rules[i].notifyNodeChanged();
		}
	}
}

Node.prototype.getPossibleGuards = function() {
	if (!this._possibleGuardCache) {
		this._possibleGuardCache = Guard.createPossibleGuards(this.getNodeModel());
	}
	return this._possibleGuardCache;
}

Node.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.flowchart.getContentPane().appendChild(this.dom);
		this.updateFromView();
		this.draggable = this.snapManager.createDraggable();
		// make this node a possible target for the arrow tool
		Droppables.add(this.getDOM(), {
			accept: 'ArrowTool', 
			hoverclass: 'Node_hover',
			onDrop: function(draggable, droppable, event) {
				draggable.__arrowTool.createRule(droppable.__node);
			}
		});
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		if (this.actionPane) {
			this.actionPane.destroy();
			this.actionPane = null;
		}
		this.flowchart.removeFromSelection(this);
		Droppables.remove(this.nodeModel.id);
		this.stopEdit();
		this.draggable.destroy();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
		this.draggable = null;
	}
}

Node.prototype.updateFromView = function() {
	if (this.getDOM() != null) {
		this.width = this.getDOM().offsetWidth;
		this.height = this.getDOM().offsetHeight;
		this.moveTo(this.getDOM().offsetLeft, this.getDOM().offsetTop);
	}
}

Node.prototype.moveBy = function(dLeft, dTop) {
	this.moveTo(this.getLeft() + dLeft, this.getTop() + dTop);
}

Node.prototype.moveTo = function(left, top) {
	this.nodeModel.position.left = left;
	this.nodeModel.position.top = top;
	if (this.dom != null) {
		this.dom.style.left = left + "px";
		this.dom.style.top  = top + "px";
	}
	if (this.isVisible() && this.arrowTool != null && this.arrowTool.isVisible()) {
		this.arrowTool.setVisible(false);
		this.arrowTool.setVisible(true);
	}
	this.flowchart.router.rerouteNodes([this]); // TODO: + alle mit Regeln verbundenen Knoten!!!
}

Node.prototype.getCenterX = function() {
	return this.getLeft() + this.width/2;
}

Node.prototype.getCenterY = function() {
	return this.getTop() + this.height/2;
}

Node.prototype.getBaseObject = function() {
	if (!this.action) return null;
	return KBInfo.lookupInfoObject(this.action.getInfoObjectName());
}

Node.prototype.render = function() {
	var contentNode;	
	var dom = Builder.node('div', {
		id: this.nodeModel.fcid,
		className: 'Node',
		//onClick: "this.__node.select();",
		//onDblClick: "this.__node.edit();",
		style: "left: " + this.getLeft() + "px; top:" + this.getTop() + "px;"
	},
	[
		contentNode = Builder.node('div', {}, [
			Builder.node('div', {className: 'decorator', style: ''})]),
		Builder.node('div', {id: this.nodeModel.fcid+'_highlight', className: 'node_highlight', style: 'visibility: hidden; z-index: 0;'})
	]);

	if (this.nodeModel.start){
		contentNode.className = 'start';
		contentNode.appendChild(Builder.node('div', {className: 'title'}, [this.nodeModel.start]));
	}
	else if (this.nodeModel.exit) {
		contentNode.className = 'exit';
		contentNode.appendChild(Builder.node('div', {className: 'title'}, [this.nodeModel.exit]));
	}
	else if (this.nodeModel.comment) {
		contentNode.className = 'comment';
		contentNode.appendChild(Builder.node('div', {className: 'title'}, [this.nodeModel.comment]));
	}
	else if (this.nodeModel.action) {
		var action = new Action(this.nodeModel.action.markup, this.nodeModel.action.expression);
		var infoObject = KBInfo.lookupInfoObject(action.getInfoObjectName());
		contentNode.className = 
			action.isDecision() ? 'decision' : 
			action.isIndication() ? 'question' : 
			infoObject && infoObject.getClassInstance() == KBInfo.Flowchart ? 'flowchart' :
			'action';
		this.actionPane = new ActionPane(contentNode, action, null, 
			function (actionPane) {
				var rules = this.flowchart.findRulesForNode(this);
				for (var i=0; i<rules.length; i++) {
					var rule = rules[i];
					if (rule.getSourceNode() == this) {
						rule.notifyNodeChanged(this);
					}
				}
			}.bind(this)
		);
	}
	else if (this.nodeModel.snapshot) {
		contentNode.className = 'snapshot';
		contentNode.appendChild(Builder.node('div', {className: 'title'}, [this.nodeModel.snapshot]));
		
	}
	else {
		contentNode.className = 'decision';
		contentNode.appendChild(Builder.node('div', {className: 'text'}, ['error: unexpected node model']));
	}

	dom.__node = this;
	return dom;
}



Node.prototype.edit = function() {
	if (this.nodeEditor && this.nodeEditor.isVisible()) {
		// wenn editor bereits sichtbar, dann nichts machen
		return;
	}
	// eventuell vorhandene Artefakte (nach cancel) aufraeumen
	this.stopEdit();
	// und neuen Editor oeffnen
//	var modalBackground = Builder.node('div', {
//		style: 'position: fixed; left: 0; top: 0; width: 100%; height: 100%; ' +
//				'z-index: 1000; background-color:#333333; ' +
//				'opacity: 0.40; filter: alpha(opacity=40);'
//	});
//	var pos = this.getDOM().cumulativeOffset();
//	this.flowchart.getContentPane().parentNode.appendChild(modalBackground);
	this.nodeEditor = new NodeEditor(
		this.flowchart.getContentPane(), 
		this.nodeModel, 
		'position:absolute; ' +
//		'position:fixed; opacity: 1.0; filter: alpha(opacity=100);' +
		'left: ' + (this.getLeft()) + 'px; ' +
		'top: ' + (this.getTop()) + 'px;',
		function(nodeEditor) {
			this.setNodeModel(nodeEditor.getNodeModel());
		}.bind(this)
	);
}

Node.prototype.stopEdit = function() {
	if (this.nodeEditor) {
		this.nodeEditor.destroy();
		this.nodeEditor = null;
	}
}

Node.prototype.select = function(multipleSelectionMode) {
	var selected = this.flowchart.isSelected(this);
	// select it 
	// (add/remove to selection in multipleSelectionMode otherwise set as only selection)
	this.flowchart.setSelection(this, 
		multipleSelectionMode && !selected, 
		multipleSelectionMode && selected);
}

Node.prototype.setSelectionVisible = function(isVisible) {
	if (this.isVisible()) {
		$(this.nodeModel.fcid+'_highlight').style.visibility = isVisible ? 'visible' : 'hidden';
		if (isVisible && this.arrowTool==null) {
			this.arrowTool = new ArrowTool(this);
			this.arrowTool.setVisible(true);
		}
		else if (!isVisible && this.arrowTool!=null) {
			this.arrowTool.destroy();
			this.arrowTool = null;
		}
	}
}

Node.prototype.intersects = function(x1, y1, x2, y2) {
	var xMin = Math.min(x1, x2);
	var xMax = Math.max(x1, x2);
	var yMin = Math.min(y1, y2);
	var yMax = Math.max(y1, y2);
	return (xMin < this.getLeft() + this.width) 
		&& (yMin < this.getTop() + this.height) 
		&& (xMax > this.getLeft()) 
		&& (yMax > this.getTop());
}

Node.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	// deselect the item (if selected)
	this.flowchart.removeFromSelection(this);
	// must destroy alle connected rules first
	var rules = this.flowchart.findRulesForNode(this);
	for (var i=0; i<rules.length; i++) {
		rules[i].destroy();
	}
	// this only works if there is no endeffekt in the draggable
	// because the case that the div has been removed is not
	// considered in the drag&drop framework.
	this.draggable.options.endeffekt = null;
	this.setVisible(false);
	this.flowchart.removeNode(this);
}


Node.prototype.toXML = function() {
	var xml = '\t<node fcid="'+this.nodeModel.fcid+'">\n';
	xml += '\t\t<position left="'+this.getLeft()+'" top="'+this.getTop()+'"></position>\n';
	if (this.nodeModel.start) {
		xml += '\t\t<start>'+this.nodeModel.start.escapeXML()+'</start>\n';
	}
	else if (this.nodeModel.exit) {
		xml += '\t\t<exit>'+this.nodeModel.exit.escapeXML()+'</exit>\n';
	}
	else if (this.nodeModel.comment) {
		xml += '\t\t<comment>'+this.nodeModel.comment.escapeXML()+'</comment>\n';
	}
	else if (this.nodeModel.snapshot) {
		xml += '\t\t<snapshot>'+this.nodeModel.snapshot.escapeXML()+'</snapshot>\n';
	}
	else if (this.nodeModel.action) {
		var action = this.nodeModel.action;

		if (action.markup == 'NOP') {
				xml += '\t\t<decision>' + 
				(action.expression ? action.expression : '') + 
				'</decision>\n';
				
		} else { 
			xml += '\t\t<action markup="' + action.markup + '">' + 
			(action.expression ? action.expression : '') + 
			'</action>\n';
		}
	}
	xml += '\t</node>\n';
	return xml;
}


Node.createFromXML = function(flowchart, xmlDom, pasteOptions) {
	var nodeModel = {
		fcid: pasteOptions.createID(xmlDom.getAttribute('fcid')),
		position: { left: 0, top: 0 },
		start: KBInfo._getNodeValueIfExists(xmlDom, 'start'),
		exit: KBInfo._getNodeValueIfExists(xmlDom, 'exit'),
		comment: KBInfo._getNodeValueIfExists(xmlDom, 'comment'),
		snapshot: KBInfo._getNodeValueIfExists(xmlDom, 'snapshot')
	};
	
	var posDoms = xmlDom.getElementsByTagName('position');
	if (posDoms && posDoms.length > 0) {
		nodeModel.position.left = posDoms[0].getAttribute('left');
		nodeModel.position.top = posDoms[0].getAttribute('top');
	}
	
	nodeModel.position.left = Math.floor(nodeModel.position.left) + pasteOptions.translate.left;
	nodeModel.position.top = Math.floor(nodeModel.position.top) + pasteOptions.translate.top;
	
	var actionDoms = xmlDom.getElementsByTagName('action');
	if (actionDoms && actionDoms.length > 0) {
		nodeModel.action = {
			markup: actionDoms[0].getAttribute('markup') || 'KnOffice',
			expression: KBInfo._nodeText(actionDoms[0])
		}
	}
	
	var decisionDoms = xmlDom.getElementsByTagName('decision');
	if (decisionDoms && decisionDoms.length > 0) {
		nodeModel.action = {
				markup: 'NOP',
				expression: KBInfo._nodeText(decisionDoms[0])
		}
	}
	
	
	
	return new Node(flowchart, nodeModel);
}


// -----
// useful arrow tools to add new rules
// -----

/**
 * ArrowTool
 * creates a new arrow tool for the node. 
 * @param {Node} node 
 */
function ArrowTool(node) {
	this.node = node;
	this.flowchart = node.flowchart;
	this.dom = null;
	this.draggable = null;
}

// register click-listener for that class
CCEvents.addClassListener('click', 'ArrowTool', function(event) {/*NOP, but avoid bubbling*/});

ArrowTool.prototype.getDOM = function() {
	return this.dom;
}

ArrowTool.prototype.isVisible = function() {
	return (this.dom != null);
}

ArrowTool.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.flowchart.getContentPane().appendChild(this.dom);
		this.draggable = this.createDraggable();
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.showLine(null, null);
		this.draggable.destroy();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
		this.draggable = null;
	}
}

ArrowTool.prototype.destroy = function() {
	this.setVisible(false);
}

ArrowTool.prototype.render = function() {
	var dom = Builder.node('div', {
		id: this.node.nodeModel.fcid+'_arrow_tool',
		className: 'ArrowTool',
		style: "position: absolute; overflow:visible; " +
				"width: 0px; height: 0px;" +
				"left: " + (this.node.getLeft() + this.node.getWidth() - 13) + "px; " +
				"top:" + (this.node.getTop() + this.node.getHeight() - 13) + "px;"
	},
	[
		Builder.build('<img src="'+FlowEditor.imagePath+'tool_arrow.gif">')
	]);
	dom.__arrowTool = this;
	return dom;
}

ArrowTool.prototype.createDraggable = function() {
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
		scroll: this.node.flowchart.fcid
	});
	newDrag.__arrowTool = this;
	return newDrag;	
}

ArrowTool.prototype.createRule = function(targetNode) {
	if (this.node != targetNode) {
		var rule = new Rule(
			/*this.flowchart.createID('rule')*/ null, 
			this.node, null, targetNode);
		rule.select();
	}
}

ArrowTool.prototype.showLine = function(x, y) {
	if (this.lineDOM) {
		this.flowchart.getContentPane().removeChild(this.lineDOM);
		this.lineDOM = null;
	}
	if (x && y && this.dom) {
		var x1 = this.node.getCenterX();
		var y1 = this.node.getCenterY();
		this.lineDOM = createDottedLine(x1, y1, x+13, y+13, 2, 'red', 5, 100);
		this.flowchart.getContentPane().appendChild(this.lineDOM);
	}
}

// -----
// handle dragging
// -----

function SnapManager(node) {
	this.node = node;
	this.flowchart = node.flowchart;
	this.hSnaps = [];
	this.vSnaps = [];
	this.snapDistance = 5;
}

SnapManager.prototype.initializeSnaps = function() {
	// empty existing ones
	this.hSnaps = [];
	this.vSnaps = [];
	
	// add snaps for itself (to have the highest priority
	this.hSnaps.push(new Snap(this.node.getWidth()/2, this.node.getCenterX()));
	this.vSnaps.push(new Snap(this.node.getHeight()/2, this.node.getCenterY()));
	
	// add snaps for all rules to have a single straight line
	var rules = this.flowchart.findRulesForNode(this.node);
	for (var i=0; i<rules.length; i++) {
		var rule = rules[i];
		var myAnchor = rule.getSourceAnchor();
		var otherAnchor = rule.getTargetAnchor();
		if (this.node == otherAnchor.node) {
			otherAnchor = rule.getSourceAnchor();
			myAnchor = rule.getTargetAnchor();
		}
		if (otherAnchor.type == 'top' || otherAnchor.type == 'bottom') {
			// horizontal snap
			this.hSnaps.push(new Snap(myAnchor.x - this.node.getLeft(), otherAnchor.x));
		}
		else {
			// vertical snap
			this.vSnaps.push(new Snap(myAnchor.y - this.node.getTop(), otherAnchor.y));
		}
	}
	
	// add snaps for all node centers to this node's center
	for (var i=0; i<this.flowchart.nodes.length; i++) {
		var node = this.flowchart.nodes[i];
		this.hSnaps.push(new Snap(this.node.getWidth()/2, node.getCenterX()));
		this.vSnaps.push(new Snap(this.node.getHeight()/2, node.getCenterY()));
	}
}

SnapManager.prototype.snapIt = function(x, y) {
	// snap to the middle of the object
	var hSnap = this.findSnap(this.hSnaps, x);
	var vSnap = this.findSnap(this.vSnaps, y);
	this.showSnapLines(hSnap, vSnap);
	return [
		hSnap ? hSnap.getNodePosition() : x, 
		vSnap ? vSnap.getNodePosition() : y];
	
}

SnapManager.prototype.findSnap = function(snaps, position) {
	// iterate to find optimal snap with less than 5 pixels (= snapDistance pixels) away
	var bestDist = this.snapDistance+1;
	var bestSnap = null;
	for (var i=0; i<snaps.length; i++) {
		var snap = snaps[i];
		var d = snap.getDistance(position);
		if (d < bestDist) {
			bestDist = d;
			bestSnap = snap;
		}
	}
	return bestSnap;
}

SnapManager.prototype.createDraggable = function() {
	var newDrag = new Draggable(this.node.getDOM(), {
		ghosting: true, starteffect: null, endeffect: null,
		onStart: function(draggable, event) {
			draggable.__snapManager.initializeSnaps();
		},
		onEnd: function(draggable, event) {
			draggable.__snapManager.showSnapLines(null, null);
			draggable.__snapManager.node.updateFromView();
		},
		snap: function (x, y, draggable) {
			return draggable.__snapManager.snapIt(x, y);
		},
		scroll: this.node.flowchart.fcid
	});
	newDrag.__snapManager = this;
	return newDrag;	
}

SnapManager.prototype.showSnapLines = function(hSnap, vSnap) {
	var hid = "dragHelpLine_"+this.flowchart.fcid+"_h";
	var vid = "dragHelpLine_"+this.flowchart.fcid+"_v";
	// remove horizontal line if available and changed
	if (this.vSnapLinePos && (vSnap==null || this.vSnapLinePos != vSnap.snapPosition)) {
		var div = document.getElementById(hid);
		this.flowchart.getContentPane().removeChild(div);
		this.vSnapLinePos = null;
	}
	// remove vertical line if available and changed
	if (this.hSnapLinePos && (hSnap==null || this.hSnapLinePos != hSnap.snapPosition)) {
		var div = document.getElementById(vid);
		this.flowchart.getContentPane().removeChild(div);
		this.hSnapLinePos = null;
	}
	
	var size = this.flowchart.getContentSize();
	// add horizontal line if desired
	if (vSnap && this.vSnapLinePos != vSnap.snapPosition) {
		var line = Builder.node('div', {
			id: hid,
			className: 'h_snapline',
			style: "left: 0px; top: "+vSnap.snapPosition+"px; width: "+(size[0]-2)+"px; height: 1px;"
		});
		this.flowchart.getContentPane().appendChild(line);
		this.vSnapLinePos = vSnap.snapPosition;
	}
	// add vertical line if desired
	if (hSnap && this.hSnapLinePos != hSnap.snapPosition) {
		var line = Builder.node('div', {
			id: vid,
			className: 'v_snapline',
			style: "left: "+hSnap.snapPosition+"px; top: 0px; width: 1px; height: "+(size[1]-2)+"px"
		});
		this.flowchart.getContentPane().appendChild(line);
		this.hSnapLinePos = hSnap.snapPosition;
	}
}


function Snap(offset, snapPosition) {
	this.offset = Math.floor(offset);
	this.snapPosition = Math.floor(snapPosition);
}

Snap.prototype.getDistance = function(position) {
	return Math.abs((position + this.offset) - this.snapPosition);
}

Snap.prototype.getNodePosition = function() {
	return this.snapPosition - this.offset;
}

