package de.d3web.we.kdom.store;

import java.util.HashMap;
import java.util.Map;

public class InfoStorePerArticle {

	private Map<String, SectionStore> map = new HashMap<String, SectionStore>();
	
	public SectionStore getStoreForKDOMID(String kdomID) {
		SectionStore store = map.get(kdomID);
		if(store == null) {
			store = new SectionStore();
			map.put(kdomID, store);
		}
		return store;
	}
	
	public Object getObject(String kdomID, String key) {
		SectionStore store = this.getStoreForKDOMID(kdomID);
		return store.getObjectForKey(key);
	}
}
