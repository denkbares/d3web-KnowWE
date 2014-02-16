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
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * Class that contains some common information about the current export that is
 * shared between all builders.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 16.02.2014
 */
public class ExportModel {

	private final ExportManager manager;
	private final XWPFDocument document;
	private final List<Exporter<?>> exporters;

	private Date maxDate = null;
	private final List<Message> messages = new LinkedList<Message>();

	public ExportModel(ExportManager manager, InputStream templateStream) throws IOException {
		// create new document based on template
		this.manager = manager;
		this.exporters = manager.createExporters();
		this.document = new XWPFDocument(templateStream);

		// delete all undesired example content
		int index = 0;
		for (IBodyElement element : document.getBodyElements()) {
			if (element instanceof XWPFParagraph) {
				XWPFParagraph paragraph = (XWPFParagraph) element;
				if (Strings.equalsIgnoreCase(paragraph.getStyle(), "StartDelete")) {
					break;
				}
			}
			index++;
		}
		while (document.getBodyElements().size() > index) {
			document.removeBodyElement(index);
		}
	}

	/**
	 * Returns the export manager that is responsible for this document builder.
	 * 
	 * @created 10.02.2014
	 * @return the export manager
	 */
	public ExportManager getManager() {
		return manager;
	}

	List<Exporter<?>> getExporters() {
		return exporters;
	}

	/**
	 * Returns the document we are currently building.
	 * 
	 * @created 09.02.2014
	 * @return the document of this model
	 */
	public XWPFDocument getDocument() {
		return document;
	}

	/**
	 * Returns the messages of this document export.
	 * 
	 * @created 12.02.2014
	 * @return the list of all messages
	 */
	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	/**
	 * Adds a new message to the protocol of this document export.
	 * 
	 * @created 11.02.2014
	 * @param message the message to be added
	 */
	public void addMessage(Message message) {
		messages.add(message);
	}

	/**
	 * Updates the modified date with the date the specified Article is modified
	 * 
	 * @created 16.02.2014
	 * @param section
	 */
	public void updateModifiedDate(Article article) {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		String title = article.getTitle();
		int version = connector.getVersion(title);
		Date date = connector.getLastModifiedDate(title, version);
		if (maxDate == null || date.after(maxDate)) {
			maxDate = date;
		}
	}

	/**
	 * Returns the date of the latest change of all exported sections' articles.
	 * 
	 * @created 16.02.2014
	 * @return the last modified date of the whole exported content
	 */
	public Date getModifiedDate() {
		return maxDate;
	}

	/**
	 * Sets a document property of the currently exported document
	 * 
	 * @created 11.02.2014
	 * @param key the property key to be set
	 * @param value the property value to be set
	 */
	public void setProperty(String key, String value) {
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
					|| Strings.equalsIgnoreCase("projekt", key)
					|| Strings.equalsIgnoreCase("subject", key)
					|| Strings.equalsIgnoreCase("betreff", key)) {
				properties.setSubjectProperty(Strings.trim(value));
			}

			// always add as custom property
			document.getProperties().getCustomProperties().addProperty(key, value);
		}
		catch (InvalidFormatException e) {
			addMessage(Messages.warning("unexpected format exception"));
		}
	}
}
