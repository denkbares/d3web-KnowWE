/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.parsing;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Registry of the sections of one {@link de.knowwe.core.Environment} by their ids. Section ids are generated lazily
 * (see {@link Section#getID()}) as the hash of the section's signature, disambiguated by incrementing on collisions.
 * The registry belongs to the environment, so sections of a closed environment cannot be resolved anymore and two
 * environments in one JVM do not see each other's sections.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 07.09.2026
 */
public final class SectionRegistry {

	private final Map<Integer, Section<?>> sections = new HashMap<>(2048);

	/**
	 * Registers the section and returns its id. If the section is already registered, its id is returned.
	 */
	synchronized int register(Section<?> section, int idCandidate) {
		Section<?> existingSection = sections.get(idCandidate);
		if (existingSection == section) return idCandidate; // already registered
		while (existingSection != null || idCandidate == -1) {
			++idCandidate;
			existingSection = sections.get(idCandidate);
		}
		sections.put(idCandidate, section);
		return idCandidate;
	}

	synchronized void unregister(int id) {
		sections.remove(id);
	}

	/**
	 * Returns the section registered for the specified (hexadecimal) id, or null if there is none.
	 */
	@Nullable
	public synchronized Section<?> get(String id) {
		// We have to parse long and convert to int, because when converting a int to a hex string, the negative
		// sign is lost, resulting in for Integer.parseInt() not parsable values. Parsing long and casting
		// to int will restore the negative sign.
		return sections.get((int) Long.parseLong(id, 16));
	}

	public synchronized int size() {
		return sections.size();
	}

	public synchronized void clear() {
		sections.clear();
	}
}
