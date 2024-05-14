package org.apache.wiki.providers.commentStrategy;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Page;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 16.02.21
 */
public class ChangeNoteStrategy implements GitCommentStrategy {
	@Override
	public String getComment(Page page) {
		if (page.getAttributes().containsKey(WikiPage.CHANGENOTE)) {
			return page.getAttribute(WikiPage.CHANGENOTE);
		}
		else return "";
	}
}
