package de.uniwue.d3web.gitConnector.oop.model;

public class GitTag {

	public final String name;

	private final GitRepository repository;

	public GitTag(String name, GitRepository gitRepository) {
		this.name = name;
		this.repository = gitRepository;
	}
}
