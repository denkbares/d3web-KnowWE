/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

/**
 * Declares a service that belongs to an {@link Environment}: the type it is kept under, and how it is built for one
 * environment. Everything that would otherwise live in a static field of a plugin or core class - caches, registries,
 * thread pools, schedulers - becomes such a service.
 * <p>
 * Declare the key exactly once, as a constant on the service class itself, next to the constructor it calls:
 * <pre>
 * public final class WDManager {
 *
 *     public static final ServiceKey&lt;WDManager&gt; SERVICE = ServiceKey.of(WDManager.class, WDManager::new);
 *
 *     private WDManager(&#64;NotNull Environment environment) { … }
 *
 *     public static WDManager of(&#64;NotNull HasEnvironment source) {
 *         return source.getEnvironment().getService(SERVICE);
 *     }
 * }
 * </pre>
 * Every other place asks for that one constant, so there is no second way to build the service, and the factory
 * cannot capture an environment other than the one it is asked for: it is handed the right one.
 *
 * @param type    the type the service is kept under, also the key of the environment's service map
 * @param factory builds the service for the environment it is given
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 17.09.2026
 */
public record ServiceKey<T>(@NotNull Class<T> type, @NotNull Function<Environment, ? extends T> factory) {

	public static <T> ServiceKey<T> of(@NotNull Class<T> type, @NotNull Function<Environment, ? extends T> factory) {
		return new ServiceKey<>(type, factory);
	}

	@Override
	public String toString() {
		return "service " + type.getName();
	}
}
