/*
 * Copyright (C) 2014 denkbares GmbH
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.utils.ResultTableChecker;
import com.denkbares.semanticcore.utils.ResultTableModel;
import de.d3web.testing.Message;
import de.d3web.testing.MessageObject;
import de.d3web.testing.TestParameter.Mode;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.ci.provider.SparqlExpectedResultSection;
import de.knowwe.ontology.ci.provider.SparqlTestObjectProviderUtils;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class ExpectedSparqlResultTest extends SparqlTest<SparqlExpectedResultSection> {

	public static final String AT_LEAST = "atLeast";
	public static final String EQUAL = "equal";

	public static final String WARNING = "warning";

	public ExpectedSparqlResultTest() {
		String[] comparators = new String[] {
				EQUAL, AT_LEAST };

		this.addParameter("comparator", Mode.Optional,
				"how to compare the result data against the expected table data: equal/atleast",
				comparators);
		this.addParameter("warning", Mode.Optional, "show warning instead of failure if this test fails", WARNING);
	}

	@Override
	public Message execute(SparqlExpectedResultSection testObject, String[] args, String[]... ignores) throws InterruptedException {
		boolean atLeastFlag = false;

		Message.Type messageTypeTestFailed = Message.Type.FAILURE;
		if (args.length > 1 && WARNING.equalsIgnoreCase(args[1])) {
			messageTypeTestFailed = Message.Type.WARNING;
		}

		if (args.length > 0) {
			String arg0 = args[0];
			if (arg0.equalsIgnoreCase(AT_LEAST)) {
				atLeastFlag = true;
			}
		}

		Section<ExpectedSparqlResultTableMarkup> expectedResultTableMarkup = testObject.getSection();
		String actualSparqlName = DefaultMarkupType.getAnnotation(expectedResultTableMarkup, ExpectedSparqlResultTableMarkup.SPARQL_ANNOTATION);
		if (actualSparqlName == null) {
			return new Message(messageTypeTestFailed, "No sparql query specified for test object, use annotation 'sparql' to set a sparql query to test against.");
		}

		Collection<Section<SparqlContentType>> actualSparqlSections = SparqlTestObjectProviderUtils.getSparqlQueryContentSection(actualSparqlName);
		if (actualSparqlSections.size() > 1) {
			return new Message(Message.Type.ERROR,
					"Multiple sparql queries in the wiki with name: " + actualSparqlName);
		}

		if (actualSparqlSections.isEmpty()) {
			return new Message(Message.Type.ERROR,
					"No sparql query in the wiki found for name: " + actualSparqlName);
		}

		Section<SparqlContentType> actualSparqlSection = actualSparqlSections.iterator().next();
		OntologyCompiler compiler = Compilers.getCompiler(actualSparqlSection, OntologyCompiler.class);
		assert compiler != null;
		Rdf2GoCore core = compiler.getRdf2GoCore();

		if (core == null) {
			return new Message(Message.Type.ERROR,
					"No repository found for section: " + actualSparqlSection);
		}

		String expectedSparqlName = DefaultMarkupType.getAnnotation(expectedResultTableMarkup, ExpectedSparqlResultTableMarkup.NAME_ANNOTATION);
		String actualSparqlString = Rdf2GoUtils.createSparqlString(core, actualSparqlSection.getText());
		CachedTupleQueryResult result;
		try {
			result = (CachedTupleQueryResult) sparqlSelect(core, actualSparqlString, getSparqlOptions(actualSparqlSection));
		}
		catch (Exception e) {
			Message message = new Message(messageTypeTestFailed, "Exception while executing SPARQL query: " + e.getMessage());
			appendMessageObjects(actualSparqlName, expectedSparqlName, message);
			return message;
		}
		ResultTableModel actualResultTable = ResultTableModel.create(result);

		List<String> variables = actualResultTable.getVariables();

		Section<ExpectedSparqlResultTable> expectedTable = Sections.successor(expectedResultTableMarkup, ExpectedSparqlResultTable.class);
		ResultTableModel expectedResultTable = ExpectedSparqlResultTable.getResultTableModel(compiler, expectedTable, variables);

		List<ResultTableChecker.Failure> failures = ResultTableChecker.checkEquality(expectedResultTable, actualResultTable, atLeastFlag);

		if (!failures.isEmpty()) {
			String errorsText = ResultTableChecker.generateErrorsText(failures, false);
			errorsText += "Expected result: " + expectedSparqlName + ", actual result: " + actualSparqlName;
			Message message = new Message(messageTypeTestFailed, errorsText);
			appendMessageObjects(actualSparqlName, expectedSparqlName, message);
			return message;
		}

		return Message.SUCCESS;
	}

	private void appendMessageObjects(String actualSparqlName, String expectedSparqlName, Message message) {
		ArrayList<MessageObject> messageObjects = new ArrayList<>();
		messageObjects.add(new MessageObject(actualSparqlName, Section.class));
		messageObjects.add(new MessageObject(expectedSparqlName, Section.class));
		message.setObjects(messageObjects);
	}

	@Override
	public Class<SparqlExpectedResultSection> getTestObjectClass() {
		return SparqlExpectedResultSection.class;
	}

	@Override
	public String getDescription() {
		return "Test a expected sparql result set against the result set of the actually executed sparql query.";
	}

	@Override
	protected @Nullable Section<?> getLinkTarget(String sectionName) {
		Collection<Section<ExpectedSparqlResultTableMarkup>> expectedQueryResultSection = SparqlTestObjectProviderUtils.getExpectedQueryResultSection(sectionName);
		if (expectedQueryResultSection.isEmpty()) {
			return null;
		}
		return expectedQueryResultSection.iterator().next();
	}
}
