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

package de.d3web.we.kdom.rendering;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>
 * The <code>EditSectionRenderer</code> renders the content of a section
 * depending on the edit flag state. If the edit flag is set for the current
 * section, the section will be wrapped into an HTML textarea which allows the
 * user to edit the content of the section. The text of the HTML textarea is the
 * original text of the section. So be carefully when editing the lines.
 * </p>
 * 
 * @author smark
 * @since 2009/10/18
 */
public class EditSectionRenderer extends KnowWEDomRenderer {

	KnowWEDomRenderer renderer;

	public EditSectionRenderer() {
		this(DelegateRenderer.getInstance());
	}

	public EditSectionRenderer(KnowWEDomRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public final void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {

		StringBuilder subTreeContent = new StringBuilder();
		renderer.render(article, sec, user, subTreeContent);
		if (subTreeContent.length() == 0) {
			// if the content renders to nothing, also no quick-edit button is
			// set.
			return;
		}

		boolean isEditable = sec.hasQuickEditModeSet(user.getUsername());
		// Specifies whether the whole content is rendered in one line or not
		boolean isInline = isInline(sec);
		boolean highlight = false;
		Map<String, String> urlParameterMap = user.getUrlParameterMap();

		// don't append if multiline or javascript action (like
		// ReRenderContentPartAction)
		if (isInline && !urlParameterMap.containsKey("action")) {
			insertTableBeforeMarkup(string, sec);
			string.append(KnowWEUtils.maskHTML("</td><td style=\"vertical-align: top; padding: 0px\">"));
		}
		String highlightKDOMID = urlParameterMap.get("highlight");
		if (highlightKDOMID != null) {
			if (highlightKDOMID.equals(sec.getID())) {
				highlight = true;
			}
		}

		String editKDOMID = urlParameterMap.get("edit");
		if (editKDOMID != null) {
			if (editKDOMID.equals(sec.getID())) {
				isEditable = true;
			}
		}

		if (highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("<div class=\"searchword\">"));
		}
		string.append(KnowWEUtils.maskHTML("<a name=\"" + sec.getID() + "\"></a>"));

		string.append(KnowWEUtils.maskHTML("<div id=\"" + sec.getID() + "\" style=\"width: 100%\">"));
		if (sec.getArticle().equals(article)) {
			string.append(KnowWEUtils.maskHTML(this.generateQuickEdit("Quickedit "
					+ sec.getObjectType().getName() + " Section", sec.getID(), isEditable, user,
					isInline)));
		}
		if (isEditable) {
			// Setting pre-Environment to avoid textarea content being rendered
			// by JSPWiki if page was refreshed (while QuickEdit being opened).
			// But only if there is no other one around it.
			boolean preNeeded = !user.getUrlParameterMap().containsKey("action")
					&& !UserSettingsManager.getInstance().quickEditIsInPre(sec.getID(),
							user.getUsername(), sec.getTitle());
			if (preNeeded) {
				string.append("{{{");
			}
			String str = sec.getOriginalText();
			// padding right: 60px = space for the buttons
			string.append(KnowWEUtils.maskHTML("<div style=\"padding-right: 60px\"><textarea name=\"default-edit-area\" id=\""
					+ sec.getID()
					+ "/default-edit-area\" style=\"width:100%; height:"
					+ this.getHeight(str, isInline) + "px;\">"));
			string.append(str);
			string.append(KnowWEUtils.maskHTML("</textarea></div>"));
			if (preNeeded) {
				string.append("}}}");
			}
			string.append(KnowWEUtils.maskHTML("<div class=\"default-edit-handle\"></div>"));
		}
		else {
			// here the normal rendered content of the subtree is appended
			// for technical reasons this string is already assembled at the
			// beginning of this method
			string.append(subTreeContent);
		}
		string.append(KnowWEUtils.maskHTML("</div>"));
		if (highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
		// Close the Inline-table which was opened in front of the Markup.
		if (isInline && !urlParameterMap.containsKey("action")) {
			string.append(KnowWEUtils.maskHTML("</td></tr></table>"));
		}
	}

	/**
	 * Generates a link used to enable or disable the Quick-Edit-Flag.
	 * 
	 * @param id - of the section the flag should assigned to.
	 * @param user - to get language preferences.
	 * @param topic - name of the current page.
	 * @param isInline - if accept and cancel button are in one line or not.
	 * @return The quick edit menu panel.
	 */
	protected String generateQuickEdit(String tooltip, String id, boolean isEditable, KnowWEUserContext user, Boolean isInline) {
		StringBuilder b = new StringBuilder();
		final ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		b.append("<div " + getQuickEditDivAttributes() + ">");
		if (!isEditable) {
			b.append("<img src=\"KnowWEExtension/images/pencil.png\" width=\"10\" title=\""
					+ tooltip + "\" class=\"quickedit default pointer\" rel=\"{id : '" + id
					+ "'}\" name=\"" + id + "_pencil\"/><br />");
		}
		if (isEditable) {
			// Accept Button
			b.append("<input class=\"pointer\" rel=\"{id : '"
					+ id
					+ "'}\" style=\"padding:0 0 0 0; width: 25px; height: 25px; background: #FFF url(KnowWEExtension/images/msg_checkmark.png) no-repeat; border: none; vertical-align:top;\" name=\""
					+ id + "_accept\" type=\"submit\" value=\"\" title=\""
					+ rb.getString("KnowWE.TableContentRenderer.accept") + "\">");
			if (!isInline) b.append("<br/>");
			// Cancel Button
			b.append("<img class=\"quickedit default pointer\" rel=\"{id : '" + id
					+ "'}\" width=\"25\" title=\""
					+ rb.getString("KnowWE.TableContentRenderer.cancel")
					+ "\" src=\"KnowWEExtension/images/msg_cross.png\" name=\"" + id
					+ "_cancel\"/>");
		}
		b.append("</div>");
		return b.toString();
	}

	protected String getQuickEditDivAttributes() {
		return "class=\"right\"";
	}

	/**
	 * Calculates the height of the HTML textarea.
	 * 
	 * @param str - The string used to calculate the height.
	 * @param isInline - If true the textarea gets no additional newlines.
	 * @return The height of the HTML textarea element.
	 */
	private Integer getHeight(String str, Boolean isInline) {
		int additionallines = 5;
		if (isInline) additionallines = 0;
		int linebreaks = str.split("\n|\f").length;
		int lineHeight = 18; // px
		return (linebreaks + additionallines) * lineHeight;
	}

	/**
	 * Searches the first ancestor Section of section with some text right in
	 * front of the section's one, and checks whether both are separated by '\n'
	 * or '\f' or not (inline).
	 * 
	 * @created 07.08.2010
	 * @param sec The section used by the ESR which could be inline.
	 * @return True if the section (its OrignialText) is in the same line as the
	 *         text before; false if they are seperated by '\n' or '\f'.
	 */
	private boolean isInline(Section section) {
		Section sec = section;
		String text = sec.getOriginalText();
		if (text.startsWith("\n") || text.startsWith("\f") || text.length() == 0) return false;
		KnowWEArticle rootTypeObj = sec.getArticle().getSection().getObjectType();
		// Move up the Section-DOM till you find one with 'more' OriginalText
		while (sec.getFather().getObjectType() != rootTypeObj) {
			sec = sec.getFather();
			Matcher m = Pattern.compile(text, Pattern.LITERAL).matcher(sec.getOriginalText());
			m.find();
			// Text BEFORE section shouldn't end with '\n' or '\f'
			if (m.start() != 0) {
				String textBefore = sec.getOriginalText().substring(0, m.start());
				return !textBefore.endsWith("\n") && !textBefore.endsWith("\f");
			}
		}
		return false;
	}

	/**
	 * This method helps to realize the Inline-QuickEdit-Rendering. Therefore it
	 * takes the last line of the actual string being rendered so far. The
	 * table-code is inserted right in front of the last (can be changed in
	 * while loop) text (which is no HTML-code) in this line.
	 * 
	 * @created 10.08.2010
	 * @param string - The so far rendered page content.
	 * @param sec - The section used by the ESR, used for the table's name.
	 */
	private void insertTableBeforeMarkup(StringBuilder string, Section sec) {
		String[] lineSplit = string.toString().split("\n");
		String lastLine = lineSplit[lineSplit.length - 1];
		Integer charsTillLS = string.length() - lastLine.length();
		Integer startInLine = 0;
		// Find Text between HTML-Elements (>...(<))
		Matcher m = Pattern.compile(
				KnowWEEnvironment.HTML_GT + "(?!" + KnowWEEnvironment.HTML_ST + ")" + "(.(?!("
						+ KnowWEEnvironment.HTML_ST + "|" + KnowWEEnvironment.HTML_GT + ")))*.").matcher(
				lastLine);
		// Get the last match
		while (!m.hitEnd()) {
			m.find();
		}
		try {
			startInLine = m.start() + KnowWEEnvironment.HTML_GT.length();
		}
		catch (IllegalStateException ise) {
			// No Text after/between HTML-tags found -> probably no HTML-code,
			// insert at the beginning of the last line (startInLine = 0)
		}
		string.insert(
				startInLine + charsTillLS,
				KnowWEUtils.maskHTML("<table name=\""
						+ sec.getID()
						+ "_inlinetableByESR\" style=\"width: 100%\"><tr><td style=\"width: 1em; vertical-align: top; padding: 0px;\">"));

	}
}
