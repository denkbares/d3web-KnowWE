/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.event;

import de.knowwe.core.event.ArticleEvent;
import de.knowwe.core.kdom.Article;

public class ArticleDeletedEvent extends ArticleEvent {

	public ArticleDeletedEvent(Article article) {
		super(article);
	}
}

