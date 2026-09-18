/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import de.knowwe.core.wikiConnector.WikiConnector;
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

	private Environment previousEnvironment;
	private boolean previousInitialized;
	private StringWriter written;
	private HttpServletResponse response;

	@Before
	public void setUp() throws Exception {
		previousEnvironment = Environment.isInitialized() ? Environment.getInstance() : null;
		previousInitialized = Environment.isInitialized();

		installEnvironment(engineBackedConnector());

		written = new StringWriter();
		response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(written));
	}

	@After
	public void tearDown() throws Exception {
		setStaticField("instance", previousEnvironment);
		setStaticField("initialized", previousInitialized);
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
		parameters.put(Attributes.WEB, Environment.DEFAULT_WEB);
		parameters.put(Attributes.USER, "Test User");
		parameters.put("page", pageName);
		return new ActionContext("AnnotatePageAction", "", parameters, mock(HttpServletRequest.class), response,
				null, mock(AuthenticationManager.class));
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

	/**
	 * Installs an environment holding the given connector without running the full initialisation, which would pull
	 * in plugins and compilers this test has no use for.
	 */
	private static void installEnvironment(WikiConnector connector) throws Exception {
		Constructor<Environment> constructor = Environment.class.getDeclaredConstructor(WikiConnector.class);
		constructor.setAccessible(true);
		setStaticField("instance", constructor.newInstance(connector));
		setStaticField("initialized", true);
	}

	private static void setStaticField(String name, Object value) throws Exception {
		Field field = Environment.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(null, value);
	}
}
