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

package de.d3web.we.flow;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.TextLine;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class FlowchartSectionRenderer extends KnowWEDomRenderer{
	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		List<Section> lines = new ArrayList<Section>(); 
		sec.findSuccessorsOfType(TextLine.class, lines);
		FlowchartType section = (FlowchartType)sec.getObjectType();
		
		String title = "Flowchart '" + section.getFlowchartName(sec) + "'";
		
		String topic = sec.getArticle().getTitle();
		String web = sec.getArticle().getWeb();
		
		String editLink = generateQuickEditLink(topic,sec.getId(), web, user.getUsername());
		String content = createPreview(sec, user, web, topic, string); //wrappContent(SpecialDelegateRenderer.getInstance().render(sec, user, web, topic));
		
//		string.append("%%collapsebox-closed \n! " + title + editLink + " \n" + content + "/%\n");
		string.append("%%collapsebox-closed \n! ");
		string.append(title);
		string.append(editLink);
		string.append(" \n");
		string.append(content);
		string.append("/%\n");
	}
	
	private String createPreview(Section sec, KnowWEUserContext user, String web, String topic, StringBuilder builder) {
		// dirty xml parsing hack for quick results
		String xml = sec.getOriginalText();
		int startPos = xml.lastIndexOf("<preview mimetype=\"text/html\">");
		int endPos = xml.lastIndexOf("</preview>");
		if (startPos >= 0 && endPos >= 0) {
			return KnowWEEnvironment
			.maskHTML(
				"<div style='zoom: 50%; cursor: pointer;' onclick='window.open(\""+createEditURL(sec.getId(), topic)+"\", \""+sec.getId()+"\").focus();'>" +
				"<link rel='stylesheet' type='text/css' href='cc/kbinfo/dropdownlist.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objectselect.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objecttree.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/floweditor.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/nodeeditor.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>" +
				"<style type='text/css'>div, span, a { cursor: pointer !important; }</style>" + 
				xml.substring(startPos+43, endPos-8) + 
				"</div>");
		}
		else {
			StringBuilder buffy = new StringBuilder();
			buffy.append("\n{{{");
			DelegateRenderer.getInstance().render(sec, user, buffy);
			buffy.append("}}}\n");
			return buffy.toString();
		}
	}
	
	private String generateQuickEditLink(String topic, String id, String web2, String user) {
		String icon = " <img src=KnowWEExtension/images/pencil.png title='Start Flowchart Editor' onclick='setQuickEditFlag(&quot;"+id+"&quot;,&quot;"+topic+"&quot;);window.open(&quot;"+createEditURL(id, topic)+"&quot;, &quot;"+id+"&quot;).focus();'></img>";

		return KnowWEEnvironment
		.maskHTML("<a>"+icon+"</a>");
		
	}
	
	private String createEditURL(String flowchartNodeID, String topic) {
		return "FlowEditor.jsp?kdomID="+flowchartNodeID+"&"+KnowWEAttributes.TOPIC+"="+topic;
	}


	protected String wrappContent(String string) {
		return "\n{{{"+string+"}}}\n";
	}


}
