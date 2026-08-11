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

package de.knowwe.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.parsing.Section;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DefaultArticleManagerTest {

	@Test
	public void queueingArticleRequiresOpenRegistrationFrame() {
		DefaultArticleManager articleManager = new DefaultArticleManager("test");
		try {
			articleManager.queueArticle("Article", "Content");
			fail("Article was queued outside a registration frame");
		}
		catch (IllegalStateException expected) {
			// expected: otherwise sectionizing could invalidate a KDOM that is still being compiled
		}
	}

	@Test
	public void articleRegistrationFrameWaitsForRunningCompilation() throws Exception {
		DefaultArticleManager articleManager = new DefaultArticleManager("test");
		CompilerManager compilerManager = articleManager.getCompilerManager();
		CountDownLatch compilationStarted = new CountDownLatch(1);
		CountDownLatch finishCompilation = new CountDownLatch(1);
		Compiler blockingCompiler = new BlockingCompiler(compilationStarted, finishCompilation);
		compilerManager.addCompiler(1, blockingCompiler);
		ExecutorService executor = Executors.newSingleThreadExecutor();

		try {
			compilerManager.compile(List.of(), List.of());
			assertTrue("Compilation did not start", compilationStarted.await(5, TimeUnit.SECONDS));

			CountDownLatch openingFrame = new CountDownLatch(1);
			Future<?> registrationFrame = executor.submit(() -> {
				openingFrame.countDown();
				articleManager.open();
				try {
					// Opening the frame is what is under test; no article change is needed here.
				}
				finally {
					articleManager.commit();
				}
			});
			assertTrue("Registration thread did not start", openingFrame.await(5, TimeUnit.SECONDS));

			try {
				registrationFrame.get(200, TimeUnit.MILLISECONDS);
				fail("Article registration frame opened during compilation");
			}
			catch (TimeoutException expected) {
				// expected: the live KDOM remains untouched until the compilation has finished
			}

			finishCompilation.countDown();
			registrationFrame.get(5, TimeUnit.SECONDS);
			compilerManager.awaitTermination();
		}
		finally {
			finishCompilation.countDown();
			executor.shutdownNow();
			compilerManager.awaitTermination();
			compilerManager.removeCompiler(blockingCompiler);
		}
	}

	private static class BlockingCompiler implements Compiler {

		private final CountDownLatch compilationStarted;
		private final CountDownLatch finishCompilation;
		private CompilerManager compilerManager;

		private BlockingCompiler(CountDownLatch compilationStarted, CountDownLatch finishCompilation) {
			this.compilationStarted = compilationStarted;
			this.finishCompilation = finishCompilation;
		}

		@Override
		public CompilerManager getCompilerManager() {
			return compilerManager;
		}

		@Override
		public boolean isCompiling(Section<?> section) {
			return true;
		}

		@Override
		public void init(CompilerManager compilerManager) {
			this.compilerManager = compilerManager;
		}

		@Override
		public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
			compilationStarted.countDown();
			try {
				finishCompilation.await();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		@Override
		public void destroy() {
		}
	}
}
