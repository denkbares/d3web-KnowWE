function downloadSVG(sectionID) {
	
	var svg = document.getElementById("svg"+sectionID);
	var serializer = new XMLSerializer();
	var svgString = serializer.serializeToString(svg);
	
	// add namespace attributes and css styles to the SVG String
	
	var stylesheets = document.styleSheets;
	var cssString = "<style type='text/css' > <![CDATA[ ";
	
	for(var i = 0; i<stylesheets.length; i++) {
		
		var rules = stylesheets[i].cssRules;
		
		for(var j = 0; j<rules.length; j++) {
			
			cssString += rules[j].cssText;
		}
		
	}
	
	cssString+= "]]> </style>";
	
	var substring = svgString.substring(0,5);
	
	substring += " xmlns:xlink=\"http://www.w3.org/1999/xlink\"";
	
		
	substring += " " + svgString.substring(5);
	svgString = substring;
	
	// insert css styles
	
	var splittedSvgString = svgString.split(">");
	svgString = splittedSvgString[0] + ">";
	svgString += cssString;
	for(var k = 1; k<splittedSvgString.length;k++) {
		if(k!=splittedSvgString.length-1) {
		svgString+=splittedSvgString[k] + ">";
		}
	}
	
	
		
	sendSVGD3DownloadAction(svgString);
}

function sendSVGD3DownloadAction(svgSource) {
	var params = {
		action : 'OntoVisSVGD3Download',
	};
	var options = {
		url : KNOWWE.core.util.getURL(params),
		data: svgSource,
		response : {
			fn : function() {
				
				svgUrl = this.response;
				saveToDisk(svgUrl, "svgSource.svg");
				
			}
		},
	};;

	new _KA(options).send();
}

function saveToDisk(fileURL, fileName) {
    // for non-IE
    if (!window.ActiveXObject) {
        var save = document.createElement('a');
        save.href = fileURL;
        save.target = '_blank';
        save.download = fileName || 'unknown';

        var event = document.createEvent('Event');
        event.initEvent('click', true, true);
        save.dispatchEvent(event);
        (window.URL || window.webkitURL).revokeObjectURL(save.href);
       
    }

    // for IE
    else if ( !! window.ActiveXObject && document.execCommand)     {
        var _window = window.open(fileURL, '_blank');
        _window.document.close();
        _window.document.execCommand('SaveAs', true, fileName || fileURL);;
        _window.close();
    }
}

