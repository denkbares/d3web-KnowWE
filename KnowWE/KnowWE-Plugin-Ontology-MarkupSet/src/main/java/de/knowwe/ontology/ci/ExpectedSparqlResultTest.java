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

import java.util.Collection;
import java.util.List;

import org.ontoware.rdf2go.model.QueryResultTable;

import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.d3web.testing.TestParameter.Mode;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.ci.provider.SparqlExpectedResultSection;
import de.knowwe.ontology.ci.provider.SparqlTestObjectProviderUtils;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.SparqlContentType;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdf2go.utils.ResultTableModel;

/**
 * @author jochenreutelshofer
 * @created 10.01.2014
 */
public class ExpectedSparqlResultTest extends AbstractTest<SparqlExpectedResultSection> {

	public static final String AT_LEAST = "atLeast";
	public static final String EQUAL = "equal";

	public ExpectedSparqlResultTest() {
		String[] comparators = new String[] {
				EQUAL, AT_LEAST };

		this.addParameter("comparator", Mode.Optional,
				"how to compare the result data against the expected table data: equal/atleast",
				comparators);
	}

	@Override
	public Message execute(SparqlExpectedResultSection testObject, String[] args, String[]... ignores) throws InterruptedException {
		Section<ExpectedSparqlResultTable> expectedResultTableSection = testObject.getSection();
		Section<DefaultMarkupType> defaultMarkup = Sections.ancestor(
				expectedResultTableSection,
				DefaultMarkupType.class);
		String sparqlName = DefaultMarkupType.getAnnotation(defaultMarkup,
				ExpectedSparqlResultTableMarkup.SPARQL_ANNOTATION);
		if (sparqlName == null) {
			return new Message(
					Type.FAILURE,
					"No sparql query specified for test object, use annotation 'sparql' to set a sparql query to test against.");
		}

		Collection<Section<SparqlContentType>> querySections = SparqlTestObjectProviderUtils.getSparqlQueryContentSection(sparqlName);
		if (querySections.size() > 1) {
			return new Message(Message.Type.ERROR,
					"Multiple sparql queries in the wiki with name: " + sparqlName);
		}

		if (querySections.size() == 0) {
			return new Message(Message.Type.ERROR,
					"No sparql query in the wiki found for name: " + sparqlName);
		}

		Section<SparqlContentType> querySection = querySections.iterator().next();
		Rdf2GoCompiler compiler = Compilers.getCompiler(querySection, Rdf2GoCompiler.class);
		Rdf2GoCore core = compiler.getRdf2GoCore();

		if (core == null) {
			return new Message(Message.Type.ERROR,
					"No repository found for section: " + querySection);
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core,
				querySection.getText());

		QueryResultTable resultSet = core.sparqlSelect(sparqlString);
		ResultTableModel actualResultTable = new ResultTableModel(resultSet);

		List<String> variables = actualResultTable.getVariables();

		ResultTableModel expectedResultTable = ExpectedSparqlResultTable.getResultTableModel(
				expectedResultTableSection, variables, compiler);

		boolean atLeastFlag = false;
		if (args.length > 0) {
			String arg0 = args[0];
			if (arg0.equalsIgnoreCase(AT_LEAST)) {
				atLeastFlag = true;
			}
		}

		List<Message> failures = ResultTableModel.checkEquality(expectedResultTable,
				actualResultTable, atLeastFlag);

		if (failures.size() > 0) {
			return new Message(Type.FAILURE, ResultTableModel.generateErrorsText(failures));
		}

		return Message.SUCCESS;
	}

	@Override
	public Class<SparqlExpectedResultSection> getTestObjectClass() {
		return SparqlExpectedResultSection.class;
	}

	@Override
	public String getDescription() {
		return "Test a expected sparql result set against the result set of the actually executed sparql query.";
	}

}
