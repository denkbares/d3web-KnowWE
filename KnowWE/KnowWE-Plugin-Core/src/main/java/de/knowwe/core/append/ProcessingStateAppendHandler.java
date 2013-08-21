package de.knowwe.core.append;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

public class ProcessingStateAppendHandler implements PageAppendHandler {

	@Override
	public void append(String web, String title, UserContext user, RenderResult result) {
		result.appendHtml("<div id='KnowWEProcessingIndicator' class='ajaxloader' state='idle' style='display:none'>"
				+ "<img src='KnowWEExtension/images/ajax-100.gif' />"
				+ "</div>");
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
