package de.knowwe.ontology.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Value;

import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCore;

public class ArticleLinkNodeRenderer implements SparqlResultNodeRenderer {

	@Override
	public String renderNode(Value node, String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
		/*
		First, check for exact matches e.g, for plain Strings
         */
		ArticleManager articleManager = user.getArticleManager();
		if (articleManager.getArticle(text) != null) {
			return KnowWEUtils.getLinkHTMLToArticle(text);
		}

		String lns = core.getLocalNamespace();

		// We are only interested in statements from the local name space.
		// Other name spaces or no name space (simple string) probably is not
		// representing an article in the wiki.
		Pattern articleLinkPattern = Pattern.compile(Pattern.quote(lns) + "\\S+");
		Matcher matcher = articleLinkPattern.matcher(text);
		StringBuilder newText = new StringBuilder(text);
		int offSet = 0;
		while (matcher.find()) {
			String link = matcher.group();
			String title = Strings.decodeURL(link.substring(lns.length()));
			if (articleManager.getArticle(title) != null) {
				if (mode == RenderMode.HTML) {
					String htmlLink = RenderResult.mask(KnowWEUtils.getLinkHTMLToArticle(title), user);
					newText.replace(matcher.start() + offSet, matcher.end() + offSet, htmlLink);
					offSet += htmlLink.length() - link.length();
				}
				if (mode == RenderMode.PlainText) {
					newText.replace(matcher.start(), matcher.end(), title);
				}
			}
		}
		return newText.toString();
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
