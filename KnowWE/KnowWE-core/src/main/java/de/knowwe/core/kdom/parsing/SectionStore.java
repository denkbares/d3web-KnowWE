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
import java.util.WeakHashMap;

import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Article;

/**
 * Container class to store and retrieve arbitrary objects for this Section.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.07.2011
 */
public class SectionStore {

	private Map<Compiler, Map<String, Object>> store = null;

	/**
	 * Has the same behavior as {@link SectionStore#getObject(Compiler, String)}
	 * with <tt>null</tt> as the {@link Compiler} argument. Use this if the
	 * Object was stored the same way (<tt>null</tt> as the {@link Compiler}
	 * argument or the method {@link SectionStore#storeObject(String, Object)}.
	 *
	 * @param key is the key for which the object was stored
	 * @return the previously stored Object for this key or <tt>null</tt>, if no
	 * Object was stored
	 * @created 08.07.2011
	 */
	public Object getObject(String key) {
		//noinspection RedundantCast
		return getObject((Compiler) null, key);
	}

	/**
	 * All objects stored in this {@link Section} with the given <tt>key</tt>
	 * are collected and returned. The {@link Map} stores them by the title of
	 * the {@link Article} they were stored for. If an object was stored without
	 * an argument {@link Article} (article independent), the returned
	 * {@link Map} contains this object with <tt>null</tt> as the key.
	 *
	 * @param key is the key for which the objects were stored
	 * @created 16.02.2012
	 */
	public synchronized Map<Compiler, Object> getObjects(String key) {
		if (store == null) return Collections.emptyMap();
		Map<Compiler, Object> objects = new HashMap<>(store.size());
		for (Entry<Compiler, Map<String, Object>> entry : store.entrySet()) {
			Compiler compiler = entry.getKey();
			if (compiler != null && !compiler.getCompilerManager().contains(compiler)) continue;
			Object object = entry.getValue().get(key);
			if (object != null) objects.put(compiler, object);
		}
		return Collections.unmodifiableMap(objects);
	}

	/**
	 * @param compiler is the {@link Article} for which the Object was stored
	 * @param key      is the key, for which the Object was stored
	 * @return the previously stored Object for the given key and article, or
	 * <tt>null</tt>, if no Object was stored
	 * @created 08.07.2011
	 */
	public synchronized Object getObject(Compiler compiler, String key) {
		if (compiler != null && !compiler.getCompilerManager().contains(compiler)) return null;
		Map<String, Object> storeForArticle = getStoreForCompiler(compiler);
		if (storeForArticle == null) return null;
		return storeForArticle.get(key);
	}

	/**
	 * Stores the given Object for the given key.<br/>
	 * <b>Attention:</b> Be aware, that some times an Object should only be
	 * stored in the context of a certain {@link Compiler}. Example: An Object
	 * was created, because the Section was compiled for a certain
	 * {@link Article}. If the Section however gets compiled again for another
	 * {@link Article}, the Object would not be created or a different
	 * {@link Object} would be created. In this case you have to use the method
	 * {@link SectionStore#storeObject(Compiler, String, Object)} to be able to
	 * differentiate between the {@link Compiler}s.
	 *
	 * @param key    is the key for which the Object should be stored
	 * @param object is the object to be stored
	 * @created 08.07.2011
	 */
	public void storeObject(String key, Object object) {
		//noinspection RedundantCast
		storeObject((Compiler) null, key, object);
	}

	/**
	 * Removes the Object stored for the given key.<br/>
	 *
	 * @param key is the key for which the Object should be removed
	 * @created 16.03.2014
	 */
	public Object removeObject(String key) {
		//noinspection RedundantCast
		return removeObject((Compiler) null, key);
	}

	/**
	 * Stores the given Object for the given key and {@link Compiler}.
	 * <b>Attention:</b> If the Object you want to store is independent from the
	 * {@link Compiler} that will or has compiled this {@link SectionStore} 's
	 * {@link Section}, you can either set the {@link Compiler} argument to
	 * <tt>null</tt> or use the method
	 * {@link SectionStore#storeObject(String, Object)} instead (same for
	 * applies for retrieving the Object later via the getObject - methods).
	 *
	 * @param compiler the compiler to store the object for
	 * @param key      the key to store the object for
	 * @param object   the object to be stored
	 * @created 08.07.2011
	 */
	public synchronized void storeObject(Compiler compiler, String key, Object object) {
		Map<String, Object> storeForCompiler = getStoreForCompiler(compiler);
		if (storeForCompiler == null) {
			storeForCompiler = new HashMap<>(8);
			putStoreForCompiler(compiler, storeForCompiler);
		}
		storeForCompiler.put(key, object);
	}

	/**
	 * Removes the Object stored for the given key and {@link Compiler}.
	 * <b>Attention:</b> If the Object you want to remove is independent from the
	 * {@link Compiler} that will or has compiled this {@link SectionStore} 's
	 * {@link Section}, you can either set the {@link Compiler} argument to
	 * <tt>null</tt> or use the method
	 * {@link SectionStore#removeObject(String)} instead.
	 *
	 * @param compiler the compiler to store the object for
	 * @param key      is the key for which the Object should be removed
	 * @created 16.03.2014
	 */
	public synchronized Object removeObject(Compiler compiler, String key) {
		Map<String, Object> storeForCompiler = getStoreForCompiler(compiler);
		if (storeForCompiler == null) return null;
		Object removed = storeForCompiler.remove(key);
		if (storeForCompiler.isEmpty()) store.remove(compiler);
		if (store.isEmpty()) store = null;
		return removed;
	}

	private Map<String, Object> getStoreForCompiler(Compiler compiler) {
		if (store == null) return null;
		return store.get(compiler);
	}

	private void putStoreForCompiler(Compiler compiler, Map<String, Object> storeForCompiler) {
		if (store == null) {
			store = new WeakHashMap<>();
		}
		store.put(compiler, storeForCompiler);
	}

	public boolean isEmpty() {
		return store == null;
	}

}
