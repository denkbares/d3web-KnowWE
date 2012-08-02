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

package de.d3web.we.knowledgebase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * Renders a knowledge base markup into the wiki page.
 * 
 * @author volker_belli
 * @created 13.10.2010
 */
public final class KnowledgeBaseRenderer extends DefaultMarkupRenderer {

	public KnowledgeBaseRenderer() {
		super("KnowWEExtension/d3web/icon/knowledgebase24.png");
	}

	@Override
	protected void renderContents(Section<?> section, UserContext user, StringBuilder string) {
		String title = KnowledgeBaseType.getContent(section).trim();
		String id = KnowledgeBaseType.getAnnotation(section, KnowledgeBaseType.ANNOTATION_ID);
		String author = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_AUTHOR);
		String comment = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_COMMENT);
		String version = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_VERSION);
		String filename = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_FILENAME);

		// render title line
		string.append(Strings.maskHTML("<b>" + title + "</b>"));
		if (id != null) {
			string.append(" (").append(id).append(")");
		}
		string.append("\n");

		// render information block
		if (version != null || author != null || comment != null || filename != null) {
			string.append(Strings.maskHTML("<div style='padding-top:1em;'>"));

			if (version != null) {
				string.append(Strings.maskHTML("<img src='KnowWEExtension/d3web/icon/date16.png'></img> "));
				string.append(version).append("\n");
			}
			if (author != null) {
				string.append(Strings.maskHTML("<img src='KnowWEExtension/d3web/icon/author16.png'></img> "));
				string.append(author).append("\n");
			}
			if (comment != null) {
				string.append(Strings.maskHTML("<img src='KnowWEExtension/d3web/icon/comment16.png'></img> "));
				string.append(comment).append("\n");
			}
			if (filename != null) {
				string.append(Strings.maskHTML("<img src='KnowWEExtension/d3web/icon/download16.gif'></img> "));
				string.append(filename).append("\n");
			}

			string.append(Strings.maskHTML("</div>"));
		}

		// render used packages and their erroneous pages
		string.append(Strings.maskHTML("<div style='padding-top:1em;'>"));
		// string.append(KnowWEUtils.maskHTML("<hr>\n"));
		Section<KnowledgeBaseCompileType> compileSection = Sections.findSuccessor(section,
				KnowledgeBaseCompileType.class);
		Collection<String> packagesToCompile = compileSection.get().getPackagesToCompile(
				compileSection);

		for (Iterator<String> packageIter = packagesToCompile.iterator(); packageIter.hasNext();) {
			String packageName = packageIter.next();
			renderCompile(section.getArticle(), packageName, string);
			if (packageIter.hasNext()) string.append(Strings.maskHTML("<br/>"));
		}
		string.append(Strings.maskHTML("</div>"));
	}

	private void renderCompile(Article article, String packageName, StringBuilder string) {

		PackageManager packageManager =
				Environment.getInstance().getPackageManager(article.getWeb());
		List<Section<?>> sectionsOfPackage = packageManager.getSectionsOfPackage(packageName);

		Collection<Message> kdomErrors = new LinkedList<Message>();
		Collection<Message> kdomWarnings = new LinkedList<Message>();

		Set<Article> errorArticles = new HashSet<Article>();
		Set<Article> warningArticles = new HashSet<Article>();

		for (Section<?> sectionOfPackage : sectionsOfPackage) {
			Collection<Message> allmsgs = Messages.getMessagesFromSubtree(article,
					sectionOfPackage, Message.Type.ERROR, Message.Type.WARNING);
			Collection<Message> errors = Messages.getErrors(allmsgs);
			Collection<Message> warnings = Messages.getWarnings(allmsgs);
			if (errors != null && errors.size() > 0) {
				kdomErrors.addAll(errors);
				errorArticles.add(sectionOfPackage.getArticle());
			}
			if (warnings != null && warnings.size() > 0) {
				kdomWarnings.addAll(warnings);
				warningArticles.add(sectionOfPackage.getArticle());
			}
		}

		int errorsCount = kdomErrors.size();
		int warningsCount = kdomWarnings.size();
		boolean hasErrors = errorsCount > 0;
		boolean hasWarnings = warningsCount > 0;

		String icon = "KnowWEExtension/d3web/icon/uses_" +
				(hasErrors ? "error" : hasWarnings ? "warn" : "ok") +
				"16.gif";
		string.append(Strings.maskHTML("<img src='" + icon + "'></img> "));
		string.append("uses package: ").append(packageName);
		if (hasErrors) {
			string.append(" (").append(errorsCount).append(" errors in ");
			string.append(errorArticles.size()).append(
					" article" + (errorArticles.size() > 1 ? "s" : "") + ")");
			renderDefectArticleNames(errorArticles, string);
			// renderDefectArticleNames(kdomErrors, icon, string);
			// renderDefectArticleNames(messagesErrors, icon, string);
		}
		else if (hasWarnings) {
			string.append(" (").append(warningsCount).append(" warnings in ");
			string.append(warningArticles.size()).append(
					" article" + (warningArticles.size() > 1 ? "s" : "") + ")");
			renderDefectArticleNames(warningArticles, string);
			// renderDefectArticleNames(kdomWarnings, icon, string);
			// renderDefectArticleNames(messagesWarnings, icon, string);
		}
	}

	private void renderDefectArticleNames(Set<Article> articles, StringBuilder string) {
		// print all articles out as links (ordered alphabetically, duplicates
		// removed)
		List<String> names = new ArrayList<String>(articles.size());
		for (Article article : articles) {
			names.add(article.getTitle());
		}
		Collections.sort(names);

		string.append(Strings.maskHTML("<ul>"));
		for (String name : names) {
			string.append(Strings.maskHTML("<li>"));
			string.append("[").append(name).append("]");
			string.append("\n");
		}
		string.append(Strings.maskHTML("</ul>"));
	}

}