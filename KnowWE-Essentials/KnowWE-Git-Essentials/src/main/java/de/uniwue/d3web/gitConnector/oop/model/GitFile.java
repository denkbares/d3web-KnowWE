package de.uniwue.d3web.gitConnector.oop.model;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import de.uniwue.d3web.gitConnector.GitConnector;

public class GitFile {

	public final GitCommit commit;
	public final String path;

	private int version;

	public GitFile(GitCommit commit, String path) {
		this.commit = commit;
		this.path = path;
		this.version = -1;
	}

	public byte[] bytes() {
		return connector().getBytesForCommit(commit.hash, this.path);
	}

	private GitConnector connector() {
		return commit.branch.getRepository().getGitConnector();
	}

	public int getVersion() {
		if (this.version == -1) {
			List<String> hashes = connector().commitHashesForFile(this.path);
			int index = hashes.indexOf(this.commit.hash);
			if (index != -1) {
				this.version = index + 1;
			}
			else {
				this.version = -1;
			}
		}

		return version;
	}

	public long getFileSize() {
		return connector().getFilesizeForCommit(commit.hash, this.path);
	}
}
