package de.knowwe.ontology.ci;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.test.ResultRenderer;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.ontology.ci.provider.SparqlTestObjectProviderUtils;
import de.knowwe.ontology.sparql.SparqlMarkupType;

/**
 * @author Tim Abler
 * @created 14.11.2018
 */
public abstract class SparqlTests<T> extends AbstractTest<T> implements ResultRenderer {

	@Override
	public void renderResult(TestResult testResult, RenderResult renderResult) {

		// prepare some information
		Message summary = testResult.getSummary();
		String text = (summary == null) ? null : summary.getText();
		String[] config = testResult.getConfiguration();
		boolean hasConfig = config != null && !(config.length == 0);
		boolean hasText = !Strings.isBlank(text);

		String className = "";
		if (this.getName() != null) {
			className = this.getName() + ":";
		}

		String name;
		String additionalInfo = "";

		String title = this.getDescription().replace("'", "&#39;");

		if (hasConfig || hasText) {
			name = className;
			if (hasConfig) {

				Section<?> sparqlMarkupSection = getLinkTarget(config[0]);

				if (sparqlMarkupSection != null) {
					additionalInfo = "(<a href = '" + KnowWEUtils.getURLLink(sparqlMarkupSection) + "'>" + config[0] + "</a> "
							+ TestParser.concatParameters(1, config) + ")";
				}
				else {
					additionalInfo = "(" + TestParser.concatParameters(config) + ")";
				}
			}
			else {
				additionalInfo = text;
			}
		}
		else {
			name = testResult.getTestName();
		}

		renderResult.appendHtml("<span class='ci-test-title' title='" + title + "'>");
		renderResult.appendHtml(name);
		renderResult.appendHtml("<span class='ci-configuration'>" + additionalInfo + "</span>");
		renderResult.appendHtml("</span>");
	}

	@Nullable
	protected Section<?> getLinkTarget(String sectionName) {
		Section<SparqlMarkupType> sparqlMarkupSection = null;
		Collection<Section<SparqlMarkupType>> sparqlMarkupSections = SparqlTestObjectProviderUtils.getSparqlQuerySection(sectionName);
		if (!sparqlMarkupSections.isEmpty()) {
			sparqlMarkupSection = sparqlMarkupSections.iterator().next();
		}
		return sparqlMarkupSection;
	}
}
