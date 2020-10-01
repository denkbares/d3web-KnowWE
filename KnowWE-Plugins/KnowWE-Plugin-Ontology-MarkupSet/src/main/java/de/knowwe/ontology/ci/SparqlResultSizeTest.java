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

import java.util.Iterator;

import org.eclipse.rdf4j.query.BindingSet;

import com.denkbares.semanticcore.TupleQueryResult;
import de.d3web.testing.Message;
import de.d3web.testing.ResultSizeTest;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.ci.provider.SparqlQuerySection;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * This test allows to verify the size of the result of a specific sparql query
 * (referenced by name). In that way one for instance can ensure the an ontology
 * contains at least/at most/exactly x instances of some class.
 *
 * @author Jochen Reutelshöfer
 * @created 08.01.2014
 */
public class SparqlResultSizeTest extends SparqlTest<SparqlQuerySection> implements ResultSizeTest {

	public SparqlResultSizeTest() {
		getParameters().forEach(this::addParameter);
	}

	@Override
	public Class<SparqlQuerySection> getTestObjectClass() {
		return SparqlQuerySection.class;
	}

	@Override
	public String getDescription() {
		return "Checks the size (amount of rows) of a given sparql query result against an expected value.";
	}

	@Override
	public Message execute(SparqlQuerySection query, String[] args, String[]... ignores) throws InterruptedException {
		Comparator comparator = getComparator(args, 0);
		int number = comparator.getNumber();
		Message.Type messageType = comparator.getMessageType();

		Section<SparqlContentType> contentSection = query.getSection();
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCoreForDefaultMarkupSubSection(contentSection);

		if (core == null) {
			return new Message(Message.Type.ERROR, "No repository found for section: " + contentSection);
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, contentSection.getText());

		// we obtain the time-out parameter from the query section (if existing)
		Rdf2GoCore.Options options = obtainQueryOptions(contentSection);

		TupleQueryResult resultSet = sparqlSelect(core, sparqlString, options);

		Iterator<BindingSet> iterator = resultSet.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			BindingSet binding = iterator.next();
			if (binding.size() == 0) {
				// invalid empty row - not actually a result
				continue;
			}
			count++;
		}

		if (comparator.getComparator().equals(GREATER_THAN)) {
			if (count > number) {
				return new Message(Message.Type.SUCCESS);
			}
			else {
				return getMessageGreater(messageType, number, count);
			}
		}

		if (comparator.getComparator().equals(SMALLER_THAN)) {
			if (count < number) {
				return new Message(Message.Type.SUCCESS);
			}
			else {
				return getMessageSmaller(messageType, number, count);
			}
		}

		if (comparator.getComparator().equals(EQUAL)) {
			if (count == number) {
				return new Message(Message.Type.SUCCESS);
			}
			else {
				return getMessageEqual(messageType, number, count);
			}
		}

		return new Message(Message.Type.ERROR, "Unknown error in test (invalid comparator?)");
	}


}
