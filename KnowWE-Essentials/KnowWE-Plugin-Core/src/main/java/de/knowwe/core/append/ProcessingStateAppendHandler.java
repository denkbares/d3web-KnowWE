package de.knowwe.core.append;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.util.Icon;

public class ProcessingStateAppendHandler implements PageAppendHandler {

	@Override
	public void append(Article article, UserContext user, RenderResult result) {
		result.appendHtml(Icon.LOADING.addClasses("ajaxloader").addId("KnowWEProcessingIndicatore")
				.addStyle("scale: 7; display: none; state='idle'").toHtml());
	}

	@Override
	public boolean isPre() {
		return false;
	}
}
