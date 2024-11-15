/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering;

/**
 * Provides and stores the key for the RenderResult...
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 29.07.2024
 */
public interface RenderResultKeyValueStore {

	<T> T getAttribute(String storeKey);

	void setAttribute(String storeKey, Object value);
}