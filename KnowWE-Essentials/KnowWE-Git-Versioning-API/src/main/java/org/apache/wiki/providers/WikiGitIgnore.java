/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package org.apache.wiki.providers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Streams;

/**
 * The ignore rules a wiki content repository needs: generated content such as CI dashboard builds (the flat
 * attachment file and its filesystem version directory), {@code OLD/} and properties files. Every git-backed part of
 * the wiki applies these, whether as a committed .gitignore or as the exclude rules of a clone.
 */
public final class WikiGitIgnore {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiGitIgnore.class);

	private static final String TEMPLATE_RESOURCE = "gitignore-template";

	private WikiGitIgnore() {
	}

	/**
	 * The rules in .gitignore syntax, comments included, ready to be written as a .gitignore file.
	 */
	@NotNull
	public static String template() {
		try (InputStream stream = WikiGitIgnore.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
			if (stream == null) {
				LOGGER.warn("Resource {} not found, the wiki has no ignore rules", TEMPLATE_RESOURCE);
				return "";
			}
			return Streams.getText(stream);
		}
		catch (IOException e) {
			LOGGER.warn("Failed to read {}", TEMPLATE_RESOURCE, e);
			return "";
		}
	}

	/**
	 * The rules as single entries, without comments and blank lines.
	 */
	@NotNull
	public static List<String> entries() {
		return template().lines()
				.map(String::trim)
				.filter(line -> !line.isEmpty() && !line.startsWith("#"))
				.toList();
	}
}
