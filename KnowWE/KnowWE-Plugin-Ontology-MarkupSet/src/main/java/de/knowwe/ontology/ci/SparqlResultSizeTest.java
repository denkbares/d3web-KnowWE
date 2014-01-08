/*
 * Copyright (C) 2013 denkbares GmbH
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
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParameter.Mode;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.SparqlContentType;
import de.knowwe.rdf2go.sparql.SparqlMarkupType;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * This test allows to verify the size of the result size of a specific sparql
 * query (referenced by name). In that way one for instance can ensure the an
 * ontology contains at least/at most/exactly x instances of some class.
 * 
 * @author jochenreutelshofer
 * @created 08.01.2014
 */
public class SparqlResultSizeTest extends AbstractTest<ArticleManager> {


	public static final String GREATER_THAN = ">";
	public static final String SMALLER_THAN = "<";
	public static final String EQUAL = "=";

	public SparqlResultSizeTest() {
		String[] comparators = new String[] {
				EQUAL, SMALLER_THAN, GREATER_THAN };

		this.addParameter("sparqlQueryName", de.d3web.testing.TestParameter.Type.String,
				Mode.Mandatory,
				"The name of the sparql query to be checked.");
		this.addParameter("comparator", Mode.Mandatory,
				"how to compare the result size against the expected size", comparators);
		this.addParameter("expected size", de.d3web.testing.TestParameter.Type.Number,
				Mode.Mandatory,
				"expected size to compare with");
	}

	@Override
	public Class<ArticleManager> getTestObjectClass() {
		return ArticleManager.class;
	}

	@Override
	public String getDescription() {
		return "Checks the size (amount of rows) of a given sparql query result against an expected value.";
	}

	@Override
	public Message execute(ArticleManager articleManager, String[] args, String[]... ignores) throws InterruptedException {
		String sparqlName = args[0];
		String comparator = args[1];
		double number = Double.parseDouble(args[2]);

		List<Section<? extends Type>> sparqlSections = Sections.findSectionsOfTypeGlobal(
				SparqlMarkupType.class, articleManager);

		List<Section<? extends Type>> sparqlSectionsWithName = new ArrayList<Section<? extends Type>>();

		for (Section<? extends Type> section : sparqlSections) {
			String name = DefaultMarkupType.getAnnotation(section, SparqlMarkupType.NAME);
			if (sparqlName.equalsIgnoreCase(name)) {
				sparqlSectionsWithName.add(section);
			}
		}

		if (sparqlSectionsWithName.size() > 1) {
			return new Message(Message.Type.ERROR,
					"Multiple sparql queries in the wiki with name: " + sparqlName);
		}

		if (sparqlSectionsWithName.size() == 0) {
			return new Message(Message.Type.ERROR,
					"No sparql query in the wiki found for name: " + sparqlName);
		}

		Section<? extends Type> sparqlDefaultMarkupSection = sparqlSectionsWithName.get(0);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(Sections.cast(sparqlDefaultMarkupSection,
				DefaultMarkupType.class));

		String sparqlString = Rdf2GoUtils.createSparqlString(core, Sections.findSuccessor(
				sparqlDefaultMarkupSection, SparqlContentType.class).getText());

		QueryResultTable resultSet = core.sparqlSelect(
				sparqlString);

		ClosableIterator<QueryRow> iterator = resultSet.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}

		if (comparator.equals(GREATER_THAN)) {
			if (count > number) {
				return new Message(Message.Type.SUCCESS);
			}
			else {
				return new Message(Message.Type.FAILURE,
						"Result size should be greater than " + (int) number + " but was: " + count);
			}
		}

		if (comparator.equals(SMALLER_THAN)) {
			if (count < number) {
				return new Message(Message.Type.SUCCESS);
			}
			else {
				return new Message(Message.Type.FAILURE,
						"Result size should be smaller than " + (int) number + " but was: " + count);
			}
		}

		if (comparator.equals(EQUAL)) {
			if (count == number) {
				return new Message(Message.Type.SUCCESS);
			}
			else {
				return new Message(Message.Type.FAILURE,
						"Result size should be " + (int) number + " but was: " + count);
			}
		}

		return new Message(Message.Type.ERROR,
				"Unknown error in test (invalid comparator?)");

	}

}
