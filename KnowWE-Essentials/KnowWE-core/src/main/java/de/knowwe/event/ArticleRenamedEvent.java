/*
 * Copyright (C) 2023 denkbares GmbH. All rights reserved.
 */

package de.knowwe.event;

import de.knowwe.core.event.ArticleEvent;
import de.knowwe.core.kdom.Article;

public class ArticleRenamedEvent extends ArticleEvent {

    protected Article oldArticle;

    public ArticleRenamedEvent(Article oldArticle, Article newArticle) {
        super(newArticle);
        this.oldArticle = oldArticle;
    }

    public Article getOldArticle() {
        return oldArticle;
    }

}
