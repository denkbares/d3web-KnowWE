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
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CosNaming._BindingIteratorImplBase;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Created by stefan on 20.02.14.
 */
public class GetInfosForInlineTermRenamingAction extends AbstractAction {
	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionid = context.getParameter("sectionId");
		Section<?> section = Sections.getSection(sectionid);
		Identifier termIdentifier = ((Term) section.get()).getTermIdentifier((Section<? extends Term>) section);

		Set<String> allTermOccurences = getAllTermOccurencesOnThisArticle(section, termIdentifier, context.getArticle());


		JSONObject json = new JSONObject();
		try {
			json.put("sectionIds", allTermOccurences);
			json.put("termIdentifier", termIdentifier.toExternalForm());
			json.put("lastPathElement", termIdentifier.getLastPathElement());
			json.put("sectionText", section.getText());
			json.write(context.getWriter());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private Set<String> getAllTermOccurencesOnThisArticle(Section<?> section, Identifier termIdentifier, Article article) {
		Set<Section<?>> sections = new HashSet<Section<?>>();
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(section.getArticleManager());
		for (TerminologyManager terminologyManager : terminologyManagers) {
			sections.addAll(terminologyManager.getTermDefiningSections(termIdentifier));
			sections.addAll(terminologyManager.getTermReferenceSections(termIdentifier));
		}
		Set<String> ids = new HashSet<String>();
		for (Section<?> occurenceSection : sections) {
			if (occurenceSection.getArticle().equals(article) && !occurenceSection.getID().equals(section.getID())) {
				ids.add(occurenceSection.getID());
			}
		}
		return ids;
	}
}
