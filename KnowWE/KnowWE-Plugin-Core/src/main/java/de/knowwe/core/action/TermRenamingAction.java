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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

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

	@SuppressWarnings("unchecked")
	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getParameter(Attributes.WEB);
		String term = context.getParameter(TERMNAME);
		String replacement = context.getParameter(REPLACEMENT);

		HashMap<String, Set<Section<? extends SimpleTerm>>> allTerms =
				new HashMap<String, Set<Section<? extends SimpleTerm>>>();

		Iterator<Article> iter = Environment.getInstance().getArticleManager(web).getArticleIterator();
		Article currentArticle;

		TerminologyManager th;
		while (iter.hasNext()) {
			currentArticle = iter.next();
			th = KnowWEUtils.getTerminologyManager(currentArticle);
			// Check if there is a TermDefinition
			Collection<Section<?>> definingSections = th.getTermDefiningSections(term);
			for (Section<?> definition : definingSections) {
				if (definition.get() instanceof SimpleTerm) {
					getTermSet(definition.getTitle(), allTerms).add(
							(Section<? extends SimpleTerm>) definition);
				}
			}

			// Check if there are References
			Collection<Section<?>> references = th.getTermReferenceSections(term);
			for (Section<?> reference : references) {
				if (reference.get() instanceof SimpleTerm) {
					getTermSet(reference.getTitle(), allTerms).add(
							(Section<? extends SimpleTerm>) reference);
				}
			}
		}

		ArticleManager mgr = Environment.getInstance().getArticleManager(web);
		Set<String> failures = new HashSet<String>();
		Set<String> success = new HashSet<String>();
		renameTerms(allTerms, replacement, mgr, context, failures, success);
		generateMessage(failures, success, context);
	}

	private Set<Section<? extends SimpleTerm>> getTermSet(String title, Map<String, Set<Section<? extends SimpleTerm>>> allTerms) {
		Set<Section<? extends SimpleTerm>> terms = allTerms.get(title);
		if (terms == null) {
			terms = new HashSet<Section<? extends SimpleTerm>>();
			allTerms.put(title, terms);
		}
		return terms;
	}

	private void generateMessage(Set<String> failures, Set<String> success, UserActionContext context) throws IOException {
		Writer w = context.getWriter();
		// successes
		for (String article : success) {
			w.write("##");
			w.write(article);
		}
		w.write("###");
		// failures
		for (String article : failures) {
			w.write("##");
			w.write(article);
		}
	}

	private void renameTerms(HashMap<String, Set<Section<? extends SimpleTerm>>> allTerms,
			String replacement,
			ArticleManager mgr,
			UserActionContext context,
			Set<String> failures,
			Set<String> success) throws IOException {

		for (String title : allTerms.keySet()) {
			if (Environment.getInstance().getWikiConnector().userCanEditArticle(
					title, context.getRequest())) {
				Map<String, String> nodesMap = new HashMap<String, String>();
				for (Section<?> term : allTerms.get(title)) {
					nodesMap.put(term.getID(), replacement);
				}
				Sections.replaceSections(context,
						nodesMap);
				success.add(title);
			}
			else {
				failures.add(title);
			}
		}
	}

}
