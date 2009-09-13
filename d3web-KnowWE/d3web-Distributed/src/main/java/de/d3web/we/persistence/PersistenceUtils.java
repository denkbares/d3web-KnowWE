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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import de.d3web.we.alignment.Alignment;
import de.d3web.we.alignment.NumericalIdentity;
import de.d3web.we.alignment.SolutionIdentity;
import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.alignment.type.IdentityAlignType;
import de.d3web.we.alignment.type.NoAlignType;
import de.d3web.we.alignment.type.NumericalIdentityAlignType;
import de.d3web.we.alignment.type.SolutionIdentityAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.TerminologyServer;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

public class PersistenceUtils {

	public static void writeIIXML(XMLStreamWriter writer, IdentifiableInstance ii) throws XMLStreamException {
		writer.writeStartElement("IdentifiableInstance");
		writer.writeAttribute("Namespace", ii.getNamespace());
		writer.writeAttribute("ID", ii.getObjectId());
		if(ii.getValue() instanceof NumericalIdentity) {
			writer.writeAttribute("ClassValue", NumericalIdentity.class.getName());
		} else if(ii.getValue() instanceof SolutionIdentity) {
			writer.writeAttribute("ClassValue", SolutionIdentity.class.getName());
		} else {
			writer.writeAttribute("Value", String.valueOf(ii.getValue()));
		}
		writer.writeEndElement();
	}
	
	public static IdentifiableInstance getII(XMLStreamReader parser) {
		String localName = parser.getAttributeLocalName(2);
		String valueString = parser.getAttributeValue(2);
		Object value = null;
		if(localName.equals("ClassValue")) {
			try {
				value = Class.forName(valueString).newInstance();
			} catch (Exception e) {
				// well, bad luck...
			}
		} else {
			value = valueString;
		}
		if(value == null || value.equals("null") || value.equals("")) {
			value = null;
		}
		IdentifiableInstance result = new IdentifiableInstance(parser.getAttributeValue(0), parser.getAttributeValue(1), value);
		return result;
	}
	
	public static void writeTermXML(XMLStreamWriter writer, Term term, boolean withInfos, boolean withEndTag) throws XMLStreamException {
		writer.writeCharacters("\n");
		writer.writeStartElement("Term");
		writer.writeCharacters("\n");
		writeTermInfoMap(writer, term, withInfos);
		writer.writeCharacters("\n");
		if(withEndTag) writer.writeEndElement();
	}
	
	public static Term getExistingTerm(XMLStreamReader parser, TerminologyServer ts, TerminologyType termType) throws XMLStreamException, ClassNotFoundException {
		// Term tag already parsed
		Term term = new Term(termType);
		parser.nextTag();
		parseTermInfoMap(parser, term);
		parser.nextTag();
		for (GlobalTerminology gt : ts.getGlobalTerminologies()) {
			Term newTerm = gt.getTerm((String) term.getInfo(TermInfoType.TERM_NAME), term.getInfo(TermInfoType.TERM_VALUE));
			if(newTerm != null) {
				return newTerm;
			}
		}
		
		return null;
	}
	
	public static Term getTerm(XMLStreamReader parser, boolean tree, TerminologyType termType) throws XMLStreamException, ClassNotFoundException {
		// Term tag already parsed
		Term term = new Term(termType);
		parser.nextTag();
		parseTermInfoMap(parser, term);
		if (!tree) parser.nextTag();
		return term;
	}
		
	
	public static void parseAlignmentProperties(XMLStreamReader parser, Alignment alignment) throws XMLStreamException {
		int event = parser.nextTag();
		event = parser.nextTag();
		if(event == XMLStreamConstants.START_ELEMENT && parser.getLocalName().equals("AlignmentProperties")) {
			while((parser.nextTag()) == XMLStreamConstants.START_ELEMENT) { 
				if(parser.getLocalName().equals("Property")) {
					String key = parser.getAttributeValue(0);
					Boolean value = Boolean.valueOf(parser.getAttributeValue(1));
					alignment.setProperty(key, value);
				}
			}
		}
	}
	
	public static void writeAlignmentProperties(XMLStreamWriter writer, Alignment alignment) throws XMLStreamException {
		writer.writeStartElement("AlignmentProperties");
		writer.writeCharacters("\n");
		for (String key : alignment.getPropertiesMap().keySet()) {
			Boolean value = alignment.getProperty(key);
			writer.writeStartElement("Property");
			writer.writeAttribute("key", key);
			writer.writeAttribute("value", value.toString());
			writer.writeEndElement();
			writer.writeCharacters("\n");
		}
		writer.writeEndElement();
	}
	
	
	public static void parseTermInfoMap(XMLStreamReader parser, Term term) throws XMLStreamException, ClassNotFoundException {
		// TermInfos already parsed
		while((parser.nextTag()) == XMLStreamConstants.START_ELEMENT) { 
			if(parser.getLocalName().equals("TermInfo")) {
				TermInfoType type = TermInfoType.getType(parser.getAttributeValue(0));
				Class objClass = Class.forName(parser.getAttributeValue(1));
				String objString = parser.getElementText();
				term.setInfo(type, getValue(objClass, objString));
			}
		}
	}
	
	private static Object getValue(Class objClass, String objString) {
		if(TerminologyType.class.isAssignableFrom(objClass)) {
			return TerminologyType.getType(objString);
		} else if(objClass.equals(NumericalIdentity.class)) {
			return new NumericalIdentity();
		} else if(objClass.equals(SolutionIdentity.class)) {
			return new SolutionIdentity();
		} else if(objClass.equals(String.class)) {
			return objString;
		} 
		return null;
	}

	public static void writeTermInfoMap(XMLStreamWriter writer, Term term, boolean complete) throws XMLStreamException {
		writer.writeStartElement("TermInfos");
		writer.writeCharacters("\n");
		for (TermInfoType eachType : term.getTermInfos().keySet()) {
			if(complete || eachType.equals(TermInfoType.TERM_NAME) || eachType.equals(TermInfoType.TERM_VALUE)) {
				Object obj = term.getTermInfos().get(eachType);
				writer.writeStartElement("TermInfo");
				writer.writeAttribute("TermInfoType", eachType.getName());
				writer.writeAttribute("TermInfoClass", obj.getClass().getName());
				writer.writeCData(term.getInfo(eachType).toString());
				writer.writeEndElement();
				writer.writeCharacters("\n");
			}
		}
		writer.writeEndElement();
	}
	
	public static AbstractAlignType getType(String attributeValue) {
		if(attributeValue.equals(NumericalIdentityAlignType.getInstance().toString())) {
			return NumericalIdentityAlignType.getInstance();
		}
		if(attributeValue.equals(IdentityAlignType.getInstance().toString())) {
			return IdentityAlignType.getInstance();
		}
		if(attributeValue.equals(SolutionIdentityAlignType.getInstance().toString())) {
			return SolutionIdentityAlignType.getInstance();
		}
		if(attributeValue.equals(NoAlignType.getInstance().toString())) {
			return NoAlignType.getInstance();
		}
		return null;
	}
	
}
