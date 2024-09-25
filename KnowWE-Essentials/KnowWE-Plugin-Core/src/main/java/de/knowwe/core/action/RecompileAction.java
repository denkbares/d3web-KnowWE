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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
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
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.FullParseEvent;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

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
		single, variant, all
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

		Mode mode = Mode.all;
		try {
			mode = Mode.valueOf(command);
		}
		catch (IllegalArgumentException e) {
			failUnexpected(context, "Unknown command for RecompileAction: " + command);
		}
		final String reason = "Recompile hotkey";
		switch (mode) {
			case single -> recompile(List.of(article), reason, context.getUserName());
			case variant -> recompileVariant(context, reason);
			case all -> fullRecompile(context.getArticleManager(), reason, context.getUserName());
		}
	}

	/**
	 * Recompiles just the compilers for the currently selected variant (grouped compilers)
	 *
	 * @param reason the reason for the recompile
	 */
	public static void recompileVariant(UserActionContext context, String reason) {
		List<GroupingCompiler> groupingCompilers = Compilers.getCompilers(context, context.getArticleManager(), GroupingCompiler.class);
		if (groupingCompilers.isEmpty()) {
			LOGGER.warn("No grouping compiler found for when trying to compile variant. Compiling current article instead...");
			recompile(List.of(context.getArticle()), reason, context.getUserName());
		}
		else {
			GroupingCompiler variantCompiler = groupingCompilers.iterator().next();
			List<Article> compileArticles = Stream.concat(Stream.of(variantCompiler), variantCompiler.getChildCompilers()
							.stream())
					.filter(c -> c instanceof PackageCompiler)
					.map(c -> (PackageCompiler) c)
					.map(p -> p.getCompileSection().getArticle()).toList();
			recompile(compileArticles, reason, context.getUserName());
		}
	}

	/**
	 * Recompiles the given article, optionally including all compilers compiling the given article
	 *
	 * @param article the article to recompile
	 */
	public static void recompile(@NotNull Article article, @NotNull String reason) {
		recompile(List.of(article), reason, null);
	}

	public static void fullRecompile(ArticleManager articleManager, @NotNull String reason, @Nullable String userName) {
		recompile(articleManager.getArticles(), reason, userName);
	}

	/**
	 * Recompiles the given article, optionally including all compilers compiling the given article
	 *
	 * @param articlesToRecompile the articles that should be recompiled
	 * @param reason              optional reason why the recompile was requested
	 */
	public static void recompile(@NotNull Collection<Article> articlesToRecompile, @NotNull String reason, @Nullable String userName) {
		if (userName == null) userName = "SYSTEM";
		if (articlesToRecompile.isEmpty()) return;
		ArticleManager articleManager = articlesToRecompile.iterator().next().getArticleManager();
		Objects.requireNonNull(articleManager);
		Stopwatch stopwatch = new Stopwatch();
		articleManager.open();
		try {
			if (articlesToRecompile.size() == 1) {
				LOGGER.info("Starting recompilation of article {}. Reason: {}. User: {}",
						articlesToRecompile.iterator().next().getTitle(), reason, userName);
			}
			else {
				LOGGER.info("Starting FULL recompilation of {}. Reason: {}. User: {}",
						Strings.pluralOf(articlesToRecompile.size(), "article"), reason, userName);
			}
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
		LOGGER_THREAD.submit(() -> {
			try {
				articleManager.getCompilerManager().awaitTermination();
			}
			catch (InterruptedException ignore) {
			}
			stopwatch.log(LOGGER, "Recompilation of " + Strings.pluralOf(articlesToRecompile.size(), "article") + " finished");
		});
	}

	@NotNull
	private static Stream<Article> getCompilerArticles(Article article) {
		ArticleManager articleManager = article.getArticleManager();
		if (articleManager == null) return Stream.of(article);
		List<GroupingCompiler> groupingCompilers = Compilers.getCompilers(articleManager, GroupingCompiler.class);
		Stream<Article> compileArticles = $(article).successor(DefaultMarkupType.class)
				.map(s -> Compilers.getCompilers(s, PackageCompiler.class))
				.flatMap(Collection::stream)
				.distinct()
				.flatMap(c -> getGroupedPackageCompilers(groupingCompilers, c))
				.distinct()
				.map(c -> c.getCompileSection().getArticle());
		return Stream.concat(Stream.of(article), compileArticles).distinct();
	}

	@NotNull
	private static Stream<PackageCompiler> getGroupedPackageCompilers(List<GroupingCompiler> groupingCompilers, PackageCompiler packageCompiler) {
		List<Stream<PackageCompiler>> streams = new ArrayList<>();
		streams.add(Stream.of(packageCompiler));
		if (packageCompiler instanceof GroupingCompiler) {
			streams.add(toPackageCompilers(((GroupingCompiler) packageCompiler).getChildCompilers().stream()));
		}
		for (GroupingCompiler groupingCompiler : groupingCompilers) {
			if (groupingCompiler.getChildCompilers().contains(packageCompiler)) {
				streams.add(toPackageCompilers(Stream.of(groupingCompiler)));
				streams.add(toPackageCompilers(groupingCompiler.getChildCompilers().stream()));
			}
		}
		return streams.stream().flatMap(Function.identity());
	}

	private static Stream<PackageCompiler> toPackageCompilers(Stream<de.knowwe.core.compile.Compiler> compilerStream) {
		return compilerStream.filter(pc -> pc instanceof PackageCompiler).map(pc -> (PackageCompiler) pc);
	}
}
