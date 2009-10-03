
/**
 *  save new input.
 */
function sendTimeEventSearchRequest() {
	

	
	var searchFrom = document.getElementById("hermesSearchFrom");
	var searchTo = document.getElementById("hermesSearchTo");
	var startIndex = document.getElementById("startIndexTimeline");
	var resultCount = document.getElementById("hermesSearchResultCount");
	
    
	var params = {
    		action : 'SearchTimeEventsAction',
    		count : resultCount.value,
    		from : searchFrom.value,
    		to : searchTo.value,
    		startIndex : startIndex.value
    		
    		
    	}
	
	var options = {
    		url : getURL( params ),
    		response : {
    			action : 'insert',
    			ids : ['hermesSearchResult']
    		}
	}
	
	new KnowWEAjax( options ).send();
	
}