package de.knowwe.event;

import java.util.Collection;
import java.util.Collections;

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
	private final Collection<String> titles;
	private String version;

	/**
	 * Constructs an event for one page
	 *
	 * @param title    page name of the update
	 * @param username author of the update
	 */
	public ArticleUpdateEvent(String title, String username) {
		this.username = username;
		this.titles = Collections.singletonList(title);
	}

	/**
	 * Constructs an event for an update with multiple pages
	 *
	 * @param titles   list of page names in the update
	 * @param username author of the update
	 */
	public ArticleUpdateEvent(Collection<String> titles, String username) {
		this.username = username;
		this.titles = titles;
	}

	public String getUsername() {
		return username;
	}

	public Collection<String> getTitles() {
		return titles;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * Sets the optional version of this update
	 *
	 * @param version version number or git revision
	 */
	public void setVersion(String version) {
		this.version = version;
	}
}
