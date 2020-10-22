package de.knowwe.ontology.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.sparql.utils.RenderOptions.RenderMode;

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

		// check for exact matches e.g, for plain Strings
		ArticleManager articleManager = user.getArticleManager();
		if (articleManager.getArticle(text) != null) {
			if (mode == RenderMode.HTML) {
				return KnowWEUtils.getLinkHTMLToArticle(text);
			}
			if (mode == RenderMode.PlainText) {
				return text;
			}
		}

		// check for exact URI matches
		String lns = core.getLocalNamespace();
		if (text.startsWith(lns)) {
			String title = decodeSilent(text.substring(lns.length()));
			if (user.getArticleManager().getArticle(title) != null) {
				if (mode == RenderMode.HTML) {
					return KnowWEUtils.getLinkHTMLToArticle(title);
				}
				if (mode == RenderMode.PlainText) {
					return title;
				}
			}
		}

		// try some heuristics to find article links in grouped literals with commonly used separators
		StringBuilder newText = new StringBuilder(text);
		if (text.contains("\n") && tryGroupedLinks(text, user, lns, mode, newText, LINE_BREAK)) return newText.toString();
		if (text.contains(",") && tryGroupedLinks(text, user, lns, mode, newText, COMMA)) return newText.toString();
		if (text.contains(";") && tryGroupedLinks(text, user, lns, mode, newText, SEMICOLON)) return newText.toString();
		if (text.contains(" ") && tryGroupedLinks(text, user, lns, mode, newText, WHITE_SPACE)) return newText.toString();

		return newText.toString();
	}

	private boolean tryGroupedLinks(String text, UserContext user, String lns, RenderMode mode, StringBuilder newText, Pattern pattern) {
		ArticleManager articleManager = user.getArticleManager();
		Matcher matcher = pattern.matcher(text);
		int offSet = 0;
		boolean foundLinks = false;
		while (matcher.find()) {
			String title = matcher.group(1);
			String decodeTitle = decodeSilent(title);
			if (articleManager.getArticle(title) == null && decodeTitle.startsWith(lns)) {
				// Check if it is a full url.
				// We are only interested in statements from the local name space.
				// Other name spaces or no name space (simple string) probably is not
				// representing an article in the wiki.
				title = decodeTitle.substring(lns.length());
			}
			if (articleManager.getArticle(title) != null) {
				foundLinks = true;
				if (mode == RenderMode.HTML) {
					offSet = replaceGroupWithLinkAndUpdateOffset(user, matcher, newText, offSet, title);
				}
				if (mode == RenderMode.PlainText) {
					offSet = replaceGroupWithPlainTextAndUpdateOffset(matcher, newText, offSet, title);
				}
			}
		}
		return foundLinks;
	}

	private String decodeSilent(String text) {
		if (text == null) return null;
		try {
			return URLDecoder.decode(text, String.valueOf(Strings.Encoding.UTF8));
		}
		catch (UnsupportedEncodingException | IllegalArgumentException e) {
			return text;
		}
	}

	@NotNull
	private static Pattern getGroupedLinksPattern(String separator) {
		return Pattern.compile("(?<=\\A|" + separator + ")(?:\\h*[-*]\\h*|\\h*)?([^" + separator + "]+)(?=" + separator + "|\\z)");
	}

	private int replaceGroupWithLinkAndUpdateOffset(UserContext user, Matcher matcher, StringBuilder newText, int offSet, String title) {
		String htmlLink = RenderResult.mask(KnowWEUtils.getLinkHTMLToArticle(title), user);
		newText.replace(matcher.start(1) + offSet, matcher.end(1) + offSet, htmlLink);
		offSet += htmlLink.length() - matcher.group(1).length();
		return offSet;
	}

	private int replaceGroupWithPlainTextAndUpdateOffset(Matcher matcher, StringBuilder newText, int offSet, String title) {
		newText.replace(matcher.start(1) + offSet, matcher.end(1) + offSet, title);
		offSet += title.length() - matcher.group(1).length();
		return offSet;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
