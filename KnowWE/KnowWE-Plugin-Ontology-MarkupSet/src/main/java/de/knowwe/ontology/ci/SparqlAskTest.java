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

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParameter;
import de.knowwe.ontology.ci.provider.SparqlQuerySection;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 01.03.16.
 */
public class SparqlAskTest extends AbstractTest<SparqlQuerySection> {

	public SparqlAskTest() {
		this.addParameter("expected value",
				TestParameter.Mode.Mandatory,
				"expected boolean value to compare with", "true", "false");
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
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCoreForDefaultMarkupSubSection(query.getSection());

		if (core == null) {
			return new Message(Message.Type.ERROR,
					"No repository found for section: " + query.getSection());
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, query.getSection().getText());

		boolean result = core.sparqlAsk(
				sparqlString);

		if(expectedTruthValue.equals(new Boolean(result))) {
			return new Message(Message.Type.SUCCESS);
		} else {
			return new Message(Message.Type.FAILURE,
					"Sparql ASK expected  " + expectedTruthValue + " but was: " + result);
		}
	}

}
