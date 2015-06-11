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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.FilterException;
import org.apache.wiki.api.exceptions.PluginException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.filters.BasicPageFilter;
import org.apache.wiki.api.plugin.WikiPlugin;
import org.apache.wiki.event.WikiEngineEvent;
import org.apache.wiki.event.WikiEvent;
import org.apache.wiki.event.WikiEventListener;
import org.apache.wiki.event.WikiEventUtils;
import org.apache.wiki.event.WikiPageEvent;
import org.apache.wiki.event.WikiPageRenameEvent;
import org.apache.wiki.providers.CachingProvider;
import org.apache.wiki.providers.WikiPageProvider;
import org.apache.wiki.ui.TemplateManager;

import de.d3web.plugin.Plugin;
import de.d3web.plugin.PluginManager;
import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.ResourceLoader;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.user.UserContextUtil;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.event.PageRenderedEvent;

public class KnowWEPlugin extends BasicPageFilter implements WikiPlugin,
		WikiEventListener {

	private static final String LEFT_MENU_FOOTER = "LeftMenuFooter";
	private static final String LEFT_MENU = "LeftMenu";
	private static final String MORE_MENU = "MoreMenu";
	public static final String FULL_PARSE_FIRED = "fullParseFired";

	private boolean wikiEngineInitialized = false;
	private final List<String> supportArticleNames;

	public KnowWEPlugin() {
		supportArticleNames = Arrays.asList(MORE_MENU, LEFT_MENU, LEFT_MENU_FOOTER);
	}

	/**
	 * To initialize KnowWE.
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
				String line;
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

				if (pagedir != null && pagedir.exists()) {
					File coreDir = new File(KnowWEUtils.getApplicationRootPath()
							+ "/WEB-INF/resources/core-pages");
					File[] files = coreDir.listFiles();
					if (files != null) {
						for (File corePage : files) {
							if (!corePage.getName().endsWith(".txt")) continue;
							File newFile = new File(pagedir.getPath() + "/" + corePage.getName());
							if (!newFile.exists()) FileUtils.copyFile(corePage, newFile);
						}
					}
				}
			}
			catch (Exception e) {
				Log.severe("Exception while trying to copy core pages", e);
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
		try {
			updateArticle(wikiContext, content);
		}
		catch (UpdateNotAllowedException e) {
			String title = wikiContext.getPage().getName();
			Log.fine("Somebody tried to update article " + title + " without appropriate rights", e);
		}
		catch (Exception e) {
			String title = wikiContext.getPage().getName();
			Log.severe("Exception while compiling article " + title, e);
		}
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
			Log.severe("Exception in post translate", e);
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
		includeDOMResources(wikiContext);
		/*
		 * The special pages MoreMenu, LeftMenu and LeftMenuFooter get extra
		 * calls: they are handled and rendered from the KDOMs in the following
		 */
		String title = wikiContext.getRealPage().getName();

		// happens when showing attachments, ignore...
		if (title.contains("/") && content.equals("")) {
			return content;
		}

		if (supportArticleNames.contains(title)) {
			try {
				Article supportArticle = Environment.getInstance()
						.getArticle(Environment.DEFAULT_WEB, title);
				if (supportArticle != null
						&& supportArticle.getRootSection().getText().equals(
						content)) {
					return renderKDOM(content, userContext, supportArticle);
				}
			}
			catch (Exception e) {
				Log.severe("Exception while compiling and rendering article '" + title + "'", e);
				return getExceptionRendering(userContext, e);
			}
		}

		int version = WikiPageProvider.LATEST_VERSION;
		String versionString = userContext.getParameter("version");
		if (versionString != null) {
			try {
				version = Integer.parseInt(versionString);
			}
			catch (NumberFormatException ignore) {
			}
		}
		WikiEngine engine = wikiContext.getEngine();
		if (engine != null) {
			String pureText = engine.getPureText(title, version);
			if (!content.equals(pureText)) return content;
		}
		DefaultArticleManager articleManager = getDefaultArticleManager();
		Article existingArticle = articleManager.getArticle(title);
		// if we have an existing article but having another case of the title,
		// we suggest a link to correct article name
		if (existingArticle != null && !existingArticle.getTitle().equals(title)) {
			return "The page \"" + title + "\" does not exist, did you mean \"["
					+ existingArticle.getTitle() + "]\"?";
		}

		try {
			String stringRaw = (String) httpRequest.getAttribute("renderresult" + title);
			if (stringRaw != null) return stringRaw;
			Article article = updateArticle(wikiContext, content);

			RenderResult renderResult = new RenderResult(userContext.getRequest());

			// this can happen, if the article was registered to the manager in a compilation frame opened
			// before the call of this preTranslate method, e.g. in an action with Sections#replace(...).
			// in this case, the article will not be compiled at this moment and rendering does not make sense and can
			// cause exceptions.
			boolean isQueuedForCompilation = articleManager.isQueuedArticle(article);

			if (article != null && !isQueuedForCompilation) {
				List<PageAppendHandler> appendHandlers = Environment.getInstance()
						.getAppendHandlers();

				renderPrePageAppendHandler(userContext, title, renderResult, appendHandlers);

				renderPage(userContext, article, renderResult);

				renderPostPageAppendHandler(userContext, title, renderResult, appendHandlers);

			}
			stringRaw = renderResult.toStringRaw();
			userContext.getRequest().setAttribute("renderresult" + title, stringRaw);
			return stringRaw;
		}
		catch (UpdateNotAllowedException e) {
			Log.fine("Somebody tried to update article " + title + " without appropriate rights", e);
			return getExceptionRendering(userContext, e);
		}
		catch (Throwable e) { // NOSONAR
			Log.severe("Exception while compiling and rendering article '" + title + "'", e);
			return getExceptionRendering(userContext, e);
		}
	}

	private DefaultArticleManager getDefaultArticleManager() {
		return (DefaultArticleManager) Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
	}

	private Article updateArticle(WikiContext wikiContext, String content) throws InterruptedException, UpdateNotAllowedException {
		HttpServletRequest httpRequest = wikiContext.getHttpRequest();
		if (httpRequest == null) {
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			return null;
		}

		String title = wikiContext.getRealPage().getName();

		String originalText = "";
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
		if (article != null) {
			originalText = article.getRootSection().getText();
		}

		boolean fullParse = isFullParse(httpRequest);
		if (fullParse) httpRequest.setAttribute(FULL_PARSE_FIRED, true);
		if (!originalText.equals(content) || fullParse) {
			if (!Environment.getInstance().getWikiConnector().userCanEditArticle(title, httpRequest)) {
				throw new UpdateNotAllowedException();
			}
			article = Environment.getInstance().buildAndRegisterArticle(Environment.DEFAULT_WEB, title, content);
			Compilers.getCompilerManager(Environment.DEFAULT_WEB).awaitTermination();
			if (fullParse) EventManager.getInstance().fireEvent(new FullParseFinishedEvent());
		}
		return article;
	}

	private boolean isFullParse(HttpServletRequest httpRequest) {
		String parse = UserContextUtil.getParameters(httpRequest).get("parse");
		Object fullParseFired = httpRequest.getAttribute(FULL_PARSE_FIRED);
		return parse != null && (parse.equals("full") || parse.equals("true")) && fullParseFired == null;
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

	private void renderPrePageAppendHandler(JSPWikiUserContext userContext, String title, RenderResult renderResult, List<PageAppendHandler> appendHandlers) {
		for (PageAppendHandler pageAppendHandler : appendHandlers) {
			if (pageAppendHandler.isPre()) {
				pageAppendHandler.append(
						Environment.DEFAULT_WEB, title,
						userContext, renderResult);
			}
		}
	}

	private String getExceptionRendering(UserContext context, Throwable e) {
		RenderResult renderResult = new RenderResult(context.getRequest());
		String message;
		if (e instanceof UpdateNotAllowedException) {
			message = "Your request tries to change the content of the current article. "
					+ "You are not authorized to do that.";
		}
		else {
			message = "An exception occurred while compiling and rendering this article, "
					+ "try going back to the last working version of the article.\n\n"
					+ ExceptionUtils.getStackTrace(e);
		}
		renderResult.appendHtmlElement("div", message, "style",
				"white-space: pre; overflow: hidden");
		return renderResult.toStringRaw();
	}

	/**
	 * Loads ALL articles stored in the pageDir (which is specified in jspwiki.properties).
	 *
	 * @param engine the wiki engine to get the articles from
	 * @created 07.06.2010
	 */
	private void initializeAllArticles(WikiEngine engine) {
		ArticleManager articleManager = getDefaultArticleManager();
		articleManager.open();
		try {
			Collection<?> wikiPages = getAllPages(engine);
			long start = System.currentTimeMillis();
			wikiPages.parallelStream().forEach(o -> {
				WikiPage wp = (WikiPage) o;
				String content = engine.getPureText(wp.getName(), wp.getVersion());
				Article article = Article.createArticle(content, wp.getName(), Environment.DEFAULT_WEB);
				((DefaultArticleManager) articleManager).queueArticle(article);
			});
			Log.info("Sectionized all articles in " + (System.currentTimeMillis() - start) + "ms");
		}
		catch (ProviderException e1) {
			Log.warning("Unable to load all articles, maybe some articles won't be initialized!");
		}
		finally {
			articleManager.commit();
		}

		try {
			// we wait to get an accurate reading on the server startup time
			articleManager.getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			Log.warning("Caught InterrupedException while waiting til compilation is finished.", e);
		}
		EventManager.getInstance().fireEvent(new InitializedArticlesEvent(articleManager));
	}

	private Collection<?> getAllPages(WikiEngine engine) throws ProviderException {
		PageManager mgr = engine.getPageManager();
		WikiPageProvider provider = mgr.getProvider();
		Collection<?> wikiPages;
		/*
		 Why do we need this workaround? Why do we check for the CachingProvider and so forth here?
		 JSPWiki does not handle case sensitivity in article names very well. On the one hand, it is possible in JSPWiki
		 to have article names that only differ in the case, which is a problem if you want to use such a wiki in a
		 case insensitive file system. On the other hand, JSPWiki will match article links case insensitively and even
		 creates pseudo wiki pages in the CachingProvider for those links which match only case insensitive.
		 Since KnowWE is designed with the promise that it also works with case insensitive file systems, it also
		 handles article names case insensitively. If the CachingProvider now serves pseudo articles based on links with
		 wrong case, we run into problems (e.g. ArticleManager in KnowWE stores articles case insensitively).
		 To solve this, we circumvent the CachingProvider here and use the actual FileSystemProvider instead, which will
		 not have those pseudo articles.
		*/
		if (provider instanceof CachingProvider) {
			wikiPages = ((CachingProvider) provider).getRealProvider().getAllPages();
		}
		else {
			wikiPages = mgr.getAllPages();
		}
		return wikiPages;
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

			ArticleManager amgr = getDefaultArticleManager();

			Article articleToDelete = amgr.getArticle(e.getPageName());
			if (articleToDelete != null) {
				// somehow the event is fired twice...
				// don't call deleteArticle if the article is already deleted
				amgr.deleteArticle(articleToDelete);
			}

		}
		else if (event instanceof WikiPageRenameEvent) {
			WikiPageRenameEvent renameEvent = (WikiPageRenameEvent) event;

			String oldArticleTitle = renameEvent.getOldPageName();
			String newArticleTitle = renameEvent.getNewPageName();

			KnowWEUtils.renameArticle(oldArticleTitle, newArticleTitle);
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
	 */
	public static void includeDOMResources(WikiContext wikiContext) {
		Object ctx = wikiContext.getVariable(TemplateManager.RESOURCE_INCLUDES);
		ResourceLoader loader = ResourceLoader.getInstance();

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
			if (ctx == null || !ctx.toString().contains(resource)) {
				if (!resource.contains("://")) {
					resource = ResourceLoader.defaultScript + resource;
				}
				TemplateManager.addResourceRequest(wikiContext, ResourceLoader.RESOURCE_SCRIPT, resource);
			}
		}

		List<String> css = loader.getStylesheetIncludes();
		for (String resource : css) {
			if (ctx == null || !ctx.toString().contains(resource)) {
				TemplateManager.addResourceRequest(wikiContext, ResourceLoader.RESOURCE_STYLESHEET, ResourceLoader.defaultStylesheet + resource);
			}
		}
	}

	@Override
	public String execute(WikiContext context, @SuppressWarnings("rawtypes") Map params) throws PluginException {
		return "";
	}

	private class UpdateNotAllowedException extends Exception {
	}
}
