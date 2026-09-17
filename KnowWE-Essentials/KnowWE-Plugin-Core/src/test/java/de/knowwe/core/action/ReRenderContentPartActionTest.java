/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.action;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReRenderContentPartActionTest {

	/**
	 * Orders three same-address requests without sleeps: B cancels A, A finishes its cleanup, then C must still
	 * find and cancel B. The controlled render futures exercise the action's actual registration/wait/cleanup path
	 * independently of wiki rendering. The address can also be reused by an unchanged article recompile.
	 */
	@Test
	public void cancelledRequestDoesNotRemoveItsSuccessorsRegistration() throws Exception {
		String key = "rerender-test-" + UUID.randomUUID();
		var executor = Executors.newFixedThreadPool(3);
		RenderFuture first = new RenderFuture();
		RenderFuture second = new RenderFuture();
		RenderFuture third = new RenderFuture();
		try {
			var firstRequest = executor.submit(() -> ReRenderContentPartAction.awaitRenderAndCancelPrevious(null, key, first));
			assertTrue(first.awaiting.get(5, TimeUnit.SECONDS));
			var secondRequest = executor.submit(() -> ReRenderContentPartAction.awaitRenderAndCancelPrevious(null, key, second));
			assertTrue(second.awaiting.get(5, TimeUnit.SECONDS));
			assertNull(firstRequest.get(5, TimeUnit.SECONDS)); // includes A's finally block
			assertTrue(first.isCancelled());
			var thirdRequest = executor.submit(() -> ReRenderContentPartAction.awaitRenderAndCancelPrevious(null, key, third));
			assertTrue(third.awaiting.get(5, TimeUnit.SECONDS));
			assertTrue("A's cleanup must leave B registered so C can cancel it", second.isCancelled());
			assertNull(secondRequest.get(5, TimeUnit.SECONDS));
			third.complete("rendered");
			assertEquals("rendered", thirdRequest.get(5, TimeUnit.SECONDS));
			assertEquals("next", ReRenderContentPartAction.awaitRenderAndCancelPrevious(
					null, key, CompletableFuture.completedFuture("next")));
			assertEquals("Completed request must remove its own registration", 0, third.cancellations.get());
		}
		finally {
			first.cancel(false);
			second.cancel(false);
			third.cancel(false);
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	private static final class RenderFuture extends CompletableFuture<String> {
		final CompletableFuture<Boolean> awaiting = new CompletableFuture<>();
		final AtomicInteger cancellations = new AtomicInteger();

		@Override public String get() throws InterruptedException, ExecutionException {
			awaiting.complete(true);
			return super.get();
		}

		@Override public boolean cancel(boolean mayInterruptIfRunning) {
			cancellations.incrementAndGet();
			return super.cancel(mayInterruptIfRunning);
		}
	}

	/** The shared browser error handler distinguishes an outdated section (409) from a deleted page (404). */
	@Test
	public void obsoleteSectionIdRequestsReloadThroughConflictResponse() throws Exception {
		AtomicInteger status = new AtomicInteger();
		String obsoleteId = UUID.randomUUID().toString();
		UserActionContext context = (UserActionContext) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[]{UserActionContext.class}, (proxy, method, args) -> {
					if (method.getName().equals("getParameter")) return obsoleteId;
					if (method.getName().equals("sendError")) {
						status.set((Integer) args[0]);
						return null;
					}
					throw new AssertionError("Unexpected action access: " + method.getName());
				});
		new ReRenderContentPartAction().execute(context);
		assertEquals(409, status.get());
	}
}
