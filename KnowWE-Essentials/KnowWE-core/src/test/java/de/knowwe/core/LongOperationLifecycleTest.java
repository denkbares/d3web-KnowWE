/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import org.junit.Rule;
import org.junit.Test;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.progress.AbstractLongOperation;
import de.knowwe.core.utils.progress.LongOperation;
import de.knowwe.core.utils.progress.LongOperationUtils;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.*;

/** Progress addresses survive unchanged recompiles; edits must not expose the previous version's operations. */
public class LongOperationLifecycleTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void progressSurvivesRecompileButIsRemovedOnContentChange() throws Exception {
		Article original = wiki.register("Page", "same\n");
		LongOperation operation = new AbstractLongOperation() {
			@Override public void execute(UserActionContext context) { }
		};
		String operationId = LongOperationUtils.registerLongOperation(line(original, 0), operation);
		Article recompiled = wiki.register("Page", original.getText());
		assertSame(operation, LongOperationUtils.getLongOperation(line(recompiled, 0), operationId));
		Article edited = wiki.register("Page", "changed\n");
		assertNull(LongOperationUtils.getLongOperation(line(edited, 0), operationId));
		assertNull(LongOperationUtils.getLongOperation(line(original, 0), operationId));
	}
}
