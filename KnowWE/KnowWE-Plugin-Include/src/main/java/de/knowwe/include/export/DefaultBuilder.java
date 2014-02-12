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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 09.02.2014
 */
public class DefaultBuilder implements DocumentBuilder {

	private final ExportManager manager;
	private final XWPFDocument document;
	private final List<Message> messages = new LinkedList<Message>();

	protected XWPFParagraph paragraph = null;

	private boolean bold;
	private boolean italic;
	private boolean suppressHeaderNumbering = false;
	private int level = 0;

	/**
	 * Creates a new default builder for the default template.
	 */
	public DefaultBuilder(ExportManager manager) throws IOException {
		this.manager = manager;

		// create new document based on template
		this.document = new XWPFDocument(getClass().getResourceAsStream(
				"/de/knowwe/include/export/template.docx"));

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

	@Override
	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	@Override
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
					|| Strings.equalsIgnoreCase("projekt", key)) {
				properties.setSubjectProperty(Strings.trim(value));
			}
			else if (Strings.equalsIgnoreCase("subject", key)
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

	@Override
	public void addMessage(Message message) {
		messages.add(message);
	}

	@Override
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	@Override
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	@Override
	public void setSuppressHeaderNumbering(boolean suppressHeaderNumbering) {
		this.suppressHeaderNumbering = suppressHeaderNumbering;
	}

	@Override
	public boolean isSuppressHeaderNumbering() {
		return suppressHeaderNumbering;
	}

	@Override
	public void incHeaderLevel(int delta) {
		level += delta;
	}

	/**
	 * Creates a new default builder that decorates the existing builder and
	 * continues to write it's existing document.
	 */
	protected DefaultBuilder(DocumentBuilder decorated) {
		this.manager = decorated.getManager();
		this.document = decorated.getDocument();
	}

	protected XWPFParagraph createParagraph() {
		return getDocument().createParagraph();
	}

	@Override
	public ExportManager getManager() {
		return manager;
	}

	@Override
	public XWPFDocument getDocument() {
		return document;
	}

	@Override
	public void export(Collection<Section<?>> sections) throws ExportException {
		for (Section<?> section : sections) {
			export(section);
		}
	}

	@Override
	public void export(Section<?> section) {
		// try to export section
		for (Exporter<?> export : getManager().getExporters()) {
			try {
				if (exportSection(section, export)) return;
			}
			catch (ExportException e) {
				addMessage(Messages.error(e));
			}
		}

		// if not, export all child sections
		for (Section<?> child : section.getChildren()) {
			export(child);
		}
	}

	private <T extends Type> boolean exportSection(Section<?> section, Exporter<T> export) throws ExportException {
		if (!export.getSectionType().isInstance(section.get())) return false;
		Section<T> castedSection = Sections.cast(section, export.getSectionType());
		if (!export.canExport(castedSection)) return false;
		export.export(castedSection, this);
		return true;
	}

	@Override
	public void closeParagraph() {
		paragraph = null;
	}

	@Override
	public XWPFParagraph getParagraph() {
		if (paragraph == null) {
			paragraph = createParagraph();
			applyStyle(getDefaultStyle());
		}
		return paragraph;
	}

	protected Style getDefaultStyle() {
		return Style.text;
	}

	protected Style mapStyle(Style style) {
		int l = style.getHeadingLevel();
		return (l == 0) ? style : Style.heading(l + level);
	}

	@Override
	public XWPFParagraph getParagraph(Style style) {
		return hasStyle(style) ? paragraph : getNewParagraph(style);
	}

	@Override
	public XWPFParagraph getNewParagraph() {
		closeParagraph();
		return getParagraph();
	}

	@Override
	public XWPFParagraph getNewParagraph(Style style) {
		getNewParagraph();
		applyStyle(style);
		return paragraph;
	}

	private boolean hasStyle(Style style) {
		String currentStyleName = paragraph == null ? null : paragraph.getStyle();
		String otherStyleName = mapStyle(style).getStyleName();
		return Strings.equalsIgnoreCase(currentStyleName, otherStyleName);
	}

	protected void applyStyle(Style style) {
		paragraph.setStyle(mapStyle(style).getStyleName());
		if (suppressHeaderNumbering) {
			paragraph.setNumID(null);
		}
	}

	@Override
	public XWPFRun append(String text) {
		XWPFRun run = getParagraph().createRun();
		return append(text, run);
	}

	@Override
	public XWPFRun append(Style style, String text) {
		XWPFRun run = getParagraph(style).createRun();
		return append(text, run);
	}

	private XWPFRun append(String text, XWPFRun run) {
		if (bold) run.setBold(true);
		if (italic) run.setItalic(true);
		run.setText(text);
		return run;
	}
}
