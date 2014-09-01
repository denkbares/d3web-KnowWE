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
 * Currently only 'KnOffice' is supported as markup language.
 * There can be at least only one of "start", "exit" or "action",
 * making the node's type. Later there might be added additional
 * optional node types such as "freeze". It is possible that there
 * is no of these attributes available. Then the node is a decision
 * node and the contained (displayed) node content is extracted from
 * the outgoing rules (maybe later we add an "decision" section).
 *
 * For comparison here an example of the same data structure in KnOffice's
 * xml representation:
 * <node id=no001>
 *        <position top=100 left=50/>
 *        <start>2 min</start>
 *        <exit>done</exit>
 *        <action markup=KnOffice>T_next = now + 2 min</action>
 * </node>
 *
 *
 * The Node stores additional (non-persistant) information outside the node model
 * and keeps them maintained when the node model is changed.
 */
function Node(flowchart, nodeModel) {
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
	// add to parent flowchart
	this.flowchart.addNode(this);
	// and inherit the visibility
	this.setVisible(this.flowchart.isVisible());
}

Node.wrapToolMenu = function(flowname, nodeID, childToWrap) {
	var identifier = {
		pagename : KNOWWE ? (KNOWWE.helper ? KNOWWE.helper.gup('page') : null ) : null,
		flowname : flowname,
		nodeID : nodeID
	};
	return Builder.node('div', {
			style : 'position:relative; display: inline-block'
		}, [Builder.node('div', {
			className : 'toolsMenuDecorator',
			toolMenuIdentifier : JSON.stringify(identifier),
			toolMenuAction : 'FlowchartToolMenuAction',
			style : 'position:absolute; right: -5px'
		}),
			childToWrap
		]
	)
};

Node.prototype.getDOM = function() {
	return this.dom;
};

Node.prototype.isVisible = function() {
	return (this.dom != null);
};

Node.prototype.getLeft = function() {
	return this.nodeModel.position.left;
};

Node.prototype.getTop = function() {
	return this.nodeModel.position.top;
};

Node.prototype.getWidth = function() {
	return this.width;
};

Node.prototype.getHeight = function() {
	return this.height;
};

Node.prototype.getNodeModel = function() {
	return this.nodeModel;
};

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
	var rules = this.getOutgoingRules();
	for (var i = 0; i < rules.length; i++) {
		rules[i].notifyNodeChanged();
	}
};

Node.prototype.getOutgoingRules = function() {
	var result = [];
	var rules = this.flowchart.findRulesForNode(this);
	for (var i = 0; i < rules.length; i++) {
		if (rules[i].getSourceNode() == this) {
			result.push(rules[i]);
		}
	}
	return result;
};

Node.prototype.getIncomingRules = function() {
	var result = [];
	var rules = this.flowchart.findRulesForNode(this);
	for (var i = 0; i < rules.length; i++) {
		if (rules[i].getTargetNode() == this) {
			result.push(rules[i]);
		}
	}
	return result;
};

Node.prototype.moveTo = function(left, top) {
	this.nodeModel.position.left = left;
	this.nodeModel.position.top = top;
	if (this.dom != null) {
		this.dom.style.left = left + "px";
		this.dom.style.top = top + "px";
	}
	if (this.isVisible() && this.arrowTool != null && this.arrowTool.isVisible()) {
		this.arrowTool.setVisible(false);
		this.arrowTool.setVisible(true);
	}
	this.flowchart.router.rerouteNodes([this]);
};

Node.prototype.getPossibleGuards = function() {
	if (!this._possibleGuardCache) {
		this._possibleGuardCache = Guard.createPossibleGuards(this.getNodeModel());
	}
	return this._possibleGuardCache;
};

Node.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.flowchart.getContentPane().appendChild(this.dom);
		this.updateFromView();
		this.createDraggable();
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		if (this.actionPane) {
			this.actionPane.destroy();
			this.actionPane = null;
		}
		this.flowchart.removeFromSelection(this);
		this.stopEdit(); //TODO only defined for nodeEditor
		this.destroyDraggable();
		this.flowchart.getContentPane().removeChild(this.dom);
		this.dom = null;
	}
};


//only implemented for editor
Node.prototype.destroyDraggable = function() {
};

//only implemented for editor
Node.prototype.createDraggable = function() {
};

Node.prototype.updateFromView = function() {
	if (this.getDOM() != null) {
		this.width = this.getDOM().offsetWidth;
		this.height = this.getDOM().offsetHeight;
		this.moveTo(this.getDOM().offsetLeft, this.getDOM().offsetTop);
	}
};


Node.prototype.getCenterX = function() {
	return this.getLeft() + this.width / 2;
};

Node.prototype.getCenterY = function() {
	return this.getTop() + this.height / 2;
};

Node.prototype.getBaseObject = function() {
	var nodeModel = this.getNodeModel();
	if (!nodeModel.action) return null;

	var action = new Action(nodeModel.action.markup, nodeModel.action.expression);
	return KBInfo.lookupInfoObject(action.getInfoObjectName());
};

Node.prototype.render = function() {
	var contentNode;
	var dom = Builder.node('div', {
			id : this.nodeModel.fcid,
			className : 'Node',
			//onClick: "this.__node.select();",
			//onDblClick: "this.__node.edit();",
			style : "left: " + this.getLeft() + "px; top:" + this.getTop() + "px;"
		},
		[
			contentNode = Builder.node('div', {}, [
				Builder.node('div', {className : 'decorator'})]),
			Builder.node('div', {id : this.nodeModel.fcid + '_highlight', className : 'node_highlight', style : 'visibility: hidden; z-index: 0;'})
		]);

	if (this.nodeModel.start) {
		contentNode.className = 'start';
		var startLabel = Builder.node('span', [this.nodeModel.start]);
		startLabel = Node.wrapToolMenu(this.flowchart.name, this.nodeModel.fcid, startLabel);
		contentNode.appendChild(Builder.node('div', {className : 'title'}, [startLabel]));
	}
	else if (this.nodeModel.exit) {
		contentNode.className = 'exit';
		var exitLabel = Builder.node('span', [this.nodeModel.exit]);
		exitLabel = Node.wrapToolMenu(this.flowchart.name, this.nodeModel.fcid, exitLabel);
		contentNode.appendChild(Builder.node('div', {className : 'title'}, [exitLabel]));
	}
	else if (this.nodeModel.comment) {
		contentNode.className = 'comment';
		contentNode.appendChild(Builder.node('div', {className : 'title'}, [this.nodeModel.comment]));
	}
	else if (this.nodeModel.action) {
		var action = new Action(this.nodeModel.action.markup, this.nodeModel.action.expression);
		var infoObject = KBInfo.lookupInfoObject(action.getInfoObjectName());
		contentNode.className =
			action.isDecision() ? 'decision' :
				action.isIndication() ? 'question' :
						infoObject && infoObject.getClassInstance() == KBInfo.Flowchart ? 'flowchart' :
					'action';
		this.actionPane = new ActionPane(contentNode, action,
			function() {
				var rules = this.getOutgoingRules();
				for (var i = 0; i < rules.length; i++) {
					rules[i].notifyNodeChanged(this);
				}
			}.bind(this)
			, this.flowchart.name, this.nodeModel.fcid);
	}
	else if (this.nodeModel.snapshot) {
		contentNode.className = 'snapshot';
		contentNode.appendChild(Builder.node('div', {className : 'title'}, [this.nodeModel.snapshot]));

	}
	else {
		contentNode.className = 'decision';
		contentNode.appendChild(Builder.node('div', {className : 'text'}, ['error: unexpected node model']));
	}

	dom.__node = this;
	return dom;
};


Node.prototype.select = function(multipleSelectionMode) {
	var selected = this.flowchart.isSelected(this);
	// select it 
	// (add/remove to selection in multipleSelectionMode otherwise set as only selection)
	this.flowchart.setSelection(this,
			multipleSelectionMode && !selected,
			multipleSelectionMode && selected);
};

Node.prototype.setSelectionVisible = function(isVisible) {
	if (this.isVisible()) {
		$(this.nodeModel.fcid + '_highlight').style.visibility = isVisible ? 'visible' : 'hidden';
		if (isVisible && this.arrowTool == null) {
			this.arrowTool = new ArrowTool(this);
			this.arrowTool.setVisible(true);
		}
		else if (!isVisible && this.arrowTool != null) {
			this.arrowTool.destroy();
			this.arrowTool = null;
		}
	}
};

Node.prototype.intersects = function(x1, y1, x2, y2) {
	if (x1 instanceof Node) {
		var node = x1;
		x1 = node.getLeft();
		y1 = node.getTop();
		x2 = x1 + node.width;
		y2 = y1 + node.height;
	}
	var xMin = Math.min(x1, x2);
	var xMax = Math.max(x1, x2);
	var yMin = Math.min(y1, y2);
	var yMax = Math.max(y1, y2);
	return (xMin < this.getLeft() + this.width)
		&& (yMin < this.getTop() + this.height)
		&& (xMax > this.getLeft())
		&& (yMax > this.getTop());
};

Node.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	// deselect the item (if selected)
	this.flowchart.removeFromSelection(this);
	// must destroy alle connected rules first
	var rules = this.flowchart.findRulesForNode(this);
	for (var i = 0; i < rules.length; i++) {
		rules[i].destroy();
	}
	// this only works if there is no endeffekt in the draggable
	// because the case that the div has been removed is not
	// considered in the drag&drop framework.
	this.draggable.options.endeffekt = null;
	this.setVisible(false);
	this.flowchart.removeNode(this);
	EditorInstance.snapshot();
};


Node.createFromXML = function(flowchart, xmlDom, pasteOptions) {
	var nodeModel = {
		fcid : pasteOptions.createID(xmlDom.getAttribute('fcid')),
		position : { left : 0, top : 0 },
		start : KBInfo._getNodeValueIfExists(xmlDom, 'start'),
		exit : KBInfo._getNodeValueIfExists(xmlDom, 'exit'),
		comment : KBInfo._getNodeValueIfExists(xmlDom, 'comment'),
		snapshot : KBInfo._getNodeValueIfExists(xmlDom, 'snapshot')
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
			markup : actionDoms[0].getAttribute('markup') || 'KnOffice',
			expression : KBInfo._nodeText(actionDoms[0])
		}
	}

	var decisionDoms = xmlDom.getElementsByTagName('decision');
	if (decisionDoms && decisionDoms.length > 0) {
		nodeModel.action = {
			markup : 'NOP',
			expression : KBInfo._nodeText(decisionDoms[0])
		}
	}


	return new Node(flowchart, nodeModel);
};


