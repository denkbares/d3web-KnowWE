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

import org.json.JSONObject;

import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 04.09.2018
 */
public abstract class AbstractTermRenamingAction extends AbstractAction {

	public static final String ALREADY_EXISTS = "alreadyExists";
	public static final String SAME = "same";
	public static final String NO_FORCE = "noForce";

	protected void executeRenamingCommands(UserActionContext context, Collection<RenamingCommand> renamingCommands) throws IOException {

		Map<Article, Map<String, String>> nodesMapByArticle = new HashMap<>();
		for (RenamingCommand renamingCommand : renamingCommands) {
			appendReplacements(renamingCommand, nodesMapByArticle);
		}

		performRenaming(nodesMapByArticle, context);
	}

	protected void performRenaming(Map<Article, Map<String, String>> nodesMapByArticle, UserActionContext context) throws IOException {
		ArticleManager mgr = context.getArticleManager();
		mgr.open();
		try {
			for (Article article : nodesMapByArticle.keySet()) {
				if (userCanEditArticle(context, article)) {
					Sections.replace(context, nodesMapByArticle.get(article)).sendErrors(context);
				}
			}
		}
		finally {
			mgr.commit();
		}
	}

	private boolean userCanEditArticle(UserActionContext context, Article article) {
		return Environment.getInstance()
				.getWikiConnector()
				.userCanEditArticle(article.getTitle(), context.getRequest());
	}

	protected void appendReplacements(RenamingCommand renamingCommand, Map<Article, Map<String, String>> nodesMapByArticle) {
		Map<Article, Set<Section<? extends RenamableTerm>>> registrations = renamingCommand.registrationsByArticle;
		for (Article article : registrations.keySet()) {
			Map<String, String> nodesMap = nodesMapByArticle.computeIfAbsent(article, k -> new HashMap<>());
			for (Section<? extends RenamableTerm> termSection : registrations.get(article)) {
				if (!termSection.get().allowRename(termSection)) continue;
				String sectionTextAfterRename = termSection.get()
						.getSectionTextAfterRename(termSection, renamingCommand.termIdentifier, renamingCommand.replacementIdentifier);
				if (!sectionTextAfterRename.equals(termSection.getText())) {
					nodesMap.put(termSection.getID(), sectionTextAfterRename);
				}
			}
		}
	}

	protected Map<Article, Set<Section<? extends RenamableTerm>>> getRegistrationsByArticle(Collection<TermCompiler> compilers, Identifier termIdentifier) {
		Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle = new HashMap<>();
		Consumer<Section<?>> addIfRenamable = (section) -> {
			if (section.get() instanceof RenamableTerm) {
				registrationsByArticle.computeIfAbsent(section.getArticle(), k -> new HashSet<>())
						.add(Sections.cast(section, RenamableTerm.class));
			}
		};

		for (TermCompiler compiler : compilers) {
			TerminologyManager manager = compiler.getTerminologyManager();
			manager.getTermDefiningSections(termIdentifier).forEach(addIfRenamable);
			manager.getTermReferenceSections(termIdentifier).forEach(addIfRenamable);
		}
		return registrationsByArticle;
	}

	protected Set<Article> getArticlesWithoutEditRights(Map<Article, Set<Section<? extends RenamableTerm>>> termsByArticle, UserActionContext context) {
		Set<Article> noEditRightsOnThisArticles = new HashSet<>();
		for (Map.Entry<Article, Set<Section<? extends RenamableTerm>>> sectionsMap : termsByArticle.entrySet()) {
			Set<Section<? extends RenamableTerm>> sectionsSet = sectionsMap.getValue();
			for (Section<? extends RenamableTerm> section : sectionsSet) {
				if (!KnowWEUtils.canWrite(section, context)) {
					noEditRightsOnThisArticles.add(section.getArticle());
				}
			}
		}
		return noEditRightsOnThisArticles;
	}

	protected void writeResponse(UserActionContext context) throws IOException {
		context.setContentType(Action.JSON);
		JSONObject response = new JSONObject();
		response.put(ALREADY_EXISTS, false);
		response.write(context.getWriter());
	}

	protected void writeAlreadyExistsResponse(UserActionContext context, Identifier termIdentifier, Identifier replacementIdentifier) throws IOException {
		context.setContentType(Action.JSON);
		JSONObject response = new JSONObject();
		response.put(ALREADY_EXISTS, true);
		response.put(SAME, termIdentifier.equals(replacementIdentifier));
		response.write(context.getWriter());
	}

	protected void writeAlreadyExistsNoForceResponse(UserActionContext context, Identifier termIdentifier, Identifier replacementIdentifier) throws IOException {
		context.setContentType(Action.JSON);
		JSONObject response = new JSONObject();
		response.put(ALREADY_EXISTS, true);
		response.put(SAME, termIdentifier.equals(replacementIdentifier));
		response.put(NO_FORCE, true);
		response.write(context.getWriter());
	}

	public static class RenamingCommand {
		public final Identifier termIdentifier;
		public final Identifier replacementIdentifier;
		public final Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle;

		public RenamingCommand(Identifier termIdentifier, Identifier replacementIdentifier, Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle) {
			this.termIdentifier = termIdentifier;
			this.replacementIdentifier = replacementIdentifier;
			this.registrationsByArticle = registrationsByArticle;
		}
	}
}
