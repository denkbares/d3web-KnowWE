package de.knowwe.core.action;

/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.RenderResultKeyValueStore;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * ReRenderContentPartAction. Renders a given section again. Often used in combination with AJAX
 * request, to refresh a certain section of an article due to user interaction.
 *
 * @author smark
 */
public class ReRenderContentPartAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReRenderContentPartAction.class);

	private static final AtomicLong THREAD_COUNTER = new AtomicLong();
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "Rerender-Thread-" + THREAD_COUNTER.incrementAndGet());
		thread.setDaemon(true);
		return thread;
	});

	private static final Map<String, Future<String>> RENDER_FUTURES = new ConcurrentHashMap<>();

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId == null) sectionId = context.getParameter("KdomNodeId"); // compatibility
		execute(context, Sections.get(sectionId));
	}

	private static void execute(UserActionContext context, Section<?> section) throws IOException {
		if (section == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The referenced section was not found. " +
					"Maybe the page content is outdated. Please reload.");
		}
		else if (!KnowWEUtils.canView(section, context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to view this content.");
		}
		else if (handleStatusChanges(context)) {
			context.sendError(HttpServletResponse.SC_NOT_MODIFIED,
					"No change on the server, no need to update.");
		}
		else {
			String renderResult = renderAndCancelOngoingRenders(context, section);
			if (renderResult != null && context.getWriter() != null) {
				context.setContentType(JSON);
				JSONObject response = new JSONObject();
				int counter = -1;
				String counterParam = context.getParameter("counter");
				if (counterParam != null) {
					counter = Integer.parseInt(counterParam);
				}
				response.put("counter", counter);
				try {
					response.put("html", renderResult);
					response.put("status", KnowWEUtils.getOverallStatus(context));
					response.write(context.getWriter());
				}
				catch (JSONException e) {
					throw new IOException(e);
				}
			}
		}
	}

	/**
	 * Returns true if a status from the client is given and it is the same as the current server status.
	 */
	private static boolean handleStatusChanges(UserActionContext context) {
		String status = context.getParameter("status");
		//noinspection SimplifiableIfStatement
		if (status == null) {
			// no status was given, we disregard status
			return false;
		}
		else {
			// we have a status from the client and compare it with the status from the server
			return status.equals(KnowWEUtils.getOverallStatus(context));
		}
	}

	/**
	 * This method does the rendering while also making sure to not clog up tomcat render threads, if the same rerender
	 * request is received multiple times from the same user, faster than they can be processed. This can for example
	 * happen, if the user tries to rerender a query that takes very long to execute on the database (e.g. be
	 * refreshing the page or filtering/pagination). Every request will wait for the same query execution to finish and
	 * all request thread will be occupied until the execution actually finishes. This method makes sure, only one
	 * request thread waits per user and section to rerender.
	 */
	private static String renderAndCancelOngoingRenders(UserActionContext context, Section<?> section) throws IOException {
		String key = generateKey(context, section);
		UserActionContext contextCopy = new AsyncActionContext(context);
		try {
			Future<String> renderFuture = EXECUTOR.submit(() -> render(contextCopy, section));
			Future<String> previous = RENDER_FUTURES.put(key, renderFuture);
			if (previous != null) {
				// previous render thread will move to CancellationException catch block and finishes
				previous.cancel(false);
			}
			return renderFuture.get();
		}
		catch (CancellationException ignore) {
			// this is expected to happen if repeated rerender requests for the same use and section are detected...
			// if the previous rerender is still ongoing, it can be ignored, since the newer response will be shown
		}
		catch (InterruptedException e) {
			LOGGER.warn("Rerender thread interrupted");
			return null;
		}
		catch (ExecutionException e) {
			LOGGER.error("Exception while rendering", e);
			failUnexpected(context, "Exception while rerendering: " + e.getMessage());
		}
		finally {
			RENDER_FUTURES.remove(key);
		}
		return null;
	}

	@NotNull
	private static String generateKey(UserActionContext context, Section<?> section) {
		return context.getUserName() + "_" + section.getID();
	}

	private static String render(UserActionContext context, Section<?> section) {
		RenderResult result = new RenderResult(context);
		section.get().getRenderer().render(section, context, result);
		String rawResult = Environment.getInstance().getWikiConnector()
				.renderWikiSyntax(result.toStringRaw());
		String unmasked = result.toString();
		// cleanup jsp wiki render artifact due to html masking...
		if (rawResult.startsWith("<p>") && rawResult.endsWith("</p>") && !unmasked.startsWith("<p>") && !unmasked.endsWith("</p>")) {
			rawResult = rawResult.substring(3, rawResult.length() - 4);
		}
		return RenderResult.unmask(rawResult, result);
	}

	private static class AsyncActionContext extends ActionContext {

		private final Cookie[] cookies;
		private final Locale[] locales;
		private final ReadOnlyHttpSession session;
		private final Map<String, Object> attributes = Collections.synchronizedMap(new HashMap<>());

		public AsyncActionContext(UserActionContext context) {
			super(context.getActionName(), context.getPath(), context.getParameters(), null, null, context.getServletContext(), context.getManager());
			this.cookies = context.getRequest().getCookies();
			this.locales = context.getBrowserLocales();
			context.getRequest().getAttributeNames().asIterator().forEachRemaining(name -> {
				this.attributes.put(name, context.getRequest().getParameter(name));
			});
			this.session = new ReadOnlyHttpSession(context.getSession());
		}

		@Override
		public Cookie[] getCookies() {
			return cookies == null ? new Cookie[0] : cookies;
		}

		@Override
		public Locale[] getBrowserLocales() {
			return locales;
		}

		@Override
		public HttpSession getSession() {
			return session;
		}

		@Override
		public RenderResultKeyValueStore getRenderResultKeyValueStore() {
			return new RenderResultKeyValueStore() {

				@Override
				public <T> T getAttribute(String storeKey) {
					//noinspection unchecked
					return (T) attributes.get(storeKey);
				}

				@Override
				public void setAttribute(String storeKey, Object maskKey) {
					attributes.put(storeKey, maskKey);
				}
			};
		}
	}

	private static class ReadOnlyHttpSession implements HttpSession {
		private final Map<String, Object> attributes = new ConcurrentHashMap<>();
		private final String id;

		public ReadOnlyHttpSession(HttpSession session) {
			Iterator<String> iterator = session.getAttributeNames().asIterator();
			while (iterator.hasNext()) {
				String attributeName = iterator.next();
				attributes.put(attributeName, session.getAttribute(attributeName));
			}
			this.id = session.getId();
		}

		@Override
		public Object getAttribute(String name) {
			return this.attributes.get(name);
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return Collections.enumeration(attributes.keySet());
		}

		@Override
		public long getCreationTime() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public long getLastAccessedTime() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getMaxInactiveInterval() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ServletContext getServletContext() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("deprecation")
		@Override
		public HttpSessionContext getSessionContext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getValue(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String[] getValueNames() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void invalidate() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isNew() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putValue(String name, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeAttribute(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeValue(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
			LOGGER.warn("Setting attribute {} in a read only session, the value will only be present during the current rerender call.", name);
		}

		@Override
		public void setMaxInactiveInterval(int interval) {
			throw new UnsupportedOperationException();
		}
	}
}
