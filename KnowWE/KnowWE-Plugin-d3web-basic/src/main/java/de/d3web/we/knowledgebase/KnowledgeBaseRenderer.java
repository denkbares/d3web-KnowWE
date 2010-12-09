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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Renders a knowledge base markup into the wiki page.
 * 
 * @author volker_belli
 * @created 13.10.2010
 */
public final class KnowledgeBaseRenderer extends DefaultMarkupRenderer<KnowledgeBaseType> {

	public KnowledgeBaseRenderer() {
		super("KnowWEExtension/d3web/icon/knowledgebase24.png", false);
	}

	@Override
	protected void renderContents(KnowWEArticle article, Section<KnowledgeBaseType> section, KnowWEUserContext user, StringBuilder string) {
		String title = KnowledgeBaseType.getContent(section).trim();
		String id = KnowledgeBaseType.getAnnotation(section, KnowledgeBaseType.ANNOTATION_ID);
		String author = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_AUTHOR);
		String comment = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_COMMENT);
		String version = KnowledgeBaseType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_VERSION);

		// render title line
		string.append(KnowWEUtils.maskHTML("<b>" + title + "</b>"));
		if (id != null) {
			string.append(" (").append(id).append(")");
		}
		string.append("\n");

		// render information block
		if (version != null || author != null || comment != null) {
			string.append(KnowWEUtils.maskHTML("<div style='padding-top:1em;'>"));

			if (version != null) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/d3web/icon/date16.png'></img> "));
				string.append(version).append("\n");
			}
			if (author != null) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/d3web/icon/author16.png'></img> "));
				string.append(author).append("\n");
			}
			if (comment != null) {
				string.append(KnowWEUtils.maskHTML("<img src='KnowWEExtension/d3web/icon/comment16.png'></img> "));
				string.append(comment).append("\n");
			}

			string.append(KnowWEUtils.maskHTML("</div>"));
		}

		// render used packages and their erroneous pages
		string.append(KnowWEUtils.maskHTML("<div style='padding-top:1em;'>"));
		// string.append(KnowWEUtils.maskHTML("<hr>\n"));
		List<Section<? extends AnnotationType>> compileSections = DefaultMarkupType.getAnnotationSections(
				section, KnowledgeBaseType.ANNOTATION_COMPILE);
		for (Section<?> annotationSection : compileSections) {
			Section<KnowledgeBaseCompileType> compileSection = annotationSection.findChildOfType(KnowledgeBaseCompileType.class);
			String packageName = compileSection.getOriginalText().trim();
			renderCompile(article, packageName, string);
		}
		string.append(KnowWEUtils.maskHTML("</div>"));
	}

	private void renderCompile(KnowWEArticle article, String packageName, StringBuilder string) {

		KnowWEPackageManager packageManager =
				KnowWEEnvironment.getInstance().getPackageManager(article.getWeb());
		List<Section<?>> packageDefinitions = packageManager.getPackageDefinitions(packageName);

		Collection<KDOMError> kdomErrors = new LinkedList<KDOMError>();
		Collection<KDOMWarning> kdomWarnings = new LinkedList<KDOMWarning>();

		Set<KnowWEArticle> errorArticles = new HashSet<KnowWEArticle>();
		Set<KnowWEArticle> warningArticles = new HashSet<KnowWEArticle>();

		for (Section<?> packageDef : packageDefinitions) {
			Collection<KDOMError> errors = KnowWEUtils.getMessagesFromSubtree(
					article, packageDef, KDOMError.class);
			if (errors != null && errors.size() > 0) {
				kdomErrors.addAll(errors);
				errorArticles.add(packageDef.getArticle());
			}
			Collection<KDOMWarning> warnings = KnowWEUtils.getMessagesFromSubtree(
					article, packageDef, KDOMWarning.class);
			if (warnings != null && warnings.size() > 0) {
				kdomWarnings.addAll(warnings);
				warningArticles.add(packageDef.getArticle());
			}
		}

		int errorsCount = kdomErrors.size();
		int warningsCount = kdomWarnings.size();
		boolean hasErrors = errorsCount > 0;
		boolean hasWarnings = warningsCount > 0;

		String icon = "KnowWEExtension/d3web/icon/uses_" +
				(hasErrors ? "error" : hasWarnings ? "warn" : "ok") +
				"16.gif";
		string.append(KnowWEUtils.maskHTML("<img src='" + icon + "'></img> "));
		string.append("uses package: ").append(packageName);
		if (hasErrors) {
			string.append(" (").append(errorsCount).append(" errors in ");
			string.append(errorArticles.size()).append(" articles)");
			renderDefectArticleNames(errorArticles, string);
			// renderDefectArticleNames(kdomErrors, icon, string);
			// renderDefectArticleNames(messagesErrors, icon, string);
		}
		else if (hasWarnings) {
			string.append(" (").append(warningsCount).append(" warnings in ");
			string.append(warningArticles.size()).append(" articles)");
			renderDefectArticleNames(warningArticles, string);
			// renderDefectArticleNames(kdomWarnings, icon, string);
			// renderDefectArticleNames(messagesWarnings, icon, string);
		}
	}

	private void renderDefectArticleNames(Set<KnowWEArticle> articles, StringBuilder string) {
		// print all articles out as links (ordered alphabetically, duplicates
		// removed)
		List<String> names = new ArrayList<String>(articles.size());
		for (KnowWEArticle article : articles) {
			names.add(article.getTitle());
		}
		Collections.sort(names);

		string.append(KnowWEUtils.maskHTML("<ul>"));
		for (String name : names) {
			string.append(KnowWEUtils.maskHTML("<li>"));
			string.append("[").append(name).append("]");
			string.append("\n");
		}
		string.append(KnowWEUtils.maskHTML("</ul>"));
	}

}