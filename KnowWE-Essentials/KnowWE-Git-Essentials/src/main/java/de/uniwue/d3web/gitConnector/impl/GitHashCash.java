package de.uniwue.d3web.gitConnector.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GitHashCash {

	private ConcurrentHashMap<String, HistoryTuple> cacheMap;

	public GitHashCash() {
		this.cacheMap = new ConcurrentHashMap<>();
	}

	public HistoryTuple get(String key) {
		return cacheMap.get(key);
	}

	public void put(String key, HistoryTuple value) {
		cacheMap.put(key, value);
	}

	public void put(String key, long timestamp, List<String> commits) {
		cacheMap.put(key, new HistoryTuple(timestamp, commits));
	}

	public boolean contains(String key) {
		return cacheMap.containsKey(key);
	}
}
