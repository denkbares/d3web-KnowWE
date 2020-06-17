/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

package de.knowwe.core.compile;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.knowwe.core.kdom.parsing.Section;

/**
 * A container for a variable that is only valid during one compilation cycle of the {@link CompilerManager}. If the
 * variable gets accessed again after a new compilations has started, the variable from the last compilation gets
 * discarded and retrieved from the supplier again.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.02.2020
 */
public final class CompilationLocal<E> {

	private final CompilerManager compilerManager;
	private final Supplier<E> supplier;
	private volatile int compileId = -1;
	private volatile E variable = null;

	private static final Map<CompilerManager, Map<Object, CompilationLocal<?>>> cache = Collections.synchronizedMap(new WeakHashMap<>());
	private static final Map<CompilerManager, Map<Object, CompilationLocal<?>>> weakCache = Collections.synchronizedMap(new WeakHashMap<>());

	/*
	 * Be more memory friendly by making sure the CompilationLocals cached here in this class are cleaned up as soon as
	 * the next compilation starts, instead of when the CompilationLocals are accessed.
	 */
	static {
		EventManager.getInstance().registerListener(new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return Collections.singleton(CompilationStartEvent.class);
			}

			@Override
			public void notify(Event event) {
				CompilationStartEvent compilationEvent = (CompilationStartEvent) event;
				cache.remove(compilationEvent.getCompilerManager());
				weakCache.remove(compilationEvent.getCompilerManager());
			}
		});
	}

	/**
	 * Get the object provided by the supplier either freshly generated, or if still valid, from the section store. The
	 * object stays valid until a new compilation is started by the given CompilerManager. The {@link CompilationLocal}
	 * is cleaned up together with the section.
	 *
	 * @param compiler the compiler for which to store/cache the object in the section store
	 * @param section  the section providing the section store the store/cache the object
	 * @param key      the key to use when storing the object in the section store, should be unique for the section
	 * @param supplier the supplier generating the object to be cached
	 * @param <L>      the type of the object to be generated
	 * @return a cached or newly generated instance of the object provided by the supplier
	 */
	public static <L> L getCached(@NotNull Compiler compiler, @NotNull Section<?> section, @NotNull String key, @NotNull Supplier<L> supplier) {
		CompilationLocal<L> compilationLocal = section.getObject(compiler, key);
		if (compilationLocal == null) {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (section) {
				compilationLocal = section.getObject(compiler, key);
				if (compilationLocal == null) {
					compilationLocal = create(compiler.getCompilerManager(), supplier);
				}
			}
		}
		return compilationLocal.get();
	}

	/**
	 * Get the object provided by the supplier either freshly generated, or if still valid, from the cache. The object
	 * stays valid until a new compilation is started by the given CompilerManager. Make sure to remove the cache,
	 * if it is no longer needed using the methode {@link #removeCache(Object, CompilerManager)}.
	 *
	 * @param cacheKey        the object by which the supplier/result is cached
	 * @param compilerManager the CompilerManager the cache refers to
	 * @param supplier        the supplier generating the object to be cached
	 * @param <L>             the type of the object to be generated
	 * @return a cached or newly generated instance of the object provided by the supplier
	 */
	public static <L> L getCached(@NotNull Object cacheKey, @NotNull CompilerManager compilerManager, @NotNull Supplier<L> supplier) {
		//noinspection unchecked
		return (L) cache.computeIfAbsent(compilerManager, cm -> new ConcurrentHashMap<>())
				.computeIfAbsent(cacheKey, ck -> new CompilationLocal<>(compilerManager, supplier)).get();
	}

	/**
	 * Removes the object cached by the given cacheKey and CompilerManager from the cache. Use this to clear up memory.
	 *
	 * @param cacheKey        the object by which the supplier/result is cached
	 * @param compilerManager the CompilerManager the cache refers to
	 */
	public static void removeCache(@NotNull Object cacheKey, @NotNull CompilerManager compilerManager) {
		cache.getOrDefault(compilerManager, Collections.emptyMap()).remove(cacheKey);
	}

	/**
	 * Get the object provided by the supplier either freshly generated, or if still valid, from the cache. The object
	 * stays valid until a new compilation is started by the given compilation manager. As soon as the object provided
	 * as the cacheKey is no longer used/referenced elsewhere and garbage collected, the cached object is also cleaned
	 * up.
	 *
	 * @param cacheKey        the object by which the supplier/result is cached
	 * @param compilerManager the CompilerManager the cache refers to
	 * @param supplier        the supplier generating the object to be cached
	 * @param <L>             the type of the object to be generated
	 * @return a cached or newly generated instance of the object provided by the supplier
	 */
	public static <L> L getWeaklyCached(@NotNull Object cacheKey, @NotNull CompilerManager compilerManager, @NotNull Supplier<L> supplier) {
		//noinspection unchecked
		return (L) weakCache.computeIfAbsent(compilerManager, cm -> Collections.synchronizedMap(new WeakHashMap<>()))
				.computeIfAbsent(cacheKey, ck -> new CompilationLocal<>(compilerManager, supplier)).get();
	}

	/**
	 * Creates a new CompilationLocal. The CompilationLocal caches the object from the supplier til the next
	 * compilation, but is not itself cached. So it is the responsibility of the caller to handle the life-cycle of the
	 * CompilationLocal.
	 *
	 * @param compilerManager the CompilerManager the cache refers to
	 * @param supplier        the supplier generating the object to be cached
	 * @param <L>             the type of the object to be generated
	 * @return the CompilationLocal
	 */
	public static <L> CompilationLocal<L> create(@NotNull CompilerManager compilerManager, @NotNull Supplier<L> supplier) {
		return new CompilationLocal<>(compilerManager, supplier);
	}

	private CompilationLocal(@NotNull CompilerManager compilerManager, @NotNull Supplier<E> supplier) {
		this.compilerManager = compilerManager;
		this.supplier = supplier;
	}

	/**
	 * Provides the desired cached variable checked for validity for the current compilation cycle.
	 */
	public E get() {
		if (invalid()) {
			synchronized (this) {
				if (invalid()) {
					variable = supplier.get();
					compileId = compilerManager.getCompilationId();
				}
			}
		}
		return variable;
	}

	private boolean invalid() {
		return variable == null || compileId != compilerManager.getCompilationId();
	}
}
