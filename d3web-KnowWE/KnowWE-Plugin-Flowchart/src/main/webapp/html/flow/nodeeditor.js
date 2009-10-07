
function NodeEditor(parent, nodeModel, style, onSuccess) {
	this.parent = $(parent);
	this.dom = null;
	this.style = style;
	this.nodeModel = nodeModel;
	this.onSuccess = onSuccess;
	this.actionEditor = null;
	this.tabPanes = null;
	this.tabItems = null;
	
	this.setVisible(true);
	this.selectTab(nodeModel.start ? 1 : nodeModel.exit ? 2 : 0);
}

// register key listener to handle ok / cancel hot keys 
// (only if not handled by contained components!!!)
CCEvents.addClassListener('keydown', 'NodeEditor',
	function(event) {
		this.__nodeEditor.handleKeyEvent(event);
	}
);

// stop custom mouse click events on this pane
CCEvents.addClassListener('click', 'NodeEditor', function(event) { event.defaultHandler(); });
CCEvents.addClassListener('dblclick', 'NodeEditor', function(event) { event.defaultHandler(); });
CCEvents.addClassListener('mousedown', 'NodeEditor', function(event) { event.defaultHandler(); });
CCEvents.addClassListener('mouseup', 'NodeEditor', function(event) { event.defaultHandler(); });


NodeEditor.prototype.getDOM = function() {
	return this.dom;
}

NodeEditor.prototype.isVisible = function() {
	return (this.dom != null);
}

NodeEditor.prototype.getNodeModel = function() {
	return this.nodeModel;
}


NodeEditor.prototype.handleOk = function() {
	// update node model before closing
	this.nodeModel = {
		id: this.nodeModel.id,
		position: this.nodeModel.position 
	};
	if (this.tabItems[0].className == 'actionTab_selected') {
		// vor dem ok noch sicherstellen, dass 
		// das Selektionsfeld übernommen wurde (blur() hilft hier)
		var select = this.dom.select
		var action = this.actionEditor.getAction();
		this.nodeModel.action = {
			markup: action ? action.getMarkup() : 'NOP',
			expression: action ? action.getExpression() : this.actionEditor.objectSelect.getValue()
		};
	}
	else if (this.tabItems[1].className == 'startTab_selected') {
		this.nodeModel.start = this.tabPanes[1].childNodes[2].value;
	}
	else if (this.tabItems[2].className == 'exitTab_selected') {
		this.nodeModel.exit = this.tabPanes[2].childNodes[2].value;
	}
	else {
		throw "invalid/unexpected tab pane layout";
	}
	this.setVisible(false);
	if (this.onSuccess) {
		this.onSuccess(this);
	}
}

NodeEditor.prototype.handleCancel = function() {
	this.setVisible(false);
}

NodeEditor.prototype.handleKeyEvent = function(e) {
	// if not, we can use the keys to commit/cancel the modal dialog
	switch(e.keyCode){
		case Event.KEY_ESC: 
			this.handleCancel();
			return;
		case Event.KEY_RETURN:
			this.handleOk();
			return;
	}
	//default handling for cursor events
	e.defaultHandler();
}

NodeEditor.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		this.actionEditor = new ActionEditor(
			this.tabPanes[0], 
			(this.nodeModel.action) ? new Action(this.nodeModel.action.markup, this.nodeModel.action.expression) : null,
			null);
		//this.keyFx = this.handleKeyEvent.bindAsEventListener(this);
		//document.observe('keydown', this.keyFx);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		//document.stopObserving('keydown',  this.keyFx);
		this.keyFx = null;
		this.actionEditor.destroy();
		this.actionEditor = null;
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

NodeEditor.prototype.selectTab = function(index) {
	if (!this.tabItems) return;
	if (!this.tabPanes) return;
	for (var i=0; i<this.tabItems.length; i++) {
		var itemBaseClass = this.tabItems[i].className.replace(/_.*$/, '');
		if (i == index) {
			this.tabItems[i].className = itemBaseClass + '_selected';
			this.tabPanes[i].show();
			if (i == 0) {
				var selectors = this.tabPanes[i].select('.ActionEditor');
				if (selectors && selectors.length >= 1) {
					selectors[0].__ActionEditor.focus();
				}			
			}
			else {
				var inputs = this.tabPanes[i].select('input');
				if (inputs && inputs.length >= 1) {
					inputs[0].focus();
					inputs[0].select();
				}
			}
		}
		else {
			this.tabItems[i].className = itemBaseClass;
			this.tabPanes[i].hide();
		}
	}
}

NodeEditor.prototype.render = function() {
	this.tabItems = [
		Builder.node('span', {className: 'actionTab', onclick: 'this.parentNode.parentNode.__nodeEditor.selectTab(0);'}),
		Builder.node('span', {className: 'startTab', onclick: 'this.parentNode.parentNode.__nodeEditor.selectTab(1);'}),
		Builder.node('span', {className: 'exitTab', onclick: 'this.parentNode.parentNode.__nodeEditor.selectTab(2);'})];
	this.tabPanes = [
		this.renderActionPane(),
		this.renderStartPane(),
		this.renderExitPane()];
		
	var dom = Builder.node('div', {
		className: 'NodeEditor',
		style: (this.style) ? this.style : ''
	}, 
	[
		Builder.node('div', {className: 'background'}),
		Builder.node('div', {className: 'tabGroup'}, this.tabItems),
		Builder.node('div', {className: 'paneGroup'}, this.tabPanes),
		Builder.node('div', {className: 'buttonGroup'}, [
			Builder.node('button', {className: 'ok', onclick: 'this.parentNode.parentNode.__nodeEditor.handleOk();'}, ['Ok']),
			Builder.node('button', {className: 'cancel', onclick: 'this.parentNode.parentNode.__nodeEditor.handleCancel();'}, ['Abbrechen'])
			])
	]);
	dom.__nodeEditor = this;
	return dom;
}

NodeEditor.prototype.renderActionPane = function() {
	var dom = Builder.node('div', {
		className: 'actionPane',
		style: 'display: none;'
	}, 
	[
		"Objekt verwenden:",
	]);
	return dom;
}

NodeEditor.prototype.renderStartPane = function() {
	var dom = Builder.node('div', {
		className: 'startPane',
		style: 'display: none;'
	}, 
	[
		'Startknoten:',
		Builder.node('br'),
		Builder.node('input', {
			className: 'value', 
			type: 'text', 
			value: this.nodeModel.start ? this.nodeModel.start: ''
			})
	]);
	return dom;
}

NodeEditor.prototype.renderExitPane = function() {
	var dom = Builder.node('div', {
		className: 'exitPane',
		style: 'display: none;'
	}, 
	[
		'Endknoten:',
		Builder.node('br'),
		Builder.node('input', {
			className: 'value', 
			type: 'text',
			value: this.nodeModel.exit ? this.nodeModel.exit : ''
			})
	]);
	return dom;
}

NodeEditor.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	this.setVisible(false);
}

