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

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 09.02.2014
 */
public class DefaultBuilder implements DocumentBuilder {

	private final ExportManager manager;
	private final XWPFDocument document;
	protected XWPFParagraph paragraph = null;

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

	public void exportSection(Section<?> section) throws ExportException {
		// try to export section
		for (Exporter<?> export : getManager().getExporters()) {
			if (exportSection(section, export)) return;
		}

		// if not, export all child sections
		for (Section<?> child : section.getChildren()) {
			exportSection(child);
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
			paragraph.setStyle(getDefaultStyle().getStyleName());
		}
		return paragraph;
	}

	protected Style getDefaultStyle() {
		return Style.text;
	}

	@Override
	public XWPFParagraph getParagraph(Style style) {
		String styleName = paragraph == null ? null : paragraph.getStyle();
		if (Strings.equalsIgnoreCase(styleName, style.getStyleName())) {
			return paragraph;
		}
		return getNewParagraph(style);
	}

	@Override
	public XWPFParagraph getNewParagraph() {
		closeParagraph();
		return getParagraph();
	}

	@Override
	public XWPFParagraph getNewParagraph(Style style) {
		getNewParagraph();
		paragraph.setStyle(style.getStyleName());
		return paragraph;
	}

	@Override
	public XWPFRun append(String text) {
		XWPFRun run = getParagraph().createRun();
		run.setText(text);
		return run;
	}

	@Override
	public XWPFRun append(Style style, String text) {
		XWPFRun run = getParagraph(style).createRun();
		run.setText(text);
		return run;
	}

}
