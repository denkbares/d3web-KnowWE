/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package org.apache.wiki.providers.autoUpdate;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.providers.GitVersioningFileProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.stubbing.Answer;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.CompilerManager;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorPull;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The auto updater pulls the wiki content from the remote repository while it holds the article manager and the push
 * lock of the provider, which block the saves and compilations of the wiki. Contacting the remote can take long, or
 * hang on a remote that does not answer, so it has to happen before these locks are taken; only applying what was
 * fetched needs them.
 */
public class GitAutoUpdaterTest {

	@Rule public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void theRemoteIsContactedBeforeTheLocksAreTaken() throws Exception {
		File directory = folder.newFolder("wiki");
		try (Git git = Git.init().setDirectory(directory).call()) {
			Files.writeString(directory.toPath().resolve("Page.txt"), "content");
			git.add().addFilepattern(".").call();
			PersonIdent author = new PersonIdent("author", "author@example.com");
			git.commit().setMessage("initial").setAuthor(author).setCommitter(author).call();

			AtomicInteger openFrames = new AtomicInteger();
			ArticleManager articleManager = mock(ArticleManager.class);
			doAnswer(invocation -> {
				openFrames.incrementAndGet();
				return null;
			}).when(articleManager).open();
			doAnswer(invocation -> {
				openFrames.decrementAndGet();
				return null;
			}).when(articleManager).commit();
			when(articleManager.getCompilerManager()).thenReturn(mock(CompilerManager.class));

			AtomicBoolean pushLocked = new AtomicBoolean();
			GitVersioningFileProvider provider = mock(GitVersioningFileProvider.class);
			doAnswer(invocation -> {
				pushLocked.set(true);
				return null;
			}).when(provider).pushLock();
			doAnswer(invocation -> {
				pushLocked.set(false);
				return null;
			}).when(provider).pushUnlock();

			List<String> gitCalls = new ArrayList<>();
			GitConnectorPull pull = mock(GitConnectorPull.class);
			when(pull.allWithRebase()).then(record("pull", gitCalls, openFrames, pushLocked));
			when(pull.allWithRebase(anyString())).then(record("pull", gitCalls, openFrames, pushLocked));
			when(pull.fetchCurrentBranch(anyString())).then(record("fetch", gitCalls, openFrames, pushLocked));
			when(pull.currentBranchOntoFetched(anyString(), any())).then(record("rebase", gitCalls, openFrames, pushLocked));
			GitConnector connector = mock(GitConnector.class);
			when(connector.pull()).thenReturn(pull);

			when(provider.getFilesystemPath()).thenReturn(directory.getAbsolutePath());
			when(provider.getRepository()).thenReturn(git.getRepository());
			when(provider.getGitConnector()).thenReturn(connector);

			new GitAutoUpdater(mock(Engine.class), provider, () -> articleManager).update();

			assertEquals(List.of("fetch without locks", "rebase with article manager and push lock"), gitCalls);
			assertEquals("The article manager must be committed", 0, openFrames.get());
		}
	}

	private static Answer<Boolean> record(String operation, List<String> calls, AtomicInteger openFrames, AtomicBoolean pushLocked) {
		return invocation -> {
			boolean opened = openFrames.get() > 0;
			if (!opened && !pushLocked.get()) {
				calls.add(operation + " without locks");
			}
			else {
				calls.add(operation + (opened ? " with article manager" : "") + (opened && pushLocked.get() ? " and" : "")
						+ (pushLocked.get() ? " push lock" : ""));
			}
			return true;
		};
	}
}
