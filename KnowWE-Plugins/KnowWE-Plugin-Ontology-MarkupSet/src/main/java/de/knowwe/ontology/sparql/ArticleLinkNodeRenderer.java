package de.knowwe.ontology.sparql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCore;

public class ArticleLinkNodeRenderer implements SparqlResultNodeRenderer {

	private static final Pattern LINE_BREAK = getGroupedLinksPattern("\n");
	private static final Pattern COMMA = getGroupedLinksPattern(",");
	private static final Pattern SEMICOLON = getGroupedLinksPattern(";");
	private static final Pattern WHITE_SPACE = getGroupedLinksPattern(" ");

	@Override
	public String renderNode(Value node, String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
		// no-op in case we don't render a wiki page in this context but rather perform an action such as Excel Export
		if (user.getTitle() == null) {
			return text;
		}

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
		Matcher matcher = Pattern.compile("(" + Pattern.quote(lns) + "\\S+)").matcher(text);
		StringBuilder newText = new StringBuilder(text);
		int offSet = 0;
		boolean foundLinks = false;
		while (matcher.find()) {
			foundLinks = true;
			String link = matcher.group();
			String title = Strings.decodeURL(link.substring(lns.length()));
			if (articleManager.getArticle(title) != null) {
				if (mode == RenderMode.HTML) {
					offSet = replaceGroupWithLinkAndUpdateOffset(user, matcher, newText, offSet, title);
				}
				if (mode == RenderMode.PlainText) {
					newText.replace(matcher.start(), matcher.end(), title);
				}
			}
		}
		if (foundLinks) return newText.toString();

		// try some heuristics to find article links in grouped literals with commonly used separators
		if (text.contains("\n") && tryGroupedLinks(text, user, newText, LINE_BREAK)) return newText.toString();
		if (text.contains(",") && tryGroupedLinks(text, user, newText, COMMA)) return newText.toString();
		if (text.contains(";") && tryGroupedLinks(text, user, newText, SEMICOLON)) return newText.toString();
		if (text.contains(" ") && tryGroupedLinks(text, user, newText, WHITE_SPACE)) return newText.toString();

		return newText.toString();
	}

	private boolean tryGroupedLinks(String text, UserContext user, StringBuilder newText, Pattern pattern) {
		ArticleManager articleManager = user.getArticleManager();
		Matcher matcher = pattern.matcher(text);
		int offSet = 0;
		boolean foundLinks = false;
		while (matcher.find()) {
			String title = matcher.group(1);
			if (articleManager.getArticle(title) != null) {
				foundLinks = true;
				offSet = replaceGroupWithLinkAndUpdateOffset(user, matcher, newText, offSet, title);
			}
		}
		return foundLinks;
	}

	@NotNull
	private static Pattern getGroupedLinksPattern(String separator) {
		return Pattern.compile("(?<=\\A|" + separator + ")(?:\\h*[-*]\\h*)?([^" + separator + "]+)(?=" + separator + "|\\z)");
	}

	private int replaceGroupWithLinkAndUpdateOffset(UserContext user, Matcher matcher, StringBuilder newText, int offSet, String title) {
		String htmlLink = RenderResult.mask(KnowWEUtils.getLinkHTMLToArticle(title), user);
		newText.replace(matcher.start(1) + offSet, matcher.end(1) + offSet, htmlLink);
		offSet += htmlLink.length() - matcher.group(1).length();
		return offSet;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
