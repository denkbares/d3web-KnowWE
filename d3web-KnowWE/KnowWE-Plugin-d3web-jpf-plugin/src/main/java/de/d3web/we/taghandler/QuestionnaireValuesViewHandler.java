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
import java.util.List;
import java.util.Map;

import de.d3web.we.action.QuestionnaireValuesViewAction;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Displays the values of the questions in a specified questionnaire. The magic
 * is done with AJAX {@link QuestionnaireValuesViewAction}
 * 
 * @author Sebastian Furth
 * @created 06.06.2010
 */
public class QuestionnaireValuesViewHandler extends AbstractTagHandler {

	private final List<String> questionnaires = new ArrayList<String>();

	public QuestionnaireValuesViewHandler() {
		super("questionnaireValues");
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin questionnaireValues = &lt;questionnaireName&gt;" + "}]";
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.Questionnaire.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		String questionnaireName = values.get("questionnaireValues");
		if (!questionnaires.contains(questionnaireName)) questionnaires.add(questionnaireName);

		int i = questionnaires.indexOf(questionnaireName);

		return "<div class='panel'>"
				+ "<h3>"
				+ questionnaireName
				+ "</h3><div id='qcvalues-panel" + i + "'>"
				+ "<input type='hidden' class='qcname' value='"
				+ questionnaireName
				+ "'/>"
				+ "<div id='qcvalues-result" + i + "'>"
				+ "</div>"
				+ "</div></div>";
	}

}
