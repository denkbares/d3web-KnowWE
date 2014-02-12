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
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;

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

		/**
		 * Returns the heading of the specified level. If there is no such
		 * heading (<1 or >9) an {@link IllegalArgumentException} is thrown
		 * 
		 * @created 11.02.2014
		 * @param headingLevel the level of the heading where 1 is the most
		 *        significant one
		 * @return the style of that heading
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
		 * Returns the level of the heading style or 0 if the style is no
		 * heading style.
		 * 
		 * @created 11.02.2014
		 * @return the heading level
		 */
		public int getHeadingLevel() {
			int l = ordinal() - heading1.ordinal() + 1;
			return (l < 1 || l > 9) ? 0 : l;
		}
	}

	/**
	 * Sets if all styles shall be additionally be bold face or not.
	 * 
	 * @created 10.02.2014
	 * @param bold true if bold shall be on
	 */
	void setBold(boolean bold);

	/**
	 * Sets if all styles shall be additionally be italics or not.
	 * 
	 * @created 10.02.2014
	 * @param italic true if italic shall be on
	 */
	void setItalic(boolean italic);

	/**
	 * Sets if all headers shall be displayed without numbering, even if
	 * specified in the styles of used template. Please note that specifying
	 * "false" will not force numbering to be on, it only reverts to use the
	 * style as defined in the template.
	 * 
	 * @created 11.02.2014
	 * @param suppress if header numbering shall be suppressed
	 */
	void setSuppressHeaderNumbering(boolean suppress);

	/**
	 * Returns if all headers shall be displayed without numbering, even if
	 * specified in the styles of used template. Please note that a value of
	 * "false" will not force numbering to be on, it then uses the style as
	 * defined in the template.
	 * 
	 * @created 11.02.2014
	 * @return if header numbering shall be suppressed
	 */
	boolean isSuppressHeaderNumbering();

	/**
	 * Increases the header level (or decreases if delta is negative) by the
	 * delta level. So all created heading sections will be of a modified level.
	 * If delta is set to 2 a heading of 3 will become a heading of 5.
	 * 
	 * @created 10.02.2014
	 * @param delta the delta to be added to the headings
	 */
	void incHeaderLevel(int delta);

	/**
	 * Exports the specified sections and all contained sub-sections using the
	 * exporters of this document writer
	 * 
	 * @created 07.02.2014
	 * @param section the section to be exported
	 * @throws ExportException
	 */
	public void export(Collection<Section<?>> sections) throws ExportException;

	/**
	 * Exports a section and all contained sub-sections using the exporters of
	 * this document writer
	 * 
	 * @created 07.02.2014
	 * @param section the section to be exported
	 * @throws ExportException
	 */
	public void export(Section<?> section) throws ExportException;

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

	/**
	 * Adds a new message to the protocol of this document export.
	 * 
	 * @created 11.02.2014
	 * @param message the message to be added
	 */
	void addMessage(Message message);

	/**
	 * Returns the messages of this document export.
	 * 
	 * @created 12.02.2014
	 * @return the list of all messages
	 */
	public List<Message> getMessages();

	/**
	 * Sets a document property of the currently exported document
	 * 
	 * @created 11.02.2014
	 * @param key the property key to be set
	 * @param value the property value to be set
	 */
	public void setProperty(String key, String value);

}