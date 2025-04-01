package de.uniwue.d3web.gitConnector.oop.model;

public class GitNote {

	//id of the note
	public final String hash;

	public final String content;

	public final GitCommit commit;

	public GitNote(String hash, String content, GitCommit commit) {
		this.hash = hash;
		this.content = content;
		this.commit = commit;
	}
}
