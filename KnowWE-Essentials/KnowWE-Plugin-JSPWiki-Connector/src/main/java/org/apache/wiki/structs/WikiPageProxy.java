package org.apache.wiki.structs;

import java.util.Date;
import java.util.List;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.jetbrains.annotations.NotNull;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;

public class WikiPageProxy extends WikiPage {

	private GitConnector gitConnector;

	public WikiPageProxy(Engine engine, String name) {
		super(engine, name);
	}

	public void setHistoryProvider(GitConnector gitConnector) {
		this.gitConnector = gitConnector;
	}

	@Override
	public int getVersion() {
		int version = super.getVersion();

		if (version != -1) {
			return version;
		}
		else {
			//would be required to set from git, however i dont think there is a way to trigger this aside from the initial call
			// to get all pages and i cant do this during that time as it would end up being way too slow!
			int a = 2;
		}

		return super.getVersion();
	}

	@Override
	public String getAuthor() {
		String author = super.getAuthor();

		if (author != null) {
			return author;
		}

		PageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.gitConnector.repo().getGitDirectory(), this.getName(), -1);
		List<String> commitHashes = this.gitConnector.log().commitHashesForFile(pageIdentifier.fileName());
		if (commitHashes == null || commitHashes.isEmpty()) {
			return null;
		}
		CommitUserData userData = this.gitConnector.log().commitUserDataFor(commitHashes.get(commitHashes.size() - 1));
		super.setAuthor(userData.user);
		//Note: this is a workaround, if i would determine the version in the corresponding method the code would end up to slow..
		if (this.getVersion() == -1) {
			super.setVersion(commitHashes.size());
		}
		return super.getAuthor();
	}


	@NotNull
	public static WikiPage fromUserData(String pageName, int version, CommitUserData userData, long fileSize, Date commitTime, Engine engine) {
		final WikiPage page = new WikiPage(engine, pageName);

		page.setAuthor(userData.user);
		page.setLastModified(commitTime);
		page.setVersion(version);
		page.setSize(fileSize);
		page.setAttribute(WikiPage.CHANGENOTE, userData.message);
		return page;
	}
}
