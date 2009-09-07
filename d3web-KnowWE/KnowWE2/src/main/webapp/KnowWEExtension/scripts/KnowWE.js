/*
 * A "class" for handling Ajax requests. 
 */
function KnowWEAjax( options ) {
	var http = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');
	var oDefault = {
		method : 'get',
		url : 'KnowWE.jsp',
        headers : {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'text/javascript, text/html, application/xml, text/xml, */*'
        },
		fn : handleResponse,
        encoding : 'utf-8',
        urlEncoded : true,		
		async : true,
		response : {
		    ids : [],
		    action : 'insert' /* replace|insert|update|none|create */,
		    fn : false	
		},
		create : {
			id : '',
			fn : false
		}
	}; 
	oDefault = enrich( options, oDefault );
	var headers = new Hash(oDefault.headers);
	      
	/* get max.length 512 byte, check and set post if bigger*/
	if( oDefault.url.getBytes() > 512)
	    oDefault.url = 'post';
		
    /* sends the request */
	this.send = function() {
		if( !http ) return;
		
        if (oDefault.urlEncoded){
            var encoding = (oDefault.encoding) ? '; charset=' + oDefault.encoding : '';
            headers.set('Content-type', 'application/x-www-form-urlencoded' + encoding);
        }		
		
		http.open( oDefault.method.toUpperCase(), oDefault.url, oDefault.async );
        
        headers.each(function(v,k){
            http.setRequestHeader(k, v);
        });
		
        /*http.setRequestHeader( 'Content-Type', oDefault.header );
        http.setRequestHeader( 'Content-length', oDefault.url.length );
        http.setRequestHeader( 'Connection', 'close' );*/
        http.onreadystatechange = handleResponse;
        http.send(oDefault.method);
	}
	
	/* 
	 * Handles the response from the Ajax request. If the Ajax request ended 
	 * without errors the action defined in oDefault.response.action is executed.
	 */
	function handleResponse() {
		if ((http.readyState == 4) && (http.status == 200)) {	
			var ids = oDefault.response.ids;
			var action = oDefault.response.action;
			switch ( action ) {
				case 'insert':
				    var max = ids.length;
				    for ( var i = 0; i < max; i++ ) {
						    $(ids[i]).innerHTML = http.responseText;
				    }
					break;
				case 'replace':
				    replace( ids, http.responseText )
				    break;
				case 'create':
				    if( oDefault.create ){
				    	var el = oDefault.create.fn.call();
				    	el.innerHTML = http.responseText;
				        $( oDefault.create.id).insertBefore( el, $( oDefault.create.id ).getChildren()[0]);
				    } 
				    break;
				default:
					break;
			}
			if( !oDefault.response.fn ) return;
			oDefault.response.fn.call();
		}  
	}
}


var kwikiEvent;
    
    function kwikiOnLoad() {
        //getSymptomGT('null');
        //getDiagnosisGT('null');
        //updateSolutions();
        //updateDialogs();
        //window.setTimeout("getSymptomGT('null')", 500);
        //window.setTimeout("getDiagnosisGT('null')", 500);
        //window.setTimeout("kwiki_poll()", 500);
    }

/*
 * 
 */
var SolutionState = 
{
	update : function() 
    {
    	var params = {
    		renderer : 'KWiki_dpsSolutions',
    		KWikiWeb : 'default_web'
    	}
        SolutionState.execute( getURL( params ) );
    },
    clear : function()
    {
    	var params = {
    		action : 'KWiki_dpsClear',
    		KWikiWeb : 'default_web'
    	}
        SolutionState.execute( getURL( params ) );
    },
    findings : function()
    {
    	var params = {
    		renderer : 'KWiki_userFindings',
    		KWikiWeb : 'default_web'
    	}
        kwiki_window( getURL(params) );
        /*SolutionState.execute( url );*/
    },
    execute : function( url )
    {
    	var id = 'sstate-result';
		var options = {
			url : url,
		    response : {
			    ids : [ id ]
			}
    	}
        if( $('sstate-result') ) {
            new KnowWEAjax( options ).send();
        }
    }
}

/*
 * Handles in view editable tables
 */
var KnowWETable = {
    map : "",	
    init : function(){
    	this.map = new Hash();
        if( $$('.table-edit') )
        {
            var elements = $$(".table-edit input[type=submit]");
            for(var i = 0; i < elements.length; i++)
            {
                elements[i].addEvent( 'click', KnowWETable.onSave );
            }
            var elements = $$('.table-edit-node');
            for(var i = 0; i < elements.length; i++)
            {
                elements[i].addEvent( 'change', KnowWETable.onChange );
            }
        }
    },
    onChange : function() {
        KnowWETable.map.set(this.id, this.name + "-" + this.value);
    },
    onSave : function(){
        var id = this.id;
    	var namespace = '';
        KnowWETable.map.each(function(value, key){
            namespace += key + "-" + value + "::\n";
        });
        namespace = namespace.substring(0, namespace.lastIndexOf('::'));

    	var params = {
    		action : 'UpdateTableKDOMNodes',
    		TargetNamespace : namespace,
    		KWiki_Topic : gup('page')
    	}
    	var options = {
        	url : getURL ( params ),
        	response : {
        		action : 'none',
        		fn : function(){QuickEdit.enable(id, null);}
        	}
        }
        new KnowWEAjax( options ).send();
    }
}

var QuickEdit = {
	enable : function( nodeID, fn ){
		var params = {
			action : 'SetQuickEditFlagAction',
			TargetNamespace : nodeID,
			KWiki_Topic : gup('page')
		}	
		
		var options = {
			url : getURL( params ),
		    response : {
		    	action : 'replace',
			    ids : [nodeID],
			    fn : fn
			}
		}
		new KnowWEAjax( options ).send();
	},
	doTable : function( nodeID ){
	    QuickEdit.enable( nodeID, function(){KnowWETable.init()} );	
	}
}




function doKbGenerating( jarfile ) {
	var params = {
		renderer : 'GenerateKBRenderer',
		NewKBName : $( jarfile ).value,
		AttachmentName : jarfile
	}
	
	var options = {
		url : getURL( params ),
		response : {
			ids : ['GeneratingInfo']
		}
	}
	new KnowWEAjax( options ).send();
}


function setQuickEditFlag( nodeID , topic ) {
        try {  
         
            kBUpload_xmlhttp = window.XMLHttpRequest 
                ? new XMLHttpRequest()  
                : new ActiveXObject("Microsoft.XMLHTTP");
            kBUpload_xmlhttp.onreadystatechange = setQuickEditFlagDone; 
            var kBUpload_poll_url = "KnowWE.jsp?action=SetQuickEditFlagAction&TargetNamespace="+nodeID+"&KWiki_Topic="+topic;
            kBUpload_xmlhttp.open("Get", kBUpload_poll_url); 
            kBUpload_xmlhttp.send(null);
        } catch (e) {alert(e);}
    
}
    
function setQuickEditFlagDone() {
	document.location.reload();
}
    
    function cellChanged(nodeID,topic) {
        if(document.getElementById("editCell"+nodeID) != null) {
                var el = document.getElementById("editCell"+nodeID);
                var selectedOption = el.options[el.selectedIndex].value; 
                 try {  
         
            kBUpload_xmlhttp = window.XMLHttpRequest 
                ? new XMLHttpRequest()  
                : new ActiveXObject("Microsoft.XMLHTTP");
            kBUpload_xmlhttp.onreadystatechange = replacedNodeContent;  
            var kBUpload_poll_url = "KnowWE.jsp?action=ReplaceKDOMNodeAction&TargetNamespace="+nodeID+"&KWikitext="+selectedOption+"&KWiki_Topic="+topic;
            kBUpload_xmlhttp.open("Get", kBUpload_poll_url); 
            kBUpload_xmlhttp.send(null);
        } catch (e) {alert(e);}
            }
    
    }
    
    function replacedNodeContent() {
        //alert(kBUpload_xmlhttp.responseText);
    }
    
	function doTiRexToXCL(topicname) {
	
		var params = {
			renderer : 'TirexToXCLRenderer',
			TopicForXCL : topicname
		}
	
		var options = {
			url : getURL( params ),
			response : {
				ids : ['GeneratingTiRexToXCLInfo']
			}
		}
		new KnowWEAjax( options ).send();
	}
    
    function highlightNode(node,topic) {
    	
    	var params = {
    		action : 'HighlightNodeAction',
    		Kwiki_Topic : topic,
    		KWikiJumpId : node
    	}
    	
    	var options = {
    		url : getURL( params ),
    		response : {
    			action : 'update',
    			ids : [],// test if it works without ids
    			fn : (function() {
    					// get the uniqueMarker marking the last marked element
    					var curMark = document.getElementById("uniqueMarker");
    					
    					// remove the last marker
    					if (curMark != null) {
    						curMark.id = "";
    						curMark.style.backgroundColor = "";
    					}
    					
    					// set the new Marker
			    	 	var element = document.getElementById(node);
			    	 	if (element != null) {
			    	 		element.firstChild.style.backgroundColor = "yellow";
			    	 		element.firstChild.id = "uniqueMarker";	
			    	 	}		    	 	
					 })()
    		}
    	}
    	new KnowWEAjax( options ).send();
    }
//        try {  
//            tiRexToXCL_xmlhttp = window.XMLHttpRequest 
//                ? new XMLHttpRequest()  
//                : new ActiveXObject("Microsoft.XMLHTTP");
//            tiRexToXCL_xmlhttp.onreadystatechange = nodeHighlightRendererSet;
//            var tiRexToXCL_poll_url = "KnowWE.jsp?action=HighlightNodeAction&KWiki_Topic="+topic+"&KWikiJumpId="+node;
//            tiRexToXCL_xmlhttp.open("Get", tiRexToXCL_poll_url); 
//            tiRexToXCL_xmlhttp.send(null);
//        } catch (e) {
//        	alert(e);
//       	}
//    }
//    
//    function nodeHighlightRendererSet() {
//        if ((tiRexToXCL_xmlhttp.readyState == 4) && (tiRexToXCL_xmlhttp.status == 200)) {
//           document.location.reload();
//           if(document.getElementById("uniqueMarker") != null) {
//                //document.getElementById("uniqueMarker").scrollIntoView(true);
//            }
//
//        } 
//    }
    
function showSolutions() {
 document.getElementById('KnowWESolutions').style.visibility = 'visible';
 document.getElementById('KnowWEDialogs').style.visibility = 'hidden';
 //document.getElementById('solutionsButton').style.backgroundColor = '#E2DCC8';
 //document.getElementById('dialogsButton').style.backgroundColor = '#FFFFFF';
}

function showDialogs() {
 document.getElementById('KnowWESolutions').style.visibility = 'hidden';
 document.getElementById('KnowWEDialogs').style.visibility = 'visible';
 //document.getElementById('solutionsButton').style.backgroundColor = '#FFFFFF';
 //document.getElementById('dialogsButton').style.backgroundColor = '#E2DCC8';
}

function doShowHideOptionsMenu() {
 if(document.getElementById("kwikiOptionsMenu").style.visibility == "visible") {
 doHideOptionsMenu();
 } else if(document.getElementById("kwikiOptionsMenu").style.visibility == "hidden") {
 doShowOptionsMenu();
 }
}

function kwiki_window(url) {
    // 420 x 500 
    var screenWidth = (window.screen.width/2) - (210 + 10);
    var screenHeight = (window.screen.height/2) - (250 + 50);
    newWindow = window.open(url, url, "height=420,width=520,left="+screenWidth + ",top="+screenHeight + ",screenX="+screenWidth + ", screenY="+screenHeight+",resizable=yes,toolbar=no,menubar=no,scrollbars=yes,location=no,status=yes,dependent=yes");
    newWindow.focus();
}

function kwiki_call(url) {
 try {
 kwiki_xmlhttp = window.XMLHttpRequest
 ? new XMLHttpRequest()
 : new ActiveXObject("Microsoft.XMLHTTP");
 kwiki_xmlhttp.open("GET", url);
 kwiki_xmlhttp.send(null);
 SolutionState.update();
 updateDialogs();
 } catch (e) {}
}

function doHideOptionsMenu() {
 document.getElementById("kwikiOptionsMenu").style.visibility="hidden";
}

function doShowOptionsMenu() {
 document.getElementById("kwikiOptionsMenu").style.visibility="visible";
}

function updateDialogs() {
    var url = 'KnowWE.jsp?renderer=KWiki_dpsDialogs&KWikiWeb=default_web';
    KnowWEAjax.id = 'KnowWEDialogs';
    KnowWEAjax.send(url, KnowWEAjax.insert, true);
} 

function kwiki_sessions(linkAction, event) {
        if(!event) {
            event = window.event;
        }
        try {  
            kwikiEvent = event;
            kwiki_xmlhttp = window.XMLHttpRequest 
                ? new XMLHttpRequest()  
                : new ActiveXObject("Microsoft.XMLHTTP");
            kwiki_xmlhttp.onreadystatechange = kwiki_sessions_triggered;  
            var kwiki_poll_url = "KnowWE.jsp?renderer=KWiki_sessionChooser&KWikiLinkAction="+linkAction+"&KWikiWeb=default_web";
            kwiki_xmlhttp.open("GET", kwiki_poll_url); 
            kwiki_xmlhttp.send(null); 
        } catch (e) {alert(e);} 
    } 
             
    function kwiki_sessions_triggered() { 
        if ((kwiki_xmlhttp.readyState == 4) && (kwiki_xmlhttp.status == 200)) {  
            dummy = document.getElementById("kwikiSessions");
            dummy.innerHTML = kwiki_xmlhttp.responseText;
            setCorrectPositionAndShow(dummy, kwikiEvent);
            setTimeout("hideSessionChooser()", 5000);
        } 
    } 

// Ajax events admin panel
function doReInit() {  
        
        try {  
            xmlhttp = window.XMLHttpRequest 
                ? new XMLHttpRequest()  
                : new ActiveXObject("Microsoft.XMLHTTP");
            xmlhttp.onreadystatechange = reInitTrigger;  
            var kwiki_poll_url = "KnowWE.jsp?renderer=KWiki_ReInitWebTermsRenderer&KWikiWeb=default_web";
            xmlhttp.open("GET", kwiki_poll_url); 
            xmlhttp.send(null); 
        } catch (e) {alert(e);} 
    }  
function reInitTrigger() { 
    insertAjaxResponse("reInit"); 
}
function doParseWeb() {  
    //Offline parse-Aktion - DPSEnvironment/Terminologien nicht aktualisiert
    try {  
        xmlhttp = window.XMLHttpRequest 
            ? new XMLHttpRequest()  
            : new ActiveXObject("Microsoft.XMLHTTP");
        xmlhttp.onreadystatechange = parseWebTrigger;  
        var kwiki_poll_url = "KnowWE.jsp?renderer=ParseWebOffline&KWikiWeb=default_web";
        xmlhttp.open("GET", kwiki_poll_url); 
        xmlhttp.send(null); 
    } catch (e) {alert(e);} 
}  

function parseWebTrigger() {
    insertAjaxResponse("parseWeb"); 
}
function doSumAll() {  
    
    try {  
        xmlhttp = window.XMLHttpRequest 
            ? new XMLHttpRequest()  
            : new ActiveXObject("Microsoft.XMLHTTP");
        xmlhttp.onreadystatechange = sumAllTrigger;  
        var kwiki_poll_url = "KnowWE.jsp?renderer=KWikiSummarizer&KWikiWeb=default_web";
        xmlhttp.open("GET", kwiki_poll_url); 
        xmlhttp.send(null); 
    } catch (e) {alert(e);} 
}   
function sumAllTrigger() { 
    insertAjaxResponse("sumAll");
} 
/*
Clears a certain area of the HTML document per id. 
Is used in the admin panel to hide the generated information for:
   - parseWeb | sumAll | reInit
*/
function clearInnerHTML(id){
    if(document.getElementById(id) != null){
        document.getElementById(id).innerHTML = "";
    }
}
/* update response text sollte auch zusammenzufassen gehen, ...*/
function insertAjaxResponse(id){
    if ((xmlhttp.readyState == 4) && (xmlhttp.status == 200)) {  
        if(document.getElementById(id) != null) {
            document.getElementById(id).innerHTML = xmlhttp.responseText; 
        }
        document.getElementById(id).scrollIntoView(true);
    } 
}


function encodeUmlauts(text) {
    text = text.replace(/�/g, "&auml;");
    text = text.replace(/�/g, "&Auml;");
    text = text.replace(/�/g, "&ouml;");
    text = text.replace(/�/g, "&Ouml;");
    text = text.replace(/�/g, "&uuml;");
    text = text.replace(/�/g, "&Uuml;");
    text = text.replace(/�/g, "&szlig;");
    return text;
}

function hideSessionChooser() {
        document.getElementById("kwikiSessions").style.visibility='hidden';
    }

function showPopupButtons(usagePrefix, event) {
if(!event) {
 event = window.event;
 }
 hideAllPopupDivs();
 var dummy = document.getElementById(usagePrefix+'Popup');
 setCorrectPositionAndShow(dummy, event);
 planToHide(dummy, event);
}

function planToHide(node, event) {
 replanToHide(node, event);
 node.kwikiTimer = window.setTimeout("hideNow('"+node.id+"')", 2000);
}

function hideNow(id, event) {
 node = document.getElementById(id);
 node.style.visibility = 'hidden';
}

function replanToHide(node, event) {
 if(node.kwikiTimer != null) {
 window.clearTimeout(node.kwikiTimer);
 }
}

function hideAllPopupDivs() {
 for (i = 0; i < document.getElementsByTagName("div").length; i++) {
 div = document.getElementsByTagName("div")[i];
 if(div.getAttribute("KWikiPopupLinksDiv") != null) {
 div.style.visibility = 'hidden';
 }
 }
}

function doNothing() {
}


function hidePopupButtons(usagePrefix) {
 document.getElementById(usagePrefix+'Popup').style.visibility = 'hidden';
}


var ie4=document.all&&navigator.userAgent.indexOf("Opera")==-1;
var ns6=document.getElementById&&!document.all;
var ns4=document.layers;
var canhide=false;

function setCorrectPositionAndShow(menuobj, e) {
 	menuobj.thestyle=(ie4||ns6)? menuobj.style : menuobj;
 	eventX = e.layerX;
 	eventY = e.layerY;
 	menuobj.thestyle.left=(eventX - menuobj.contentwidth) + "px";
 	menuobj.thestyle.top = eventY + "px";
    menuobj.thestyle.visibility="visible";
 }

function switchTypeActivation(size) {
	try {
		var j = null;
		var params = {
			renderer : 'KnowWEObjectTypeActivationRenderer',
			KnowWeObjectType : (function () {
				var test = "";
				for (i=0;i<size;i++) {
					if (document.typeactivator.Auswahl.options[i].selected) {
						test += document.typeactivator.Auswahl.options[i].value;
						j = i;
					}			
				}
				return test;
			})()
    	}
		
    	var options = {
        	url : getURL ( params ),
		    response : {
		    	action : 'update',
			    ids : [],
			    fn : (function() {
			    	 	var element = document.typeactivator.Auswahl.options[j];
			    	 	if (element.style.color == "red") {
			    	 		element.style.color = "green";
			    	 	} else {
			    	 		element.style.color = "red";
			    	 	}
					 })()
		    }
    	}
		new KnowWEAjax( options ).send();
	} catch (e) {alert(e);}
}

function searchTypes(size) {
	try {

		var params = {
			renderer : 'KnowWEObjectTypeBrowserRenderer',
			TypeBrowserParams : (function () {
				var test = "";
				for (i=0;i<size;i++) {
					if (document.typebrowser.Auswahl.options[i].selected) {
						test += document.typebrowser.Auswahl.options[i].value;
					}			
				}
				return test;
			})()
    	}
    	
    	/*for (i=0;i<size;i++) {
			if (document.typebrowser.Auswahl.options[i].selected) {
				params += "&TypeBrowserParams"+"="+document.typebrowser.Auswahl.options[i].value;
			}			
		}*/
		console.log( getURL(params));
    	var options = {
        	url : getURL ( params ),
		    response : {
		    	action : 'insert',
			    ids : ['TypeSearchResult']
			}
        }
        new KnowWEAjax( options ).send();
		
	} catch (e) {alert(e);}
}

function sendRenameRequest() {
	
	var params = {
		action : 'RenamingRenderer',
		TargetNamespace : $('renameInputField').value,
		KWikiFocusedTerm : $('replaceInputField').value,
		ContextPrevious : $('renamePreviousInputContext').value,
		ContextAfter : $('renameAfterInputContext').value,
		CaseSensitive :$('search-sensitive').checked
	}
	
	var options = {
		url : getURL(params),
		response : {
			ids : ['rename-result']
		}
	}
	new KnowWEAjax( options ).send();
	/*
     try {        
         var renameText = document.getElementById("renameInputField").value; 
         var replacementText = document.getElementById("replaceInputField").value;
         var renameContextPrevious = document.getElementById("renamePreviousInputContext").value;
         var renameContextAfter = document.getElementById("renameAfterInputContext").value;
         var caseSensitive = document.getElementById("search-sensitive").checked;
                 
         var url = "KnowWE.jsp";
         var params = 'TargetNamespace='+encodeURIComponent(encodeUmlauts(renameText))
             +'&action=RenamingRenderer&KWikiFocusedTerm='+replacementText
             +'&ContextPrevious='+encodeURIComponent(encodeUmlauts(renameContextPrevious))
             +'&ContextAfter='+encodeURIComponent(encodeUmlauts(renameContextAfter))
             +'&CaseSensitive='+caseSensitive;
         
         KnowWEAjax.id = 'rename-result';
         KnowWEAjax.send(url + "?" + params, KnowWEAjax.insert, true);
     } catch (e) {alert(e);}*/
}

// gets the additional text, displayed in the match column of the TypeBrowser. 
function getAdditionalMatchTextTypeBrowser(atmUrl, query){
    try{
        kwiki_xmlhttp2 = window.XMLHttpRequest ? new XMLHttpRequest()
            : new ActiveXObject("Microsoft.XMLHTTP");

        var kwiki_poll_url = "KnowWE.jsp";
        var params = 'TypeBrowserQuery='+query
        	 + "&action=KnowWEObjectTypeBrowserRenderer"
             +'&ATMUrl='+atmUrl;
        
        kwiki_xmlhttp2.open("POST", kwiki_poll_url,false);
        kwiki_xmlhttp2.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
        kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
        kwiki_xmlhttp2.setRequestHeader("Connection", "close");
        kwiki_xmlhttp2.send(params);
        
        kwiki_xmlhttp2.onreadystatechange = insertAdditionalMatchText(atmUrl);
    }catch(e){alert(e);}
}

// gets the additional text, displayed in the match column of the renaming tool. 
function getAdditionalMatchText(atmUrl){
    try{
        kwiki_xmlhttp2 = window.XMLHttpRequest ? new XMLHttpRequest()
            : new ActiveXObject("Microsoft.XMLHTTP");
        
        var renameText = document.getElementById("renameInputField").value; 
        var replacementText = document.getElementById("replaceInputField").value;
        var renameContextPrevious = document.getElementById("renamePreviousInputContext").value;
        var renameContextAfter = document.getElementById("renameAfterInputContext").value;
        var caseSensitive = document.getElementById("search-sensitive").checked;

        var kwiki_poll_url = "KnowWE.jsp";
        var params = 'TargetNamespace='+encodeURIComponent(encodeUmlauts(renameText))
             +'&action=RenamingRenderer&KWikiFocusedTerm='+replacementText
             +'&ContextPrevious='+encodeURIComponent(encodeUmlauts(renameContextPrevious))
             +'&ContextAfter='+encodeURIComponent(encodeUmlauts(renameContextAfter))
             +'&CaseSensitive='+caseSensitive
             +'&ATMUrl='+atmUrl;
        
        kwiki_xmlhttp2.open("POST", kwiki_poll_url,false);
        kwiki_xmlhttp2.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
        kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
        kwiki_xmlhttp2.setRequestHeader("Connection", "close");
        kwiki_xmlhttp2.send(params);
        
        kwiki_xmlhttp2.onreadystatechange = insertAdditionalMatchText(atmUrl);
    }catch(e){alert(e);}
}

//inserts an additional text into a certain preview column. 
function insertAdditionalMatchText(atmUrl){

    var id = atmUrl.split("#")[2];
    var direction = atmUrl.split("#")[4];
    var text = "";

    id = direction + id;

    if((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)){
        if(document.getElementById(id) != null){
            text = kwiki_xmlhttp2.responseText;
            document.getElementById(id).innerHTML = text;
        }
    }
    //document.getElementById("rename-result").scrollIntoView(true);
}

function insertRenamingMask() {
    if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
        if(document.getElementById("rename-result") != null) {
            //alert("hab was bekommen!");
            var text = kwiki_xmlhttp2.responseText;
                if(text.length == 0) {
                    text = "no response for request";
                }

            document.getElementById("rename-result").innerHTML = text;
        }
        document.getElementById("rename-result").scrollIntoView(true);
        addLoadEvent(init_sortable(myColumns, 'sortable1'));
    }
 } 


 function replaceAll() {
 try {
 kwiki_xmlhttp2 = window.XMLHttpRequest
 ? new XMLHttpRequest()
 : new ActiveXObject("Microsoft.XMLHTTP");
 kwiki_xmlhttp2.onreadystatechange = showReplaceReport;
var renameText = document.getElementById("renameInputField").value; 
var replacementText = document.getElementById("replaceInputField").value;
var codedReplacements = '';
var inputs = document.getElementsByTagName("input");
for(var i = 0; i < inputs.length; i++) {
    var inputID = inputs[i].id;
    if(inputID.substring(0,11) == 'replaceBox_') {
        if(inputs[i].checked) {
            var code = inputID.substring(11);
            codedReplacements += "__"+code;
        }
    }
}
 var kwiki_poll_url = "KnowWE.jsp";
 var params = 'TargetNamespace='+encodeURIComponent(encodeUmlauts(renameText))+'&action=GlobalReplaceAction&KWikitext='+codedReplacements+'&KWikiFocusedTerm='+replacementText+'&page=blubb';
 kwiki_xmlhttp2.open("POST", kwiki_poll_url,false);
 kwiki_xmlhttp2.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
 kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
 kwiki_xmlhttp2.setRequestHeader("Connection", "close");
 kwiki_xmlhttp2.send(params);
 //kwiki_xmlhttp2.send(encodeURI('KWikitext='+kopictext.substring(0,20)));

 } catch (e) {alert(e);}
 }

function showReplaceReport() {
 if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
 if(document.getElementById("rename-result") != null) {
 //alert("hab was bekommen!");
 var text = kwiki_xmlhttp2.responseText;
 if(text.length == 0) {
 text = "no response for request";
 }
 
 document.location.reload();
 document.getElementById("rename-result").innerHTML = text;
 }
 document.getElementById("rename-result").scrollIntoView(true);
 
   var els = document.getElementById("rename-panel").getElementsByTagName("input");
   for(var i in els){
      if(els[i].type == "text")
          els[i].value = "";
   }
 
 }
 } 

function kwiki_pollSyntax(article) {
 try {
 kwiki_xmlhttp2 = window.XMLHttpRequest
 ? new XMLHttpRequest()
 : new ActiveXObject("Microsoft.XMLHTTP");
 kwiki_xmlhttp2.onreadystatechange = kwiki_triggeredSyntax;
 var semi = new RegExp("\;","g");
// var kopictext = "<topic>Swimming</topic>"+(document.main.text.value.replace(semi,"\;"));
var kopictext = document.getElementById("editorarea").value; 
 var kwiki_poll_url = "KnowWE.jsp";
 var params = 'KWikitext='+encodeURIComponent(encodeUmlauts(kopictext))+'&KWiki_Topic='+article+'&renderer=KWikiParseRenderer'+'&action=KWiki_parseTextToKnowledgebase';
 kwiki_xmlhttp2.open("POST", kwiki_poll_url,false);
 kwiki_xmlhttp2.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
 kwiki_xmlhttp2.setRequestHeader("Content-length", params.length);
 kwiki_xmlhttp2.setRequestHeader("Connection", "close");
 kwiki_xmlhttp2.send(params);
 //kwiki_xmlhttp2.send(encodeURI('KWikitext='+kopictext.substring(0,20)));

 } catch (e) {alert(e);}
 } 

function kwiki_triggeredSyntax() {
 if ((kwiki_xmlhttp2.readyState == 4) && (kwiki_xmlhttp2.status == 200)) {
 if(document.getElementById("kwikiSyntaxReportArea") != null) {
 //alert("hab was bekommen!");
 var text = kwiki_xmlhttp2.responseText;
 if(text.length == 0) {
 text = "no response for request";
 }

 document.getElementById("kwikiSyntaxReportArea").innerHTML = text;
 }
 document.getElementById("kwikiSyntaxReportArea").scrollIntoView(true);
 }
 } 

/**
 * Handles a key down event in the edit textarea.<b>
 * Used for KnowWE-CodeCompletion.
 */
function handleKeyDown(event) {
    $('autoCompleteMenu').style['display'] = 'inline';
    if (event.ctrlKey && event.keyCode == 32) {        
        var textArea = $('editorarea');
        var endPos;
        if( document.selection ){
            var range = document.selection.createRange();
            var stored_range = range.duplicate();
            stored_range.moveToElementText( textArea );           
            stored_range.setEndPoint( 'EndToEnd', range );
            textArea.selectionStart = stored_range.text.length - range.text.length;            
            endPos = textArea.selectionStart + range.text.length;
        } 
        else 
        {   // Firefox, etc.
            if(textArea.selectionEnd) 
            {
                textArea.focus();
                endPos = textArea.selectionEnd;
            }
            else
                textArea.focus();
        }          
        
        var tmp = textArea.value.substring(0, endPos);
        var startPos = 0; var i = endPos;
        while( startPos == 0 )
        {
            if(tmp.charAt(i) == '\n' || tmp.charAt(i) == '\r' ||tmp.charAt(i) == ' ')
            {
                 startPos = i;
            }
            i--;
        }
        //send request
        var encodedData = encodeURIComponent(tmp.substring(startPos + 1));
        var url = "KnowWE.jsp?renderer=KWiki_codeCompletion&KWikiWeb=default_web&CompletionText="+encodedData;
        
        KnowWEAjax.ext = {
            id: 'autoCompleteMenu',
            div: '',
            fn: function(){
                $('codeCompletion').addEvent( 'keydown', function(event){
                    var event = new Event(event);
                    if( event.code == 13){
                        if( $('codeCompletion').selectedIndex == 1) {
                            $('codeCompletion').fireEvent('change');
                        }
                    }
                });
                $('codeCompletion').options[1].addEvent('click', function(){
                      $('codeCompletion').fireEvent('change');
                });                
                $('codeCompletion').addEvent( 'change', function(){
                        index = $('codeCompletion').selectedIndex;
                        value = $('codeCompletion').options[index].value;
                        insertAtCursor($('editorarea'), value);
                });
                $('codeCompletion').focus();
            }
        };
        KnowWEAjax.send(url, KnowWEAjax.insertExtended , true);
    }
}
/**
 * Inserts an text element at the current cursor position in a textarea, etc.
 * @param element the textarea, etc.
 * @param value the text string
 */
function insertAtCursor(element, value) {
    if (document.selection) { //IE support
        element.focus();
        sel = document.selection.createRange();
        sel.text = value;
    } else if(element.selectionStart || element.selectionStart == '0'){ //Gecko based
         var startPos = element.selectionStart;
         var endPos = element.selectionEnd;
         element.value = element.value.substring(0, startPos) + value
             + element.value.substring(endPos, element.value.length);
         element.setSelectionRange(endPos + value.length, endPos + value.length);
    } else {
        element.value = value;
    }
    element.focus();
}
/* ############################################################### */
/* ############################################################### */
/*
 * Extends the headings of the KnowWEPlugin DIV with collabs abilities.
 * The funtion searches for all DIVs with an ".panel" class attribute and
 * extends them. The Plugin DIV should have the following structure in order
 * to work proberly:
 * <div class='panel'><h3>Pluginname</h3><x> some plugin content</x></div>
 */
function addPanelToggle()
{
    var panels = $$('div .panel');
    for(var i = 0; i < panels.length; i++)
    {
        var span = new Element('span');
        span.setText('- ');
        
        var el = panels[i].getChildren()[0];
        span.injectTop( el );
        
        el.addEvent( 'click', function(){
        	var style = this.getNext().getStyle('display');
        	style = (style == 'block') ? 'none' : ((style == '') ? 'none' : 'block');
        	
        	var children = this.getParent().getChildren();       	
		    for(var i = 0; i < children.length;i++){
		        if(children[i].getTag() == "h3"){
		        	children[i].getChildren()[0].setText( (style == 'block')? '- ' : '+ ' );
		        } else {
		            children[i].setStyle('display', style);
		        }
		    } 
        });
    }
}
/*
 * Shows a panel in certain plugin with additional options.
 */
function showExtendedPanel(event){
    var evt = event || window.event;
    var el = evt.target || evt.srcElement;
    
    var style = el.nextSibling.style;
    el.removeAttribute('class');           
 
    if(style['display'] == 'inline'){
        style['display'] = 'none';
        el.setAttribute('class', 'pointer extend-panel-down');
    }else{
        style['display'] = 'inline';
        el.setAttribute('class', 'pointer extend-panel-up');
    }
}

function formHint(event){
    if(!$('knoffice-panel')) return;
    
    var els = $('knoffice-extend-panel').getElementsByTagName("input");
    
    for (var i = 0; i < els.length; i++){
      if(els[i].nextSibling.tagName.toLowerCase() == 'span'){
      	els[i].addEvent('focus', function (event) {
           var evt = event || window.event;
           var el = evt.target || evt.srcElement;
           el.nextSibling.style.display = "inline";});
        els[i].addEvent('blur', function (event) {
           var evt = event || window.event;
           var el = evt.target || evt.srcElement;
           el.nextSibling.style.display = "none";});
      }
    }
}
/*
 * Returns the value of a URL parameter. Which paramater is specified through
 * name.
 */
function gup( name ){
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( window.location.href );
    if( results === null )
        return "";
    else
        return results[1];
}
/*
 * Checks if the current page allows certain onload events.
 * If the page list contains the current page, the onload event is triggered,
 * otherwise none. Prevents errors due incorrect context for onload events.
 */
function loadCheck( pages ){
      var path = window.location.pathname.split('/');
      var page = path[path.length - 1];
      
      for(var i = 0; i < pages.length; i++){
          if(page === pages[i])
              return true;
      }
      return false;
}
/*
 * Returns an URL that is createt out of the given parameters. 
 */
function getURL( params ) {
	var baseURL = 'KnowWE.jsp';
	var tokens = []

	if( !params && typeof params != 'object') return baseURL;
	
	for( keys in params ){
		tokens.push(keys + "=" + params[keys]);
	};	
	return baseURL + '?' + tokens.join('&');
}

/* ############################################################### */
/* KnowWEAjax - common ajax ability.                               */
/* ############################################################### */
//var http;
//var KnowWEAjax = {
//    /* stores current request */
//    send : function(url, fn, post){
//        http = (window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');       
//        if(!http) return;
//        
//        var method = (post) ? 'POST' : 'GET';
//        
//        http.open(method, url, true);
//        http.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
//        http.setRequestHeader('Content-length', url.length);
//        http.setRequestHeader('Connection', 'close');
//        http.onreadystatechange = fn;
//        http.send(method);
//    },
//    /* inserts the repsonse. mainly text or html result that changed*/
//    insert : function(){
//        if ((http.readyState == 4) && (http.status == 200)) { 
//            var response = http.responseText;
//            if(!KnowWEAjax.id) return;
//            if(KnowWEAjax.id.contains(';-;')){
//                var token = KnowWEAjax.id.split(';-;');
//                for(var i = 0; i < token.length; i++){
//                    if(!$(token[i])) return;
//                    $(token[i]).innerHTML = response;
//                }  
//            } else if( response.contains( KnowWEAjax.id) ){
//            	replace(KnowWEAjax.id, response)
//            } else {
//                $(KnowWEAjax.id).innerHTML = response;
//            }
//        }
//    },
//    /* inserts an extendend response. user can specify additional information to execute tasks.*/
//    insertExtended : function(){   
//        if ((http.readyState == 4) && (http.status == 200)) {  
//            if(KnowWEAjax.ext && KnowWEAjax.ext.id)
//            {          
//	            if(!KnowWEAjax.ext.div || KnowWEAjax.ext.div === ''){
//	                $(KnowWEAjax.ext.id).innerHTML = http.responseText;
//	            } else {
//	                var div = document.createElement('div');
//	                div.setAttribute('id', KnowWEAjax.ext.div);
//	                div.innerHTML = http.responseText;
//	                $(KnowWEAjax.ext.id).insertBefore(div, $(KnowWEAjax.ext.id).getChildren()[0]);; 
//	            }
//            }
//            if(!KnowWEAjax.ext.fn) return;
//            console.log("handle_response:"+KnowWEAjax.ext.fn);
//            KnowWEAjax.ext.fn.call();      
//        
//        /*
//        
//            if((KnowWEAjax.ext || KnowWEAjax.ext.id) && KnowWEAjax.ext.id != '')
//            {          
//	            if(!KnowWEAjax.ext.div || KnowWEAjax.ext.div === ''){
//	            	var response = http.responseText;
//		            if( response.contains( KnowWEAjax.ext.id) ){
//		            	replace(KnowWEAjax.ext.id, response)
//		                console.log("replace: " + response );
//		            } else {
//	                    $(KnowWEAjax.ext.id).innerHTML = response;
//		            }
//	            } else {
//	                var div = document.createElement('div');
//	                div.setAttribute('id', KnowWEAjax.ext.div);
//	                div.innerHTML = http.responseText;
//	                $(KnowWEAjax.ext.id).insertBefore(div, $(KnowWEAjax.ext.id).getChildren()[0]);; 
//	            }
//            }
//            if(!KnowWEAjax.ext.fn || KnowWEAjax.ext.fn == '') return;
//            eval(KnowWEAjax.ext.fn);  
//            console.log( "fn: " + KnowWEAjax.ext.fn);    */
//        }
//    }
//};

function replace(ids, text)
{
	for (var i in ids ) {
		if( typeof ids[i] != 'string' ) return;
		div = new Element('div', {
		    'styles': {
		        'display': 'hidden'	
		    },
		    'id' : 'KnowWE-temp'
		});
		div.injectBefore($( ids[i] ));
		$( ids[i] ).remove();
	    div.innerHTML = text;
	    $( ids[i] ).injectAfter( $('KnowWE-temp') );
	    $('KnowWE-temp').remove();
	}
}

/*
 *  
 */
String.prototype.getBytes = function() {
    return encodeURIComponent(this).replace(/%../g, 'x').length;
};


function log( msg ){
	if(console.log) console.log(msg);
	else alert(msg);
}

function echo( object ){
	if( typeof object == 'object'){
		
		for (var i in object ){
			if( typeof object[i] == 'object') {
				console.log(i);
				echo (object[i]);
			}
			else console.log( i + ":" + object[i]);
		}
	}
}

/*
 * Enriches an object by replacing its key:value pairs with those from an other
 * object. Also non existing key:value pairs from an object are added. Key:value
 * pairs that occur not in the oNew object are not changed in the oDefault object.
 */
function enrich (oNew, oDefault) {
    if( typeof(oNew) != 'undefined' && oNew != null) {
    	for( var i in oNew ) {
    		if( oNew[i] != null && typeof oNew[i] != 'object' ) oDefault[i] = oNew[i];
    		if(typeof oNew[i] == 'object') enrich( oNew[i], oDefault[i]);
    	}
    }
    return oDefault;
}




/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init(){
	if( loadCheck( ['Wiki.jsp'] )) {
		window.addEvent( 'domready', function(){
			addPanelToggle();
			formHint();
			KnowWETable.init();
			SolutionState.update();
			
			if($('rename-show-extend')){
            $('rename-show-extend').addEvent( 'click', showExtendedPanel);
	        }
	        if($('knoffice-show-extend')){
	            $('knoffice-show-extend').addEvent( 'click', showExtendedPanel);
	        }
		});
	};
	//CodeCompletion
	if(loadCheck( ['Edit.jsp'] ))
	{
	    window.addEvent('domready', function(){
	        if($('autoCompleteMenu')) return false;
	        
	        var span = new Element('span', { 'id' : 'autoCompleteMenu' });
	        span.inject($('submitbuttons'));
	    });
	    window.addEvent('domready', function(){
	        $('editorarea').addEvent( 'keydown', handleKeyDown );
	    });
	}
}());
