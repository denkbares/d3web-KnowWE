/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.hermes.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.hermes.maps.Placemark;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ShowMapHandler extends AbstractHTMLTagHandler {

	private static final String LOCATIONS_FOR_TOPIC = "SELECT  ?long ?lat WHERE { <URI> lns:hasLatitude ?lat . <URI> lns:hasLongitude ?long .}";

	public ShowMapHandler() {
		super("showMap");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		double latitude = 0;
		double longitude = 0;
		double zoom = 5;

		if (values.containsKey("longitude")) {
			try {
				longitude = Double.parseDouble(values.get("longitude"));
			}
			catch (NumberFormatException nfe) {
				// do nothing
			}
		}
		if (values.containsKey("latitude")) {
			try {
				latitude = Double.parseDouble(values.get("latitude"));
			}
			catch (NumberFormatException nfe) {
				// do nothing
			}
		}
		if (values.containsKey("zoom")) {
			try {
				zoom = Double.parseDouble(values.get("zoom"));
			}
			catch (NumberFormatException nfe) {
				// do nothing
			}
		}

		if (longitude == 0 && latitude == 0) {
			String concept = topic;
			if (values.containsKey("concept")) {
				concept = values.get("concept");
			}
			OwlHelper helper = SemanticCoreDelegator.getInstance().getUpper().getHelper();
			String querystring = LOCATIONS_FOR_TOPIC.replaceAll("URI",
					helper.createlocalURI(concept).toString());
			TupleQueryResult queryResult = TimeEventSPARQLUtils.executeQuery(querystring);
			Collection<? extends Placemark> placemark = buildPlacemarksForLocation(queryResult);
			if (placemark != null && placemark.size() > 0) {
				Placemark p = placemark.iterator().next();
				longitude = p.getLongitude();
				latitude = p.getLatitude();
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

	private static Collection<? extends Placemark> buildPlacemarksForLocation(
			TupleQueryResult result) {
		List<Placemark> placemarks = new ArrayList<Placemark>();
		if (result == null) return placemarks;
		try {
			while (result.hasNext()) {

				BindingSet set = result.next();
				String latString = set.getBinding("lat").getValue()
						.stringValue();
				String longString = set.getBinding("long").getValue()
						.stringValue();
				try {
					latString = URLDecoder.decode(latString, "UTF-8");
					longString = URLDecoder.decode(longString, "UTF-8");
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				double latitude = Double.parseDouble(latString.replaceAll(",",
						"."));
				double longitude = Double.parseDouble(longString.replaceAll(
						",", "."));

				placemarks.add(new Placemark(null, latitude, longitude, ""));

			}
		}
		catch (QueryEvaluationException e) {
			return null;
		}
		return placemarks;
	}

	private String getJavaScript(double latitude, double longitude,
			double zoom, String divID) {
		String output = "";
		output += "<script src=\"http://maps.google.com/maps?file=api&v=2&key=abcdefg&sensor=false\" type=\"text/javascript\"> </script>";
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
