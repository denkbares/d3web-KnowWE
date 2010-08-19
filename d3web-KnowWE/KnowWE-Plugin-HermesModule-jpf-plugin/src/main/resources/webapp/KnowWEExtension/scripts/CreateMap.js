var blueIcon = new GIcon();
// BlueIcon is not used in Hermes
// blueIcon.image = "KnowWEExtension/markerblue.png";
// blueIcon.iconSize = new GSize(20, 34);
// blueIcon.iconAnchor = new GPoint(9, 34);
var map = null;
var geocoder = null;
var x = 0;
var xFix = 0;
var y = 0;
var yFix = 0;
var area = 0;
window.onload=function initialize() {
	document.getElementById("address").value = "Hier Adresse zum Suchen eingeben.";
	if (GBrowserIsCompatible()) {
    map = new GMap2(document.getElementById("map_canvas"));
	map.setMapType(G_PHYSICAL_MAP);
    map.setCenter(new GLatLng(0, 0), 1);
    map.setUIToDefault();
    geocoder = new GClientGeocoder();
    GEvent.addListener(map, "click", function(overlay, point)
	{ 
	if(document.getElementById("rot").innerHTML == "") {
	//document.getElementById("rot").innerHTML = "Koordinaten der roten Markierung:<br><input id='lat' size='13' readonly='readonly' /><input id='lng' size='13' readonly='readonly' /><input type='button' id='fixieren' value='Diesen Punkt fixieren' onclick='setXY()' />";
	document.getElementById("rot").innerHTML = "Koordinaten der Markierung:<br><input id='lat' size='13' readonly='readonly' /><input id='lng' size='13' readonly='readonly' />";
	}
		map.clearOverlays(); 
			if (point) { 
			map.addOverlay(new GMarker(point)); 
			map.panTo(point);
			document.getElementById("lat").value  = point.lat();
			document.getElementById("lng").value  = point.lng();

			/*
			x = point.lat();
			y = point.lng();
			var x1unround = Math.abs(point.lat());
			var ns = "N";
			if (point.lat() < 0) {
				ns = "S";
			}
			var x1 = Math.floor(x1unround);
			var x2unround = (x1unround-x1)*60;
			var x2 = Math.floor(x2unround);
			var x3 = (Math.floor(100*((x2unround-x2)*60)))/100;
			document.getElementById("lat").value = x1+"°"+x2+"°"+x3+"°"+ns;
			var ow = "O";
			var y1unround = Math.abs(point.lng());
			if (point.lng() < 0) {
				ow = "W";
			}
			var y1 = Math.floor(y1unround);
			var y2unround = (y1unround-y1)*60;
			var y2 = Math.floor(y2unround);
			var y3 = (Math.floor(100*((y2unround-y2)*60)))/100;
			document.getElementById("lng").value = y1+"°"+y2+"°"+y3+"°"+ow;
			*/
			}
			
			//No areas used in Hermes
			/*
				if (area != 0) {
				var markerblue = new GMarker(new GLatLng(xFix, yFix),{clickable:false, icon:blueIcon});
				map.addOverlay(markerblue);
						var polygon = new GPolygon([
						new GLatLng(x,y),
						new GLatLng(x,yFix),
						new GLatLng(xFix, yFix),
						new GLatLng(xFix, y),
						new GLatLng(x, y)
						], "#003ff3", 5, 1, "#0000ff", 0.2, {clickable:false});
						map.addOverlay(polygon);
				}
			 */
		}
	); 
  }
}

function showAddress(address) {
  if (geocoder) {
    geocoder.getLatLng(
      address,
      function(point) {
        if (!point) {
          alert(address + " not found");
        } else {
		map.clearOverlays(); 
	if(document.getElementById("rot").innerHTML == "") {
		//document.getElementById("rot").innerHTML = "Koordinaten der roten Markierung:<br><input id='lat' size='13' readonly='readonly' /><input id='lng' size='13' readonly='readonly' /><input type='button' id='fixieren' value='Diesen Punkt fixieren' onclick='setXY()' />";
		document.getElementById("rot").innerHTML = "Koordinaten der roten Markierung:<br><input id='lat' size='13' readonly='readonly' /><input id='lng' size='13' readonly='readonly' />";
	}
          map.setCenter(point, 13);
          var marker = new GMarker(point);
          map.addOverlay(marker);
			document.getElementById("lat").value  = point.lat();
			document.getElementById("lng").value  = point.lng();

			/*
			x = point.lat();
			y = point.lng();
			var x1unround = Math.abs(point.lat());
			var ns = "N";
			if (point.lat() < 0) {
				ns = "S";
			}
			var x1 = Math.floor(x1unround);
			var x2unround = (x1unround-x1)*60;
			var x2 = Math.floor(x2unround);
			var x3 = (Math.floor(100*((x2unround-x2)*60)))/100;
			document.getElementById("lat").value = x1+"°"+x2+"°"+x3+"°"+ns;
			var ow = "O";
			var y1unround = Math.abs(point.lng());
			if (point.lng() < 0) {
				ow = "W";
			}
			var y1 = Math.floor(y1unround);
			var y2unround = (y1unround-y1)*60;
			var y2 = Math.floor(y2unround);
			var y3 = (Math.floor(100*((y2unround-y2)*60)))/100;
			document.getElementById("lng").value = y1+"°"+y2+"°"+y3+"°"+ow;
			*/

			//No areas used in Hermes
			/*
				if (area != 0) {
				var markerblue = new GMarker(new GLatLng(xFix, yFix),{clickable:false, icon:blueIcon});
				map.addOverlay(markerblue);
						var polygon = new GPolygon([
						new GLatLng(x,y),
						new GLatLng(x,yFix),
						new GLatLng(xFix, yFix),
						new GLatLng(xFix, y),
						new GLatLng(x, y)
						], "#003ff3", 5, 1, "#0000ff", 0.2, {clickable:false});
						map.addOverlay(polygon);
				}
			 */
        }
      }
    );
  }
}

function tagErzeugen() {
	if (document.getElementById("rot").innerHTML != "") {
		if (document.getElementById("titel").value.indexOf(";") > -1)
		{
			document.getElementById("tag").value = "Der Titel enthält das nicht erlaubte Zeichen ';'";
		} else if (document.getElementById("titel").value=="Hier Titel eingeben.")
		{
			document.getElementById("tag").value = "Bitte Titel angeben.";
		} else {
			// Hermes has no description or areas
			/*
			if (document.getElementById("blau").innerHTML=="")
			{
				if (document.getElementById("beschreibung").value == "Hier Beschreibung eingeben. (optional)") {
					document.getElementById("tag").value = "<ORT:" + document.getElementById("titel").value + ";" + document.getElementById("lat").value +";" +document.getElementById("lng").value +">";
				} else {
					document.getElementById("tag").value = "<ORT:" + document.getElementById("titel").value + ";" + document.getElementById("lat").value +";" +document.getElementById("lng").value +";"+document.getElementById("beschreibung").value+">";
				}
			/*} else {
					if (document.getElementById("beschreibung").value == "Hier Beschreibung eingeben. (optional)") {
						document.getElementById("tag").value = "<ORT:" + document.getElementById("titel").value + ";" + document.getElementById("lat").value +":"+ document.getElementById("lat2").value +";" +document.getElementById("lng").value +":" + document.getElementById("lng2").value +">";
					} else {
						document.getElementById("tag").value = "<ORT:" + document.getElementById("titel").value + ";" + document.getElementById("lat").value +":"+ document.getElementById("lat2").value +";" +document.getElementById("lng").value +":" + document.getElementById("lng2").value +";"+document.getElementById("beschreibung").value+">";}
			}*/
			document.getElementById("tag").value = "<<ORT: " + document.getElementById("titel").value + "; " + document.getElementById("lat").value +";" +document.getElementById("lng").value +" >>";
		}
	}
}

// No areas in Hermes
/*
function setXY() {
	document.getElementById("blau").innerHTML = "Koordinaten der blauen Markierung:<br><input id='lat2' size='13' readonly='readonly' /><input id='lng2' size='13' readonly='readonly' /><input type='button' value='Fixierten Punkt löschen' onclick='clearXY()' /><br><br>";
	document.getElementById("lat2").value =	document.getElementById("lat").value; 
	document.getElementById("lng2").value =	document.getElementById("lng").value; 
	area = 1;
	xFix = x;
	yFix = y;
	var markerblue = new GMarker(new GLatLng(xFix, yFix),{clickable:false, icon:blueIcon});
	map.addOverlay(markerblue);
	document.getElementById("blau").style.visibility="visible";
	document.getElementById("fixieren").disabled=true;
}

function clearXY() {
	document.getElementById("blau").innerHTML = "";
	xFix = 0;
	yFix = 0;
	area = 0;
	map.clearOverlays();
	map.addOverlay(new GMarker(new GLatLng(x,y)));
	document.getElementById("fixieren").disabled=false;
}
*/

function resetSite() {
	window.location.reload();
}

function appendToPage() {
	if (document.getElementById("tag").value!="Hier entsteht der Tag"){
		var params = {
				action : 'AppendToPageContentAction',
				KWiki_Topic : document.getElementById("seiten").value,
				KWikiWeb : document.getElementById("seiten").value,
				KWikitext : document.getElementById("tag").value
		}
		var options = {
				url : KNOWWE.core.util.getURL(params),
		}
		new _KA( options ).send();
		alert("Tag an das Ende der Seite "+document.getElementById("seiten").value+" hinzugefügt");
	} else{ 
		alert("Bitte erst Tag erzeugen");
	}
}
