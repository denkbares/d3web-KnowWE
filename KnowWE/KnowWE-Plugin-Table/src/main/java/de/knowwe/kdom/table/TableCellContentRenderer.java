/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.kdom.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;

/**
 * <p>
 * Renders the content of a <code>TableCellContent</code> element depending on
 * the state of the QuickEditFlag. If <code>TRUE</code> each cell is rendered as
 * an HTML input field, containing the text of the cell.
 * </p>
 * <p>
 * If the <code>value</code> attribute (@see Table) is given the input filed is
 * replaced by an drop down list. If <code>FALSE</code> simple text is rendered.
 * </p>
 * 
 * <p>
 * e.g:
 * </p>
 * <code>
 * Cell given in JSPWiki syntax "| cell 1"
 * =>
 * "&lt;input type='text' name='sectionID' id='sectionID' value='cell 1' /&gt;"
 * </code>
 * 
 * <p>
 * where <code>sectionID</code> is the id in the KDOM.
 * </p>
 * 
 * @author smark
 * @see Renderer
 * @see Table
 */
public class TableCellContentRenderer implements Renderer {

	/**
	 * Determines of the DelegateRenderer is called for content
	 */
	private boolean callDelegate;

	/**
	 * Creates a new renderer.
	 * 
	 * @param callDelegate determines if the delegateRenderer should be called
	 *        for rendering the content
	 */
	public TableCellContentRenderer(boolean callDelegate) {
		this.callDelegate = callDelegate;
	}

	/**
	 * Creates a new Renderer which calls the delegateRenderer
	 */
	public TableCellContentRenderer() {
		this(true);
	}

	@Override
	public void render(Section<?> sec, UserContext user, StringBuilder string) {

		String content;

		if (callDelegate) {
			StringBuilder builder = new StringBuilder();
			DelegateRenderer.getInstance().render(sec, user, builder);
			content = builder.toString();
		}
		else {
			content = sec.getText();
		}

		string.append(wrappContent(content, sec, user));
	}

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	protected String wrappContent(String sectionText, Section<?> sec, UserContext user) {

		String sectionID = sec.getID();
		StringBuilder html = new StringBuilder();

		boolean sort = TableUtils.sortTest(sec);
		boolean tablehead = TableCellContent.isTableHeadContent(sec);

		if (sort) {
			html.append("<th class=\"sort\">");
		}
		else if (tablehead) {
			html.append("<th>");
		}
		else {
			html.append("<td>");
		}

		generateContent(sectionText, sec, user, sectionID, html);

		if (sort || tablehead) {
			html.append("</th>");
		}
		else {
			html.append("</td>");
		}
		return Strings.maskHTML(html.toString());
	}

	protected void generateContent(String sectionText, Section<?> s,
			UserContext user, String sectionID, StringBuilder html) {

		html.append(translateTextForView(sectionText, s));
	}

	protected String translateTextForView(String sectionText, Section<?> sec) {
		// can be overriden by subclasses
		return sectionText;
	}

	public boolean isCallDelegate() {
		return callDelegate;
	}

	public void setCallDelegate(boolean callDelegate) {
		this.callDelegate = callDelegate;
	}

}
