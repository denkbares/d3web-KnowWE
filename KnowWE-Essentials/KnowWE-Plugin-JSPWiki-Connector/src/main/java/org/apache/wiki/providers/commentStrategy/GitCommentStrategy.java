package org.apache.wiki.providers.commentStrategy;

import java.lang.reflect.InvocationTargetException;

import org.apache.wiki.api.core.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 16.02.21
 */
@FunctionalInterface
public interface GitCommentStrategy {
	Logger LOGGER = LoggerFactory.getLogger(GitCommentStrategy.class);

	String getComment(Page page, String defaultValue);

	default String getCommentForUser(String user) {
		return "";
	}

	/**
	 * Try to load the according comment strategy via reflections
	 *
	 * @param commentStrategyClassName
	 * @param classLoader
	 * @return
	 */
	static GitCommentStrategy fromProperty(String commentStrategyClassName, ClassLoader classLoader) {

		GitCommentStrategy gitCommentStrategy;
		try {
			Class<?> commentStrategyClass = classLoader.loadClass(commentStrategyClassName);
			gitCommentStrategy = (GitCommentStrategy) commentStrategyClass.getConstructor()
					.newInstance(new Object[] {});
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
			   InvocationTargetException e) {
			LOGGER.error("Comment strategy not found " + commentStrategyClassName, e);
			gitCommentStrategy = new ChangeNoteStrategy();
		}

		return gitCommentStrategy;
	}
}
