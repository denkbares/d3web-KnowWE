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
package de.knowwe.core.compile.terminology;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Rule;
import org.junit.Test;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleLifecycleFixture;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

import static org.junit.Assert.assertTrue;

/**
 * Listeners of term events are called without holding the terminology manager. A listener that reads the terminology, or waits for
 * another thread that does (the lazy reference manager of the ontology plugin does both), used to stop the compile thread forever.
 */
public class TerminologyManagerEventTest {

	@Rule
	public final ArticleLifecycleFixture fixture = new ArticleLifecycleFixture();

	@Test
	public void otherThreadsCanReadTheTerminologyWhileAnUnregistrationIsNotified() {
		Environment environment = fixture.environment;
		TerminologyManager terminologyManager = new TerminologyManager();
		TermCompiler compiler = compiler(environment);
		Section<?> definition = Article.createTemporaryArticle(environment, "term", "Terms-" + UUID.randomUUID(), new RootType())
				.getRootSection();
		Identifier term = new Identifier("term");
		terminologyManager.registerTermDefinition(compiler, definition, String.class, term);

		AtomicBoolean answered = new AtomicBoolean();
		EventListener listener = new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return List.of(TermDefinitionUnregisteredEvent.class);
			}

			@Override
			public void notify(Event event) {
				Thread reader = new Thread(() -> answered.set(terminologyManager.getAllDefinedTerms() != null), "reads-terms");
				reader.setDaemon(true);
				reader.start();
				try {
					reader.join(5000);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		KnowWEUtils.getEventManager(compiler).registerListener(listener);
		try {
			terminologyManager.unregisterTermDefinition(compiler, definition, String.class, term);
			assertTrue("another thread could not read the terminology while the unregistration was notified", answered.get());
		}
		finally {
			KnowWEUtils.getEventManager(compiler).unregister(listener);
		}
	}

	private static TermCompiler compiler(Environment environment) {
		return (TermCompiler) Proxy.newProxyInstance(TermCompiler.class.getClassLoader(), new Class<?>[] { TermCompiler.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "getEnvironment", "findEnvironment" -> environment;
					case "getCompilerManager" -> environment.getArticleManager().getCompilerManager();
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					case "toString" -> "test compiler";
					default -> {
						if (method.isDefault()) yield InvocationHandler.invokeDefault(proxy, method, args);
						throw new UnsupportedOperationException("Unexpected compiler access: " + method.getName());
					}
				});
	}
}
