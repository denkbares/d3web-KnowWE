function answerClicked(answerID, web, namespace, oid, termName) {
	
	var params = {
		action : 'setSingleFinding',
		KWikiWeb : web,
		namespace : namespace,
		ObjectID : oid,
		ValueID : answerID,
		TermName : termName
	}
	
    var options = {
		url : getURL( params ),
		response : {
		    ids : [ 'dialog-panel' ],
		    fn : function(){showDialogAfterRefresh( getVisibilityStatusDivs() )}
		}
    }
    new KnowWEAjax( options ).send();
}

function answerActiveClicked(answerID, web, namespace, oid, termName) {
    answerClicked(answerID, web, namespace, oid, termName);
}

/*shows the dialog as it looked like before the page refresh. */
function showDialogAfterRefresh( visibilityStates ){
    SolutionState.update();
        
    var divs = visibilityStates.split(";");
    
    for(var i = 0; i < divs.length; i++){
        var currentDiv = document.getElementById(divs[i].substring(1));
        var currentTbl = currentDiv.getElementsByTagName('table');
        var currentImg = currentDiv.getElementsByTagName('h4')[0].getElementsByTagName('img');
        var status = visibilityStatus2CSSName(divs[i].charAt(0));
        
        currentTbl[0].style['display'] = status;
        setImage(currentImg[0], status);
    }
    
    var dialog = document.getElementById('dialog');
    var imgNode = dialog.getElementsByTagName('h4')[0].getElementsByTagName('img');
    setVisible(dialog, imgNode[0]);
}

function numOkClicked(web,namespace,oid,termName,inputid) {
    var inputtext = 'inputTextNotFound';
    if(document.getElementById(inputid) != null) {
            inputtext = document.getElementById(inputid).value; 
    }
    sendNumInput(web,namespace,oid,termName,inputtext);
}

function sendNumInput(web,namespace,oid,termName,inputtext) {
	
	var params = {
		action : 'setSingleFinding',
		KWikiWeb : web,
		namespace : namespace,
		ObjectID : oid,
		ValueNum : inputtext,
		TermName : termName
	}
	
	var options = {
		url : getURL( params ),
		response : {
			ids : ['dialog-panel'],
			fn : function(){showDialogAfterRefresh( getVisibilityStatusDivs() )}
		}
	}
	new KnowWEAjax( options ).send();
}

function numInputEntered(event,web,namespace,oid,termName,inputid) {
    if(event.keyCode == 13) {
        var inputtext = 'inputTextNotFound';
        if(document.getElementById(inputid) != null) {
            inputtext = document.getElementById(inputid).value; 
        }
        sendNumInput(web,namespace,oid,termName,inputtext);
    } 
}
    
/* shows a certain element of the dialog mask*/
function showDialogElement(ID){
    
    if(ID == null)
        return;

     var imgNode = document.getElementById(ID).getElementsByTagName('h4')[0].getElementsByTagName('img');     
     var table = document.getElementById(ID).getElementsByTagName('table')[0];   
     setVisible(table, imgNode[0]);
}

/* show the complete dialog with all sub elements visible*/
function showDialog(){
    var dialog = document.getElementById('dialog');
    var dialogDivs = dialog.getElementsByTagName('div');
    var imgNode = dialog.getElementsByTagName('h4')[0].getElementsByTagName('img');
    
    var id;
    for(var i = 0; i < dialogDivs.length; i++){
        if(checkBrowser != -1){
            if(checkClassName(dialogDivs[i]) != -1){
                id = dialogDivs[i].id;
                showDialogElement(id);
            }
        } else if(dialogDivs[i].getAttribute('class') == 'qcontainer'){
            id = dialogDivs[i].getAttribute('id');
            showDialogElement(id);
        }
    }
    setVisible(dialog, imgNode[0]);
}

/* shows a certain element and changes the collapse/expand image */
function setVisible(node, imgNode){

    var style = node.style;

    if(!isNotEmpty(node) || !isNotEmpty(imgNode))
        return;
 
    if(style['display'] == 'inline'){
         style['display'] = 'none';
         setImage(imgNode, "hidden");         
     }else{
         style['display'] = 'inline';
         setImage(imgNode, "visible");
     }
}
/* sets the link image depending on the current visibility.*/
function setImage(imgNode, status){
    if(status == "hidden"){
        imgNode.setAttribute("src", "KnowWEExtension/images/arrow_down.png");
    }else{
        imgNode.setAttribute("src", "KnowWEExtension/images/arrow_up.png");
    }
}

/* creates a string witch encodes the visibility of the div containers.*/
function getVisibilityStatusDivs(){
    var status = '';
    var dialog = document.getElementById('dialog');
    var tables = dialog.getElementsByTagName('table');
    
    for(var i = 0; i < tables.length; i++){
        var divID = tables[i].parentNode.id;
    
        if(tables[i].style['display'] == 'none'){
            status = status.concat(0 + divID);
        }else{
            status = status.concat(1 + divID);
        }
        if(i + 1 < tables.length)
            status = status.concat(";");
    }
    return status;
}
/* decodes the status information into a valid css attribute name. */
function visibilityStatus2CSSName(status){
    if(status == 1)
        return "inline";
    else
        return "none";
}

function checkClassName(node){
    var str = '';  
    for( var i = 0; i < node.attributes.length; i++ ) {
        str = str + " node: " + node.attributes[i].nodeName;
        str = str + " value:" + node.attributes[i].nodeValue + "<br />";
    }
    return str.indexOf("qcontainer");
}

function checkBrowser(){
    return navigagor.appName.indexOf("Explorer");
}


function insertDialog(){
	
	var params = {
		namespace : gup('page'),
		action : 'DialogRenderer'
	}
	var options = {
		url : getURL( params ),
		response : {
			action : 'create',
			fn : function() {    
				var el = getElementsByClass('a', 'dialog', document.getElementById('actionsTop'))[0];
                el.innerHTML = "Dialog schliesen";
                el.href = "javascript:removeDialog()";
            	}
		},
		create : {
			id : 'pagecontent',
			fn : function(){
				    var div = document.createElement('div');
		            div.setAttribute('id', 'dialog-panel');
			           
				    var class_att = document.createAttribute("class");
				    class_att.nodeValue = "panel";
				    div.attributes.setNamedItem(class_att);
				    return div;
			}
		}
	}
	new KnowWEAjax( options ).send();
}

function removeDialog(){
    document.getElementById('dialog-panel').parentNode.removeChild(document.getElementById('dialog-panel'));
    var el = getElementsByClass('a', 'dialog', document.getElementById('actionsTop'))[0];
    el.innerHTML = "Dialog starten";
    el.href = "javascript:insertDialog()";
}
function insertNavButton(){
	if( !$('actionsTop') ) return;
    var li = document.createElement('li');
    li.setAttribute('id', 'moreKnowWE');
    var a = document.createElement('a');
    a.setAttribute('href', 'javascript:insertDialog()');
    //a.setAttribute('class', 'action dialog'); geht im IE nicht !!!
    a.innerHTML = 'Dialog starten';

    var class_att = document.createAttribute("class");
    class_att.nodeValue = "action dialog";
    a.attributes.setNamedItem(class_att);
    
    li.appendChild(a);
    $$('#actionsTop ul')[0].appendChild(li);
}

function getElementsByClass(tag, classID, el){        
    if(tag == null) tag = '*';
    if(el == null) el = document;
    
    var classEls = [];
    var els = el.getElementsByTagName(tag);
    var pattern = new RegExp('(^|\\s)' + classID + '(\\s|$)');
    for(var i = 0; i < els.length; i++){
        if(pattern.test(els[i].className)){
            classEls.push(els[i]);
        }
    }
    return classEls;   
}
if(loadCheck(['Wiki.jsp'])){
	window.addEvent('domready', insertNavButton);   
}