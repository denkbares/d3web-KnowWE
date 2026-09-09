/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.test;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.d3web.testing.Test;
import de.d3web.testing.TestSpecification;
import de.knowwe.core.Environment;

/**
 * A test specification of a CI dashboard: it knows the environment of the wiki the dashboard belongs to, so that tests
 * whose test object does not know its environment (e.g. plain model objects) can still resolve the wiki they run in
 * via {@link de.d3web.we.ci4ke.util.CIUtils#environment(TestSpecification)}. The dashboard binds all its
 * specifications, whoever created them, see {@link #bind(Environment, List)}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 */
public class CITestSpecification<T> extends TestSpecification<T> {

	private final Environment environment;

	public CITestSpecification(@NotNull Environment environment, @NotNull Test<T> test, @NotNull String testObject, String[] args, String[][] ignores, boolean isSoftTest, boolean isFrozenTest) {
		super(test, testObject, args, ignores, isSoftTest, isFrozenTest);
		this.environment = environment;
	}

	/**
	 * The environment of the wiki the dashboard of this specification belongs to.
	 */
	@NotNull
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Returns the specification bound to the environment: the specification itself if it already is bound to that
	 * environment, otherwise an equal copy that is.
	 */
	@NotNull
	public static <T> TestSpecification<T> bind(@NotNull Environment environment, @NotNull TestSpecification<T> specification) {
		if (specification instanceof CITestSpecification<T> bound && bound.environment == environment) return specification;
		return new CITestSpecification<>(environment, specification.getTest(), specification.getTestObject(),
				specification.getArguments(), specification.getIgnores(), specification.isSoftTest(), specification.isFrozenTest());
	}

	/**
	 * Returns the specifications bound to the environment, see {@link #bind(Environment, TestSpecification)}.
	 */
	@NotNull
	public static List<TestSpecification<?>> bind(@NotNull Environment environment, @NotNull List<TestSpecification<?>> specifications) {
		List<TestSpecification<?>> bound = new ArrayList<>(specifications.size());
		for (TestSpecification<?> specification : specifications) {
			bound.add(bind(environment, specification));
		}
		return bound;
	}
}
