/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.core.kdom.parsing;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;

import static org.junit.Assert.*;

/**
 * Tests the per-section (compiler, key, value) store of {@link Section}.
 */
public class SectionStoreTest {

	private static final CompilerManager MANAGER = new CompilerManager(null);

	private static class TestType extends AbstractType {
	}

	/**
	 * Minimal compiler, optionally registered at the shared manager as live compilers usually are.
	 */
	private static class TestCompiler implements Compiler {

		private final boolean registered;

		TestCompiler(boolean registered) {
			this.registered = registered;
			if (registered) MANAGER.addCompiler(5, this);
		}

		@Override
		public CompilerManager getCompilerManager() {
			return registered ? MANAGER : null;
		}

		@Override
		public boolean isCompiling(Section<?> section) {
			return false;
		}

		@Override
		public void init(CompilerManager compilerManager) {
		}

		@Override
		public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
		}

		@Override
		public void destroy() {
		}
	}

	private static Section<? extends Type> newSection() {
		return Section.createSection("some text", new TestType(), null);
	}

	@Test
	public void storeAndGet() {
		Section<?> section = newSection();
		assertTrue(section.isEmpty());
		assertNull(section.getObject("key"));

		section.storeObject("key", "value");
		assertFalse(section.isEmpty());
		assertEquals("value", section.getObject("key"));
		assertNull(section.getObject("other"));

		// overwrite
		section.storeObject("key", "value2");
		assertEquals("value2", section.getObject("key"));
	}

	@Test
	public void compilerSeparation() {
		Section<?> section = newSection();
		Compiler c1 = new TestCompiler(true);
		Compiler c2 = new TestCompiler(true);
		try {
			section.storeObject("key", "global");
			section.storeObject(c1, "key", "one");
			section.storeObject(c2, "key", "two");

			assertEquals("global", section.getObject("key"));
			assertEquals("one", section.getObject(c1, "key"));
			assertEquals("two", section.getObject(c2, "key"));

			assertEquals(Map.of("key", "one"), section.getObjects(c1));
			assertEquals(Map.of("key", "global"), section.getObjects((Compiler) null));

			Map<Compiler, Object> byCompiler = section.getObjects("key");
			assertEquals(3, byCompiler.size());
			assertEquals("global", byCompiler.get(null));
			assertEquals("one", byCompiler.get(c1));
			assertEquals("two", byCompiler.get(c2));
		}
		finally {
			MANAGER.removeCompiler(c1);
			MANAGER.removeCompiler(c2);
		}
	}

	@Test
	public void removedCompilersAreFiltered() {
		Section<?> section = newSection();
		Compiler c1 = new TestCompiler(true);
		section.storeObject(c1, "key", "one");

		assertEquals("one", section.getObject(c1, "key"));
		MANAGER.removeCompiler(c1);

		// after removal from the manager, the entry is treated as gone
		assertNull(section.getObject(c1, "key"));
		assertTrue(section.getObjects("key").isEmpty());
	}

	@Test
	public void removeObject() {
		Section<?> section = newSection();
		Compiler c1 = new TestCompiler(true);
		try {
			section.storeObject("a", "1");
			section.storeObject(c1, "a", "2");

			assertEquals("1", section.removeObject("a"));
			assertNull(section.getObject("a"));
			assertEquals("2", section.getObject(c1, "a"));
			assertNull(section.removeObject("a"));

			assertEquals("2", section.removeObject(c1, "a"));
			assertTrue(section.isEmpty());
		}
		finally {
			MANAGER.removeCompiler(c1);
		}
	}

	@Test
	public void computeIfAbsent() {
		Section<?> section = newSection();
		AtomicInteger computed = new AtomicInteger();
		String first = section.computeIfAbsent(null, "key", (c, s) -> "v" + computed.incrementAndGet());
		String second = section.computeIfAbsent(null, "key", (c, s) -> "v" + computed.incrementAndGet());
		assertEquals("v1", first);
		assertEquals("v1", second);
		assertEquals(1, computed.get());
	}

	@Test
	public void nullValuesAreStorable() {
		Section<?> section = newSection();
		section.storeObject("key", null);
		assertFalse(section.isEmpty());
		assertNull(section.getObject("key"));
		// getObjects skips null values, as before
		assertTrue(section.getObjects("key").isEmpty());
	}

	@Test
	public void entriesOfCollectedCompilersDisappear() throws Exception {
		Section<?> section = newSection();
		Compiler collectable = new TestCompiler(false);
		section.storeObject(collectable, "key", "dead");
		section.storeObject("key", "alive");

		assertEquals("dead", section.getObject(collectable, "key"));

		WeakReference<Compiler> ref = new WeakReference<>(collectable);
		//noinspection UnusedAssignment
		collectable = null;
		for (int i = 0; i < 100 && ref.get() != null; i++) {
			System.gc();
			Thread.sleep(10);
		}
		assertNull("test compiler was not collected", ref.get());

		// the compiler entry is gone, the compiler independent one remains
		assertEquals("alive", section.getObject("key"));
		Map<Compiler, Object> objects = section.getObjects("key");
		assertEquals(1, objects.size());
		assertEquals("alive", objects.get(null));

		// a write compacts the stale slot away without disturbing live entries
		section.storeObject("other", "x");
		assertEquals("alive", section.getObject("key"));
		assertEquals("x", section.getObject("other"));
	}

	@Test
	public void concurrentReadersAndWriters() throws Exception {
		Section<?> section = newSection();
		Compiler[] compilers = {
				new TestCompiler(true), new TestCompiler(true), new TestCompiler(true), new TestCompiler(true)
		};
		int rounds = 2000;

		ExecutorService executor = Executors.newFixedThreadPool(compilers.length * 2);
		try {
			CountDownLatch start = new CountDownLatch(1);
			Future<?>[] futures = new Future[compilers.length * 2];
			for (int t = 0; t < compilers.length; t++) {
				Compiler compiler = compilers[t];
				// writers, one per compiler
				futures[t] = executor.submit(() -> {
					start.await();
					for (int i = 0; i < rounds; i++) {
						section.storeObject(compiler, "counter", i);
						if (i % 7 == 0) section.removeObject(compiler, "counter");
					}
					section.storeObject(compiler, "counter", rounds);
					return null;
				});
				// readers, one per compiler
				futures[compilers.length + t] = executor.submit(() -> {
					start.await();
					for (int i = 0; i < rounds; i++) {
						Integer value = section.getObject(compiler, "counter");
						assertTrue(value == null || (value >= 0 && value <= rounds));
						section.getObjects("counter");
					}
					return null;
				});
			}
			start.countDown();
			for (Future<?> future : futures) {
				future.get(30, TimeUnit.SECONDS);
			}

			// every compiler ends up with its final value, none got lost
			for (Compiler compiler : compilers) {
				assertEquals((Integer) rounds, section.getObject(compiler, "counter"));
			}
		}
		finally {
			executor.shutdownNow();
			for (Compiler compiler : compilers) {
				MANAGER.removeCompiler(compiler);
			}
		}
	}
}
