package de.knowwe.include;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Streams;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.GetSectionTextAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 *
 * An action to retrieve the wiki source text of a chapter of a wiki page.
 * For other parts of wiki source (i.e. markup block) see GetSectionTextAction in the core-plugins.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.09.16.
 */
public class GetWikiSectionTextAction extends GetSectionTextAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionText = "";
		String wikiReference = context.getParameter("reference");
		String fileName = null;
		List<Section<? extends Type>> sectionList = null;

		ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);

		if (wikiReference != null) {
			Article auxiliaryArticle = Article.createArticle("%%include\n["+wikiReference+"]\n%", "AuxArticle", "AuxWeb");
			Section<WikiReference> wikiReferenceSection = Sections.successor(auxiliaryArticle.getRootSection(), WikiReference.class);
			Section<?> headerSection = WikiReference.findReferencedSection(wikiReferenceSection, articleManager);
			if(headerSection != null && headerSection.get() instanceof HeaderType) {
				sectionList = JSPWikiMarkupUtils.getContent(Sections.cast(headerSection, HeaderType.class));
				fileName = wikiReference;
			} else {
				context.sendError(HttpServletResponse.SC_NOT_FOUND, "Reference not found: "+wikiReference);
				return;
			}
		}
		if (sectionList != null) {
			for (Section<? extends Type> subSection : sectionList) {
				if (KnowWEUtils.canView(subSection, context)) {
					sectionText += subSection.getText();
				}
				else {
					context.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to view/download section");
					return;
				}
			}
		}
		else {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No valid section id or title and name given");
			return;
		}

		writeFile(context, sectionText, fileName);
	}


}
