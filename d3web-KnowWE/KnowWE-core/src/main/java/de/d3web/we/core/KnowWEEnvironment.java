/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.core;

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
import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.event.ArticleCreatedEvent;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.InitEvent;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.RootType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.ConditionalRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.PageAppendHandler;
import de.d3web.we.search.MultiSearchEngine;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.utils.KnowWEObjectTypeSet;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;
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

	private KnowWEObjectType rootTypes;
	private List<KnowWEObjectType> globalTypes = new ArrayList<KnowWEObjectType>();
	private List<PageAppendHandler> appendHandlers = new ArrayList<PageAppendHandler>();

	public List<PageAppendHandler> getAppendHandlers() {
		return appendHandlers;
	}

	public List<KnowWEObjectType> getGlobalTypes() {
		return globalTypes;
	}

	private List<KnowWEObjectType> allKnowWEObjectTypes;

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
	 * An knowledge manager for each web. In case of JSPWiki there is only on
	 * web ('default_web')
	 */
	private final Map<String, KnowledgeRepresentationManager> knowledgeManagers = new HashMap<String, KnowledgeRepresentationManager>();

	/**
	 * An include manager for each web. In case of JSPWiki there is only on web
	 * ('default_web')
	 */
	private final Map<String, KnowWEIncludeManager> includeManagers = new HashMap<String, KnowWEIncludeManager>();

	// /**
	// * The servlet context of the running application. Necessary to determine
	// * the path of the running app on the server
	// *
	// * TODO cant this be factored out?
	// */
	// private ServletContext context;

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

	public ResourceBundle getKwikiBundle(KnowWEUserContext user) {

		Locale.setDefault(wikiConnector.getLocale(user.getHttpRequest()));
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
		instance.initModules(wiki.getServletContext(), DEFAULT_WEB);

		// firing the init event
		EventManager.getInstance().fireEvent("system init", null, InitEvent.getInstance());

	}

	public boolean registerConditionalRendererToType(Class<? extends KnowWEObjectType> clazz,
			KnowWEDomRenderer renderer) {
		List<KnowWEObjectType> instances = KnowWEEnvironment.getInstance()
				.searchTypeInstances(clazz);

		for (KnowWEObjectType annoType : instances) {
			if (annoType.getRenderer() instanceof ConditionalRenderer) {
				((ConditionalRenderer) annoType.getRenderer())
						.addConditionalRenderer(renderer);
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the KnowWEArticle object for a given web and pagename
	 *
	 * @param web
	 * @param topic
	 * @return
	 */
	public KnowWEArticle getArticle(String web, String topic) {
		return getArticleManager(web).getArticle(topic);
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
	 * returns the ArtilceManager for a given web
	 *
	 * @param web
	 * @return
	 */
	public KnowWEIncludeManager getIncludeManager(String web) {
		KnowWEIncludeManager mgr = this.includeManagers.get(web);
		if (mgr == null) {
			mgr = new KnowWEIncludeManager(web);
			includeManagers.put(web, mgr);
		}
		return mgr;
	}

	public static final String EVENT_INIT = "KnowWE initialization";

	/**
	 * private contructor
	 *
	 * @see getInstance()
	 *
	 * @param wiki
	 *            Connector to the used core wiki engine
	 */
	/**
	 * @param wiki
	 */
	private KnowWEEnvironment(KnowWEWikiConnector wiki) {
		try {
			this.wikiConnector = wiki;

			System.out.println("INITIALISING KNOWWE ENVIRONMENT...");
			ResourceBundle bundle = ResourceBundle.getBundle("KnowWE_config");
			if (bundle != null && !(wiki instanceof KnowWETestWikiConnector)) {
				// convert the $web_app$-variable from the resourcebundle
				// defaultJarsPath = KnowWEUtils.getRealPath(context, bundle
				// .getString("path_to_jars"));

				knowweExtensionPath = KnowWEUtils.getRealPath(wikiConnector
						.getServletContext(), bundle
						.getString("path_to_knowweextension"));

			}
			if (wiki instanceof KnowWETestWikiConnector) {
				KnowWETestWikiConnector connector = (KnowWETestWikiConnector) wiki;
				String userdir = System.getProperty("user.dir");
				knowweExtensionPath = userdir + connector.getHackedPath();

			}

			rootTypes = RootType.getInstance();

			// adding TaggingMangler as SearchProvider to KnowWE-MultiSearch
			MultiSearchEngine.getInstance().addProvider(TaggingMangler.getInstance());

			// loadData(context);
			initWikiSolutionsPage();
			SemanticCore.getInstance(this); /// init lazy instance


			System.out.println("INITIALISED KNOWWE ENVIRONMENT");
		}
		catch (Exception e) {
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			System.out.println("*****EXCEPTION IN initKnowWE !!! *********");
			Logger.getLogger("KnowWE").log(Level.SEVERE, "unexpected exception during KnowWE initialization", e);
		}

	}

	/**
	 * Initialises the WikiSolutionPage --> creates it if not existing
	 */
	private void initWikiSolutionsPage() {

		if (this.wikiConnector.doesPageExist("WikiSolutions")) {
			writeNewSolutionsPage();
		}
	}

	/**
	 * creates a WikiSolution-page.
	 *
	 * @see WikiSolutionTagHandler
	 */
	private void writeNewSolutionsPage() {
		getWikiConnector().createWikiPage("WikiSolutions",
				"[{KnowWEPlugin WikiSolutions}]", "engine");
	}

	/**
	 * Initializes the KnowWE modules
	 */
	private void initModules(ServletContext context, String web) {
		// add the default modules
		// modules.add(new de.d3web.we.dom.kopic.KopicModule());

		File libDir = new File(knowweExtensionPath+"/../WEB-INF/lib");
		//when testing, libDir doesn't exist, but the pluginframework is initialised
		//in junittest, so there is no problem
		//if libDir is doesn't exist in runtime, nothing will work, so this code won't be reached ;-)
		if (libDir.exists()) {
			List<File> pluginFiles = new ArrayList<File>();
			for (File file: libDir.listFiles()) {
				if (file.getName().contains("jpf-plugin")) {
					pluginFiles.add(file);
				}
			}
			JPFPluginManager.init(pluginFiles.toArray(new File[pluginFiles.size()]));

			Plugin[] plugins = PluginManager.getInstance().getPlugins();

			for (Plugin p: plugins) {
				Resource[] resources = p.getResources();
				for (Resource r: resources) {
					String pathName = r.getPathName();
					if (!pathName.endsWith("/")&&pathName.startsWith("webapp/")) {
						pathName = pathName.substring("webapp/".length());
						try {
							File file = new File(new File(knowweExtensionPath).getParentFile().getCanonicalPath()+"/"+pathName);
							File parent = file.getParentFile();
							if (!parent.isDirectory()) {
								parent.mkdirs();
							}
							FileOutputStream out = new FileOutputStream(file);
							InputStream in = r.getInputStream();
							try {
								stream(in, out);
							} finally {
								in.close();
								out.close();
							}
						}
						catch (IOException e) {
							throw new InstantiationError("Cannot instantiate plugin "+p+", the following error occured while extracting its resources: "+e.getMessage());
						}
					}
				}
			}
		}


		for (Instantiation inst: Plugins.getInstantiations()) {
			inst.init(context);
		}

		for (TagHandler tagHandler: Plugins.getTagHandlers() ) {
			initTagHandler(tagHandler);
		}

		appendHandlers = Plugins.getPageAppendHandlers();

		for (KnowWEObjectType type: Plugins.getRootTypes()) {
			addRootType(type);
		}
		this.globalTypes=Plugins.getGlobalTypes();
		KnowledgeRepresentationManager manager = this.getKnowledgeRepresentationManager(web);
		for (KnowledgeRepresentationHandler handler: Plugins.getKnowledgeRepresentationHandlers()) {
			handler.setWeb(web);
			manager.registerHandler(handler);
		}



		Plugins.initJS();
	}

	private void initTagHandler(TagHandler tagHandler) {
		String tagName = tagHandler.getTagName();

		if (tagHandlers.containsKey(tagName)) {
			Logger.getLogger(this.getClass().getName()).warning(
					"TagHandler for tag '" + tagName
					+ "' had already been added.");
		}
		else {
			this.tagHandlers.put(tagName, tagHandler);
		}
	}

	private boolean addRootType(KnowWEObjectType type) {
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
	 * This delegates the JSPWiki execute method to all modules
	 */
	public String renderTags(Map<String, String> params, String topic,
			KnowWEUserContext user, String web) {

		// First asking KnowWEDefaultTagHandlers
		String key = params.get("_cmdline").split("=")[0].trim();
		if (this.tagHandlers.containsKey(key.toLowerCase())) {
			return this.tagHandlers.get(key.toLowerCase()).render(topic, user,
					params, web);
		}

		// // Then asking Modules in given order for TagHandlers
		// for (KnowWEModule modul : modules) {
		// String rendered = modul.renderTags(params, topic, user, web);
		// if (rendered != null && rendered.length() > 0)
		// return rendered;
		// }
		return "__tag not found by KnowWE/KnowWEModuls__";
	}

	// /**
	// * On KnowWE initialisation>
	// * Loads the knowledgebases into the distributed reasoning engine.
	// *
	// * @param context
	// */
	// private void loadData(ServletContext context) { ResourceBundle rb =
	// ResourceBundle.getBundle("KnowWE_config");
	// String webPath = rb.getString("knowwe.config.path.webs");
	// webPath = KnowWEUtils.getRealPath(context, webPath);
	// File path = new File(webPath);
	// if (!path.exists()) {
	// try {
	// System.err.println("trying to create kb dir:"+path.getAbsolutePath());
	// File dweb=new File(path+"/default_web");
	// dweb.mkdirs();
	// } catch (SecurityException e) {
	// System.err.println("KB directory creation failed, check permissions!!
	// path:" +path.getAbsolutePath());
	// e.printStackTrace();
	// System.exit(1);
	// }
	// }
	// WebEnvironmentManager.getInstance().setWebEnvironmentLocation(webPath);
	// File[] files = path.listFiles();
	// if (files!=null){
	// for (File each : files) {
	// if (each.isDirectory()) {
	// WebEnvironmentManager.getInstance().createEnvironment(
	// each.getName());
	// //initArticleManager(each.getName());
	//
	// }
	// }}
	//
	// }

	/**
	 * Replaced with introduction of DOM. Delete when DOM established
	 *
	 * @param user
	 * @param topic
	 * @param web
	 * @param text
	 * @return
	 */
	// @Deprecated
	// public SectionList getSectionedArticle(String user, String topic, String
	// web, String text) {
	// KnowWEArticle art = this.articleManagers.get(web).getArticle(topic);
	// if (art != null && art.getSections() != null) {
	// return art.getSections();
	// } else {
	// processAndUpdateArticle(user, text, topic, web);
	// }
	// return this.articleManagers.get(web).getArticle(topic).getSections();
	//
	// }

	public static final String EVENT_ARTICLE_CREATED = "article-created";

	/**
	 * Called from the Wikiplugin when article is saved. Parses and updates
	 * inner knowledge representation of KnowWE2
	 *
	 * @param username
	 * @param content
	 * @param topic
	 * @param web
	 * @return
	 */
	public String processAndUpdateArticle(String username, String content,
			String topic, String web) {

		// create article with the new content
		KnowWEArticle article = new KnowWEArticle(content, topic, KnowWEEnvironment
				.getInstance().getRootType(), web);

		// fire 'article-created' event
		EventManager.getInstance().fireEvent(username, article.getSection(),
				ArticleCreatedEvent.getInstance());

		return this.getArticleManager(web).saveUpdatedArticle(
				article).getHTML();
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
			String topic, String web, KnowWEObjectType rootType) {
		this.rootTypes = rootType;
		this.articleManagers.get(web).saveUpdatedArticle(
				new KnowWEArticle(content, topic, rootType, web));
	}

	public ServletContext getContext() {
		return wikiConnector.getServletContext();
	}

	/**
	 * Knowledge Services (Kopic) needs to have an id. This is how a default id
	 * is generated when users dont enter one.
	 *
	 * @param topic
	 * @return
	 */
	public static String generateDefaultID(String topic) {
		return topic + "_KB";
	}

	// /**
	// * Returns whether the last parse report of an artilce contains parsing
	// errors
	// *
	// * @param topic
	// * @param web
	// * @return
	// * @throws NoParseResultException
	// */
	// public boolean containsError(String topic, String web) throws
	// NoParseResultException {
	// KnowWEArticle article = this.articleManagers.get(web).getArticle(topic);
	// if(article == null) {
	// throw new NoParseResultException("Article not (yet) parsed!");
	// }
	// KopicParseResult res = article.getLastParseResult();
	// if(res == null) {
	// throw new NoParseResultException("Article not (yet) parsed!");
	// }
	// return res.hasErrors();
	// }

	/**
	 *
	 * Writes a modified article to the wiki engine using the wikiConnector
	 *
	 * @param web
	 * @param name
	 * @param articleText
	 * @param map
	 * @return
	 */
	public boolean saveArticle(String web, String name, String articleText,
			KnowWEParameterMap map) {
		return this.wikiConnector.saveArticle(name, articleText, map);

	}

	/**
	 *
	 * use KnowWEUtils.unmaskHTML
	 *
	 * @param htmlContent
	 * @return
	 */
	@Deprecated
	public static String unmaskHTML(String htmlContent) {

		return KnowWEUtils.unmaskHTML(htmlContent);
	}

	/**
	 *
	 * use KnowWEUtils.maskHTML
	 *
	 * @param htmlContent
	 * @return
	 */
	@Deprecated
	public static String maskHTML(String htmlContent) {
		return KnowWEUtils.maskHTML(htmlContent);
	}

	public String getNodeData(String web, String topic, String nodeID) {
		if (web == null || topic == null)
			return null;
		KnowWEArticle art = this.articleManagers.get(web).getArticle(topic);
		Section<?> sec = art.getNode(nodeID);
		String data = "Node not found: " + nodeID;
		if (sec != null) {
			data = sec.getOriginalText();
		}
		return data;
	}

	public KnowWEObjectType getRootType() {
		return rootTypes;
	}

	/**
	 * Collects all KnowWEObjectTypes.
	 *
	 * @return
	 */
	public List<KnowWEObjectType> getAllKnowWEObjectTypes() {

		if (this.allKnowWEObjectTypes == null) {
			KnowWEObjectTypeSet allTypes = new KnowWEObjectTypeSet();

			KnowWEObjectTypeSet s = KnowWEObjectTypeUtils
					.getAllChildrenTypesRecursive(getRootType(),
					new KnowWEObjectTypeSet());
			allTypes.addAll(s.toList());

			this.allKnowWEObjectTypes = allTypes.toLexicographicalList();
		}

		return this.allKnowWEObjectTypes;
	}

	/**
	 * @See KnowWEObjectTypeBrowserAction
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public KnowWEObjectType searchType(Class clazz) {
		for (KnowWEObjectType t : this.allKnowWEObjectTypes) {
			if (t.isType(clazz)) {
				return t;
			}
		}
		return null;
	}

	public List<KnowWEObjectType> searchTypeInstances(Class<?> clazz) {
		List<KnowWEObjectType> instances = new ArrayList<KnowWEObjectType>();
		getRootType().findTypeInstances(clazz, instances);
		// for (KnowWEModule mod : this.modules) {
		// mod.findTypeInstances(clazz, instances);
		// }
		return instances;
	}

	private static void stream(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}
}
