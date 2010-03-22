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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;

public class SessionPersistenceHandler {

	private static SessionPersistenceHandler instance = new SessionPersistenceHandler();
	
	private SessionPersistenceHandler() {
		super();
	}
	
	public static SessionPersistenceHandler getInstance() {
		return instance;
	}

	
	public List<Information> loadSession(Broker broker, URL target) {
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
				return parseXML(parser, broker);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning(
						"Error: Cannot parse: " + target + " :\n "
								+ e.getMessage());
			}
		}
		return new ArrayList<Information>();
	}
	
	private List<Information> parseXML(XMLStreamReader parser, Broker broker) throws Exception {
		List<Information> result = new ArrayList<Information>();
		for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser
				.next()) {
			if(event == XMLStreamConstants.START_ELEMENT) {
				if(parser.getLocalName().equals("Information")) {
					String date = parser.getAttributeValue(0);
					String terminologyType = parser.getAttributeValue(1);
					String inforamtionType = parser.getAttributeValue(2);
					String namespace = parser.getAttributeValue(3);
					String objectID = parser.getAttributeValue(4);
					List<Object> values = new ArrayList<Object>();
					if(parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
						while(parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
							if(parser.getLocalName().equals("Value")) {
								String type = parser.getAttributeValue(0);
								String value = parser.getAttributeValue(1);
								Object obj = getValue(type, value);
								if(obj != null) {
									values.add(obj);						
								}
							}
							parser.nextTag();
						}
					}
					Information info = new Information(namespace, objectID, values, TerminologyType.getType(terminologyType), InformationType.getType(inforamtionType));
					info.setCreationDate(new Date(Long.parseLong(date)));
					result.add(info);
				}
			}
		}
		parser.close();
		return result;
	}

	private Object getValue(String type, String value) {
		if(type.equals("String")) {
			return value;
		} else if(type.equals("Double")) {
			return Double.valueOf(value);
		} else if(type.equals("SolutionState")) {
			return SolutionState.getType(value);
		}
		return null;
	}

	public void saveSession(Broker broker, URL target) throws IOException, URISyntaxException, XMLStreamException {
		OutputStream out = new FileOutputStream(new File(target.toURI()));
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "ISO-8859-1");
		
		//writer.writeStartDocument("ISO-8859-1", "1.0");
		writer.writeStartElement("Informations");
		for (Information each : broker.getSession().getBlackboard().getAllInformation()) {		
			writer.writeStartElement("Information");
			writer.writeAttribute("Date", String.valueOf(each.getCreationDate().getTime()));
			writer.writeAttribute("TerminologyType", each.getTerminologyType().getIdString());
			writer.writeAttribute("InformationType", each.getInformationType().getIdString());
			writer.writeAttribute("Namespace", each.getNamespace());
			writer.writeAttribute("ObjectID", each.getObjectID());
			writer.writeStartElement("Values");
			for (Object obj : each.getValues()) {
				writer.writeStartElement("Value");
				writer.writeAttribute("Type", obj.getClass().getSimpleName());
				writer.writeAttribute("Value", obj.toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		
		Logger.getLogger(getClass().getName()).info("Saved session to " + target);
		
		writer.flush();
		writer.close();
		out.close();
	}
	
	
}
