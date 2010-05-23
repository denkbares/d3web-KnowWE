function fctGetBuildResults( dashboardID , buildNr ) {

    var params = {
           action : 'CIAction',
           task   : 'getBuildResults',
           id     : dashboardID,
           nr     : buildNr
       }
   
    var options = {
           url : KNOWWE.core.util.getURL( params ),
           response : {
               ids : [ dashboardID + '-column-right'],
               action : 'insert'
           }
    }
    
    new _KA( options ).send();
}

function fctExecuteNewBuild( dashboardID ) {

	var params = {
            action : 'CIAction',
            task   : 'executeNewBuild',
            id     : dashboardID
        }
    
     var options = {
            url : KNOWWE.core.util.getURL( params ),
            response : {
                ids : [ dashboardID + '-column-right'],
                action : 'insert'
            }
     }
     
     new _KA( options ).send();
}

function fctGetWikiChanges( dashboardID , buildNr ) {

    var params = {
           action : 'CIAction',
           task   : 'getWikiChanges',
           id     : dashboardID,
           nr     : buildNr
       }
   
    var options = {
           url : KNOWWE.core.util.getURL( params ),
           response : {
               ids : [ dashboardID + '-column-middle'],
               action : 'insert',
               fn : makeCIChangesCollapsible
           }
    }
    
    new _KA( options ).send();
}


/*
 * 
 */
function makeCIChangesCollapsible(){
    var selector = "div .ci-changes-panel";
    
    var panels = _KS( selector );
    if( panels.length < 1 ) return;
    if( !panels.length ) panels = new Array(panels);
    
    for(var i = 0; i < panels.length; i++){
        var span = new _KN('span');
        span._setText('+ ');
        
        var heading = panels[i].getElementsByTagName('h4')[0];
        if(!heading.innerHTML.startsWith('<span>')){
             span._injectTop( heading );
        }
        _KE.add('click', heading , function(){
            var el = new _KN( this );
            var style = el._next()._getStyle('display');
            style = (style == 'block') ? 'none' : ((style == '') ? 'none' : 'block');                    
            
            el._getChildren()[0]._setText( (style == 'block')? '- ' : '+ ' );
            el._next()._setStyle('display', style);
        });
    }
}

