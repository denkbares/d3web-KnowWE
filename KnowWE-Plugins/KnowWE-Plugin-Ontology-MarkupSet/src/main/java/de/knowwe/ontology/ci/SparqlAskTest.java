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

import de.d3web.testing.Message;
import de.d3web.testing.TestParameter;
import de.knowwe.ontology.ci.provider.SparqlTestObject;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 01.03.16.
 */
public class SparqlAskTest extends SparqlTest<SparqlTestObject> {

	public static final String WARNING = "warning";

	public SparqlAskTest() {
		this.addParameter("expected value",
				TestParameter.Mode.Mandatory,
				"expected boolean value to compare with", "true", "false");
		this.addParameter("warning", TestParameter.Mode.Optional, "show warning instead of failure if this test fails", WARNING);
	}

	@Override
	public Class<SparqlTestObject> getTestObjectClass() {
		return SparqlTestObject.class;
	}

	@Override
	public String getDescription() {
		return "Checks the truth value of given sparql ask query against an expected value.";
	}

	@Override
	public Message execute(SparqlTestObject testObject, String[] args, String[]... ignores) throws InterruptedException {
		Boolean expectedTruthValue = Boolean.parseBoolean(args[0]);
		Message.Type messageTypeTestFailed = Message.Type.FAILURE;
		if (args.length > 1 && args[1] != null && WARNING.equalsIgnoreCase(args[1])) {
			messageTypeTestFailed = Message.Type.WARNING;
		}

		Rdf2GoCompiler compiler = testObject.getCompiler();
		Rdf2GoCore core = compiler == null ? null : compiler.getRdf2GoCore();
		if (core == null) {
			return new Message(Message.Type.SKIPPED, "No ontology found for section: " + testObject.getName());
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, testObject.getSection().getText());

		boolean result = sparqlAsk(core, sparqlString);

		if (expectedTruthValue.equals(result)) {
			return new Message(Message.Type.SUCCESS);
		}
		else {
			return new Message(messageTypeTestFailed, "Sparql ASK expected  " + expectedTruthValue + " but was: " + result);
		}
	}
}
