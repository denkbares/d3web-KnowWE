package de.d3web.we.hermes.maps;
public class Placemark {

    String title;

    double latitude;

    double longitude;

    public Placemark(String title, double latitude, double longitude) {
	super();
	this.title = title;
	this.latitude = latitude;
	this.longitude = longitude;
    }

    public double getLatitude() {
	return latitude;
    }

    public double getLongitude() {
	return longitude;
    }

    public String getTitle() {
	return title;
    }

    public void setLatitude(double latitude) {
	this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
	this.longitude = longitude;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    @Override
    public String toString() {
	return title + "[" + longitude + ", " + latitude + "]";
    }

    public String generateMapScript() {
	String s = "";
	s += "function initialize() { \n";
	s += "var map = new GMap2(document.getElementById(\"map_canvas\"));\n";
	s += "map.setCenter (new GLatLng(" + longitude + ", " + latitude
		+ "), 5);\n";
	s += "map.enableScrollWheelZoom();\n";
	s += "var point = new GLatLng(" + longitude + "," + latitude + ")\n";
	s += "map.addOverlay(new GMarker(point));\n";
	s += "}\n\n";
	return s;
    }
}
