

function DropDownList(parent, onSelect, renderer, onCancel) {
	this.parent = $(parent);
	this.dom = null;
	this.items = null;
	this.onSelect = onSelect;
	this.onCancel = onCancel;
	this.renderer = renderer ? renderer : function(item, index) {
		return '<div>'+Object.toHTML(item)+'</div>'
	};
	this.selectedIndex = -1;
}

// register key listener for object select to handle dropdownlist well
CCEvents.addClassListener('keydown', 'ObjectSelect', 
	function(event) {
		// find all drop down lists in the ObjectSelect
		var dropDownLists = this.select('.DropDownList');
		if (dropDownLists && dropDownLists.length>0) {
			dropDownLists[0].__dropDownList.handleKeyEvent(event);
		}
		else {
			event.nextHandler();
		}
	}
);

DropDownList.prototype.setDefaultText = function(defaultText) {
	this.defaultText = defaultText;
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
}

DropDownList.prototype.setMaxItems = function(maxCount, maxExceededText) {
	this.maxCount = maxCount;
	this.maxExceededText = maxExceededText;
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
}

DropDownList.prototype.hasItems = function() {
	return this.items && this.items.length > 0;
}

DropDownList.prototype.getItemCount = function() {
	if (!this.items) return 0;
	if (!this.maxCount) return this.items.length;
	return Math.min(this.items.length, this.maxCount);
}

DropDownList.prototype.setItems = function(items) {
	this.items = items;
	if (this.hasItems()) {
		if (this.selectedIndex == -1 || this.selectedIndex >= this.getItemCount()) {
			this.setSelectedIndex(0);
		}
	}
	else {
		this.selectedIndex = -1;
	}
	this.setVisible(false);
	this.setVisible(true);
}
DropDownList.prototype.getDOM = function() {
	return this.dom;
}

DropDownList.prototype.isVisible = function() {
	return (this.dom != null);
}

DropDownList.prototype.handleKeyEvent = function(e) {
	switch(e.keyCode){
		case Event.KEY_ESC: 
			if (this.onCancel) this.onCancel();
			return;
	}	
	
	var hasItems = this.items && this.items.length > 0;
	switch(e.keyCode){
		case Event.KEY_PAGEDOWN: 
			if (hasItems) this.setSelectedIndex(this.getItemCount()-1);
			break;
		case Event.KEY_PAGEUP: 
			if (hasItems) this.setSelectedIndex(0);
			break;
		case Event.KEY_UP:
			if (hasItems) {  
				if (this.selectedIndex-1 >= 0) {
					this.setSelectedIndex(this.selectedIndex-1);
				}
				else {
					if (this.items) this.setSelectedIndex(this.getItemCount()-1);
				}
			}
			break;
		case Event.KEY_DOWN: 
			if (hasItems) {  
				if (this.selectedIndex+1 < this.getItemCount()) {
					this.setSelectedIndex(this.selectedIndex+1);
				}
				else {
					if (this.items) this.setSelectedIndex(0);
				}
			}
			break;
		case Event.KEY_RETURN: 
			if (hasItems) this.selectIt();
			break;
		default:
			e.nextHandler();
			break;
	}
}

DropDownList.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		// IE will fail! Workaround for testing: 
		// document.body.appendChild(this.dom)
		this.parent.appendChild(this.dom);
		//this.keyFx = this.handleKeyEvent.bindAsEventListener(this);
		//document.observe('keydown',  this.keyFx);
		Element.scrollVisible($('contents'), this.dom);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		//document.stopObserving('keydown',  this.keyFx);
		this.keyFx = null;
		this.dom.remove();
		this.dom = null;
	}
}

DropDownList.prototype.render = function() {
	var html = '';
	var count = this.getItemCount();
	if (count > 0) {
		for (var i=0; i<count; i++) {
			html += '<div class="' +
					((this.selectedIndex == i) ? 'DropDownItem_highlight' : 'DropDownItem') +
					'" ' +
					'onmousedown="this.parentNode.__dropDownList.setSelectedIndex('+i+');this.parentNode.__dropDownList.selectIt();" ' +
					'onmouseover="this.parentNode.__dropDownList.setSelectedIndex('+i+');" ' +
					'onmousemove="this.parentNode.__dropDownList.setSelectedIndex('+i+');" ' +
					'>' +
					this.renderer(this.items[i], i) + 
					'</div>';
		}
		if (this.maxCount && this.items.length > count) {
			//append message for more items here
			html += '<div class="DropDownItem_disabled">' + this.maxExceededText + '</div>';
		}
	}
	else {
		html = '<div class="DropDownItem_disabled">'+this.defaultText+'</div>';
	}
	var dom = Builder.node('div', {
		className: "DropDownList",
		style: "position:absolute; left: 0px; top: 0px;"
	});
	dom.innerHTML = html;
	dom.__dropDownList = this;
	return dom;
}

DropDownList.prototype.dispose = function() {
	this.setVisible(false);
}

DropDownList.prototype.selectIt = function() {
	this.onSelect(this.items[this.selectedIndex]);
}

DropDownList.prototype.setSelectedIndex = function(index) {
	this.selectedIndex = index;
	if (this.isVisible()) {
		var nodes = this.dom.childNodes;
		for (var i=0; i<nodes.length; i++) {
			if (nodes[i].className == 'DropDownItem_disabled') continue;
			nodes[i].className = (i == index) ? "DropDownItem_highlight" : "DropDownItem";
		}
	}
}
