package de.knowwe.rdf2go.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.strings.Strings;
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
		if (user.getArticleManager().getArticle(text) != null) {
			return KnowWEUtils.getLinkHTMLToArticle(text);
		}

		boolean foundArticle = false;
		String lns = core.getLocalNamespace();

		String separator = ", ";
		List<String> statements = new ArrayList<>();
		// We are only interested in statements from the local name space.
		// Other name spaces or no name space (simple string) probably is not
		// representing an article in the wiki.
		if (text.contains(lns)) {
			Matcher matcher = Pattern.compile("([,;:.]?\\s+|\\s*[-+*#]\\s*)(?=" + Pattern.quote(lns) + ")")
					.matcher(text);
			int start = 0;
			while (matcher.find()) {
				statements.add(text.substring(start, matcher.start()));
				start = matcher.end();
				separator = matcher.group(1);
			}
			statements.add(text.substring(start, text.length()));
		}
		else {
			return text;
		}

		List<String> articleLinks = new ArrayList<>(statements.size());
		for (String statement : statements) {
			statement = Strings.decodeURL(statement);
			if (statement.isEmpty()) continue;
			String title = Rdf2GoUtils.trimNamespace(core, statement);

			Article article = user.getArticleManager().getArticle(title);
			if (article != null) {
				foundArticle = true;
				if (mode == RenderMode.HTML) {
					articleLinks.add(new RenderResult(user)
							.appendHtml(KnowWEUtils.getLinkHTMLToArticle(article.getTitle()))
							.toStringRaw());
				}
				if (mode == RenderMode.PlainText) {
					articleLinks.add(RenderResult.mask(article.getTitle(), user));
				}
			}
		}
		if (foundArticle) {
			StringBuilder links = new StringBuilder();
			for (int i = 0; i < articleLinks.size(); i++) {
				String articleLink = articleLinks.get(i);
				links.append(separator);
				links.append(articleLink);
			}
			return links.toString().replaceAll("^\\s*?\n", "");
		}
		return text;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
