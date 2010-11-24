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

package de.d3web.we.flow;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class FlowchartSectionRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {


		String topic = sec.getArticle().getTitle();
		String web = sec.getArticle().getWeb();

		String content = createPreview(article, sec, user, web, topic, string); // wrappContent(SpecialDelegateRenderer.getInstance().render(sec,
		string.append(content);
	}

	private String createPreview(KnowWEArticle article, Section sec, KnowWEUserContext user, String web, String topic, StringBuilder builder) {
		// dirty xml parsing hack for quick results
		String xml = sec.getOriginalText();
		int startPos = xml.lastIndexOf("<preview mimetype=\"text/html\">");
		int endPos = xml.lastIndexOf("</preview>");
		if (startPos >= 0 && endPos >= 0) {
			String preview = xml.substring(startPos + 43, endPos - 8);
			return KnowWEUtils
					.maskHTML(
					"<div>"
							+
										// sec.getID() contains a '/' which is
										// not allowed. FF
										// ignores it, IE doesnt open a new
										// window
										// "onclick='window.open(\""+createEditURL(sec.getId(),
										// topic)+"\", \""+sec.getId()+"\")'>"
										// +"\r\n" +
										"<link rel='stylesheet' type='text/css' href='cc/kbinfo/dropdownlist.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objectselect.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objecttree.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/floweditor.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/nodeeditor.css'></link>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>"
							+ // overrides the move cursor on nodes
							"<style type='text/css'>div, span, a { cursor: default !important; }</style>"
							+
							preview +
							"</div>");
		}
		else { // TODO remove
			StringBuilder buffy = new StringBuilder();
			buffy.append("\n{{{");
			buffy.append(KnowWEUtils.maskHTML(
			"<div style='cursor: pointer;' " +
					"onclick='window.open(\""
					+ createEditURL(sec.getID(), topic)
					+ "\", \""
					+ sec.getID().replaceAll("[^\\w]", "_")
					+ "\")'>"));
			DelegateRenderer.getInstance().render(article, sec, user, buffy);
			buffy.append(KnowWEUtils.maskHTML(" </div>"));
			buffy.append("}}}\n");
			return buffy.toString();
		}
	}


	private String createEditURL(String flowchartNodeID, String topic) {
		return "FlowEditor.jsp?kdomID=" + flowchartNodeID + "&" + KnowWEAttributes.TOPIC + "="
				+ topic;
	}

	protected String wrappContent(String string) {
		return "\n{{{" + string + "}}}\n";
	}

}
