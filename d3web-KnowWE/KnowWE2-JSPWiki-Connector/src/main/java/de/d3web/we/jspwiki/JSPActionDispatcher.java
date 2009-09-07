package de.d3web.we.jspwiki;


import com.ecyrd.jspwiki.WikiEngine;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEFacade;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class JSPActionDispatcher extends KnowWEActionDispatcher {
	
	private KnowWEFacade env;
	public JSPActionDispatcher(WikiEngine engine) {
		
		env = KnowWEFacade.getInstance();
	}
	public JSPActionDispatcher ()  {
		env = KnowWEFacade.getInstance();
	}
	
	 
	@Override
	public String performAction(KnowWEParameterMap parameterMap) {
		String action = parameterMap.get("action");
		String renderer =  parameterMap.get("renderer");
		parameterMap.put("env", "JSPWiki");
		parameterMap.put("KWikiWeb", "default_web");
		
		if(renderer != null && renderer.equals("KnowWEObjectTypeBrowserRenderer")) {			
			return env.collectAllSearchedSections(parameterMap);
		}
		
		if(renderer != null && renderer.equals("KnowWEObjectTypeActivationRenderer")) {			
			return env.switchKnowWeObjectActivation(parameterMap);
		}
		
		if(renderer != null && renderer.equals("TirexToXCLRenderer")) {			
			return env.tirexToXCL(parameterMap);
		}
		
		if(renderer != null && renderer.equals("GenerateKBRenderer")) {			
			return env.generateKB(parameterMap);
		}	
		
		if(renderer != null && renderer.equals("semAno")) {
			parameterMap.put("sendToUrl", "KnowWE.jsp");
			return env.semAno(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_dpsSolutions")) {
			return env.getActualSolutionState(parameterMap);
		}

		if(renderer != null && renderer.equals("graphml")) {
			return env.getGraphML(parameterMap);
		}
		
		
		if(renderer != null && renderer.equals("KWikiParseRenderer")) {
			return KnowWEEnvironment.getInstance().processAndUpdateArticle(parameterMap.getUser(),parameterMap.get(KnowWEAttributes.TEXT), parameterMap.get(KnowWEAttributes.TOPIC), parameterMap.get(KnowWEAttributes.WEB));			
			
		}
		if(action != null && action.equals("getParseReport")) {
			return env.getParseReport( parameterMap.get(KnowWEAttributes.TOPIC), parameterMap.get(KnowWEAttributes.WEB));
		}
		if(action != null && action.equals("ReplaceKDOMNode")) {
			return env.replaceKDOMNode(parameterMap);
		}
		if(action != null && action.equals("UpdateTableKDOmNodes")){
			return env.updateKDOMNodes(parameterMap);
		}
		if(action != null && action.equals("getQuestionState")) {
			return env.getQuestionState(parameterMap);
		}
		
		if(action != null && action.equals("RenamingRenderer")) {
			return env.getRenamingMask(parameterMap);
		}
		
		if(action != null && action.equals("KnowWEObjectTypeBrowserRenderer")) {
			return env.collectAllSearchedSections(parameterMap);
		}
		
		if(action != null && action.equals("GlobalReplaceAction")) {
			return env.replaceOperation(parameterMap);
		}
		
		if(action != null && action.equals("RefreshHTMLDialogAction")) {
			return env.refreshHTMLDialog(parameterMap);
		}
		if(action != null && action.equals("KWiki_dpsClear")) {
			return env.clearSession(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_allSolutions")) {
			return env.showAllSolutions(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_explain")) {
			//TODO reactivate
			//return env.showExplanation(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_solutionLog")) {
			return env.showSolutionLog(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_codeCompletion")) {
			return env.autoCompletion(parameterMap);
		}
		
		if(renderer != null && renderer.equals("XCLExplanation")) {
			return env.getXCLExplanation(parameterMap);
		}
			
		if(renderer != null && renderer.equals("KWiki_dpsDialogs")) {
			return env.getDialogs(parameterMap);
		}
		if(action != null && action.equals("setFinding")) {
			return env.setFinding(parameterMap);
		}
		if(action != null && action.equals("setSingleFinding")) {
			return env.setSingleFinding(parameterMap);
		}
		if(action != null && action.equals("setFinding")) {
			env.setFinding(parameterMap);
			return "findings set";
		}
		
		
		if(action != null && action.equals("DialogRenderer")) {
			return env.getDialogMask(parameterMap);
		}
		
		if(renderer != null && renderer.equals("ParseWebOffline")) {
			//parameterMap.put("dataFolder","/var/lib/kjspwiki");
			return env.parseAll(parameterMap);
		}
		
		if(renderer != null && renderer.equals("KWikiSummarizer")) {
			return env.summarize(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_viewKSSHistory")) {
			return env.getDialogHistory(parameterMap);
		}
		if(renderer != null && renderer.equals("KWiki_userFindings")) {
			return env.getUserFindings(parameterMap);
		}
		
		if(renderer != null && renderer.equals("KWiki_dialog")) {
			return env.requestDialog(parameterMap);
		}
		
		if(renderer != null && renderer.equals("KWiki_ReInitWebTermsRenderer")) {
			return env.reInitTerminologies(parameterMap);
		}
		
		if(action != null && action.equals("SetQuickEditFlagAction")) {
			return env.setQuickEdit( parameterMap );
		}
		
		if(action != null) {
			return env.performAction(parameterMap);
		}
		
		
		return "no action found";
	}

}

