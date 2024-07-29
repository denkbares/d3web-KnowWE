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
public class ServletRequestKeyStore implements RenderResultKeyStore {

	private final HttpServletRequest request;

	public ServletRequestKeyStore(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getAttribute(String storeKey) {
		return (String) request.getAttribute(storeKey);
	}

	@Override
	public void setAttribute(String storeKey, String maskKey) {
		request.setAttribute(storeKey, maskKey);
	}
}
