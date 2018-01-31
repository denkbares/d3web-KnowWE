/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package de.knowwe.core.objectinfo;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Action which renames all Definitions and References of a given Term. The following parameters are mandatory! <ul>
 * <li>termname</li> <li>termreplacement</li> <li>web</li> </ul>
 *
 * @author Sebastian Furth
 * @created Dec 15, 2010
 */
public class InlineTermRenamingAction extends AbstractAction {

	public static final String TERMNAME = "termname";
	public static final String REPLACEMENT = "termreplacement";

	@SuppressWarnings("unchecked")
	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getWeb();
		String term = context.getParameter(TERMNAME);
		String replacement = context.getParameter(REPLACEMENT);
		String force = context.getParameter("force");

		if (force.equals("false")
				&& TermUtils.getTermIdentifiers(web).contains(new Identifier(replacement))) {
			JSONObject response = new JSONObject();
			try {
				response.append("alreadyexists", "true");
				boolean sameTerm = new Identifier(replacement).toExternalForm().equals(
						new Identifier(term).toExternalForm());
				response.append("same", String.valueOf(sameTerm));
				response.write(context.getWriter());
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}

		Identifier termIdentifier = Identifier.fromExternalForm(term);
		Identifier replacmentIdentifier = createReplacingIdentifier(termIdentifier, replacement);

		Map<String, Set<Section<? extends RenamableTerm>>> allTerms = new HashMap<>();

		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(web).getArticles().iterator();
		Article currentArticle;

		while (iter.hasNext()) {
			currentArticle = iter.next();
			Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(
					KnowWEUtils.getArticleManager(
							currentArticle.getWeb()));
			for (TerminologyManager terminologyManager : terminologyManagers) {

				// terminologyManager = KnowWEUtils
				// .getGlobalTerminologyManager(currentArticle.getWeb());
				// Check if there is a TermDefinition
				Collection<Section<?>> definingSections = terminologyManager
						.getTermDefiningSections(termIdentifier);
				for (Section<?> definition : definingSections) {
					if (definition.get() instanceof RenamableTerm) {
						getTermSet(definition.getTitle(), allTerms).add((Section<? extends RenamableTerm>) definition);
					}
				}

				// Check if there are References
				Collection<Section<?>> references = terminologyManager
						.getTermReferenceSections(termIdentifier);
				for (Section<?> reference : references) {
					if (reference.get() instanceof RenamableTerm) {
						getTermSet(reference.getTitle(), allTerms).add((Section<? extends RenamableTerm>) reference);
					}
				}
			}
		}

		ArticleManager mgr = Environment.getInstance().getArticleManager(web);
		Set<String> failures = new HashSet<>();
		Set<String> success = new HashSet<>();
		renameTerms(allTerms, termIdentifier, replacmentIdentifier, mgr, context, failures, success);
		writeResponse(failures, success, termIdentifier, replacmentIdentifier, context);
	}

	private Set<Section<? extends RenamableTerm>> getTermSet(String title, Map<String, Set<Section<? extends RenamableTerm>>> allTerms) {
		Set<Section<? extends RenamableTerm>> terms = allTerms.get(title);
		if (terms == null) {
			terms = new HashSet<>();
			allTerms.put(title, terms);
		}
		return terms;
	}

	private void writeResponse(Set<String> failures, Set<String> success,
							   Identifier termIdentifier, Identifier replacement,
							   UserActionContext context) throws IOException {

		JSONObject response = new JSONObject();
		try {
			// the new external form of the TermIdentifier
			String[] pathElements = termIdentifier.getPathElements();
			String newLastPathElement = replacement.getLastPathElement();
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

	private Identifier createReplacingIdentifier(Identifier oldIdentifier, String text) {
		String[] elements = oldIdentifier.getPathElements();
		elements[elements.length - 1] = text;
		return new Identifier(elements);
	}

}
