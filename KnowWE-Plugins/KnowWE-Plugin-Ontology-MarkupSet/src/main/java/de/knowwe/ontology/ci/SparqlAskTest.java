/*
 * Copyright (C) 2016 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.ontology.ci;


import java.util.Collection;

import com.denkbares.strings.Strings;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParameter;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.test.ResultRenderer;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.ontology.ci.provider.SparqlQuerySection;
import de.knowwe.ontology.ci.provider.SparqlTestObjectProviderUtils;
import de.knowwe.ontology.sparql.SparqlMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 01.03.16.
 */
public class SparqlAskTest extends AbstractTest<SparqlQuerySection> implements ResultRenderer{

	public static final String WARNING = "warning";

	public SparqlAskTest() {
		this.addParameter("expected value",
				TestParameter.Mode.Mandatory,
				"expected boolean value to compare with", "true", "false");
		this.addParameter("warning", TestParameter.Mode.Optional, "show warning instead of failure if this test fails", WARNING);
	}

	@Override
	public Class<SparqlQuerySection> getTestObjectClass() {
		return SparqlQuerySection.class;
	}

	@Override
	public String getDescription() {
		return "Checks the truth value of given sparql ask query against an expected value.";
	}

	@Override
	public Message execute(SparqlQuerySection query, String[] args, String[]... ignores) throws InterruptedException {
		Boolean expectedTruthValue = Boolean.parseBoolean(args[0]);
		Message.Type messageTypeTestFailed = Message.Type.FAILURE;
		if (args.length > 1 && args[1] != null && WARNING.equalsIgnoreCase(args[1])){
			messageTypeTestFailed = Message.Type.WARNING;
		}

		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCoreForDefaultMarkupSubSection(query.getSection());

		if (core == null) {
			return new Message(Message.Type.ERROR,
					"No repository found for section: " + query.getSection());
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, query.getSection().getText());

		boolean result = core.sparqlAsk(
				sparqlString);

		if (expectedTruthValue.equals(result)) {
			return new Message(Message.Type.SUCCESS);
		} else {
			return new Message(messageTypeTestFailed,
					"Sparql ASK expected  " + expectedTruthValue + " but was: " + result);
		}
	}

	@Override
	public void renderResult(TestResult testResult, RenderResult renderResult) {

		// prepare some information
		Message summary = testResult.getSummary();
		String text = (summary == null) ? null : summary.getText();
		String[] config = testResult.getConfiguration();
		boolean hasConfig = config != null && !(config.length == 0);
		boolean hasText = !Strings.isBlank(text);

		String name = "";

		String title = this.getDescription().replace("'", "&#39;");

		if (hasConfig || hasText) {
			if (hasConfig) {
				Collection<Section<SparqlMarkupType>> testObj = SparqlTestObjectProviderUtils.getSparqlQuerySection(config[0]);

				if (!testObj.isEmpty() && Sections.ancestor(testObj.iterator().next(), SparqlMarkupType.class) != null) {
					Section<SparqlMarkupType> markup = Sections.ancestor(testObj.iterator()
							.next(), SparqlMarkupType.class);
					assert(markup != null);
					name = "(" + "<a href = '" + KnowWEUtils.getURLLink(markup) + "'>" + config[0] + "</a> " + TestParser
							.concatParameters(1, config) + ")";
				}
				else {
					name = "(" + TestParser.concatParameters(config) + " )";
				}
			}
			if (hasText) {
				name = name + ": " + text;
			}
		}
		else {
			name = testResult.getTestName();
		}

		renderResult.appendHtml("<span class='ci-test-title' title='" + title + "'>");
		renderResult.appendHtml(name);
		renderResult.appendHtml("</span>");
	}
}
