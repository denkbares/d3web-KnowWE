/*
 * Copyright (C) 2012 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core.kdom;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Creates instances of types that reference themselves in their own child graph (recursive types).
 * <p>
 * A type with a recursive child reference cannot be created by simply calling its constructor and initializing it:
 * while its children are built, the recursion would construct the same type again and never terminate. This helper
 * registers the instance as the "in construction" instance for the current thread <em>before</em> its children are
 * built, so recursive references within the same construction resolve to that instance. The scope is cleared
 * afterwards, so the next construction (e.g. for the next environment) gets a fresh instance.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public final class RecursiveType {

	private RecursiveType() {
	}

	/**
	 * Returns the instance currently being constructed for the given scope, or creates and initializes a new one.
	 *
	 * @param scope       the thread-local scope holding the instance that is currently under construction
	 * @param constructor creates a new (uninitialized) instance
	 * @param initializer builds the children of the instance; recursive references while this runs resolve to the
	 *                    instance itself
	 */
	public static <T> T create(@NotNull ThreadLocal<T> scope, @NotNull Supplier<T> constructor, @NotNull Consumer<T> initializer) {
		T current = scope.get();
		if (current != null) return current;
		T instance = constructor.get();
		scope.set(instance);
		try {
			initializer.accept(instance);
		}
		finally {
			scope.remove();
		}
		return instance;
	}
}
