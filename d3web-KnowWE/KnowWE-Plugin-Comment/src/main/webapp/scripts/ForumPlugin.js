
/**
 *  save new input.
 */
function saveForumBox() {
	
	var text = document.answer.text.value;
	var topic = document.answer.topic.value;
	
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
    			action : 'none',
    			ids : []
    		}
	}
	
	new _KA( options ).send();
	
}

function loadAction( topic ) {

	var params = {
   		action : 'ForumBoxAction',
   		ForumBoxText : '',
   		ForumArticleTopic : topic
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
