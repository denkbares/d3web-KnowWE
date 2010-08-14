package de.d3web.we.hermes.taghandler;

import java.util.List;
import java.util.Map;

import de.d3web.we.hermes.maps.Placemark;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ShowMapForTopicHandler extends AbstractTagHandler {

	public ShowMapForTopicHandler() {
		super("showMapForTopic");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		String givenTopic = values.get("topic");
		if (givenTopic != null) {
			topic = givenTopic;
		}

		List<Placemark> placemarks = TimeEventSPARQLUtils
				.findLocationsOfTimeEventsForTopic(topic);

		if (placemarks.isEmpty()) {
			return "Keine Orte zum Anzeigen vorhanden.";
		}
		else {
			String output = "";
			String divId = (Math.random() + "_map").substring(4);
			output += "<div id=\"" + divId
					+ "\" style=\"width: 600px; height: 400px\"/>";
			// List<Placemark> placemarks = new ArrayList<Placemark>();
			// placemarks.add(new Placemark("London", 51.3, 0));
			output += getJavaScript(placemarks, divId);
			output += "</div>";
			return output;
		}
	}

	private String getJavaScript(List<Placemark> placemarks, String divID) {

		if (placemarks.isEmpty()) {
			return "no placemarks to render";
		}
		String output = "";
		output += "<script src=\"http://maps.google.com/maps?file=api&v=2&key=abcdefg&sensor=true_or_false\" type=\"text/javascript\"> </script>";
		output += "<script type=\"text/javascript\">\n";
		output += "if (GBrowserIsCompatible()) {"
				+ "var map = new GMap2(document.getElementById(\"" + divID
				+ "\"));";
		output += "map.setCenter(new GLatLng("
				+ placemarks.get(0).getLatitude() + ","
				+ placemarks.get(0).getLongitude() + "), 6);" + "}";
		output += "map.setMapType(G_SATELLITE_MAP);";
		output += "map.addControl(new GSmallMapControl());";

		for (Placemark p : placemarks) {
			// String escapedDescription = p.getDescription().replaceAll("\"",
			// "''");
			// escapedDescription = escapedDescription.replaceAll("\n", "<br>");
			String htmlTextForInfoWindows = "<h4>" + p.getTitle() + "</h4>";
			// htmlTextForInfoWindows += "<div>" + escapedDescription +
			// "</div>";
			String latitude = Double.toString(p.getLatitude());
			String longitude = Double.toString(p.getLongitude());

			output += "var point = new GLatLng(" + latitude + ", " + longitude
					+ ");";
			output += "var marker = new GMarker(point);";
			output += "marker.bindInfoWindowHtml(\"" + htmlTextForInfoWindows
					+ "\");";
			output += "map.addOverlay(marker);";
		}
		output += "</script>";
		return output;
	}
}
