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
            action : 'insert' /* replace|insert|update|none|create|string */,
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
    
    this.getResponse = function() {
        return http.responseText;
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
                    replace( ids, http.responseText );
                    break;
                case 'create':
                    if( oDefault.create ){
                        var el = oDefault.create.fn.call();
                        el.innerHTML = http.responseText;
                        $( oDefault.create.id).insertBefore( el, $( oDefault.create.id ).getChildren()[0]);
                    } 
                    break;
                case 'string':
                    if( http.responseText.startsWith('@info@')){
                         var info = new Element('p', { 'class' : 'box info' });
                         info.setHTML( http.responseText.replace(/@info@/, '') );
                         info.injectTop($( ids[0]));
                    }
                    if( http.responseText.startsWith('@replace@')){
                         replace( ids, http.responseText.replace(/@replace@/, '') );	
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
        
        // Called to update the Relations in CoveringLists
        ReRenderKnowWESectionContent.update();
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
        KnowWETable.map.set(this.id, this.value);
    },
    onSave : function(){
        var id = this.id;
        var namespace = '';
        KnowWETable.map.each(function(value, key){
            namespace += key + ";-;" + value + "::";
        });
        namespace = namespace.substring(0, namespace.lastIndexOf('::'));

        var params = {
            action : 'UpdateTableKDOMNodes',
            TargetNamespace : encodeURIComponent(namespace),
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
                action : 'string',
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
    
    function highlightXCLRelation(node, topic, depth, breadth) {
    	ReRenderKnowWESectionContent.updateForXCLRelation(node, topic, depth, breadth);		
    }
    
    function highlightRule(node, topic, depth, breadth) {
        
        // Restore the Highlighting that was before
        var restore = document.getElementById('uniqueMarker');
        if (restore != null) {
            ReRenderKnowWESectionContent.updateNode(restore.className, topic);
        }
		highlightNode(node, topic, depth, breadth);
    }
    
    /*
     * You need a span with an id to use this.
     * there the uniqueMarker is located.
     * node is the tag that has the marker under it.
     * depth means the tag that has the marker as firstchild.
     * Note: if a node has more than 1 element this function.
     * will not work because it cannot foresee how the html-tree
     * is build
     */
    function highlightNode(node, topic, depth, breadth) {
        
        var params = {
            action : 'HighlightNodeAction',
            Kwiki_Topic : topic,
            KWikiJumpId : node
        }
        
        var options = {
            url : getURL( params ),
            response : {
                action : 'update',
                ids : [],
                fn : (function() {
//                        // get the uniqueMarker marking the last marked element
//                        var curMark = document.getElementById("uniqueMarker");
//                        
//                        // remove the last marker
//                        if (curMark != null) {
//                            curMark.id = "";
//                            curMark.style.backgroundColor = "";
//                        }
                        
                        // set the new Marker: Get the root node
                        var element = document.getElementById(node);
                        
                        // get to the depth given.
                        for (var i = 0; i < depth; i++) {
                            element = element.firstChild;
                        }
                        
                        // get to the given breadth
                        for (var j = 0; j < breadth; j++) {
                        	var test = element.nextSibling;                       	
                        	if (test != null)
                        		element = element.nextSibling;
                        }
                                              
                        if (element != null) {
                            element.firstChild.style.backgroundColor = "yellow";
                            element.firstChild.id = "uniqueMarker";
                            element.firstChild.className = node;
                            element.scrollIntoView(true);
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
//          alert(e);
//          }
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
            })(),
        }
        
        /*for (i=0;i<size;i++) {
            if (document.typebrowser.Auswahl.options[i].selected) {
                params += "&TypeBrowserParams"+"="+document.typebrowser.Auswahl.options[i].value;
            }           
        }*/
//        console.log( getURL(params));
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
	
	var params = {
        action : 'KnowWEObjectTypeBrowserRenderer',
        ATMUrl : atmUrl,
        kwiki_poll_url : "KnowWE.jsp",
        TypeBrowserQuery : query
    }
    
    var options = {
        url : getURL( params ),
        method : 'get',
        response : {
            action : 'insert',
            ids : [(atmUrl.split(":")[4] + atmUrl.split(":")[2])]
        }
    }
    new KnowWEAjax( options ).send();
}

// gets the additional text, displayed in the match column of the renaming tool. 
function getAdditionalMatchText(atmUrl)
{   
    var params = {
        action : 'RenamingRenderer',
        ATMUrl : atmUrl,
        KWikiFocusedTerm : $('replaceInputField').getValue(),
        TargetNamespace : $('renameInputField').getValue(),
        ContextAfter : $('renameAfterInputContext').getValue(),
        ContextPrevious : $('renamePreviousInputContext').getValue(),
        CaseSensitive : $('search-sensitive').getValue()
    }
    
    var options = {
        url : getURL( params ),
        method : 'post',
        response : {
            action : 'none',
            fn : function(){ 
                var id = atmUrl.split(":")[4] + atmUrl.split(":")[2];
                $(id).setHTML( request.getResponse() );
                response = null;
            }
        }
    }
    var request = new KnowWEAjax( options );
    request.send();
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


 function replaceAll() 
 {
    var codeReplacements = '';
    var inputs = $ES('input');
    for(var i = 0; i < inputs.length; i++) {
        var inputID = inputs[i].id;
        if(inputID.substring(0,11) == 'replaceBox_') {
            if(inputs[i].checked) {
                var code = inputID.substring(11);
                codeReplacements += "__" + code;
            }
        }
    } 
 
    var params = {
        TargetNamespace : encodeURIComponent(encodeUmlauts( $('renameInputField').getValue() )),
        action : 'GlobalReplaceAction',
        KWikitext : codeReplacements,
        KWikiFocusedTerm : $('replaceInputField').getValue()
    }
     
    var options = {
        url : getURL( params ),
        method : 'post',
        response : {
            action : 'insert',
            ids : [ 'rename-result' ],
            fn : function(){ setTimeout ( 'document.location.reload()', 5000 ); }
        }
    }    
    new KnowWEAjax( options ).send();
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
//              replace(KnowWEAjax.id, response)
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
//              if(!KnowWEAjax.ext.div || KnowWEAjax.ext.div === ''){
//                  $(KnowWEAjax.ext.id).innerHTML = http.responseText;
//              } else {
//                  var div = document.createElement('div');
//                  div.setAttribute('id', KnowWEAjax.ext.div);
//                  div.innerHTML = http.responseText;
//                  $(KnowWEAjax.ext.id).insertBefore(div, $(KnowWEAjax.ext.id).getChildren()[0]);; 
//              }
//            }
//            if(!KnowWEAjax.ext.fn) return;
//            console.log("handle_response:"+KnowWEAjax.ext.fn);
//            KnowWEAjax.ext.fn.call();      
//        
//        /*
//        
//            if((KnowWEAjax.ext || KnowWEAjax.ext.id) && KnowWEAjax.ext.id != '')
//            {          
//              if(!KnowWEAjax.ext.div || KnowWEAjax.ext.div === ''){
//                  var response = http.responseText;
//                  if( response.contains( KnowWEAjax.ext.id) ){
//                      replace(KnowWEAjax.ext.id, response)
//                      console.log("replace: " + response );
//                  } else {
//                      $(KnowWEAjax.ext.id).innerHTML = response;
//                  }
//              } else {
//                  var div = document.createElement('div');
//                  div.setAttribute('id', KnowWEAjax.ext.div);
//                  div.innerHTML = http.responseText;
//                  $(KnowWEAjax.ext.id).insertBefore(div, $(KnowWEAjax.ext.id).getChildren()[0]);; 
//              }
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
 * Some string helper functions  
 */
String.prototype.getBytes = function() {
    return encodeURIComponent(this).replace(/%../g, 'x').length;
};
String.prototype.startsWith = function( str ) {
	return (this.match("^"+str)==str);
};
String.prototype.endsWith = function(str){
  return (this.match(str+"$")==str);
};

/*
 * Used to ReRender the updated CoveringLists.
 * Called by SolutionState.execute()
 */
var ReRenderKnowWESectionContent  = {
    
    updateNode : function(node, topic) {
        var params = {
            action : 'ReRenderContentPartAction',
            KWikiWeb : 'default_web',
            KdomNodeId : node,
            ArticleTopic : topic
        }
        var url = getURL( params );
        ReRenderKnowWESectionContent.execute(url, node);
    },
    
    update : function() {
        
   		// get the current topic
		var topic = gup('page');
		
		// get all CoveringLists
		var classlist = getElementsByClass(null, 'ReRenderSectionMarker', null);             
        
        // Rerender the CoveringLists
        if (classlist != null) {
            for (var i = 0; i < classlist.length; i++) {
                var kdomnodeid = classlist[i].id;
                var params = {
                    action : 'ReRenderContentPartAction',
                    KWikiWeb : 'default_web',
                    KdomNodeId : kdomnodeid,
                    ArticleTopic : topic
                }           
                var url = getURL( params );
                ReRenderKnowWESectionContent.execute(url, kdomnodeid);
            }
        }
    },
    
    execute : function( url, id ) {
        var options = {
            url : url,
            action : 'replace',
            response : {
                ids : [ id ],
            }
        }

        new KnowWEAjax( options ).send();
    },
    
    updateForXCLRelation : function (node, topic, depth, breadth) {
    	// get the current topic
		var topic = gup('page');
		
		// get all CoveringLists
		var classlist = getElementsByClass(null, 'ReRenderSectionMarker', null);             
        
        // Rerender the CoveringLists
        if (classlist != null) {
            for (var i = 0; i < classlist.length; i++) {
                var kdomnodeid = classlist[i].id;
                var params = {
                    action : 'ReRenderContentPartAction',
                    KWikiWeb : 'default_web',
                    KdomNodeId : kdomnodeid,
                    ArticleTopic : topic
                }           
                var url = getURL( params );
                ReRenderKnowWESectionContent.
                		executeForXCLRelation
                			(url, kdomnodeid, node, topic, depth, breadth);
            }
        }   	
    },
    
    executeForXCLRelation : function(url, id, node, topic, depth, breadth) {
    	var options = {
            url : url,
            action : 'replace',
            response : {
                ids : [ id ]
            }
        }
        new KnowWEAjax( options ).send();
		setTimeout(function () {ReRenderKnowWESectionContent.sleepForXCLRelation(node, topic, depth, breadth)}, 500);
    },
    
    sleepForXCLRelation : function(node, topic, depth, breadth) {

    	if (document.getElementById('uniqueMarker') != null) {
    		setTimeout(function () {ReRenderKnowWESectionContent.sleepForXCLRelation(node, topic, depth, breadth)}, 500);
    	} else {
    		
    		if (document.getElementById(node) != null) {
    			// Restore the Highlighting that was before
       			var restore = document.getElementById('uniqueMarker');
        		if (restore != null) {
            		ReRenderKnowWESectionContent.updateNode(restore.className, topic);
        		}
        		highlightNode(node, topic, depth, breadth);
    			} else {
					setTimeout(function () {ReRenderKnowWESectionContent.sleepForXCLRelation(node, topic, depth, breadth)}, 100);
    			}
    	}

    }
}

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
			if($('testsuite-show-extend')){
                $('testsuite-show-extend').addEvent( 'click', showExtendedPanel);
            }
            if($('testsuite2-show-extend')){
                $('testsuite2-show-extend').addEvent( 'click', showExtendedPanel);
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

/* 140 SearchBox
 * FIXME: remember 10 most recent search topics (cookie based)
 * Extended with quick links for view, edit and clone (ref. idea of Ron Howard - Nov 05)
 * Refactored for mootools, April 07
 */
var TagSearchBox = {

	onPageLoad: function(){		
		this.onPageLoadFullSearch();
	},

	onPageLoadFullSearch : function(){
		var q2 = $("tagquery"); if( !q2 ) return;
		this.query2 = q2;

		q2.observe( this.runfullsearch0.bind(this) );
				

		if(location.hash){
			/* hash contains query:pagination(-1=all,0,1,2...) */
			var s = decodeURIComponent(location.hash.substr(1)).match(/(.*):(-?\d+)$/);
			if(s && s.length==3){
				q2.value = s[1];
				$('start').value = s[2];				
			}
		}
	},

	/* reset the start page before rerunning the ajax search */
	runfullsearch0: function(){
		$('start').value='0';
		this.runfullsearch();
	},

	runfullsearch: function(e){
		var q2 = this.query2.value;
		if( !q2 || (q2.trim()=='')) {
			$('searchResult2').empty();
			return;
		}
		$('spin').show();


		new Ajax(Wiki.TemplateUrl+'AJAXTagSearch.jsp', {
			postBody: $('searchform2').toQueryString(),
			update: 'searchResult2',
			method: 'post',
			onComplete: function() {
				$('spin').hide();
				GraphBar.render($('searchResult2'));
				Wiki.prefs.set('PrevQuery', q2);
			}
		}).request();

		location.hash = '#'+q2+":"+$('start').value;  /* push the query into the url history */
	},

	submit: function(){
		var v = this.query.value.stripScripts(); //xss vulnerability
		if( v == this.query.defaultValue) this.query.value = '';
		if( !this.recent ) this.recent=[];
		if( !this.recent.test(v) ){
			if(this.recent.length > 9) this.recent.pop();
			this.recent.unshift(v);
			Wiki.prefs.set('RecentSearch', this.recent);
		}
	},

	clear: function(){
		this.recent = [];
		Wiki.prefs.remove('RecentSearch');
		$('recentSearches','recentClear').hide();
	},

	ajaxQuickSearch: function(){
		var qv = this.query.value.stripScripts() ;
		if( (qv==null) || (qv.trim()=="") || (qv==this.query.defaultValue) ) {
			$('searchOutput').empty();
			return;
		}
		$('searchTarget').setHTML('('+qv+') :');
		$('searchSpin').show();

		Wiki.jsonrpc('search.findPages', [qv,20], function(result){
				$('searchSpin').hide();
				if(!result.list) return;
				var frag = new Element('ul');

				result.list.each(function(el){
					new Element('li').adopt(
						new Element('a',{'href':Wiki.getUrl(el.map.page) }).setHTML(el.map.page),
						new Element('span',{'class':'small'}).setHTML(" ("+el.map.score+")")
					).inject(frag);
				});
				$('searchOutput').empty().adopt(frag);
				Wiki.locatemenu( $('query'), $('searchboxMenu') );
		});
	} ,

	/* navigate to url, after smart pagename handling */
	navigate: function(url, promptText, clone, search){
		var p = Wiki.PageName,
			defaultResult = (clone) ? p+'sbox.clone.suffix'.localize() : p,
			s = this.query.value;
		if(s == this.query.defaultValue) s = '';

		var handleResult = function(s){
			if(s == '') return;
			if(!search)	s = Wiki.cleanLink(s);//remove invalid chars from the pagename

			p=encodeURIComponent(p);
			s=encodeURIComponent(s);
			if(clone && (s != p)) s += '&clone=' + p;

			location.href = url.replace('__PAGEHERE__', s );
		};

		if(s!='') {
			handleResult(s);
		} else {
			Wiki.prompt(promptText, defaultResult, handleResult.bind(this));
		}
	}
}

window.addEvent('load', function(){
	TagSearchBox.onPageLoad();
});