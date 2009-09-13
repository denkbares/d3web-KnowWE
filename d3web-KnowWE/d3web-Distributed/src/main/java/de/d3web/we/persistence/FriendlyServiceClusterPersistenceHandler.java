/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;

public class FriendlyServiceClusterPersistenceHandler {

	private static FriendlyServiceClusterPersistenceHandler instance = new FriendlyServiceClusterPersistenceHandler();
	
	private FriendlyServiceClusterPersistenceHandler() {
		super();
	}
	
	public static FriendlyServiceClusterPersistenceHandler getInstance() {
		return instance;
	}

	public ISetMap<String, String> loadClusterInformation(URL target) {
		XMLInputFactory fact = XMLInputFactory.newInstance();
		XMLStreamReader parser = null;
		try {
			InputStream in = target.openStream();
			parser = fact.createXMLStreamReader(in, "ISO-8859-1");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot load: " + target + " :\n "
							+ e.getMessage());
		} 
		if (parser != null) {
			try {
				return parseXML(parser);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning(
						"Error: Cannot parse: " + target + " :\n "
								+ e.getMessage());
			}
		}
		return new SetMap<String, String>();
	}
	
	private ISetMap<String, String> parseXML(XMLStreamReader parser) throws Exception {
		ISetMap<String, String> result = new SetMap<String, String>();
		for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser
				.next()) {
			if(event == XMLStreamConstants.START_ELEMENT) {
				if(parser.getLocalName().equals("Cluster")) {
					String clusterID = parser.getAttributeValue(0);
					while(parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
						if(parser.getLocalName().equals("Service")) {
							String serviceID = parser.getAttributeValue(0);
							if(serviceID != null) {
								result.add(clusterID, serviceID);
							}
						}
						parser.nextTag();
					}
				}
			}
		}
		parser.close();
		return result;
	}

	public void saveClusterInformation(ISetMap<String, String> clusters, URL target) throws Exception {
		OutputStream out = new FileOutputStream(new File(target.toURI()));
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "ISO-8859-1");
		
		//writer.writeStartDocument("ISO-8859-1", "1.0");
		writer.writeStartElement("Clusters");
		for (String eachCluster : clusters.keySet()) {		
			writer.writeStartElement("Cluster");
			writer.writeAttribute("ID", eachCluster);
			for (String eachService : clusters.get(eachCluster)) {
				writer.writeStartElement("Service");
				writer.writeAttribute("ID", eachService);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		
		Logger.getLogger(getClass().getName()).info("Saved cluster information to " + target);
		
		writer.flush();
		writer.close();
		out.close();
	}
	
}
