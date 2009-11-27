package de.d3web.we.hermes.maps;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

public class KMLLoader {

    private Document doc;

    public static void main(String[] args) {
	String urlString = "http://maps.google.de/maps/ms?ie=UTF8&hl=de&t=p&s=AARTsJow0Y-Ok57sIyhHL3nAoxsDPZuGYw&msa=0&msid=107111568253256110145.000462b8e236afe188363&ll=38.376115,25.817871&spn=6.888653,13.183594&z=6&output=kml";
	KMLLoader loader = new KMLLoader(urlString);
	List<Placemark> placemarks = loader.getPlacemarks();
	System.out.println(placemarks.get(0).generateMapScript());
    }

    public KMLLoader(String urlString) {
	URL url;
	try {
	    String fixedAmps = urlString.replaceAll("&amp;", "&");
	    String toKMLExport = fixedAmps.replaceAll("embed", "kml");
	    url = new URL(toKMLExport);

	    SAXBuilder saxBuilder = new SAXBuilder();
	    doc = saxBuilder.build(url.openStream());
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (JDOMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public List<Placemark> getPlacemarks() {
	List<Placemark> result = new ArrayList<Placemark>();

	ElementFilter plmFilter = new ElementFilter("Placemark");
	Iterator<Element> plIterator = doc.getDescendants(plmFilter);
	while (plIterator.hasNext()) {
	    Element placemarkElem = plIterator.next();
	    Namespace namespace = placemarkElem.getNamespace();
	    String name = placemarkElem.getChild("name", namespace).getText();
	    String coordinateString = placemarkElem
		    .getChild("Point", namespace).getChild("coordinates",
			    namespace).getText();
	    String[] coordinates = coordinateString.split(",");
	    double longitude = Double.parseDouble(coordinates[1].trim());
	    double latitude = Double.parseDouble(coordinates[0].trim());

	    Placemark placemark = new Placemark(name, longitude, latitude);
	    result.add(placemark);

	}

	// System.out.println(plmElements.size());

	return result;
    }
}
