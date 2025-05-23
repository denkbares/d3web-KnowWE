package de.knowwe.event;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.Event;

/**
 * Signals the update (created, delete, modify and attachments) of one or more pages
 * <p>
 * Includes the author and page titles, optional a version is included
 *
 * @author Josua Nürnberger
 * @created 2019-08-08
 */
public class ArticleUpdateEvent implements Event {
	private final String username;
	private final String title;
	private Version version;

	/**
	 * Constructs an event for one page
	 *
	 * @param title    page name of the update
	 * @param username author of the update
	 */

	public ArticleUpdateEvent(String title, @NotNull String username) {
		this.username = username;
		this.title = title;
	}

	public @NotNull String getUsername() {
		return username;
	}

	public String getTitle() {
		return title;
	}

	public Version getVersion() {
		return version;
	}

	/**
	 * Sets the optional version of this update
	 *
	 * @param version version number or git revision
	 */
	public void setVersion(Version version) {
		this.version = version;
	}


	public static class Version {
		private final int wikiVersionNumber;
		private String commitHash;

		public Version(int wikiVersionNumber) {
			this.wikiVersionNumber = wikiVersionNumber;
		}

		public Version(int wikiVersionNumber, String commitHash) {
			this.wikiVersionNumber = wikiVersionNumber;
			this.commitHash = commitHash;
		}

		public String getCommitHash() {
			return commitHash;
		}

		public void setCommitHash(String commitHash) {
			this.commitHash = commitHash;
		}

		public int getWikiVersionNumber() {
			return wikiVersionNumber;
		}
	}
}
