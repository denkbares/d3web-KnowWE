package org.apache.wiki.providers.commentStrategy;

import org.apache.wiki.api.core.Page;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 16.02.21
 */
@FunctionalInterface
public interface GitCommentStrategy {
	String getComment(Page page,String defaultValue);

	default String getCommentForUser(String user){
		return "";
	}
}
