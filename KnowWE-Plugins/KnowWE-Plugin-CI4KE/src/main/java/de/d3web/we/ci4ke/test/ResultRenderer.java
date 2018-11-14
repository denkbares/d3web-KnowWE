package de.d3web.we.ci4ke.test;

import de.d3web.testing.TestResult;
import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * @author Tim Abler
 * @created 07.11.2018
 */
public interface ResultRenderer  {
	void renderResult(TestResult testResult, RenderResult renderResult);
}
