package org.apache.wiki.providers;

import org.apache.wiki.WikiPage;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 16.02.21
 */
@FunctionalInterface
public interface GitCommentStrategy {
	String getComment(WikiPage page);

	default String getCommentForUser(String user){
		return "";
	}
}
