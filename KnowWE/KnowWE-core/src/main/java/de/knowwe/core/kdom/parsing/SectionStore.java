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
package de.knowwe.core.kdom.parsing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.knowwe.core.kdom.Article;

/**
 * Container class to store and retrieve arbitrary objects for this Section.
 * 
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.07.2011
 */
public class SectionStore {

	private HashMap<String, HashMap<String, Object>> store = null;

	/**
	 * Has the same behavior as {@link SectionStore#getObject(Article, String)}
	 * with <tt>null</tt> as the {@link Article} argument. Use this if the
	 * Object was stored the same way (<tt>null</tt> as the {@link Article}
	 * argument or the method {@link SectionStore#storeObject(String, Object)}.
	 * 
	 * @created 08.07.2011
	 * @param key is the key for which the object was stored
	 * @return the previously stored Object for this key or <tt>null</tt>, if no
	 *         Object was stored
	 */
	public Object getObject(String key) {
		return getObject(null, key);
	}

	/**
	 * All objects stored in this {@link Section} with the given <tt>key</tt>
	 * are collected and returned. The {@link Map} stores them by the title of
	 * the {@link Article} they were stored for. If an object was stored without
	 * an argument {@link Article} (article independent), the returned
	 * {@link Map} contains this object with <tt>null</tt> as the key.
	 * 
	 * @created 16.02.2012
	 * @param key is the key for which the objects were stored
	 */
	public Map<String, Object> getObjects(String key) {
		Map<String, Object> objects = new HashMap<String, Object>(store == null ? 2 : store.size());
		if (store != null) {
			for (Entry<String, HashMap<String, Object>> entry : store.entrySet()) {
				Object object = entry.getValue().get(key);
				if (object != null) objects.put(entry.getKey(), object);
			}
		}
		return Collections.unmodifiableMap(objects);
	}

	/**
	 * @created 08.07.2011
	 * @param article is the {@link Article} for which the Object was stored
	 * @param key is the key, for which the Object was stored
	 * @return the previously stored Object for the given key and article, or
	 *         <tt>null</tt>, if no Object was stored
	 */
	public Object getObject(Article article, String key) {
		HashMap<String, Object> storeForArticle = getStoreForArticle(article);
		if (storeForArticle == null) return null;
		return storeForArticle.get(key);
	}

	/**
	 * Stores the given Object for the given key.<br/>
	 * <b>Attention:</b> Be aware, that some times an Object should only be
	 * stored in the context of a certain {@link Article}. Example: An Object
	 * was created, because the Section was compiled for a certain
	 * {@link Article}. If the Section however gets compiled again for another
	 * {@link Article}, the Object would not be created or a different
	 * {@link Object} would be created. In this case you have to use the method
	 * {@link SectionStore#storeObject(Article, String, Object)} to be able to
	 * differentiate between the {@link Article}s.
	 * 
	 * @created 08.07.2011
	 * @param key is the key for which the Object should be stored
	 * @param object is the object to be stored
	 */
	public void storeObject(String key, Object object) {
		storeObject(null, key, object);
	}

	/**
	 * Stores the given Object for the given key and {@link Article}.
	 * <b>Attention:</b> If the Object you want to store is independent from the
	 * {@link Article} that will or has compiled this {@link SectionStore} 's
	 * {@link Section}, you can either set the {@link Article} argument to
	 * <tt>null</tt> or use the method
	 * {@link SectionStore#storeObject(String, Object)} instead (same for
	 * applies for retrieving the Object later via the getObject - methods).
	 * 
	 * @created 08.07.2011
	 * @param article
	 * @param key
	 * @param object
	 */
	public void storeObject(Article article, String key, Object object) {
		HashMap<String, Object> storeForArticle = getStoreForArticle(article);
		if (storeForArticle == null) {
			storeForArticle = new HashMap<String, Object>();
			putStoreForArticle(article, storeForArticle);
		}
		storeForArticle.put(key, object);
	}

	private HashMap<String, Object> getStoreForArticle(Article article) {
		if (store == null) return null;
		return store.get(getKeyForArticle(article));
	}

	private void putStoreForArticle(Article article, HashMap<String, Object> storeForArticle) {
		if (store == null) store = new HashMap<String, HashMap<String, Object>>();
		store.put(getKeyForArticle(article), storeForArticle);
	}

	private String getKeyForArticle(Article article) {
		return article == null ? null : article.getTitle();
	}

	public boolean isEmpty() {
		return store == null;
	}
}
