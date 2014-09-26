package de.knowwe.instantedit.tools;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;

/**
 * Util methods involving InstantEdit.
 * <p>
 * @author Albrecht Striffler (denkbares GmbH) on 09.09.2014.
 */
public class InstantEditUtils {

	/**
	 * There is a delay between requesting the new title and creating the articles with these titles. To avoid
	 * returning a title that is currently on the way to being created, we block them until we can confirm them as
	 * articles.
	 */
	private static final Set<String> blockedNames = new HashSet<>();

	public static String createNewEnumeratedTitle(String prefix, ArticleManager articleManager) {
		return createNewEnumeratedTitle(prefix, articleManager, -1);
	}

	public static String createNewEnumeratedTitle(String prefix, ArticleManager articleManager, int digits) {

		// clean up blocked names...
		synchronized (blockedNames) {
			for (Iterator<String> iterator = blockedNames.iterator(); iterator.hasNext(); ) {
				String blockedName = iterator.next();
				if (articleManager.getArticle(blockedName) != null) {
					// article was created, no need to block any longer
					iterator.remove();
				}
			}
		}

		int highest = getCurrentlyHighestNumberForPrefix(prefix, articleManager);
		highest++;
		String title = prefix + fill(highest, digits);
		synchronized (blockedNames) {
			while (blockedNames.contains(title)) {
				int number = highest++;
				title = prefix + fill(number, digits);
			}
			blockedNames.add(title);
		}
		return title;
	}

	private static String fill(int num, int digits) {
		if (digits <= 0) return String.valueOf(num);
		// create variable length array of zeros
		char[] zeros = new char[digits];
		Arrays.fill(zeros, '0');
		// format number as String
		DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

		return df.format(num);
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
