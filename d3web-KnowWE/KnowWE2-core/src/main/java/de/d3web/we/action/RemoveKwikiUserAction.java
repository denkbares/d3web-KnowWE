package de.d3web.we.action;

import de.d3web.we.javaEnv.KnowWEParameterMap;

public class RemoveKwikiUserAction implements KnowWEAction {


	public String perform(KnowWEParameterMap map) {
//		String userId = map.get(KnowWEAttributes.USER);
		//[TODO]: Nothing? remove session + dir
		return "done";
	}

}
