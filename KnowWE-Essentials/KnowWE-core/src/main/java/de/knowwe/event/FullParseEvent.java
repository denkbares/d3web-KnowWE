/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.event;

import com.denkbares.events.Event;
import de.knowwe.core.kdom.Article;

/**
 * Is fired when a full parse is performed on an article. It is fired after the new article was registered in the
 * article manager, but before the compilation frame is committed.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 15.05.14
 */
public class FullParseEvent implements Event {
	private final Article article;

	public FullParseEvent(Article article) {
		this.article = article;
	}

	/**
	 * Returns the article the full parse was performed on
	 */
	public Article getArticle() {
		return article;
	}
}
