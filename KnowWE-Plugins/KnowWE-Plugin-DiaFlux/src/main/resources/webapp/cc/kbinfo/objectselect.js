
function ObjectSelect(parent, kbInfoClasses, value, changeListener) {
	this.parent = $(parent);
	this.dom = null;
	this.element_editField = null;
	this.element_dropDownListParent = null;
	this.value = value ? value : '';
	this.kbInfoClasses = kbInfoClasses;
	this.matchedItem = null;
	this.changeListener = changeListener;
	this.suggestionFx = function(value) {
		if (value && !value.blank()) {
			var phrases = value.strip().split(' ');
			var regexps = [];
			for (var i=0; i<phrases.length; i++) {
				regexps.push(new RegExp(DiaFluxUtils.escapeRegex(phrases[i]), 'i'));
			}
			return KBInfo.findInfoObjects(function(item) {
				// if a class of objects is given, return if class does not match
				if (this.kbInfoClasses && this.kbInfoClasses.length>0 && !this.kbInfoClasses.contains(item.getClassInstance())) return false;
				// otherwise return whether item matches all search phrases
				var name = item.getName();
				for  (var i=0; i<regexps.length; i++) {
					// return false if one phrase does not match
					if (! name.match(regexps[i])) return false;
				}
				// and true if all phrases have matched
				return true;
			}.bind(this), value, 10);
		}
		else {
			return null;
		}
	}.bind(this);
	this.items = this.suggestionFx(this.value);
	this.handleBlur();
	this.cacheListener = function(changedInfoObjects) {this.handleCacheChange(changedInfoObjects);}.bind(this);
	KBInfo.addCacheChangeListener(this.cacheListener);
}

// register key listener for object select to handle key events well
CCEvents.addClassListener('keydown', 'ObjectSelect', 
	function(event) {
		// find all drop down lists in the ObjectSelect
		this.__objectSelector.handleKeyEvent(event);
	}
);


ObjectSelect.prototype.handleKeyEvent = function(e) {
	if (!this.dropDownList && (e.keyCode >= 41 && e.keyCode <= 127)) {
		this.startEdit();
		// and also accept first key as entered key
		e.nextHandler();
		return;
	}
	// if event was not handled ==> bubble!
	e.nextHandler();
}



ObjectSelect.prototype.destroy = function() {
	KBInfo.removeCacheChangeListener(this.cacheListener);
	this.stopEdit();
	this.dom.remove();
	this.dom = null;
}

ObjectSelect.prototype.handleCacheChange = function(changedInfoObjects) {
	if (this.dropDownList) {
		this.updateSelector();
	}
	else {
		this.items = this.suggestionFx(this.value);

		//TODO disabled: test for disappearing editor when inserting newly created object into cache
		//sideeffects unclear!!!
		//this.handleBlur();
	}
}

ObjectSelect.prototype.getEditField = function() {
	return this.element_editField;
}

ObjectSelect.prototype.getDropDownListParent = function() {
	//alert(this.dom.firstChild);
	//alert(this.dom.firstChild.nextSibling);
	return this.element_dropDownListParent;
}

ObjectSelect.prototype.getMatchedItem = function() {
	return this.matchedItem;
}

ObjectSelect.prototype.getValue = function() {
	return this.value;
}

ObjectSelect.prototype.setDom = function(newDom) {
	this.parent.innerHTML = "";
	this.dom = newDom;
	if (this.dom) this.parent.appendChild(this.dom);
}

ObjectSelect.prototype.startEdit = function() {
	this.valueBeforeEditing = this.value;
	this.matchedItemBeforeEditing = this.matchedItem;
	this.setDom(this.renderEditField());
	var osThis = this;
	this.inputObserver = new PeriodicalExecuter(function() {osThis.checkInputChanged();},0.02);
	this.getEditField().focus();
	this.getEditField().select();
}

ObjectSelect.prototype.cancelEdit = function() {
	this.element_editField.value = this.valueBeforeEditing;
	this.checkInputChanged();
	this.handleBlur();
}

ObjectSelect.prototype.stopEdit = function() {
	if (this.inputObserver) {
		this.inputObserver.stop();
		this.inputObserver = null;
	}
	if (this.dropDownList) {
		this.dropDownList.dispose();
		this.dropDownList = null;
	}
	this.setDom(this.renderResultDiv());
	if (this.changeListener) {
		this.changeListener(this);
	}
}

ObjectSelect.prototype.focus = function() {
	if (!this.dom) return;
	var items = this.dom.select('a');
	if (items.length == 0) return;
	try {
		// may fail in IE, if component is not visible (e.g. hidden parent)
		items[0].focus();
	}
	catch(e) {
		// so handle gracefully
		showMessage(e);
	}
}

ObjectSelect.prototype.valueSelected = function(item) {
	this.matchedItem = item;
	this.getEditField().value = this.value = item.getName();
	this.stopEdit();
	// we have to select the result div, 
	// because otherwise we loose the input focus 
	// and cannot react to key events
	(function() {this.focus()}).bind(this).defer();
}

ObjectSelect.prototype.handleBlur = function() {
	this.matchedItem = null;
	if (this.value && this.items) {
		this.value = this.value.strip();
		var key = this.value.toLowerCase();
		for (var i=0; i<this.items.length; i++) {
			if (this.items[i].getName().toLowerCase() == key) {
				this.matchedItem = this.items[i];
				break;
			}
		}
	}
	this.stopEdit();
}

ObjectSelect.prototype.checkInputChanged = function() {
	var v = this.getEditField().value;
	if (v != this.value) {
		this.value = v;
		this.ajaxSearchRequest();
		this.updateSelector();
	}
}

ObjectSelect.prototype.ajaxSearchRequest = function() {
	KBInfo.searchInfoObject(this.value, this.kbInfoClasses, 11);
}

ObjectSelect.prototype.renderListItem = function(item, index) {
	var text = item.getName().escapeHTML();
	var desc = item.getDescription();
	var icon = item.getIconURL();
	if (this.value && !this.value.blank()) {
		var phrases = this.value.strip().split(' ');
		for (var i=0; i<phrases.length; i++) {
			if (phrases[i].length == 0) continue;
			text = text.gsub(new RegExp(DiaFluxUtils.escapeRegex(phrases[i]), 'i'), '<em>#{0}</em>');
		}
	}
	if (icon) {
		text = '<div style="position:relative;padding-left:20px;">' +
				'<span class=icon><img src="'+icon+'"></img></span>' +
				text +
				'</div>';
	}
	if (desc && !desc.empty()) {
		text += '<div><font size=-2>'+desc+'</font></div>';
	}
	return '<div>'+text+'</div>';
}

ObjectSelect.prototype.renderEditField = function() {
	var childs = [
		Builder.node('input', {
			type: 'text',
			value: this.value,
			onfocus: 'this.parentNode.__objectSelector.updateSelector();',
			onblur: 'this.parentNode.__objectSelector.handleBlur();'
		}),
		Builder.node('div', {
			style: 'position:relative; width: 0px; heigth: 0px; overflow:visible;'
		})
	];
	
	this.element_editField = childs[0];
	this.element_dropDownListParent = childs[1];
	
	var dom = Builder.node('span', {
		className: 'ObjectSelect'
	}, childs);
	
	dom.__objectSelector = this;
	return dom;
}

ObjectSelect.prototype.renderResultDiv = function() {
	var childs = [];
	var icon = this.matchedItem ? this.matchedItem.getIconURL() : KBInfo.imagePath+'no-object.gif';
	if (icon) {
		childs.push(Builder.node('span', {
			className: 'icon'
		}, 
		[
			Builder.node('img', {src: icon})
		]));
	}
	childs.push(
		Builder.node('a', {href: '#'}, [
			(this.value && !this.value.blank()) ? this.value : '---'
		]
	));
	childs.push(Builder.node('span', {
		className: 'editbutton'
	}));

	var dom = Builder.node('span', {
		className: 'ObjectSelect'
	}, 
	[
		Builder.node('span', {
			className: (this.matchedItem ? 'match' : 'nomatch'),
			style: (icon ? 'padding-left:20px; ' : ''), 
			onclick: 'this.parentNode.__objectSelector.startEdit();'
		}, childs)
	]);
	
	dom.__objectSelector = this;
	return dom;
}


ObjectSelect.prototype.updateSelector = function() {
	if (!this.dropDownList) {
		this.dropDownList = new DropDownList(
			this.getDropDownListParent(), 
			this.valueSelected.bind(this), 
			this.renderListItem.bind(this),
			this.cancelEdit.bind(this));
		this.dropDownList.setMaxItems(10, 'more...');
		this.dropDownList.setDefaultText('Type in the target object of this node - no matches yet.');
	}
	this.items = this.suggestionFx(this.value);
	this.dropDownList.setItems(this.items);
	//showMessage(this.items);
	Element.scrollVisible($('contents'), this.dom.parentNode.parentNode.parentNode);
}

