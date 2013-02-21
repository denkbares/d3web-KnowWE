package de.knowwe.core.append;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

public class ProcessingStateAppendHandler implements PageAppendHandler {

	@Override
	public void append(String web, String topic, UserContext user, RenderResult result) {
		result.appendHtml("<div id='KnowWEProcessingIndicator' class='ajaxloader' state='idle'>"
				+ "<img src='KnowWEExtension/images/ajax-100.gif'></img>"
				+ "</div>");
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
