/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;

/**
 * Manages the export of the included wiki pages into a specific export
 * artifact.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class ExportManager {

	private List<Exporter<?>> exporters = new LinkedList<Exporter<?>>();
	private DefaultBuilder builder = null;
	private List<Message> messages = new LinkedList<Message>();

	public ExportManager() {
		// add exporters
		exporters.add(new WikiBookPropertyExporter());
		exporters.add(new TOCExporter());
		exporters.add(new BoldExporter());
		exporters.add(new ItalicExporter());
		exporters.add(new WikiTextExporter());
		exporters.add(new PlainTextExporter());
		exporters.add(new HeaderExporter());
		exporters.add(new IncludeExporter());
		exporters.add(new ParagraphExporter());
		exporters.add(new ListExporter());
		exporters.add(new TableExporter());
		exporters.add(new ImageExporter());

		// default exporter
		exporters.add(new DefaultMarkupExporter());
	}

	public List<Exporter<?>> getExporters() {
		return Collections.unmodifiableList(exporters);
	}

	public XWPFDocument createDocument(Section<?> section) throws IOException {
		this.builder = new DefaultBuilder(this);
		this.messages.clear();
		builder.export(section);
		// properties.setRevisionProperty("13");
		// properties.setLastModifiedByProperty(lastModifiedBy);
		// properties.setModifiedProperty(lastModified);
		return builder.getDocument();
	}

	/**
	 * Sets a document property of the currently exported document
	 * 
	 * @created 11.02.2014
	 * @param key the property key to be set
	 * @param value the property value to be set
	 */
	public void setProperty(String key, String value) {
		XWPFDocument document = builder.getDocument();
		try {
			PackageProperties properties = document.getPackage().getPackageProperties();
			if (Strings.equalsIgnoreCase("author", key)
					|| Strings.equalsIgnoreCase("autor", key)) {
				properties.setCreatorProperty(Strings.trim(value));
			}
			else if (Strings.equalsIgnoreCase("title", key)
					|| Strings.equalsIgnoreCase("titel", key)) {
				properties.setTitleProperty(Strings.trim(value));
			}
			else if (Strings.equalsIgnoreCase("project", key)
					|| Strings.equalsIgnoreCase("projekt", key)) {
				properties.setSubjectProperty(Strings.trim(value));
			}
			else if (Strings.equalsIgnoreCase("subject", key)
					|| Strings.equalsIgnoreCase("betreff", key)) {
				properties.setSubjectProperty(Strings.trim(value));
			}
			else {
				addMessage(Type.WARNING, "unsupported document property '" + key + "'");
			}
		}
		catch (InvalidFormatException e) {
			addMessage(Type.WARNING, "unexpected format exception");
		}
	}

	/**
	 * Adds a new message to the protocol of this document export.
	 * 
	 * @created 11.02.2014
	 * @param type the type of the message
	 * @param text the message text
	 */
	public void addMessage(Type type, String text) {
		messages.add(new Message(type, text));
	}

}
