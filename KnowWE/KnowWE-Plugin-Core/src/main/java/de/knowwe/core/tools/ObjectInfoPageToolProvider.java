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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;

/**
 * @author volker_belli
 * @created 01.12.2010
 */
public class ObjectInfoPageToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return getIdentifier(section) != null;
	}

	private Identifier getIdentifier(Section<?> section) {
		if (section.get() instanceof Term) {
			Term termType = (Term) section.get();
			return termType.getTermIdentifier(Sections.cast(section, Term.class));
		}
		return null;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Identifier identifier = getIdentifier(section);
		if (identifier == null) return ToolUtils.emptyToolArray();
		Section<? extends Term> term = Sections.cast(section, Term.class);

		// get sorted list of all defining articles
		Set<String> articleNames = new HashSet<String>();
		for (TermCompiler termCompiler : Compilers.getCompilers(section, TermCompiler.class)) {
			TerminologyManager manager = termCompiler.getTerminologyManager();
			Collection<Section<?>> definitions = manager.getTermDefiningSections(identifier);
			for (Section<?> definition : definitions) {
				articleNames.add(definition.getTitle());
			}
		}
		List<String> sorted = new ArrayList<String>(articleNames);
		Collections.sort(sorted);

		// check if we have a home page for that term (article that has the same title)
		Article home = getHomeArticle(section.getArticleManager(), identifier);
		if (home != null) {
			sorted.remove(home.getTitle());
			sorted.add(0, home.getTitle());
		}

		// and remove the current article
		sorted.remove(userContext.getTitle());

		// create tools for edit, rename and definitions
		Tool[] tools = new Tool[sorted.size() + 2];
		int index = 0;
		tools[index++] = getObjectInfoPageTool(term);
		tools[index++] = getRenamingTool(term);
		for (String title : sorted) {
			String link = KnowWEUtils.getURLLink(title);
			String description = (home != null && title.equals(home.getTitle()))
					? "Opens the home page for the specific object."
					: "Opens the definition page for the specific object to show its usage inside this wiki.";
			tools[index++] = new DefaultTool(
					"KnowWEExtension/images/article16.png",
					"Open '" + title + "'", description,
					"window.location.href = '" + Strings.encodeHtml(link) + "'");
		}
		return tools;
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
		if (article != null) return article;

		return null;
	}

	protected Tool getObjectInfoPageTool(Section<? extends Term> section) {
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/infoPage16.png",
				"Show Info Page",
				"Opens the information page for the specific object to show its usage inside this wiki.",
				createObjectInfoJSAction(section));
	}

	protected Tool getRenamingTool(Section<? extends Term> section) {
		return new DefaultTool(
				"http://localhost:8080/KnowWE/KnowWEExtension/images/textfield_rename.png",
				"Rename",
				"Rename this term wiki wide.",
				createRenamingAction(section));
	}

	public static String createObjectInfoJSAction(Section<? extends Term> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(section);
		return createObjectInfoPageJSAction(termIdentifier);
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

	public static String createRenamingAction(Section<? extends Term> section) {
		return "KNOWWE.plugin.renaming.renameTerm('" + section.getID() + "')";
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
