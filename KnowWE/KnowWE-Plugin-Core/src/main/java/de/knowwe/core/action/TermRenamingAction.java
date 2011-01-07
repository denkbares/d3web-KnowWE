/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.action;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Action which renames all Definitions and References of a given Term. The
 * following parameters are mandatory!
 * <ul>
 * <li>termname</li>
 * <li>termreplacement</li>
 * <li>web</li>
 * </ul>
 *
 * @author Sebastian Furth
 * @created Dec 15, 2010
 */
public class TermRenamingAction extends AbstractAction {

	public static final String TERMNAME = "termname";
	public static final String REPLACEMENT = "termreplacement";

	@Override
	public void execute(ActionContext context) throws IOException {

		String web = context.getParameter(KnowWEAttributes.WEB);
		String term = context.getParameter(TERMNAME);
		String replacement = context.getParameter(REPLACEMENT);

		TerminologyHandler th = KnowWEUtils.getTerminologyHandler(web);
		Section<? extends TermDefinition<?>> definition;
		Set<Section<? extends TermDefinition<?>>> definitions = new HashSet<Section<? extends TermDefinition<?>>>();
		Set<Section<? extends TermReference<?>>> references = new HashSet<Section<? extends TermReference<?>>>();
		Set<Section<? extends TermReference<?>>> temp = new HashSet<Section<? extends TermReference<?>>>();

		Iterator<KnowWEArticle> iter = KnowWEEnvironment.getInstance().getArticleManager(web).getArticleIterator();
		KnowWEArticle currentArticle;

		while (iter.hasNext()) {
			currentArticle = iter.next();

			// Check if there is a TermDefinition
			definition = th.getTermDefiningSection(currentArticle, term, KnowWETerm.LOCAL);
			if (definition != null) {
				definitions.add(definition);
			}

			// Check if there are References
			temp = th.getTermReferenceSections(currentArticle, term, KnowWETerm.LOCAL);
			if (temp != null && temp.size() > 0) {
				references.addAll(temp);
			}
		}

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Set<KnowWEArticle> failures = new HashSet<KnowWEArticle>();
		renameTermDefinitions(definitions, replacement, mgr, context, failures);
		renameTermReferences(references, replacement, mgr, context, failures);
		generateMessage(failures, context);
	}

	private void generateMessage(Set<KnowWEArticle> failures, ActionContext context) throws IOException {
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		Writer w = context.getWriter();
		if (failures.size() == 0) {
			w.write("<span style=\"color:green;\">");
			w.write(rb.getString("KnowWE.ObjectInfoTagHandler.renamingSuccessful"));
			w.write("</span>");
		}
		else {
			w.write("<span style=\"color:green;\">");
			w.write(rb.getString("KnowWE.ObjectInfoTagHandler.renamingFailed"));
			KnowWEArticle[] failureArticles = failures.toArray(new KnowWEArticle[failures.size()]);
			for (int i = 0; i < failureArticles.length - 1; i++) {
				w.write(failureArticles[i].getTitle());
				w.write(", ");
			}
			w.write(failureArticles[failureArticles.length - 1].getTitle());
			w.write("</span>");
		}
	}

	private void renameTermDefinitions(Set<Section<? extends TermDefinition<?>>> definitions, String replacement, KnowWEArticleManager mgr, ActionContext context, Set<KnowWEArticle> failures) {
		for (Section<? extends TermDefinition<?>> definition : definitions) {
			if (KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(
					definition.getTitle())) {
				Map<String, String> nodesMap = new HashMap<String, String>();
				nodesMap.put(definition.getID(), replacement);
				mgr.replaceKDOMNodesSaveAndBuild(context.getKnowWEParameterMap(),
						definition.getArticle().getTitle(), nodesMap);
			}
			else {
				failures.add(definition.getArticle());
			}
		}
	}

	private void renameTermReferences(Set<Section<? extends TermReference<?>>> references, String replacement, KnowWEArticleManager mgr, ActionContext context, Set<KnowWEArticle> failures) {
		Map<KnowWEArticle, List<Section<? extends TermReference<?>>>> groupedReferences = groupByArticle(references);
		for (KnowWEArticle article : groupedReferences.keySet()) {
			if (KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(
					article.getTitle())) {
				Map<String, String> nodesMap = new HashMap<String, String>();
				for (Section<? extends TermReference<?>> reference : groupedReferences.get(article)) {
					nodesMap.put(reference.getID(), replacement);
				}
				mgr.replaceKDOMNodesSaveAndBuild(context.getKnowWEParameterMap(),
							article.getTitle(), nodesMap);
			}
			else {
				failures.add(article);
			}
		}
	}

	private Map<KnowWEArticle, List<Section<? extends TermReference<?>>>> groupByArticle(Set<Section<? extends TermReference<?>>> references) {

		Map<KnowWEArticle, List<Section<? extends TermReference<?>>>> result = new HashMap<KnowWEArticle, List<Section<? extends TermReference<?>>>>();
		KnowWEArticle article;

		for (Section<? extends TermReference<?>> reference : references) {
			article = reference.getArticle();
			List<Section<? extends TermReference<?>>> existingReferences = result.get(article);
			if (existingReferences == null) {
				existingReferences = new LinkedList<Section<? extends TermReference<?>>>();
			}
			existingReferences.add(reference);
			result.put(article, existingReferences);
		}

		return result;
	}

}
