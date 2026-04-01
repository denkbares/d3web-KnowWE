package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.Environment;
import de.knowwe.core.action.GetSectionTextAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.jspwiki.types.HeaderType;

import static de.knowwe.include.ImportMarker.REFERENCE;

/**
 * An action to retrieve the wiki source text of a chapter of a wiki page.
 * For other parts of wiki source (i.e. markup block) see GetSectionTextAction in the core-plugins.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.09.16.
 */
public class GetWikiSectionTextAction extends GetSectionTextAction {
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GetWikiSectionTextAction.class);

	record SourceInfo(Section<?> referencedSection, @Nullable String sourceText, @Nullable Instant sourceLatestChange) {
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiReference = context.getParameter(REFERENCE);

		if (wikiReference == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "URL parameter 'reference' missing.");
			return;
		}

		SourceInfo sourceInfo = getSourceInfo(wikiReference, context);
		if (sourceInfo == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Reference not found: " + wikiReference);
			return;
		}
		if (sourceInfo.sourceText() == null) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to view/download section");
			return;
		}
		ImportMarker.markAsImported(sourceInfo.referencedSection(), context);
		writeFile(context, sourceInfo.sourceText(), wikiReference, sourceInfo.sourceLatestChange());
	}

	static SourceInfo getSourceInfo(String wikiReference, UserActionContext context) {
		Article auxiliaryArticle = Article.createTemporaryArticle("%%include\n[" + wikiReference + "]\n%", "AuxArticle", "AuxWeb");
		try {
			Section<WikiReference> wikiReferenceSection = Sections.successor(auxiliaryArticle.getRootSection(), WikiReference.class);
			if (wikiReferenceSection == null) return null;
			return getSourceInfo(wikiReferenceSection, context);
		}
		finally {
			auxiliaryArticle.destroy(null);
		}
	}

	private static SourceInfo getSourceInfo(Section<WikiReference> wikiReferenceSection, UserActionContext context) {
		Section<?> referencedSection = WikiReference.findReferencedSection(wikiReferenceSection, KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB));
		if (referencedSection == null) return null;
		if (!KnowWEUtils.canView(referencedSection, context)) return new SourceInfo(referencedSection, null, null);

		Instant sourceLatestChange = Environment.getInstance()
				.getWikiConnector()
				.getLastModifiedDate(referencedSection.getTitle(), -1)
				.toInstant();
		StringBuilder sectionText = new StringBuilder();
		if (referencedSection.get() instanceof HeaderType) {
			List<Section<? extends Type>> sectionList = JSPWikiMarkupUtils.getContent(Sections.cast(referencedSection, HeaderType.class));
			for (Section<? extends Type> subSection : sectionList) {
				if (!KnowWEUtils.canView(subSection, context)) {
					return new SourceInfo(referencedSection, null, null);
				}
				sectionText.append(subSection.getText());
			}
		}
		else {
			sectionText.append(referencedSection.getText());
		}
		return new SourceInfo(referencedSection, sectionText.toString(), sourceLatestChange);
	}
}
