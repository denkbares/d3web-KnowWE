/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom;

import java.util.HashMap;

/**
 * Container class to store and retrieve arbitrary objects for this Section.
 * 
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.07.2011
 */
public class SectionStore {

	private HashMap<String, HashMap<String, Object>> store = null;

	public Object getObject(String key) {
		return getObject(null, key);
	}

	public Object getObject(KnowWEArticle article, String key) {
		HashMap<String, Object> storeForArticle = getStoreForArticle(article);
		if (storeForArticle == null) return null;
		return storeForArticle.get(key);
	}

	public void storeObject(String key, Object object) {
		storeObject(null, key, object);
	}

	public void storeObject(KnowWEArticle article, String key, Object object) {
		HashMap<String, Object> storeForArticle = getStoreForArticle(article);
		if (storeForArticle == null) {
			storeForArticle = new HashMap<String, Object>();
			putStoreForArticle(article, storeForArticle);
		}
		storeForArticle.put(key, object);
	}

	private HashMap<String, Object> getStoreForArticle(KnowWEArticle article) {
		if (store == null) return null;
		return store.get(getKeyForArticle(article));
	}

	private void putStoreForArticle(KnowWEArticle article, HashMap<String, Object> storeForArticle) {
		if (store == null) store = new HashMap<String, HashMap<String, Object>>();
		store.put(getKeyForArticle(article), storeForArticle);
	}

	private String getKeyForArticle(KnowWEArticle article) {
		return article == null ? null : article.getTitle();
	}
}
