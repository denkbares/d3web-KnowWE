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

package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.WikiEnvironmentManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;

public class ShowAllKBsHandler extends AbstractHTMLTagHandler {

	public ShowAllKBsHandler() {
		super("showAllKBs");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.showAllKBs.description");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		List<KnowledgeBase> ks = new ArrayList<KnowledgeBase>(
				WikiEnvironmentManager.getInstance().getEnvironments(web).getServices());
		Collections.sort(ks, new KDComparator());
		StringBuffer html = new StringBuffer();
		int cnt = ks.size();
		int allSCcnt = 0;
		int allRuleCnt = 0;

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);

		String[] tblHeader = {
				rb.getString("KnowWE.KnowledgeSummerize.th.name"),
				rb.getString("KnowWE.KnowledgeSummerize.th.xcl"),
				rb.getString("KnowWE.KnowledgeSummerize.th.rule"),
				rb.getString("KnowWE.KnowledgeSummerize.th.questions"),
				rb.getString("KnowWE.KnowledgeSummerize.th.start") };
		html.append("<br><table style=\"border-style: solid; border-width:1px;\"><thead><tr>");
		for (String string : tblHeader) {
			html.append("<td><strong>" + string + "<strong></td> \n"); // \n
																		// only
																		// to
																		// avoid
																		// hmtl-code
																		// being
																		// cut
																		// by
																		// JspWiki
																		// (String.length
																		// >
																		// 10000)
		}
		html.append("</tr></thead><tbody>");

		boolean even = false;
		for (Iterator<KnowledgeBase> iterator = ks.iterator(); iterator.hasNext();) {
			even = !even;
			KnowledgeBase kb = iterator.next();
			String id = kb.getId();
			String[] parts = id.split("\\.\\.");
			if (parts.length == 1) {
				parts = new String[] {
							parts[0], "" };
			}

			Collection<KnowledgeSlice> all = kb.getAllKnowledgeSlices();
			int xclCount = 0;
			HashSet<Rule> rules = new HashSet<Rule>();
			for (Iterator<KnowledgeSlice> iter = all.iterator(); iter.hasNext();) {
				KnowledgeSlice element = iter.next();
				if (element instanceof de.d3web.xcl.XCLModel) {
					Map<XCLRelationType, Collection<XCLRelation>> map = ((XCLModel) element).getTypedRelations();
					for (java.util.Map.Entry<XCLRelationType, Collection<XCLRelation>> entry : map.entrySet()) {
						xclCount += entry.getValue().size();
					}

				}
				if (element instanceof RuleSet) {
					RuleSet rs = (RuleSet) element;
					rules.addAll(rs.getRules());
				}

			}
			allSCcnt += xclCount;
			allRuleCnt += rules.size();
			int qCount = kb.getQuestions().size();

			if (even) {
				html.append("<tr class=\"even\">");
			}
			else {
				html.append("<tr>");
			}

			Object[] tblContent = {
						"<a href=\"Wiki.jsp?page=" + parts[0] + "#" + parts[1] + "\">"
								+ id.substring(0, id.indexOf("..")) + "</a>",
						xclCount,
						rules.size(),
						qCount,
						KnowWEEnvironment.unmaskHTML(DialogLinkTagHandler.generateDialogLink(
								user.getUserName(), user.getHttpRequest(), topic, id)) };
			for (Object object : tblContent) {
				html.append("<td>" + object + "</td>");
			}
			html.append("</tr>");
			html.append(" \n"); // \n only to avoid hmtl-code being cut by
								// JspWiki (String.length > 10000)
		}
		html.append("</tbody></table>");

		// add summary
		html.insert(0, "<div class=\"ok\"><a href=\"#\" id='sumAll' class='clear-element'>"
				+ rb.getString("KnowWE.buttons.close") + "</a><br />"
				+ "<a name=\"summarizer\"></a>"
				+ rb.getString("KnowWE.KnowledgeSummerize.count.kb") + cnt + "<br />"
				+ rb.getString("KnowWE.KnowledgeSummerize.count.xcl") + allSCcnt + "<br />"
				+ rb.getString("KnowWE.KnowledgeSummerize.count.rules") + allRuleCnt + "<br />");
		html.append("</div>");
		return html.toString();
	}

	class KDComparator implements Comparator<KnowledgeBase> {

		@Override
		public int compare(KnowledgeBase arg0, KnowledgeBase arg1) {
			return arg0.getId().compareTo(arg1.getId());

		}
	}

}
