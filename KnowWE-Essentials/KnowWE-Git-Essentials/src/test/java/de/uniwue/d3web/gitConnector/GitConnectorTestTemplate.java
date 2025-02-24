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

import de.uniwue.d3web.gitConnector.impl.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitConnector;
import de.uniwue.d3web.gitConnector.impl.RawGitExecutor;

import static de.uniwue.d3web.gitConnector.GitTestUtils.clearAndMakeWikiPath;
import static de.uniwue.d3web.gitConnector.GitTestUtils.initGitAndSetOriginRepo;
import static org.junit.Assume.assumeTrue;

public class GitConnectorTestTemplate {

	public static final String TARGET = "target";
	public static final String TARGET_ORIGIN = TARGET+"/origin/";
	public static final String REPO = "Repo1";
	public static final String TARGET_ORIGIN_Repo = TARGET_ORIGIN + REPO;
	public static final String WIKI_1 = "Wiki1";
	public static final String WIKI_PATH = TARGET + "/" + WIKI_1;
	public static final String FILE = "Foo.txt";
	public static final File CONTENT_FILE = new File(WIKI_PATH, FILE);

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
			commit(initFile);
			gitConnector.pushAll();
			assumeTrue(gitConnector.isClean());
		}

	}

	protected void delete(boolean cached) {
		gitConnector.deletePaths(List.of(FILE), new UserData("huhu", "", ""), cached );
	}

	protected void commit(String file) {
		gitConnector.commitPathsForUser("huhu", "markus merged", "m@merged.com", Collections.singleton(file));
	}

	protected void commit() {
		commit(FILE);
	}

	protected void writeAndAddContentFile(String file) throws IOException {
		writeAndAddContentFile(file, "CONTENT");
	}

	protected void writeAndAddContentFile(String file, String content) throws IOException {
		write(file, content);
		add(file);
	}

	protected void add() {
		gitConnector.addPath(FILE);
	}

	protected void add(String file) {
		gitConnector.addPath(file);
	}

	protected static void write(String file, String content) throws IOException {
		FileUtils.write(new File(WIKI_PATH, file), content, Charset.defaultCharset());
	}

	protected static void delete() throws IOException {
		FileUtils.delete(new File(WIKI_PATH, FILE));
	}

	protected static void write() throws IOException {
		FileUtils.write(new File(WIKI_PATH, FILE), "CONTENT", Charset.defaultCharset());
	}

	void writeAndAddContentFile() throws IOException {
		writeAndAddContentFile(FILE, "CONTENT");
	}
}
