package org.apache.wiki.providers.gitCache;

import org.apache.wiki.api.core.Engine;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Repository;

public interface GitCacheFactory {

    GitVersionCache create(Engine engine, Repository repository, IgnoreNode ignoreNode);
}
