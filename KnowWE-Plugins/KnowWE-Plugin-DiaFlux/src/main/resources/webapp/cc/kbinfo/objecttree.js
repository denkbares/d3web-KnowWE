
function ObjectTree(parent, defaultHTML, roots) {
	this.parent = $(parent);
	this.dom = null;
	this.defaultHTML = defaultHTML ? defaultHTML : "<center><i><p>no objects</i><center>";
	this.roots = roots ? roots : [];
	this.domsByID = {};
	this.cacheListener = function(changedInfoObjects) {
		// rerender newly incoming objects, replacing wait-indicating tree node
		for (var i=0; i<changedInfoObjects.length; i++) {
			var infoObject = changedInfoObjects[i];
			var id = infoObject.getID();
			var oldDoms = this.domsByID[id];
			this.domsByID[id] = [];
			if (oldDoms) for (var k=0; k<oldDoms.length; k++) {
				var oldDom = oldDoms[k];
				if (oldDom.parentNode) {
					// Nur wenn dieser Knoten auch im dom vorhanden ist
					// (auch andere JS-Routinen laden Objekte in den Cache nach)
					var newDom = this.renderTreeItem(infoObject, oldDom.className, oldDom.visible());
					oldDom.parentNode.replaceChild(newDom, oldDom);
					this.domsByID[id].push(newDom);
				}
			}
		}
	}.bind(this);
	this.setVisible(true);
}

ObjectTree.prototype.isVisible = function() {
	return (this.dom != null);
}

ObjectTree.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		KBInfo.addCacheChangeListener(this.cacheListener);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		KBInfo.removeCacheChangeListener(this.cacheListener);
		this.dom.remove();
		this.dom = null;
	}
}

ObjectTree.prototype.setRoots = function(roots) {
	this.roots = roots;
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
}

ObjectTree.prototype.render = function() {
	if (!this.roots) return Builder.build(this.defaultHTML); //TODO remove build

	var dom = Builder.node('div', {
		className: 'ObjectTree',
//		style: "position:relative;"
	});

	this.addChildTreeItems(dom, this.roots, 'rootTree');

	return dom;
}

ObjectTree.prototype.handleToggle = function(parentElement, toggleElement) {
	var expandIt = (toggleElement.className == 'expandButton');
	var parentDom = parentElement;//this.domByID[parentID];
	var parentObj = parentElement.__infoObject; //parentElementKBInfo.lookupInfoObject(parentID);
	if (parentObj) {
		if (expandIt) {
			// expand it
			toggleElement.className = 'collapseButton';
			// first try to make existing subTrees visible
			var hasSubTrees = false;
			var childs = parentDom.childNodes;
			for (var i=childs.length-1; i>=0; i--) {
				if (childs[i].className == 'subTree') {
					childs[i].show();
					hasSubTrees = true;
				}
			}
			// if not, then append the subTrees the first time
			if (!hasSubTrees) {
				var childs = parentObj.getChilds();
				this.addChildTreeItems(parentDom, childs, 'subTree');
			}
		}
		else {
			// collapse it
			toggleElement.className = 'expandButton';
			var childs = parentDom.childNodes;
			for (var i=childs.length-1; i>=0; i--) {
				if (childs[i].className == 'subTree') {
					childs[i].hide();
				}
			}
		}
	}
}

ObjectTree.prototype.addChildTreeItems = function(dom, childIDs, className) {
	var missingIDs = [];
	for (var i=0; i<childIDs.length; i++) {
		var childID = childIDs[i];
		var child = KBInfo.lookupInfoObject(childID);
		var childDom = null;
		if (child) {
			childDom = this.renderTreeItem(child, className, true);
		}
		else {
			missingIDs.push(childID);
			childDom = this.renderWaitItem(childID, className);
		}
		if (this.domsByID[childID]) {
			this.domsByID[childID].push(childDom);
		}
		else {
			this.domsByID[childID] = [childDom];
		}
		dom.appendChild(childDom);
	}
	KBInfo.prepareInfoObject(missingIDs);
}

ObjectTree.prototype.renderTreeItem = function(infoObject, className, isVisible) {
	var isLeaf = (infoObject.getChilds().length == 0);
	var icon = infoObject.getIconURL();
	var childs = [];

	if (!isLeaf) {
		childs.push(Builder.node('span', {
			className: 'expandButton',
			onclick: 'this.parentNode.__objectTree.handleToggle(this.parentNode, this);'
		}));
	}

	var name = ActionPane.insertWordWrapPoints(infoObject.getName());
	var dragItem;
	childs.push(dragItem = Builder.node('span', {
		className: 'item NodePrototype',
		onclick: (!isLeaf ? 'this.parentNode.__objectTree.handleToggle(this.parentNode, this.previousSibling);' : '')
		},
		(icon)
			? [Builder.node('span', {className: 'icon'}, [
					Builder.node('img', {src: icon})
				]), name]
			: [name]
		));

	var dom = Builder.node('div', {
		className: className,
		style: (isVisible ? '' : 'display: none;')
	},
	childs);

	dom.__objectTree = this;
	dom.__infoObject = infoObject;

	if (infoObject.getClassInstance() != KBInfo.Article) {
		dragItem.createNode = function(flowchart, left, top) {
			var d1 = $(dom).cumulativeScrollOffset().top;
			var d2 = $(dom).cumulativeOffset().top;
			var action;
			if (infoObject.getClassInstance() == KBInfo.Solution) {
				var name = IdentifierUtils.quoteIfNeeded(infoObject.getName());
				action = {markup: 'KnOffice', expression: name + ' = P7' };
			}
			else if (infoObject.getClassInstance() == KBInfo.Question && infoObject.isAbstract()) {
				var name = IdentifierUtils.quoteIfNeeded(infoObject.getName());
				action = {markup: 'NOP', expression: name };
			}
			else if (infoObject.getClassInstance() == KBInfo.Flowchart) {
				var startName = (infoObject.getStartNames().length > 0) ? infoObject.getStartNames()[0] : '---';
				startName = IdentifierUtils.quoteIfNeeded(startName);
				var objectName = IdentifierUtils.quoteIfNeeded(infoObject.getName());
				action = {markup: 'KnOffice', expression: 'CALL[' + objectName + '(' + startName + ')]' };
			}
			else {
				var name = IdentifierUtils.quoteIfNeeded(infoObject.getName());
				action = {markup: 'KnOffice', expression: name };
			}
			var model = {
				//id: 'mf'+(mfCounter++),
				position: {left: left, top: top-d2},
				action: action
			};
			EditorInstance.withUndo("Add New Node", function() {
				new Node(flowchart, model).select();
			});
		};
		//TODO: avoid memory leak by remeber the Draggable and remove it on destroy
		new Draggable(dragItem, {
			ghosting: true,
			//scroll: theFlowchart.getContentPane().parentNode.parentNode,
			revert: true,
			reverteffect: ObjectTree.revertEffect,
			starteffect: ObjectTree.startEffect
		});
	}
	return dom;
}

ObjectTree.revertEffect = function(element,  top_offset, left_offset) {
	element = $(element);
    element.makePositioned();
	var x = parseFloat(element.getStyle('left') || '0');
	var y = parseFloat(element.getStyle('top')  || '0');
	element.setStyle({
		position: "", //"relative",
		left: (x - left_offset) + 'px',
		top:  (y - top_offset) + 'px'
	});
}

ObjectTree.startEffect = function(element) {
	element = $(element);
	element.setStyle({position: "fixed"});
}


ObjectTree.prototype.renderWaitItem = function(objectID, className) {
	var name = objectID;
	var pos = name.lastIndexOf('/');
	if (pos >= 0) {
		name = name.substring(pos+1);
	}
	var dom = Builder.node('div', {
		className: className
	},
	[
		Builder.node('span', {className: 'waitIcon'}),
		Builder.node('span', {className: 'wait'}, [name])
	]);
	return dom;
}

