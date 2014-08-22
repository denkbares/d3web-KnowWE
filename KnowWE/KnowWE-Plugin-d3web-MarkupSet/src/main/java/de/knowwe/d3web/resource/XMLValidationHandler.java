/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.d3web.resource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalHandler;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * This Handler validates an XML file against a schema.
 * 
 * @author Reinhard Hatko
 * @created 08.08.2012
 */
public class XMLValidationHandler extends DefaultGlobalHandler<AttachmentType> {

	@Override
	public Collection<Message> create(DefaultGlobalCompiler compiler, Section<AttachmentType> section) {
		Map<de.knowwe.core.compile.Compiler, Collection<Message>> errors = Messages.getMessagesMapFromSubtree(
				section,
				new Message.Type[] { Message.Type.ERROR });

		// if the attachment is not found, we stop here
		if (!errors.isEmpty()) {
			return Messages.noMessage();
		}

		Section<ResourceType> resourceSection = Sections.ancestor(section,
				ResourceType.class);

		String schemaName = DefaultMarkupType.getAnnotation(resourceSection,
				ResourceType.ANNOTATION_XMLSCHEMA);

		// for an empty annotation, the AttachmentType is not instantiated :-(
		if (schemaName == null) {
			return Messages.noMessage();
		}

		// load the schema file
		Source schemasource;
		try {
			schemasource = loadAsSource(AttachmentType.getAttachment(section));
		}
		catch (IOException e) {
			return Messages.asList(Messages.warning("Could not load schema '" + schemaName + "'."));
		}

		// create a schema from it
		Schema schema;
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(getSchemaType(schemaName));
			schema = schemaFactory.newSchema(schemasource);
		}
		catch (SAXException e) {
			return verbalizeSAXException("Schema", e);
		}

		// create a source for the xml
		Source xml;
		try {
			xml = getXML(resourceSection);
		}
		catch (IOException e) {
			return Messages.asList(Messages.warning("Could not load xml '" + schemaName + "'."));
		}

		// and validate it
		try {
			validate(xml, schema);
		}
		catch (SAXException e) {
			return verbalizeSAXException("XML", e);
		}
		catch (IOException e) {
			return Messages.asList(Messages.warning("Could not validate XML: "
					+ e.getLocalizedMessage()));
		}

		return Messages.noMessage();
	}

	private static String getSchemaType(String schemaName) {
		// TODO only tested for Schema so far
		if (schemaName.endsWith(".xsd")) {
			return XMLConstants.W3C_XML_SCHEMA_NS_URI;
		}
		else if (schemaName.endsWith(".dtd")) {
			return XMLConstants.XML_DTD_NS_URI;
		}
		else {
			return XMLConstants.DEFAULT_NS_PREFIX;
		}
	}

	/**
	 * Returns a Source either from a supplied src-file or the section content
	 */
	private Source getXML(Section<ResourceType> section) throws IOException {
		String src = DefaultMarkupType.getAnnotation(section, ResourceType.ANNOTATION_SRC);
		Source xml;
		if (src != null) {
			xml = loadAsSource(section, src);
		}
		// ignore content, even if present
		// (in accordance with ResourceHandler)
		else {
			String content = DefaultMarkupType.getContent(section);
			xml = new StreamSource(new StringReader(content));
		}

		return xml;
	}

	public Collection<Message> verbalizeSAXException(String source, SAXException e) {
		String location = getLocation(e);
		String message = source + " is not valid: " + location + e.getLocalizedMessage();
		return Messages.asList(Messages.error(message));
	}

	private static String getLocation(SAXException e) {
		if (!(e instanceof SAXParseException)) {
			return " ";
		}

		String location = "";
		SAXParseException parseException = (SAXParseException) e;
		int lineNumber = parseException.getLineNumber();
		int columnNumber = parseException.getColumnNumber();

		if (lineNumber != -1) {
			location = "line " + lineNumber;
		}

		if (columnNumber != -1) {
			location += ", col " + columnNumber;
		}

		return location + ": ";
	}

	private static Source loadAsSource(WikiAttachment attachment) throws IOException {
		return new StreamSource(attachment.getInputStream());
	}

	private static Source loadAsSource(Section<ResourceType> section, String path) throws IOException {
		path.trim();
		if (!path.contains("/")) {
			path = section.getTitle() + "/" + path;
		}

		WikiAttachment attachment = Environment.getInstance().getWikiConnector().getAttachment(
				path);

		if (attachment == null) {
			return null;
		}
		return loadAsSource(attachment);
	}

	private static void validate(Source source, Schema schema) throws SAXException, IOException {

		Validator validator = schema.newValidator();
		validator.validate(source);
	}

}
