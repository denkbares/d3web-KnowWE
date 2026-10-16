/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.parsing;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Registry of the published sections of one {@link de.knowwe.core.Environment} by their ids. Section ids are opaque
 * strings computed lazily from the section's signature (see {@link Section#getID()}); only sections of published
 * articles are registered here. The registry belongs to the environment, so sections of a closed environment cannot be
 * resolved anymore and two environments in one JVM do not see each other's sections. Compound operations on ids
 * synchronize on the registry instance.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 07.09.2026
 */
public final class SectionRegistry {

	private final Map<String, Section<?>> sections = new HashMap<>(2048);

	/**
	 * Registers the section under its id.
	 */
	synchronized void put(String id, Section<?> section) {
		sections.put(id, section);
	}

	/**
	 * Removes the registration of the section, but only if the id is still owned by this section.
	 */
	synchronized void remove(String id, Section<?> section) {
		sections.remove(id, section);
	}

	/**
	 * Returns the section registered for the specified id, or null if there is none.
	 */
	@Nullable
	public synchronized Section<?> get(String id) {
		return sections.get(id);
	}

	public synchronized int size() {
		return sections.size();
	}

	public synchronized void clear() {
		sections.clear();
	}
}
