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

import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class FlowchartSectionRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {

		String sectionID = sec.getID();
		Tool[] tools = ToolUtils.getTools(article, sec, user);

		String name = FlowchartType.getFlowchartName(sec);
		String title = "Flowchart: " + name;

		String topic = sec.getArticle().getTitle();
		String web = sec.getArticle().getWeb();

		StringBuilder content = new StringBuilder();
		DefaultMarkupRenderer.renderMessages(article, sec, content);
		content.append(createPreview(article, sec, user, web, topic));

		// string.append("%%collapsebox-closed \n! ");
		// string.append(title);
		// string.append(" \n");
		// string.append(content);
		// string.append("/%\n");
		String header = DefaultMarkupRenderer.renderHeader(
				"KnowWEExtension/flowchart/icon/flowchart24.png", title);
		DefaultMarkupRenderer.renderDefaultMarkupStyled(
				header, content.toString(), sectionID, tools, string);
	}

	private String createPreview(KnowWEArticle article, Section<?> sec, KnowWEUserContext user, String web, String topic) {
		// dirty xml parsing hack for quick results
		String xml = sec.getOriginalText();
		int startPos = xml.lastIndexOf("<preview mimetype=\"text/html\">");
		int endPos = xml.lastIndexOf("</preview>");
		if (startPos >= 0 && endPos >= 0) {
			String preview = xml.substring(startPos + 43, endPos - 8).trim();
			return KnowWEUtils
					.maskHTML(
					"<div>"
							+
							"<link rel='stylesheet' type='text/css' href='cc/kbinfo/dropdownlist.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objectselect.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objecttree.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/floweditor.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/nodeeditor.css'></link>"
							// + "\r\n"
							+
							"<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>"
							// + "\r\n"
							+
							preview +
							"</div>");
		}
		else {
			StringBuilder buffy = new StringBuilder();
			DelegateRenderer.getInstance().render(article, sec, user, buffy);
			return buffy.toString();
		}
	}

}
