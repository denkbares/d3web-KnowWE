package org.apache.wiki.providers.gitCache.adhoc;

import java.util.HashMap;
import java.util.Map;

public class PageVersionCache {

	//pagename -> (version -> text)
	private Map<String, Map<Integer, String>> pageVersionCache;

	public PageVersionCache() {
		this.pageVersionCache = new HashMap<>();
	}

	public String getTextFor(String pageName, int version) {
		if (this.pageVersionCache.containsKey(pageName)) {
			return this.pageVersionCache.get(pageName).get(version);
		}
		return null;
	}

	public void addToCache(String pageName, int version, String text) {
		//never cache for -1!!
		if(version==-1){
			return;
		}
		if (this.pageVersionCache.containsKey(pageName)) {
			this.pageVersionCache.get(pageName).put(version, text);
		}
		else {
			HashMap<Integer, String> versionTextCache = new HashMap<>();
			versionTextCache.put(version, text);
			this.pageVersionCache.put(pageName, versionTextCache);
		}
	}
}
