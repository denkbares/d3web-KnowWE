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

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageTermDefinition;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.ScopeUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.plugin.Plugins;

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
	protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
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
		string.appendHtml("<b>" + title + "</b>");
		if (id != null) {
			string.append(" (").append(id).append(")");
		}
		string.append("\n");

		// render information block
		if (version != null || author != null || comment != null || filename != null) {
			string.appendHtml("<div style='padding-top:1em;'>");

			if (version != null) {
				string.appendHtml("<img src='KnowWEExtension/d3web/icon/date16.png' /> ");
				string.append(version).append("\n");
			}
			if (author != null) {
				string.appendHtml("<img src='KnowWEExtension/d3web/icon/author16.png' /> ");
				string.append(author).append("\n");
			}
			if (comment != null) {
				string.appendHtml("<img src='KnowWEExtension/d3web/icon/comment16.png' /> ");
				string.append(comment).append("\n");
			}
			if (filename != null) {
				string.appendHtml("<img src='KnowWEExtension/d3web/icon/download16.gif' /> ");
				string.append(filename).append("\n");
			}

			string.appendHtml("</div>");
		}

		// render used packages and their erroneous pages
		string.appendHtml("<div style='padding-top:1em;'>");
		// string.append(KnowWEUtils.maskHTML("<hr>\n"));
		Section<KnowledgeBaseCompileType> compileSection = Sections.findSuccessor(section,
				KnowledgeBaseCompileType.class);
		Collection<String> packagesToCompile = compileSection.get().getPackagesToCompile(
				compileSection);

		for (Iterator<String> packageIter = packagesToCompile.iterator(); packageIter.hasNext();) {
			String packageName = packageIter.next();
			renderCompile(section.getArticle(), section, packageName, string, user);
			if (packageIter.hasNext()) string.appendHtml("<br/>");
		}

		// render plugged annotations
		Extension[] pluggedAnnotations = getPluggedAnnotation(section);

		for (Extension pluggedAnnotation : pluggedAnnotations) {
			Section<? extends AnnotationContentType> annotationContentSection = KnowledgeBaseType.getAnnotationContentSection(
					section, pluggedAnnotation.getName());
			if (annotationContentSection != null) {
				Section<AnnotationType> ancestorOfType = Sections.findAncestorOfType(
						annotationContentSection, AnnotationType.class);
				if (ancestorOfType != null) {
					ancestorOfType.get().getRenderer().render(ancestorOfType, user, string);
				}
			}
		}

		string.appendHtml("</div>");

	}

	private void renderCompile(Article article, Section<?> section, String packageName, RenderResult string, UserContext user) {

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

		Section<?> currentSection = null;
		List<Section<PackageTermDefinition>> packageSecs = Sections.findSuccessorsOfType(section,
				PackageTermDefinition.class);
		for (Section<PackageTermDefinition> packageSec : packageSecs) {
			if (packageSec.getText().equals(packageName)) {
				currentSection = packageSec;
				break;
			}

		}
		if (currentSection != null) {
			Section<? extends Type> grandFather = currentSection.getFather().getFather();
			Section<AnnotationType> annotationTypeSec = Sections.findSuccessor(grandFather,
					AnnotationType.class);
			annotationTypeSec.get().getRenderer().render(annotationTypeSec, user, string);
		}
		else {
		 
			String icon;
			if(hasErrors){
				icon = "KnowWEExtension/d3web/icon/uses_error16.gif";
			}
			else if(hasWarnings){
				icon = "KnowWEExtension/d3web/icon/warn_error16.gif";
			}
			else {
				icon = "KnowWEExtension/images/package_obj.gif";
			}

			string.appendHtml("<img style='position:relative; top:2px;' class='packageOpacity' src='"
					+ icon
					+ "' />");
			string.append(" ");
			string.appendHtml("<span style='color:rgb(121,79, 64);'>");
			string.append(packageName);
			string.appendHtml("</span>");
		}
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

	private void renderDefectArticleNames(Set<Article> articles, RenderResult string) {
		// print all articles out as links (ordered alphabetically, duplicates
		// removed)
		List<String> names = new ArrayList<String>(articles.size());
		for (Article article : articles) {
			names.add(article.getTitle());
		}
		Collections.sort(names);

		string.appendHtml("<ul>");
		for (String name : names) {
			string.appendHtml("<li>");
			string.append("[").append(name).append("]");
			string.append("\n");
		}
		string.appendHtml("</ul>");
	}

	private Extension[] getPluggedAnnotation(Section<?> sec) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_Annotation);
		extensions = ScopeUtils.getMatchingExtensions(extensions, sec.get().getPathToRoot());

		if (extensions.length >= 1) {
			return extensions;
		}
		else {
			return new Extension[0];
		}
	}

}