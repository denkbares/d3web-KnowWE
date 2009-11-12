/**
 *  save new input.
 */
function saveForumBox() {
	
	var text = document.getElementById('text').value;
	var topic = document.getElementById('topic').value;
	
	text = encodeURIComponent( text );
    topic = topic.replace(/\ /g,"+");
    
	var params = {
    		action : 'ForumBoxAction',
    		ForumBoxText : text,
    		ForumArticleTopic : topic
    	}
	
	var options = {
			url : KNOWWE.core.util.getURL( params ),
    		response : {
    			action : 'insert',
    			ids : [ 'newBox' ]
    		}
	}
	
	new _KA( options ).send();
}

function reloadForumBox() {
	
	var url = document.URL;
	
	var url = url.substring(0,url.length-4);
	
	document.location.replace(url);
	
}
