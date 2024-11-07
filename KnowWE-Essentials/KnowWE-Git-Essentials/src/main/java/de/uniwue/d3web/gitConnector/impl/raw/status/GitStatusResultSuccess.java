package de.uniwue.d3web.gitConnector.impl.raw.status;

import java.util.ArrayList;
import java.util.List;

import de.uniwue.d3web.gitConnector.impl.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.raw.gitexceptions.GitCommandResultSuccess;

public final class GitStatusResultSuccess implements GitCommandResultSuccess,GitStatusCommandResult {

	private final List<String> removedFiles;
	private final List<String> conflictingFiles;
	private final List<String> untrackedFiles;
	private final List<String> changedFiles;

	private final String errorMessage;
	private final String output;

	public GitStatusResultSuccess(List<String> removedFiles, List<String> conflictingFiles, List<String> untrackedFiles, List<String> changedFiles, String errorMessage, String output) {
		this.removedFiles = removedFiles;
		this.conflictingFiles = conflictingFiles;
		this.errorMessage = errorMessage;
		this.untrackedFiles = untrackedFiles;
		this.changedFiles = changedFiles;
		this.output = output;
	}

	public boolean isClean(){
		return removedFiles.isEmpty() && conflictingFiles.isEmpty() && untrackedFiles.isEmpty() && changedFiles.isEmpty();
	}

	public static GitStatusResultSuccess fromOutput(String output) {
		//parse form output
		List<String> deletedFiles = new ArrayList<>();
		List<String> conflictingFiles = new ArrayList<>();
		List<String> modifiedFiles = new ArrayList<>();
		List<String> untrackedFiles = new ArrayList<>();

		String[] lines = output.split("\n");
		int lineIndex =0;
		for (String line : lines) {
			if (line.matches(".*deleted:.*")) {
				deletedFiles.add(line.replaceAll("deleted:", "").trim());
			}
			else if (line.matches(".*both added:.*") || line.matches(".*both modified:.*")) {
				conflictingFiles.add(line.replaceAll("both modified:","").replaceAll("both added:","").trim());
			}
			else if (line.matches(".*modified:.*")) {
				modifiedFiles.add(line.replaceAll("modified:", "").trim());
			}
			else if (line.matches(".*changed:.*")) {
				modifiedFiles.add(line.replaceAll("changed:", "").trim());
			}

			else if (line.matches(".*Untracked files:.*")) {
				// Untracked files starten ab der 2-nächsten Zeile
				int index = lineIndex+2;

				while ((index < lines.length) && lines[index].startsWith("\t")) {
					untrackedFiles.add(lines[index].trim());
					index++;
				}
			}
			lineIndex++;
		}

		return new GitStatusResultSuccess(deletedFiles, conflictingFiles, untrackedFiles, modifiedFiles, output, output);
	}

	public static void main(String[] args) {
		BareGitConnector connector = BareGitConnector.fromPath("/Users/mkrug/Konap/testWiki3");

		GitStatusCommandResult status = connector.status();
	}
}
