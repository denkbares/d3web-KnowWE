/*
 * Copyright (C) 2016 denkbares GmbH, Germany 
 */
package de.knowwe.rightpanel.custom;

import java.io.IOException;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 03.11.16
 */
public class GetRightPanelContentAction extends de.knowwe.core.action.AbstractAction {

	private static final String ARTICLE_NAME = "RightPanel";

	@Override
	public void execute(UserActionContext context) throws IOException {
		Article article = context.getArticleManager().getArticle(ARTICLE_NAME);
		if (article != null) {
			RenderResult renderResult = new RenderResult(context);
			DelegateRenderer.getInstance().render(article.getRootSection(), context, renderResult);
			context.getWriter().write(renderResult.toString());
		} else {
			context.getWriter().write("<a href='Edit.jsp?page=RightPanel' class='createpage'>RightPanel</a>");
		}
	}
}
