package org.apache.wiki.providers;

public interface GitProviderProperties {
	String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT = "jspwiki.gitVersioningFileProvider.remoteGit";
	String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_AUTOUPDATE = "jspwiki.gitVersioningFileProvider.autoUpdate";

	//These are used in order to commit or initialize the repository!
	String JSPWIKI_GIT_DEFAULT_BRANCH = "jspwiki.git.defaultBranch";
	String JSPWIKI_GIT_REMOTE_USERNAME = "jspwiki.git.remoteUsername";
	String JSPWIKI_GIT_REMOTE_TOKEN = "jspwiki.git.remoteToken";


	String JSPWIKI_GIT_COMMENT_STRATEGY = "jspwiki.git.commentStrategy";
	String JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR = "jspwiki.fileSystemProvider.pageDir";
}
