/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.pages.PageManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionContext;
import de.knowwe.core.user.AuthenticationManager;
import de.knowwe.jspwiki.JSPWikiConnector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that the action answers a request built the way {@code JSPActionServlet} builds one.
 *
 * The servlet hands every action a plain {@link ActionContext}, which is unrelated to the wiki user context that
 * only the JSP rendering path produces. An engine lookup that accepts nothing else therefore fails for every
 * request that can actually reach this action.
 */
public class AnnotatePageActionTest {

	private static final String PAGE = "Main";

	private Environment environment;
	private StringWriter written;
	private HttpServletResponse response;

	@Before
	public void setUp() throws Exception {
		// no plugins here: an environment without them is enough for the action's engine lookup
		environment = Environment.create(engineBackedConnector());

		written = new StringWriter();
		response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(written));
	}

	@After
	public void tearDown() {
		if (environment != null) environment.close();
	}

	@Test
	public void servesARequestCarryingNoWikiContext() throws Exception {
		AnnotatePageAction action = new AnnotatePageAction();

		action.execute(actionContextFor(PAGE));

		String body = written.toString();
		// the annotation itself, rather than the error the engine lookup used to produce
		assertTrue("expected annotation markup but got: " + body, body.contains("<knowwe-page-annotate"));
		assertFalse("expected no error response but got: " + body, body.contains("Wiki engine not available"));
		verify(response, never()).setStatus(anyInt());
	}

	/**
	 * A context of exactly the type {@code JSPActionServlet} builds, which is the whole point of the test.
	 */
	private ActionContext actionContextFor(String pageName) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(Attributes.USER, "Test User");
		parameters.put("page", pageName);
		Environment environment = this.environment;
		// a context of exactly that type, but knowing its environment: there is no servlet context in a test
		return new ActionContext("AnnotatePageAction", "", parameters, mock(HttpServletRequest.class), response,
				null, mock(AuthenticationManager.class)) {
			@Override
			public Environment findEnvironment() {
				return environment;
			}
		};
	}

	/**
	 * A wiki connector whose engine knows one page with two versions, enough for the annotation to be rendered.
	 */
	private static JSPWikiConnector engineBackedConnector() {
		Page first = pageMock(1);
		Page second = pageMock(2);

		Engine engine = mock(Engine.class);
		PageManager pageManager = mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);

		when(pageManager.getPage(PAGE)).thenReturn(second);
		// doReturn, because getVersionHistory is generic and the plain form cannot express its element type here
		doReturn(List.of(first, second)).when(pageManager).getVersionHistory(PAGE);
		when(pageManager.getPureText(PAGE, 1)).thenReturn("alpha\n");
		when(pageManager.getPureText(PAGE, 2)).thenReturn("alpha\nbeta\n");
		// the renderer reads the text it annotates separately, at the latest version
		when(pageManager.getPureText(PAGE, PageProvider.LATEST_VERSION)).thenReturn("alpha\nbeta\n");

		JSPWikiConnector connector = mock(JSPWikiConnector.class);
		when(connector.getEngine()).thenReturn(engine);
		return connector;
	}

	private static Page pageMock(int version) {
		Page page = mock(Page.class);
		when(page.getName()).thenReturn(PAGE);
		when(page.getVersion()).thenReturn(version);
		when(page.getAuthor()).thenReturn("alice");
		when(page.getLastModified()).thenReturn(new Date(1_700_000_000_000L));
		when(page.getAttribute(Page.CHANGENOTE)).thenReturn("change " + version);
		return page;
	}

}
