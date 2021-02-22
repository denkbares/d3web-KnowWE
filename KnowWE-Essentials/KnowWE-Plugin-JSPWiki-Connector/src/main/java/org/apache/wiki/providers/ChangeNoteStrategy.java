package org.apache.wiki.providers;

import org.apache.wiki.WikiPage;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 16.02.21
 */
public class ChangeNoteStrategy implements GitCommentStrategy {
	@Override
	public String getComment(WikiPage page) {
		if (page.getAttributes().containsKey(WikiPage.CHANGENOTE)) {
			return ((String) page.getAttribute(WikiPage.CHANGENOTE));
		}
		else return "";
	}
}
