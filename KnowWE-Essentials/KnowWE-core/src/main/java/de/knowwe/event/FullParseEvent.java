/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.event;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	private final Collection<Article> articles;
	private final String user;

	public FullParseEvent(@NotNull Collection<Article> articles, @Nullable String user) {
		this.articles = articles;
		this.user = user == null ? "SYSTEM" : user;
	}

	public FullParseEvent(Article article, String author) {
		this(List.of(article), author);
	}

	/**
	 * Returns the article the full parse was performed on
	 */
	public Collection<Article> getArticles() {
		return articles;
	}

	public String getUserName() {
		return user;
	}
}
