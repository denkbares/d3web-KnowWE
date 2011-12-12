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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEAttributes;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.KnowWETerm.Scope;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.objects.TermReference;
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

	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getParameter(KnowWEAttributes.WEB);
		String term = context.getParameter(TERMNAME);
		String replacement = context.getParameter(REPLACEMENT);

		TerminologyHandler th = KnowWEUtils.getTerminologyHandler(web);
		HashMap<String, Set<Section<?>>> allTerms =
				new HashMap<String, Set<Section<?>>>();

		Iterator<KnowWEArticle> iter = KnowWEEnvironment.getInstance().getArticleManager(web).getArticleIterator();
		KnowWEArticle currentArticle;

		while (iter.hasNext()) {
			currentArticle = iter.next();

			// Check if there is a TermDefinition
			Section<? extends TermDefinition<?>> definition = th.getTermDefiningSection(
					currentArticle, term, Scope.LOCAL);
			if (definition != null) {
				getTermSet(definition.getTitle(), allTerms).add(definition);
			}

			// Check if there are References
			Set<Section<? extends TermReference<?>>> references = th.getTermReferenceSections(
					currentArticle, term, Scope.LOCAL);
			if (references != null && references.size() > 0) {
				for (Section<?> reference : references) {
					getTermSet(reference.getTitle(), allTerms).add(reference);
				}
			}
		}

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Set<String> failures = new HashSet<String>();
		Set<String> success = new HashSet<String>();
		renameTerms(allTerms, replacement, mgr, context, failures, success);
		generateMessage(failures, success, context);
	}

	private Set<Section<?>> getTermSet(String title, Map<String, Set<Section<?>>> allTerms) {
		Set<Section<?>> terms = allTerms.get(title);
		if (terms == null) {
			terms = new HashSet<Section<?>>();
			allTerms.put(title, terms);
		}
		return terms;
	}

	private void generateMessage(Set<String> failures, Set<String> success, UserActionContext context) throws IOException {
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		Writer w = context.getWriter();
		if (failures.size() == 0) {
			w.write("<p style=\"color:green;\">");
			w.write(rb.getString("KnowWE.ObjectInfoTagHandler.renamingSuccessful"));
			w.write("</p>");
			w.write("<ul>");
			for (String article : success) {
				w.write("<li>");
				w.write(article);
				w.write("</li>");
			}
			w.write("</ul>");
		}
		else {
			w.write("<p style=\"color:red;\">");
			w.write(rb.getString("KnowWE.ObjectInfoTagHandler.renamingFailed"));
			w.write("</p>");
			w.write("<ul>");
			for (String article : failures) {
				w.write("<li>");
				w.write(article);
				w.write("</li>");
			}
			w.write("</ul>");
		}
	}

	private void renameTerms(HashMap<String, Set<Section<?>>> allTerms,
			String replacement,
			KnowWEArticleManager mgr,
			UserActionContext context,
			Set<String> failures,
			Set<String> success) throws IOException {

		for (String title : allTerms.keySet()) {
			if (KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(
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
