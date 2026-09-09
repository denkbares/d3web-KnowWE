/*
 * Copyright (C) 2022 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

/**
 * A service that is owned by an {@link Environment}, see {@link Environment#getService(Class, java.util.function.Supplier)}.
 * The environment closes its services when it is closed, in reverse order of their creation. Deliberately not an
 * {@link AutoCloseable}: callers obtain a service to use it, never to close it, so try-with-resources would be wrong
 * (and IDE inspections would demand it).
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 09.09.2026
 */
public interface EnvironmentService {

	/**
	 * Releases what the service holds (listeners, threads, caches). Called once by the owning environment when it is
	 * closed; must not throw.
	 */
	void close();
}
