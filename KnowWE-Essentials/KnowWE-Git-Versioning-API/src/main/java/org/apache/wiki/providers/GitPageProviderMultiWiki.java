/*
 * Copyright (C) 2025 denkbares GmbH, Germany
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

package org.apache.wiki.providers;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

/**
 * PageProvider that does a commit with each page modification.
 */
public class GitPageProviderMultiWiki extends FileSystemProviderMultiWiki {

	@Override
	public void movePage(Page from, String to) {
		GitConnector gitConnector = getGitConnector(from);
		String localPageName = SubWikiUtils.getLocalPageName(from.getName());
		String filename = mangleWikiFile(localPageName);
		gitConnector.commit().moveAndCommit(filename, to);
	}

	@Override
	public void putPageText( final Page page, final String text ) throws ProviderException {
		super.putPageText(page, text);

		commitChange(page, "Modified page " + page.getName());
	}

	private void commitChange(Page page, String message) {
		GitConnector gitConnector = getGitConnector(page);
		gitConnector.commit().addPath(mangleWikiFile(SubWikiUtils.getLocalPageName(page.getName())));
		// TODO: handle user data
		gitConnector.commit().commitForUser(new CommitUserData("user", "mail", message));
	}

	@Override
	public void deletePage(final Page page) throws ProviderException {
		super.deletePage(page);
		commitChange(page,  "Deleted page " + page.getName());
	}

	@Override
	protected void putPageProperties(final Page page) throws IOException {
		// write page properties file as usual
		super.putPageProperties(page);

		// then commit it
		GitConnector gitConnector = getGitConnector(page);
		File pagePropertiesFile = getPagePropertiesFile(page);
		gitConnector.commit().addPath(pagePropertiesFile.getName());
		gitConnector.commit().commitForUser(new CommitUserData("user?", "mail?", ""));
	}

	private GitConnector getGitConnector(Page page) {
		String globalPageName = page.getName();
		String subWiki = SubWikiUtils.getSubFolderNameOfPage(globalPageName, this.initProperties);
		return getGitConnector(this.initProperties, subWiki);
	}

	private static GitConnector getGitConnector(Properties wikiProperties, String subWikiFolder) {
		String wikiBaseFolder = (String) wikiProperties.get("jspwiki.fileSystemProvider.pageDir");
		return JGitBackedGitConnector.fromPath(new File(wikiBaseFolder, subWikiFolder).getAbsolutePath());
	}

	public static String unmangleWikiFile(String file) {
		// TODO: implement proper unmangling mechanism
		return file.replace(".txt", "");
	}

	public static String mangleWikiFile(String pageName) {
		String articleName = SubWikiUtils.getLocalPageName(pageName);
		if (articleName.endsWith(".txt") && !articleName.contains(" ")) {
			// is already mangled
			return articleName;
		}
		// TODO: implement proper mangling mechanism
		return articleName.replace(" ", "+") + ".txt";
	}
}
