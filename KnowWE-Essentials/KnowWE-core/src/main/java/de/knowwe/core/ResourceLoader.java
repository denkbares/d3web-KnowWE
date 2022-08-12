/*
 * Copyright (C) 2022 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Files;

/**
 * KnowWERessourceLoader.
 * <p/>
 * The KnowWERessourceLoader stores the JS and CSS files used to extend KNOWWE. Please use the
 * Loader to register your own JS and CSS files. The KnowWERessourceLoader was introduced to ensure
 * the correct order of the JS files, because the KnowWE-helper and KnowWE-core files should loaded
 * first.
 *
 * @author smark
 * @since 2010/02/15
 */
public class ResourceLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

	public enum Type {
		/**
		 * Requests a CSS to be inserted.
		 */
		stylesheet("KnowWEExtension/css/", "css"),

		/**
		 * Requests a script to be loaded.
		 */
		script("KnowWEExtension/scripts/", "js"),

		/**
		 * Requests a module to be loaded. This is basically a script file, which is imported as module.
		 */
		module("KnowWEExtension/scripts/", "js");

		/**
		 * The default path were the resources of the specified type are stored on the server. Used
		 * to minimize typing when adding new scripts to the loader.
		 */
		private final String defaultPath;
		private final String fileExtension;

		Type(String defaultPath, String fileExtension) {
			this.defaultPath = defaultPath;
			this.fileExtension = fileExtension;
		}

		/**
		 * Returns the path where the specified resource is stored on the server.
		 *
		 * @param resource the file to get the path for
		 * @return the path pointing to the specified file (including the file name)
		 */
		public String getPath(String resource) {
			if (resource.contains("://")) {
				return resource;
			}
			else {
				return defaultPath + resource;
			}
		}

		/**
		 * Returns if the specified resource is of this type, checked by the file extension of the
		 * resource's filename.
		 *
		 * @param filename the file to check the type
		 * @return if the extension matches this type
		 */
		public boolean isValid(String filename) {
			return Files.hasExtension(filename, fileExtension);
		}

		/**
		 * Detects the type from the specified file name by considering it's file extension. If no
		 * matching Type is find for the specified file name, an IllegalArgumentException is
		 * thrown.
		 *
		 * @param filename the file name to detect the type for
		 * @return the detected type
		 */
		public static Type detect(String filename) {
			for (Type type : Type.values()) {
				if (type.isValid(filename)) return type;
			}
			throw new IllegalArgumentException("no known resource type for file extension: " + filename);
		}
	}

	/**
	 * Stores the registered script files.
	 */
	private final List<String> scripts = new LinkedList<>();

	/**
	 * Stores the registered module files.
	 */
	private final List<String> module = new LinkedList<>();

	/**
	 * Stores the registered CSS files.
	 */
	private final List<String> stylesheets = new LinkedList<>();

	/**
	 * Instance of the Loader. The loader is implemented as a Singleton.
	 */
	private static ResourceLoader instance;

	/**
	 * Returns the instance of the ResourceLoader.
	 *
	 * @return the singleton instance
	 */
	public static synchronized ResourceLoader getInstance() {
		if (instance == null) {
			instance = new ResourceLoader();
		}
		return instance;
	}

	/**
	 * Creates a ResourceLoader instance by first loading the KnowWE default resources.
	 */
	private ResourceLoader() {
		loadDefaultResources();
	}

	/**
	 * Loads the KnowWE standard Resources:
	 * <p/>
	 * - KnowWE.js
	 * <p/>
	 * - KnowWE-helper.js
	 * <p/>
	 * - general.css
	 *
	 * @created 05.07.2010
	 */
	private void loadDefaultResources() {
		add("tooltipster.css", Type.stylesheet);
		add("jquery-treeTable.css", Type.stylesheet);
		add("jquery-ui.min.css", Type.stylesheet);
		add("jquery-ui.structure.min.css", Type.stylesheet);
		add("jquery-ui.theme.min.css", Type.stylesheet);
		add("general.css", Type.stylesheet);
		add("font-awesome/css/fontawesome.min.css", Type.stylesheet);
		addFirst("KnowWE-Plugin-DropZone.js", Type.script);
		addFirst("KnowWE-notification.js", Type.script);
		addFirst("KnowWE.js", Type.script);
		addFirst("KnowWE-helper.js", Type.script);
		addFirst("jquery-compatibility.js", Type.script);
		addFirst("simpleStorage.min.js", Type.script);
		addFirst("highlight.pack.js", Type.script);
		addFirst("jquery.mousewheel.js", Type.script);
		addFirst("jquery-plugin-collection.js", Type.script);
		addFirst("jquery-tooltipster.js", Type.script);
		addFirst("jquery-treeTable.js", Type.script);
		addFirst("polyfill-promises.min.js", Type.script);
		addFirst("react-dom.production.min.js", Type.script);
		addFirst("react.production.min.js", Type.script);
		addFirst("jquery-ui.min.js", Type.script);
		addFirst("jquery-2.1.0.min.js", Type.script);
	}

	public void replaceResource(String resourceBefore, String resourceAfter, Type type) {
		List<String> list = this.getList(type);

		int i = list.indexOf(resourceBefore);
		if (i < 0) {
			list.add(resourceAfter);
		}
		else {
			list.set(i, resourceAfter);
		}
	}

	/**
	 * Adds a resource file to the loader. The type of the resource is detected from the file
	 * extension of the specified file name.
	 * <p/>
	 * Note: Only the file name has to be added. The ResourceLoader knows the default resource file
	 * location.
	 *
	 * @param file The resource file that should be added
	 */
	public void add(String file) {
		add(file, Type.detect(file));
	}

	/**
	 * Adds a resource file to the loader.
	 * <p/>
	 * Note: Only the file name has to be added. The ResourceLoader knows the default resource file
	 * location.
	 *
	 * @param file The resource file that should be added.
	 * @param type the type of the file
	 */
	public void add(String file, Type type) {
		List<String> list = this.getList(type);

		if (!list.contains(file)) {
			checkFileType(file, type);
			list.add(file);
		}
	}

	/**
	 * Adds a resource file to the loader as first element. Note: Only the file name has to be
	 * added. The ResourceLoader knows the default resource file location.
	 *
	 * @param file The resource file that should be added.
	 */
	public void addFirst(String file, Type type) {
		List<String> list = this.getList(type);
		checkFileType(file, type);

		// add as first element
		list.remove(file);
		list.add(0, file);
	}

	private void checkFileType(String file, Type type) {
		if (!type.isValid(file)) {
			LOGGER.warn("The specified file '" + file + "' " +
					"does not seem to match the requested type " + type.name());
		}
	}

	/**
	 * Removes a formerly added resource file.
	 *
	 * @param file The resource file that should be removed.
	 */
	public void remove(String file, Type type) {
		this.getList(type).remove(file);
	}

	/**
	 * Returns the script files the loader knows.
	 *
	 * @return String The script files.
	 */
	public List<String> getScriptIncludes() {
		return this.scripts;
	}

	/**
	 * Returns the module files the loader knows.
	 *
	 * @return String of module files.
	 */
	public List<String> getModuleIncludes() {
		return this.module;
	}

	/**
	 * Returns the CSS files the loader knows.
	 *
	 * @return String The CSS files.
	 */
	public List<String> getStylesheetIncludes() {
		return this.stylesheets;
	}

	private List<String> getList(Type type) {
		return switch (type) {
			case script -> this.scripts;
			case stylesheet -> this.stylesheets;
			case module -> this.module;
		};
	}
}
