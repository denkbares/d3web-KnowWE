package de.d3web.we.ci4ke.test;

import de.d3web.testing.Message;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * @author Tim Abler
 * @created 07.11.2018
 */
public interface ResultRenderer  {
	default void renderResult(TestResult testResult, RenderResult renderResult) {
		CIRenderer.renderResultTitle(testResult, renderResult);
	}

	default void renderResultMessage(String web, String testObjectName, Message message, TestResult testResult, RenderResult renderResult) {
		CIRenderer.renderResultMessageDefault(web, testObjectName, message, testResult, renderResult);
	}
}
