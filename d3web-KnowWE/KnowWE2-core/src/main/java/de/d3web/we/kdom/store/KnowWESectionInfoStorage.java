package de.d3web.we.kdom.store;

import java.util.HashMap;
import java.util.Map;

public class KnowWESectionInfoStorage {
	
	private Map<String, InfoStorePerArticle> articleStores = new HashMap<String, InfoStorePerArticle>();
	
	private InfoStorePerArticle getStoreForArticle(String articleName) {
		InfoStorePerArticle store =  articleStores.get(articleName);
		if(store == null) {
			store = new InfoStorePerArticle();
			articleStores.put(articleName, store);
		}
		return store;
		
	}

	public Object getStoredObject(String articleName, String kdomid, String key) {
		InfoStorePerArticle artStore = this.getStoreForArticle(articleName);
		return artStore.getObject(kdomid, key);
			
	}
	
	public void storeObject(String articleName, String kdomid, String key, Object o) {
		InfoStorePerArticle artStore = this.getStoreForArticle(articleName);
		SectionStore secStore = artStore.getStoreForKDOMID(kdomid);
		secStore.storeObject(key, o);
	}
	
	public void clearStoreForArticle(String articleName) {
		this.articleStores.put(articleName,null);
	}
}
