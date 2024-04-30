package de.knowwe.core.append;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.util.Icon;

public class ProcessingStateAppendHandler implements PageAppendHandler {

	@Override
	public void append(Article article, UserContext user, RenderResult result) {
		result.appendHtml(Icon.LOADING_LIGHT.addClasses("ajaxloader").addId("KnowWEProcessingIndicator")
				.addStyle("display: none;").increaseSize(Icon.Percent.by700).toHtml());
	}

	@Override
	public boolean isPre() {
		return false;
	}
}
