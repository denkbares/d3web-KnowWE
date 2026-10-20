package com.denkbares.knowwe.textdiff;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Environment;
import de.knowwe.core.ResourceLoader;
import de.knowwe.plugin.Instantiation;

/**
 * Registers the script and the stylesheet of the web components once at startup, so they load on every page.
 */
public class TextDiffResources implements Instantiation {

	@Override
	public void init(@NotNull Environment environment) {
		String version = ResourceLoader.getJarVersion(TextDiffResources.class);
		ResourceLoader.getInstance().add("KnowWE-Plugin-TextDiff.js", ResourceLoader.Type.script, version);
		ResourceLoader.getInstance().addFirst("KnowWE-Plugin-TextDiff.css", ResourceLoader.Type.stylesheet, version);
	}
}
