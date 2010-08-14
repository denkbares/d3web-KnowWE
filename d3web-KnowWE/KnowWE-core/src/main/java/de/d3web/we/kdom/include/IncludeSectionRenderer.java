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

package de.d3web.we.kdom.include;

import java.util.Collection;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class IncludeSectionRenderer extends KnowWEDomRenderer {

	private static IncludeSectionRenderer instance;

	public static IncludeSectionRenderer getInstance() {
		if (instance == null) {
			instance = new IncludeSectionRenderer();
		}
		return instance;
	}

	private IncludeSectionRenderer() {

	}

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {

		String render = sec.getObjectType() instanceof AbstractXMLObjectType ?
				AbstractXMLObjectType.getAttributeMapFor(sec).get("render") : null;

		StringBuilder content = new StringBuilder();

		String srclink;
		if (sec.getObjectType() instanceof Include && Include.getIncludeAddress(sec) != null) {
			Section<Include> child = (Section<Include>) sec.getChildren().get(0);
			String baseURL = KnowWEEnvironment.getInstance().getWikiConnector().getBaseUrl();
			srclink = "<a href=\"" + baseURL + (baseURL.endsWith("/") ? "" : "/")
					+ "Wiki.jsp?page="
					+ Include.getIncludeAddress(sec).getTargetArticle()
					+ (!child.getTitle().equals(sec.getTitle()) ? "#" + child.getID() : "") + "\">"
					+ "Include: src=\"" + Include.getIncludeAddress(sec).getOriginalAddress()
					+ "\"</a>";
		}
		else {
			srclink = "Unknown Source";
		}

		string.append(KnowWEUtils.maskHTML("<div style=\"text-align:left; padding-top:5px; padding-right:5px; "
				+ "padding-left:5px; padding-bottom: 6px; border:thin solid #99CC99\">"));

		List<Section> includedChildren = sec.getChildren();
		if (render != null && render.equalsIgnoreCase("false")
				&& !(includedChildren != null && includedChildren.size() == 1
						&& includedChildren.get(0).getObjectType() instanceof IncludeError)) {

			boolean errors = false;

			Collection<Message> msgsType1 = KnowWEUtils.getMessagesFromSubtree(article, sec,
					Message.class);
			Collection<KDOMWarning> msgsType2 = KnowWEUtils.getMessagesFromSubtree(article, sec,
					KDOMWarning.class);
			Collection<KDOMError> msgsType3 = KnowWEUtils.getMessagesFromSubtree(article, sec,
					KDOMError.class);

			for (Message msg : msgsType1) {
				if (msg.getMessageType().equals(Message.WARNING)
						|| msg.getMessageType().equals(Message.ERROR)) {
					errors = true;
					break;
				}
			}

			if (errors || !msgsType2.isEmpty() || !msgsType3.isEmpty()) {
				renderNormally(article, sec, user, srclink, content, string);
			}
			else {
				string.append(KnowWEUtils.maskHTML("<div style=\"padding-left: 2px;\">"
						+ srclink + "</div></div>"));
			}

		}
		else {
			renderNormally(article, sec, user, srclink, content, string);
		}

		string.append(KnowWEUtils.maskHTML("<p>\n"));
	}

	private void renderNormally(KnowWEArticle article, Section sec, KnowWEUserContext user,
			String srclink, StringBuilder content, StringBuilder string) {
		DelegateRenderer.getInstance().render(article, sec, user, content);
		string.append(content.toString());
		string.append(KnowWEUtils.maskHTML("<div style=\"text-align:right\"><font size=\"1\">"
				+ srclink + "</font></div></div>"));
	}

}
