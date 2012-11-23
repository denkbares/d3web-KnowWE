package de.knowwe.core.append;

import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;

public class ProcessingStateAppendHandler implements PageAppendHandler {

	@Override
	public String getDataToAppend(String topic, String web, UserContext user) {
		return Strings.maskHTML("<div id='KnowWEProcessingIndicator' class='ajaxloader' state='idle'>"
				+ "<img src='KnowWEExtension/images/ajax-100.gif'></img>"
				+ "</div>");
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
