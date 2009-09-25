
var myColumns = [{key:'match'  , label:'Match'   , type:'string' , sortable:'false'},
				{key:'section', label:'Section' , type:'string' , sortable:'true'},
				{key:'replace', label:'Replace?', type:'string' , sortable:'false'},
				{key:'preview', label:'Preview' , type:'string' , sortable:'false'}];

/* loads a certain functio after the page finished loading */
function addLoadEvent(func) {
    window.onload = func;
}

/*initialize the sortable function :) */
function init_sortable(columns, tableID){
	if (!document.getElementsByTagName) return;

	var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
	for( var i = 0; i < tblHeader.length; i++){
	    if(columns[i].sortable == "true"){
	        var text = tblHeader[i].innerHTML; 
	        tblHeader[i].innerHTML = '<a href="#" onclick="sortColumn(' 
	            + i + ",'" + tableID + "'" + ');">' + text + '</a>';
	    }
	}
}

/*sort the columns */
function sortColumn(columnID, tableID){

	var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
	var tbody = document.getElementById(tableID).getElementsByTagName('tbody');
		    
    var sortingType; var direction;
    var rowsSort = new Array();
    
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
	    if(myColumns[columnID].type == "string"){
	        rowsSort.sort(stringSort);
	    }
	        
	    /* replace old table with new sorted one */
	    for(var k = 0; k < rows.length; k++){
	        rows[k].parentNode.replaceChild(rowsSort[k], rows[k]);
	    }
    }
    
    /* store current sorting type */
	tblHeader[columnID].classname = sortingType;
    
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
}