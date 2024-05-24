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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
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

	@Override
	public void execute(UserActionContext context) throws IOException {

		String command = context.getParameter("command");
		String title = context.getParameter("title");
		Article article = context.getArticleManager().getArticle(title);
		if (article == null) {
			failUnexpected(context, "No article found for title '" + title + ", unable to recompile");
			return;
		}
		boolean all = "recompileAll".equals(command);
		boolean equals = "recompile".equals(command);
		if (!all && !equals) {
			failUnexpected(context, "Unknown command for RecompileAction: " + command);
		}

		recompile(article, all, "Manually triggered by user " + context.getUserName());
	}

	/**
	 * Recompiles the given article, optionally including all compilers compiling the given article
	 *
	 * @param article      the article to recompile
	 * @param recompileAll whether the compilers compiling anything on the articles should also recompile
	 */
	public static void recompile(Article article, boolean recompileAll) {
		recompile(article, recompileAll, null);
	}

	/**
	 * Recompiles the given article, optionally including all compilers compiling the given article
	 *
	 * @param article      the article to recompile
	 * @param recompileAll whether the compilers compiling anything on the articles should also recompile
	 * @param reason optional reason why the recompile was requested
	 */
	public static void recompile(Article article, boolean recompileAll, @Nullable String reason) {
		ArticleManager articleManager = article.getArticleManager();
		Objects.requireNonNull(articleManager);
		articleManager.open();
		try {
			if (recompileAll) {
				List<Article> articlesToRecompile = getCompilerArticles(article).toList();
				LOGGER.info("Starting FULL recompilation for article " + article.getTitle() +
						(reason == null ? "" : "\nReason: " + reason) +
						"\nRecompiling the following " + Strings.pluralOf(articlesToRecompile.size(), "article") + ": " +
						articlesToRecompile.stream()
								.map(Article::getTitle)
								.collect(Collectors.joining(", ")));
				for (Article recompileArticle : articlesToRecompile) {
					articleManager.registerArticle(recompileArticle.getTitle(), recompileArticle.getText());
					EventManager.getInstance().fireEvent(new FullParseEvent(recompileArticle));
				}
			}
			else {
				LOGGER.info("Starting recompilation of article " + article.getTitle() + (reason == null ? "" : ". Reason: " + reason));
				Article recompiledArticle = articleManager.registerArticle(article.getTitle(), article.getText());
				// also update all markups
				$(recompiledArticle).successor(AttachmentUpdateMarkup.class).stream().parallel().forEach(markup -> {
					LOGGER.info("Checking " + markup.get().getUrl(markup) + " for updates...");
					markup.get().performUpdate(markup);
				});
				EventManager.getInstance().fireEvent(new FullParseEvent(recompiledArticle));
			}
		}
		finally {
			articleManager.commit();
		}
	}

	@NotNull
	public static Stream<Article> getCompilerArticles(Article article) {
		List<GroupingCompiler> groupingCompilers = Compilers.getCompilers(article.getArticleManager(), GroupingCompiler.class);
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
	private static Stream<PackageCompiler> getGroupedPackageCompilers(List<GroupingCompiler> groupingCompilers, PackageCompiler c) {
		List<Stream<PackageCompiler>> streams = new ArrayList<>();
		streams.add(Stream.of(c));
		if (c instanceof GroupingCompiler) {
			streams.add(toPackageCompilers(((GroupingCompiler) c).getChildCompilers().stream()));
		}
		for (GroupingCompiler groupingCompiler : groupingCompilers) {
			if (groupingCompiler.getChildCompilers().contains(c)) {
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
