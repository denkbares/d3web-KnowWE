/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.store;

import java.util.HashMap;
import java.util.Map;

public class KnowWESectionInfoStorage {
	
	private Map<String, InfoStorePerArticle> articleStores = new HashMap<String, InfoStorePerArticle>();
	
	private Map<String, InfoStorePerArticle> lastArticleStores = new HashMap<String, InfoStorePerArticle>();
	
	private InfoStorePerArticle getStoreForArticle(String articleName) {
		InfoStorePerArticle store =  articleStores.get(articleName);
		if(store == null) {
			store = new InfoStorePerArticle();
			articleStores.put(articleName, store);
		}
		return store;
		
	}
	
	private InfoStorePerArticle getStoreForLastVersionOfArticle(String articleName) {
		InfoStorePerArticle store =  lastArticleStores.get(articleName);
		if(store == null) {
			store = new InfoStorePerArticle();
			lastArticleStores.put(articleName, store);
		}
		return store;
		
	}

	public Object getStoredObject(String articleName, String kdomid, String key) {
		InfoStorePerArticle artStore = this.getStoreForArticle(articleName);
		return artStore.getObject(kdomid, key);
			
	}
	
	public Object getLastStoredObject(String articleName, String kdomid, String key) {
		InfoStorePerArticle oldArtStore = this.getStoreForLastVersionOfArticle(articleName);
		return oldArtStore.getObject(kdomid, key);
			
	}
	
	public SectionStore getStoredObjects(String articleName, String kdomid) {
		InfoStorePerArticle artStore = this.getStoreForArticle(articleName);
		return artStore.getStoreForKDOMID(kdomid);
			
	}
	
	public SectionStore getLastStoredObjects(String articleName, String kdomid) {
		InfoStorePerArticle oldArtStore = this.getStoreForLastVersionOfArticle(articleName);
		return oldArtStore.getStoreForKDOMID(kdomid);
			
	}
	
	public void storeObject(String articleName, String kdomid, String key, Object o) {
		InfoStorePerArticle artStore = this.getStoreForArticle(articleName);
		SectionStore secStore = artStore.getStoreForKDOMID(kdomid);
		secStore.storeObject(key, o);
	}
	
	public void putSectionStore(String articleName, String kdomid, SectionStore store) {
		InfoStorePerArticle artStore = this.getStoreForArticle(articleName);
		artStore.putSectionStore(kdomid, store);
	}
	
	public void clearStoreForArticle(String articleName) {
		this.lastArticleStores.put(articleName, this.articleStores.remove(articleName));
	}
}
