package de.d3web.we.ci4ke.test;

import de.d3web.testing.Message;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * @author Tim Abler
 * @created 07.11.2018
 */
public interface ResultRenderer  {

	default void renderResultTitle(TestResult testResult, RenderResult renderResult) {
		CIRenderer.renderResultTitle(testResult, renderResult);
	}

	default void renderResultMessage(UserContext context, String testObjectName, Message message, TestResult testResult, RenderResult renderResult) {
		CIRenderer.renderResultMessageDefault(context, testObjectName, testResult, message, renderResult);
	}
}
