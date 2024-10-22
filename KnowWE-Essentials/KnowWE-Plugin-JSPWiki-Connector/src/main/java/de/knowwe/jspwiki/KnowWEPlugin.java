/*
 * Copyright (C) 2022 denkbares GmbH, Germany
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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.exceptions.FilterException;
import org.apache.wiki.api.exceptions.PluginException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.filters.BasePageFilter;
import org.apache.wiki.api.plugin.Plugin;
import org.apache.wiki.api.providers.AttachmentProvider;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.content.PageRenamer;
import org.apache.wiki.event.GitUpdateByPullPageEvent;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiAttachmentEvent;
import org.apache.wiki.event.WikiEngineEvent;
import org.apache.wiki.event.WikiEvent;
import org.apache.wiki.event.WikiEventListener;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.event.WikiPageEvent;
import org.apache.wiki.event.WikiPageRenameEvent;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.CachingAttachmentProvider;
import org.apache.wiki.providers.CachingProvider;
import org.apache.wiki.providers.GitProviderProperties;
import org.apache.wiki.ui.TemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import com.denkbares.plugin.PluginManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.ResourceLoader;
import de.knowwe.core.UpdateNotAllowedException;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.ServletRequestKeyValueStore;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.user.UserContextUtil;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.ArticleDeletedEvent;
import de.knowwe.event.ArticleUpdateEvent;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentStoredEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.event.PageRenderedEvent;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector;

import static de.knowwe.core.ResourceLoader.Type.*;

public class KnowWEPlugin extends BasePageFilter implements Plugin,
		WikiEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(KnowWEPlugin.class);

	private static final String LEFT_MENU_FOOTER = "LeftMenuFooter";
	private static final String LEFT_MENU = "LeftMenu";
	private static final String MORE_MENU = "MoreMenu";
	private static final String FULL_PARSE_FIRED = "fullParseFired";
	public static final String RENDER_MODE = "renderMode";
	public static final String PREVIEW = "preview";
	private static final int DEFAULT_RENDER_WAIT_TIMEOUT_SECONDS = 5;
	private static final String COMPILATION_TIMEOUT = "compilationTimeout";
	private static final String INITIAL_WAIT_ON = "initialWaitOn";

	private boolean wikiEngineInitialized = false;
	private final List<String> supportArticleNames;

	public KnowWEPlugin() {
		supportArticleNames = Arrays.asList(MORE_MENU, LEFT_MENU, LEFT_MENU_FOOTER);
	}

	/**
	 * To initialize KnowWE.
	 */
	@Override
	public void initialize(Engine engine, Properties properties)
			throws FilterException {
		super.initialize(engine, properties);

		String copyCorePages = KnowWEUtils.getConfigBundle().getString("knowweplugin.jspwikiconnector.copycorepages");
		if ("true".equals(copyCorePages)) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
						KnowWEUtils.getApplicationRootPath() + "/WEB-INF/classes/jspwiki-custom.properties"),
						StandardCharsets.UTF_8));
				String line;
				File pageDir = null;
				while ((line = in.readLine()) != null) {
					if (!line.contains("#")
						&& line.contains("jspwiki.fileSystemProvider.pageDir")) {
						line = line.trim();
						line = line.substring(line.lastIndexOf(" ") + 1);
						pageDir = new File(line);
						in.close();
						break;
					}
				}

				if (pageDir != null && pageDir.exists()) {
					File coreDir = new File(KnowWEUtils.getApplicationRootPath()
											+ "/WEB-INF/resources/core-pages");
					File[] files = coreDir.listFiles();
					if (files != null) {
						for (File corePage : files) {
							if (!corePage.getName().endsWith(".txt")) continue;
							File newFile = new File(pageDir.getPath() + "/" + corePage.getName());
							if (!newFile.exists()) FileUtils.copyFile(corePage, newFile);
						}
					}
				}
			}
			catch (Exception e) {
				LOGGER.error("Exception while trying to copy core pages", e);
				// Start wiki without pages...
			}
		}

		WikiEventManager.addWikiEventListener(getEngine(), this);
		WikiEventManager.addWikiEventListener(getPageManager(), this);

		AttachmentProvider currentProvider = getAttachmentManager().getCurrentProvider();

		if (currentProvider instanceof CachingAttachmentProvider) {
			currentProvider = ((CachingAttachmentProvider) currentProvider).getRealProvider();
		}

		WikiEventManager.addWikiEventListener(currentProvider, this);

		PageProvider pageProvider = getPageManager().getProvider();
		if (pageProvider instanceof CachingProvider) {
			pageProvider = ((CachingProvider) pageProvider).getRealProvider();
		}
		WikiEventManager.addWikiEventListener(pageProvider, this);
	}

	private PageManager getPageManager() {
		return getEngine().getManager(PageManager.class);
	}

	private AttachmentManager getAttachmentManager() {
		return getEngine().getManager(AttachmentManager.class);
	}

	public Engine getEngine() {
		return this.m_engine;
	}

	private void initEnvironmentIfNeeded() {
		if (!Environment.isInitialized()) {
			Environment.initInstance(new JSPWikiConnector(getEngine()));
			// MultiSearchEngine.getInstance().addProvider(
			// new JSPWikiSearchConnector());
		}
	}

	@Override
	public void postSave(Context wikiContext, String content) {
		try {
			updateArticle(wikiContext, content);
		}
		catch (UpdateNotAllowedException e) {
			String title = wikiContext.getPage().getName();
			LOGGER.debug("Somebody tried to update article " + title + " without appropriate rights", e);
		}
		catch (Exception e) {
			String title = wikiContext.getPage().getName();
			LOGGER.error("Exception while compiling article " + title, e);
		}
	}

	@Override
	public String postTranslate(Context wikiContext, String htmlContent) {

		try {
			HttpServletRequest httpRequest = wikiContext.getHttpRequest();
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			if (httpRequest != null) {
				htmlContent = RenderResult.unmask(htmlContent, new ServletRequestKeyValueStore(httpRequest));
			}

			return htmlContent;
		}
		catch (Exception e) {
			LOGGER.error("Exception in post translate", e);
			return "";
		}
	}

	@Override
	public String preTranslate(Context context, String content) {
		if (!(context instanceof WikiContext wikiContext)) {
			throw new IllegalStateException("We expect a wiki engine, otherwise KnowWE can't function");
		}
		if (context.getHttpRequest() != null
			&& !isWorkflow(wikiContext)
			&& context.getHttpRequest().getParameter("action") != null) {
			// we don't want to trigger our KnowWE compilation and render pipeline,
			// if we are just executing some action (ajax from client)
			return content;
		}
		if (!wikiEngineInitialized) {
			return content;
		}

		/* creating KnowWEUserContext with username and requestParameters */
		HttpServletRequest httpRequest = wikiContext.getHttpRequest();
		if (httpRequest == null) {
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			return content;
		}

		JSPWikiUserContext userContext = new JSPWikiUserContext(wikiContext,
				UserContextUtil.getParameters(httpRequest));
		if (isRenderingPreview(userContext)) {
			return content;
		}

		/*
		 * The special pages MoreMenu, LeftMenu and LeftMenuFooter get extra
		 * calls: they are handled and rendered from the KDOMs in the following
		 */
		String title = wikiContext.getRealPage().getName();

		// happens when showing attachments, ignore...
		if (title.contains("/") && "".equals(content)) {
			return content;
		}

		if (isSupportArticle(title)) {
			try {
				Article supportArticle = Environment.getInstance()
						.getArticle(Environment.DEFAULT_WEB, title);
				if (supportArticle != null
					&& supportArticle.getRootSection().getText().equals(
						content)) {
					RenderResult renderResult = new RenderResult(userContext);
					render(userContext, supportArticle, renderResult);
					return renderResult.toStringRaw();
				}
			}
			catch (Exception e) {
				LOGGER.error("Exception while compiling and rendering article '" + title + "'", e);
				return getExceptionRendering(userContext, e);
			}
		}

		int version = PageProvider.LATEST_VERSION;
		String versionString = userContext.getParameter("version");
		if (versionString != null) {
			try {
				version = Integer.parseInt(versionString);
			}
			catch (NumberFormatException ignore) {
			}
		}

		String pureText = getPageManager().getPureText(title, version);
		if (!content.equals(pureText)) return content;

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
			Article article;
			if (version == PageProvider.LATEST_VERSION) {
				article = updateArticle(wikiContext, content);
			}
			else {
				// just create/sectionize temp version of the article that is NOT added and compiled to the article
				// manager
				article = Article.createTemporaryArticle(content, title, Environment.DEFAULT_WEB);
			}

			RenderResult renderResult = new RenderResult(userContext);

			if (article != null) {
				long start = System.currentTimeMillis();
				render(userContext, article, renderResult);
				LOGGER.info("Rendered article '" + article.getTitle() + "' in "
							+ (System.currentTimeMillis() - start) + "ms");
			}
			stringRaw = renderResult.toStringRaw();
			userContext.getRequest().setAttribute("renderresult" + title, stringRaw);
			return stringRaw;
		}
		catch (UpdateNotAllowedException e) {
			LOGGER.debug("Somebody tried to update article " + title + " without appropriate rights", e);
			return getExceptionRendering(userContext, e);
		}
		catch (Throwable e) { // NOSONAR
			LOGGER.error("Exception while compiling and rendering article '" + title + "'", e);
			return getExceptionRendering(userContext, e);
		}
	}

	private boolean isWorkflow(WikiContext wikiContext) {
		return wikiContext.getJSP() != null && wikiContext.getJSP().contains("Workflow");
	}

	public boolean isSupportArticle(String title) {
		return supportArticleNames.contains(title);
	}

	private void render(JSPWikiUserContext userContext, Article article, RenderResult renderResult) throws InterruptedException {
		ArticleManager articleManager = article.getArticleManager();
		if (!isSupportArticle(article.getTitle()) && articleManager != null) {
			CompilerManager compilerManager = articleManager.getCompilerManager();
			if (!compilerManager.awaitTermination(300)) {
				HtmlElement compileWarning = new Span().clazz("warning").attributes("id", "compile-warning");
				compileWarning.children(new Span("Compilation still ongoing, please wait... " +
												 "You are currently viewing a preview of the page, compilation messages and parts of the " +
												 "content might still be missing!"));
				String commitMessage = compilerManager.getCompileMessage();
				if (Strings.isNotBlank(commitMessage)) {
					compileWarning.children(new Span("\n" + commitMessage));
				}
				long started = new Date().getTime() - compilerManager.getLastCompilationStart().getTime();
				String runningSince = "Running since: ";
				if (Strings.isBlank(commitMessage) || !commitMessage.matches(".+\\w+$")) {
					runningSince = "\n" + runningSince;
				}
				else  {
					runningSince = ". " + runningSince;
				}
				compileWarning.children(
						new Span(runningSince),
						new Span(Stopwatch.getDisplay(started))
								.attributes("id", "time-value")
								.attributes("data-started-ms-ago", started + ""));
				renderResult.append(compileWarning);
			}
		}
		renderArticle(userContext, article, renderResult);
	}

	private void renderArticle(JSPWikiUserContext userContext, Article article, RenderResult renderResult) {
		List<PageAppendHandler> appendHandlers = Environment.getInstance().getAppendHandlers();
		renderPrePageAppendHandler(userContext, article, renderResult, appendHandlers);
		article.getRootType().getRenderer().render(article.getRootSection(), userContext, renderResult);
		EventManager.getInstance().fireEvent(new PageRenderedEvent(article.getTitle(), userContext));
		renderPostPageAppendHandler(userContext, article, renderResult, appendHandlers);
	}

	private static DefaultArticleManager getDefaultArticleManager() {
		return (DefaultArticleManager) Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
	}

	private static Article updateArticle(Context wikiContext, String content) throws InterruptedException,
			UpdateNotAllowedException {
		HttpServletRequest httpRequest = wikiContext.getHttpRequest();
		if (httpRequest == null) {
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			return null;
		}
		boolean fullParse = isFullParse(httpRequest);
		if (fullParse) httpRequest.setAttribute(FULL_PARSE_FIRED, true);

		return Environment.getInstance()
				.updateArticle(wikiContext.getRealPage().getName(), wikiContext.getCurrentUser()
						.getName(), content, fullParse, httpRequest);
	}

	private static boolean isFullParse(HttpServletRequest httpRequest) {
		String parse = UserContextUtil.getParameters(httpRequest).get("parse");
		Object fullParseFired = httpRequest.getAttribute(FULL_PARSE_FIRED);
		return ("full".equals(parse) || "true".equals(parse)) && fullParseFired == null;
	}

	private static void renderPostPageAppendHandler(JSPWikiUserContext userContext, Article article,
													RenderResult renderResult,
													List<PageAppendHandler> appendhandlers) {
		// in case we are rendering a support article (like LeftMenu or MoreMenu), skip this
		if (!article.getTitle().equals(userContext.getTitle())) return;
		for (PageAppendHandler pageAppendHandler : appendhandlers) {
			if (!pageAppendHandler.isPre()) {
				pageAppendHandler.append(article, userContext, renderResult);
			}
		}
	}

	private static void renderPrePageAppendHandler(JSPWikiUserContext userContext, Article article,
												   RenderResult renderResult, List<PageAppendHandler> appendHandlers) {
		// in case we are rendering a support article (like LeftMenu or MoreMenu), skip this
		if (!article.getTitle().equals(userContext.getTitle())) return;
		for (PageAppendHandler pageAppendHandler : appendHandlers) {
			if (pageAppendHandler.isPre()) {
				pageAppendHandler.append(article, userContext, renderResult);
			}
		}
	}

	public static String renderPreview(Context wikiContext, String content) {
		if (content == null) return "";

		HttpServletRequest httpRequest = wikiContext.getHttpRequest();
		if (httpRequest == null) {
			// When a page is rendered the first time, the request is null.
			// Since this version with no http request is not shown to the user,
			// we can just ignore it.
			return content;
		}

		JSPWikiUserContext userContext = new JSPWikiUserContext(wikiContext,
				UserContextUtil.getParameters(httpRequest));
		userContext.setAsychronousRenderingAllowed(false);
		userContext.getRequest().setAttribute(RENDER_MODE, PREVIEW);
		includeDOMResources(wikiContext);
		RenderResult renderResult = new RenderResult(userContext);
		String title = wikiContext.getRealPage().getName();
		Article article = Article.createTemporaryArticle(content, title, Environment.DEFAULT_WEB);

		List<PageAppendHandler> appendHandlers = Environment.getInstance()
				.getAppendHandlers();
		renderPrePageAppendHandler(userContext, article, renderResult, appendHandlers);
		article.getRootType().getRenderer().render(article.getRootSection(), userContext,
				renderResult);
		renderPostPageAppendHandler(userContext, article, renderResult, appendHandlers);

		return renderResult.toStringRaw();
	}

	private String getExceptionRendering(UserContext context, Throwable e) {
		RenderResult renderResult = new RenderResult(context);
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
	 * @created 07.06.2010
	 */
	private void initializeAllArticles() {
		Stopwatch stopwatchAll = new Stopwatch();
		DefaultArticleManager articleManager = getDefaultArticleManager();
		articleManager.open();
		try {
			Collection<?> wikiPages = getAllPages(getEngine());
			Stopwatch stopwatchSectionizing = new Stopwatch();
			wikiPages.parallelStream().forEach(o -> {
				WikiPage wp = (WikiPage) o;
				String content = getPageManager().getPureText(wp.getName(), wp.getVersion());
				articleManager.queueArticle(wp.getName(), content);
			});
			stopwatchSectionizing.log("Sectionized all articles");
		}
		catch (ProviderException e1) {
			LOGGER.warn("Unable to load all articles, maybe some articles won't be initialized!", e1);
		}
		finally {
			articleManager.commit();
		}

		try {
			// we wait to get an accurate reading on the server startup time
			articleManager.getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			LOGGER.warn("Caught InterrupedException while waiting til compilation is finished.", e);
		}
		articleManager.setInitialized(true);
		EventManager.getInstance().fireEvent(new InitializedArticlesEvent(articleManager));
		stopwatchAll.log("Initialized all articles");
	}

	private Collection<?> getAllPages(Engine engine) throws ProviderException {
		PageProvider provider = getPageManager().getProvider();
		Collection<?> wikiPages;
        /*
         Why do we need this workaround? Why do we check for the CachingProvider and so forth here?
         JSPWiki does not handle case sensitivity in article names very well. On the one hand, it is possible in
         JSPWiki
         to have article names that only differ in the case, which is a problem if you want to use such a wiki in a
         case insensitive file system. On the other hand, JSPWiki will match article links case insensitively and even
         creates pseudo wiki pages in the CachingProvider for those links which match only case insensitive.
         Since KnowWE is designed with the promise that it also works with case insensitive file systems, it also
         handles article names case insensitively. If the CachingProvider now serves pseudo articles based on links
         with
         wrong case, we run into problems (e.g. ArticleManager in KnowWE stores articles case insensitively).
         To solve this, we circumvent the CachingProvider here and use the actual FileSystemProvider instead, which
         will
         not have those pseudo articles.
        */
		if (provider instanceof CachingProvider) {
			wikiPages = ((CachingProvider) provider).getRealProvider().getAllPages();
		}
		else {
			wikiPages = getPageManager().getAllPages();
		}
		return wikiPages;
	}

	/**
	 * Handles events passed from JSPWiki
	 */
	@Override
	public void actionPerformed(WikiEvent event) {
		// When deleting a page, remove it from the ArticleManager and
		// invalidate all knowledge
		if ((event instanceof WikiPageEvent e) && (event.getType() == WikiPageEvent.PAGE_DELETE_REQUEST)) {

			ArticleManager articleManager = getDefaultArticleManager();
			new Thread(() -> {
				// somehow the event is fired twice...
				// don't call deleteArticle if the article is already deleted
				final Article article = articleManager.getArticle(e.getPageName());
				if (article != null) {
					EventManager.getInstance().fireEvent(new ArticleDeletedEvent(article));
					articleManager.deleteArticle(e.getPageName());
				}
			}).start();
		}
		else if (event instanceof WikiPageRenameEvent renameEvent) {
			new Thread(() -> {
				String oldArticleTitle = renameEvent.getOldPageName();
				String newArticleTitle = renameEvent.getNewPageName();
				KnowWEUtils.renameArticle(oldArticleTitle, newArticleTitle);
			}).start();
		}
		else if (event instanceof WikiAttachmentEvent) {
			// we fire the KnowWE events and commit asynchronously to avoid dead locks, because we cannot
			// guarantee at this point, that the thread accessing the attachment has not locked resources
			// that are required during compilation
			new AttachmentEventHandler((WikiAttachmentEvent) event).start();
		}
		else if (event instanceof WikiEngineEvent) {
			if (event.getType() == WikiEngineEvent.INITIALIZED) {
				this.m_engine = event.getSrc();
				initEnvironmentIfNeeded();
				initializeAllArticles();
				registerLateListeners();
				wikiEngineInitialized = true;
			}
		}
		else if (event instanceof GitVersioningWikiEvent gitEvent) {

			ArticleUpdateEvent articleUpdateEvent;
			String pageTitle = gitEvent.getPages().stream().findFirst().get();
			if (event instanceof GitUpdateByPullPageEvent) {
				articleUpdateEvent = new ArticleUpdateEvent(pageTitle, gitEvent.getAuthor());
				handleArticleRefreshEvent(gitEvent.getPages(), gitEvent.getType());
			}
			else {
				articleUpdateEvent = new ArticleUpdateEvent(pageTitle, gitEvent.getAuthor());
				String gitCommitRev = gitEvent.getGitCommitRev();
				String pageFile = JSPUtils.mangleName(pageTitle) + ".txt";
				int latestVersionNumber = getGitConnector().numberOfCommitsForFile(pageFile);
				ArticleUpdateEvent.Version version = new ArticleUpdateEvent.Version(latestVersionNumber);
				version.setCommitHash(gitCommitRev);
				articleUpdateEvent.setVersion(version);
			}
			EventManager.getInstance().fireEvent(articleUpdateEvent);
		}
	}

	private GitConnector gitConnector = null;

	private GitConnector getGitConnector() {
		if (gitConnector == null) {
			String gitDir = Environment.getInstance()
					.getWikiConnector()
					.getWikiProperty(GitProviderProperties.JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR);
			gitConnector = JGitBackedGitConnector.fromPath(gitDir);
		}
		return gitConnector;
	}

	private static void handleArticleRefreshEvent(Collection<String> pageTitles, int type) {
		ArticleManager articleManager = KnowWEUtils.getDefaultArticleManager();
		articleManager.open();
		LOGGER.info("open");

		try {
			for (String title : pageTitles) {
				//for some reason we dont want to do this for attachments
				if (title.contains("/")) {
					continue;
				}
				LOGGER.info(title);
				if (type == GitUpdateByPullPageEvent.UPDATE) {
					//this block checks wheter the new text is empty, this gets interpreted as a deletion
					String articleText = Environment.getInstance()
							.getWikiConnector()
							.getArticleText(title);
					if (articleText == null || articleText.isEmpty()) {
						Article article = articleManager.getArticle(title);
						if (article != null) {
							articleManager.deleteArticle(title);
						}
						else {
							LOGGER.warn("Article not found: " + title);
						}
					}
					else {
						//standard case of KnowWE article update
						articleManager.registerArticle(title, articleText);
					}
				}
				else {
					Article article = articleManager.getArticle(title);
					if (article != null) {
						articleManager.deleteArticle(title);
					}
				}
			}
		}
		finally {
			articleManager.commit();
			LOGGER.info("commit");
		}
	}

	private static boolean articleIsEmpty(String title) {
		return false;
	}

	/**
	 * Some managers are not available immediately, so register a bit later
	 */
	private void registerLateListeners() {
		PageRenamer manager = getEngine().getManager(PageRenamer.class);
		WikiEventManager.addWikiEventListener(manager, this);
	}

	/**
	 * Adds the CSS and JS files to the current page.
	 */
	public static void includeDOMResources(Context wikiContext) {
		Object ctx = wikiContext.getVariable(TemplateManager.RESOURCE_INCLUDES);
		ResourceLoader loader = ResourceLoader.getInstance();
		final List<String> scriptIncludes = loader.getScriptIncludes();
		addResourceToTemplateManager(wikiContext, ctx, scriptIncludes, script);

		final List<String> moduleIncludes = loader.getModuleIncludes();
		addResourceToTemplateManager(wikiContext, ctx, moduleIncludes, module);

		List<String> css = loader.getStylesheetIncludes();
		List<String> cssToAdd = new ArrayList<>();
		List<String> types = new ArrayList<>();
		for (String resource : css) {
			if (ctx == null || !ctx.toString().contains(resource)) {
				cssToAdd.add(stylesheet.getPath(resource));
				types.add(stylesheet.name());
			}
		}
		TemplateManager.addResourceRequests(wikiContext, types, cssToAdd);
	}

	private static void addResourceToTemplateManager(Context wikiContext, Object ctx, List<String> scriptIncludes,
													 ResourceLoader.Type type) {

		List<String> types = new ArrayList<>();
		List<String> resourcePaths = new ArrayList<>();
		for (String resource : scriptIncludes) {
			/*
			 * Check whether the corresponding plugin shipping the resource is
			 * also existing in current installation As css and js dependencies
			 * within the plugin manifests are not resolved by the build process
			 * broken dependencies might happen.
			 */
			String pluginPrefix = "KnowWE-Plugin-";
			if (resource.startsWith(pluginPrefix)) {
				String resourceName = resource.substring(0, resource.lastIndexOf("."));
				com.denkbares.plugin.Plugin[] plugins = PluginManager.getInstance().getPlugins();
				boolean found = false;
				for (com.denkbares.plugin.Plugin plugin : plugins) {
					if (resourceName.startsWith(plugin.getPluginID())) {
						found = true;
					}
				}
				if (!found) {
					// obviously the plugin is not available in current
					// installation
					LOGGER.warn("Found dependency to a css/js resource (" + resource +
								") where the corresponding plugin is not available. " +
								"This can also happen, if the plugin.xml has the wrong plugin id " +
								"(id other than the name of module it is in).");
					continue;
				}
			}
			if (ctx == null || !ctx.toString().contains(resource)) {
				types.add(type.name());
				resourcePaths.add(type.getPath(resource));
			}
		}
		TemplateManager.addResourceRequests(wikiContext, types, resourcePaths);
	}

	@Override
	public String execute(Context context, Map<String, String> params) throws PluginException {
		return "";
	}

	private static class AttachmentEventHandler extends Thread {

		private final DefaultArticleManager articleManager;
		private final WikiAttachmentEvent event;

		private AttachmentEventHandler(WikiAttachmentEvent attachmentEvent) {
			this.event = attachmentEvent;
			this.articleManager = getDefaultArticleManager();
		}

		@Override
		public void run() {
			if (event.getType() == WikiAttachmentEvent.STORED) {
				EventManager.getInstance()
						.fireEvent(new AttachmentStoredEvent(articleManager.getWeb(), event.getParentName(),
								event.getFileName()));
			}
			else if (event.getType() == WikiAttachmentEvent.DELETED) {
				EventManager.getInstance()
						.fireEvent(new AttachmentDeletedEvent(articleManager.getWeb(), event.getParentName(),
								event.getFileName()));
			}
		}
	}

	public static boolean isRenderingPreview(UserContext user) {
		return user != null
			   && user.getRequest() != null
			   && user.isRenderingPreview();
	}
}
