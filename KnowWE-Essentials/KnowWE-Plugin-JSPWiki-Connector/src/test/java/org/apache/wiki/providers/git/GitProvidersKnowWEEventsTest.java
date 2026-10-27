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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.content.PageRenamer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.plugin.test.InitPluginManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.ArticleUpdateEvent;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.GitCommitEvent;
import de.knowwe.jspwiki.KnowWEPlugin;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.apache.wiki.providers.git.GitTestWiki.JANNE;
import static org.apache.wiki.providers.git.GitTestWiki.eventually;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Changes made through the git providers of a real JSPWiki engine reach KnowWE: the {@link KnowWEPlugin} page filter
 * boots the KnowWE environment, and the provider events keep its articles and listeners up to date.
 */
public class GitProvidersKnowWEEventsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private GitTestWiki wiki;
	private Environment environment;
	private final List<Event> events = new CopyOnWriteArrayList<>();
	private final EventListener recorder = new EventListener() {
		@Override
		public Collection<Class<? extends Event>> getEvents() {
			return List.of(ArticleUpdateEvent.class, GitCommitEvent.class, AttachmentStoredEvent.class,
					AttachmentDeletedEvent.class);
		}

		@Override
		public void notify(Event event) {
			events.add(event);
		}
	};

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
		environment.getEventManager().registerListener(recorder);
	}

	@After
	public void tearDown() {
		if (environment != null) {
			environment.getEventManager().unregister(recorder);
			environment.close();
		}
		if (wiki != null) {
			wiki.close();
		}
	}

	@Test
	public void aSaveUpdatesTheArticleAndAnnouncesTheCommit() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "some text");
		String head = wiki.git("rev-parse", "HEAD");

		assertTrue("the article must be registered",
				eventually(() -> article("Topic") != null && article("Topic").getText().contains("some text")));

		// the environment announces the content change itself, the provider's commit event adds the one with the commit
		List<ArticleUpdateEvent> withCommit = all(ArticleUpdateEvent.class).stream()
				.filter(event -> event.getVersion() != null && event.getVersion().getCommitHash() != null)
				.toList();
		assertEquals("expected one update with the commit in " + events, 1, withCommit.size());
		ArticleUpdateEvent update = withCommit.get(0);
		assertEquals("Topic", update.getTitle());
		assertEquals(JANNE, update.getUsername());
		assertEquals(1, update.getVersion().getWikiVersionNumber());
		assertEquals(head, update.getVersion().getCommitHash());

		GitCommitEvent commit = single(GitCommitEvent.class);
		assertEquals(head, commit.commitHash());
		assertEquals(List.of("Topic"), List.copyOf(commit.pages()));
		assertEquals(JANNE, commit.author());
		assertEquals(GitCommitEvent.Origin.LOCAL_SAVE, commit.origin());
		assertEquals(wiki.repository.getCanonicalFile(), new File(commit.repoPath()).getCanonicalFile());
	}

	@Test
	public void theCommitUpdateKeepsThePageNameAsItIs() throws Exception {
		// names that look like the file layout of the git providers, a plus and a dot
		wiki.save(wiki.login(JANNE), "Notes-att C+ v1.txt", "some text");

		List<String> titles = all(ArticleUpdateEvent.class).stream()
				.filter(event -> event.getVersion() != null && event.getVersion().getCommitHash() != null)
				.map(ArticleUpdateEvent::getTitle)
				.toList();
		assertEquals(List.of("Notes-att C+ v1.txt"), titles);
	}

	@Test
	public void aDeleteRemovesTheArticle() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "some text");
		assertTrue(eventually(() -> article("Topic") != null));

		wiki.pages().deletePage("Topic");

		assertTrue("the article must be removed", eventually(() -> article("Topic") == null));
	}

	@Test
	public void aRenameMovesTheArticle() throws Exception {
		wiki.save(wiki.login(JANNE), "Before", "some text");
		assertTrue(eventually(() -> article("Before") != null));

		wiki.engine.getManager(PageRenamer.class)
				.renamePage(wiki.context(wiki.login(JANNE), "Before"), "Before", "After", true);

		assertTrue("the article must be renamed",
				eventually(() -> article("Before") == null && article("After") != null));
		assertTrue(article("After").getText().contains("some text"));
	}

	@Test
	public void aStoredAttachmentIsAnnounced() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "some text");
		storeAttachment("Topic", "data.txt");

		assertTrue("KnowWE must learn about the stored attachment", eventually(() -> events.stream()
				.anyMatch(event -> event instanceof AttachmentStoredEvent stored
								   && "Topic".equals(stored.getParentName())
								   && "data.txt".equals(stored.getFileName()))));
	}

	@Test
	public void anAttachmentCommitIsAnnouncedWithTheAttachmentVersion() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "some text");
		storeAttachment("Topic", "data.txt");
		storeAttachment("Topic", "data.txt", "changed");
		String head = wiki.git("rev-parse", "HEAD");

		ArticleUpdateEvent update = all(ArticleUpdateEvent.class).stream()
				.filter(event -> "Topic/data.txt".equals(event.getTitle()) && event.getVersion() != null
								 && head.equals(event.getVersion().getCommitHash()))
				.findFirst().orElseThrow(() -> new AssertionError("no update for commit " + head + " of the second upload in "
																  + all(ArticleUpdateEvent.class).stream().map(GitProvidersKnowWEEventsTest::describe).toList()));
		assertEquals(2, update.getVersion().getWikiVersionNumber());
	}

	@Test
	public void replacingAnAttachmentWithoutVersioningIsAnnouncedAsAStore() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "some text");
		storeAttachment("Topic", "data.txt");
		assertTrue(eventually(() -> !all(AttachmentStoredEvent.class).isEmpty()));
		events.clear();

		environment.getWikiConnector().storeAttachment("Topic", "data.txt", JANNE,
				new ByteArrayInputStream("replaced".getBytes(StandardCharsets.UTF_8)), false);

		assertTrue(eventually(() -> !all(AttachmentStoredEvent.class).isEmpty()));
		// the stored event is delivered on a thread of its own, give a delete event the same chance
		Thread.sleep(500);
		assertEquals(List.of(), all(AttachmentDeletedEvent.class));
	}

	@Test
	public void aDeletedAttachmentIsAnnounced() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "some text");
		storeAttachment("Topic", "data.txt");

		wiki.attachments().deleteAttachment(wiki.attachments().getAttachmentInfo("Topic/data.txt"));

		assertTrue("KnowWE must learn about the deleted attachment", eventually(() -> events.stream()
				.anyMatch(event -> event instanceof AttachmentDeletedEvent deleted
								   && "Topic".equals(deleted.getParentName())
								   && "data.txt".equals(deleted.getFileName()))));
	}

	private Article article(String title) {
		ArticleManager articleManager = environment.getArticleManager();
		return articleManager.getArticle(title);
	}

	private void storeAttachment(String page, String fileName) throws Exception {
		storeAttachment(page, fileName, "content");
	}

	private void storeAttachment(String page, String fileName, String content) throws Exception {
		Attachment attachment = Wiki.contents().attachment(wiki.engine, page, fileName);
		attachment.setAuthor(JANNE);
		wiki.attachments().storeAttachment(attachment,
				new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	}

	private static String describe(ArticleUpdateEvent event) {
		ArticleUpdateEvent.Version version = event.getVersion();
		return event.getTitle() + (version == null ? "" : " v" + version.getWikiVersionNumber() + " " + version.getCommitHash());
	}

	private <E extends Event> List<E> all(Class<E> type) {
		return events.stream().filter(type::isInstance).map(type::cast).toList();
	}

	private <E extends Event> E single(Class<E> type) {
		List<E> matching = all(type);
		assertEquals("expected exactly one " + type.getSimpleName() + " in " + events, 1, matching.size());
		return matching.get(0);
	}
}
