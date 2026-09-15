/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
	public void retiredOwnerCannotAddWorkToEqualIdSuccessor() throws Exception {
		Article old = wiki.register("Page", "same\n");
		LongOperation retained = operation();
		String retainedId = LongOperationUtils.registerLongOperation(line(old, 0), retained);
		Article current = wiki.register("Page", old.getText());
		assertEquals(line(old, 0).getID(), line(current, 0).getID());
		assertThrows(IllegalStateException.class, () -> LongOperationUtils.registerLongOperation(line(old, 0), operation()));
		assertEquals(1, LongOperationUtils.getLongOperations(line(current, 0)).size());
		assertSame(retained, LongOperationUtils.getLongOperation(line(current, 0), retainedId));
		LongOperation fresh = operation();
		String freshId = LongOperationUtils.registerLongOperation(line(current, 0), fresh);
		assertSame(fresh, LongOperationUtils.getLongOperation(line(current, 0), freshId));
	}

	@Test
	public void lookupDoesNotCreateEntriesAndManagedDraftCannotRegister() throws Exception {
		Article draft = wiki.draft("Draft", "draft\n");
		String id = line(draft, 0).getID();
		assertNull(LongOperationUtils.getRegistrationID(line(draft, 0), operation()));
		assertThrows(IllegalStateException.class, () -> LongOperationUtils.registerLongOperation(line(draft, 0), operation()));
		Field field = LongOperationUtils.class.getDeclaredField("LONG_OPERATIONS");
		field.setAccessible(true);
		assertFalse(((Map<?, ?>) field.get(null)).containsKey(id));
	}

	@Test
	public void retirementDuringOperationIdResolutionIsRejected() throws Exception {
		Article old = wiki.register("Page", "same\n");
		LongOperation operation = new AbstractLongOperation() {
			@Override public void execute(UserActionContext context) { }
			@Override public String getId() {
				// Model retirement while operation-provided metadata is being calculated.
				wiki.manager.registerArticle("Page", old.getText());
				return "late-operation";
			}
		};
		assertThrows(IllegalStateException.class, () -> LongOperationUtils.registerLongOperation(line(old, 0), operation));
		wiki.awaitCompilation();
		assertTrue(LongOperationUtils.getLongOperations(line(wiki.manager.getArticle("Page"), 0)).isEmpty());
	}

	@Test
	public void concurrentRegistrationsRetainAllOperationsAndDeduplicate() throws Exception {
		Article article = wiki.register("Page", "same\n");
		var executor = Executors.newFixedThreadPool(4);
		try {
			var operations = new ArrayList<LongOperation>();
			var registrations = new ArrayList<Future<String>>();
			for (int i = 0; i < 32; i++) {
				LongOperation operation = operation();
				operations.add(operation);
				registrations.add(executor.submit(() -> LongOperationUtils.registerLongOperation(line(article, 0), operation)));
			}
			for (int i = 0; i < registrations.size(); i++) {
				String id = registrations.get(i).get(5, TimeUnit.SECONDS);
				assertSame(operations.get(i), LongOperationUtils.getLongOperation(line(article, 0), id));
				assertEquals(id, LongOperationUtils.registerLongOperation(line(article, 0), operations.get(i)));
			}
			assertEquals(32, LongOperationUtils.getLongOperations(line(article, 0)).size());
		}
		finally {
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	private static LongOperation operation() {
		return new AbstractLongOperation() {
			@Override public void execute(UserActionContext context) { }
		};
	}

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
