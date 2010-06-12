/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.rendering;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>The <code>EditSectionRenderer</code> renders the content of a section
 * depending on the edit flag state. If the edit flag is set for the current section,
 * the section will be wrapped into an HTML textarea which allows the user to
 * edit the content of the section. The text of the HTML textarea is the original
 * text of the section. So be carefully when editing the lines.</p>
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

		boolean isEditable = sec.hasQuickEditModeSet( user.getUsername() );
		boolean highlight = false;
		Map<String, String> urlParameterMap = user.getUrlParameterMap();


		String highlightKDOMID = urlParameterMap.get("highlight");
		if(highlightKDOMID != null) {
			if(highlightKDOMID.equals(sec.getId())) {
				highlight = true;
			}
		}

		String editKDOMID = urlParameterMap.get("edit");
		if(editKDOMID != null) {
			if(editKDOMID.equals(sec.getId())) {
				isEditable = true;
			}
		}

		if(highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("<div class=\"searchword\">"));
		}
		string.append( KnowWEUtils.maskHTML( "<a name=\""+sec.getId()+"\"></a><div id=\"" + sec.getId() + "\">" ));

		if( sec.getArticle().equals( article ) ) {
			string.append(KnowWEUtils.maskHTML( this.generateQuickEdit
					("Quickedit " + sec.getObjectType().getName() + " Section", sec.getId(),
							isEditable, user)));
		}

		if ( isEditable ) {
			// Setting pre-Environment to avoid textarea content being rendered
			// by JSPWiki if page was refreshed (while QuickEdit being opened).
			// But only if there is no other one around it.
			boolean preNeeded = !user.getUrlParameterMap().containsKey("action")
					&& !UserSettingsManager.getInstance().quickEditIsInPre(sec.getId(),
							user.getUsername(), sec.getTitle());
			if (preNeeded) {
				string.append("{{{");
			}
			String str = sec.getOriginalText();
			string.append( KnowWEUtils.maskHTML( "<textarea name=\"default-edit-area\" id=\"" + sec.getId() + "/default-edit-area\" style=\"width:92%; height:"+this.getHeight(str)+"px;\">" ));
			string.append( str );
			string.append( KnowWEUtils.maskHTML( "</textarea>" ));
			if (preNeeded) {
				string.append("}}}");
			}
			string.append( KnowWEUtils.maskHTML( "<div class=\"default-edit-handle\"></div>" ));
		} else {
			// here the normal rendered content of the subtree is appended
			// for technical reasons this string is already assembled at the
			// beginning of this method
			string.append(subTreeContent);
		}
		string.append( KnowWEUtils.maskHTML( "</div>" ));
		if(highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
	}


	/**
	 * Generates a link used to enable or disable the Quick-Edit-Flag.
	 * @param
	 *     id - of the section the flag should assigned to.
	 * @param
	 * 	   user - to get language preferences.
	 * @param
	 *     topic - name of the current page.
	 * @return
	 *     The quick edit menu panel.
	 */
	protected String generateQuickEdit(String tooltip, String id, boolean isEditable, KnowWEUserContext user) {
		StringBuilder b = new StringBuilder();
		final ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		b.append( "<div " + getQuickEditDivAttributes() + ">" );
		if (!isEditable) {
			b.append("<img src=\"KnowWEExtension/images/pencil.png\" width=\"10\" title=\"" + tooltip + "\" class=\"quickedit default pointer\" rel=\"{id : '" + id + "'}\" name=\"" + id + "_pencil\"/><br />");
		}
		if( isEditable ){
			b.append("<input class=\"pointer\" rel=\"{id : '" + id + "'}\" style=\"padding:0 0 0 0; width: 25px; height: 25px; background: #FFF url(KnowWEExtension/images/msg_checkmark.png) no-repeat; border: none; vertical-align:top;\" name=\"" + id + "_accept\" type=\"submit\" value=\"\" title=\"" + rb.getString("KnowWE.TableContentRenderer.accept") + "\"><br/>" );
			b.append("<img class=\"quickedit default pointer\" rel=\"{id : '" + id + "'}\" width=\"25\" title=\"" + rb.getString("KnowWE.TableContentRenderer.cancel") + "\" src=\"KnowWEExtension/images/msg_cross.png\" name=\"" + id + "_cancel\"/>");
		}
		b.append( "</div>" );
		return b.toString();
	}

	protected String getQuickEditDivAttributes() {
		return "class=\"right\"";
	}

	/**
	 * Calculates the height of the HTML textarea.
	 *
	 * @param
	 *      str - The string used to calculated the height.
	 * @return
	 *      The height of the HTML textarea element.
	 */
	private Integer getHeight( String str ){
		int linebreaks = str.split("\n|\f").length;
		int lineHeight = 18; //px
		return (linebreaks + 5) * lineHeight;
	}

}
