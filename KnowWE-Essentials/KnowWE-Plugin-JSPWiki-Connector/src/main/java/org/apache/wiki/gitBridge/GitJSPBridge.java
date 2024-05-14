package org.apache.wiki.gitBridge;

import java.util.Properties;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.providers.gitCache.GitVersionCache;
import org.apache.wiki.providers.gitCache.history.GitHistoryProvider;

public interface GitJSPBridge {

	void init(Engine engine, Properties properties);

	GitCommentStrategy getCommentStrategy();

	GitVersionCache getCache();

	GitHistoryProvider getHistoryProvider();
}
