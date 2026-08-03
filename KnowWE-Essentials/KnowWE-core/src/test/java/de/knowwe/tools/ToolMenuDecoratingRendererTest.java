/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */
package de.knowwe.tools;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.RenderResultKeyValueStore;
import de.knowwe.core.user.UserContext;

import static org.junit.Assert.*;

/**
 * Pins the markup emitted by the tool menu decoration.
 *
 * @author Konstantin Herud (denkbares GmbH)
 * @created 03.08.2026
 */
public class ToolMenuDecoratingRendererTest {

	private static UserContext userContext() {
		Map<String, Object> attributes = new HashMap<>();
		RenderResultKeyValueStore store = new RenderResultKeyValueStore() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAttribute(String storeKey) {
				return (T) attributes.get(storeKey);
			}

			@Override
			public void setAttribute(String storeKey, Object value) {
				attributes.put(storeKey, value);
			}
		};
		return (UserContext) Proxy.newProxyInstance(
				ToolMenuDecoratingRendererTest.class.getClassLoader(),
				new Class<?>[] { UserContext.class },
				(proxy, method, args) -> {
					if (method.getName().equals("getRenderResultKeyValueStore")) return store;
					throw new UnsupportedOperationException(method.getName());
				});
	}

	private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

	@Test
	public void decorationMarkup() {
		RenderResult result = new RenderResult(userContext());
		ToolMenuDecoratingRenderer.renderToolMenuDecorator("<inner>", "menu-id", true, result);
		String html = result.toString();
		assertTrue(html.replaceAll(UUID_PATTERN, "UUID").equals(
				"<span class=\"toolMenuDecorated\">"
				+ "<span class=\"toolsMenuDecorator2\" id=\"UUID\" toolMenuIdentifier=\"menu-id\">"
				+ "<inner></span></span>"));
		assertTrue(html.startsWith("<span class=\"toolMenuDecorated\"><span class=\"toolsMenuDecorator2\" id=\""));
		assertTrue(html.endsWith("\" toolMenuIdentifier=\"menu-id\"><inner></span></span>"));
		assertFalse(html.contains("toolMenuAction"));
	}

	@Test
	public void decorationWithAction() {
		RenderResult result = new RenderResult(userContext());
		ToolMenuDecoratingRenderer.renderToolMenuDecorator("inner", "menu-id", "some-action", true, result);
		String html = result.toString();
		assertTrue(html.contains("\" toolMenuIdentifier=\"menu-id\" toolMenuAction=\"some-action\">inner</span></span>"));
	}

	@Test
	public void noTools() {
		RenderResult result = new RenderResult(userContext());
		ToolMenuDecoratingRenderer.renderToolMenuDecorator("inner", "menu-id", false, result);
		assertEquals("inner", result.toString());
	}

	@Test
	public void inPlaceMatchesBuffered() {
		UserContext user = userContext();
		RenderResult buffered = new RenderResult(user);
		RenderResult inner = new RenderResult(buffered);
		inner.appendHtml("<b>").append("content").appendHtml("</b>");
		ToolMenuDecoratingRenderer.renderToolMenuDecorator(inner, "id-1", null, true, buffered);

		RenderResult inPlace = new RenderResult(user);
		ToolMenuDecoratingRenderer.appendToolMenuDecoratorStart("id-1", null, inPlace);
		inPlace.appendHtml("<b>").append("content").appendHtml("</b>");
		ToolMenuDecoratingRenderer.appendToolMenuDecoratorEnd(inPlace);

		// identical except for the random header id
		String uuidBlanked1 = buffered.toString().replaceAll(UUID_PATTERN, "UUID");
		String uuidBlanked2 = inPlace.toString().replaceAll(UUID_PATTERN, "UUID");
		assertEquals(uuidBlanked1, uuidBlanked2);
	}
}
