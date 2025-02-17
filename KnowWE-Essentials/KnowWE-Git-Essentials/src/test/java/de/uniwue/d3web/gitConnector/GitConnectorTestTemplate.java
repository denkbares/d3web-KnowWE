/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.runners.Parameterized;

import de.uniwue.d3web.gitConnector.impl.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitConnector;
import de.uniwue.d3web.gitConnector.impl.RawGitExecutor;

import static de.uniwue.d3web.gitConnector.impl.RawGitExecutor.clearAndMakeWikiPath;
import static de.uniwue.d3web.gitConnector.impl.RawGitExecutor.initGitAndSetOriginRepo;


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


	public void setUp() throws IOException {

		String wikiPathA = clearAndMakeWikiPath(WIKI_PATH);
		String originPath = clearAndMakeWikiPath(TARGET_ORIGIN_Repo);

		// prepare 'remote' bare repo
		RawGitExecutor.executeGitCommand("git init --bare", originPath);

		initGitAndSetOriginRepo(wikiPathA, REPO);



	}

	void writeAndAddContentFile() throws IOException {
		FileUtils.write(CONTENT_FILE, "CONTENT", Charset.defaultCharset());
		gitConnector.addPath(FILE);
	}
}
