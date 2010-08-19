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

package de.d3web.we.hermes.maps;

public class Placemark {

	String description;

	double latitude;

	double longitude;

	String title;

	public Placemark(String title, double latitude, double longitude) {
		this(title, latitude, longitude, null);
	}

	public Placemark(String title, double latitude, double longitude,
			String description) {
		super();
		this.title = title;
		this.latitude = latitude;
		this.longitude = longitude;
		this.description = description;
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

	public String getDescription() {
		return description;
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

	public void setDescription(String description) {
		this.description = description;
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
		return title + "[" + latitude + ", " + longitude + "]";
	}

	public String toHTMLString() {
		String s = "<pre>";
		s += "<b>" + title + ": </b>";
		s += "<span>" + latitude + "°N ; " + longitude + "°E</span>";
		if (description != null) {
			s += "<div>" + description + "</div>";
		}
		s += "</pre>";
		return s;
	}
}
