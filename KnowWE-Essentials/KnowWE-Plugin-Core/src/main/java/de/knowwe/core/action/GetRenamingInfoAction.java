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

import com.denkbares.strings.Identifier;
import de.knowwe.core.Attributes;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author stefan
 * @created 20.02.2014
 */
public class GetRenamingInfoAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(sectionId);
		Identifier termIdentifier = ((Term) section.get()).getTermIdentifier(Sections.cast(section, Term.class));

		Set<String> allTermOccurrences = getAllTermOccurencesOnThisArticle(section, termIdentifier, context.getArticle());

		JSONObject json = new JSONObject();
		try {
			json.put("sectionIds", allTermOccurrences);
			json.put("termIdentifier", termIdentifier.toExternalForm());
			json.put("lastPathElement", termIdentifier.getLastPathElement());
			json.put("sectionText", section.getText());
			json.write(context.getWriter());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

	}

	protected Section<?> getSection(String identifier) {
		return Sections.get(identifier);
	}

	private Set<String> getAllTermOccurencesOnThisArticle(Section<?> section, Identifier termIdentifier, Article article) {
		Set<Section<?>> sections = new HashSet<>();
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(section.getArticleManager());
		for (TerminologyManager terminologyManager : terminologyManagers) {
			sections.addAll(terminologyManager.getTermDefiningSections(termIdentifier));
			sections.addAll(terminologyManager.getTermReferenceSections(termIdentifier));
		}
		Set<String> ids = new HashSet<>();
		for (Section<?> occurenceSection : sections) {
			if (occurenceSection.getArticle().equals(article) && !occurenceSection.getID().equals(section.getID())) {
				ids.add(occurenceSection.getID());
			}
		}
		return ids;
	}
}
