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

package org.apache.wiki.providers.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.BooleanSupplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.wiki.HttpMockFactory;
import org.apache.wiki.TestEngine;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthenticationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.AbstractFileProvider;
import org.mockito.Mockito;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitResult;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;

import static org.junit.Assert.assertTrue;

/**
 * A real JSPWiki {@link TestEngine} configured like a production KnowWE wiki, with {@link GitPageProvider} and
 * {@link GitAttachmentProvider} sharing one git repository behind the JSPWiki caching wrappers.
 */
final class GitTestWiki implements AutoCloseable {

	/**
	 * Login name of the test user database's user with a full name and an e-mail address.
	 */
	static final String JANNE = "janne";
	static final String JANNE_FULL_NAME = "Janne Jalkanen";
	static final String JANNE_EMAIL = "janne@ecyrd.com";
	static final String ADMIN = "admin";
	// both users share this password in the user database of the jspwiki test jar
	private static final String PASSWORD = "myP@5sw0rd";

	final TestEngine engine;
	final File repository;

	private GitTestWiki(TestEngine engine, File repository) {
		this.engine = engine;
		this.repository = repository;
	}

	/**
	 * Starts a wiki in the given empty directory, with the page filters of the given classpath resource, or the
	 * jspwiki test filters if it is null.
	 */
	static GitTestWiki start(File root, String filterConfig) throws IOException {
		File repository = new File(root, "wikicontent");
		File work = new File(root, "work");
		assertTrue(repository.mkdirs() && work.mkdirs());
		initRepository(repository);

		Properties properties = TestEngine.getTestProperties();
		// TestEngine would otherwise move the page directory to a fresh one below target
		properties.setProperty("jspwiki.test.disable-clean-props", "true");
		properties.setProperty(AuthenticationManager.PROP_LOGIN_THROTTLING, "false");
		// the provider keys and values of the production wiki configuration
		properties.setProperty(PageManager.PROP_PAGEPROVIDER, GitPageProvider.class.getName());
		properties.setProperty("jspwiki.attachmentProvider", GitAttachmentProvider.class.getName());
		properties.setProperty("jspwiki.usePageCache", "true");
		properties.setProperty(AbstractFileProvider.PROP_PAGEDIR, repository.getAbsolutePath());
		properties.setProperty("jspwiki.basicAttachmentProvider.storageDir", repository.getAbsolutePath());
		properties.setProperty(Engine.PROP_ENCODING, "UTF-8");
		properties.setProperty(Engine.PROP_WORKDIR, work.getAbsolutePath());
		properties.setProperty("jspwiki.xmlUserDatabaseFile", copyResource("/userdatabase.xml", root).getAbsolutePath());
		properties.setProperty("jspwiki.xmlGroupDatabaseFile", copyResource("/groupdatabase.xml", root).getAbsolutePath());
		if (filterConfig != null) {
			properties.setProperty("jspwiki.filterConfig", filterConfig);
		}
		return new GitTestWiki(TestEngine.build(properties), repository);
	}

	/**
	 * Pins the identity and line endings of the repository instead of inheriting the machine's git configuration.
	 */
	private static void initRepository(File repository) {
		git(repository, "init");
		git(repository, "config", "user.name", "committer");
		git(repository, "config", "user.email", "committer@example.com");
		git(repository, "config", "core.autocrlf", "false");
	}

	private static File copyResource(String resource, File directory) throws IOException {
		File target = new File(directory, resource.substring(1));
		try (InputStream in = GitTestWiki.class.getResourceAsStream(resource)) {
			assertTrue("missing test resource " + resource, in != null);
			Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return target;
	}

	PageManager pages() {
		return engine.getManager(PageManager.class);
	}

	AttachmentManager attachments() {
		return engine.getManager(AttachmentManager.class);
	}

	/**
	 * A request of a user logged in with the user database, every request built from it shares its wiki session.
	 */
	HttpServletRequest login(String loginName) throws WikiException {
		HttpServletRequest request = withoutParameters(HttpMockFactory.createHttpRequest());
		Session session = Wiki.session().find(engine, request);
		assertTrue("login of " + loginName + " failed",
				engine.getManager(AuthenticationManager.class).login(session, request, loginName, PASSWORD));
		return request;
	}

	/**
	 * A fresh request in the wiki session of the given request, as a browser sends it with its session cookie.
	 */
	HttpServletRequest request(HttpServletRequest sessionOf, String servletPath) {
		HttpServletRequest request = withoutParameters(HttpMockFactory.createHttpRequest(servletPath));
		HttpSession session = sessionOf.getSession();
		Mockito.doReturn(session).when(request).getSession();
		Mockito.doReturn(session).when(request).getSession(Mockito.anyBoolean());
		return request;
	}

	/**
	 * Lets the mocked request report no parameters instead of null, as a container does for a request without any.
	 */
	private static HttpServletRequest withoutParameters(HttpServletRequest request) {
		Mockito.doAnswer(call -> Collections.emptyEnumeration()).when(request).getParameterNames();
		Mockito.doReturn(Map.of()).when(request).getParameterMap();
		return request;
	}

	Context context(HttpServletRequest request, String pageName) {
		Page page = Wiki.contents().page(engine, pageName);
		return Wiki.context().create(engine, request, page);
	}

	/**
	 * Saves the page through the page manager, the way the editor does, with the author the wiki derives from the
	 * logged-in user.
	 */
	void save(HttpServletRequest user, String pageName, String text) throws WikiException {
		pages().saveText(context(user, pageName), text);
	}

	String git(String... args) {
		return git(repository, args);
	}

	private static String git(File directory, String... args) {
		String[] command = new String[args.length + 1];
		command[0] = "git";
		System.arraycopy(args, 0, command, 1, args.length);
		BareGitResult result = RawGitExecutor.executeGitCommand(command, directory.getAbsolutePath());
		assertTrue(String.join(" ", command) + ": " + result.output(), result.successful());
		return result.stdout().trim();
	}

	/**
	 * Author name and e-mail of the latest commit touching the path, as {@code name <email>}.
	 */
	String lastAuthor(String path) {
		return git("log", "-1", "--format=%an <%ae>", "--", path);
	}

	int commitCount(String path) {
		String log = git("log", "--format=%H", "--", path);
		return log.isEmpty() ? 0 : log.split("\n").length;
	}

	/**
	 * Waits for a condition that wiki listeners establish on threads of their own.
	 */
	static boolean eventually(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 10_000;
		while (System.currentTimeMillis() < deadline) {
			if (condition.getAsBoolean()) {
				return true;
			}
			Thread.sleep(50);
		}
		return condition.getAsBoolean();
	}

	@Override
	public void close() {
		engine.shutdown();
	}
}
