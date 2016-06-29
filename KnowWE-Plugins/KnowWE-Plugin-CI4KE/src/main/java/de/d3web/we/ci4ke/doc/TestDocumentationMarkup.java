/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.doc;

import java.util.Collections;
import java.util.List;

import de.d3web.testing.Test;
import de.d3web.testing.TestManager;
import de.d3web.testing.TestParameter;
import de.d3web.testing.TestParameter.Mode;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Shows documentation for all tests that are available on the current KnowWE
 * installation
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 31.07.2012
 */
public class TestDocumentationMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestDocumentation");
	}

	public TestDocumentationMarkup() {
		super(MARKUP);
		this.setRenderer(new TestDocumentationRenderer());
	}

	class TestDocumentationRenderer extends DefaultMarkupRenderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			List<String> allTests = TestManager.findAllTestNames();
			Collections.sort(allTests);

			StringBuffer temp = new StringBuffer();

			temp.append("\n<table class='wikitable'>");

			appendHeader(temp);

			for (String testName : allTests) {
				appendTest(temp, testName);
			}

			temp.append("</table>\n");

			result.append("\n%%table-filter");
			result.append("\n%%sortable");
			result.append("\n");
			result.appendHtml(temp.toString());
			result.append("\n/%");
			result.append("\n/%");
			result.append("\n");
		}

		private void appendTest(StringBuffer buffer, String testName) {
			buffer.append("<tr>");

			Test<?> test = TestManager.findTest(testName);

			// name
			buffer.append("<td>");
			buffer.append(testName);
			buffer.append("</td>");

			// test object class
			buffer.append("<td>");
			buffer.append(getTestObjectName(test));
			buffer.append("</td>");

			// description
			buffer.append("<td>");
			appendDescription(buffer, test);
			appendParameterDetails(buffer,
					"Parameter Details",
					test.getParameterSpecification());
			appendParameterDetails(buffer,
					"This test also allows to specify items to be ignored",
					test.getIgnoreSpecification());
			buffer.append("</td>");

			buffer.append("</tr>\n");
		}

		private void appendDescription(StringBuffer buffer, Test<?> test) {
			buffer.append("<i>Synopsis: @test ").append(test.getName());
			buffer.append(" \u00AB").append(getTestObjectName(test)).append("\u00BB");
			for (TestParameter parameter : test.getParameterSpecification()) {
				boolean optional = Mode.Optional == parameter.getMode();
				if (optional) {
					buffer.append(" [\u00AB").append(parameter.getName()).append("\u00BB]");
				}
				else {
					buffer.append(" \u00AB").append(parameter.getName()).append("\u00BB");
				}
			}
			buffer.append("</i><p>");
			buffer.append(test.getDescription());
		}

		private void appendParameterDetails(StringBuffer buffer, String title, List<TestParameter> parameters) {
			if (parameters.isEmpty()) return;
			buffer.append("<p>").append(title).append(":<ul>");
			for (TestParameter parameter : parameters) {
				buffer.append("<li><i>&laquo;").append(parameter.getName()).append("&raquo;</i>");
				boolean optional = Mode.Optional == parameter.getMode();
				if (optional) buffer.append(" (optional)");
				buffer.append(": ");
				buffer.append("<br>").append(parameter.getDescription());
				buffer.append("</li>");
			}
			buffer.append("</ul>");
		}

		private String getTestObjectName(Test<?> test) {
			String testObjectName = "Object";
			Class<?> objectClass = test.getTestObjectClass();
			if (objectClass == null) {
				testObjectName = "Void";
			}
			if (Article.class.isAssignableFrom(objectClass)) {
				testObjectName = "Article";
			}
			else if (PackageManager.class.isAssignableFrom(objectClass)) {
				testObjectName = "Package";
			}
			else if (objectClass.getSimpleName().equals("KnowledgeBase")) {
				testObjectName = "KnowledgeBase";
			}
			else if (objectClass.getSimpleName().equals("TestCase")) {
				testObjectName = "TestCase";
			}
			else if (objectClass.getSimpleName().equals("OWLAPIConnector")) {
				testObjectName = "Ontology";
			}
			else if (objectClass.getSimpleName().equals("Rdf2GoCore")) {
				testObjectName = "Ontology";
			}
			return testObjectName;
		}

		private void appendHeader(StringBuffer buffer) {
			buffer.append("<tr>");

			buffer.append("<th>");
			buffer.append("Name of Test");
			buffer.append("</th>");

			buffer.append("<th>");
			buffer.append("Test Object");
			buffer.append("</th>");

			buffer.append("<th>");
			buffer.append("Description");
			buffer.append("</th>");

			buffer.append("</tr>");
		}

	}

}
