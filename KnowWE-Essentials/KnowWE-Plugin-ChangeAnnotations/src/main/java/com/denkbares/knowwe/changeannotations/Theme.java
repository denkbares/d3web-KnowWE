package com.denkbares.knowwe.changeannotations;

import org.jetbrains.annotations.Nullable;

/**
 * Color theme for the rendered {@code <knowwe-page-annotate>}. {@link #AUTO} omits the
 * {@code data-theme} attribute so the bundled stylesheet's {@code prefers-color-scheme}
 * fallback can take effect; {@link #LIGHT} / {@link #DARK} pin the theme explicitly.
 */
public enum Theme {

	LIGHT("light"),
	DARK("dark"),
	AUTO(null);

	private final @Nullable String dataAttributeValue;

	Theme(@Nullable String dataAttributeValue) {
		this.dataAttributeValue = dataAttributeValue;
	}

	@Nullable
	public String dataAttributeValue() {
		return dataAttributeValue;
	}
}
