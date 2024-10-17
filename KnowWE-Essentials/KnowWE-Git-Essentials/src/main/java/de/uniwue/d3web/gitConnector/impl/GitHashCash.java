package de.uniwue.d3web.gitConnector.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.uniwue.d3web.gitConnector.UserData;

public class GitHashCash {

	private String HEAD;

	//maps a page to a list of commit hashes that were valid up to the given HEAD
	private ConcurrentHashMap<String, List<String>> cacheMap;

	//maps a commit hash to its userdata
	private ConcurrentHashMap<String, UserData> userDataCache;

	//and a map for commithash to timestamp of commit
	private ConcurrentHashMap<String, Long> timestampCache;

	//and a map for commithash to filesizes of commit
	private ConcurrentHashMap<String, Map<String,Long>> filesizesCache;


	public GitHashCash() {
		this.cacheMap = new ConcurrentHashMap<>();
		this.userDataCache = new ConcurrentHashMap<>();
		this.timestampCache = new ConcurrentHashMap<>();
		this.filesizesCache = new ConcurrentHashMap<>();
	}

	public List<String> get(String key) {
		if(cacheMap.containsKey(key)){
			return cacheMap.get(key);
		}
		return Collections.emptyList();
	}

	public void put(String key, List<String> value) {
		if(value==null || value.isEmpty()){
			throw new IllegalStateException("Tried to cache an empty list for key: " + key);
		}
		cacheMap.put(key, value);
	}

	public boolean contains(String key) {
		return cacheMap.containsKey(key);
	}

	public String getHEAD() {
		return this.HEAD;
	}

	public void setHEAD(String HEAD) {
		this.HEAD = HEAD;
	}

	public void invalidate(String file) {
		this.cacheMap.remove(file);
	}

	public boolean hasUserDataFor(String commitHash){
		return this.userDataCache.containsKey(commitHash);
	}

	public void putUserDataFor(String commitHash, UserData userData){
		this.userDataCache.put(commitHash, userData);
	}

	public UserData getUserDataFor(String commitHash){
		return this.userDataCache.get(commitHash);
	}

	public boolean hasTimestampFor(String commitHash){
		return this.timestampCache.containsKey(commitHash);
	}

	public void putTimestampFor(String commitHash, Long value){
		this.timestampCache.put(commitHash, value);
	}

	public Long getTimestampFor(String commitHash){
		return this.timestampCache.get(commitHash);
	}

	public boolean hasFilesizeFor(String commitHash,String path){
		if(this.filesizesCache.containsKey(commitHash)){
			Map<String, Long> stringLongMap = this.filesizesCache.get(commitHash);
			return stringLongMap.containsKey(path);
		}

		return false;
	}

	public void putFilesizeFor(String commitHash, String path,Long value){
		if(this.filesizesCache.containsKey(commitHash)){
			Map<String, Long> stringLongMap = this.filesizesCache.get(commitHash);
			stringLongMap.put(path,value);
		}
		else{
			ConcurrentHashMap<String, Long> stringLongConcurrentHashMap = new ConcurrentHashMap<>();
			stringLongConcurrentHashMap.put(path,value);
			this.filesizesCache.put(commitHash,stringLongConcurrentHashMap);
		}
	}

	//unsafe, use with hasFilesizeFor!!
	public Long getFilesizeFor(String commitHash,String path){
		return this.filesizesCache.get(commitHash).get(path);
	}
}
