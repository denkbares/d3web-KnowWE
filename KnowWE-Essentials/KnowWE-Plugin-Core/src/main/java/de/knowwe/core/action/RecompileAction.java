/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.FullParseEvent;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;

import static de.knowwe.core.action.RecompileAction.Mode.*;
import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Does a full compilation of the current article or the compilers compiling this article
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.04.2020
 */
public class RecompileAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(RecompileAction.class);

	public enum Mode {
		single, variant, full, unspecified
	}

	private static final ExecutorService LOGGER_THREAD = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "Recompile-Logger-Thread"));

	@Override
	public void execute(UserActionContext context) throws IOException {

		String command = context.getParameter("command");
		Article article = context.getArticle();
		if (article == null) {
			failUnexpected(context, "No article found, unable to recompile");
			return;
		}

		Mode mode = unspecified;
		try {
			mode = Mode.valueOf(command);
		}
		catch (IllegalArgumentException e) {
			failUnexpected(context, "Unknown command for RecompileAction: " + command);
		}
		final String reason = "manual recompile";
		switch (mode) {
			case single, unspecified -> recompile(List.of(article), single, reason, context.getUserName());
			case variant -> recompileVariant(context, reason);
			case full -> recompile(context.getArticleManager().getArticles(), full, reason, context.getUserName());
		}
	}

	/**
	 * Recompiles just the compilers for the currently selected variant (grouped compilers)
	 */
	public static void recompileVariant(UserActionContext context, String reason) {
		List<GroupingCompiler> groupingCompilers = Compilers.getCompilers(context, context.getArticleManager(), GroupingCompiler.class);
		if (groupingCompilers.isEmpty()) {
			LOGGER.warn("No grouping compiler found for when trying to compile variant. Compiling current article instead...");
			recompile(List.of(context.getArticle()), single, reason, context.getUserName());
		}
		else {
			GroupingCompiler variantCompiler = groupingCompilers.iterator().next();
			recompileVariant(context, reason, variantCompiler);
		}
	}

	/**
	 * Recompiles just the compilers of the given group compiler
	 */
	public static void recompileVariant(UserActionContext context, String reason, Compiler variantCompiler) {
		Stream<Compiler> compilerStream;
		if (variantCompiler instanceof GroupingCompiler groupingCompiler) {
			compilerStream = streamCompilers(groupingCompiler);
		}
		else {
			compilerStream = Compilers.getCompilers(variantCompiler.getCompilerManager()
							.getArticleManager(), GroupingCompiler.class)
					.stream()
					.filter(g -> g.getChildCompilers().contains(variantCompiler))
					.findFirst().map(RecompileAction::streamCompilers).orElse(Stream.of(variantCompiler));
		}

		List<Article> compileArticles = compilerStream
				.filter(c -> c instanceof PackageCompiler)
				.map(c -> (PackageCompiler) c)
				.map(p -> p.getCompileSection().getArticle()).toList();
		recompile(compileArticles, variant, reason, context.getUserName());
	}

	private static @NotNull Stream<Compiler> streamCompilers(GroupingCompiler groupingCompiler) {
		return Stream.concat(Stream.of(groupingCompiler), groupingCompiler.getChildCompilers().stream());
	}

	/**
	 * Recompiles the given article, optionally including all compilers compiling the given article
	 *
	 * @param articlesToRecompile the articles that should be recompiled
	 */

	public static void recompile(@NotNull Collection<Article> articlesToRecompile, @NotNull Mode mode, @NotNull String reason, @Nullable String userName) {
		if (userName == null) userName = "SYSTEM";
		if (articlesToRecompile.isEmpty()) return;
		ArticleManager articleManager = articlesToRecompile.iterator().next().getArticleManager();
		Objects.requireNonNull(articleManager);
		Stopwatch stopwatch = new Stopwatch();
		articleManager.open();
		try {
			LOGGER.info("Starting {} recompilation ({}). Reason: {}. User: {}",
					mode.name(), Strings.pluralOf(articlesToRecompile.size(), "article"), reason, userName);
			articleManager.getCompilerManager()
					.setCompileMessage("Mode: " + mode.name() + ", reason: " + reason + ", user: " + userName);
			if (articleManager instanceof DefaultArticleManager defaultArticleManager) {
				articlesToRecompile.parallelStream()
						.forEach(article -> defaultArticleManager.queueArticle(article.getTitle(), article.getText()));
			}
			else {
				for (Article recompileArticle : articlesToRecompile) {
					articleManager.registerArticle(recompileArticle.getTitle(), recompileArticle.getText());
				}
			}
			stopwatch.log(LOGGER, "Sectionized " + Strings.pluralOf(articlesToRecompile.size(), "article") + " for recompilation");
			EventManager.getInstance().fireEvent(new FullParseEvent(articlesToRecompile, userName));

			if (articlesToRecompile.size() == 1) {
				// also update all markups
				$(articlesToRecompile.iterator().next()).successor(AttachmentUpdateMarkup.class)
						.stream()
						.forEach(markup -> {
							LOGGER.info("Checking {} for updates...", markup.get().getUrl(markup));
							markup.get().performUpdate(markup, true);
						});
			}
		}
		finally {
			articleManager.commit();
		}
	}
}
