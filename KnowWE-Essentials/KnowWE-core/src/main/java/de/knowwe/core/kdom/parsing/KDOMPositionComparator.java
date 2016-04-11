package de.knowwe.core.kdom.parsing;

import java.util.Comparator;
import java.util.Iterator;

import de.knowwe.core.kdom.ArticleComparator;

/**
 * Compares two sections by their position in the kdom. If the sections are from
 * two articles named differently, the order of the articles is preserved, as
 * defined by {@link ArticleComparator}.
 * 
 * @see ArticleComparator
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 17.08.2013
 */
public class KDOMPositionComparator implements Comparator<Section<?>> {

	private static final KDOMPositionComparator instance = new KDOMPositionComparator();

	@Override
	public int compare(Section<?> o1, Section<?> o2) {
		// check for null, which is the lowest element
		if (o1 == o2) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return 1;

		// check for articles if they are named identically
		int articleCompare = ArticleComparator.getInstance().compare(
				o1.getArticle(), o2.getArticle());
		if (articleCompare != 0) return articleCompare;

		// check position in kdom
		Iterator<Integer> i1 = o1.getPositionInKDOM().iterator();
		Iterator<Integer> i2 = o2.getPositionInKDOM().iterator();
		while (i1.hasNext() && i2.hasNext()) {
			int v1 = i1.next();
			int v2 = i2.next();
			if (v1 != v2) return v1 - v2;
		}
		if (i1.hasNext()) return 1;
		if (i2.hasNext()) return -1;
		return 0;
	}

	public static KDOMPositionComparator getInstance() {
		return instance;
	}

}
