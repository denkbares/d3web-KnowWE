/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * An {@link Environment} together with the right to close it, for tests that want try-with-resources:
 * <pre>
 * try (OwnedEnvironment owned = OwnedEnvironment.create(new DummyConnector())) {
 *     Environment environment = owned.get();
 *     ...
 * }
 * </pre>
 * The environment itself is deliberately not {@link AutoCloseable}, because everybody else only obtains it to use it
 * (see {@link Environment}); this type marks the one place that created it.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 14.10.2026
 */
public final class OwnedEnvironment implements AutoCloseable {

	private final Environment environment;

	private OwnedEnvironment(@NotNull Environment environment) {
		this.environment = environment;
	}

	/**
	 * Creates a new environment for the specified connector, see {@link Environment#create(WikiConnector)}.
	 */
	@NotNull
	public static OwnedEnvironment create(@NotNull WikiConnector wiki) {
		return new OwnedEnvironment(Environment.create(wiki));
	}

	/**
	 * Creates a new environment with the specified type tree, see {@link Environment#create(WikiConnector, RootType)}.
	 */
	@NotNull
	public static OwnedEnvironment create(@NotNull WikiConnector wiki, @Nullable RootType rootType) {
		return new OwnedEnvironment(Environment.create(wiki, rootType));
	}

	/**
	 * Returns the owned environment, to use it; it is closed by {@link #close()} of this owner.
	 */
	@NotNull
	public Environment get() {
		return environment;
	}

	/**
	 * Closes the owned environment.
	 */
	@Override
	public void close() {
		environment.close();
	}
}
