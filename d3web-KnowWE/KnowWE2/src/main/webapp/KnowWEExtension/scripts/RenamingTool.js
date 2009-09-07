/* Select all checkboxes within a certain section. */
function selectPerSection(element, section) {
	var renameForm = element.form
	var i = 0;

	for(i = 0; i < renameForm.length; i++){
		if(renameForm[i].type == 'checkbox' && renameForm[i].id != ''){
		    if(renameForm[i].id.search(section) != -1)
		        renameForm[i].checked = true;
	    }
    }
}

/* Deselects all chechboxes in the renaming form. */
function deselectPerSection(element, section){
   	var renameForm = element.form
	var i = 0;

    for(i = 0; i < renameForm.length; i++){
		if(renameForm[i].type == 'checkbox' && renameForm[i].id != ''){
		    if(renameForm[i].id.search(section) != -1)
		        renameForm[i].checked = false;
	    }
    }
}

/* Checks if an field is not empty.*/
function isNotEmpty(input){
    if ( input == null || input == "" ) { 
        return false;
    }
    return true;
}


/* Shows an additional panel for the renaming tool*/
function showFurtherSettings(){
     // show the mask
     var object = document.getElementById('search-furtherSettings').style;
     var el = document.getElementById('furtherSettings');
 	 var text = el.lastChild.nodeValue;
 	     
     if(object['display'] == 'inline'){
         object['display'] = 'none';
         el.innerHTML = '<img src="KnowWEExtension/images/arrow_down.png" border="0"/>' + text;         
     }else{
         object['display'] = 'inline';
         el.innerHTML = '<img src="KnowWEExtension/images/arrow_up.png" border="0"/>' + text;
     }
}

/* checks if the user entered a search term. if not, an error message is displayed. */
function checkRenamingToolForm(){
    var search = document.getElementById('renameInputField');
    var info = document.getElementById('info').style;
    var text = document.getElementById('inner');
    
    if(search != null && search.value != ""){
        info['display'] = 'none';
        sendRenameRequest();
    }else{
        text.innerHTML = "Oops. Please enter a search term!";
        info['display'] = 'block';
    }
}

/* shows the given div */
function showDiv(id){
// show the mask
     var object = document.getElementById(id).style;
 	     
     if(object['display'] == 'inline'){
         object['display'] = 'none';         
     }else{
         object['display'] = 'inline';
     }
}
