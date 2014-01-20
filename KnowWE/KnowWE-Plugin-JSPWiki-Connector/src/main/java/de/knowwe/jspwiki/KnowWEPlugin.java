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

package de.knowwe.jspwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.ecyrd.jspwiki.PageManager;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.event.WikiEngineEvent;
import com.ecyrd.jspwiki.event.WikiEvent;
import com.ecyrd.jspwiki.event.WikiEventListener;
import com.ecyrd.jspwiki.event.WikiEventUtils;
import com.ecyrd.jspwiki.event.WikiPageEvent;
import com.ecyrd.jspwiki.event.WikiPageRenameEvent;
import com.ecyrd.jspwiki.filters.BasicPageFilter;
import com.ecyrd.jspwiki.filters.FilterException;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;
import com.ecyrd.jspwiki.providers.ProviderException;
import com.ecyrd.jspwiki.ui.TemplateManager;

import de.d3web.plugin.Plugin;
import de.d3web.plugin.PluginManager;
import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.RessourceLoader;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.user.UserContextUtil;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.event.PageRenderedEvent;

public class KnowWEPlugin extends BasicPageFilter implements WikiPlugin,
		WikiEventListener {

	private static final String LEFT_MENU_FOOTER = "LeftMenuFooter";
	private static final String LEFT_MENU = "LeftMenu";
	private static final String MORE_MENU = "MoreMenu";

	private boolean wikiEngineInitialized = false;
	private final List<String> supportArticleNames;

	public KnowWEPlugin() {
		supportArticleNames = Arrays.asList(MORE_MENU, LEFT_MENU, LEFT_MENU_FOOTER);
	}

	/**
	 * To initialize KnowWE.
	 * 
	 * @see KnowWE_config.properties
	 */
	@Override
	public void initialize(WikiEngine engine, Properties properties)
			throws FilterException {

		super.initialize(engine, properties);
		m_engine = engine;

		String copyCorePages = KnowWEUtils.getConfigBundle().getString(
				"knowweplugin.jspwikiconnector.copycorepages");
		if (copyCorePages.equals("true")) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(
						KnowWEUtils.getApplicationRootPath() + "/WEB-INF/jspwiki.properties"));
				String line = null;
				File pagedir = null;
				while ((line = in.readLine()) != null) {
					if (!line.contains("#")
							&& line.contains("jspwiki.fileSystemProvider.pageDir")) {
						line = line.trim();
						line = line.substring(line.lastIndexOf(" ") + 1);
						pagedir = new File(line);
						in.close();
						break;
					}
				}

				if (pagedir.exists()) {
					File coreDir = new File(KnowWEUtils.getApplicationRootPath()
							+ "/WEB-INF/resources/core-pages");
					for (File corePage : coreDir.listFiles()) {
						if (!corePage.getName().endsWith(".txt")) continue;
						File newFile = new File(pagedir.getPath() + "/"
								+ corePage.getName());
						if (!newFile.exists()) FileUtils.copyFile(corePage, newFile);
					}
				}
			}
			catch (Exception e) {
				Log.severe("Exception while trying to copy core pages: " + e.getMessage());
				// Start wiki without pages...
			}
		}

		WikiEventUtils.addWikiEventListener(engine,
				WikiEngineEvent.INITIALIZED, this);

		WikiEventUtils.addWikiEventListener(engine.getPageManager(),
				WikiPageEvent.PAGE_DELETE_REQUEST, this);

		WikiEventUtils.addWikiEventListener(engine.getPageManager(),
				WikiPageRenameEvent.PAGE_RENAMED, this);
	}

	private void initEnvironmentIfNeeded(WikiEngine wEngine) {
		if (!Environment.isInitialized()) {
			Environment.initInstance(new JSPWikiConnector(wEngine));
			// MultiSearchEngine.getInstance().addProvider(
			// new JSPWikiSearchConnector());
		}
	}

	@Override
	public void postSave(WikiContext wikiContext, String content)
			throws FilterException {
		// nothing to do here, everything is handled in pre- and post translate
	}

	@Override
	public String postTranslate(WikiContext wikiContext, String htmlContent)
			throws FilterException {

		try {
			HttpServletRequest httpRequest = wikiContext.getHttpRequest();
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			if (httpRequest != null) {
				htmlContent = RenderResult.unmask(htmlContent, httpRequest);
			}

			return htmlContent;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String preTranslate(WikiContext wikiContext, String content)
			throws FilterException {

		if (!wikiEngineInitialized) {
			return content;
		}

		/* creating KnowWEUserContext with username and requestParamteters */
		HttpServletRequest httpRequest = wikiContext.getHttpRequest();
		if (httpRequest == null) {
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			return content;
		}

		JSPWikiUserContext userContext = new JSPWikiUserContext(wikiContext,
				UserContextUtil.getParameters(httpRequest));

		/*
		 * The special pages MoreMenu, LeftMenu and LeftMenuFooter get extra
		 * calls: they are handled and rendered from the KDOMs in the following
		 */
		String title = wikiContext.getRealPage().getName();

		if (supportArticleNames.contains(title)) {
			try {
				Article supportArticle = Environment.getInstance()
						.getArticle(Environment.DEFAULT_WEB, title);
				if (supportArticle != null
						&& supportArticle.getRootSection().getText().equals(
								content)) {
					includeDOMResources(wikiContext);
					return renderKDOM(content, userContext, supportArticle);
				}
			}
			catch (Exception e) {
				Log.severe("Exception while compiling and rendering article '" + title + "'", e);
				return getExceptionRendering(userContext, e);
			}
		}

		WikiEngine engine = wikiContext.getEngine();
		String pureText = "";
		if (engine != null) {
			pureText = engine.getPureText(title,
					wikiContext.getPage().getVersion());
			if (!content.equals(pureText)) return content;
		}
		Set<String> titles = Environment.getInstance().getArticleManager(
				Environment.DEFAULT_WEB).getTitles();
		if (!titles.contains(title)) {
			for (String availableTitle : titles) {
				if (title.equalsIgnoreCase(availableTitle)) {
					return "The page \"" + title + "\" does not exist, did you mean \"["
							+ availableTitle + "]\"?";
				}
			}
		}
		try {

			String stringRaw = (String) httpRequest.getAttribute("renderresult");
			if (stringRaw != null) return stringRaw;

			Article article = Environment.getInstance().getArticle(
					Environment.DEFAULT_WEB, title);

			String originalText = "";
			if (article != null) {
				originalText = article.getRootSection().getText();
			}
			String parse = userContext.getParameter("parse");
			boolean fullParse = parse != null && (parse.equals("full") || parse.equals("true"));

			if (fullParse || !originalText.equals(content)) {
				deleteRenamedArticles(title);
				article = Environment.getInstance().buildAndRegisterArticle(
						Environment.DEFAULT_WEB, title,
						content, fullParse);
				Compilers.getCompilerManager(Environment.DEFAULT_WEB).awaitTermination();
			}

			RenderResult renderResult = new RenderResult(userContext.getRequest());

			if (article != null && httpRequest != null) {
				List<PageAppendHandler> appendhandlers = Environment.getInstance()
						.getAppendHandlers();

				renderPrePageAppendHandler(userContext, title, renderResult, appendhandlers);

				renderPage(userContext, article, renderResult);

				renderPostPageAppendHandler(userContext, title, renderResult, appendhandlers);

				includeDOMResources(wikiContext);
			}
			stringRaw = renderResult.toStringRaw();
			userContext.getRequest().setAttribute("renderresult", stringRaw);
			return stringRaw;
		}
		catch (Exception e) {
			Log.severe("Exception while compiling and rendering article '" + title + "'", e);
			return getExceptionRendering(userContext, e);
		}
	}

	private void renderPostPageAppendHandler(JSPWikiUserContext userContext, String title, RenderResult renderResult, List<PageAppendHandler> appendhandlers) {
		for (PageAppendHandler pageAppendHandler : appendhandlers) {
			if (!pageAppendHandler.isPre()) {
				pageAppendHandler.append(
						Environment.DEFAULT_WEB, title,
						userContext, renderResult);
			}
		}
	}

	private void renderPage(JSPWikiUserContext userContext, Article article, RenderResult renderResult) {
		long start = System.currentTimeMillis();
		article.getRootType().getRenderer().render(article.getRootSection(), userContext,
				renderResult);
		Log.info("Rendered article '" + article.getTitle() + "' in "
				+ (System.currentTimeMillis() - start) + "ms");
		EventManager.getInstance().fireEvent(
				new PageRenderedEvent(article.getTitle(), userContext));
	}

	private void renderPrePageAppendHandler(JSPWikiUserContext userContext, String title, RenderResult renderResult, List<PageAppendHandler> appendhandlers) {
		for (PageAppendHandler pageAppendHandler : appendhandlers) {
			if (pageAppendHandler.isPre()) {
				pageAppendHandler.append(
						Environment.DEFAULT_WEB, title,
						userContext, renderResult);
			}
		}
	}

	private String getExceptionRendering(UserContext context, Exception e) {
		RenderResult renderResult = new RenderResult(context.getRequest());
		String message = "An exception occured while compiling and rendering this article, "
				+ "try going back to the last working version of the article.\n\n"
				+ ExceptionUtils.getStackTrace(e);
		renderResult.appendHtmlElement("div", message, "style",
				"white-space: pre; overflow: hidden");
		return renderResult.toStringRaw();
	}

	private void deleteRenamedArticles(String title) {
		String changeNote = Environment.getInstance().getWikiConnector().getChangeNote(title, -1);
		Pattern renamePattern = Pattern.compile("^(.+)" + Pattern.quote(" ==> " + title) + "$");
		Matcher renameMatcher = renamePattern.matcher(changeNote);
		if (renameMatcher.find()) {
			// from the change note we get the hint, that an article was renamed
			String oldTitle = renameMatcher.group(1);
			WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
			ArticleManager articleManager = Environment.getInstance().getArticleManager(
					Environment.DEFAULT_WEB);
			Article oldArticle = articleManager.getArticle(oldTitle);
			// only delete in KnowWE if it exists in KnowWE but not JSPWiki
			if (!wikiConnector.doesArticleExist(oldTitle) && oldArticle != null) {
				articleManager.deleteArticle(oldArticle);
			}
		}
	}

	/**
	 * Loads ALL articles stored in the pageDir (which is specified in
	 * jspwiki.properties).
	 * 
	 * @created 07.06.2010
	 * @param engine
	 */
	private void initializeAllArticles(WikiEngine engine) {

		ArticleManager articleManager = Environment.getInstance().getArticleManager(
				Environment.DEFAULT_WEB);
		articleManager.open();

		try {
			PageManager mgr = engine.getPageManager();
			Collection<?> wikipages = null;

			try {
				wikipages = mgr.getAllPages();
			}
			catch (ProviderException e1) {
				Log.warning("Unable to load all articles, maybe some articles won't be initialized!");
			}

			for (Object o : wikipages) {
				WikiPage wp = (WikiPage) o;
				Article article = Environment.getInstance().getArticle(
						Environment.DEFAULT_WEB, wp.getName());
				if (article == null) {
					String content = engine.getPureText(wp.getName(), wp.getVersion());
					Environment.getInstance().buildAndRegisterArticle(content, wp.getName(),
							Environment.DEFAULT_WEB);
				}
			}
		}
		finally {
			articleManager.commit();
		}

		try {
			// we wait to get an accurate reading on the server startup time
			articleManager.getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			Log.warning("Caught InterrupedException while waiting til compilation is finished.",
					e);
		}
		EventManager.getInstance().fireEvent(new InitializedArticlesEvent(articleManager));
	}

	private String renderKDOM(String content, UserContext userContext,
			Article article) {
		if (article != null) {
			RenderResult articleString = new RenderResult(userContext.getRequest());
			article.getRootType().getRenderer().render(article.getRootSection(), userContext,
					articleString);
			return articleString.toStringRaw();
		}
		return content + "\n(no KDOM)";
	}

	/**
	 * Handles events passed from JSPWiki
	 */
	@Override
	public void actionPerformed(WikiEvent event) {
		// When deleting a page, remove it from the ArticleManager and
		// invalidate all knowledge
		if ((event instanceof WikiPageEvent)
				&& (event.getType() == WikiPageEvent.PAGE_DELETE_REQUEST)) {
			WikiPageEvent e = (WikiPageEvent) event;

			ArticleManager amgr = Environment.getInstance()
					.getArticleManager(Environment.DEFAULT_WEB);

			Article articleToDelete = amgr.getArticle(e.getPageName());
			if (articleToDelete != null) {
				// somehow the event is fired twice...
				// don't call deleteArticle if the article is already deleted
				amgr.deleteArticle(articleToDelete);
			}

		}
		else if (event instanceof WikiPageRenameEvent) {
			WikiPageRenameEvent e = (WikiPageRenameEvent) event;

			ArticleManager amgr = Environment.getInstance()
					.getArticleManager(Environment.DEFAULT_WEB);

			amgr.deleteArticle(amgr.getArticle(e.getOldPageName()));
		}
		else if (event instanceof WikiEngineEvent) {
			if (event.getType() == WikiEngineEvent.INITIALIZED) {
				WikiEngine engine = ((WikiEngineEvent) event).getEngine();
				initEnvironmentIfNeeded(engine);
				initializeAllArticles(engine);
				wikiEngineInitialized = true;
			}
		}
	}

	/**
	 * Adds the CSS and JS files to the current page.
	 * 
	 * @param wikiContext
	 */
	private void includeDOMResources(WikiContext wikiContext) {
		Object ctx = wikiContext.getVariable(TemplateManager.RESOURCE_INCLUDES);
		RessourceLoader loader = RessourceLoader.getInstance();

		List<String> script = loader.getScriptIncludes();
		for (String resource : script) {

			/*
			 * Check whether the corresponding plugin shipping the resource is
			 * also existing in current installation As css and js dependencies
			 * within the plugin manifests are not resolved by the build process
			 * broken dependencies might happen.
			 */
			String pluginPrefix = "KnowWE-Plugin-";
			if (resource.startsWith(pluginPrefix)) {
				String resourceName = resource.substring(0, resource.lastIndexOf("."));
				Plugin[] plugins = PluginManager.getInstance().getPlugins();
				boolean found = false;
				for (Plugin plugin : plugins) {
					if (resourceName.startsWith(plugin.getPluginID())) {
						found = true;
					}
				}
				if (!found) {
					// obviously the plugin is not available in current
					// installation
					Log.warning("Found dependency to a css/js resource (" + resource
							+ ") where the corresponding plugin is not available.");
					continue;
				}
			}
			if (ctx != null && !ctx.toString().contains(resource)) {
				if (!resource.contains("://")) {
					TemplateManager.addResourceRequest(wikiContext,
							RessourceLoader.RESOURCE_SCRIPT,
							RessourceLoader.defaultScript + resource);
				}
				else {
					TemplateManager.addResourceRequest(wikiContext,
							RessourceLoader.RESOURCE_SCRIPT,
							resource);
				}
			}
			else if (ctx == null) {
				if (!resource.contains("://")) {
					TemplateManager.addResourceRequest(wikiContext,
							RessourceLoader.RESOURCE_SCRIPT,
							RessourceLoader.defaultScript + resource);
				}
				else {
					TemplateManager.addResourceRequest(wikiContext,
							RessourceLoader.RESOURCE_SCRIPT,
							resource);
				}
			}
		}

		List<String> css = loader.getStylesheetIncludes();
		for (String resource : css) {
			if (ctx != null && !ctx.toString().contains(resource)) {
				TemplateManager.addResourceRequest(wikiContext,
						RessourceLoader.RESOURCE_STYLESHEET,
						RessourceLoader.defaultStylesheet + resource);
			}
			else if (ctx == null) {
				TemplateManager.addResourceRequest(wikiContext,
						RessourceLoader.RESOURCE_STYLESHEET,
						RessourceLoader.defaultStylesheet + resource);
			}
		}
	}

	@Override
	public String execute(WikiContext context, @SuppressWarnings("rawtypes") Map params) throws PluginException {
		return "";
	}

}
