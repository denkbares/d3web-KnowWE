package org.apache.wiki.providers.gitCache.adhoc;

import java.util.HashMap;
import java.util.Map;

import org.apache.wiki.api.core.Page;

public class PageIdentifierCache {

	//pagename -> (version -> Page)
	private Map<String, Map<Integer, Page>> pageVersionCache;

	public PageIdentifierCache() {
		this.pageVersionCache = new HashMap<>();
	}

	public Page getPageFor(String pageName, int version) {
		if (this.pageVersionCache.containsKey(pageName)) {
			return this.pageVersionCache.get(pageName).get(version);
		}
		return null;
	}

	public void addToCache(String pageName, int version, Page page) {
		//never cache for -1!!
		if(version==-1){
			return;
		}
		if (this.pageVersionCache.containsKey(pageName)) {
			this.pageVersionCache.get(pageName).put(version, page);
		}
		else {
			HashMap<Integer, Page> versionPageCache = new HashMap<>();
			versionPageCache.put(version, page);
			this.pageVersionCache.put(pageName, versionPageCache);
		}
	}
}
