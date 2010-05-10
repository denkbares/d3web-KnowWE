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
               ids : ['ci-column-right'],
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
                ids : ['ci-column-right'],
                action : 'insert'
            }
     }
     
     new _KA( options ).send();
}