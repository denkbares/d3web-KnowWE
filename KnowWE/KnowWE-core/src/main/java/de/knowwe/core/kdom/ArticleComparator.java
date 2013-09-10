package de.knowwe.core.kdom;

import java.util.Comparator;

/**
 * Class to compare {@link Article}s by their name, e.g. for sorting. The
 * compare is case insensitive.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 17.08.2013
 */
public class ArticleComparator implements Comparator<Article> {

	private static final ArticleComparator instance = new ArticleComparator();

	@Override
	public int compare(Article o1, Article o2) {
		if (o1 == o2) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return 1;
		return o1.getTitle().compareToIgnoreCase(o2.getTitle());
	}

	public static ArticleComparator getInstance() {
		return instance;
	}

}
