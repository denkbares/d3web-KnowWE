/**
 * Namespace: KNOWWE.tablesorter
 * The KNOWWE table sorter namespace.
 * Contains functions to sort HTMLTables.
 */
KNOWWE.tablesorter = function(){
    /*sorting function for strings */
    function stringSort(el1, el2){
        var cellOne = el1.getElementsByTagName("td")[col].innerHTML;
        var cellTwo = el2.getElementsByTagName("td")[col].innerHTML;       
                
        return (cellOne > cellTwo) ? direction :(cellOne < cellTwo)? -direction : 0;
    }
    
    /*sorting function for integers */
    function intSort(el1, el2){
        var cellOne = parseInt(el1.getElementsByTagName("td")[col].innerHTML);
        var cellTwo = parseInt(el2.getElementsByTagName("td")[col].innerHTML);
                
        return (cellOne > cellTwo) ? direction :(cellOne < cellTwo)? -direction : 0;
    }   
    return {
        /**
         * Function: init
         * Initializes the sort ability.
         * 
         * Parameters:
         *     columns - The columns of the to sort table.
         *     tableID - The id of the table.
         */
        init : function(columns, tableID){
            if(!_KS('#' + tableID)) return;
            var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
            for( var i = 0; i < tblHeader.length; i++){
                if(columns[i].sortable == "true"){
                    var text = tblHeader[i].innerHTML;
                    _KE.add('click', tblHeader[i], function(){
                        KNOWWE.tablesorter.sort(i, tableID);
                    });
                }
            }
        },
        /**
         * Function: sort
         * Sorts the table according to the selected column.
         * 
         * Parameters:
         *     columns - The columns of the to sort table.
         *     tableID - The id of the table.
         */
        sort : function(columnID, tableID){
            var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
            var tbody = document.getElementById(tableID).getElementsByTagName('tbody');
                    
            var sortingType; var direction;
            var rowsSort = [];
            
            /* choose sorting type [asc desc]*/
            if(tblHeader[columnID].classname == "asc"){
                sortingType = "des";
                direction = -1;
            }else if(tblHeader[columnID].classname == "des"){
                sortingType = "asc";
                direction = 1;
            }else{
                sortingType = "asc";
                direction = 1;
            }       
                
            /* for each tbody if query is found in more than one article*/
            for(var i = 0; i < tbody.length; i++){
                var rows = tbody[i].getElementsByTagName('tr');
                col = columnID;
                
                /* clone original nodes´, necessary for comparision. */
                for(var j = 0; j < rows.length; j++){
                    rowsSort[j] = rows[j].cloneNode(true);
                }
                
                /* sort the table*/
                rowsSort.sort(stringSort);
                    
                /* replace old table with new sorted one */
                for(var k = 0; k < rows.length; k++){
                    rows[k].parentNode.replaceChild(rowsSort[k], rows[k]);
                }
            }
            
            /* store current sorting type */
            tblHeader[columnID].classname = sortingType;
        }       
    }   
}();

/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init(){
    
    window.addEvent( 'domready', _KL.setup );

    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
            KNOWWE.tablesorter.init();
        });
    };
}());