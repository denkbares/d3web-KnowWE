package de.d3web.we.kdom.kopic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.report.Message;
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

public final class KnowledgeBaseRenderer extends DefaultMarkupRenderer<KnowledgeBaseType> {

	public KnowledgeBaseRenderer() {
		super("KnowWEExtension/d3web/icon/knowledgebase24.png");
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

		string.append(KnowWEUtils.maskHTML("<b>" + title + "</b>"));
		if (id != null) {
			string.append(" (").append(id).append(")");
		}

		string.append("\n");
		if (version != null || author != null || comment != null) {
			string.append(KnowWEUtils.maskHTML("<p style='padding-top:0.5em;'>"));
		}

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

		string.append("\n");
		string.append(KnowWEUtils.maskHTML("<p style='padding-top:0.5em;'>"));
		// string.append(KnowWEUtils.maskHTML("<hr>\n"));
		List<Section<? extends AnnotationType>> compileSections = DefaultMarkupType.getAnnotationSections(
				section, KnowledgeBaseType.ANNOTATION_COMPILE);
		for (Section<?> annotationSection : compileSections) {
			Section<KnowledgeBaseCompileType> compileSection = annotationSection.findChildOfType(KnowledgeBaseCompileType.class);
			String packageName = compileSection.getOriginalText().trim();
			renderCompile(article, packageName, string);
		}
	}

	private void renderCompile(KnowWEArticle article, String packageName, StringBuilder string) {

		KnowWEPackageManager packageManager =
				KnowWEEnvironment.getInstance().getPackageManager(article.getWeb());
		List<Section<?>> packageDefinitions = packageManager.getPackageDefinitions(packageName);

		Collection<Message> messagesErrors = new LinkedList<Message>();
		Collection<Message> messagesWarnings = new LinkedList<Message>();
		Collection<KDOMError> kdomErrors = new LinkedList<KDOMError>();
		Collection<KDOMWarning> kdomWarnings = new LinkedList<KDOMWarning>();

		Set<KnowWEArticle> errorArticles = new HashSet<KnowWEArticle>();
		Set<KnowWEArticle> warningArticles = new HashSet<KnowWEArticle>();

		for (Section<?> packageDef : packageDefinitions) {
			for (Message m : KnowWEUtils.getMessagesFromSubtree(article,
					packageDef, Message.class)) {
				if (m.getMessageType().equals(Message.ERROR)) {
					messagesErrors.add(m);
					errorArticles.add(packageDef.getArticle());
				}
				else if (m.getMessageType().equals(Message.WARNING)) {
					messagesWarnings.add(m);
					warningArticles.add(packageDef.getArticle());
				}
			}
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

		int errorsCount = messagesErrors.size() + kdomErrors.size();
		int warningsCount = messagesWarnings.size() + kdomWarnings.size();
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