<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>KnowWE Map Koordinatenpaar: <%= request.getParameter("num") %></title>
<script type="text/javascript"
src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript">
 function initialize(){
	var lat = <%= request.getParameter("lat") %>;
	var long = <%= request.getParameter("long") %>;
	var latlong = new google.maps.LatLng(lat, long);
	
    var myOptions = {
      zoom: 8,
      center: latlong,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    
    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	 var marker = new google.maps.Marker({
		position: latlong, 
		map: map, 
		title:""
	});   	
}
 </script>
  </head>
  <body onload="initialize()" onunload="GUnload()" style="font-family: Arial;border: 0 none;">
    <div id="map_canvas" style="width: 600px; height: 400px"></div>
  </body>
</html>