package de.d3web.we.d3webModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import de.d3web.we.action.ClearDPSSessionAction;
import de.d3web.we.action.CodeCompletionRenderer;
import de.d3web.we.action.DPSDialogsRenderer;
import de.d3web.we.action.DPSSolutionsRenderer;
import de.d3web.we.action.ExplanationRenderer2;
import de.d3web.we.action.GenerateKBRenderer;
import de.d3web.we.action.KSSViewHistoryRenderer;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.action.KnowledgeSummerizeRenderer;
import de.d3web.we.action.ParseWebOfflineRenderer;
import de.d3web.we.action.QuestionStateReportAction;
import de.d3web.we.action.ReInitDPSEnvironmentRenderer;
import de.d3web.we.action.RefreshHTMLDialogAction;
import de.d3web.we.action.RequestDialogRenderer;
import de.d3web.we.action.SemanticAnnotationRenderer;
import de.d3web.we.action.SetFindingAction;
import de.d3web.we.action.SetSingleFindingAction;
import de.d3web.we.action.SolutionLogRenderer;
import de.d3web.we.action.TirexToXCLRenderer;
import de.d3web.we.action.UserFindingsRenderer;
import de.d3web.we.action.XCLExplanationRenderer;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Annotation.Annotation;
import de.d3web.we.kdom.TiRex.TiRex;
import de.d3web.we.kdom.kopic.Kopic;
import de.d3web.we.kdom.kopic.renderer.AnnotationInlineAnswerRenderer;
import de.d3web.we.kdom.xcl.XCL;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.renderer.xml.GraphMLOwlRenderer;
import de.d3web.we.taghandler.DialogPaneTagHandler;
import de.d3web.we.taghandler.KBRenderer;
import de.d3web.we.taghandler.ObjectInfoTagHandler;
import de.d3web.we.taghandler.QuestionSheetHandler;
import de.d3web.we.taghandler.ShowAllKBsHandler;
import de.d3web.we.taghandler.SolutionStateViewHandler;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.taghandler.TagHandlerType;
import de.d3web.we.taghandler.WikiSolutionsTagHandler;
import de.d3web.we.terminology.D3webTerminologyHandler;
import de.d3web.we.utils.KnowWEUtils;

public class D3webModule implements KnowWEModule {

	private String defaultJarsPath = "/var/lib/tomcat-6/webapps/JSPWiki/KnowWEExtension/KBrepository/";

	private Map<Class<? extends KnowWEAction>, KnowWEAction> actionMap = new HashMap<Class<? extends KnowWEAction>, KnowWEAction>();

	private List<de.d3web.we.taghandler.TagHandler> tagHandlers = new ArrayList<de.d3web.we.taghandler.TagHandler>();

	private static D3webModule instance;
	
	private static ResourceBundle kwikiBundle_d3web = ResourceBundle.getBundle("KnowWE2_plugin_d3web_messages");

	
	public ResourceBundle getKwikiBundle_d3web() {
		return kwikiBundle_d3web;
	}

	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
		rootTypes.add(new Kopic());
		rootTypes.add(new TiRex());
		rootTypes.add(new XCL());
		rootTypes.add(new Annotation());
		rootTypes.add(new TagHandlerType());
		return rootTypes;
	}

	public static D3webModule getInstance() {
		if(instance == null) {
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

		boolean registerRenderer = KnowWEEnvironment.getInstance()
				.registerConditionalRendererToType(Annotation.class,
						new AnnotationInlineAnswerRenderer());

		if (!registerRenderer) {
			Logger.getLogger(KnowWEUtils.class.getName()).warning(
					"Failed to register Renderer for Type: "
							+ Annotation.class.getName() + " - "
							+ AnnotationInlineAnswerRenderer.class.getName());

		}

		this.addAction(actionMap);

		loadData(context);

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
				//System.exit(1);
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
		actionMap.put(GenerateKBRenderer.class, new GenerateKBRenderer());
		actionMap.put(TirexToXCLRenderer.class, new TirexToXCLRenderer());
		actionMap.put(SemanticAnnotationRenderer.class,
				new SemanticAnnotationRenderer());
		actionMap.put(GraphMLOwlRenderer.class, new GraphMLOwlRenderer());
		actionMap.put(DPSSolutionsRenderer.class, new DPSSolutionsRenderer());

		actionMap.put(KnowledgeSummerizeRenderer.class,
				new KnowledgeSummerizeRenderer());
		actionMap.put(ReInitDPSEnvironmentRenderer.class,
				new ReInitDPSEnvironmentRenderer());
		actionMap.put(RequestDialogRenderer.class, new RequestDialogRenderer());

		actionMap.put(DPSSolutionsRenderer.class, new DPSSolutionsRenderer());
		actionMap.put(DPSDialogsRenderer.class, new DPSDialogsRenderer());
		actionMap.put(UserFindingsRenderer.class, new UserFindingsRenderer());
		actionMap.put(KSSViewHistoryRenderer.class,
				new KSSViewHistoryRenderer());
		actionMap.put(ExplanationRenderer2.class, new ExplanationRenderer2());
		actionMap.put(SolutionLogRenderer.class, new SolutionLogRenderer());
		actionMap.put(QuestionStateReportAction.class,
				new QuestionStateReportAction());
		actionMap.put(CodeCompletionRenderer.class,
				new CodeCompletionRenderer());
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
	}

	public String generateDialogLink(String user, String topic, String actualID) {
		return "<a target=\"kwiki-dialog\" href=\"KnowWE.jsp?renderer=KWiki_dialog&KWikisessionid="
				+ topic
				+ ".."
				+ actualID
				+ "&KWikiWeb=default_web&KWikiUser="
				+ user
				+ "\"><img src=\"KnowWEExtension/images/run.gif\" title=\"Fall im d3web-Dialog starten \"/></a>";
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

	private static void backupFile(String filestr) throws Exception {

		FileInputStream in = new FileInputStream(filestr);
		FileOutputStream out = new FileOutputStream(filestr + ".bak");

		FileChannel fcIn = in.getChannel();
		FileChannel fcOut = out.getChannel();

		MappedByteBuffer buf = fcIn.map(FileChannel.MapMode.READ_ONLY, 0, fcIn
				.size());
		fcOut.write(buf);

		fcIn.close();
		fcOut.close();

		File fin = new File(filestr);
		fin.delete();

	}

	public static boolean backupFile(String url, String type) {
		try {

			if (type.equals("jar")) {
				String realUrl = url.substring(4, url.length() - 2);
				System.out.println(realUrl);

				URL u = new URL(realUrl);
				backupFile(u.getFile());
				return true;
			} else if (type.equals("xml")) {
				backupFile(url);
				return true;
			} else
				return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static String getVariablePath(ServletContext context, String realPath) {
		String varPath = context.getRealPath("");
		varPath = varPath.replace('\\', '/');
		realPath = realPath.replaceAll(varPath, "\\$webapp_path\\$");
		return realPath;
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
		if (varPath.indexOf("$webapp_path$") != -1) {
			String realPath = context.getRealPath("");
			realPath = realPath.replace('\\', '/');
			while (realPath.endsWith("/")) {
				realPath  = realPath.substring(0, realPath.length() - 1);
			}
			varPath = varPath.replaceAll("\\$webapp_path\\$", realPath);
		}
		return varPath;
	}

	@Override
	public void onSave(String topic) {
		// TODO Auto-generated method stub

	}

//	@Override
//	public String renderTags(Map<String, String> params, String topic,
//			String user, String web) {
//
//		String tag = params.get("_cmdline");
//		if (this.tagHandlers.containsKey(tag.toLowerCase())) {
//			return tagHandlers.get(tag.toLowerCase()).render(topic, user,
//					params.get(tag.toLowerCase()), web);
//		}
//
//		return null;
//	}

	@Override
	public void registerKnowledgeRepresentationHandler(KnowledgeRepresentationManager mgr) {
		mgr.registerHandler("d3web", new D3webTerminologyHandler());

	}

	@Override
	public void findTypeInstances(Class clazz, List<KnowWEObjectType> instances) {
		for (KnowWEObjectType type : this.getRootTypes()) {
			type.findTypeInstances(clazz, instances);
		}

	}

	@Override
	public List<TagHandler> getTagHandlers() {
		return this.tagHandlers;
	}

	// private String generateReportLink(String topicname, String web) {
	// return "<a
	// href=\"KnowWE.jsp?action=getParseReport&topic="+topicname+"&KWiki_Topic="+topicname+"&web="+web+"\"
	// target=_blank><img src='KnowWEExtension/images/statistics.gif'
	// title='Report'/></a>";
	// }

	// @Override
	// public String preCacheModifications(String text, KnowledgeBase kb, String
	// topicname) {
	// return text;
	// }

}
