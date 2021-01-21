/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.LogManager;

import javax.servlet.ServletContext;

import org.jetbrains.annotations.NotNull;

import com.denkbares.collections.PriorityList;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.plugin.Extension;
import com.denkbares.plugin.JPFPluginManager;
import com.denkbares.plugin.Plugin;
import com.denkbares.plugin.PluginManager;
import com.denkbares.plugin.Resource;
import com.denkbares.utils.Log;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageUnregistrationCompiler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.SectionizerModule;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.taghandler.TagHandler;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.InitEvent;
import de.knowwe.plugin.Instantiation;
import de.knowwe.plugin.Plugins;
import de.knowwe.tools.ToolUtils;

/**
 * This is the core class of KnowWE. It manages the {@link ArticleManager} and provides methods to access {@link
 * Article}s and other Managers. Further it is connected to the used Wiki-engine, holding an instance of {@link
 * WikiConnector} and allows page saves.
 *
 * @author Jochen
 */

public class Environment {

	/**
	 * Indicates whether this environment is initialized or not.
	 */
	private static boolean initialized = false;

	/**
	 * Stores additional renderer if renderer are plugged via the plugin framework The renderer plugged with highest
	 * priority _might_ decided to look up in this list and call other renderer.
	 */
	private final Map<Type, List<Renderer>> additionalRenderer = new HashMap<>();

	/**
	 * An article manager for each web. In case of JSPWiki there is only on web ('default_web')
	 */
	private final Map<String, ArticleManager> articleManagers = new HashMap<>();

	/**
	 * This is the link to the connected Wiki-engine. Allows saving pages etc.
	 */
	private final WikiConnector wikiConnector;

	/**
	 * Holding the default tag handlers of KnowWE
	 */
	private final Map<String, TagHandler> tagHandlers = new HashMap<>();

	/**
	 * The {@link CompilationMode} of KnowWE:
	 */
	private CompilationMode currentCompilationMode = CompilationMode.DEFAULT;

	/**
	 * Hard coded name of the default web
	 */
	public static final String DEFAULT_WEB = "default_web";

	public enum CompilationMode {
		INCREMENTAL, DEFAULT
	}

	/**
	 * Singleton instance
	 */
	private static Environment instance;

	/**
	 * Singleton lazy factory
	 */
	public static synchronized Environment getInstance() {
		if (instance == null) {
			Log.severe("Environment was not instantiated!");
		}
		return instance;
	}

	public static boolean isInitialized() {
		return initialized;
	}

	public static void initInstance(WikiConnector wiki) {
		Log.info("STARTING TO INITIALIZE KNOWWE ENVIRONMENT");

		instance = new Environment(wiki);
		instance.init();
		initialized = true;
		EventManager.getInstance().fireEvent(new InitEvent());

		Log.info("INITIALIZED KNOWWE ENVIRONMENT");
	}

	/**
	 * private constructor
	 *
	 * @param wiki Connector to the used core wiki engine
	 * @see #getInstance()
	 */
	private Environment(WikiConnector wiki) {
		this.wikiConnector = wiki;
	}

	private void init() {

		try {
			initProperties();
			initPlugins();
			initEventManager();
			initTagHandler();
			initSectionizerModules();
			initCompilers();

			// decorate types in breast-first-search
			decorateTypeTree();

			initInstantiations();
			initBlockedTools();
			Plugins.initJS();
			Plugins.initCSS();
		}
		catch (Throwable e) {
			String msg = "Invalid initialization of the wiki. This is caused by an invalid wiki plugin. "
					+ "Wiki is in unstable state. Please exit and correct before using the wiki.";
			Log.severe(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	private void initEventManager() {
		// get all EventListeners
		List<Extension> extensions = new ArrayList<>(Arrays.asList(PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_EventListener)));
		for (Extension extension : extensions) {
			Object o = extension.getSingleton();
			if (o instanceof EventListener) {
				EventManager.getInstance().registerListener(((EventListener) o));
			}
		}
	}

	private void initCompilers() {
		CompilerManager compilerManager = Compilers.getCompilerManager(DEFAULT_WEB);
		PackageManager packageManager = new PackageManager();

		// we want this compiler to run before alle package compilers
		PackageRegistrationCompiler packageRegistrationCompiler = new PackageRegistrationCompiler();
		compilerManager.addCompiler(2, packageRegistrationCompiler);

		compilerManager.addCompiler(4, new DefaultGlobalCompiler());

		for (PriorityList.Group<Double, Compiler> priorityGroup : Plugins.getCompilers().getPriorityGroups()) {
			List<Compiler> compilers = priorityGroup.getElements();
			for (Compiler compiler : compilers) {
				compilerManager.addCompiler(priorityGroup.getPriority(), compiler);
			}
		}

		// we want this compiler to run after all package compilers
		compilerManager.addCompiler(10000, new PackageUnregistrationCompiler(packageRegistrationCompiler));
	}

	/**
	 * Returns true if the specified compiler is a global singleton compiler, that is instantiated (once) for the whole
	 * wiki, and not manually instantiated within the wiki articles.
	 *
	 * @param compiler the compiler to be checked
	 * @return true if the compiler is a wiki-wide (global) singleton compiler
	 */
	public boolean isGlobalCompiler(Compiler compiler) {
		// check for PackageRegistrationCompiler is redundant as being subclass of DefaultGlobalCompiler
		return (compiler instanceof DefaultGlobalCompiler) || Plugins.getCompilers().contains(compiler);
	}

	private void initProperties() {

		setCompilationMode();

		configureLogging();
	}

	private void configureLogging() {

		Log.setContextName(wikiConnector.getApplicationName());

		ResourceBundle config = KnowWEUtils.getConfigBundle();
		Collection<String> logLevelConfigs = getLogLevelConfigs(config);
		if (!logLevelConfigs.isEmpty()) {

			Properties loggingProperties = new Properties();

			// root logger loglevel
			loggingProperties.put(".level", "INFO");

			// specify root logger handler
			loggingProperties.put("handlers",
					"java.util.logging.ConsoleHandler");

			// configure ConsoleHandler
			loggingProperties.put("java.util.logging.ConsoleHandler.formatter",
					"java.util.logging.SimpleFormatter");

			// set ConsoleHandler's log level
			loggingProperties.put("java.util.logging.ConsoleHandler.level",
					"ALL");

			for (String logLevel : logLevelConfigs) {
				// if nothing follows 'loglvl', root logger's log level will
				// be changed
				if (logLevel.equals("loglvl")) {
					loggingProperties.put(".level", config.getString(logLevel));
				}
				// change specific logger's log level
				else {
					String logger = logLevel.substring(logLevel.indexOf('.') + 1);
					loggingProperties.put(logger + ".level",
							config.getString(logLevel));
				}
			}

			// forward properties to LogManager
			PipedOutputStream pos = new PipedOutputStream();
			try {
				PipedInputStream pis = new PipedInputStream(pos);

				loggingProperties.store(pos, "");
				pos.close();

				LogManager.getLogManager().readConfiguration(pis);
				pis.close();
			}
			catch (IOException ioe) {
				Log.severe("Failed to set LogLevel ", ioe);
			}
		}
	}

	private ResourceBundle setCompilationMode() {
		ResourceBundle config = KnowWEUtils.getConfigBundle();
		if (config != null && config.getString("compilation.mode").contains("incremental")) {
			this.setCompilationMode(CompilationMode.INCREMENTAL);
		}
		return config;
	}

	private Collection<String> getLogLevelConfigs(ResourceBundle config) {
		Collection<String> logLevelConfigs = new ArrayList<>();
		// loop config file
		for (String logLevel : config.keySet()) {
			// logLevel properties start with 'loglvl'
			if (logLevel.startsWith("loglvl")) {
				logLevelConfigs.add(logLevel);
			}
		}
		return logLevelConfigs;
	}

	private void initInstantiations() {
		for (Instantiation inst : Plugins.getInstantiations()) {
			inst.init(DEFAULT_WEB);
		}
	}

	private void initBlockedTools() {
		// first check for toolmenu settings file in wiki-content
		String sourceFolder = getWikiConnector().getWikiProperty("var.basedir");
		if (sourceFolder != null) {
			File jsonFile = new File(sourceFolder, ToolUtils.SETTINGS_FILE);
			if (jsonFile.isFile()) {
				ToolUtils.initSettings(jsonFile);
				return;
			}
		}

		// otherwise load all json files in 'KnowWEExtension/toolmenu' folder in webapp
		File appRoot = new File(KnowWEUtils.getApplicationRootPath(), "KnowWEExtension/toolmenu");
		File[] jsonFiles = appRoot.listFiles(f -> f.isFile() && f.getName().endsWith(".json"));
		if (jsonFiles != null) {
			for (File jsonFile : jsonFiles) {
				ToolUtils.initSettings(jsonFile);
			}
		}
	}

	private void initTagHandler() {
		for (TagHandler tagHandler : Plugins.getTagHandlers()) {
			initTagHandler(tagHandler);
		}
	}

	private void initSectionizerModules() {
		for (SectionizerModule sm : Plugins.getSectionizerModules()) {
			SectionizerModule.Registry.register(sm);
		}
	}

	private void initPlugins() throws InstantiationError {
		File libDir = new File(KnowWEUtils.getApplicationRootPath() + "/WEB-INF/lib");
		// when testing, libDir doesn't exist, but the plugin framework is
		// initialized in junit test, so there is no problem
		// if libDir is doesn't exist in runtime, nothing will work, so this
		// code won't be reached ;-)
		if (libDir.exists()) {
			List<File> pluginFiles = getPluginFiles(libDir);
			JPFPluginManager.init(pluginFiles.toArray(new File[0]));
			extractPluginResources();
		}
	}

	private List<File> getPluginFiles(File libDir) {
		File[] files = libDir.listFiles();
		if (files == null) return Collections.emptyList();
		List<File> pluginFiles = new ArrayList<>();
		for (File file : files) {
			if (file.getName().contains("-Plugin-")) {
				pluginFiles.add(file);
			}
		}
		return pluginFiles;
	}

	private void extractPluginResources() throws InstantiationError {
		Plugin[] plugins = PluginManager.getInstance().getPlugins();

		for (Plugin plugin : plugins) {
			Resource[] resources = plugin.getResources();
			for (Resource resource : resources) {
				String pathName = resource.getPathName();
				if (!pathName.endsWith("/") && pathName.startsWith("webapp/")) {
					pathName = pathName.substring("webapp/".length());
					try {
						File file = new File(KnowWEUtils.getApplicationRootPath() + "/" + pathName);
						File parent = file.getParentFile();
						if (!parent.isDirectory()) {
							parent.mkdirs();
						}
						try (FileOutputStream out = new FileOutputStream(file);
							 InputStream in = resource.getInputStream()) {
							byte[] buf = new byte[1024];
							int len;
							while ((len = in.read(buf)) != -1) {
								out.write(buf, 0, len);
							}
						}
					}
					catch (IOException e) {
						String msg = "Cannot instantiate plugin "
								+ plugin
								+ ", the following error occurred while extracting its resources: "
								+ e.getMessage();
						throw new InstantiationError(msg);
					}
				}
			}
		}
	}

	/**
	 * Initialize all types by decorating them. The method makes sure that each instance is decorated only once. To do
	 * this a breath-first-search is used. Thus each item is initialized with the shortest path towards it.
	 *
	 * @created 24.10.2013
	 */
	private void decorateTypeTree() {

		// check whether the priorities between plugged types lead to deterministic structure of the type tree
		Plugins.checkTypePriorityClarity();

		// queue the queue of paths to be initialized
		RootType root = RootType.getInstance();
		LinkedList<Type[]> queue = new LinkedList<>();
		queue.add(new Type[] { root });

		// queuedTypes the already queued instances
		Set<Type> queuedTypes = Collections.newSetFromMap(new IdentityHashMap<>());
		queuedTypes.add(root);

		while (!queue.isEmpty()) {
			// initialize the first element of the queue
			// until the queue is empty
			Type[] path = queue.pop();
			Type type = path[path.length - 1];

			// initialize plugged items
			Plugins.addChildrenTypesToType(type, path);
			Plugins.addAnnotations(type, path);
			Plugins.addRendererToType(type, path);
			Plugins.addCompileScriptsToType(type, path);

			// initialize type itself
			type.init(path);

			// recurse for child types
			for (Type childType : type.getChildrenTypes()) {
				// secure against malformed plugins
				if (childType == null) {
					throw new NullPointerException("type '" + type.getClass().getName() + "' contains 'null' as child");
				}

				// avoid multiple decoration in recursive type declarations
				if (!queuedTypes.add(childType)) continue;

				// create path to this type
				Type[] childPath = new Type[path.length + 1];
				System.arraycopy(path, 0, childPath, 0, path.length);
				childPath[path.length] = childType;

				// add child to the end of the queue --> breadth first search
				queue.add(childPath);
			}
		}
	}

	private void checkPriorityConsistency() {

	}

	private void initTagHandler(TagHandler tagHandler) {
		String tagName = tagHandler.getTagName();
		String key = tagName.toLowerCase();

		if (tagHandlers.containsKey(key)) {
			Log.warning("TagHandler for tag '" + tagName + "' had already been added.");
		}
		else {
			this.tagHandlers.put(key, tagHandler);
		}
	}

	public WikiConnector getWikiConnector() {
		return this.wikiConnector;
	}

	/**
	 * Returns from the prioritized list of plugged renderer for the given type the renderer positioned next in the
	 * priority list after the given renderer. If the passed renderer is not contained in the given list, the first
	 * renderer of the list is returned.
	 *
	 * @param type            the type we want the next renderer for
	 * @param currentRenderer the current renderer
	 * @return the next renderer
	 * @created 04.11.2013
	 */
	public Renderer getNextRendererForType(Type type, Renderer currentRenderer) {
		if (additionalRenderer.containsKey(type)) {
			List<Renderer> pluggedRendererForType = additionalRenderer.get(type);
			if (pluggedRendererForType != null) {
				if (pluggedRendererForType.contains(currentRenderer)) {
					int currentIndex = pluggedRendererForType.indexOf(currentRenderer);
					if (pluggedRendererForType.size() > currentIndex + 1) {
						return pluggedRendererForType.get(currentIndex + 1);
					}
				}
				else {
					if (!pluggedRendererForType.isEmpty()) {
						return pluggedRendererForType.get(0);
					}
				}
			}
		}

		return null;
	}

	public void addRendererForType(Type t, Renderer r) {
		if (!this.additionalRenderer.containsKey(t)) {
			this.additionalRenderer.put(t, new ArrayList<>());
		}
		List<Renderer> rendererForType = this.additionalRenderer.get(t);
		rendererForType.add(0, r);
	}

	public Collection<Renderer> getRenderersForType(Type type) {
		return additionalRenderer.getOrDefault(type, Collections.emptyList());
	}

	/**
	 * Builds an {@link Article} and registers it in the {@link ArticleManager}.
	 */
	public Article buildAndRegisterArticle(String web, String title, String content) {

		// create article with the new content
		return this.getArticleManager(web).registerArticle(title, content);
	}

	/**
	 * Returns the {@link Article} object for a given web and title
	 *
	 * @param web   the web of the {@link Article}
	 * @param title the title of the {@link Article}
	 */
	public Article getArticle(String web, String title) {
		return getArticleManager(web).getArticle(title);
	}

	/**
	 * Returns the {@link ArticleManager} for a given web.
	 *
	 * @param web the web of the {@link ArticleManager}
	 */
	@NotNull
	public ArticleManager getArticleManager(String web) {
		return this.articleManagers.computeIfAbsent(web, k -> new DefaultArticleManager(web));
	}

	public ServletContext getContext() {
		return wikiConnector.getServletContext();
	}

	/**
	 * grants access on the default tag handlers of KnowWE
	 *
	 * @return HashMap holding the default tag handlers of KnowWE
	 */
	public Map<String, TagHandler> getDefaultTagHandlers() {
		return tagHandlers;
	}

	public List<PageAppendHandler> getAppendHandlers() {
		return Plugins.getPageAppendHandlers();
	}

	public void setCompilationMode(CompilationMode mode) {
		currentCompilationMode = mode;
	}

	public CompilationMode getCompilationMode() {
		return currentCompilationMode;
	}

	/**
	 * @created 15.11.2013
	 * @deprecated
	 */
	@Deprecated
	public TerminologyManager getTerminologyManager(String defaultWeb, String master) {
		return KnowWEUtils.getTerminologyManager((master == null)
				? null
				: KnowWEUtils.getArticleManager(defaultWeb).getArticle(master));
	}
}
