package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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

/**
 * An action to retrieve the wiki source text of a chapter of a wiki page.
 * For other parts of wiki source (i.e. markup block) see GetSectionTextAction in the core-plugins.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.09.16.
 */
public class GetWikiSectionTextAction extends GetSectionTextAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String wikiReference = context.getParameter("reference");

		if (wikiReference == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "URL parameter 'reference' missing.");
			return;
		}

		Article auxiliaryArticle = Article.createTemporaryArticle("%%include\n[" + wikiReference + "]\n%", "AuxArticle", "AuxWeb");
		try {

			Section<WikiReference> wikiReferenceSection = Sections.successor(auxiliaryArticle.getRootSection(), WikiReference.class);
			if (wikiReferenceSection == null) {
				context.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid reference: " + wikiReference);
				return;
			}

			Section<?> headerSection = WikiReference.findReferencedSection(wikiReferenceSection, KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB));
			if (headerSection != null && headerSection.get() instanceof HeaderType) {
				List<Section<? extends Type>> sectionList = JSPWikiMarkupUtils.getContent(Sections.cast(headerSection, HeaderType.class));
				StringBuilder sectionText = new StringBuilder();
				Instant lastModified = Environment.getInstance()
						.getWikiConnector()
						.getLastModifiedDate(headerSection.getTitle(), -1)
						.toInstant();
				for (Section<? extends Type> subSection : sectionList) {
					if (KnowWEUtils.canView(subSection, context)) {
						sectionText.append(subSection.getText());
					}
					else {
						context.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to view/download section");
						return;
					}
				}
				writeFile(context, sectionText.toString(), wikiReference, lastModified);
			}
			else {
				context.sendError(HttpServletResponse.SC_NOT_FOUND, "Reference not found: " + wikiReference);
			}
		}
		finally {
			auxiliaryArticle.destroy(null);
		}
	}
}
