/*
 * Copyright (C) 2022 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

/**
 * Marks a service that is closed together with the {@link Environment} it belongs to, in reverse order of creation.
 * The name says who owns it, the single method says what that means: the environment calls {@link #close()} once,
 * nobody else ever does. A service is declared with a {@link ServiceKey} and fetched with
 * {@link Environment#getService(ServiceKey)}; implementing this interface is optional and only says "I hold
 * something that has to be released" (listeners, threads, caches).
 * <p>
 * Deliberately not an {@link AutoCloseable}: callers obtain a service to use it, never to close it, so
 * try-with-resources would be wrong (and IDE inspections would demand it).
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
