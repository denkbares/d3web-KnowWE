package org.apache.wiki.providers.commentStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.providers.GitProviderProperties;
import org.apache.wiki.util.TextUtil;
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
	 * The strategy configured by {@code jspwiki.git.commentStrategy}, falling back to {@link ChangeNoteStrategy} if the
	 * property is unset or names a class that cannot be loaded.
	 */
	/**
	 * Called once by the git provider that created this strategy, with the engine the provider belongs to. Strategies
	 * that need wiki state (e.g. the KnowWE environment of that engine) resolve it from here; the default does nothing.
	 */
	default void init(Engine engine) {
	}

	/**
	 * Creates the strategy configured in the properties and initializes it with the specified engine.
	 */
	static GitCommentStrategy fromProperties(Engine engine, Properties properties) {
		GitCommentStrategy strategy = fromProperties(properties);
		strategy.init(engine);
		return strategy;
	}

	static GitCommentStrategy fromProperties(Properties properties) {
		String className = TextUtil.getStringProperty(properties, GitProviderProperties.JSPWIKI_GIT_COMMENT_STRATEGY,
				ChangeNoteStrategy.class.getName());
		return fromProperty(className, GitCommentStrategy.class.getClassLoader());
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
