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
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.permissions.PermissionFactory;
import org.apache.wiki.render.RenderingManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.denkbares.plugin.test.InitPluginManager;
import de.knowwe.core.Environment;
import de.knowwe.jspwiki.acl.AccessRuleType;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.apache.wiki.providers.git.GitTestWiki.ADMIN;
import static org.apache.wiki.providers.git.GitTestWiki.JANNE;
import static org.apache.wiki.providers.git.GitTestWiki.eventually;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Renders a page with an ACL through the whole chain of a production wiki - JSPWiki, the KnowWE page filter and the
 * {@code JSPWikiConnector} - and checks that editors see the warnings of {@link AccessRuleType}, readers do not, and
 * JSPWiki still enforces the ACL. Lives here for {@link GitTestWiki}.
 */
public class AccessRuleRenderingTest {

	private static final String PAGE = "AclPage";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private GitTestWiki wiki;
	private Environment environment;

	@BeforeClass
	public static void initPlugins() {
		// the classpath of the test run, the module's own classes are added by the plugin manager itself
		String[] classpath = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
				.filter(entry -> !new File(entry).getAbsoluteFile().equals(new File("target/classes").getAbsoluteFile()))
				.toArray(String[]::new);
		InitPluginManager.init(classpath, "^d3web-Plugin.*", "^KnowWE-Plugin.*", "^denkbares-(.+-)?Plugin-.+");
	}

	@Before
	public void setUp() throws Exception {
		File probe = folder.newFolder("git-probe");
		assumeTrue(BareGitConnector.fromPath(probe.getAbsolutePath()).gitInstalledAndReady());
		wiki = GitTestWiki.start(folder.newFolder("wiki"), "/git-engine-knowwe-filters.xml");
		environment = Environment.findIn(wiki.engine.getServletContext());
		assertNotNull("the KnowWE page filter must have booted the environment", environment);
	}

	@After
	public void tearDown() {
		if (environment != null) environment.close();
		if (wiki != null) wiki.close();
	}

	@Test
	public void editorsSeeTheWarningsAndJSPWikiStillEnforcesTheAcl() throws Exception {
		HttpServletRequest admin = wiki.login(ADMIN);
		// "Janne Jalkanen" is Janne's full name, "Redaktoin" nobody at all; the admin group keeps the page editable
		wiki.save(admin, PAGE, "[{ALLOW edit Janne Jalkanen, Redaktoin, Admin}]\n\nSome text.\n");
		assertTrue("the article must be registered", eventually(() -> environment.getArticleManager().getArticle(PAGE) != null));

		// still with KnowWE's HTML masking, which the page filter undoes after JSPWiki rendered the page
		String editorView = render(admin);
		assertTrue(editorView, editorView.contains("warning"));
		assertTrue("the wiki name should be suggested for the full name: " + editorView,
				editorView.contains("JanneJalkanen"));
		// (the JSPWiki test configuration turns "JanneJalkanen" into a CamelCase link, wikis keep camelCaseLinks off)
		assertTrue("the unknown entry should be reported: " + editorView, editorView.contains("Redaktoin"));
		assertFalse("the ACL markup must not show up as text: " + editorView, editorView.contains("[{ALLOW"));

		HttpServletRequest janne = wiki.login(JANNE);
		String readerView = render(janne);
		assertTrue("Janne may still read the page: " + readerView, readerView.contains("Some text."));
		assertFalse("readers do not see the warnings: " + readerView, readerView.contains("Redaktoin"));

		// the markup is still an ACL for JSPWiki: the full name grants Janne nothing, so she may not edit
		AuthorizationManager authorization = wiki.engine.getManager(AuthorizationManager.class);
		Page page = Wiki.contents().page(wiki.engine, PAGE);
		assertFalse(authorization.checkPermission(session(janne), PermissionFactory.getPagePermission(page, "edit")));
		assertTrue(authorization.checkPermission(session(admin), PermissionFactory.getPagePermission(page, "edit")));
	}

	private String render(HttpServletRequest user) {
		Page page = wiki.pages().getPage(PAGE);
		return wiki.engine.getManager(RenderingManager.class).getHTML(wiki.context(user, PAGE), page);
	}

	private Session session(HttpServletRequest user) {
		return Wiki.session().find(wiki.engine, user);
	}
}
