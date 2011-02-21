<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>KnowWE Map Koordinatenpaar: <%= request.getParameter("num") %></title>
<script src="http://maps.google.com/maps?file=api&v=2&key=ABQIAAAAb3JzCPOo-PmQupF8WKTY_BQhTDteWOscIBEFxr5sPfw40-jPhhS0zVcy-utMHpbsLwjf1yApcwxvXg&sensor=false" type="text/javascript"></script>
 <script type="text/javascript">
 function initialize(){
	var lat = <%= request.getParameter("lat") %>;
	var long = <%= request.getParameter("long") %>;

	if (GBrowserIsCompatible()) {
		var map = new GMap2(document.getElementById("map_canvas"));
		map.setCenter(new GLatLng(lat,long),7);
		map.addControl(new GSmallMapControl(),new GControlPosition(G_ANCHOR_TOP_RIGHT));
		map.enableScrollWheelZoom();
		}
	var point = new GLatLng(lat, long);
	map.addOverlay(new GMarker(point));
}
 </script>
  </head>
  <body onload="initialize()" onunload="GUnload()" style="font-family: Arial;border: 0 none;">
    <div id="map_canvas" style="width: 600px; height: 400px"></div>
  </body>
</html>