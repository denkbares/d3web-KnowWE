package de.d3web.we.action;

import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.taghandler.QuestionSheetHandler;

public class ReRenderQuestionSheetAction extends AbstractKnowWEAction {
	
	@Override
	public String perform(KnowWEParameterMap map) {	
		
		return QuestionSheetHandler.getInstance().render( map.getTopic(), map.getWikiContext(), null, map.getWeb());
		
	}

}
