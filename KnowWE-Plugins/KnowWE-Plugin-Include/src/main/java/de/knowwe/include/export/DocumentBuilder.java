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

import java.util.Collection;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Interface to specify a document builder that is responsible to create a printed media artifact.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 09.02.2014
 */
public interface DocumentBuilder {

	@SuppressWarnings("UnnecessaryEnumModifier")
	enum Style {
		title("Title"),
		heading1("Heading1"),
		heading2("Heading2"),
		heading3("Heading3"),
		heading4("Heading4"),
		heading5("Heading5"),
		heading6("Heading6"),
		heading7("Heading7"),
		heading8("Heading8"),
		heading9("Heading9"),
		text("TextBody"),
		code("Code"),
		caption("Caption"),
		image("Image"),
		list("ListBody"),
		tableHeader("TableHeader"),
		tableText("TableBody"),
		footnote("Footnote");

		private final String styleName;

		Style(String styleName) {
			this.styleName = styleName;
		}

		/**
		 * Returns the style that is associated to the specified word template style name. If no such style is found,
		 * null is returned. The style names are matched case insensitive.
		 *
		 * @param styleName the style name to get the style for
		 * @return the style with the specified style name or null
		 */
		public static Style getByStyleName(String styleName) {
			for (Style style : values()) {
				if (Strings.equalsIgnoreCase(styleName, style.getStyleName())) {
					return style;
				}
			}
			return null;
		}

		public String getStyleName() {
			return styleName;
		}

		/**
		 * Returns the heading of the specified level. If there is no such heading (<1 or >9) an {@link
		 * IllegalArgumentException} is thrown
		 *
		 * @param headingLevel the level of the heading where 1 is the most significant one
		 * @return the style of that heading
		 * @created 11.02.2014
		 */
		public static Style heading(int headingLevel) {
			if (headingLevel < 1 || headingLevel > 9) {
				throw new IllegalArgumentException(
						"heading level exceeded supported depth: " + headingLevel);
			}
			int ordinal = heading1.ordinal() - 1 + headingLevel;
			return values()[ordinal];
		}

		/**
		 * Returns the level of the heading style or 0 if the style is no heading style.
		 *
		 * @return the heading level
		 * @created 11.02.2014
		 */
		public int getHeadingLevel() {
			int level = ordinal() - heading1.ordinal() + 1;
			return (level < 1 || level > 9) ? 0 : level;
		}
	}

	/**
	 * Sets if all styles shall be additionally be bold face or not.
	 *
	 * @param bold true if bold shall be on
	 * @created 10.02.2014
	 */
	void setBold(boolean bold);

	/**
	 * Sets if all styles shall be additionally be italics or not.
	 *
	 * @param italic true if italic shall be on
	 * @created 10.02.2014
	 */
	void setItalic(boolean italic);

	/**
	 * Sets if all styles shall be additionally using a code styled font or not.
	 *
	 * @param code true if code style font shall be forced
	 * @created 10.02.2014
	 */
	void setCode(boolean code);

	/**
	 * Sets if all headers shall be displayed without numbering, even if specified in the styles of used template.
	 * Please note that specifying "false" will not force numbering to be on, it only reverts to use the style as
	 * defined in the template.
	 *
	 * @param suppress if header numbering shall be suppressed
	 * @created 11.02.2014
	 */
	void setSuppressHeaderNumbering(boolean suppress);

	/**
	 * Returns if all headers shall be displayed without numbering, even if specified in the styles of used template.
	 * Please note that a value of "false" will not force numbering to be on, it then uses the style as defined in the
	 * template.
	 *
	 * @return if header numbering shall be suppressed
	 * @created 11.02.2014
	 */
	boolean isSuppressHeaderNumbering();

	/**
	 * Increases the header level (or decreases if delta is negative) by the delta level. So all created heading
	 * sections will be of a modified level. If delta is set to 2 a heading of 3 will become a heading of 5.
	 *
	 * @param delta the delta to be added to the headings
	 * @created 10.02.2014
	 */
	void incHeaderLevel(int delta);

	/**
	 * Exports the specified sections and all contained sub-sections using the exporters of this document writer.
	 *
	 * @param sections the sections to be exported
	 * @throws ExportException
	 * @created 07.02.2014
	 */
	void export(Collection<Section<? extends Type>> sections) throws ExportException;

	/**
	 * Exports a section and all contained sub-sections using the exporters of this document writer.
	 *
	 * @param section the section to be exported
	 * @throws ExportException
	 * @created 07.02.2014
	 */
	void export(Section<?> section) throws ExportException;

	/**
	 * Returns the document we are currently building.
	 *
	 * @return the document of this builder
	 * @created 09.02.2014
	 */
	XWPFDocument getDocument();

	/**
	 * Closes a paragraph. If it is already closed, nothing happens. After closing the paragraph, the methods like
	 * {@link #getParagraph()} will create a new one.
	 *
	 * @created 07.02.2014
	 */
	void closeParagraph();

	/**
	 * Returns the current paragraph or creates a new one if no current one exists.
	 *
	 * @return the current paragraph
	 * @created 07.02.2014
	 */
	XWPFParagraph getParagraph();

	/**
	 * Returns the current paragraph of the specified style. It creates a new paragraph if there is no current one or if
	 * the current one has the wrong style.
	 *
	 * @param style the style the paragraph shall have
	 * @return the current paragraph of the specified style
	 * @created 07.02.2014
	 */
	XWPFParagraph getParagraph(Style style);

	/**
	 * Returns a newly created paragraph. If there has been any paragraph opened before the previous one is closed.
	 *
	 * @return a newly created paragraph
	 * @created 07.02.2014
	 */
	XWPFParagraph getNewParagraph();

	/**
	 * Returns a newly created paragraph with the specified style. If there has been any paragraph opened before the
	 * previous one is closed.
	 *
	 * @param style the style the paragraph shall have
	 * @return a newly created paragraph
	 * @created 07.02.2014
	 */
	XWPFParagraph getNewParagraph(Style style);

	/**
	 * Appends a new text run to the actual section. If currently no paragraph is available, a new one is created.
	 *
	 * @param text the text to be appended
	 * @return the run created to append the specified text
	 * @created 09.02.2014
	 */
	XWPFRun append(String text);

	/**
	 * Appends a new text run to the current paragraph with the specified style. If currently no paragraph is available,
	 * or if the current paragraph's style is not the specified style, a new one is created.
	 *
	 * @param style the style the paragraph shall have
	 * @param text the text to be appended
	 * @return the run created for the text, may be used for further formatting
	 * @created 09.02.2014
	 */
	XWPFRun append(Style style, String text);

	/**
	 * Appends a forced line break to the current text. If currently no paragraph is available, the method call is
	 * ignored. Please note that this method may created different types of breaks, even may create new paragraphs,
	 * depending of where the break is inserted (text, table, ...)
	 *
	 * @created 09.02.2014
	 */
	void appendLineBreak();

	/**
	 * Returns the export model this document builder shall be update.
	 *
	 * @return the export manager
	 * @created 10.02.2014
	 */
	ExportModel getModel();

}