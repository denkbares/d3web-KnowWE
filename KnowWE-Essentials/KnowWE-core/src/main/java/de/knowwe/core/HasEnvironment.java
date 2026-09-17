/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Something that belongs to an {@link Environment} and can hand it out: a {@link de.knowwe.core.kdom.parsing.Section},
 * an {@link de.knowwe.core.kdom.Article}, an {@link ArticleManager}, a {@link de.knowwe.core.user.UserContext}, a
 * {@link de.knowwe.core.compile.Compiler} - and the environment itself, which returns itself.
 * <p>
 * Take this type as the parameter wherever a method only needs "the environment of whatever the caller has at hand",
 * instead of writing one overload per context class:
 * <pre>
 * public static WDManager of(&#64;NotNull HasEnvironment source) {
 *     return source.getEnvironment().getService(WDManager.SERVICE);
 * }
 * </pre>
 * Callers then pass their section, compiler, user context or environment directly.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 16.09.2026
 */
public interface HasEnvironment {

	/**
	 * Returns the environment this object belongs to, or null if it belongs to none - a section of an article that
	 * was created in a plain unit test, a compiler that is not attached to an article manager.
	 */
	@Nullable
	Environment findEnvironment();

	/**
	 * Returns the environment this object belongs to.
	 *
	 * @throws IllegalStateException if the object belongs to no environment, see {@link #findEnvironment()}
	 */
	@NotNull
	default Environment getEnvironment() {
		Environment environment = findEnvironment();
		if (environment == null) {
			throw new IllegalStateException("No environment available for " + this);
		}
		return environment;
	}
}
