// handles the section selection tree in the renaming tool
var typeTree;
var labelMap = new Object();
	function fillLabelMap(node) {
	if (node instanceof YAHOO.widget.TextNode) {
		if (labelMap[node.label] == undefined) {
			labelMap[node.label] = new Array();
		}
		labelMap[node.label].push(node);
	}
	for (var i = 0; i < node.children.length; i++){
		fillLabelMap(node.children[i]);
	}
}
propagateHighlightEvent = function (node) {
	if (node instanceof YAHOO.widget.TextNode) {
		for (var i = 0; i < labelMap[node.label].length; i++){
			var similarNode = labelMap[node.label][i];
			if(node.highlightState == 1 && similarNode.highlightState != 1) {
				similarNode.highlight();
			}
			if(node.highlightState == 0 && similarNode.highlightState != 0) {
				similarNode.unhighlight();
			}
		}
		if(node.highlightState == 1) {
			for (var j = 0;j < node.children.length;j++) {
				node.children[j].highlight();
			}
		}
		if(node.highlightState == 0) {
			for (var j = 0;j < node.children.length;j++) {
				node.children[j].unhighlight();
			}
		}
	}
}
var treeInit = function() {
	typeTree = new YAHOO.widget.TreeView("typeTree");
	typeTree.subscribe('clickEvent',typeTree.onEventToggleHighlight);
	typeTree.setNodesProperty('propagateHighlightUp',true);
	typeTree.setNodesProperty('propagateHighlightDown',true);
	fillLabelMap(typeTree.getRoot());
	typeTree.subscribe('highlightEvent', propagateHighlightEvent);	
	typeTree.render();
	typeTree.getRoot().highlight();
}
YAHOO.util.Event.onDOMReady(treeInit);

getSelectedSections = function () {
	var selectedNodes = new Array();
	var partialSelectedNodes =  typeTree.getNodesByProperty("highlightState", 1);
	var fullSelectedNodes    =  typeTree.getNodesByProperty("highlightState", 2);
	if (partialSelectedNodes != null) selectedNodes = selectedNodes.concat(partialSelectedNodes);
	if (fullSelectedNodes != null) selectedNodes = selectedNodes.concat(fullSelectedNodes);
	var selectedSectionsSet = new Object();
	for(var i = 0; i < selectedNodes.length; i++) {
		selectedSectionsSet[selectedNodes[i].label] = true;
	}
	var ar = new Array();
	for(var v in selectedSectionsSet) {
		ar.push(v);
	}
	//alert(ar);
	return ar;
}
//childrenHighlighted = function(node) {
//	var yes = false, no = false;
//	if (node.enableHighlight) {
//		for (var i = 0;i < node.children.length;i++) {
//			switch(node.children[i].highlightState) {
//				case 0:
//					no = true;
//					break;
//				case 1:
//					yes = true;
//					break;
//				case 2:
//					yes = no = true;
//					break;
//			}
//		}
//		if (yes && no) {
//		} else if (yes && node.highlightState != 1) {
//			node.highlight();
//		} else if (node.highlightState != 0) {
//			node.unhighlight();
//		}
//		if (node.propagateHighlightUp) {
//			if (node.parent) {
//				childrenHighlighted(node.parent);
//			}
//		}
//	}
//}