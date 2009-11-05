package de.d3web.we.hermes.maps;

import java.util.List;
import java.util.Map;

import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ShowMapHandler extends AbstractTagHandler {

    public ShowMapHandler() {
	super("showMap");
    }

    @Override
    public String render(String topic, KnowWEUserContext user,
	    Map<String, String> values, String web) {
	double latitude = 0;
	double longitude = 0;
	double zoom = 0;

	if (values.containsKey("longitude")) {
	    try {
		longitude = Double.parseDouble(values.get("longitude"));
	    } catch (NumberFormatException nfe) {
		// do nothing
	    }
	}
	if (values.containsKey("latitude")) {
	    try {
		latitude = Double.parseDouble(values.get("latitude"));
	    } catch (NumberFormatException nfe) {
		// do nothing
	    }
	}
	if (values.containsKey("zoom")) {
	    try {
		zoom = Double.parseDouble(values.get("zoom"));
	    } catch (NumberFormatException nfe) {
		// do nothing
	    }
	}

	String output = "";
	String divId = (Math.random() + "_map").substring(4);
	output += "<div id=\"" + divId
		+ "\" style=\"width: 600px; height: 400px\"/>";
	// List<Placemark> placemarks = new ArrayList<Placemark>();
	// placemarks.add(new Placemark("London", 51.3, 0));
	output += getJavaScript(latitude, longitude, zoom, divId);
	output += "</div>";
	return output;
    }

    private String getJavaScript(double latitude, double longitude,
	    double zoom, String divID) {
	String output = "";
	output += "<script src=\"http://maps.google.com/maps?file=api&v=2&key=abcdefg&sensor=true_or_false\" type=\"text/javascript\"> </script>";
	output += "<script type=\"text/javascript\">\n";
	output += "if (GBrowserIsCompatible()) {"
		+ "var map = new GMap2(document.getElementById(\"" + divID
		+ "\"));" + "map.setCenter(new GLatLng(" + latitude + ","
		+ longitude + ")," + zoom + ");" + "}";
	output += "var point = new GLatLng(" + latitude + ", " + longitude
		+ ");";
	output += "map.addOverlay(new GMarker(point));";
	output += "</script>";
	return output;
    }

    private String getJavaScript(List<Placemark> placemarks, String divID) {
	String output = "";
	output += "<script src=\"http://maps.google.com/maps?file=api&v=2&key=abcdefg&sensor=true_or_false\" type=\"text/javascript\"> </script>";
	output += "<script type=\"text/javascript\">\n";
	output += "if (GBrowserIsCompatible()) {"
		+ "var map = new GMap2(document.getElementById(\"" + divID
		+ "\"));" + "map.setCenter(new GLatLng(51.3, 0), 5);" + "}";

	for (Placemark p : placemarks) {
	    String latitude = Double.toString(p.getLatitude());
	    String longitude = Double.toString(p.getLongitude());

	    output += "var point = new GLatLng(" + latitude + ", " + longitude
		    + ");";
	    output += "map.addOverlay(new GMarker(point));";
	}
	output += "</script>";
	return output;
    }
}
