/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.impl.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.RawGitExecutor;

public class GitTestUtils {
	public static @NotNull String clearAndMakeWikiPath(String wikiPath) {
		File wikiDirA = new File(wikiPath);
		//noinspection ResultOfMethodCallIgnored
		try {
			FileUtils.deleteDirectory(wikiDirA);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		assert (!wikiDirA.exists());
		wikiDirA.mkdirs();
		return wikiPath;
	}

	public static void initGitAndSetOriginRepo(String localPath, String remoteOriginRelativeFolder) {
		RawGitExecutor.executeGitCommand("git init --initial-branch=" + GitConnector.DEFAULT_BRANCH, localPath);
		RawGitExecutor.executeGitCommand("git remote add origin ../../origin/" + remoteOriginRelativeFolder, localPath);
	}


}
