package org.apache.wiki.providers.commentStrategy;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Page;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 16.02.21
 */
public class ChangeNoteStrategy implements GitCommentStrategy {
	@Override
	public String getComment(Page page, String defaultValue) {
		if (page.getAttributes().containsKey(WikiPage.CHANGENOTE)) {
			String changeNote = page.getAttribute(WikiPage.CHANGENOTE);
			// Only a non-blank note counts as an explicit comment. A present-but-empty/null value (a save with no
			// note, or a value left over from reading page info) must fall through to the caller's default instead
			// of being used verbatim.
			if (changeNote != null && !changeNote.isBlank()) {
				return changeNote;
			}
		}
		return defaultValue == null ? "" : defaultValue;
	}
}
