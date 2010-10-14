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

package de.d3web.we.terminology.local;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.basic.TerminologyType;

public class LocalTerminologyStorage {

	private Map<TerminologyType, Map<String, LocalTerminologyAccess>> storage;

	public LocalTerminologyStorage() {
		super();
		storage = new HashMap<TerminologyType, Map<String, LocalTerminologyAccess>>();
	}

	public void register(String id, TerminologyType type, LocalTerminologyAccess access) {
		Map<String, LocalTerminologyAccess> map = storage.get(type);
		if (map == null) {
			map = new HashMap<String, LocalTerminologyAccess>();
			storage.put(type, map);
		}
		map.put(id, access);
	}

	public void signoff(String id, TerminologyType type) {
		Map<String, LocalTerminologyAccess> map = storage.get(type);
		if (map == null) return;
		map.remove(id);
		if (map.isEmpty()) {
			storage.remove(type);
		}
	}

	public final LocalTerminologyAccess getTerminology(TerminologyType type, String id) {
		return storage.get(type).get(id);
	}
}
