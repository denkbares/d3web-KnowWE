package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.HermesUserManagement;

public class SetFilterLevelAction extends DeprecatedAbstractKnowWEAction {

    @Override
    public String perform(KnowWEParameterMap parameterMap) {
	String user = parameterMap.getUser();

	String level = parameterMap.get("level");

	if (level != null) {
	    try {
		int l = Integer.parseInt(level);
		HermesUserManagement.getInstance()
			.storeEventFilterLevelForUser(user, l);
		return "done";
	    } catch (Exception e) {
		// TODO
	    }
	}

	return "failed";
    }

}
