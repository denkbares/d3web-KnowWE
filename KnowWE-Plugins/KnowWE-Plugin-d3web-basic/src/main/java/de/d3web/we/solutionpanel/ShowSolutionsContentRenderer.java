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
import java.util.Set;
import java.util.TreeSet;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.manage.SolutionComparator;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * Displays a configurable pane presenting derived solutions and abstractions.
 * The following options are available:
 * <ul>
 * <li>@show_established: true/false
 * <li>@show_suggested: true/false
 * <li>@show_excluded: true/false
 * <li>@show_abstractions: true/false
 * <li>@only_derivations: questionnaire name
 * <li>@show_digits: 0..NUMBER of fractional digits to be shown
 * <li>@master: Name of the article with the knowledge base
 * </ul>
 *
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 15.10.2010
 */
public class ShowSolutionsContentRenderer implements Renderer {

	public ShowSolutionsContentRenderer() {
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		string.appendHtml("<span id='" + section.getID() + "'>");
		String text = section.getText();
		if (!text.isEmpty()) {
			string.append(text + "\n");
		}


		Session session = getSessionFor(section, user);
		if (session == null) {
			if (user.isRenderingPreview()) {
				string.append("%%information Solutions are not rendered in live preview. /%");
				//String msg = "Solutions are not rendered in preview mode";
				//DefaultMarkupRenderer.renderMessagesOfType(Message.Type.INFO,
				//		Messages.asList(Messages.info(msg)), string);
			} else {
				String msg = "Unable to find knowledge base. Please either add to a package" +
						" used for a knowledge base or specify a master article.";
				DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING,
						Messages.asList(Messages.warning(msg)), string);
			}
		}
		else {

			renderSolutions(section, session, string);
			renderAbstractions(section, session, string);
		}
		string.appendHtml("</span>");
	}

	private static Section<ShowSolutionsType> getShowSolutionsSection(Section<?> section) {
		return Sections.ancestor(section, ShowSolutionsType.class);
	}

	/**
	 * Renders the derived abstractions when panel opted for it.
	 */
	private void renderAbstractions(Section<?> section, Session session, RenderResult buffer) {
		// Check, if the shown abstractions are limited to a number of
		// questionnaires
		Section<ShowSolutionsType> parentSection = getShowSolutionsSection(section);
		String[] allowedParents = ShowSolutionsType.getAllowedParents(parentSection);
		String[] excludedParents = ShowSolutionsType.getExcludedParents(parentSection);

		if (ShowSolutionsType.shouldShowAbstractions(parentSection)) {
			List<Question> abstractions = new ArrayList<>();
			List<Question> questions = D3webUtils.getAnsweredQuestionsNonBlocking(session);
			if (questions == null) {
				renderPropagationError(buffer);
				return;
			}
			for (Question question : questions) {
				Boolean isAbstract = question.getInfoStore().getValue(
						BasicProperties.ABSTRACTION_QUESTION);
				if (isAbstract != null && isAbstract) {
					if (SolutionPanelUtils.isShownObject(allowedParents, excludedParents, question)) {
						abstractions.add(question);
					}
				}
			}
			Collections.sort(abstractions, new Comparator<Question>() {

				@Override
				public int compare(Question o1, Question o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			int digits = ShowSolutionsType.numberOfShownDigits(getShowSolutionsSection(section));

			for (Question question : abstractions) {
				SolutionPanelUtils.renderAbstraction(question, session, digits, buffer);
			}
		}
	}

	private void renderPropagationError(RenderResult buffer) {
		buffer.appendHtml(
				"<i style='color:grey'>values in calculation, please reload later</i>\n");
	}

	/**
	 * Renders the derived solutions when panel opted for it.
	 */
	private void renderSolutions(Section<?> section, final Session session, RenderResult content) {
		Set<Solution> allSolutions = new TreeSet<>(new SolutionComparator(session));
		Section<ShowSolutionsType> parentSection = getShowSolutionsSection(section);

		// collect the solutions to be presented
		// --- established solutions are presented by default and have to be
		// --- opted out
		if (ShowSolutionsType.shouldShowEstablished(parentSection)) {
			List<Solution> solutions =
					D3webUtils.getSolutionsNonBlocking(session, State.ESTABLISHED);
			if (solutions == null) {
				renderPropagationError(content);
				return;
			}
			allSolutions.addAll(solutions);
		}
		if (ShowSolutionsType.shouldShowSuggested(parentSection)) {
			List<Solution> solutions = D3webUtils.getSolutionsNonBlocking(session, State.SUGGESTED);
			if (solutions == null) {
				renderPropagationError(content);
				return;
			}
			allSolutions.addAll(solutions);
		}
		if (ShowSolutionsType.shouldShowExcluded(parentSection)) {
			List<Solution> solutions = D3webUtils.getSolutionsNonBlocking(session, State.EXCLUDED);
			if (solutions == null) {
				renderPropagationError(content);
				return;
			}
			allSolutions.addAll(solutions);
		}

		// filter unwanted solutions
		String[] allowedParents = ShowSolutionsType.getAllowedParents(parentSection);
		String[] excludedParents = ShowSolutionsType.getExcludedParents(parentSection);
		for (Solution solution : new ArrayList<>(allSolutions)) {
			if (!SolutionPanelUtils.isShownObject(allowedParents, excludedParents, solution)) {
				allSolutions.remove(solution);
			}
		}

		boolean endUserMode = false;
		Section<ShowSolutionsType> markup = Sections.ancestor(section,
				ShowSolutionsType.class);
		String flagString = ShowSolutionsType.getEndUserModeFlag(markup);
		if ("true".equalsIgnoreCase(flagString)) {
			endUserMode = true;
		}

		// format the solutions
		for (Solution solution : allSolutions) {
			SolutionPanelUtils.renderSolution(solution, session, endUserMode, content);
		}

	}

	private Session getSessionFor(Section<?> section, UserContext user) {
		String packageName = ShowSolutionsType.getPackageName(getShowSolutionsSection(section));
		String masterArticleName = ShowSolutionsType.getMaster(getShowSolutionsSection(section));
		String title = null;
		if (masterArticleName == null) {
			ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
			PackageManager packageManager = KnowWEUtils.getPackageManager(articleManager);
			Set<String> compilingArticles = packageManager.getCompilingArticles(packageName);

			for (String compilingArticle : compilingArticles) {
				title = compilingArticle;
				break;
			}
		}
		else {
			title = masterArticleName;
		}
		if (title == null) return null;

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
		if (kb == null) return null;

		return SessionProvider.getExistingSession(user, kb);
	}

}
