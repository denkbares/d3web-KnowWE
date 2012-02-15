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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.plugin.JPFPluginManager;
import de.d3web.plugin.Plugin;
import de.d3web.plugin.PluginManager;
import de.d3web.plugin.Resource;
import de.knowwe.core.action.KnowWEActionDispatcher;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.packaging.KnowWEPackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sectionizer;
import de.knowwe.core.kdom.parsing.SectionizerModule;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.taghandler.TagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.user.UserSettingsManager;
import de.knowwe.core.utils.KnowWETypeSet;
import de.knowwe.core.utils.KnowWETypeUtils;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.KnowWEWikiConnector;
import de.knowwe.event.InitEvent;
import de.knowwe.knowRep.KnowledgeRepresentationHandler;
import de.knowwe.knowRep.KnowledgeRepresentationManager;
import de.knowwe.plugin.Instantiation;
import de.knowwe.plugin.Plugins;
import dummies.KnowWETestWikiConnector;

/**
 * @author Jochen
 * 
 *         This is the core class of KnowWE2. It manages the ArticleManager(s)
 *         and provides methods to access KnowWE-Articles, KnowWE-Modules and
 *         Parse-reports. Further it is connected to the used Wiki-engine,
 *         holding an instance of KnowWEWikiConnector and allows page saves.
 * 
 * 
 */

public class KnowWEEnvironment {

	// private KnowWETopicLoader topicLoader;

	private RootType rootTypes;
	private List<Type> globalTypes = new ArrayList<Type>();
	private List<PageAppendHandler> appendHandlers = new ArrayList<PageAppendHandler>();

	public List<PageAppendHandler> getAppendHandlers() {
		return appendHandlers;
	}

	public List<Type> getGlobalTypes() {
		return globalTypes;
	}

	private List<Type> allKnowWETypes;

	/**
	 * default paths, used only, if null is given to the constructor as path
	 * should NOT BE USED, path a read from the KnowWE_config properties file.
	 */
	private String knowweExtensionPath = "/var/lib/tomcat-6/webapps/JSPWiki/KnowWEExtension/";

	/**
	 * @return the defaultModulesPath
	 */
	public String getKnowWEExtensionPath() {
		return knowweExtensionPath;
	}

	/**
	 * An article manager for each web. In case of JSPWiki there is only on web
	 * ('default_web')
	 */
	private final Map<String, KnowWEArticleManager> articleManagers = new HashMap<String, KnowWEArticleManager>();

	/**
	 * A knowledge manager for each web. In case of JSPWiki there is only on web
	 * ('default_web')
	 */
	private final Map<String, KnowledgeRepresentationManager> knowledgeManagers = new HashMap<String, KnowledgeRepresentationManager>();

	/**
	 * A package manager for each web. In case of JSPWiki there is only on web
	 * ('default_web')
	 */
	private final Map<String, KnowWEPackageManager> packageManagers = new HashMap<String, KnowWEPackageManager>();

	/**
	 * A terminology handler for each web and article. In case of JSPWiki there
	 * is only on web ('default_web')
	 */
	private final Map<String, Map<String, TerminologyManager>> terminologyHandlers = new HashMap<String, Map<String, TerminologyManager>>();

	/**
	 * This is the link to the connected Wiki-engine. Allows saving pages etc.
	 */
	private KnowWEWikiConnector wikiConnector = null;

	/**
	 * holding the default tag handlers of KnowWE2
	 * 
	 * @see renderTags
	 */
	private final HashMap<String, TagHandler> tagHandlers = new HashMap<String, TagHandler>();

	/**
	 * grants access on the default tag handlers of KnowWE2
	 * 
	 * @return HashMap holding the default tag handlers of KnowWE2
	 */
	public HashMap<String, TagHandler> getDefaultTagHandlers() {
		return tagHandlers;
	}

	public ResourceBundle getKwikiBundle() {

		return ResourceBundle.getBundle("KnowWE_messages");
	}

	public ResourceBundle getKwikiBundle(UserContext user) {

		Locale.setDefault(wikiConnector.getLocale(user.getRequest()));
		return this.getKwikiBundle();
	}

	public ResourceBundle getKwikiBundle(HttpServletRequest request) {

		Locale.setDefault(wikiConnector.getLocale(request));
		return this.getKwikiBundle();
	}

	/**
	 * Hard coded name of the default web
	 */
	public static final String DEFAULT_WEB = "default_web";

	/**
	 * This is used to mask HTML-syntax (and Markup, that collides with the
	 * wiki-core-markup) Necessary to enable HTML-generation in the
	 * pretranslate-hook, because the wiki engine (with HTML-diabled) escapes
	 * HTML So HTML needs to be masked with these replacements and are unmasked
	 * in the posttranslate-hook.
	 */
	public static final String HTML_DOUBLEQUOTE = "KNOWWEHTML_DOUBLEQUOTE";
	public static final String HTML_GT = "KNOWWEHTML_GREATERTHAN";
	public static final String HTML_ST = "KNOWWEHTML_SMALLERTHAN";
	public static final String HTML_QUOTE = "KNOWWEHTML_QUOTE";
	public static final String HTML_BRACKET_OPEN = "KNOWWE_BRACKET_OPEN";
	public static final String HTML_BRACKET_CLOSE = "KNOWWE_BRACKET_CLOSE";
	public static final String HTML_PLUGIN_BRACKETS_OPEN = "KNOWEPLUGIN_BRACKETS_OPEN";
	public static final String HTML_PLUGIN_BRACKETS_CLOSE = "KNOWEPLUGIN_BRACKETS_CLOSE";
	public static final String HTML_CURLY_BRACKET_OPEN = "KNOWWE_CURLY_BRACKET_OPEN";
	public static final String HTML_CURLY_BRACKET_CLOSE = "KNOWWE_CURLY_BRACKET_CLOSE";

	public static final String NEWLINE = "KNOWWE_NEWLINE";

	/**
	 * Name of the WikiFindings-page
	 */
	public static final String WIKI_FINDINGS = "WikiFindings";

	public static final boolean GLOBAL_TYPES_ENABLED = true;

	public enum CompilationMode {
		INCREMENTAL, DEFAULT
	}

	private CompilationMode currentCompilationMode = CompilationMode.DEFAULT;

	public void setCompilationMode(CompilationMode mode) {
		currentCompilationMode = mode;
	}

	public CompilationMode getCompilationMode() {
		return currentCompilationMode;
	}

	/**
	 * Singleton instance
	 */
	private static KnowWEEnvironment instance;

	/**
	 * Singleton lazy factory
	 */
	public static synchronized KnowWEEnvironment getInstance() {
		if (instance == null) {
			Logger.getLogger("KnowWE2").severe(
					"KnowWEEnvironment was not instantiated!");
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
		}
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static boolean isInitialized() {
		return !(instance == null);
	}

	public UserSettingsManager getUserSettingsManager() {
		return UserSettingsManager.getInstance();
	}

	public static void initKnowWE(KnowWEWikiConnector wiki) {
		instance = new KnowWEEnvironment(wiki);
		instance.initModules(wiki.getServletContext(), DEFAULT_WEB, wiki);

		// firing the init event
		EventManager.getInstance().fireEvent(InitEvent.getInstance());

	}

	/**
	 * Returns the KnowWEArticle object for a given web and pagename
	 * 
	 * @param web
	 * @param topic
	 * @return
	 */
	public KnowWEArticle getArticle(String web, String title) {
		return getArticleManager(web).getArticle(title);
	}

	/**
	 * returns the ArtilceManager for a given web
	 * 
	 * @param web
	 * @return
	 */
	public KnowWEArticleManager getArticleManager(String web) {
		KnowWEArticleManager mgr = this.articleManagers.get(web);
		if (mgr == null) {
			mgr = new KnowWEArticleManager(this, web);
			articleManagers.put(web, mgr);
		}
		return mgr;
	}

	public KnowledgeRepresentationManager getKnowledgeRepresentationManager(String web) {
		KnowledgeRepresentationManager mgr = this.knowledgeManagers.get(web);
		if (mgr == null) {
			mgr = new KnowledgeRepresentationManager(web);
			knowledgeManagers.put(web, mgr);
		}
		return mgr;
	}

	/**
	 * returns the PackageManager for a given web
	 * 
	 * @param web
	 * @return
	 */
	public KnowWEPackageManager getPackageManager(String web) {
		KnowWEPackageManager mgr = this.packageManagers.get(web);
		if (mgr == null) {
			mgr = new KnowWEPackageManager(web);
			packageManagers.put(web, mgr);
		}
		return mgr;
	}

	/**
	 * returns the TerminologyHandler for a given web
	 * 
	 * @param web
	 * @return
	 */
	public TerminologyManager getTerminologyHandler(String web, String title) {
		Map<String, TerminologyManager> handlersOfWeb = this.terminologyHandlers.get(web);
		if (handlersOfWeb == null) {
			handlersOfWeb = new HashMap<String, TerminologyManager>();
			this.terminologyHandlers.put(web, handlersOfWeb);
		}
		TerminologyManager mgr = handlersOfWeb.get(title);
		if (mgr == null) {
			mgr = new TerminologyManager(web, title);
			handlersOfWeb.put(title, mgr);
		}
		return mgr;
	}

	/**
	 * private contructor
	 * 
	 * @see getInstance()
	 * 
	 * @param wiki Connector to the used core wiki engine
	 */
	/**
	 * @param wiki
	 */
	private KnowWEEnvironment(KnowWEWikiConnector wiki) {
		try {
			this.wikiConnector = wiki;

			System.out.println("INITIALISING KNOWWE ENVIRONMENT...");
			ResourceBundle bundle = ResourceBundle.getBundle("KnowWE_config");
			if (bundle != null) {
				if (!(wiki instanceof KnowWETestWikiConnector)) {
					// convert the $web_app$-variable from the resourcebundle
					// defaultJarsPath = KnowWEUtils.getRealPath(context, bundle
					// .getString("path_to_jars"));

					knowweExtensionPath = KnowWEUtils.getRealPath(wikiConnector
							.getServletContext(), bundle
							.getString("path_to_knowweextension"));
				}
				if (bundle.getString("compilation.mode").contains("incremental")) {
					this.setCompilationMode(CompilationMode.INCREMENTAL);
				}
			}
			if (wiki instanceof KnowWETestWikiConnector) {
				KnowWETestWikiConnector connector = (KnowWETestWikiConnector) wiki;
				knowweExtensionPath = connector.getHackedPath();
			}

			rootTypes = RootType.getInstance();

			System.out.println("INITIALISED KNOWWE ENVIRONMENT");
		}
		catch (Exception e) {
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			Logger.getLogger("KnowWE").log(Level.SEVERE,
					"unexpected exception during KnowWE initialization", e);
		}

	}

	/**
	 * Initializes the KnowWE modules
	 */
	private void initModules(ServletContext context, String web, KnowWEWikiConnector wiki) {
		// add the default modules
		// modules.add(new de.d3web.we.dom.kopic.KopicModule());

		File libDir = new File(knowweExtensionPath + "/../WEB-INF/lib");
		// when testing, libDir doesn't exist, but the pluginframework is
		// initialised
		// in junittest, so there is no problem
		// if libDir is doesn't exist in runtime, nothing will work, so this
		// code won't be reached ;-)
		if (libDir.exists()) {
			List<File> pluginFiles = new ArrayList<File>();
			for (File file : libDir.listFiles()) {
				if (file.getName().contains("KnowWE-Plugin-")
						|| file.getName().contains("d3web-Plugin-")) {
					pluginFiles.add(file);
				}
			}
			JPFPluginManager.init(pluginFiles.toArray(new File[pluginFiles.size()]));

			Plugin[] plugins = PluginManager.getInstance().getPlugins();

			for (Plugin p : plugins) {
				Resource[] resources = p.getResources();
				for (Resource r : resources) {
					String pathName = r.getPathName();
					if (!pathName.endsWith("/") && pathName.startsWith("webapp/")) {
						pathName = pathName.substring("webapp/".length());
						try {
							File file = new File(
									new File(knowweExtensionPath).getParentFile().getCanonicalPath()
											+ "/" + pathName);
							File parent = file.getParentFile();
							if (!parent.isDirectory()) {
								parent.mkdirs();
							}
							FileOutputStream out = new FileOutputStream(file);
							InputStream in = r.getInputStream();
							try {
								stream(in, out);
							}
							finally {
								in.close();
								out.close();
							}
						}
						catch (IOException e) {
							throw new InstantiationError(
									"Cannot instantiate plugin "
											+ p
											+ ", the following error occured while extracting its resources: "
											+ e.getMessage());
						}
					}
				}
			}
		}

		// List<ISemanticCore> sclist = Plugins.getSemanticCoreImpl();
		// if (sclist.size() == 1) {
		// SemanticCoreDelegator.setImpl(sclist.get(0));
		// }
		// else {
		// for (ISemanticCore cur : sclist) {
		// if (!cur.getClass().toString().contains("Dummy")) {
		// SemanticCoreDelegator.setImpl(cur);
		// }
		// }
		// }
		//
		// SemanticCoreDelegator.initImpl(this);

		for (Instantiation inst : Plugins.getInstantiations()) {
			inst.init(context);
		}

		for (TagHandler tagHandler : Plugins.getTagHandlers()) {
			initTagHandler(tagHandler);
		}

		for (SectionizerModule sm : Plugins.getSectionizerModules()) {
			Sectionizer.registerSectionizerModule(sm);
		}

		appendHandlers = Plugins.getPageAppendHandlers();

		for (Type type : Plugins.getRootTypes()) {
			addRootType(type);
		}
		this.globalTypes = Plugins.getGlobalTypes();
		KnowledgeRepresentationManager manager = this.getKnowledgeRepresentationManager(web);
		for (KnowledgeRepresentationHandler handler : Plugins.getKnowledgeRepresentationHandlers()) {
			handler.setWeb(web);
			manager.registerHandler(handler);
		}

		Plugins.initJS();
		Plugins.initCSS();
	}

	private void initTagHandler(TagHandler tagHandler) {
		String tagName = tagHandler.getTagName();
		String key = tagName.toLowerCase();

		if (tagHandlers.containsKey(key)) {
			Logger.getLogger(this.getClass().getName()).warning(
					"TagHandler for tag '" + tagName
							+ "' had already been added.");
		}
		else {
			this.tagHandlers.put(key, tagHandler);
		}
	}

	private boolean addRootType(Type type) {
		return RootType.getInstance().addChildType(type);
	}

	/**
	 * Getter for KnowWEWikiConnector
	 * 
	 * @return this.wikiConnector
	 */
	public KnowWEWikiConnector getWikiConnector() {
		return this.wikiConnector;
	}

	/**
	 * returns the ActionDispatcher from the WikiConnector (JSPWiki: used by
	 * KnowWE.jsp)
	 * 
	 * TODO factor out in KnowWE.jsp
	 * 
	 * @return
	 */
	public KnowWEActionDispatcher getDispatcher() {
		return wikiConnector.getActionDispatcher();
	}

	/**
	 * Builds an {@link KnowWEArticle} and registers it in the
	 * {@link KnowWEArticleManager}.
	 */
	public KnowWEArticle buildAndRegisterArticle(String content,
			String title, String web) {
		return buildAndRegisterArticle(content, title, web, false);
	}

	/**
	 * Builds an {@link KnowWEArticle} and registers it in the
	 * {@link KnowWEArticleManager}.
	 */
	public KnowWEArticle buildAndRegisterArticle(String content,
			String title, String web, boolean fullParse) {

		if (KnowWEArticle.isArticleCurrentlyBuilding(web, title)) {
			return getArticle(DEFAULT_WEB, title);
		}

		// create article with the new content
		KnowWEArticle article = KnowWEArticle.createArticle(content, title, KnowWEEnvironment
				.getInstance().getRootType(), web);

		this.getArticleManager(web).registerArticle(article);

		return article;
	}

	/**
	 * Called by the Core-Junit-Tests
	 * 
	 * @param username
	 * @param content
	 * @param topic
	 * @param web
	 * @return
	 */
	public void processAndUpdateArticleJunit(String username, String content,
			String topic, String web, RootType rootType) {
		this.rootTypes = rootType;
		this.articleManagers.get(web).registerArticle(
				KnowWEArticle.createArticle(content, topic, rootType, web));
	}

	public ServletContext getContext() {
		return wikiConnector.getServletContext();
	}

	/**
	 * Knowledge Services (Kopic) needs to have an id. This is how a default id
	 * is generated when users dont enter one.
	 * 
	 * @param title
	 * @return
	 */
	public static String generateDefaultID(String title) {
		return title;
	}

	public String getSectionText(String id) {
		Section<?> sec = Sections.getSection(id);
		String data = "Section not found: " + id;
		if (sec != null) {
			data = sec.getText();
		}
		return data;
	}

	public RootType getRootType() {
		return rootTypes;
	}

	/**
	 * Collects all Types.
	 * 
	 * @return
	 */
	public List<Type> getAllTypes() {

		if (this.allKnowWETypes == null) {
			KnowWETypeSet allTypes = new KnowWETypeSet();

			KnowWETypeSet s = KnowWETypeUtils
					.getAllChildrenTypesRecursive(getRootType(),
							new KnowWETypeSet());
			allTypes.addAll(s.toList());

			this.allKnowWETypes = allTypes.toLexicographicalList();
		}

		return this.allKnowWETypes;
	}

	/**
	 * @See KnowWETypeBrowserAction
	 * 
	 * @param clazz
	 * @return
	 */
	public Type searchType(Class<? extends Type> clazz) {
		for (Type t : this.allKnowWETypes) {
			if (t.isType(clazz)) {
				return t;
			}
		}
		return null;
	}

	private static void stream(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}

}
