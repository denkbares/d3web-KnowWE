/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.defaultMarkup;

import java.util.List;

import org.junit.Test;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

import static org.junit.Assert.*;

/**
 * Checks how a {@link DefaultMarkup} that is shared by all type trees (the usual static MARKUP field) is decorated:
 * a plugged annotation is added to the markup once, while the types and renderers of that annotation belong to the
 * type tree they are built for, so that nothing is shared between environments.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 16.09.2026
 */
public class AnnotationDecorationTest {

	@Test
	public void everyTypeTreeGetsItsOwnPluggedRenderer() {
		DefaultMarkup markup = new DefaultMarkup("Shared");
		markup.addAnnotation("plugged", false);
		markup.addAnnotationRenderer("plugged", PluggedRenderer::new);

		Renderer first = rendererOfAnnotation(new DefaultMarkupType(markup), "plugged");
		Renderer second = rendererOfAnnotation(new DefaultMarkupType(markup), "plugged");

		assertTrue(first instanceof PluggedRenderer);
		assertTrue(second instanceof PluggedRenderer);
		assertNotSame("each type tree must get its own renderer instance", first, second);
	}

	@Test
	public void aFixedRendererIsStillSharedAsRegistered() {
		DefaultMarkup markup = new DefaultMarkup("Shared");
		markup.addAnnotation("fixed", false);
		PluggedRenderer renderer = new PluggedRenderer();
		markup.addAnnotationRenderer("fixed", renderer);

		assertSame(renderer, rendererOfAnnotation(new DefaultMarkupType(markup), "fixed"));
		assertSame(renderer, rendererOfAnnotation(new DefaultMarkupType(markup), "fixed"));
	}

	@Test
	public void anAnnotationAddedToTheSharedMarkupGetsOneTypeOnly() {
		DefaultMarkup markup = new DefaultMarkup("Shared");
		DefaultMarkupType first = new DefaultMarkupType(markup);

		// what decorating the first type tree does with a plugged annotation
		markup.addAnnotation("plugged", false);
		first.addAnnotationType(markup.getAnnotation("plugged"));
		assertEquals(1, annotationTypes(first, "plugged").size());

		// the next type tree already gets the annotation from the shared markup, decorating must not add it again
		DefaultMarkupType second = new DefaultMarkupType(markup);
		assertEquals(1, annotationTypes(second, "plugged").size());
		second.addAnnotationType(markup.getAnnotation("plugged"));
		assertEquals("the annotation must not be added twice to the same type tree",
				1, annotationTypes(second, "plugged").size());
	}

	private static Renderer rendererOfAnnotation(DefaultMarkupType type, String name) {
		List<AnnotationType> types = annotationTypes(type, name);
		assertEquals(1, types.size());
		return types.get(0).getRenderer();
	}

	private static List<AnnotationType> annotationTypes(DefaultMarkupType type, String name) {
		return type.getChildrenTypes().stream()
				.filter(AnnotationType.class::isInstance)
				.map(AnnotationType.class::cast)
				.filter(annotationType -> annotationType.getName().equals(name))
				.toList();
	}

	private static class PluggedRenderer implements Renderer {
		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			// nothing to render, this is about the instance, not the output
		}
	}
}
