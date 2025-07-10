package de.uniwue.d3web.gitConnector.oop.model;

import java.util.List;

import de.uniwue.d3web.gitConnector.impl.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector;

public class Example {

	public static void main(String[] args) {
		String path = "/Users/mkrug/Konap/Wiki_VM_release";

		JGitBackedGitConnector gitConnector = JGitBackedGitConnector.fromPath(path);
		GitRepository gitRepository = new GitRepository(new CachingGitConnector(gitConnector));

		GitBranch gitBranch = gitRepository.getCurrentLocalBranch().get();

		List<GitCommit> gitCommits = gitBranch.commitsForPath("versionen.txt");

		System.out.println(gitBranch.name);

		for (GitCommit gitCommit : gitCommits) {

			for(GitFile gitFile: gitCommit.getAffectedPaths()){
				System.out.println(gitFile.path);
				System.out.println(gitFile.getVersion());
			}
		}
	}
}
