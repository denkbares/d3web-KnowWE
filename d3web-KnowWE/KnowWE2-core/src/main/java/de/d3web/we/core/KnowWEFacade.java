package de.d3web.we.core;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;

import de.d3web.we.action.GlobalReplaceAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.action.KnowWEObjectTypeBrowserRenderer;
import de.d3web.we.action.ParseWebOfflineRenderer;
import de.d3web.we.action.RenamingRenderer;
import de.d3web.we.action.ReplaceKDOMNodeAction;
import de.d3web.we.action.SetQuickEditFlagAction;
import de.d3web.we.action.UpdateTableKDOMNodes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.module.KnowWEModule;
import de.d3web.we.module.semantic.owl.UpperOntology2;
import de.d3web.we.renderer.xml.GraphMLOwlRenderer;
import de.d3web.we.upload.KnOfficeUploadManager;


/**
 * @author Jochen
 * 
 * This Facade offers Methods to perform user driven actions, coming by as a
 * (AJAX-) request through (for example) some instance of KnowWEActionDispatcher
 * (e.g. JspwikiActionDispatcher) Each method returns a String shown in the
 * user-view of the webapp. In the KnowWEParameterMap all
 * HTTP-Request-Attributes are stores to give the necessary informations to the
 * specific action.
 * 
 * 
 */
public class KnowWEFacade {

	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");

	/**
	 * holds all possible Actions, that can be performed by the KnowWEEngine
	 */
	private Map<Class<? extends KnowWEAction>, KnowWEAction> actionMap = new HashMap<Class<? extends KnowWEAction>, KnowWEAction>();

	/**
	 * Singleton instance
	 */
	private static KnowWEFacade instance;

	/**
	 * lazy singleton instance factory
	 * 
	 * @return
	 */
	public static synchronized KnowWEFacade getInstance() {
		if (instance == null) {
			instance = new KnowWEFacade();
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

	private KnowWEFacade() {
		initActions();
	}

	/**
	 * initialises all supported actions dont forget to register your new
	 * written actions here!
	 */
	/**
	 * 
	 */
	private void initActions() {
        actionMap.put(SetQuickEditFlagAction.class, new SetQuickEditFlagAction());

		actionMap.put(RenamingRenderer.class, new RenamingRenderer());
		actionMap.put(GlobalReplaceAction.class, new GlobalReplaceAction());

		actionMap.put(ReplaceKDOMNodeAction.class, new ReplaceKDOMNodeAction());
		actionMap.put(UpdateTableKDOMNodes.class, new UpdateTableKDOMNodes());
		actionMap.put(KnowWEObjectTypeBrowserRenderer.class, new KnowWEObjectTypeBrowserRenderer());

		List<KnowWEModule> modules = KnowWEEnvironment.getInstance()
				.getModules();
		for (KnowWEModule knowWEModule : modules) {
			knowWEModule.addAction(actionMap);
		}
	}

	public String replaceKDOMNode(KnowWEParameterMap map) {
		return this.actionMap.get(ReplaceKDOMNodeAction.class).perform(map);
	}

	/**
	 * Used for in-line table editing. Stores the changes back in the KDOM.
	 * 
	 * @param map
	 * @return
	 */
	public String updateKDOMNodes(KnowWEParameterMap map){
		return this.actionMap.get(UpdateTableKDOMNodes.class).perform(map);
	}
	
	/**
	 * 
	 * AutoCompletion for editing in the KnowWE-Wiki. Returns a list of
	 * propositions for continuing the users writing. Result is further
	 * processed and inserted by several javascript functions.
	 * 
	 * @param parameterMap
	 * @return propositions for autocompletion
	 */
	public String autoCompletion(KnowWEParameterMap parameterMap) {
		return performAction("CodeCompletionRenderer", parameterMap);

	}

	/**
	 * returns the current solution states to be shown in the solutions-panel
	 * 
	 * @param map
	 * @return
	 */
	public String getActualSolutionState(KnowWEParameterMap map) {
		return performAction("DPSSolutionsRenderer", map);

	}

	/**
	 * returns the graphml representation of one pages statements
	 * 
	 * @param map
	 * @return
	 */
	public String getGraphML(KnowWEParameterMap map) {
		KnowWEAction action = actionMap.get(GraphMLOwlRenderer.class);
		return action.perform(map);
	}

	/**
	 * renders the dialog history to be shown in the solutions-panel * * TODO:
	 * check whether stil usefull/necessary
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getDialogHistory(KnowWEParameterMap parameterMap) {
		return performAction("KSSViewHistoryRenderer", parameterMap);

	}

	/**
	 * renders the opened dialogs to be shown in the solutions-panel * TODO:
	 * check whether stil usefull/necessary
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getDialogs(KnowWEParameterMap map) {
		return performAction("DPSDialogsRenderer", map);
	}

	/**
	 * returns the answer state of a question used in GuideLineModule only
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getQuestionState(KnowWEParameterMap map) {
		return performAction("QuestionStateReportAction", map);
	}

	/**
	 * renders the global search and renaming tool
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getRenamingMask(KnowWEParameterMap parameterMap) {
		return this.actionMap.get(RenamingRenderer.class).perform(parameterMap);
	}

	/**
	 * renders all currently entered findings for a user
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getUserFindings(KnowWEParameterMap parameterMap) {
		return performAction("UserFindingsRenderer", parameterMap);
	}

	/**
	 * renders the XCL-Explanation for a certain solution
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getXCLExplanation(KnowWEParameterMap map) {
		return performAction("XCLExplanationRenderer", map);
	}

	/**
	 * clears a user session --> Start new Case --> all Findings deleted
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String clearSession(KnowWEParameterMap map) {
		return performAction("ClearDPSSessionAction", map);
	}

	/**
	 * (re-)parses the whole web/wiki
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String parseAll(KnowWEParameterMap parameterMap) {

		ResourceBundle bundle = ResourceBundle.getBundle("KnowWE_config");

		
		if (bundle.getString("knowwe2wiki.parseAllFunction").equals("true")) {

			if (parameterMap.getWikiContext().userIsAdmin()) {
				KnowWEAction action = actionMap
						.get(ParseWebOfflineRenderer.class);
				return action.perform(parameterMap);
			}

			return "<p class=\"info box\">"
					+ kwikiBundle.getString("KnowWE.login.error.admin")
					+ "</p>";
		}

		KnowWEAction action = actionMap.get(ParseWebOfflineRenderer.class);
		return action.perform(parameterMap);
	}

	/**
	 * updates the embedded HTML-Dialog to mark entered answers
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String refreshHTMLDialog(KnowWEParameterMap parameterMap) {
		return performAction("RefreshHTMLDialogAction", parameterMap);

	}

	/**
	 * regenerates the terminologies of the distributed reasoning engine
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String reInitTerminologies(KnowWEParameterMap parameterMap) {
		return performAction("ReInitDPSEnvironmentRenderer", parameterMap);
	}

	/**
	 * replaces a set of occurences of a textphrase with the specified
	 * replacement used by the global search and renaming tool
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String replaceOperation(KnowWEParameterMap map) {
		return performAction("GlobalReplaceAction", map);
	}

	/**
	 * Sets the QuickEditFlag in the UserSettingsManager.
	 * @param map
	 * @return
	 */
	public String setQuickEdit(KnowWEParameterMap map) {
		return performAction("SetQuickEditFlagAction", map);
	}
	
	/**
	 * starts the external dialog
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String requestDialog(KnowWEParameterMap map) {
		return performAction("RequestDialogRenderer", map);
	}

	/**
	 * renders the popup for an inline-Answer
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String semAno(KnowWEParameterMap parameterMap) {
		return performAction("SemanticAnnotationRenderer", parameterMap);
	}

	/**
	 * Sets findings for a question. For MCs possibly multiple at once
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String setFinding(KnowWEParameterMap parameterMap) {
		return performAction("SetFindingAction", parameterMap);
	}

	/**
	 * Sets a single finding for a question
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String setSingleFinding(KnowWEParameterMap parameterMap) {
		performAction("SetSingleFindingAction", parameterMap);
		return performAction("RefreshHTMLDialogAction", parameterMap);
	}

	/**
	 * Shows all solution states * * TODO: check whether stil usefull/necessary
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String showAllSolutions(KnowWEParameterMap parameterMap) {
		return performAction("DPSSolutionsRenderer", parameterMap);
	}

	// /**
	// * Renders Explanation Component - possibly outdated (from KnowWE1)
	// *
	// * TODO: check wether stil usefull/necessary
	// *
	// * @param parameterMap
	// * @return
	// */
	// public String showExplanation(KnowWEParameterMap parameterMap) {
	// ((RequestDialogRenderer) actionMap.get(RequestDialogRenderer.class))
	// .prepareDialog(parameterMap);
	// return actionMap.get(ExplanationRenderer2.class).perform(parameterMap);
	// }

	/**
	 * Shows solution log * * TODO: check whether stil usefull/necessary
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String showSolutionLog(KnowWEParameterMap parameterMap) {
		return performAction("SolutionLogRenderer", parameterMap);
	}

	/**
	 * Renders an overview of all knowledge articles with short stats *
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String summarize(KnowWEParameterMap parameterMap) {
		return performAction("KnowledgeSummerizeRenderer", parameterMap);
	}

	/**
	 * Allows to perform actions by Class
	 * 
	 * @param parameterMap
	 * @return
	 */
	public KnowWEAction getAction(Class<? extends KnowWEAction> clazz) {
		return actionMap.get(clazz);
	}

	/**
	 * Allows to perform actions by Classname
	 * 
	 * @param parameterMap
	 * @return
	 */
	public KnowWEAction getAction(String name) {
		Set<Class<? extends KnowWEAction>> classSet = actionMap.keySet();
		for (Class<? extends KnowWEAction> class1 : classSet) {
			if (class1.getName().equals(name)) {
				return actionMap.get(class1);
			}
		}
		return null;
	}

	/**
	 * Returns the embedded HTML-Dialog
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getDialogMask(KnowWEParameterMap parameterMap) {
		
		return performAction("RefreshHTMLDialogAction", parameterMap);
	}

	/**
	 * Returns the last parse-report of a specified article
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String getParseReport(String topic, String web) {
		return KnowWEEnvironment.getInstance().loadReport(topic, web);

	}

	/**
	 * Generates a KnowledgeBase from an Uploaded jarFile(Attachment).
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String generateKB(KnowWEParameterMap parameterMap) {
		String className = "GenerateKBRenderer";
		return performAction(className, parameterMap);
	}

	public String performAction(String action, KnowWEParameterMap parameterMap) {
		if (action == null) {
			action = parameterMap.get("action");
			if (action == null) {
				throw new IllegalArgumentException("no action specified");
			}
		}
		try {
			// check is action is fully qualified class name
			if (!action.contains(".")) {
				// if not, use d3web default package
				action = "de.d3web.we.action." + action;
			}
			Class clazz = Class.forName(action);
			KnowWEAction actionInstance = actionMap.get(clazz);
			if (actionInstance == null) {
				Logger.getLogger(this.getClass().getName()).warning(
						"Action/Render " + action + " wasn't registered => default instanciation");
				actionInstance = (KnowWEAction)clazz.newInstance();
			} 
			return actionInstance.perform(parameterMap);
		} 
		catch (ClassNotFoundException e) {
			String msg = "action's class not found: "+action;
			Logger.getLogger(this.getClass().getName()).severe(msg);
			throw new IllegalArgumentException(msg, e);
		}
		catch (InstantiationException e) {
			String msg = "action cannot be instanciated: "+action;
			Logger.getLogger(this.getClass().getName()).severe(msg);
			throw new IllegalArgumentException(msg, e);
		} 
		catch (IllegalAccessException e) {
			String msg = "action constructor cannot be accessed: "+action;
			Logger.getLogger(this.getClass().getName()).severe(msg);
			throw new IllegalArgumentException(msg, e);
		}
		catch (ClassCastException e) {
			String msg = "action is not an implementation of KnowWEAction: "+action;
			Logger.getLogger(this.getClass().getName()).severe(msg);
			throw new IllegalArgumentException(msg, e);
		}
	}

	public String performAction(KnowWEParameterMap parameterMap) {
		return this.performAction(null, parameterMap);
	}

	public String getNodeData(String web, String topic, String nodeID) {
		return KnowWEEnvironment.getInstance().getNodeData(web, topic, nodeID);
	}

	public String uploadFiles(Collection<FileItem> fileItems) {
		return KnOfficeUploadManager.getInstance().manageUpload(fileItems);
	}

	/**
	 * Generates XCL from a Tirex Input of a given Page.
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String tirexToXCL(KnowWEParameterMap parameterMap) {
		String className = "TirexToXCLRenderer";
		Class clazz;
		try {
			this.getClass();
			clazz = Class.forName(className);
			KnowWEAction action = actionMap.get(clazz);
			return action.perform(parameterMap);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Class not Found: " + className;
	}

	/**
	 * Switches the Activation-State of a KnowWeObjectType
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String switchKnowWeObjectActivation(KnowWEParameterMap parameterMap) {
		return performAction("KnowWEObjectTypeActivationRenderer", parameterMap);
	}
	
	/**
	 * Collects all Sections selected in KnowWEObjectTypeBrowser.
	 * 
	 * @param parameterMap
	 * @return
	 */
	public String collectAllSearchedSections(KnowWEParameterMap parameterMap) {
		KnowWEAction action = actionMap
		.get(KnowWEObjectTypeBrowserRenderer.class);
		return action.perform(parameterMap);
	}
	/**
	 * This returns a dump of the current Ontolgy
	 * @return
	 */
	public void writeOwl(OutputStream stream){
	    SemanticCore.getInstance().writeDump(stream);
	}

}
