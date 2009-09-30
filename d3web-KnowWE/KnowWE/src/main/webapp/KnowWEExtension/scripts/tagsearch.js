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
			}
		}).request();	
	}

}

window.addEvent('load', function(){
	TagSearchBox.onPageLoad();
});