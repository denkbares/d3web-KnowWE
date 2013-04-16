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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.strings.Strings;
import de.d3web.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
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
		String force = context.getParameter("force");

		if (force.equals("false")
				&& getTerms(web).contains(new Identifier(replacement))) {
			JSONObject response = new JSONObject();
			try {
				response.append("alreadyexists", "true");
				if (new Identifier(replacement).equals(new Identifier(
						term))) {
					response.append("same", "true");
				}
				else {
					response.append("same", "false");
				}
				response.write(context.getWriter());
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		replacement = makeExternalFormIfNeeded(replacement);

		Identifier termIdentifier = Identifier.fromExternalForm(term);

		HashMap<String, Set<Section<? extends Term>>> allTerms = new HashMap<String, Set<Section<? extends Term>>>();

		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(web).getArticleIterator();
		Article currentArticle;

		TerminologyManager terminologyManager;
		while (iter.hasNext()) {
			currentArticle = iter.next();
			terminologyManager = KnowWEUtils
					.getTerminologyManager(currentArticle);
			// Check if there is a TermDefinition
			Collection<Section<?>> definingSections = terminologyManager
					.getTermDefiningSections(termIdentifier);
			for (Section<?> definition : definingSections) {
				if (definition.get() instanceof Term) {
					getTermSet(definition.getTitle(), allTerms).add(
							(Section<? extends Term>) definition);
				}
			}

			// Check if there are References
			Collection<Section<?>> references = terminologyManager
					.getTermReferenceSections(termIdentifier);
			for (Section<?> reference : references) {
				if (reference.get() instanceof Term) {
					getTermSet(reference.getTitle(), allTerms).add(
							(Section<? extends Term>) reference);
				}
			}
		}

		ArticleManager mgr = Environment.getInstance().getArticleManager(web);
		Set<String> failures = new HashSet<String>();
		Set<String> success = new HashSet<String>();
		renameTerms(allTerms, replacement, mgr, context, failures, success);
		writeResponse(failures, success, termIdentifier, replacement, context);
	}

	private Set<Section<? extends Term>> getTermSet(String title,
			Map<String, Set<Section<? extends Term>>> allTerms) {
		Set<Section<? extends Term>> terms = allTerms.get(title);
		if (terms == null) {
			terms = new HashSet<Section<? extends Term>>();
			allTerms.put(title, terms);
		}
		return terms;
	}

	private void writeResponse(Set<String> failures, Set<String> success,
			Identifier termIdentifier, String replacement,
			UserActionContext context) throws IOException {

		JSONObject response = new JSONObject();
		try {
			// the new external form of the TermIdentifier
			String[] pathElements = termIdentifier.getPathElements();
			String newLastPathElement = Identifier.fromExternalForm(
					replacement).getLastPathElement();
			pathElements[pathElements.length - 1] = newLastPathElement;
			response.append("newTermIdentifier", new Identifier(
					pathElements).toExternalForm());

			// the new object name
			response.append("newObjectName", new Identifier(
					newLastPathElement).toExternalForm());

			// renamed Articles
			StringBuilder renamedArticles = new StringBuilder();
			// successes
			for (String article : success) {
				renamedArticles.append("##");
				renamedArticles.append(article);
			}
			renamedArticles.append("###");
			// failures
			for (String article : failures) {
				renamedArticles.append("##");
				renamedArticles.append(article);
			}
			response.append("alreadyexists", false);
			response.accumulate("renamedArticles", renamedArticles);

			response.write(context.getWriter());
		}
		catch (JSONException e) {
			throw new IOException(e.getMessage());
		}
	}

	private void renameTerms(
			HashMap<String, Set<Section<? extends Term>>> allTerms,
			String replacement, ArticleManager mgr, UserActionContext context,
			Set<String> failures, Set<String> success) throws IOException {

		for (String title : allTerms.keySet()) {
			if (Environment.getInstance().getWikiConnector()
					.userCanEditArticle(title, context.getRequest())) {
				Map<String, String> nodesMap = new HashMap<String, String>();
				for (Section<?> termSection : allTerms.get(title)) {
					nodesMap.put(termSection.getID(), replacement);
				}
				Sections.replaceSections(context, nodesMap);
				success.add(title);
			}
			else {
				failures.add(title);
			}
		}
	}

	private String makeExternalFormIfNeeded(String text) {
		boolean quoted = Strings.isQuoted(text);
		boolean needsQuotes = Identifier.needsQuotes(text)
				|| text.replaceAll("\\s", "").length() < text.length();

		if (needsQuotes && !quoted) text = Strings.quote(text);

		return text;
	}

	public Set<Identifier> getTerms(String web) {
		// gathering all terms
		Set<Identifier> allTerms = new HashSet<Identifier>();
		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(web).getArticleIterator();
		Article currentArticle;

		TerminologyManager terminologyManager;
		while (iter.hasNext()) {
			currentArticle = iter.next();
			terminologyManager = KnowWEUtils
					.getTerminologyManager(currentArticle);
			Collection<Identifier> allDefinedTerms = terminologyManager
					.getAllDefinedTerms();
			allTerms.addAll(allDefinedTerms);

		}
		return allTerms;
	}
}
