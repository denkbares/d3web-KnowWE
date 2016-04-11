
/**
 * Class for flowchart editor context menu.
 * @author Ralf Oechsner
 * @param name name of the context menu (must be equal to object variable name!)
 * @return context menu object
 */
function ContextMenu(name) {
	
	this.name = name;
	this.noContext = false;
	this.contextSelection = null;
	this.items = [];
	this.left = 0;
	this.top = 0;
	
	// create div for context menu and append it to the body tag (hidden)
	this.divContext = document.createElement("div");
	this.divContext.setAttribute("class", "ContextMenu", 1);
	this.divContext.setAttribute("id", "div" + name, 1);
	this.divList = document.createElement("ul");
	this.divContext.appendChild(this.divList);
	document.body.appendChild(this.divContext);
}

// register select and edit click events for node
CCEvents.addClassListener('contextmenu', 'ContextMenu', 
	function(event) {
		// just prevent browser menu to pop up
		event.stop();
	}
);

/**
 * Closes context menu.
 */
ContextMenu.prototype.close = function() {
	
	this.divContext.style.display = 'none';
}

/**
 * Shows the context menu.
 * @param event browser event
 * @param selection selection on which the context menu is called 
 */
ContextMenu.prototype.show = function(event, selection) {
	
	this.contextSelection = selection;
	
	// menu deactivated
	if (this.noContext)
		return;
	
	// IE is evil and doesn't pass the event object
	if (event == null)
		event = window.event;

	// we assume we have a standards compliant browser, but check if we have IE
	var target = event.target != null ? event.target : event.srcElement;

	// document.body.scrollTop does not work in IE
	var scrollTop = document.body.scrollTop ? document.body.scrollTop :
		document.documentElement.scrollTop;
	var scrollLeft = document.body.scrollLeft ? document.body.scrollLeft :
		document.documentElement.scrollLeft;

	// hide the menu first to avoid an "up-then-over" visual effect
	this.divContext.style.display = 'none';
	this.left = event.clientX + scrollLeft;
	this.divContext.style.left = this.left + 'px';
	this.top = event.clientY + scrollTop;
	this.divContext.style.top = this.top + 'px';
	this.divContext.style.display = 'block';
	return false;
}

/**
 * Adds an item to the context menu.
 * @param description description text of the entry
 * @param command command of the item
 * @param icon icon of the item
 */
ContextMenu.prototype.addItem = function(description, command, icon) {
	
	this.items.push(new ContextItem(description, command, icon));
		
	var li = document.createElement("li");
	li.setAttribute("style", "background-image: url(" + (icon || "") + ");", 1);
	var a = document.createElement("a");
	li.setAttribute("onclick",this.name + ".close(); " + command, 1);
	a.innerHTML = description;
	li.appendChild(a);
	
	this.divList.appendChild(li);	
	
	return li;
}

/**
 * Adds an item to the context menu with an separator above the entry.
 * @param description description text of the entry
 * @param command command of the item
 * @param icon icon of the item
 */
ContextMenu.prototype.addSeparator = function(description, command, icon) {

	this.items.push(new ContextItem(description, command, icon));
	
	var li = document.createElement("li");
	li.setAttribute("class", "topSep", 1);
	li.setAttribute("style", "background-image: url(" + (icon || "") + ");", 1);
	var a = document.createElement("a");
	li.setAttribute("onclick",this.name + ".close(); " + command, 1);
	a.innerHTML = description;
	li.appendChild(a);
	
	this.divList.appendChild(li);
}

/**
 * Disables the context menu.
 */
ContextMenu.prototype.disable = function() {
	this.noContext = true;
	this.close();

	return false;
}

/**
 * Enables the context menu.
 */
ContextMenu.prototype.enable = function() {
	this.Context = false;

	return false;
}

/**
 * Returns the selection on which the context menu is called.
 */
ContextMenu.prototype.getSelection = function() {
	
	return this.contextSelection;
}

/**
 * Returns x coordinate of mouse position when context menu was called.
 */
ContextMenu.prototype.getLeft = function() {
	
	return this.left;
}

/**
 * Returns y coordinate of mouse position when context menu was called.
 */
ContextMenu.prototype.getTop = function() {
	
	return this.top;
}

/**
 * Datatype for context menu items.
 * @param description item description text
 * @param command item command
 * @param icon item icon
 */
function ContextItem(description, command, icon) {

	this.description = description;
	this.command = command;
	this.icon = icon || "";
	this.isSeperator = false;
}

//Events
CCEvents.addClassListener('contextmenu', 'FlowchartGroup', 		
	function(event) {
		this.__flowchart.setSelection(null);
		contextMenuNode.close();
		contextMenuRule.close();
		contextMenuFlowchart.show(event, this.__flowchart.getSelection());
	}
);



CCEvents.addClassListener('contextmenu', 'Node', 
	function(event) {
		// only select node when nothing is selected
		if (!this.__node.flowchart.isSelected(this.__node)) {
			if (this.__node) this.__node.select(DiaFluxUtils.isControlKey(event));
		}
		contextMenuFlowchart.close();
		contextMenuRule.close();
		contextMenuNode.show(event, this.__node);
	}
);



CCEvents.addClassListener('contextmenu', 'Rule', 
	function(event) {
		// only select rule when nothing is selected
		if (!this.__rule.flowchart.isSelected(this.__rule)) {
			if (this.__rule) this.__rule.select(DiaFluxUtils.isControlKey(event));
		}
		contextMenuFlowchart.close();
		contextMenuNode.close();
		contextMenuRule.show(event, this.__rule);
	}
);





//createMenu when dom is ready
document.observe("dom:loaded", function() {

	return; // context menu is currently deactivated

	contextMenuNode = new ContextMenu("contextMenuNode");
	contextMenuNode.addItem("Cut", "theFlowchart.cut();");
	contextMenuNode.addItem("Copy", "theFlowchart.copySelectionToClipboard();");
	contextMenuNode.addItem("Paste", "theFlowchart.pasteFromClipboard();", Flowchart.imagePath + "contextmenu/paste.png");
	contextMenuNode.addItem("Delete", "theFlowchart.trashSelection();", Flowchart.imagePath + "contextmenu/delete.png");
		

	// context menu for the flowchart
	contextMenuFlowchart = new ContextMenu("contextMenuFlowchart");
	contextMenuFlowchart.addItem("New decision node", "FlowEditor.newNode(theFlowchart, contextMenuFlowchart.getLeft(), contextMenuFlowchart.getTop(), \"decision\");", Flowchart.imagePath + "contextmenu/decision.png");
	contextMenuFlowchart.addItem("New start node", "FlowEditor.newNode(theFlowchart, contextMenuFlowchart.getLeft(), contextMenuFlowchart.getTop(), \"start\");", Flowchart.imagePath + "contextmenu/start.png");
	contextMenuFlowchart.addItem("New exit node", "FlowEditor.newNode(theFlowchart, contextMenuFlowchart.getLeft(), contextMenuFlowchart.getTop(), \"exit\");", Flowchart.imagePath + "contextmenu/exit.png");
	contextMenuFlowchart.addItem("New comment node", "FlowEditor.newNode(theFlowchart, contextMenuFlowchart.getLeft(), contextMenuFlowchart.getTop(), \"comment\");", Flowchart.imagePath + "contextmenu/comment.png");
	contextMenuFlowchart.addItem("New snapshot node", "FlowEditor.newNode(theFlowchart, contextMenuFlowchart.getLeft(), contextMenuFlowchart.getTop(), \"snapshot\");", Flowchart.imagePath + "contextmenu/snapshot.png");
	contextMenuFlowchart.addSeparator("Paste", "theFlowchart.pasteFromClipboard();", Flowchart.imagePath + "contextmenu/paste.png");

	// context menu for rules
	contextMenuRule = new ContextMenu("contextMenuRule");
	contextMenuRule.addItem("Insert decision node", "FlowEditor.insertNode(contextMenuRule.getSelection(), contextMenuRule.getLeft(), contextMenuRule.getTop(), \"decision\");", Flowchart.imagePath + "contextmenu/decision.png");
	contextMenuRule.addItem("Insert comment node", "FlowEditor.insertNode(contextMenuRule.getSelection(), contextMenuRule.getLeft(), contextMenuRule.getTop(), \"comment\");", Flowchart.imagePath + "contextmenu/comment.png");
	contextMenuRule.addItem("Insert snapshot node", "FlowEditor.insertNode(contextMenuRule.getSelection(), contextMenuRule.getLeft(), contextMenuRule.getTop(), \"snapshot\");", Flowchart.imagePath + "contextmenu/snapshot.png");
	//TODO 
	//contextMenuRule.addSeparator("Delete", "FlowEditor.removeSelection(contextMenuRule.getSelection());", Flowchart.imagePath + "contextmenu/delete.png");
	

});

