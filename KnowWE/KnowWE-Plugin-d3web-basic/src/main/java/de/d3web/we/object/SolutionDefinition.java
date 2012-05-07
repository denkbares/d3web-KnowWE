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
package de.d3web.we.object;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.renderer.ObjectInfoLinkRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

/**
 * 
 * Type for the definition of solution
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class SolutionDefinition
		extends D3webTermDefinition<Solution> {

	public SolutionDefinition() {
		this(Priority.HIGHEST);
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends SimpleTerm> section) {
		return Solution.class;
	}

	public SolutionDefinition(Priority p) {
		this.setRenderer(
				new ToolMenuDecoratingRenderer(
						new SolutionIDHighlightingRenderer()));
		// this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addSubtreeHandler(p, new CreateSolutionHandler());
	}

	/**
	 * 
	 * @author Johannes Dienst
	 * 
	 *         Highlights the Solutions in CoveringList according to state. Also
	 *         Includes the ObjectInfoLinkRenderer.
	 * 
	 */
	class SolutionIDHighlightingRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user,
				StringBuilder string) {

			Article article = KnowWEUtils.getCompilingArticles(sec).iterator().next();

			KnowledgeBase kb = D3webUtils.getKnowledgeBase(user.getWeb(), article.getTitle());
			Session session = SessionProvider.getSession(user, kb);

			String spanStart = Strings
					.maskHTML("<span style=\"background-color:");
			String spanStartEnd = Strings.maskHTML(";\">");
			String spanEnd = Strings.maskHTML("</span>");

			if (session != null) {
				@SuppressWarnings("unchecked")
				Solution solution = getTermObject(article,
						(Section<? extends D3webTerm<Solution>>) sec);
				if (solution != null) {
					Rating state = session.getBlackboard().getRating(solution);

					if (state.hasState(State.ESTABLISHED)) {
						string.append(spanStart + StyleRenderer.CONDITION_FULLFILLED + spanStartEnd);
					}
					else if (state.hasState(State.EXCLUDED)) {
						string.append(spanStart + StyleRenderer.CONDITION_FALSE + spanStartEnd);
					}
					else {
						string.append(spanStart + " rgb()" + spanStartEnd);
					}
				}
			}

			new ObjectInfoLinkRenderer(StyleRenderer.SOLUTION).render(sec, user,
					string);
			string.append(spanEnd);
		}
	}

	static class CreateSolutionHandler extends D3webSubtreeHandler<SolutionDefinition> {

		@Override
		public Collection<Message> create(Article article,
				Section<SolutionDefinition> section) {

			TermIdentifier termIdentifier = section.get().getTermIdentifier(section);
			String name = section.get().getTermName(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);
			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			terminologyHandler.registerTermDefinition(section, termObjectClass, termIdentifier);

			Collection<Message> msgs = section.get().canAbortTermObjectCreation(article, section);
			if (msgs != null) return msgs;

			KnowledgeBase kb = getKB(article);

			TerminologyObject o = kb.getManager().search(name);

			if (o != null) {
				if (!(o instanceof Solution)) {
					return Messages.asList(Messages.error("The term '"
							+ name + "' is reserved by the system."));
				}
				return Messages.asList();
			}

			new Solution(kb.getRootSolution(), name);

			return Messages.asList(Messages.objectCreatedNotice(
					termObjectClass.getSimpleName() + " '" + name + "'"));

		}

	}

}
