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

public class ArticleStore {

	private final Map<String, SectionStore> map = new HashMap<String, SectionStore>(4);

	public void putSectionStore(String kdomID, SectionStore store) {
		map.put(kdomID, store);
	}

	public SectionStore getSectionStore(String kdomID) {
		return map.get(kdomID);
	}

	public Object getObject(String kdomID, String key) {
		SectionStore store = map.get(kdomID);
		if (store != null) return store.getObjectForKey(key);
		return null;
	}

	public void storeObject(String kdomID, String key, Object o) {
		SectionStore secStore = map.get(kdomID);
		if (secStore == null) {
			secStore = new SectionStore();
			map.put(kdomID, secStore);
		}
		secStore.storeObject(key, o);
	}

}
