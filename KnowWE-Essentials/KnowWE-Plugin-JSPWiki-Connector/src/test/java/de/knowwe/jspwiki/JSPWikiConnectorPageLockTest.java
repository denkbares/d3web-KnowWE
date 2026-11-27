/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.jspwiki;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.wiki.TestEngine;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.authorize.XMLGroupDatabase;
import org.apache.wiki.auth.user.XMLUserDatabase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.RootType;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The page locks the connector takes with the real page manager of a JSPWiki engine: storing and deleting lock the page
 * for their duration, also if they fail, and unlocking a page releases only the lock of that page.
 */
public class JSPWikiConnectorPageLockTest {

	private static TestEngine engine;
	/** a connector whose attachment operations all fail, like on a full or unreachable disk */
	private static JSPWikiConnector failing;
	/** a connector whose attachment operations all succeed, without storing anything */
	private static JSPWikiConnector succeeding;
	private static Environment environment;

	@BeforeClass
	public static void setUp() throws Exception {
		Path dir = Files.createDirectories(Path.of("target/jspWikiConnectorPageLockTest"));
		Properties properties = TestEngine.getTestProperties();
		properties.setProperty(XMLUserDatabase.PROP_USERDATABASE, copyResource("/userdatabase.xml", dir).toString());
		properties.setProperty(XMLGroupDatabase.PROP_DATABASE, copyResource("/groupdatabase.xml", dir).toString());
		engine = new TestEngine(properties);

		AttachmentManager failingAttachments = mock(AttachmentManager.class);
		doThrow(new ProviderException("disk full")).when(failingAttachments)
				.storeAttachment(any(Attachment.class), any(InputStream.class));
		when(failingAttachments.getAttachmentInfo(anyString())).thenThrow(new ProviderException("disk gone"));
		when(failingAttachments.listAttachments(any(Page.class))).thenThrow(new ProviderException("disk gone"));
		failing = withAttachmentManager(failingAttachments);
		// deleting an article first removes it from the article manager of the environment
		environment = Environment.create(failing, new RootType());

		AttachmentManager succeedingAttachments = mock(AttachmentManager.class);
		when(succeedingAttachments.listAttachments(any(Page.class))).thenReturn(List.of());
		succeeding = withAttachmentManager(succeedingAttachments);
	}

	@AfterClass
	public static void tearDown() {
		if (environment != null) environment.close();
	}

	private static JSPWikiConnector withAttachmentManager(AttachmentManager attachmentManager) {
		return new JSPWikiConnector(engine) {
			@Override
			public AttachmentManager getAttachmentManager() {
				return attachmentManager;
			}
		};
	}

	private static Path copyResource(String resource, Path dir) throws IOException {
		Path target = dir.resolve(resource.substring(1)).toAbsolutePath();
		try (InputStream in = JSPWikiConnectorPageLockTest.class.getResourceAsStream(resource)) {
			Files.copy(Objects.requireNonNull(in, "missing classpath resource " + resource), target,
					StandardCopyOption.REPLACE_EXISTING);
		}
		// jspwiki 3 refuses to start if a database no longer matches the integrity hash it wrote next to it
		Files.deleteIfExists(target.resolveSibling(target.getFileName() + ".check"));
		return target;
	}

	private static InputStream content() {
		return new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void failingToStoreAnAttachmentReleasesThePage() {
		assertThrows(IOException.class, () -> failing.storeAttachment("StoreFails", "file.txt", "alice", content()));
		assertFalse("The page must not stay locked", failing.isArticleLocked("StoreFails"));
	}

	@Test
	public void failingToDeleteAnAttachmentReleasesThePage() {
		assertThrows(IOException.class, () -> failing.deleteAttachment("DeleteAttachmentFails", "file.txt", "alice"));
		assertFalse("The page must not stay locked", failing.isArticleLocked("DeleteAttachmentFails"));
	}

	@Test
	public void failingToDeleteAnArticleReleasesThePage() {
		assertThrows(IOException.class, () -> failing.deleteArticle("DeleteArticleFails", "alice"));
		assertFalse("The page must not stay locked", failing.isArticleLocked("DeleteArticleFails"));
	}

	@Test
	public void storingAnAttachmentReleasesThePage() throws Exception {
		succeeding.storeAttachment("StoreSucceeds", "file.txt", "alice", content());
		assertFalse(succeeding.isArticleLocked("StoreSucceeds"));
	}

	@Test
	public void aLockTakenBeforeIsKept() {
		assertTrue(failing.lockArticle("LockedBefore", "alice"));
		try {
			assertThrows(IOException.class, () -> failing.storeAttachment("LockedBefore", "file.txt", "alice", content()));
			assertTrue("The lock taken before the store is not the store's to release",
					failing.isArticleLockedCurrentUser("LockedBefore", "alice"));
		}
		finally {
			failing.unlockArticle("LockedBefore", "alice");
		}
	}

	/**
	 * A user may edit several pages at once, e.g. in several tabs. Finishing the work on one of them must not release the
	 * locks on the others.
	 */
	@Test
	public void unlockingAPageKeepsTheLocksOfTheUserOnOtherPages() {
		assertTrue(succeeding.lockArticle("EditedFirst", "alice"));
		assertTrue(succeeding.lockArticle("EditedSecond", "alice"));
		try {
			succeeding.unlockArticle("EditedFirst", "alice");

			assertFalse(succeeding.isArticleLocked("EditedFirst"));
			assertTrue("The lock on the other page must stay",
					succeeding.isArticleLockedCurrentUser("EditedSecond", "alice"));
		}
		finally {
			succeeding.unlockArticle("EditedFirst", null);
			succeeding.unlockArticle("EditedSecond", null);
		}
	}

	@Test
	public void unlockingAPageKeepsTheLockOfAnotherUser() {
		assertTrue(succeeding.lockArticle("EditedByBob", "bob"));
		try {
			succeeding.unlockArticle("EditedByBob", "alice");
			assertTrue(succeeding.isArticleLockedCurrentUser("EditedByBob", "bob"));

			succeeding.unlockArticle("EditedByBob", null);
			assertFalse("Without user, the lock of the page is released whoever holds it",
					succeeding.isArticleLocked("EditedByBob"));
		}
		finally {
			succeeding.unlockArticle("EditedByBob", null);
		}
	}
}
