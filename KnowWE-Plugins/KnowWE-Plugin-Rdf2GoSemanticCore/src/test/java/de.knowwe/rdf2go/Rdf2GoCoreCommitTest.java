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

package de.knowwe.rdf2go;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.rdf4j.model.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import de.knowwe.core.compile.CompilerManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * What the other threads may rely on while an {@link Rdf2GoCore} is committing: statements can be added without
 * waiting for the commit, a commit returns only when everything added before is in the repository, and the queries of
 * compile threads wait for a commit that is writing to the repository, but not for the listeners of that commit.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 23.11.2026
 */
public class Rdf2GoCoreCommitTest {

	private static final String NS = "http://example.org/commit#";

	private Rdf2GoCore core;
	private ExecutorService threads;
	/** threads of this pool are compile threads, as the ones compile scripts are running in */
	private ExecutorService compileThreads;

	@BeforeClass
	public static void init() throws IOException {
		InitPluginManager.init();
	}

	@Before
	public void setUp() {
		core = new Rdf2GoCore("http://localhost:8080/KnowWE/Wiki.jsp?page=", RepositoryConfigs.find("OWL_HORST_OPTIMIZED_WITH_PROPERTY_CHAINS"));
		threads = Executors.newCachedThreadPool();
		compileThreads = CompilerManager.createExecutorService("CommitTest");
	}

	@After
	public void tearDown() {
		compileThreads.shutdownNow();
		threads.shutdownNow();
		core.close();
	}

	@Test
	public void statementsCanBeAddedWhileCommitting() throws Exception {
		try (CommitBlocker blocker = new CommitBlocker()) {
			core.addStatements(statement("first"));
			Future<Boolean> firstCommit = threads.submit(() -> core.commit());
			blocker.awaitCommit();

			// adding statements must not wait for the commit that is still in progress
			Future<?> adding = threads.submit(() -> core.addStatements(statement("second")));
			try {
				adding.get(10, TimeUnit.SECONDS);
			}
			catch (TimeoutException e) {
				fail("adding a statement was blocked by the running commit");
			}

			// but a second commit has to wait for the first one, and commits the added statement afterwards
			Future<Boolean> secondCommit = threads.submit(() -> core.commit());
			try {
				secondCommit.get(500, TimeUnit.MILLISECONDS);
				fail("the second commit did not wait for the first one");
			}
			catch (TimeoutException expected) {
				// still waiting, as it should
			}
			assertFalse(firstCommit.isDone());

			blocker.release();
			assertTrue(firstCommit.get(30, TimeUnit.SECONDS));
			assertTrue(secondCommit.get(30, TimeUnit.SECONDS));
			assertTrue(ask("first"));
			assertTrue(ask("second"));
		}
	}

	@Test
	public void compileThreadQueriesWaitForRunningCommit() throws Exception {
		int askedWhileCommitting = 0;
		for (int round = 0; round < 5; round++) {
			String marker = "round" + round;
			List<Statement> statements = new ArrayList<>();
			for (int i = 0; i < 5000; i++) {
				statements.add(statement(marker + "-" + i));
			}
			statements.add(statement(marker));
			core.addStatements(statements);

			// ask as soon as the commit is writing: the answer must not come from the state before the commit
			Future<Boolean> commit = threads.submit(() -> core.commit());
			while (!core.isCommitting() && !commit.isDone()) {
				Thread.onSpinWait();
			}
			Future<boolean[]> query = compileThreads.submit(() -> new boolean[] { core.isCommitting(), ask(marker) });
			boolean[] result = query.get(60, TimeUnit.SECONDS);
			if (result[0]) askedWhileCommitting++;
			assertTrue("query of a compile thread did not wait for the running commit", result[1]);
			assertTrue(commit.get(60, TimeUnit.SECONDS));
		}
		assertTrue("no query was asked while a commit was writing, the test did not check anything", askedWhileCommitting > 0);
	}

	@Test
	public void queriesDoNotWaitForListenersOfTheCommit() throws Exception {
		try (CommitBlocker blocker = new CommitBlocker()) {
			core.addStatements(statement("listener"));
			Future<Boolean> commit = threads.submit(() -> core.commit());
			blocker.awaitCommit();

			// the repository is written, the commit is only busy with its listeners now
			assertFalse(core.isCommitting());
			core.awaitCommit();
			try {
				assertTrue(compileThreads.submit(() -> ask("listener")).get(10, TimeUnit.SECONDS));
			}
			catch (TimeoutException e) {
				fail("query of a compile thread was blocked by a listener of the commit");
			}
			assertFalse(commit.isDone());

			blocker.release();
			assertTrue(commit.get(30, TimeUnit.SECONDS));
		}
	}

	private Statement statement(String name) {
		return core.createStatement(core.createIRI(NS + name), core.createIRI(NS + "is"), core.createLiteral(name));
	}

	private boolean ask(String name) {
		return core.sparqlAsk("ASK { <" + NS + name + "> <" + NS + "is> ?value }");
	}

	/**
	 * Holds a commit of the core after it has written to the repository, by blocking a listener the commit notifies.
	 */
	private class CommitBlocker implements EventListener, AutoCloseable {

		private final CountDownLatch reached = new CountDownLatch(1);
		private final CountDownLatch released = new CountDownLatch(1);

		CommitBlocker() {
			EventManager.getInstance().registerListener(this);
		}

		@Override
		public Collection<Class<? extends Event>> getEvents() {
			return List.of(InsertStatementsEvent.class);
		}

		@Override
		public void notify(Event event) {
			if (((InsertStatementsEvent) event).getCore() != core) return;
			reached.countDown();
			try {
				released.await(30, TimeUnit.SECONDS);
			}
			catch (InterruptedException ignore) {
			}
		}

		void awaitCommit() throws InterruptedException {
			assertTrue("the commit did not reach its listeners", reached.await(30, TimeUnit.SECONDS));
		}

		void release() {
			released.countDown();
		}

		@Override
		public void close() {
			release();
			EventManager.getInstance().unregister(this);
		}
	}
}
