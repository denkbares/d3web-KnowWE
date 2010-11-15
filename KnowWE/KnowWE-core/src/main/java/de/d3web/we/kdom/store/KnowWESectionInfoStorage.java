/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.store;

import java.util.HashMap;
import java.util.Map;

public class KnowWESectionInfoStorage {

	private final Map<String, InfoStorePerArticle> articleStores = new HashMap<String, InfoStorePerArticle>();

	private final Map<String, InfoStorePerArticle> lastArticleStores = new HashMap<String, InfoStorePerArticle>();

	public Object getStoredObject(String articleName, String kdomid, String key) {
		InfoStorePerArticle artStore = articleStores.get(articleName);
		if (artStore != null) return artStore.getObject(kdomid, key);
		return null;
	}

	public Object getLastStoredObject(String articleName, String kdomid, String key) {
		InfoStorePerArticle oldArtStore = lastArticleStores.get(articleName);
		if (oldArtStore != null) return oldArtStore.getObject(kdomid, key);
		return null;
	}

	public SectionStore getStoredObjects(String articleName, String kdomid) {
		InfoStorePerArticle artStore = articleStores.get(articleName);
		if (artStore != null) return artStore.getSectionStore(kdomid);
		return null;
	}

	public SectionStore getLastSectionStore(String articleName, String kdomid) {
		InfoStorePerArticle oldArtStore = lastArticleStores.get(articleName);
		if (oldArtStore != null) return oldArtStore.getSectionStore(kdomid);
		return null;
	}

	public void storeObject(String articleName, String kdomid, String key, Object o) {
		InfoStorePerArticle artStore = articleStores.get(articleName);
		if (artStore == null) {
			artStore = new InfoStorePerArticle();
			articleStores.put(articleName, artStore);
		}
		artStore.storeObject(kdomid, key, o);
	}

	public void putSectionStore(String articleName, String kdomid, SectionStore store) {
		InfoStorePerArticle artStore = articleStores.get(articleName);
		if (artStore == null) {
			artStore = new InfoStorePerArticle();
			articleStores.put(articleName, artStore);
		}
		artStore.putSectionStore(kdomid, store);
	}

	public void clearStoreForArticle(String articleName) {
		this.lastArticleStores.put(articleName, this.articleStores.remove(articleName));
	}
}
