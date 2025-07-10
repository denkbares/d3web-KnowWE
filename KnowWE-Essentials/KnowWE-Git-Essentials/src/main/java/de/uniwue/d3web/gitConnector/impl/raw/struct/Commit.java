package de.uniwue.d3web.gitConnector.impl.raw.struct;

import java.util.Date;

public class Commit {

	public final String hash;

	private String author;
	private String email;
	private Date commitDate;
	private String commitMessage;

	public Commit(String hash) {
		this.hash = hash;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
}
