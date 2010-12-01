function requestToolsPopupMenu(nodeID, sectionID) {
	//TODO: request menu html by a new action and show it afterwards
	//showToolsPopupMenu(nodeID, ...);
	alert("not implemented yet");
}

function showToolsPopupMenu(nodeID, menuHTML) {
	hideToolsPopupMenu();
	var node = $(nodeID);
	var pos = node.getPosition();
	var w=node.offsetWidth, h=node.offsetHeight;
	var par = new Element('div',{
		'id': 'toolPopupMenuID',
		'styles': {
			'top': pos.y + 'px',
			'left': pos.x + 'px',
			'position': 'absolute'
		},
		'events': {
			'mouseleave': hideToolsPopupMenu
		}
	});
	document.body.appendChild(par);
	par.innerHTML = 
		"<div class='defaultMarkupFrame'>" +
		"<div style='width:"+w+"px;height:"+h+"px;' onclick='hideToolsPopupMenu();'></div>" +
		menuHTML + 
		"</div>";
}

function hideToolsPopupMenu() {
	var old = $('toolPopupMenuID');
	if (old) {
		old.remove();
	}
}