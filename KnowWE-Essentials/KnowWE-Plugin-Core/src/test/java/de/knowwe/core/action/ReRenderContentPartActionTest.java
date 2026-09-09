/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.action;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReRenderContentPartActionTest {

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
