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
package de.knowwe.ontology.turtlePimped;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.vocabulary.RDF;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.ontology.kdom.relation.LiteralType;
import de.knowwe.ontology.turtlePimped.TurtleCollection.ItemList.CollectionItem;
import de.knowwe.ontology.turtlePimped.compile.NodeProvider;
import de.knowwe.ontology.turtlePimped.compile.ResourceProvider;
import de.knowwe.ontology.turtlePimped.compile.StatementProvider;
import de.knowwe.ontology.turtlePimped.compile.StatementProviderResult;
import de.knowwe.rdf2go.Rdf2GoCore;

public class TurtleCollection extends AbstractType implements ResourceProvider<TurtleCollection>, StatementProvider<TurtleCollection> {

	private static TurtleCollection instance = null;

	public static TurtleCollection getInstance() {
		if (instance == null) {
			instance = new TurtleCollection();
			instance.init();
		}
		return instance;
	}

	private void init() {
		this.setSectionFinder(new ExpressionInBracketsFinder(OPEN_COLLECTION, CLOSE_COLLECTION));

		AnonymousType openBracket = new AnonymousType("open bracket");
		openBracket.setSectionFinder(new RegexSectionFinder("^\\" + OPEN_COLLECTION));
		this.addChildType(openBracket);

		AnonymousType closingBracket = new AnonymousType("closing bracket");
		closingBracket.setSectionFinder(new RegexSectionFinder("\\" + CLOSE_COLLECTION + "$"));
		this.addChildType(closingBracket);

		this.addChildType(new ItemList());

	}

	static final char OPEN_COLLECTION = '(';
	static final char CLOSE_COLLECTION = ')';

	private TurtleCollection() {
		// intialization is performed in init()
	}

	class ItemList extends AbstractType {

		public ItemList() {
			this.setSectionFinder(new AllTextFinderTrimmed());

			this.addChildType(new CollectionItem());
		}

		class CollectionItem extends AbstractType {

			public CollectionItem() {

				this.addChildType(TurtleCollection.getInstance());
				this.addChildType(new BlankNode());
				this.addChildType(new BlankNodeID());
				this.addChildType(new LiteralType());
				this.addChildType(new TurtleLongURI());
				this.addChildType(new TurtleURI());

				this.setSectionFinder(new SectionFinder() {

					@Override
					public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
						return SectionFinderResult.createResultList(Strings.splitUnquoted(text,
								" ",
								false,
								TurtleMarkup.TURTLE_QUOTES));
					}
				});
			}
		}

	}

	@Override
	public Node getNode(Section<TurtleCollection> section, Rdf2GoCore core) {
		return core.createBlankNode(section.getID());
	}

	@Override
	public StatementProviderResult getStatements(Section<TurtleCollection> section, Rdf2GoCore core) {
		StatementProviderResult result = new StatementProviderResult();
		List<Section<CollectionItem>> listItems = new ArrayList<Section<CollectionItem>>();
		Sections.findSuccessorsOfType(section,
				CollectionItem.class, 2, listItems);
		Resource listNode = getResource(section, core);
		if (listItems.size() > 0) {
			addListStatements(listNode, 0, listItems, result, core, section);
		}
		else {
			result.addStatement(core.createStatement(listNode, RDF.rest, RDF.nil));
		}

		return result;
	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	private void addListStatements(Resource subject, int listIndex, List<Section<CollectionItem>> subList, StatementProviderResult result, Rdf2GoCore core, Section<TurtleCollection> collectionSection) {

		if (subList.size() > 0) {
			Section<CollectionItem> dataSection = subList.get(0);

			// search data value node
			Section<NodeProvider> dataNodeSection = Sections.findSuccessor(dataSection,
					NodeProvider.class);

			// add data triple
			result.addStatement(core.createStatement(subject, RDF.first,
					dataNodeSection.get().getNode(dataNodeSection, core)));

			// go on to next list element
			listIndex++;
			org.ontoware.rdf2go.model.node.BlankNode nextListNode = core.createBlankNode(collectionSection.getID()
					+ "_" + listIndex);
			result.addStatement(core.createStatement(subject, RDF.rest, nextListNode));

			addListStatements(nextListNode, listIndex,
					subList.subList(1, subList.size()), result, core, collectionSection);
		}
		else {
			// end of list and end of recursion
			result.addStatement(core.createStatement(subject, RDF.rest, RDF.nil));
		}

	}

	@Override
	public Resource getResource(Section<TurtleCollection> section, Rdf2GoCore core) {
		return (Resource) getNode(section, core);
	}

}
