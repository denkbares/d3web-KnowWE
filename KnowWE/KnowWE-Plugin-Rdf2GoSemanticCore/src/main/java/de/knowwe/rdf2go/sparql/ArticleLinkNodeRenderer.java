package de.knowwe.rdf2go.sparql;

import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class ArticleLinkNodeRenderer implements SparqlResultNodeRenderer {

	@Override
	public String renderNode(String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
        /*
        First, check for exact matches e.g, for plain Strings
         */
        if(Environment.getInstance().getWikiConnector().doesArticleExist(text)) {
            return KnowWEUtils.getLinkHTMLToArticle(text);
        }



        boolean foundArticle = false;
        String lns = core.getLocalNamespace();

		String[] statements;
		// We are only interested in statements from the local name space.
		// Other name spaces or no name space (simple string) probably is not
		// representing an article in the wiki.
		if (text.startsWith(lns)) {
			statements = text.split(" ");
		}
		else {
			return text;
		}

		String[] articleLinks = new String[statements.length];
		for (int i = 0; i < statements.length; i++) {
			String statement = statements[i];
			statement = Strings.decodeURL(statement);
			if (statement.isEmpty()) continue;
			String title = Rdf2GoUtils.trimNamespace(core, statement);

			Article article = user.getArticleManager().getArticle(title);
			if (article != null) {
				foundArticle = true;
				if (mode == RenderMode.HTML) {
					articleLinks[i] = new RenderResult(user)
							.appendHtml(KnowWEUtils.getLinkHTMLToArticle(article.getTitle()))
							.toStringRaw();
				}
				if (mode == RenderMode.PlainText) {
					articleLinks[i] = new RenderResult(user)
							.append(article.getTitle()).toStringRaw();
				}
			}
		}
		if (foundArticle) {
			boolean first = true;
			StringBuilder links = new StringBuilder();
			for (String articleLink : articleLinks) {
				if (first) {
					first = false;
				}
				else {
					links.append(", ");
				}
				links.append(articleLink);
			}
			return links.toString();
		}
		return text;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
