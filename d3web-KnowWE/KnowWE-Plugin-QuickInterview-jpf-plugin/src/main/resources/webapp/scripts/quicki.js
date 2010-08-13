/**
 * Namespace: quicki
 * The KNOWWE quick interview namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin.quicki = function(){
    return {
    }
}();


KNOWWE.plugin.quicki = function(){
    
    return {
        // Function: init
        // Creates an button in the top actions menu in the WIKI. This button
        // is use to insert an HTMLDialog into the current article.
        //init : function(){
            //if( !_KS('#actionsTop') ) return;
            //var li = new _KN('li', {'id': 'moreKnowWE'});
            //var a = new _KN('a', {'id': 'bttn-dialog','href' : '#', 'class' : 'action dialog'});
            //a._setText( bttn_name );
            //li.appendChild(a);
            //_KS('#actionsTop ul')[0].appendChild(li);
            //_KE.add('click', _KS('#bttn-dialog'), KNOWWE.plugin.d3web.dialog.insert);        
        //},
        /**
         * Function: initAction
         * Adds some events to the Quick Interview. Without this QuickI does not 
         * respond to any user action.
         */
        initAction : function(){
            _KS('.containerHeader').each(function(element){
                _KE.add('click', element, alert('hallo'));
                //var p = element.parentNode;
                //var tbl = _KS('#tbl'+p.id, p);
                //_KE.add('click', tbl, KNOWWE.plugin.d3web.dialog.answerClicked);                
            });
               
           
        }
    }
}();