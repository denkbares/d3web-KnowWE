package de.d3web.we.kdom.store;

import java.util.HashMap;
import java.util.Map;

public class SectionStore {

	private Map<String,Object> store = new HashMap<String, Object>();
	
	public Object getObjectForKey(String key) {
		return store.get(key);
	}
	
	public void storeObject(String key, Object o) {
		store.put(key, o);
	}
} 
