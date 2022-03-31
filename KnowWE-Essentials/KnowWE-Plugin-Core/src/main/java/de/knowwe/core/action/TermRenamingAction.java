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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.EventManager;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
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

	public static final String TERM_NAME = "termName";
	public static final String REPLACEMENT = "termReplacement";
	public static final String SECTION_ID = "sectionId";
	public static final String FORCE = "force";

	protected Identifier createReplacingIdentifier(Identifier oldIdentifier, String text) {
		String[] pathElements = oldIdentifier.getPathElements();
		pathElements[pathElements.length - 1] = text;
		return new Identifier(pathElements);
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String term = context.getParameter(TERM_NAME);
		String replacement = context.getParameter(REPLACEMENT);
		String force = context.getParameter(FORCE);
		String sectionId = context.getParameter(SECTION_ID);

		Identifier termIdentifier = Identifier.fromExternalForm(term);
		Identifier replacementIdentifier = createReplacingIdentifier(termIdentifier, replacement);


		if ("false".equals(force) && TermUtils.getTermIdentifiers(context).contains(replacementIdentifier)) {
			writeAlreadyExistsResponse(context, termIdentifier, replacementIdentifier);
			return;
		}

		Collection<TermCompiler> compilers = Compilers.getCompilers(Sections.get(sectionId), TermCompiler.class);
		Collection<RenamingCommand> renamingCommands = getRenamingCommands(termIdentifier, replacementIdentifier, compilers);

		checkEditRights(context, renamingCommands);

		ArticleManager mgr = context.getArticleManager();
		for (RenamingCommand command : renamingCommands) {
			EventManager.getInstance()
					.fireEvent(new TermRenamingStartEvent(mgr, context, command.termIdentifier, command.replacementIdentifier));
		}

		executeRenamingCommands(context, renamingCommands);

		Compilers.awaitTermination(mgr.getCompilerManager());

		for (RenamingCommand command : renamingCommands) {
			EventManager.getInstance()
					.fireEvent(new TermRenamingFinishEvent(mgr, context, command.termIdentifier, command.replacementIdentifier));
		}

		writeResponse(context);
	}

	@NotNull
	protected Collection<RenamingCommand> getRenamingCommands(Identifier termIdentifier, Identifier replacementIdentifier, Collection<TermCompiler> compilers) {
		Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle = getRegistrationsByArticle(compilers, termIdentifier);
		return List.of(new RenamingCommand(termIdentifier, replacementIdentifier, registrationsByArticle));
	}

	private void checkEditRights(UserActionContext context, Collection<RenamingCommand> renamingCommands) throws IOException {
		Set<Article> articlesWithoutEditRights = renamingCommands.stream()
				.flatMap(c -> getArticlesWithoutEditRights(c.registrationsByArticle, context).stream())
				.collect(Collectors.toSet());

		if (!articlesWithoutEditRights.isEmpty()) {
			String errorMessage = "You are not allowed to rename this term, because you do not have permission to " +
					"edit all articles on which this term occurs: \n" + Strings.concat(", ", articlesWithoutEditRights);
			NotificationManager.addNotification(context, new StandardNotification(errorMessage, Message.Type.ERROR));
			fail(context, 403, errorMessage);
		}
	}
}
