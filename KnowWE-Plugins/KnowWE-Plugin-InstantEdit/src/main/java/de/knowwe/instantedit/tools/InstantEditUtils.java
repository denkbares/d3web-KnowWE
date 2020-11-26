package de.knowwe.instantedit.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;

/**
 * Util methods involving InstantEdit.
 * <p>
 *
 * @author Albrecht Striffler (denkbares GmbH) on 09.09.2014.
 */
public class InstantEditUtils {

	/**
	 * There is a delay between requesting the new title and creating the articles with these titles. To avoid
	 * returning a title that is currently on the way to being created, we block them until we can confirm them as
	 * articles.
	 */
	private static final Set<String> blockedNames = new HashSet<>();

	public static void unblockReservedTitle(String titleToUnblock) {
		synchronized (blockedNames) {
			blockedNames.remove(titleToUnblock);
		}
	}

	public static String createNewEnumeratedTitle(String prefix, ArticleManager articleManager) {
		return createNewEnumeratedTitle(prefix, articleManager, -1);
	}

	public static String createNewEnumeratedTitle(String prefix, ArticleManager articleManager, int digits) {

		int current = getCurrentlyHighestNumberForPrefix(prefix, articleManager);
		current++;

		return createNewEnumeratedTitle(prefix, articleManager, digits, current);
	}

	@NotNull
	public static String createNewEnumeratedTitle(String prefix, ArticleManager articleManager, int digits, int current) {
		// clean up blocked names...
		synchronized (blockedNames) {
			// article was created, no need to block any longer
			blockedNames.removeIf(blockedName -> articleManager.getArticle(blockedName) != null);
		}

		String title = prefix + String.format("%0" + Math.max(digits, 1) + "d", current++);
		synchronized (blockedNames) {
			while (blockedNames.contains(title) || articleManager.getArticle(title) != null) {
				title = prefix + String.format("%0" + digits + "d", current++);
			}
			blockedNames.add(title);
		}
		return title;
	}

	/**
	 * Returns the currently highest number for the given prefix.
	 */
	private static int getCurrentlyHighestNumberForPrefix(String prefix, ArticleManager articleManager) {

		TreeMap<Integer, Article> articles = getAllArticlesWithPrefix(prefix, articleManager);

		int highest = 0;
		if (!articles.isEmpty()) {
			highest = articles.lastKey();
		}
		return highest;
	}

	/**
	 * Returns all articles where the title matches the prefix followed by a number.
	 * <p>
	 * Looks slow (O(n) with n = articles), but is not that bad
	 * Even for around 30k articles this takes less than 10ms
	 * <p>
	 * In the future or for more extreme use cases, we should think about using SuffixTrees or such...
	 */
	public static TreeMap<Integer, Article> getAllArticlesWithPrefix(String prefix, ArticleManager articleManager) {
		Pattern titlePattern = Pattern.compile("^" + Pattern.quote(prefix) + "\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);
		TreeMap<Integer, Article> numbers = new TreeMap<>();
		articleManager.getArticles().parallelStream().forEach(article -> {
			Matcher m = titlePattern.matcher(article.getTitle());
			if (m.find()) {
				try {
					Integer number = Integer.parseInt(m.group(1));
					synchronized (numbers) {
						numbers.put(number, article);
					}
				}
				catch (NumberFormatException e) {
					// do nothing here
				}
			}
		});
		return numbers;
	}
}
