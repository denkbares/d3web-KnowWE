/*
 * Copyright (C) ${year} denkbares GmbH, Germany
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

package de.knowwe.core.compile.packaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.ToolMenuDecoratingRenderer;
import de.knowwe.util.Icon;

/**
 * Renders a {@link DefaultMarkupType} section the does package compilation.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.10.2010
 */
public class DefaultMarkupPackageCompileTypeRenderer extends DefaultMarkupRenderer {

	public DefaultMarkupPackageCompileTypeRenderer(String icon) {
		super(icon);
	}

	public DefaultMarkupPackageCompileTypeRenderer() {
		this("KnowWEExtension/d3web/icon/knowledgebase24.png");
	}

	@Override
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult string) {
		renderPackages(section, user, string);
	}

	private void renderPackages(Section<?> section, UserContext user, RenderResult string) {
		// render used packages and their erroneous pages
		string.appendHtml("<div style='padding-top:1em;'>");
		// string.append(KnowWEUtils.maskHTML("<hr>\n"));
		Section<DefaultMarkupPackageCompileType> compileSection = Sections.successor(section,
				DefaultMarkupPackageCompileType.class);
		assert compileSection != null;
		String[] packagesToCompile = compileSection.get().getPackagesToCompile(
				compileSection);

		for (String packageName : packagesToCompile) {
			renderPackage(compileSection, section, packageName, string, user);
		}

		string.appendHtml("</div>");
	}

	private void renderPackage(Section<? extends PackageCompileType> compileSection, Section<?> section, String packageName, RenderResult string, UserContext user) {

		Collection<Message> kdomErrors = new LinkedList<>();
		Collection<Message> kdomWarnings = new LinkedList<>();

		Set<Article> errorArticles = new HashSet<>();
		Set<Article> warningArticles = new HashSet<>();

		for (Section<?> sectionWithMessage : Messages.getSectionsWithMessages(Message.Type.WARNING, Message.Type.ERROR)) {
			if (!sectionWithMessage.getPackageNames().contains(packageName)) continue;
			Collection<PackageCompiler> packageCompilers = compileSection.get().getPackageCompilers(compileSection);
			Collection<Message> errors = new ArrayList<>();
			Collection<Message> warnings = new ArrayList<>();
			Map<Compiler, Collection<Message>> allmsgs = Messages.getMessagesMap(
					sectionWithMessage, Message.Type.ERROR, Message.Type.WARNING);
			for (PackageCompiler packageCompiler : packageCompilers) {
				Collection<Message> compileMessages = allmsgs.get(packageCompiler);
				if (compileMessages == null) continue;
				errors.addAll(Messages.getErrors(compileMessages));
				warnings.addAll(Messages.getWarnings(compileMessages));
			}
			if (!errors.isEmpty()) {
				kdomErrors.addAll(errors);
				errorArticles.add(sectionWithMessage.getArticle());
			}
			if (!warnings.isEmpty()) {
				kdomWarnings.addAll(warnings);
				warningArticles.add(sectionWithMessage.getArticle());
			}
		}

		int errorsCount = kdomErrors.size();
		int warningsCount = kdomWarnings.size();
		boolean hasErrors = errorsCount > 0;
		boolean hasWarnings = warningsCount > 0;

		String icon;
		if (hasErrors) {
			icon = Icon.ERROR.addTitle("Package with error(s)").toHtml();
		}
		else if (hasWarnings) {
			icon = Icon.WARNING.addTitle("Package with warning(s)").toHtml();
		}
		else {
			icon = Icon.PACKAGE.addTitle("Package").addClasses("packageOpacity").toHtml();
		}

		string.appendHtml(icon);
		string.append(" ");
		RenderResult subString = new RenderResult(string);
		subString.appendHtml("<span class='style-package-name'>");
		subString.append(packageName);
		subString.appendHtml("</span>");
		boolean rendered = false;
		List<Section<PackageTerm>> packageTermSections = Sections.successors(
				compileSection.getArticle().getRootSection(), PackageTerm.class);
		for (Section<PackageTerm> packageTermSection : packageTermSections) {
			if (packageTermSection.get().getTermName(packageTermSection).equals(packageName)) {
				ToolMenuDecoratingRenderer.renderToolMenuDecorator(subString,
						packageTermSection.getID(), true, string);
				rendered = true;
				break;
			}
		}
		if (!rendered) {
			string.append(subString);
		}
		if (hasErrors) {
			string.append(" (").append(errorsCount).append(" errors in ");
			string.append(errorArticles.size()).append(
					" article" + (errorArticles.size() > 1 ? "s" : "") + ")");
			renderDefectArticleNames(errorArticles, string);
			// renderDefectArticleNames(kdomErrors, icon, string);
			// renderDefectArticleNames(messagesErrors, icon, string);
		}
		if (hasWarnings) {
			string.append(" (").append(warningsCount).append(" warnings in ");
			string.append(warningArticles.size()).append(
					" article" + (warningArticles.size() > 1 ? "s" : "") + ")");
			renderDefectArticleNames(warningArticles, string);
			// renderDefectArticleNames(kdomWarnings, icon, string);
			// renderDefectArticleNames(messagesWarnings, icon, string);
		}
		if (!(hasWarnings || hasErrors)) {
			string.appendHtml("<br/>");
		}
	}

	private void renderDefectArticleNames(Set<Article> articles, RenderResult string) {
		// print all articles out as links (ordered alphabetically, duplicates
		// removed)
		string.appendHtml("<ul style='white-space: normal'>");
		int limit = articles.size() <= 5 ? 5 : 3;
		articles.stream().sorted(Comparator.comparing(Article::getTitle)).limit(limit).forEach(article -> {
			string.appendHtml("<li>");
			string.appendHtmlElement("a", article.getTitle(), "href", KnowWEUtils.getURLLink(article));
			string.appendHtml("</li>");
			string.append("\n");
		});
		if (limit == 3) {
			string.appendHtmlElement("li", articles.size() - limit + " more...");
		}
		string.appendHtml("</ul>");
	}
}
