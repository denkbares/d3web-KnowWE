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
package de.knowwe.core.compile;

import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import de.knowwe.core.ArticleLifecycleFixture;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.compile.CompilationFinishedEvent;

import static org.junit.Assert.assertTrue;

/**
 * A compilation ends, whatever its listeners do. The event manager only catches exceptions, and a listener throwing an error used to
 * leave the compiler manager in the state "compiling" forever: no further compilation started, every save waited for it.
 */
public class CompilationEndTest {

	@Rule
	public final ArticleLifecycleFixture fixture = new ArticleLifecycleFixture();

	@Test
	public void compilationEndsAlthoughAFinishListenerThrowsAnError() throws Exception {
		CompilerManager compilerManager = fixture.environment.getArticleManager().getCompilerManager();
		EventListener failing = new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return List.of(CompilationFinishedEvent.class);
			}

			@Override
			public void notify(Event event) {
				throw new AssertionError("a listener failing badly");
			}
		};
		KnowWEUtils.getEventManager(fixture.environment.getArticleManager()).registerListener(failing);
		try {
			compilerManager.compile(List.of(), List.of());
			assertTrue("the compilation did not end", compilerManager.awaitTermination(10000));
		}
		finally {
			KnowWEUtils.getEventManager(fixture.environment.getArticleManager()).unregister(failing);
		}
		compilerManager.compile(List.of(), List.of());
		assertTrue("the next compilation did not end", compilerManager.awaitTermination(10000));
	}
}
