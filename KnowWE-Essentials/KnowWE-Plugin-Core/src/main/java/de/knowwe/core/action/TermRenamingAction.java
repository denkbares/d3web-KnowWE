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
package de.knowwe.core.action;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.events.EventManager;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.TermRenamingFinishEvent;
import de.knowwe.event.TermRenamingStartEvent;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.StandardNotification;

/**
 * Action which renames a term by e.g. renaming all definitions and references.
 *
 * @author Sebastian Furth
 * @created Dec 15, 2010
 */
public class TermRenamingAction extends AbstractTermRenamingAction {

	public static final String TERMNAME = "termname";
	public static final String REPLACEMENT = "termreplacement";
	public static final String SECTIONID = "sectionid";

	private Identifier createReplacingIdentifier(Identifier oldIdentifier, String text) {
		String[] pathElements = oldIdentifier.getPathElements();
		pathElements[pathElements.length - 1] = text;
		return new Identifier(pathElements);
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getParameter(Attributes.WEB);
		String term = context.getParameter(TERMNAME);
		String replacement = context.getParameter(REPLACEMENT);
		String force = context.getParameter("force");
		String sectionId = context.getParameter(SECTIONID);

		Identifier termIdentifier = Identifier.fromExternalForm(term);
		Identifier replacementIdentifier = createReplacingIdentifier(termIdentifier, replacement);

		if (force.equals("false") && TermUtils.getTermIdentifiers(web).contains(replacementIdentifier)) {
			writeAlreadyExistsResponse(context, term, replacementIdentifier);
			return;
		}

		Collection<TerminologyManager> managers = KnowWEUtils.getTerminologyManagers(Sections.get(sectionId));
		Map<String, Set<Section<? extends RenamableTerm>>> allTerms = getAllTermSections(managers, termIdentifier);

		ArticleManager mgr = Environment.getInstance().getArticleManager(web);
		Set<String> failures = new HashSet<>();
		Set<String> success = new HashSet<>();
		Set<String> articlesWithoutEditRights = getArticlesWithoutEditRights(allTerms, context);
		if (articlesWithoutEditRights.isEmpty()) {
			EventManager.getInstance()
					.fireEvent(new TermRenamingStartEvent(mgr, context, termIdentifier, replacementIdentifier));
			renameTerms(allTerms, termIdentifier, replacementIdentifier, mgr, context, failures, success);
			Compilers.awaitTermination(mgr.getCompilerManager());
			EventManager.getInstance()
					.fireEvent(new TermRenamingFinishEvent(mgr, context, termIdentifier, replacementIdentifier));
			writeResponse(failures, success, termIdentifier, replacementIdentifier, context);
		}
		else {
			String errorMessage = "You are not allowed to rename this term, because you do not have permission to " +
					"edit all articles on which this term occurs: \n" + Strings.concat(", ", articlesWithoutEditRights);
			NotificationManager.addNotification(context, new StandardNotification(errorMessage, Message.Type.ERROR));
			context.sendError(403, errorMessage);
		}
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
			response.put("newTermIdentifier", new Identifier(
					pathElements).toExternalForm());

			// the new object name
			response.put("newObjectName", new Identifier(
					newLastPathElement).toExternalForm());

			String title = context.getTitle();
			if (title.equals("ObjectInfoPage")) {
				response.put("objectinfopage", true);
			}

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
}
