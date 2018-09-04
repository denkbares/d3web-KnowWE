/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.action;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 04.09.2018
 */
public abstract class AbstractTermRenamingAction extends AbstractAction {
	protected void renameTerms(
			Map<String, Set<Section<? extends RenamableTerm>>> allTerms, Identifier term,
			Identifier replacement, ArticleManager mgr, UserActionContext context,
			Set<String> failures, Set<String> success) throws IOException {
		mgr.open();
		try {
			for (String title : allTerms.keySet()) {
				if (Environment.getInstance().getWikiConnector().userCanEditArticle(title, context.getRequest())) {
					Map<String, String> nodesMap = new HashMap<>();
					for (Section<? extends RenamableTerm> termSection : allTerms.get(title)) {
						nodesMap.put(termSection.getID(),
								termSection.get().getSectionTextAfterRename(termSection, term, replacement));
					}
					Sections.replace(context, nodesMap).sendErrors(context);
					success.add(title);
				}
				else {
					failures.add(title);
				}
			}
		}
		finally {
			mgr.commit();
		}
	}

	@NotNull
	protected Map<String, Set<Section<? extends RenamableTerm>>> getAllTermSections(Collection<TerminologyManager> managers, Identifier termIdentifier) {
		Map<String, Set<Section<? extends RenamableTerm>>> allTerms = new HashMap<>();
		Consumer<Section<?>> addIfRenamable = (sec) -> {
			if (sec.get() instanceof RenamableTerm) {
				allTerms.computeIfAbsent(sec.getTitle(), k -> new HashSet<>())
						.add(Sections.cast(sec, RenamableTerm.class));
			}
		};

		for (TerminologyManager terminologyManager : managers) {
			// Check if there is a TermDefinition
			terminologyManager.getTermDefiningSections(termIdentifier).forEach(addIfRenamable);
			// Check if there are References
			terminologyManager.getTermReferenceSections(termIdentifier).forEach(addIfRenamable);
		}
		return allTerms;
	}

	protected Set<String> getArticlesWithoutEditRights(Map<String, Set<Section<? extends RenamableTerm>>> allTerms, UserActionContext context) {
		Set<String> noEditRightsOnThisArticles = new HashSet<>();
		for (Map.Entry<String, Set<Section<? extends RenamableTerm>>> sectionsMap : allTerms.entrySet()) {
			Set<Section<? extends RenamableTerm>> sectionsSet = sectionsMap.getValue();
			for (Section<? extends RenamableTerm> section : sectionsSet) {
				if (!KnowWEUtils.canWrite(section, context)) {
					noEditRightsOnThisArticles.add(section.getTitle());
				}
			}
		}
		return noEditRightsOnThisArticles;
	}

	protected void writeAlreadyExistsResponse(UserActionContext context, String term, Identifier identifier) throws IOException {
		JSONObject response = new JSONObject();
		response.append("alreadyexists", "true");
		boolean sameTerm = identifier.toExternalForm().equals(
				new Identifier(term).toExternalForm());
		response.append("same", String.valueOf(sameTerm));
		response.write(context.getWriter());
	}
}
