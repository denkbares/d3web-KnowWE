/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;

/**
 * 
 * @author Stefan Plehn
 * @created 22.05.2013
 */
public class PackageAnnotationName extends AbstractType {

	public PackageAnnotationName() {

		this.setSectionFinder(new AllTextSectionFinder());
		this.setRenderer(new PackageAnnotationNameRenderer());

	}

	private class PackageAnnotationNameRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult string) {
			renderCompile(section, section.getText(), string);

		}

		private RenderResult renderCompile(Section<?> section, String packageName, RenderResult string) {

			Article article = section.getArticle();

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
			string.appendHtml("<img src='" + icon + "'></img> ");
			string.append("uses package: ");

			// .append(packageName);
			// if (hasErrors) {
			// string.append(" (").append(errorsCount).append(" errors in ");
			// string.append(errorArticles.size()).append(
			// " article" + (errorArticles.size() > 1 ? "s" : "") + ")");
			// string.append(renderDefectArticleNames(errorArticles, string));
			// // renderDefectArticleNames(kdomErrors, icon, string);
			// // renderDefectArticleNames(messagesErrors, icon, string);
			// }
			// else if (hasWarnings) {
			// string.append(" (").append(warningsCount).append(" warnings in ");
			// string.append(warningArticles.size()).append(
			// " article" + (warningArticles.size() > 1 ? "s" : "") + ")");
			// string.append(renderDefectArticleNames(warningArticles, string));
			// // renderDefectArticleNames(kdomWarnings, icon, string);
			// // renderDefectArticleNames(messagesWarnings, icon, string);
			// }
			return string;
		}

		// private RenderResult renderDefectArticleNames(Set<Article> articles,
		// RenderResult string) {
		// // print all articles out as links (ordered alphabetically,
		// // duplicates
		// // removed)
		// List<String> names = new ArrayList<String>(articles.size());
		// for (Article article : articles) {
		// names.add(article.getTitle());
		// }
		// Collections.sort(names);
		//
		// string.appendHtml("<ul>");
		// for (String name : names) {
		// string.appendHtml("<li>");
		// string.append("[").append(name).append("]");
		// string.append("\n");
		// }
		// string.appendHtml("</ul>");
		//
		// return string;
		// }

	}
}
