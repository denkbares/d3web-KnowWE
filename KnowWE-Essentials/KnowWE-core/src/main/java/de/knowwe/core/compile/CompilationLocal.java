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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;

/**
 * A container for a variable that is only valid during one compilation cycle. The variable is discarded as soon as a
 * new compilations starts, so it will be retrieved and cached again as soon as it is first accessed in the new
 * compilation cycle.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.02.2020
 */
public final class CompilationLocal<E> {

	private final Supplier<E> supplier;
	private volatile E variable = null;
	private static final Map<Compiler, Map<Object, CompilationLocal<?>>> compilerCache = new ConcurrentHashMap<>();
	private static final Map<CompilerManager, Map<Object, CompilationLocal<?>>> compilerManagerCache = new ConcurrentHashMap<>();

	static {
		EventManager.getInstance().registerListener(new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return List.of(
						CompilerRemovedEvent.class,
						CompilationStartEvent.class,
						ScriptCompilerCompilePhaseStartEvent.class);
			}

			@Override
			public void notify(Event event) {
				if (event instanceof CompilerRemovedEvent compilerRemovedEvent) {
					compilerCache.remove(compilerRemovedEvent.getCompiler());
				}
				else if (event instanceof @SuppressWarnings("rawtypes")ScriptCompilerCompilePhaseStartEvent scriptCompilerCompilePhaseStartEvent) {
					compilerCache.remove(scriptCompilerCompilePhaseStartEvent.getCompiler());
				}
				else if (event instanceof CompilationStartEvent compilationStartEvent) {
					compilerManagerCache.remove(compilationStartEvent.getCompilerManager());
				}
			}
		});
	}

	/**
	 * Get the object provided by the supplier either freshly generated, or if still valid, from the cache. Use this
	 * method, if the object is dependent on a section. The section, together with the cacheKey, will be used to store
	 * the object in the cache. The object is available/valid during the given compilers current compile phase and, in
	 * case of an incremental compiler, also during destroy phase of the next compilation. It will be cleared, as soon
	 * as the compiler is removed/destroyed or the compiler start the compile phase of the next compilation.
	 * Use method {@link #removeCache(Compiler, Section, Object)} to remove the cached object even sooner.
	 *
	 * @param compiler the compiler the cache refers to
	 * @param section  the section by which the supplier/result is cached
	 * @param cacheKey the object by which the supplier/result is cached
	 * @param supplier the supplier generating the object to be cached
	 * @param <L>      the type of the object to be generated
	 * @return a cached or newly generated instance of the object provided by the supplier
	 */
	public static <L> L getCached(@NotNull Compiler compiler, @NotNull Section<?> section, @NotNull Object cacheKey, @NotNull Supplier<L> supplier) {
		return getCached(compiler, new Pair<>(section, cacheKey), supplier);
	}

	/**
	 * Get the object provided by the supplier either freshly generated, or if still valid, from the cache. The object
	 * is available/valid during the given compilers current compile phase and, in case of an incremental compiler,
	 * also during destroy phase of the next compilation. It will be cleared, as soon as the compiler is
	 * removed/destroyed or the compiler start the compile phase of the next compilation.
	 * Use method {@link #removeCache(Compiler, Object)} to remove the cached object even sooner.
	 *
	 * @param compiler the compiler the cache refers to
	 * @param cacheKey the object by which the supplier/result is cached
	 * @param supplier the supplier generating the object to be cached
	 * @param <L>      the type of the object to be generated
	 * @return a cached or newly generated instance of the object provided by the supplier
	 */
	public static <L> L getCached(@NotNull Compiler compiler, @NotNull Object cacheKey, @NotNull Supplier<L> supplier) {
		//noinspection unchecked
		return (L) compilerCache.computeIfAbsent(compiler, cm -> new ConcurrentHashMap<>())
				.computeIfAbsent(cacheKey, ck -> new CompilationLocal<>(supplier)).get();
	}

	/**
	 * Get the object provided by the supplier either freshly generated, or if still valid, from the cache. Use this
	 * method, if the object should be cached independently of a specific compiler. It will be available/valid during
	 * the current compilation of the given CompilerManager. It will be cleared, as soon as the CompilerManager starts
	 * the next compilation. Use method {@link #removeCache(CompilerManager, Object)} to remove the cached object even
	 * sooner.
	 *
	 * @param <L>             the type of the object to be generated
	 * @param compilerManager the CompilerManager the cache refers to
	 * @param cacheKey        the object by which the supplier/result is cached
	 * @param supplier        the supplier generating the object to be cached
	 * @return a cached or newly generated instance of the object provided by the supplier
	 */
	public static <L> L getCached(@NotNull CompilerManager compilerManager, @NotNull Object cacheKey, @NotNull Supplier<L> supplier) {
		//noinspection unchecked
		return (L) compilerManagerCache.computeIfAbsent(compilerManager, cm -> new ConcurrentHashMap<>())
				.computeIfAbsent(cacheKey, ck -> new CompilationLocal<>(supplier)).get();
	}

	/**
	 * Removes the object cached by the given cacheKey and CompilerManager from the cache. Use this to clear up memory.
	 *
	 * @param compilerManager the CompilerManager the cache refers to
	 * @param cacheKey        the object by which the supplier/result is cached
	 */
	public static void removeCache(@NotNull CompilerManager compilerManager, @NotNull Object cacheKey) {
		compilerManagerCache.getOrDefault(compilerManager, Collections.emptyMap()).remove(cacheKey);
	}

	/**
	 * Removes the object cached by the given cacheKey and compiler from the cache. Use this to clear up memory.
	 *
	 * @param cacheKey the object by which the supplier/result is cached
	 * @param compiler the Compiler the cache refers to
	 */
	public static void removeCache(@NotNull Compiler compiler, @NotNull Object cacheKey) {
		compilerCache.getOrDefault(compiler, Collections.emptyMap()).remove(cacheKey);
	}

	/**
	 * Removes the object cached by the given cacheKey and compiler from the cache. Use this to clear up memory.
	 *
	 * @param cacheKey the object by which the supplier/result is cached
	 * @param compiler the Compiler the cache refers to
	 */
	public static void removeCache(@NotNull Compiler compiler, Section<?> section, @NotNull Object cacheKey) {
		removeCache(compiler, new Pair<>(section, cacheKey));
	}

	private CompilationLocal(@NotNull Supplier<E> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Provides the desired cached variable checked for validity for the current compilation cycle.
	 */
	private E get() {
		if (variable == null) {
			synchronized (this) {
				if (variable == null) {
					variable = supplier.get();
				}
			}
		}
		return variable;
	}
}
