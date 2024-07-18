package de.knowwe.core.wikiConnector;

import java.util.Date;

import de.knowwe.core.Environment;

/**
 * Implementation of WikiPageInfo that also holds the actual page content text.
 */
public class WikiPageInfoFull extends WikiPageInfo {

	private final String text;

	public WikiPageInfoFull(String name, String author, int version, Date date, String changeNote, String text) {
		super(name, author, version, date, changeNote);
		this.text = text;
	}

	@Override
	public String getText() {
		return text;
	}
}
