/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.build;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.d3web.testing.TestObjectContainer;
import de.d3web.testing.TestObjectProvider;
import de.knowwe.core.Environment;

/**
 * A test object provider that takes its test objects from a wiki environment. Plugged providers are singletons
 * without a context object; the CI build binds them to the environment of the dashboard it runs for, see {@link
 * #bind(TestObjectProvider, Environment)}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 09.09.2026
 */
public interface WikiTestObjectProvider extends TestObjectProvider {

	/**
	 * Returns the test objects of the specified environment, see {@link TestObjectProvider#getTestObjects(Class,
	 * String)}.
	 */
	<T> List<TestObjectContainer<T>> getTestObjects(@NotNull Environment environment, Class<T> clazz, String name);

	/**
	 * Wiki providers cannot serve test objects without an environment; the CI build binds them first, see {@link
	 * #bind(TestObjectProvider, Environment)}.
	 */
	@Override
	default <T> List<TestObjectContainer<T>> getTestObjects(Class<T> clazz, String name) {
		throw new IllegalStateException("wiki test object provider " + getClass().getName() + " is not bound to an environment");
	}

	/**
	 * Returns a provider that serves the test objects of the specified environment: wiki providers are bound to the
	 * environment, other providers are returned as they are.
	 */
	@NotNull
	static TestObjectProvider bind(@NotNull TestObjectProvider provider, @NotNull Environment environment) {
		if (!(provider instanceof WikiTestObjectProvider wikiProvider)) return provider;
		return new TestObjectProvider() {
			@Override
			public <T> List<TestObjectContainer<T>> getTestObjects(Class<T> clazz, String name) {
				return wikiProvider.getTestObjects(environment, clazz, name);
			}
		};
	}
}
