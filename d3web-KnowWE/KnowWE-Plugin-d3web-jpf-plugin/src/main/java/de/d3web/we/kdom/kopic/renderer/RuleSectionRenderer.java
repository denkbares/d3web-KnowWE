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

package de.d3web.we.kdom.kopic.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.report.Message;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.packaging.PackageRenderUtils;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class RuleSectionRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder result) {

		result.append(KnowWEUtils.maskHTML("<a name=\"" + sec.getID() + "\" />"));
		StringBuilder messagesBuilder = new StringBuilder();

		result.append("\n{{{");
		article = PackageRenderUtils.checkArticlesCompiling(article, sec, result);
		int parsedCount = 0;
		if (sec.getObjectType() instanceof AbstractKopicSection) {

			List<Message> sectionMessages = new ArrayList<Message>();
			List<Section> rules = new ArrayList<Section>();
			sec.findSuccessorsOfType(Rule.class, rules);

			List<String> errors = new ArrayList<String>();
			List<String> warnings = new ArrayList<String>();

			String rulesParsed = MessageKnOfficeGenerator.getResourceBundle().getString("rule");
			int rnum = 1;
			for (Section r : rules) {
				Collection<Message> rmsgs = AbstractKnowWEObjectType.getMessages(article, r);
				boolean foundWarning = false;
				boolean foundError = false;
				for (Message msg : rmsgs) {
					if (msg.getMessageText().substring(0, msg.getMessageText().length() - 2)
								.equals(rulesParsed.substring(0, rulesParsed.length() - 4))) {
						parsedCount += msg.getCount();
					}
					else if (msg.getMessageType() == Message.WARNING && foundWarning == false) {
						warnings.add(KnowWEUtils.maskHTML("<a "// href=\"#\" "
								+ "class=\"highligh-rule\" "
									+ "rel=\"{kdomid: '" + r.getID() + "', topic: '"
									+ r.getTitle() + "', depth: 0, breadth: 0}\""
									+ "/>" + rnum + "</a>"));
						foundWarning = true;
					}
					else if (msg.getMessageType() == Message.ERROR && foundError == false) {
						errors.add(KnowWEUtils.maskHTML("<a "// href=\"#\" "
								+ "class=\"highligh-rule\" "
									+ "rel=\"{kdomid: '" + r.getID() + "', topic: '"
									+ r.getTitle() + "', depth: 0, breadth: 0}\""
									+ "/>" + rnum + "</a>"));
						foundError = true;
					}
				}
				rnum++;
			}

			if (sectionMessages != null) {
				// messagesBuilder.append("{{{");
				// messagesBuilder.append(MessageKnOfficeGenerator.createNoteMSG("rule",
				// "", 0, "", parsedCount).getMessageText() + " " +
				// KnowWEUtils.maskHTML("<br>"));
				if (warnings.size() > 0) {
					messagesBuilder.append(wrappSpan(D3webModule.getKwikiBundle_d3web()
							.getString("KnowWE.KopicRenderer.RulesSection.warnings") + " "
							+ warnings.toString(), "warning"));
				}
				if (errors.size() > 0) {
					messagesBuilder.append(wrappSpan(D3webModule.getKwikiBundle_d3web()
							.getString("KnowWE.KopicRenderer.RulesSection.errors") + " "
							+ errors.toString(), "error"));
				}
				messagesBuilder.append("\n");
			}

			// result.append("%%collapsebox-closed \n");

			// result.append(generateTitle(sec, user, errors.size() > 0));

			result.append(messagesBuilder);
		}

		// a div containing
		// the class SetCoveringList and the nodes id

		String title = MessageKnOfficeGenerator.createNoteMSG("rule", "", 0, "", parsedCount).getMessageText();

		result.append(KnowWEUtils.maskHTML("<div title='" + title
				+ "' class=\"ReRenderSectionMarker\" id=\""
					+ sec.getID() + "-pre\" rel=\"{id:'" + sec.getID() + "'}\">"));

		// Rendering children
		StringBuilder b = new StringBuilder();
		DelegateRenderer.getInstance().render(article, sec, user, b);
		result.append(b.toString());

		// close the div
		result.append(KnowWEUtils.maskHTML("</div>"));

		result.append("}}}\n");

		// result.append("/%\n");

		// return result.toString();
	}

	protected String wrappSpan(String messages, String className) {
		if (messages == null) return null;
		if (messages.isEmpty()) return "";
		return KnowWEUtils.maskHTML("<span class='" + className + "'>")
					+ messages
					+ KnowWEUtils.maskHTML("</span>");
	}
}
