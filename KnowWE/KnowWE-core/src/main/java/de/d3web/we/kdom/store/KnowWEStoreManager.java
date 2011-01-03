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

import de.d3web.we.kdom.SectionID;

public class KnowWEStoreManager {

	private final Map<String, ArticleStore> articleStores = new HashMap<String, ArticleStore>();

	private final Map<String, ArticleStore> lastArticleStores = new HashMap<String, ArticleStore>();

	private final Map<String, ArticleStore> articleIndependentStores = new HashMap<String, ArticleStore>();

	private final Map<String, ArticleStore> lastArticleIndependentStores = new HashMap<String, ArticleStore>();

	public Object getStoredObject(String articleName, String kdomid, String key) {

		// the article independent store overrides the article dependent store
		Object artIndObject = getStoredObjectArticleIndependent(kdomid, key);
		if (artIndObject != null) return artIndObject;

		// if there is nothing inside the article independent store, use the
		// article dependent store
		return getStoredObjectArticleDependent(articleName, kdomid, key);
	}

	public Object getStoredObjectArticleIndependent(String kdomid, String key) {
		String idArticleName = SectionID.getArticleNameFromID(kdomid);
		ArticleStore artIndependentStore = articleIndependentStores.get(idArticleName);
		if (artIndependentStore != null) {
			return artIndependentStore.getObject(kdomid, key);
		}
		return null;
	}

	public Object getStoredObjectArticleDependent(String articleName, String kdomid, String key) {
		if (articleName != null) {
			ArticleStore artStore = articleStores.get(articleName);
			if (artStore != null) return artStore.getObject(kdomid, key);
		}
		return null;
	}

	public Object getLastStoredObject(String articleName, String kdomid, String key) {
		if (articleName == null || articleName.equals("")) {
			articleName = SectionID.getArticleNameFromID(kdomid);
		}
		// the article independent store overrides the article dependent store
		ArticleStore artIndependentStore = lastArticleIndependentStores.get(articleName);
		if (artIndependentStore != null) {
			Object artIndObject = artIndependentStore.getObject(kdomid, key);
			if (artIndObject != null) return artIndObject;
		}
		// if there is nothing inside the article independent store, use the
		// article dependent store
		ArticleStore artStore = lastArticleStores.get(articleName);
		if (artStore != null) return artStore.getObject(kdomid, key);
		return null;
	}

	public void storeObject(String articleName, String kdomid, String key, Object o) {
		if (articleName == null) {
			articleName = SectionID.getArticleNameFromID(kdomid);
			ArticleStore artStore = articleIndependentStores.get(articleName);
			if (artStore == null) {
				artStore = new ArticleStore();
				articleIndependentStores.put(articleName, artStore);
			}
			artStore.storeObject(kdomid, key, o);
		}
		else {
			ArticleStore artStore = articleStores.get(articleName);
			if (artStore == null) {
				artStore = new ArticleStore();
				articleStores.put(articleName, artStore);
			}
			artStore.storeObject(kdomid, key, o);
		}
	}

	public SectionStore getLastSectionStore(String articleName, String
			kdomid) {
		ArticleStore oldArtStore = lastArticleStores.get(articleName);
		if (oldArtStore != null) return oldArtStore.getSectionStore(kdomid);
		return null;
	}

	public void putSectionStore(String articleName, String kdomid,
			SectionStore store) {
		ArticleStore artStore = articleStores.get(articleName);
		if (artStore == null) {
			artStore = new ArticleStore();
			articleStores.put(articleName, artStore);
		}
		artStore.putSectionStore(kdomid, store);
	}

	public SectionStore getLastArticleIndependentSectionStore(String kdomid) {
		ArticleStore oldArtStore = lastArticleIndependentStores.get(SectionID.getArticleNameFromID(kdomid));
		if (oldArtStore != null) return oldArtStore.getSectionStore(kdomid);
		return null;
	}

	public void putArticleIndependentSectionStore(String kdomid, SectionStore store) {
		String articleName = SectionID.getArticleNameFromID(kdomid);
		ArticleStore artStore = articleIndependentStores.get(articleName);
		if (artStore == null) {
			artStore = new ArticleStore();
			articleIndependentStores.put(articleName, artStore);
		}
		artStore.putSectionStore(kdomid, store);
	}

	public void clearStoreForArticle(String title) {
		this.lastArticleStores.put(title, this.articleStores.remove(title));
		this.lastArticleIndependentStores.put(title, this.articleIndependentStores.remove(title));
	}

}
