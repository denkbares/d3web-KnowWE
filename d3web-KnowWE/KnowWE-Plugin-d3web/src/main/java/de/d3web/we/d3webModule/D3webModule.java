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

package de.d3web.we.d3webModule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.ClearDPSSessionAction;
import de.d3web.we.action.CodeCompletionAction;
import de.d3web.we.action.DPSDialogsAction;
import de.d3web.we.action.DPSSolutionsAction;
import de.d3web.we.action.ExplanationRenderer2;
import de.d3web.we.action.GenerateKBAction;
import de.d3web.we.action.KSSViewHistoryAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.action.KnowledgeSummerizeAction;
import de.d3web.we.action.ParseWebOfflineRenderer;
import de.d3web.we.action.QuestionStateReportAction;
import de.d3web.we.action.ReInitDPSEnvironmentAction;
import de.d3web.we.action.RefreshHTMLDialogAction;
import de.d3web.we.action.RequestDialogRenderer;
import de.d3web.we.action.SaveDialogAsXCLAction;
import de.d3web.we.action.SemanticAnnotationAction;
import de.d3web.we.action.SetFindingAction;
import de.d3web.we.action.SetSingleFindingAction;
import de.d3web.we.action.SolutionLogAction;
import de.d3web.we.action.UserFindingsAction;
import de.d3web.we.action.XCLExplanationRenderer;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.TerminalType;
import de.d3web.we.kdom.Annotation.Annotation;
import de.d3web.we.kdom.bulletLists.scoring.BulletScoring;
import de.d3web.we.kdom.dashTree.questionnaires.QuestionnairesSection;
import de.d3web.we.kdom.dashTree.solutions.SolutionsSection;
import de.d3web.we.kdom.decisionTree.QuestionsSection;
import de.d3web.we.kdom.kopic.Kopic;
import de.d3web.we.kdom.kopic.renderer.AnnotationInlineAnswerRenderer;
import de.d3web.we.kdom.rules.RulesSection;
import de.d3web.we.kdom.table.attributes.AttributeTableSection;
import de.d3web.we.kdom.table.xcl.CoveringTableSection;
import de.d3web.we.kdom.xcl.CoveringListSection;
import de.d3web.we.kdom.xcl.XCL;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.module.PageAppendHandler;
import de.d3web.we.renderer.xml.GraphMLOwlRenderer;
import de.d3web.we.taghandler.DialogPaneTagHandler;
import de.d3web.we.taghandler.KBRenderer;
import de.d3web.we.taghandler.KnOfficeUploadHandler;
import de.d3web.we.taghandler.ObjectInfoTagHandler;
import de.d3web.we.taghandler.QuestionSheetHandler;
import de.d3web.we.taghandler.ShowAllKBsHandler;
import de.d3web.we.taghandler.SolutionStateViewHandler;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.taghandler.TestsuiteTagHandler;
import de.d3web.we.taghandler.WikiSolutionsTagHandler;
import de.d3web.we.terminology.D3webTerminologyHandler;
import de.d3web.we.testsuite.TestsuiteSection;
import de.d3web.we.upload.UploadManager;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class D3webModule implements KnowWEModule {

	private String defaultJarsPath = "/var/lib/tomcat-6/webapps/JSPWiki/KnowWEExtension/KBrepository/";
	private String ontfile = "file:resources/d3web.owl";
	private Map<Class<? extends KnowWEAction>, KnowWEAction> actionMap = new HashMap<Class<? extends KnowWEAction>, KnowWEAction>();

	private List<de.d3web.we.taghandler.TagHandler> tagHandlers = new ArrayList<de.d3web.we.taghandler.TagHandler>();

	private static D3webModule instance;

	private D3webTerminologyHandler handler = null;

	public static ResourceBundle getKwikiBundle_d3web() {

		return ResourceBundle.getBundle("KnowWE2_plugin_d3web_messages");
	}

	public static ResourceBundle getKwikiBundle_d3web(KnowWEUserContext user) {

		Locale.setDefault(KnowWEEnvironment.getInstance().getWikiConnector()
				.getLocale(user.getHttpRequest()));
		return getKwikiBundle_d3web();
	}

	public static ResourceBundle getKwikiBundle_d3web(HttpServletRequest request) {

		Locale.setDefault(KnowWEEnvironment.getInstance().getWikiConnector()
				.getLocale(request));
		return getKwikiBundle_d3web();
	}

	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
		rootTypes.add(new Kopic());
		rootTypes.add(new SolutionsSection());
		rootTypes.add(new QuestionnairesSection());
		rootTypes.add(new QuestionsSection());
		rootTypes.add(new AttributeTableSection());
		rootTypes.add(new XCL());
		rootTypes.add(new Annotation());
		rootTypes.add(new TestsuiteSection());
		rootTypes.add(new CoveringTableSection());
		rootTypes.add(new CoveringListSection());
		rootTypes.add(new RulesSection());
		rootTypes.add(new BulletScoring());
		return rootTypes;
	}

	@Override
	public List<TerminalType> getGlobalTypes() {
		return null;
	}

	public static D3webModule getInstance() {
		if (instance == null) {
			instance = new D3webModule();
		}
		return instance;
	}

	public String performAction(String action, KnowWEParameterMap parameterMap) {
		if (action == null) {
			action = parameterMap.get("action");
		}
		if (action != null) {
			action = "de.d3web.we.renderer." + action;
			Class clazz;
			try {
				clazz = Class.forName(action);
				KnowWEAction a = actionMap.get(clazz);
				return a.perform(parameterMap);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "class not found: " + action;
	}

	@Override
	public void initModule(ServletContext context) {
		this.addTagHandler(new QuestionSheetHandler());
		this.addTagHandler(new KBRenderer());
		this.addTagHandler(new ShowAllKBsHandler());
		this.addTagHandler(new SolutionStateViewHandler());
		this.addTagHandler(new WikiSolutionsTagHandler());
		this.addTagHandler(new DialogPaneTagHandler());
		this.addTagHandler(new ObjectInfoTagHandler());
		this.addTagHandler(new TestsuiteTagHandler());

		boolean registerRenderer = KnowWEEnvironment.getInstance()
				.registerConditionalRendererToType(Annotation.class,
						new AnnotationInlineAnswerRenderer());

		if (!registerRenderer) {
			Logger.getLogger(KnowWEUtils.class.getName()).warning(
					"Failed to register Renderer for Type: "
							+ Annotation.class.getName() + " - "
							+ AnnotationInlineAnswerRenderer.class.getName());

		}
		// Introduce my ontology parts to the core
		SemanticCore sc = SemanticCore.getInstance();
		sc.loadOwlFile(new File(ontfile));

		this.addAction(actionMap);
		this.loadData(context);

		UploadManager.getInstance().registerHandler(new KnOfficeUploadHandler());
		
		//add the javascript files
		KnowWEScriptLoader.getInstance().add("KnowWE-plugin-d3web.js",false);
		KnowWEScriptLoader.getInstance().add( "silveripe.0.2.js",false);
	}

	protected void addTagHandler(de.d3web.we.taghandler.TagHandler handler) {
		tagHandlers.add(handler);

	}

	/**
	 * On KnowWE initialisation> Loads the knowledgebases into the distributed
	 * reasoning engine.
	 * 
	 * @param context
	 */
	private void loadData(ServletContext context) {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String webPath = rb.getString("KWiki.config.path.webs");
		webPath = getRealPath(context, webPath);
		File path = new File(webPath);
		if (!path.exists()) {
			try {
				System.err.println("trying to create kb dir:"
						+ path.getAbsolutePath());
				File dweb = new File(path + "/default_web");
				dweb.mkdirs();
			} catch (SecurityException e) {
				System.err
						.println("KB directory creation failed, check permissions!! path:"
								+ path.getAbsolutePath());
				e.printStackTrace();
				// System.exit(1);
			}
		}
		DPSEnvironmentManager.getInstance().setWebEnvironmentLocation(webPath);
		File[] files = path.listFiles();
		if (files != null) {
			for (File each : files) {
				if (each.isDirectory()) {
					DPSEnvironmentManager.getInstance().createEnvironment(
							each.getName());
					// initArticleManager(each.getName());

				}
			}
		}

	}

	public void addAction(
			Map<Class<? extends KnowWEAction>, KnowWEAction> actionMap) {
		actionMap.put(GenerateKBAction.class, new GenerateKBAction());
		actionMap.put(SemanticAnnotationAction.class,
				new SemanticAnnotationAction());
		actionMap.put(GraphMLOwlRenderer.class, new GraphMLOwlRenderer());
		actionMap.put(DPSSolutionsAction.class, new DPSSolutionsAction());

		actionMap.put(KnowledgeSummerizeAction.class,
				new KnowledgeSummerizeAction());
		actionMap.put(ReInitDPSEnvironmentAction.class,
				new ReInitDPSEnvironmentAction());
		actionMap.put(RequestDialogRenderer.class, new RequestDialogRenderer());

		actionMap.put(DPSSolutionsAction.class, new DPSSolutionsAction());
		actionMap.put(DPSDialogsAction.class, new DPSDialogsAction());
		actionMap.put(UserFindingsAction.class, new UserFindingsAction());
		actionMap.put(KSSViewHistoryAction.class, new KSSViewHistoryAction());
		actionMap.put(ExplanationRenderer2.class, new ExplanationRenderer2());
		actionMap.put(SolutionLogAction.class, new SolutionLogAction());
		actionMap.put(QuestionStateReportAction.class,
				new QuestionStateReportAction());
		actionMap.put(CodeCompletionAction.class, new CodeCompletionAction());
		actionMap.put(XCLExplanationRenderer.class,
				new XCLExplanationRenderer());
		actionMap.put(SetSingleFindingAction.class,
				new SetSingleFindingAction());
		actionMap.put(ParseWebOfflineRenderer.class,
				new ParseWebOfflineRenderer());
		actionMap.put(ClearDPSSessionAction.class, new ClearDPSSessionAction());
		actionMap.put(RefreshHTMLDialogAction.class,
				new RefreshHTMLDialogAction());
		actionMap.put(SetFindingAction.class, new SetFindingAction());
		actionMap.put(SaveDialogAsXCLAction.class, new SaveDialogAsXCLAction());
	}

	/**
	 * returns a knowledge-service from the distributed reasoing engine for a
	 * given web and knowledge-service id
	 * 
	 * @param web
	 * @param id
	 * @return
	 */
	public KnowledgeService getKnowledgeService(String web, String id) {
		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironment(web, defaultJarsPath);
		return env.getService(id);
	}

	/**
	 * returns all KnowledgeServices for a given web.
	 * 
	 * @param web
	 * @return
	 */
	public Collection<KnowledgeService> getKnowledgeServices(String web) {
		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironment(web, defaultJarsPath);
		return env.getServices();
	}

	/**
	 * Returns a KnowledgeService for a given article name
	 * 
	 * @param web
	 * @param topic
	 * @return
	 */
	public D3webKnowledgeService getAD3webKnowledgeServiceInTopic(String web,
			String topic) {
		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironment(web, defaultJarsPath);
		Collection<KnowledgeService> coll = env.getServices();
		for (KnowledgeService knowledgeService : coll) {
			if (knowledgeService.getId().startsWith(topic + "..")) {
				if (knowledgeService instanceof D3webKnowledgeService) {
					return (D3webKnowledgeService) knowledgeService;
				}
			}
		}

		return null;
	}

	public static Broker getBroker(java.util.Map<String, String> parameterMap) {
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		return getBroker(user, web);
	}

	public static Broker getBroker(String user, String web) {
		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironments(web);
		Broker broker = env.getBroker(user);
		return broker;
	}

	public static DPSEnvironment getDPSE(
			java.util.Map<String, String> parameterMap) {
		// String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironments(web);
		return env;
	}

	public static DPSEnvironment getDPSE(String web) {

		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironments(web);
		return env;
	}

	public static URL getKbUrl(String web, String id) {
		String varPath = getWebEnvironmentPath(web);
		varPath = varPath + id + ".jar";
		URL url = null;
		try {
			url = new File(varPath).toURI().toURL();
		} catch (MalformedURLException e) {
			Logger.getLogger(KnowWEUtils.class.getName())
					.warning(
							"Cannot identify url for knowledgebase : "
									+ e.getMessage());
		}
		return url;
	}

	public static String getWebEnvironmentPath(String web) {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String sessionDir = rb.getString("KWiki.config.path.currentWeb");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = getRealPath(KnowWEEnvironment.getInstance()
				.getWikiConnector().getServletContext(), sessionDir);
		return sessionDir;
	}

	public static String getSessionPath(KnowWEParameterMap parameterMap) {
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String sessionDir = rb.getString("KWiki.config.path.sessions");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = sessionDir.replaceAll("\\$user\\$", user);

		sessionDir = getRealPath(parameterMap.getContext(), sessionDir);
		return sessionDir;
	}

	public static String getRealPath(ServletContext context, String varPath) {
		if (context != null && varPath.indexOf("$webapp_path$") != -1) {
			String realPath = context.getRealPath("");
			realPath = realPath.replace('\\', '/');
			while (realPath.endsWith("/")) {
				realPath = realPath.substring(0, realPath.length() - 1);
			}
			varPath = varPath.replaceAll("\\$webapp_path\\$", realPath);
		}
		return varPath;
	}

	@Override
	public void onSave(String topic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerKnowledgeRepresentationHandler(
			KnowledgeRepresentationManager mgr) {
		handler = new D3webTerminologyHandler();
		mgr.registerHandler("d3web", handler);

	}

	public D3webTerminologyHandler getKnowledgeRepresentationHandler() {
		return handler;
	}

	@Override
	public List<TagHandler> getTagHandlers() {
		return this.tagHandlers;
	}

	@Override
	public List<PageAppendHandler> getPageAppendHandlers() {
		return new ArrayList<PageAppendHandler>();
	}

}
