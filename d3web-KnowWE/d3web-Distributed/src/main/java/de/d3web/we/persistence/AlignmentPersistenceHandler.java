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
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.d3web.we.alignment.Alignment;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.alignment.LocalAlignment;
import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.terminology.TerminologyServer;
import de.d3web.we.terminology.term.Term;


public class AlignmentPersistenceHandler {

	private static AlignmentPersistenceHandler instance = new AlignmentPersistenceHandler();

	private AlignmentPersistenceHandler() {
		super();
	}

	public static AlignmentPersistenceHandler getInstance() {
		return instance;
	}

	public Collection<GlobalAlignment> loadGlobalAlignment(
			URL alignmentURL,  TerminologyServer server) {
		XMLInputFactory fact = XMLInputFactory.newInstance();
		XMLStreamReader parser = null;
		try {
			InputStream in = alignmentURL.openStream();
			parser = fact.createXMLStreamReader(in, "ISO-8859-1");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot load: " + alignmentURL + " :\n "
							+ e.getMessage());
		} 
		if (parser != null) {
			try {
				return (Collection<GlobalAlignment>) parseXML(parser, server);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning(
						"Error: Cannot parse: " + alignmentURL + " :\n "
								+ e.getMessage());
			}
		}
		return null;
	}

	public Collection<LocalAlignment> loadLocalAlignment(
			URL alignmentURL,  TerminologyServer server) {
		XMLInputFactory fact = XMLInputFactory.newInstance();
		XMLStreamReader parser = null;
		try {
			InputStream in = alignmentURL.openStream();
			parser = fact.createXMLStreamReader(in, "ISO-8859-1");
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning(
					"Error: Cannot load: " + alignmentURL + " :\n "
							+ e.getMessage());
		} 
		if (parser != null) {
			try {
				return (Collection<LocalAlignment>) parseXML(parser, server);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning(
						"Error: Cannot parse: " + alignmentURL + " :\n "
								+ e.getMessage());
			}
		}
		return null;
	}

	private Collection<? extends Alignment> parseXML(XMLStreamReader parser, TerminologyServer server) throws XMLStreamException, ClassNotFoundException {
		Collection<Alignment> result = new ArrayList<Alignment>();
		for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser
				.next()) {
			if(event == XMLStreamConstants.START_ELEMENT) {
				if(parser.getLocalName().equals("GlobalAlignment")) {
					AbstractAlignType type = PersistenceUtils.getType(parser.getAttributeValue(0));
					Term term = null;
					if(parser.nextTag() == XMLStreamConstants.START_ELEMENT) { 
						term = PersistenceUtils.getExistingTerm(parser, server, null);
					}
					IdentifiableInstance ii = null;
					if(parser.nextTag() == XMLStreamConstants.START_ELEMENT) { 
						ii = PersistenceUtils.getII(parser);						
					}
					Alignment alignment = new GlobalAlignment(term, ii, type);
					PersistenceUtils.parseAlignmentProperties(parser, alignment);
					result.add(alignment);
				}
				if(parser.getLocalName().equals("LocalAlignment")) {
					AbstractAlignType type = PersistenceUtils.getType(parser.getAttributeValue(0));
					if(parser.nextTag() == XMLStreamConstants.START_ELEMENT) { 
						IdentifiableInstance ii1 = PersistenceUtils.getII(parser);		
						if(parser.nextTag() == XMLStreamConstants.END_ELEMENT) { 
							if(parser.nextTag() == XMLStreamConstants.START_ELEMENT) { 
								IdentifiableInstance ii2 = PersistenceUtils.getII(parser);	
								Alignment alignment = new LocalAlignment(ii1, ii2, type);
								PersistenceUtils.parseAlignmentProperties(parser, alignment);
								result.add(alignment);
							}
						}
					}
				}
			}
		}
		parser.close();

		return result;
	}
	
	public void saveGlobalAlignments(Collection<GlobalAlignment> alignments, URL target) throws Exception {
		OutputStream out = new FileOutputStream(new File(target.toURI()));
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "ISO-8859-1");
		writer.writeStartElement("GlobalAlignments");
		for (GlobalAlignment each : alignments) {		
			writer.writeStartElement("GlobalAlignment");
			Term term = each.getTerm();
			IdentifiableInstance ii = each.getObject();
			writer.writeAttribute("Type", each.getType().toString());
			PersistenceUtils.writeTermXML(writer, term, false, true);
			PersistenceUtils.writeIIXML(writer, ii);
			if(!each.getPropertiesMap().isEmpty()) {
				PersistenceUtils.writeAlignmentProperties(writer, each);
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		Logger.getLogger(getClass().getName()).info("Saved global alignments to " + target);
		writer.flush();
		writer.close();
		out.close();
	}

	public void saveLocalAlignments(Collection<LocalAlignment> alignments, URL target) throws Exception {
		OutputStream out = new FileOutputStream(new File(target.toURI()));
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "ISO-8859-1");
		
		//writer.writeStartDocument("ISO-8859-1", "1.0");
		writer.writeStartElement("LocalAlignments");
		for (LocalAlignment each : alignments) {		
			writer.writeStartElement("LocalAlignment");
			writer.writeAttribute("Type", each.getType().toString());
			IdentifiableInstance ii1 = each.getLocal();
			IdentifiableInstance ii2 = each.getObject();
			PersistenceUtils.writeIIXML(writer, ii1);
			PersistenceUtils.writeIIXML(writer, ii2);
			if(!each.getPropertiesMap().isEmpty()) {
				PersistenceUtils.writeAlignmentProperties(writer, each);
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		Logger.getLogger(getClass().getName()).info("Saved local alignments to " + target);
		writer.flush();
		writer.close();
		out.close();
	}
	
	
}
