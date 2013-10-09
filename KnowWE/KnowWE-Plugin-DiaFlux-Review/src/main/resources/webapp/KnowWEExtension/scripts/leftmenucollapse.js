/* hoverIntent r6 // 2011.02.26 // jQuery 1.5.1+ // by Brian Cherne
* <http://cherne.net/brian/resources/jquery.hoverIntent.html> */
(function($){$.fn.hoverIntent=function(f,g){var cfg={sensitivity:7,interval:100,timeout:0};cfg=$.extend(cfg,g?{over:f,out:g}:f);var cX,cY,pX,pY;var track=function(ev){cX=ev.pageX;cY=ev.pageY};var compare=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);if((Math.abs(pX-cX)+Math.abs(pY-cY))<cfg.sensitivity){$(ob).unbind("mousemove",track);ob.hoverIntent_s=1;return cfg.over.apply(ob,[ev])}else{pX=cX;pY=cY;ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}};var delay=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);ob.hoverIntent_s=0;return cfg.out.apply(ob,[ev])};var handleHover=function(e){var ev=jQuery.extend({},e);var ob=this;if(ob.hoverIntent_t){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t)}if(e.type=="mouseenter"){pX=ev.pageX;pY=ev.pageY;$(ob).bind("mousemove",track);if(ob.hoverIntent_s!=1){ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}}else{$(ob).unbind("mousemove",track);if(ob.hoverIntent_s==1){ob.hoverIntent_t=setTimeout(function(){delay(ev,ob)},cfg.timeout)}}};return this.bind('mouseenter',handleHover).bind('mouseleave',handleHover)}})(jq$);

jq$(document).ready(function() { 

	if (jq$('#collapsehandler').length) {
		return;
	}
	
	jq$('#leftmenucollapsepin').click(function(event) {
		
		if (this.hasClass('pinned')){
			unpin();
		} else {
			pin();
		}
		//seem that this method is called twice, no idea why
		//so if we do not stop the propagation of the event,
		// the same state as before is restored and it seems not to work at all
		
		event.stopImmediatePropagation();
		
	});

	//returns the state of the menu
	if (getState()) {
		pin();
	} else {
		unpin();
	}
	
	//saves the state in a cookie
	function safeState(pinned) {
		document.cookie = "leftmenustate=" + pinned;
	}
	
	function getState(){
		
		var pos = document.cookie.indexOf("leftmenustate=");
		if (pos != -1) {
		    var start = pos + 14;                       
		    var end = document.cookie.indexOf(";", start);  
		    if (end == -1) end = document.cookie.length;
		    var value = document.cookie.substring(start, end);  
		    return value == "true";
		    
		} else {
			return true;
		}
	}
	
	function unpin() {
		var handler = jq$('<div>', {id: 'collapsehandler'}).append(jq$('#favorites')).appendTo('#content')
		.click(expandMenu).hoverIntent({
			timeout: 700,
	        over: expandMenu,
	        out: collapseMenu
	    });
		jq$('#leftmenucollapsepin').removeClass('pinned');
		jq$('#page').addClass('expanded');
		safeState(false);
	}
	
	function pin(){
		jq$('#leftmenucollapsepin').addClass('pinned');
		
		jq$('#favorites').insertBefore('#page');
		jq$('#collapsehandler').remove();
		
		jq$('#page').removeClass('expanded');
		safeState(true);
		
	}
	
	var rightpanel = false;
	
	function expandMenu(){
    	 if (! (rightpanel)) {
	        rightpanel = true;
	        jq$('#collapsehandler').unbind('click').addClass('expanded').animate({width: '20%'}, 666);
	    }
	    return false;
     };
     
    function collapseMenu () {
         if (rightpanel) {
             rightpanel = false;
             jq$('#collapsehandler').animate({width: '0px'}, 400,
                 function() {
                     jq$('#collapsehandler').removeClass('expanded');
                 }
             ).click(collapseMenu);
         }
     };
	
});
