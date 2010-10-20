/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.we.solutionpanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.taghandler.AbstractDefaultStyledTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Displays a configurable pane presenting derived solutions and abstractions.
 * The following options are available:
 * <ul>
 * <li>show_master_name (default = false)
 * <li>show_established (default = true)
 * <li>show_suggested (default = false)
 * <li>show_excluded (default = false)
 * <li>show_abstractions (default = false)
 * </ul>
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 15.10.2010
 */
public class StyledSolutionsPanelHandler extends AbstractDefaultStyledTagHandler {

	public StyledSolutionsPanelHandler() {
		super("StyledSolutions", true);
	}

	@Override
	public String renderContent(KnowWEArticle article, Section<?> section, KnowWEUserContext user, Map<String, String> parameters) {
		String id = section.getID();
		StringBuffer content = new StringBuffer();

		String masterArticleName = paramValueOf(parameters, "master");
		Session session = getSessionFor(masterArticleName, article.getWeb(), user);

		if (session == null) {
			content.append("No knowledge base for: " + masterArticleName + "\n\n");
			content.append(parameters.toString() + "\n");
		}
		else {
			if (paramHasValue(parameters, "show_master_name", "true")) {
				content.append("__" + masterArticleName + "__\n\n");
			}
			content.append(renderSolutions(parameters, session));
			content.append(renderAbstractions(parameters, session));
		}

		return content.toString();
	}

	/**
	 * Renders the derived abstractions when panel opted for it.
	 */
	private StringBuffer renderAbstractions(Map<String, String> parameters, Session session) {
		StringBuffer buffer = new StringBuffer();
		if (paramHasValue(parameters, "show_abstractions", "true")) {
			List<Question> abstractions = new ArrayList<Question>();
			for (Question question : session.getBlackboard().getAnsweredQuestions()) {
				Boolean isAbstract = (Boolean) question.getInfoStore().getValue(
						BasicProperties.ABSTRACTION_QUESTION);
				if (isAbstract != null && isAbstract) {
					abstractions.add(question);
				}
			}
			Collections.sort(abstractions, new Comparator<Question>() {

				@Override
				public int compare(Question o1, Question o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (Question question : abstractions) {
				addListItem(buffer, question, session);
			}
		}
		return buffer;
	}

	/**
	 * Renders the derived solutions when panel opted for it.
	 */
	private StringBuffer renderSolutions(Map<String, String> parameters, final Session session) {
		StringBuffer content = new StringBuffer();
		List<Solution> allSolutions = new ArrayList<Solution>();

		// collect the solutions to be presented
		// --- established solutions are presented by default and have to be
		// --- opted out
		if (!paramHasValue(parameters, "show_established", "false")) {
			allSolutions.addAll(session.getBlackboard().getSolutions(State.ESTABLISHED));
		}
		if (paramHasValue(parameters, "show_suggested", "true")) {
			allSolutions.addAll(session.getBlackboard().getSolutions(State.SUGGESTED));
		}
		if (paramHasValue(parameters, "show_excluded", "true")) {
			allSolutions.addAll(session.getBlackboard().getSolutions(State.EXCLUDED));
		}

		// sort the solutions to be presented
		Collections.sort(allSolutions, new Comparator<Solution>() {

			@Override
			public int compare(Solution o1, Solution o2) {
				Rating rating1 = session.getBlackboard().getRating(o1);
				Rating rating2 = session.getBlackboard().getRating(o2);
				int comparison = rating2.compareTo(rating1);
				if (comparison == 0) {
					return o1.getName().compareTo(o2.getName());
				}
				return comparison;
			}
		});

		// format the solutions
		for (Solution solution : allSolutions) {
			addListItem(content, solution, session);
		}

		return content;
	}

	private void addListItem(StringBuffer content, Solution solution, Session session) {
		// TODO: look for internationalization and only print getName,
		// when no intlz is available
		// content.append("* ");
		if (session.getBlackboard().getRating(solution).hasState(State.ESTABLISHED)) {
			content.append(renderImage("KnowWEExtension/images/fsp_established.gif", "Established"));
		}
		else if (session.getBlackboard().getRating(solution).hasState(State.SUGGESTED)) {
			content.append(renderImage("KnowWEExtension/images/fsp_suggested.gif", "Suggested"));
		}
		else if (session.getBlackboard().getRating(solution).hasState(State.EXCLUDED)) {
			content.append(renderImage("KnowWEExtension/images/fsp_excluded.gif", "Excluded"));
		}
		content.append(solution.getName());

		content.append("\n");
	}

	private String renderImage(String filename, String altText) {
		return KnowWEUtils
				.maskHTML(" <img src='" + filename
						+ "' id='sstate-update' class='pointer'"
							+ " align='top' alt='" + altText + "'"
							+ " title='" + altText + "' "
						+ "/> ");
	}

	private void addListItem(StringBuffer buffer, Question question, Session session) {
		// TODO: look for internationalization and only print getName,
		// when no intlz is available
		// buffer.append("* ");
		buffer.append(renderImage("KnowWEExtension/images/fsp_abstraction.gif", "Abstraction"));
		buffer.append(question.getName() + " = "
				+ formatValue(session.getBlackboard().getValue(question)) + "\n");
	}

	/**
	 * Renders the string representation of the specified value. For a
	 * {@link NumValue} the float is truncated to its integer value, when
	 * possible.
	 * 
	 * @created 19.10.2010
	 * @param value the specified value
	 * @return A string representation of the specified value.
	 */
	private String formatValue(Value value) {

		if (value instanceof NumValue) {
			Double numValue = (Double) value.getValue();
			if (Math.abs(numValue - Math.round(numValue)) > 0) {
				return numValue.toString();
			}
			else {
				return "" + Math.round(numValue);
			}
		}
		else if (value instanceof Unknown || value instanceof UndefinedValue) {
			return "-";
		}
		else {
			return value.toString();
		}
	}

	private boolean paramHasValue(Map<String, String> parameters, String key, String value) {
		return paramValueOf(parameters, key).equalsIgnoreCase(value);
	}

	private String paramValueOf(Map<String, String> parameters, String key) {
		String value = parameters.get(key);
		if (value == null) {
			return "";
		}
		else {
			return value;
		}
	}

	private Session getSessionFor(String articleName, String webName, KnowWEUserContext user) {
		return D3webUtils.getSession(articleName, user, webName);
	}

}
