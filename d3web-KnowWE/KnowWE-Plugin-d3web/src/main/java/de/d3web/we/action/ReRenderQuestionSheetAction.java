package de.d3web.we.action;

import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.taghandler.QuestionSheetHandler;

public class ReRenderQuestionSheetAction implements KnowWEAction{
	
	@Override
	public String perform(KnowWEParameterMap map) {	
		
		return QuestionSheetHandler.getInstance().render( map.get("ArticleTopic"), map.getWikiContext(), null, map.getWeb());
		
	}

}
