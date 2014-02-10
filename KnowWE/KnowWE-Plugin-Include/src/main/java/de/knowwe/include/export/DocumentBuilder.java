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

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.knowwe.core.kdom.parsing.Section;

/**
 * Interface to specify a document builder that is responsible to create a
 * printed media artifact.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 09.02.2014
 */
public interface DocumentBuilder {

	public enum Style {
		title("Title"),
		heading1("Heading1"),
		heading2("Heading2"),
		heading3("Heading3"),
		text("TextBody"),
		code("Code"),
		caption("Caption"),
		image("Image"),
		list("ListParagraph"),
		tableHeader("TableHeader"),
		tableText("TableBody");

		private final String styleName;

		private Style(String styleName) {
			this.styleName = styleName;
		}

		public String getStyleName() {
			return styleName;
		}
	}

	/**
	 * Exports a section and all contained sub-sections using the exporters of
	 * this document writer
	 * 
	 * @created 07.02.2014
	 * @param section the section to be exported
	 * @throws ExportException
	 */
	public void exportSection(Section<?> section) throws ExportException;

	/**
	 * Returns the document we are currently building
	 * 
	 * @created 09.02.2014
	 * @return the document of this builder
	 */
	public XWPFDocument getDocument();

	/**
	 * Closes a paragraph. If it is already closed, nothing happens. After
	 * closing the paragraph, the methods like {@link #getParagraph()} will
	 * create a new one.
	 * 
	 * @created 07.02.2014
	 */
	public void closeParagraph();

	/**
	 * Returns the current paragraph or creates a new one if no current one
	 * exists.
	 * 
	 * @return the current paragraph
	 * @created 07.02.2014
	 */
	public XWPFParagraph getParagraph();

	/**
	 * Returns the current paragraph of the specified style. It creates a new
	 * paragraph if there is no current one or if the current one has the wrong
	 * style.
	 * 
	 * @param style the style the paragraph shall have
	 * @return the current paragraph of the specified style
	 * @created 07.02.2014
	 */
	public XWPFParagraph getParagraph(Style style);

	/**
	 * Returns a newly created paragraph. If there has been any paragraph opened
	 * before the previous one is closed.
	 * 
	 * @return a newly created paragraph
	 * @created 07.02.2014
	 */
	public XWPFParagraph getNewParagraph();

	/**
	 * Returns a newly created paragraph with the specified style. If there has
	 * been any paragraph opened before the previous one is closed.
	 * 
	 * @param style the style the paragraph shall have
	 * @return a newly created paragraph
	 * @created 07.02.2014
	 */
	public XWPFParagraph getNewParagraph(Style style);

	/**
	 * Appends a new text run to the actual section. If currently no paragraph
	 * is available, a new one is created.
	 * 
	 * @created 09.02.2014
	 * @param text the text to be appended
	 * @return
	 */
	public XWPFRun append(String text);

	/**
	 * Appends a new text run to the current paragraph with the specified style.
	 * If currently no paragraph is available, or if the current paragraph's
	 * style is not the specified style, a new one is created.
	 * 
	 * @created 09.02.2014
	 * @param style the style the paragraph shall have
	 * @param text the text to be appended
	 * @return the run created for the text, may be used for further formatting
	 */
	public XWPFRun append(Style style, String text);

	/**
	 * Returns the export manager that is responsible for this document builder.
	 * 
	 * @created 10.02.2014
	 * @return the export manager
	 */
	public ExportManager getManager();

}