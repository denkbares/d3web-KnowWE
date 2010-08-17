package de.d3web.we.module;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.rendering.PageAppendHandler;
import de.d3web.we.kdom.validation.Validator;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ValidationPageAppendHandler implements PageAppendHandler {

	@Override
	public String getDataToAppend(String topic, String web,
			KnowWEUserContext user) {

		if (user.userIsAdmin()) {
			KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(web, topic);
			boolean valid = Validator.getTagHandlerInstance().validateArticle(article);
			String header = "<div id=\"validator-panel\" class=\"panel\"><h3>"
					+ KnowWEEnvironment.getInstance().getKwikiBundle(user).getString(
							"KnowWE.ValidatorHandler.header")
					+ "</h3><div><ul>";
			return valid ? "" : KnowWEUtils.maskHTML(header
					+ Validator.getTagHandlerInstance().getBuilder().toString()
					+ "</ul></div></div>");
		}
		else {
			return "";
		}
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
