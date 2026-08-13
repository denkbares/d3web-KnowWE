/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core.utils.progress;

import java.util.UUID;

import org.junit.Test;

import com.denkbares.events.EventManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.event.ArticleManagerCommitDoneEvent;
import de.knowwe.event.ArticleRegisteredEvent;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class LongOperationUtilsTest {

	@Test
	public void cleanupWaitsUntilArticleRegistrationCommitIsComplete() {
		DefaultArticleManager articleManager = new DefaultArticleManager("test");
		Article article = Article.createTemporaryArticle(
				"content", "Article-" + UUID.randomUUID(), "test", new RootType());
		Section<?> section = article.getRootSection();
		LongOperation operation = new AbstractLongOperation() {
			@Override
			public void execute(UserActionContext context) {
			}
		};
		String operationId = LongOperationUtils.registerLongOperation(section, operation);

		try {
			// During a batch registration, section IDs can temporarily point to sections that are not live yet. An event
			// for one of the other articles must not clean the operation in this transient state.
			EventManager.getInstance().fireEvent(new ArticleRegisteredEvent(article));
			assertSame(operation, LongOperationUtils.getLongOperation(section, operationId));
		}
		finally {
			EventManager.getInstance().fireEvent(new ArticleManagerCommitDoneEvent(articleManager, true));
			article.destroy(null);
		}
		assertNull(LongOperationUtils.getLongOperation(section, operationId));
	}
}
