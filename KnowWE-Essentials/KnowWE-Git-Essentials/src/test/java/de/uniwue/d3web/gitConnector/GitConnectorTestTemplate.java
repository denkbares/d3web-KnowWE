/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.runners.Parameterized;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;
import de.uniwue.d3web.gitConnector.impl.jgit.JGitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;

import static de.uniwue.d3web.gitConnector.GitTestUtils.clearAndMakeWikiPath;
import static de.uniwue.d3web.gitConnector.GitTestUtils.initGitAndSetOriginRepo;
import static org.junit.Assume.assumeTrue;

/**
 * Provides a lot of helpful stuff to create tests for the GitConnectors including
 * this parameterization, instantiating all existing GitConnector implementations
 * to run every test on each of them.
 */
public class GitConnectorTestTemplate {

	public static final String FILE = "Foo.txt";
	private static final String TARGET = "target";
	private static final String TARGET_ORIGIN = TARGET+"/origin/";
	private static final String REPO = "Repo1";
	private static final String TARGET_ORIGIN_Repo = TARGET_ORIGIN + REPO;
	private static final String WIKI_1 = "Wiki1";
	private static final String WIKI_PATH = TARGET + "/" + WIKI_1;
	static final File CONTENT_FILE = new File(WIKI_PATH, FILE);

	protected  GitConnector gitConnector;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<GitConnector> sources() {
		return List.of(
				JGitConnector.fromPath(WIKI_PATH),
				JGitBackedGitConnector.fromPath(WIKI_PATH),
				new CachingGitConnector(JGitBackedGitConnector.fromPath(WIKI_PATH)),
				BareGitConnector.fromPath(WIKI_PATH)
		);
	}


	public void setUp(boolean doInitialCommit) throws IOException {

		assumeTrue(gitConnector.gitInstalledAndReady()) ;

		String wikiPathA = clearAndMakeWikiPath(WIKI_PATH);
		String originPath = clearAndMakeWikiPath(TARGET_ORIGIN_Repo);

		// prepare 'remote' bare repo
		RawGitExecutor.executeGitCommand("git init --bare", originPath);

		initGitAndSetOriginRepo(wikiPathA, REPO);


		if(doInitialCommit) {
			String initFile = "InitContent.txt";
			writeAndAddContentFile(initFile);
			gitCommit(initFile);
			gitConnector.pushAll();
			assumeTrue(gitConnector.status().isClean());
		}

	}

	void gitDelete(boolean cached) {
		gitConnector.deletePaths(List.of(FILE), new UserData("huhu", "", ""), cached );
	}

	static void gitDelete() throws IOException {
		FileUtils.delete(new File(WIKI_PATH, FILE));
	}

	private void gitCommit(String file) {
		gitConnector.commitPathsForUser("huhu", "markus merged", "m@merged.com", Collections.singleton(file));
	}

	void gitCommit() {
		gitCommit(FILE);
	}

	private void writeAndAddContentFile(String file) throws IOException {
		writeAndAddContentFile(file, "CONTENT");
	}

	private void writeAndAddContentFile(String file, String content) throws IOException {
		write(file, content);
		gitAdd(file);
	}

	void gitAdd() {
		gitConnector.addPath(FILE);
	}

	private void gitAdd(String file) {
		gitConnector.addPath(file);
	}

	protected static void write(String file, String content) throws IOException {
		FileUtils.write(new File(WIKI_PATH, file), content, Charset.defaultCharset());
	}

	protected static void write() throws IOException {
		FileUtils.write(new File(WIKI_PATH, FILE), "CONTENT", Charset.defaultCharset());
	}

	void writeAndAddContentFile() throws IOException {
		writeAndAddContentFile(FILE, "CONTENT");
	}
}
