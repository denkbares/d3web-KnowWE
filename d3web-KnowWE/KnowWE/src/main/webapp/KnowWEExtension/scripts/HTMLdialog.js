/* global variable to save the actual visibility states of the dialog parts*/
var visibStates = '';
var visibStatesFollows = '';
var visibStatesAnswers = '';

function saveAsXCL(){
	var params = {
		action : 'saveDialogAsCase',
		KWiki_Topic : gup('page'),
		XCLSolution : $('xcl-solution').value
	}
	
    var options = {
		url : getURL( params ),
		response : {
		    ids : [ ],
		    fn : function(){ window.location.reload();}
		}
    }
    new KnowWEAjax( options ).send();
}


function chk_xcl(){
    if($('xcl-save-as')){
        $('xcl-save-as').addEvent( 'click', saveAsXCL);
    } 
}


function answerClicked(answerID, web, namespace, oid, termName) {
		
	var dialog = document.getElementById('dialog');
	var tablerows = dialog.getElementsByTagName('tr');
	

	for(var i = 0; i < tablerows.length; i++){
		var trid = tablerows[i].id;
		
		if(trid.contains('trf')){
			if(trid.contains(oid)){
			    var tr = document.getElementById(trid); 
			    visibStatesFollows = visibStatesFollows + tr.id + ';';
			}
		}
	}	

	/*
	var spans = document.getElementById(oid).getElementsByTagName('span');
	alert(visibStatesAnswers);
	if(visibStatesAnswers.contains(oid)){
		var states = visibStatesAnswers.split(';');
		var newStates = '';
		for(var i=0; i<states.length; i++){
			
			if(states[i].contains(oid)){
				if(states[i].charAt(0)==0){
					states[i].replace('0', '1');
				} else {
					states[i].replace('1', '0');
				}	
			}
			
		}
		
	} else { 
		for(var i=0; i<spans.length; i++){
			if(spans[i].id.contains(answerID)){
				
				if(spans[i].className == 'answerTextActive'){
					visibStatesAnswers = visibStatesAnswers.concat('0' + spans[i].id + ';');
				} else {
					visibStatesAnswers = visibStatesAnswers.concat('1' + spans[i].id + ';');
				}
				
			}
		}
	}
	alert(visibStatesAnswers);*/
	
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
		    fn : function(){showDialogAfterRefresh()}
		}
    }
    new KnowWEAjax( options ).send();
}


function answerActiveClicked(answerID, web, namespace, oid, termName) {
    answerClicked(answerID, web, namespace, oid, termName);
}


/*shows the dialog as it looked like before the page refresh. */
function showDialogAfterRefresh(  ){
   
	SolutionState.update();
        
	// Sichtbarkeit der Fragecontainer wiederherstellen
    var divs = visibStates.split(";");
    for(var i = 0; i < divs.length-1; i++){
        var currentDiv = document.getElementById(divs[i].substring(1));
        var currentTbl = currentDiv.getElementsByTagName('table');
        var currentImg = currentDiv.getElementsByTagName('h4')[0].getElementsByTagName('img');
        var status = visibilityStatus2CSSName(divs[i].charAt(0));
        currentTbl[0].className = status;
        setImage(currentImg[0], status);
    }    
    
    // Sichtbarkeit Folgefragen
    if(visibStatesFollows!=''){
    	var fls = visibStatesFollows.split(";"); 
        for(var i = 0; i < fls.length-1; i++){
        	var currentFollowQ = document.getElementById(fls[i]);            
            currentFollowQ.className = 'trFollow';
        }    
    }
    
    // Markierung der Antworten
    if(visibStatesAnswers!=''){
    	var ans = visibStatesAnswers.split(";"); 
    	for(var i = 0; i<ans.length-1; i++){
    		var currentAnswerSpan = document.getElementById(ans[i].substring(1));
    		if(ans[i].charAt(0)==0){
        		currentAnswerSpan.className = 'fieldcell';
    		} else {
    			currentAnswerSpan.className = 'answerTextActive';
    		}
    	}
    }
    chk_xcl();
}


function numOkClicked(web,namespace,oid,termName,inputid) {
    var inputtext = 'inputTextNotFound';
    
    var dialog = document.getElementById('dialog');
	var tablerows = dialog.getElementsByTagName('tr');

	for(var i = 0; i < tablerows.length; i++){
		var trid = tablerows[i].id;
		
		if(trid.contains('trf')){
			if(trid.contains(oid)){
			    var tr = document.getElementById(trid); 
			    visibStatesFollows = visibStatesFollows + tr.id + ';';
			}
		}
	}	
	
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
			fn : function(){showDialogAfterRefresh()}
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
     setVisibleDialogTablePart(table, imgNode[0]);
}

/* shows a certain element of the dialog mask*/
function showDialogElementTr(ID){
    
    if(ID == null)
        return;
    

    var tableRowId = tr.id;	//trf03
	//alert(tableId);
	
    
    if(tr.className=='hidden'){
    	tr.className='trFollow';
    	
    }
}


function initializeVisibilityStates(){
	 var states = '';
	 var dialog = document.getElementById('dialog');
	 var tables = dialog.getElementsByTagName('table');
	    
	 for(var i = 0; i < tables.length; i++){
	        var parentId = tables[i].parentNode.id;
	       
	        if(tables[i].className == 'hidden'){
	            states = states.concat(0 + parentId);
	        }else{
	            states = states.concat(1 + parentId);
	        }
	        states = states.concat(";");
	 }
	 visibStates = states;
}


/* shows a certain part of the dialog table, i.e. questions/answers of a certain
 * qcontainer, and changes the collapse/expand image */
function setVisibleDialogTablePart(node, imgNode){
	
	   if(!isNotEmpty(node) || !isNotEmpty(imgNode))
	    	return;
	
	   if(visibStates==''){
		   initializeVisibilityStates();
	   }
	   
		var parentid = node.parentNode.id;
	    var tableId = '';
	    tableId = tableId.concat('tbl' + parentid);
	    
	    if(document.getElementById(tableId).className == 'visible'){
	    	var searchstring = '1'+parentid+';';
	    	var newstring = '0'+parentid+';';
			visibStates = visibStates.replace(new RegExp(searchstring), newstring);
	    
	    	document.getElementById(tableId).className = 'hidden';
	    	setImage(imgNode, "hidden");
	    	
	    } else {	
	    	var searchstring = '0'+parentid+';';
			var newstring = '1'+parentid+';';
			visibStates = visibStates.replace(new RegExp(searchstring), newstring);
	    			    	
	    	document.getElementById(tableId).className = 'visible';
	    	setImage(imgNode, "visible");
	    }
}

 
/* sets the link image depending on the current visibility.*/
function setImage(imgNode, status){
    if(status == "hidden"){
        imgNode.setAttribute("src", "KnowWEExtension/images/arrow_right.png");
    }else{
        imgNode.setAttribute("src", "KnowWEExtension/images/arrow_down.png");
    }
}


/* decodes the status information into a valid css attribute name. */
function visibilityStatus2CSSName(status){
    if(status == 1)
        return "visible";
    else
        return "hidden";
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
	
	visibStates = '';
	visibStatesFollows = '';
	visibStatesAnswers = '';
	
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
                el.innerHTML = "Interview";
                el.href = "javascript:removeDialog()";
				chk_xcl();            
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
    el.innerHTML = "Interview";
    el.href = "javascript:insertDialog()";
}


function insertNavButton(){
	if( !$('actionsTop') ) return;
    var li = document.createElement('li');
    li.setAttribute('id', 'moreKnowWE');
    var a = document.createElement('a');
    a.setAttribute('href', 'javascript:insertDialog()');
    //a.setAttribute('class', 'action dialog'); geht im IE nicht !!!
    a.innerHTML = 'Interview';

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
	window.addEvent('domready', function() {
		insertNavButton();
	});   
}


/* show the complete dialog with all sub elements visible*/
/*function showDialog(){
    var dialog = document.getElementById('dialog');
    var dialogDivs = dialog.getElementsByTagName('div');
    var imgNode = dialog.getElementsByTagName('h4')[0].getElementsByTagName('img');
    
    var id;
    for(var i = 0; i < dialogDivs.length; i++){
    	
    	// nur das erste qaset aufgeklappt anzeigen, alle anderen per default erstmal zu
        if(i == 0){
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
    }
    setVisible(dialog, imgNode[0]);
}*/

/* shows a certain element and changes the collapse/expand image */
/*function setVisible(node, imgNode){

    var style = node.style;
 
    
    if(!isNotEmpty(node) || !isNotEmpty(imgNode))
    	return;
    
    
    if (style['display'] == 'inline') {
    	node.removeAttribute('style');
    	node.setAttribute('style', 'display:none');
        document.getElementById(tableId).className = 'hidden';
    	setImage(imgNode, "hidden");
    } else {
    	node.removeAttribute('display');
    	node.setAttribute('style', 'display:inline');
    	setImage(imgNode, "visible");     
    } 
} */

/* creates a string witch encodes the visibility of the div containers.*/
/*function getVisibilityStatusDivs(){
    var status = '';
    var dialog = document.getElementById('dialog');
    var tables = dialog.getElementsByTagName('table');
    
    for(var i = 0; i < tables.length; i++){
        var divID = tables[i].parentNode.id;
       
        if(tables[i].className == 'hidden'){
            status = status.concat(0 + divID);
        }else{
            status = status.concat(1 + divID);
        }
        if(i + 1 < tables.length)
            status = status.concat(";");
    }
    
    return status;
}*/