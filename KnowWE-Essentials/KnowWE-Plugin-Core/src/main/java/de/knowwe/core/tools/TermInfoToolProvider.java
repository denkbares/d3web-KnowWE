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
package de.knowwe.core.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * Provides tools to open definition of term or highlight it on the current page.
 *
 * @author volker_belli
 * @created 01.12.2010
 */
public class TermInfoToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return getTools(section, userContext).length > 0;
	}

	protected Identifier getIdentifier(TermCompiler compiler, Section<?> section) {
		if (compiler != null && section.get() instanceof Term) {
			try {
				return ((Term) section.get()).getTermIdentifier(compiler, Sections.cast(section, Term.class));
			}
			catch (ClassCastException ignore) {  // in case the identifier can only be generated for certain term compilers
			}
		}
		return null;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<TermCompiler> compilers = new ArrayList<>(Compilers.getCompilersWithCompileScript(section, TermCompiler.class));
		Optional<Identifier> identifierOpt = compilers.stream()
				.map(c -> getIdentifier(c, section))
				.filter(Objects::nonNull)
				.findFirst();
		if (!identifierOpt.isPresent()) return ToolUtils.emptyToolArray();
		Identifier identifier = identifierOpt.get();

		// get sorted list of all defining articles
		Map<String, Section<?>> articles = new HashMap<>();
		if (compilers.isEmpty()) compilers = new ArrayList<>(Compilers.getCompilers(section, TermCompiler.class));
		compilers.sort((o1, o2) -> {
			if (o1 instanceof PackageCompiler && o2 instanceof PackageCompiler) return 0;
			if (o1 instanceof PackageCompiler) return -1;
			return 1;
		});
		for (TermCompiler termCompiler : compilers) {
			Collection<Section<?>> definitions = getTermDefiningSections(termCompiler, section);
			for (Section<?> definition : definitions) {
				Section<?> previewAncestor = PreviewManager.getInstance().getPreviewAncestor(definition);
				articles.put(definition.getTitle(), previewAncestor == null ? definition : previewAncestor);
			}
		}
		List<String> sorted = new ArrayList<>(articles.keySet());
		Collections.sort(sorted);

		// check if we have a home page for that term (article that has the same title)
		final ArticleManager articleManager = section.getArticleManager();
		if (articleManager == null) {
			return ToolUtils.emptyToolArray();
		}
		Article home = getHomeArticle(articleManager, identifier);
		if (home != null) {
			sorted.remove(home.getTitle());
			sorted.add(0, home.getTitle());
		}

		// restrict to max number of items
		if (sorted.size() > 5) sorted = sorted.subList(0, 5);

		// remove or sort current page to bottom of list
		if (sorted.remove(userContext.getTitle())
				&& "termbrowser".equals(userContext.getParameter("location"))) {
			sorted.add(userContext.getTitle());
		}

		// create tools for edit, rename and definitions
		Tool[] tools = new Tool[sorted.size()];
		int index = 0;
		//tools[index++] = getCompositeEditTool(term);
		//tools[index++] = getRenamingTool(term);
		for (String title : sorted) {
			Section<?> definition = articles.get(title);
			String link;
			if (definition == null) {
				link = KnowWEUtils.getURLLink(title);
			}
			else {
				link = KnowWEUtils.getURLLink(definition);
			}
			String description;
			String titleText;

			if (title.equals(userContext.getTitle())) {
				titleText = "Highlight on this page";
				description = "Highlights the occurrence of the term on this page";
			}
			else {
				titleText = getTitle(title);
				description = getDescription(home, title);
			}
			tools[index++] = new DefaultTool(
					Icon.ARTICLE,
					titleText, description,
					link, Tool.ActionType.HREF, Tool.CATEGORY_NAVIGATE);
		}
		return tools;
	}

	protected @NotNull Collection<Section<?>> getTermDefiningSections(TermCompiler termCompiler, Section<?> section) {
		Identifier identifierForCompiler = getIdentifier(termCompiler, section);
		return termCompiler.getTerminologyManager().getTermDefiningSections(identifierForCompiler);
	}

	@NotNull
	public String getDescription(Article home, String title) {
		return (home != null && title.equals(home.getTitle()))
				? "Opens the home page for the specific object."
				: "Opens the definition page for the specific object to show its usage inside this wiki.";
	}

	@NotNull
	protected String getTitle(String title) {
		return "Open '" + title + "'";
	}

	private Article getHomeArticle(ArticleManager manager, Identifier identifier) {
		String[] prefixes = { "", "Info ", "About " };
		for (String prefix : prefixes) {
			Article article = getHomeArticle(manager, identifier, prefix);
			if (article != null) return article;
		}
		return null;
	}

	private Article getHomeArticle(ArticleManager manager, Identifier identifier, String prefix) {
		// check for original name
		Article article = manager.getArticle(prefix + identifier.toExternalForm());
		if (article != null) return article;

		// check if concatenated with space
		article = manager.getArticle(prefix + Strings.concat(" ", identifier.getPathElements()));
		if (article != null) return article;

		// check if concatenated with "-"
		article = manager.getArticle(prefix + Strings.concat("-", identifier.getPathElements()));
		if (article != null) return article;

		// check if concatenated with " - "
		article = manager.getArticle(prefix + Strings.concat(" - ", identifier.getPathElements()));
		if (article != null) return article;

		// check if concatenated with "#"
		article = manager.getArticle(prefix + Strings.concat("#", identifier.getPathElements()));
		if (article != null) return article;

		// check if concatenated with " # "
		article = manager.getArticle(prefix + Strings.concat(" # ", identifier.getPathElements()));
		if (article != null) return article;

		// check if concatenated with ":"
		article = manager.getArticle(prefix + Strings.concat(":", identifier.getPathElements()));
		if (article != null) return article;

		// check if concatenated with " : "
		article = manager.getArticle(prefix + Strings.concat(" : ", identifier.getPathElements()));
		return article;
	}

	public static String createObjectInfoPageJSAction(Identifier termIdentifier) {
		String lastPathElementExternalForm = new Identifier(termIdentifier.getLastPathElement()).toExternalForm();
		String externalTermIdentifierForm = termIdentifier.toExternalForm();
		return "window.location.href = "
				+ "'Wiki.jsp?page=ObjectInfoPage&amp;" + ObjectInfoTagHandler.TERM_IDENTIFIER
				+ "=' + encodeURIComponent('"
				+ maskTermForHTML(externalTermIdentifierForm)
				+ "') + '&amp;" + ObjectInfoTagHandler.OBJECT_NAME + "=' + encodeURIComponent('"
				+ maskTermForHTML(lastPathElementExternalForm) + "')";
	}

	public static String maskTermForHTML(String string) {
		string = string.replace("\\", "\\\\").replace("'", "\\'");
		string = Strings.encodeHtml(string);
		// in some strange wiki pages we got terms with linebreaks,
		// so handle them well
		string = string.replace("\n", "\\n").replace("\r", "\\r");
		return string;
	}
}
