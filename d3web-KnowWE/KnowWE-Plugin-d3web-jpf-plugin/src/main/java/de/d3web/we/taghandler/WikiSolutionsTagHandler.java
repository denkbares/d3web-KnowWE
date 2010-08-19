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

import java.util.Collection;
import java.util.Map;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class WikiSolutionsTagHandler extends AbstractTagHandler {

	public WikiSolutionsTagHandler() {
		this("WikiSolutions");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.WikiSolutions.description");
	}

	public WikiSolutionsTagHandler(String name) {
		super(name);
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		Collection<KnowledgeService> services = DPSEnvironmentManager.getInstance().getEnvironments(
				web).getServices();

		String text = "<h1>"
				+ D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.WikiSolutions.headline")
				+ "</h1>";
		GlobalTerminology solutions = DPSEnvironmentManager.getInstance().getEnvironments(web).getTerminologyServer().getGlobalTerminology(
				TerminologyType.diagnosis);

		if (solutions == null) return "no global solutions";

		Collection<Term> allTerms = solutions.getAllTerms();
		for (Term term : allTerms) {
			text += term.getInfo(TermInfoType.TERM_NAME) + "<br>";

			// TODO refactor using alingnment knowledge
			for (KnowledgeService service : services) {
				if (service instanceof D3webKnowledgeService) {
					Solution d = KnowledgeBaseManagement.createInstance(
							((D3webKnowledgeService) service).getBase()).findSolution(
							(String) term.getInfo(TermInfoType.TERM_NAME));
					if (d != null) {
						String topicName = service.getId().substring(0,
								service.getId().indexOf(".."));
						String link = "<a href=\"Wiki.jsp?page=" + topicName + "\">"
								+ term.getInfo(TermInfoType.TERM_NAME) + "</a>";
						text += link + "<br /> \n"; // \n only to avoid
													// hmtl-code being cut by
													// JspWiki (String.length >
													// 10000)
					}
				}

			}
			text += "<br />";
		}
		return text;
	}

}
