
/**
 *  goto date.
 */
function gotoDate() {
	
	var date = document.date.input.value;
    
	var params = {
    		action : 'CalendarAction',
    		CalendarGoTo : date
    	}
	
	var options = {
    		url : KNOWWE.core.util.getURL( params ),
    		response : {
    			action : 'none',
    			ids : []
    		}
	}
	
	new _KA( options ).send();
	
}

/**
*  go back to today.
*/
function gotoToday() {
	
	var date = document.date.today.value;
   
	var params = {
   		action : 'CalendarAction',
   		CalendarGoTo : date
   	}
	
	var options = {
   		url : KNOWWE.core.util.getURL( params ),
   		response : {
   			action : 'none',
   			ids : []
   		}
	}
	
	new _KA( options ).send();
	
}

function loadAction( today ) {

	var params = {
   		action : 'CalendarAction',
   		CalendarGoTo : today,
   	}
	
	var options = {
   		url : KNOWWE.core.util.getURL( params ),
   		response : {
   			action : 'none',
   			ids : []
   		}
	}
	
	new _KA( options ).send();

}

/**
 * link
 */
function link(topic, week) {
	
	var url = document.URL;
	
 	if(url.charAt(url.length - 1) == '/') {
 	    
 	    url = url + "Wiki.jsp?page=" + topic;
 	    
 	} else {
	
    	var urlSplit = url.split('&');
    	
    	var path = urlSplit[0].split("#");
    	
    	url = path[0];
    
    	for(var i = 1; i < urlSplit.length; i++) {
    	    parameterSplit = urlSplit[i].split('=');
    	    if(parameterSplit[0] == 'week') break;
    	    if(parameterSplit[0].contains('manually')) break;
    	    url = url + "&" + urlSplit[i];
    	}
 	}
	
	url = url + '&week=' + week + "#Calendar";
	
	location.replace(url);
	
}
