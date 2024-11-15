/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation for the render result key store
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 29.07.2024
 */
public class ServletRequestKeyValueStore implements RenderResultKeyValueStore {

	private final HttpServletRequest request;

	public ServletRequestKeyValueStore(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public <T> T getAttribute(String storeKey) {
		//noinspection unchecked
		return (T) request.getAttribute(storeKey);
	}

	@Override
	public void setAttribute(String storeKey, Object object) {
		request.setAttribute(storeKey, object);
	}
}
