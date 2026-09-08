/*
 * Copyright (C) 2022 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import com.denkbares.collections.PriorityList;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.plugin.*;
import com.denkbares.strings.Identifier;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.*;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyExtension;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.SectionRegistry;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.parsing.SectionizerModule;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.taghandler.TagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.core.wikiConnector.WikiPageInfo;
import de.knowwe.event.ArticleUpdateEvent;
import de.knowwe.event.DeInitEvent;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.InitEvent;
import de.knowwe.event.WikiContentReplacedEvent;
import de.knowwe.plugin.Instantiation;
import de.knowwe.plugin.Plugins;
import de.knowwe.tools.ToolUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.LogManager;

import static de.knowwe.core.utils.KnowWEUtils.getDefaultArticleManager;

/**
 * This is the core class of KnowWE. It manages the {@link ArticleManager} and provides methods to access {@link
 * Article}s and other Managers. Further it is connected to the used Wiki-engine, holding an instance of {@link
 * WikiConnector} and allows page saves.
 *
 * @author Jochen
 */

public final class Environment implements AutoCloseable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

	/**
	 * Indicates whether this environment is initialized or not.
	 */
	private static boolean initialized = false;

	/**
	 * Stores additional renderer if renderer are plugged via the plugin framework The renderer plugged with the highest
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

	private RootType rootType;

	/**
	 * The sections of this environment by id.
	 */
	private final SectionRegistry sectionRegistry = new SectionRegistry();

	/**
	 * The compile scripts of this environment's type tree, indexed per compiler class.
	 */
	private final Map<Class<? extends Compiler>, ScriptManager<? extends Compiler>> scriptManagers = new ConcurrentHashMap<>();

	/**
	 * The plugged sectionizer modules of this environment.
	 */
	private final List<SectionizerModule> sectionizerModules = new CopyOnWriteArrayList<>();

	/**
	 * Terms that are occupied by plugged terminology extensions and must not be defined in the wiki.
	 */
	private final Set<Identifier> occupiedTerms = ConcurrentHashMap.newKeySet();

	/**
	 * Environment-scoped services, see {@link #getService(Class, Supplier)}. Insertion ordered, so they can be closed
	 * in reverse order.
	 */
	private final Map<Class<?>, Object> services = Collections.synchronizedMap(new LinkedHashMap<>());

	/**
	 * Tasks to run when this environment is closed, see {@link #onClose(Runnable)}.
	 */
	private final Deque<Runnable> closeTasks = new ConcurrentLinkedDeque<>();

	/**
	 * Set when {@link #close()} starts; services stay available while closing, so cleanup code can still use them.
	 */
	private volatile boolean closing = false;

	/**
	 * Set when {@link #close()} has released the services; from then on no service can be provided anymore.
	 */
	private volatile boolean closed = false;

	/**
	 * The plugged (global) compilers of this environment, instantiated for this environment only.
	 */
	private final List<Compiler> pluggedCompilers = new ArrayList<>();

	/**
	 * The plugged event listeners this environment registered at the (JVM-wide) event manager; unregistered on close.
	 */
	private final List<EventListener> pluggedEventListeners = new ArrayList<>();

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
	 * Returns the singleton environment of this JVM, or null (logging an error) if none has been initialized.
	 *
	 * @deprecated The environment is on its way to become an ordinary instance that is reached through the objects at
	 * hand instead of a static accessor. Use {@link ArticleManager#getEnvironment()}, which is available from every
	 * {@link de.knowwe.core.kdom.parsing.Section#getArticleManager() Section}, {@link Article#getArticleManager()
	 * Article}, {@link UserContext#getArticleManager() UserContext} and (via its {@link CompilerManager}) every
	 * {@link Compiler}. See {@code doc/environment-instance-plan.md} (KnowWE repository root) for the migration plan.
	 */
	@Deprecated
	public static synchronized Environment getInstance() {
		if (instance == null) {
			LOGGER.error("Environment was not instantiated!");
		}
		return instance;
	}

	/**
	 * Returns the singleton environment of this JVM, or null if there is none, without logging.
	 *
	 * @deprecated transitional accessor for code that has no context object at hand yet; it goes away together with
	 * {@link #getInstance()}. Prefer {@link KnowWEUtils#findEnvironment(Section)} and its siblings.
	 */
	@Deprecated
	@Nullable
	public static synchronized Environment peekInstance() {
		return instance;
	}

	/**
	 * Returns the singleton environment of this JVM.
	 *
	 * @throws IllegalStateException if no environment is initialized
	 * @deprecated transitional accessor for code that has no context object at hand yet; it goes away together with
	 * {@link #getInstance()}. Prefer {@link KnowWEUtils#getEnvironment(Section)} and its siblings.
	 */
	@Deprecated
	@NotNull
	public static synchronized Environment requireInstance() {
		if (instance == null) throw new IllegalStateException("No KnowWE environment is initialized");
		return instance;
	}

	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Creates and initializes the singleton environment for the specified wiki connector. A previously initialized
	 * environment is closed first, so that there is never more than one current environment.
	 */
	public static void initInstance(WikiConnector wiki) {
		LOGGER.info("STARTING TO INITIALIZE KNOWWE ENVIRONMENT");
		Environment environment;
		synchronized (Environment.class) {
			Environment previous = instance;
			if (previous != null) {
				LOGGER.warn("An environment is still active while a new one is initialized, closing the old one first");
				previous.close();
			}
			initialized = false;
			environment = new Environment(wiki);
			instance = environment;
		}
		try {
			environment.init();
		}
		catch (RuntimeException | Error e) {
			synchronized (Environment.class) {
				if (instance == environment) instance = null;
			}
			throw e;
		}
		initialized = true;
		EventManager.getInstance().fireEvent(new InitEvent());

		LOGGER.info("INITIALIZED KNOWWE ENVIRONMENT");
	}

	/**
	 * private constructor
	 *
	 * @param wiki Connector to the used core wiki engine
	 * @see #getInstance()
	 */
	private Environment(WikiConnector wiki) {
		this.wikiConnector = wiki;
		wiki.setEnvironment(this);
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
			initScriptManagers();

			initInstantiations();
			initBlockedTools();
			Plugins.initJS();
			Plugins.initJsModules();
			Plugins.initCSS();
		}
		catch (Throwable e) {
			String msg = "Invalid initialization of the wiki. This is caused by an invalid wiki plugin. "
					+ "Wiki is in unstable state. Please exit and correct before using the wiki.";
			LOGGER.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	/**
	 * Closes this environment: waits for running compilations to terminate, notifies all {@link DeInitEvent}
	 * listeners, removes the compilers and stops the compile threads of all article managers, runs the
	 * {@link #onClose(Runnable) close tasks} in reverse order of their registration, closes all {@link AutoCloseable}
	 * {@link #getService(Class, Supplier) services} and finally detaches the singleton instance if this environment is
	 * the current one. Everything this
	 * environment owns becomes garbage afterwards; nothing has to be reset by hand. Calling the method twice is
	 * harmless.
	 * <p>
	 * A fresh environment may be started in the same JVM afterwards, e.g. by tests.
	 */
	@Override
	public void close() {
		synchronized (this) {
			if (closing) return;
			closing = true;
		}
		LOGGER.info("SHUTTING DOWN KNOWWE ENVIRONMENT");
		synchronized (Environment.class) {
			if (instance == this) initialized = false;
		}
		awaitCompileTerminationForShutdown();
		EventManager.getInstance().fireEvent(new DeInitEvent());
		// the plugged listeners belong to this environment's lifecycle; the event manager itself is JVM-wide
		for (EventListener listener : pluggedEventListeners) {
			EventManager.getInstance().unregister(listener);
		}
		pluggedEventListeners.clear();
		// stop the compilers first: destroying their content may still use services (e.g. the message index)
		for (ArticleManager manager : articleManagers.values()) {
			manager.getCompilerManager().shutdown();
			if (manager instanceof DefaultArticleManager defaultManager) {
				defaultManager.getAttachmentManager().shutdown();
			}
		}
		runCloseTasks();
		closeServices();
		closed = true;
		// release what is owned by this environment anyway, to make leaks through stale references less harmful
		scriptManagers.clear();
		sectionizerModules.clear();
		sectionRegistry.clear();
		occupiedTerms.clear();
		additionalRenderer.clear();
		synchronized (Environment.class) {
			if (instance == this) instance = null;
		}
		LOGGER.info("KNOWWE ENVIRONMENT SHUT DOWN");
	}

	/**
	 * @deprecated use {@link #close()}
	 */
	@Deprecated
	public void shutdown() {
		close();
	}

	/**
	 * Returns whether {@link #close()} has been called on this environment.
	 */
	public boolean isClosed() {
		return closing;
	}

	private void awaitCompileTerminationForShutdown() {
		for (ArticleManager manager : articleManagers.values()) {
			try {
				manager.getCompilerManager().awaitTermination();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.warn("Interrupted while waiting for compilation of web '{}' to terminate during shutdown", manager.getWeb());
				return;
			}
		}
	}

	private void runCloseTasks() {
		Runnable task;
		while ((task = closeTasks.pollLast()) != null) {
			try {
				task.run();
			}
			catch (RuntimeException e) {
				LOGGER.error("Close task of environment failed", e);
			}
		}
	}

	private void closeServices() {
		List<Object> ordered;
		synchronized (services) {
			ordered = new ArrayList<>(services.values());
			services.clear();
		}
		Collections.reverse(ordered);
		for (Object service : ordered) {
			if (service instanceof AutoCloseable closeable) {
				try {
					closeable.close();
				}
				catch (Exception e) {
					LOGGER.error("Closing service {} of environment failed", service.getClass().getName(), e);
				}
			}
		}
	}

	private void initEventManager() {
		// get all EventListeners
		List<Extension> extensions = new ArrayList<>(Arrays.asList(PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_EventListener)));
		for (Extension extension : extensions) {
			Object o = extension.getSingleton();
			if (o instanceof EventListener listener) {
				EventManager.getInstance().registerListener(listener);
				pluggedEventListeners.add(listener);
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

		// the plugged compilers are instantiated for this environment; they hold the state of the wiki
		for (PriorityList.Group<Double, Compiler> priorityGroup : Plugins.getCompilers().getPriorityGroups()) {
			List<Compiler> compilers = priorityGroup.getElements();
			for (Compiler compiler : compilers) {
				pluggedCompilers.add(compiler);
				compilerManager.addCompiler(priorityGroup.getPriority(), compiler);
			}
		}

		// we want this compiler to run after all package compilers
		compilerManager.addCompiler(10000, new PackageUnregistrationCompiler(packageRegistrationCompiler));
	}

	/**
	 * Given that all the wiki content has changed for some reason,
	 * this resets all content incl. the wiki engine and recompiles.
	 *
	 */
	public void reinitializeForNewWikiContent() throws IOException {
		EventManager.getInstance().fireEvent(new WikiContentReplacedEvent());

		articleManager().removeAllArticles();

		awaitCompileTermination();

		getWikiConnector().reinitializeWikiEngine();
	}

	private void awaitCompileTermination() {
		try {
			articleManager().getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			String message = "Compilation interrupted: " + e.getMessage();
			LOGGER.error(message);
			throw new RuntimeException(e);
		}
	}

	public ArticleManager getArticleManager() {
		return articleManager();
	}

	private ArticleManager articleManager() {
		return getArticleManager(DEFAULT_WEB);
	}

	public RootType getRootType() {
		return rootType;
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
		return (compiler instanceof DefaultGlobalCompiler) || pluggedCompilers.contains(compiler);
	}

	private void initProperties() {

		setCompilationMode();

		configureLogging();
	}

	public Article updateArticle(String title, String author, String content, boolean fullParse, UserContext context) throws InterruptedException,
			UpdateNotAllowedException {

		ArticleManager articleManager = getDefaultArticleManager();
		String originalText = "";
		Article article = articleManager.getArticle(title);
		if (article != null) {
			originalText = article.getRootSection().getText();
		}

		String cleanedContent = Article.cleanupText(content);
		boolean contentChange = !originalText.equals(cleanedContent);
		if (contentChange || fullParse) {
			if (contentChange && !getWikiConnector().userCanEditArticle(title, context)) {
				throw new UpdateNotAllowedException();
			}

			CompilerManager compilerManager = articleManager.getCompilerManager();
			if (compilerManager.isCompiling(title)) {
				// it is possible, that compilation of this article was triggered independently of calling this
				// method...
				compilerManager.awaitTermination();
			}
			ArticleUpdateEvent event = new ArticleUpdateEvent(title, author);
			int latestVersion = getLatestVersionNumber(title);
			event.setVersion(new ArticleUpdateEvent.Version(latestVersion));
			EventManager.getInstance().fireEvent(event);
			articleManager.open();
			try {
				articleManager.getCompilerManager()
						.setCompileMessage("Reason: saving page " + title + ", user: " + author);
				// create article with the new content
				article = this.getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, cleanedContent);
				if (fullParse) EventManager.getInstance().fireEvent(new FullParseEvent(article, author));
			}
			finally {
				articleManager.commit();
			}
			compilerManager.awaitTermination();
		}
		return article;
	}

	/**
	 * @deprecated use {@link #getLatestVersionNumber(String)} of the environment at hand
	 */
	@Deprecated
	public static int retrieveLatestVersionNumber(String title) {
		return requireInstance().getLatestVersionNumber(title);
	}

	/**
	 * Returns the latest version number of the specified article in the wiki, or -1 if the wiki has no history for it.
	 */
	public int getLatestVersionNumber(String title) {
		try {
			List<WikiPageInfo> articleHistory = getWikiConnector().getArticleHistory(title);
			if (articleHistory == null || articleHistory.isEmpty()) {
				LOGGER.warn("Error occurred when retrieving wiki page history from WikiConnector: History is empty");
				return -1;
			}
			WikiPageInfo wikiPageInfo = articleHistory.get(0);
			return wikiPageInfo.getVersion();
		}
		catch (IOException e) {
			LOGGER.error("Error occurred when retrieving wiki page history from WikiConnector");
			throw new RuntimeException(e);
		}
	}

	private void configureLogging() {
		MDC.put("context", wikiConnector.getApplicationName());

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
				if ("loglvl".equals(logLevel)) {
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
				LOGGER.error("Failed to set LogLevel ", ioe);
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
			inst.init(this);
		}
	}

	private void initBlockedTools() {
		// first check for toolmenu settings file in wiki-content
		String sourceFolder = getWikiConnector().getWikiProperty("var.basedir");
		if (sourceFolder != null) {
			File jsonFile = new File(sourceFolder, ToolUtils.SETTINGS_FILE);
			if (jsonFile.isFile()) {
				ToolUtils.initSettings(this, jsonFile);
			}
		}

		// additionally load all json files in 'KnowWEExtension/toolmenu' folder in webapp
		File appRoot = new File(KnowWEUtils.getApplicationRootPath(), "KnowWEExtension/toolmenu");
		File[] jsonFiles = appRoot.listFiles(f -> f.isFile() && f.getName().endsWith(".json"));
		if (jsonFiles != null) {
			for (File jsonFile : jsonFiles) {
				ToolUtils.initSettings(this, jsonFile);
			}
		}
	}

	private void initTagHandler() {
		for (TagHandler tagHandler : Plugins.getTagHandlers()) {
			initTagHandler(tagHandler);
		}
	}

	private void initSectionizerModules() {
		sectionizerModules.addAll(Plugins.getSectionizerModules());
	}

	/**
	 * Marks the script managers as initialized, after all types of the type tree are decorated and have registered
	 * their compile scripts. This is when the parent/child links of the types are complete, which the managers need to
	 * know which subtrees can have scripts at all.
	 */
	private void initScriptManagers() {
		for (ScriptManager<?> scriptManager : scriptManagers.values()) {
			scriptManager.initialize();
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
	 * this a breath-first-search is used. Thus, each item is initialized with the shortest path towards it.
	 *
	 * @created 24.10.2013
	 */
	private void decorateTypeTree() {

		// check whether the priorities between plugged types lead to deterministic structure of the type tree
		Plugins.checkTypePriorityClarity();

		// queue the queue of paths to be initialized
		rootType = new RootType();
		LinkedList<Type[]> queue = new LinkedList<>();
		queue.add(new Type[] { rootType });

		// queuedTypes the already queued instances
		Set<Type> queuedTypes = Collections.newSetFromMap(new IdentityHashMap<>());
		queuedTypes.add(rootType);

		while (!queue.isEmpty()) {
			// initialize the first element of the queue
			// until the queue is empty
			Type[] path = queue.pop();
			Type type = path[path.length - 1];

			// initialize plugged items
			Plugins.addChildrenTypesToType(type, path);
			Plugins.addAnnotations(type, path);
			Plugins.addRendererToType(this, type, path);
			Plugins.addCompileScriptsToType(type, path);

			// initialize type itself
			type.init(path);

			// index the compile scripts of the type for the compilers of this environment
			if (type instanceof AbstractType abstractType) {
				abstractType.registerCompileScripts(this);
			}

			// recurse for child types
			for (Type childType : type.getChildrenTypes()) {
				// secure against malformed plugins
				if (childType == null) {
					throw new NullPointerException("type '" + type.getClass().getName() + "' contains 'null' as " +
							"child");
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
			LOGGER.warn("TagHandler for tag '" + tagName + "' had already been added.");
		}
		else {
			this.tagHandlers.put(key, tagHandler);
		}
	}

	public WikiConnector getWikiConnector() {
		return this.wikiConnector;
	}

	/**
	 * Returns the registry of the sections of this environment by their ids.
	 */
	@NotNull
	public SectionRegistry getSectionRegistry() {
		return sectionRegistry;
	}

	/**
	 * Returns the section of this environment with the specified id, or null if there is none.
	 *
	 * @see Sections#get(String)
	 */
	@Nullable
	public Section<?> getSection(String id) {
		return sectionRegistry.get(id);
	}

	/**
	 * Returns the script manager of this environment for the specified compiler class, holding the compile scripts of
	 * this environment's type tree for that class of compilers.
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public <C extends Compiler> ScriptManager<C> getScriptManager(@NotNull Class<C> compilerClass) {
		return (ScriptManager<C>) scriptManagers.computeIfAbsent(compilerClass, c -> new ScriptManager<>(compilerClass));
	}

	/**
	 * Returns all script managers of this environment.
	 */
	@NotNull
	public Collection<ScriptManager<? extends Compiler>> getScriptManagers() {
		return Collections.unmodifiableCollection(scriptManagers.values());
	}

	/**
	 * Returns the plugged sectionizer modules of this environment, in the order they are tried.
	 */
	@NotNull
	public List<SectionizerModule> getSectionizerModules() {
		return Collections.unmodifiableList(sectionizerModules);
	}

	/**
	 * Registers an additional sectionizer module for this environment (the plugged ones are registered on
	 * initialization).
	 */
	public void addSectionizerModule(@NotNull SectionizerModule module) {
		sectionizerModules.add(module);
	}

	/**
	 * Registers the terms of the specified terminology extension as occupied, so they cannot be defined in the wiki.
	 */
	public void registerOccupiedTerms(@NotNull TerminologyExtension extension) {
		for (String termName : extension.getTermNames()) {
			occupiedTerms.add(Identifier.fromExternalForm(termName));
		}
	}

	/**
	 * Returns whether the specified term is occupied by a plugged terminology extension.
	 */
	public boolean isOccupiedTerm(@NotNull Identifier identifier) {
		return occupiedTerms.contains(identifier);
	}

	/**
	 * Returns the service of the specified type that is scoped to this environment, creating it with the specified
	 * factory on first access. This is the place for everything that would otherwise be kept in a static field of a
	 * plugin or core class: caches, registries, thread pools, schedulers. If the service implements
	 * {@link AutoCloseable}, it is closed when this environment is {@link #close() closed}, in reverse order of
	 * creation.
	 *
	 * @param type    the type of the service, also used as key
	 * @param factory creates the service if it does not exist yet
	 * @return the service instance of this environment
	 * @throws IllegalStateException if the environment is already closed
	 */
	@NotNull
	public <T> T getService(@NotNull Class<T> type, @NotNull Supplier<? extends T> factory) {
		if (closed) throw new IllegalStateException("Environment is closed, cannot provide service " + type.getName());
		synchronized (services) {
			Object service = services.get(type);
			if (service == null) {
				service = Objects.requireNonNull(factory.get(), () -> "factory for " + type.getName() + " returned null");
				services.put(type, service);
			}
			return type.cast(service);
		}
	}

	/**
	 * Returns the service of the specified type if it has already been created for this environment, or null.
	 */
	@Nullable
	public <T> T getService(@NotNull Class<T> type) {
		return type.cast(services.get(type));
	}

	/**
	 * Registers a task to be run when this environment is {@link #close() closed}. Tasks run in reverse order of
	 * registration, before the services are closed. Use it to stop threads, timers and listeners that were started on
	 * behalf of this environment.
	 *
	 * @throws IllegalStateException if the environment is already closed
	 */
	public void onClose(@NotNull Runnable task) {
		if (closing) throw new IllegalStateException("Environment is closed, cannot register close task");
		closeTasks.addLast(task);
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
		return this.articleManagers.computeIfAbsent(web, k -> new DefaultArticleManager(this, web));
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
