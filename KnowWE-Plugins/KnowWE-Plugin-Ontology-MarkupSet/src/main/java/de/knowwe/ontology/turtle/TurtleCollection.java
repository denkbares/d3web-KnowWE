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
package de.knowwe.ontology.turtle;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.BNodeImpl;
import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.ontology.compile.provider.ResourceProvider;
import de.knowwe.ontology.compile.provider.StatementProvider;
import de.knowwe.ontology.compile.provider.StatementProviderResult;
import de.knowwe.ontology.turtle.TurtleCollection.ItemList.CollectionItem;

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

	static class ItemList extends AbstractType {

		public ItemList() {
			this.setSectionFinder(new AllTextFinderTrimmed());

			this.addChildType(new CollectionItem());
		}

		static class CollectionItem extends AbstractType {

			public CollectionItem() {

				this.addChildType(TurtleCollection.getInstance());
				this.addChildType(new BlankNode());
				this.addChildType(new BlankNodeID());
				this.addChildType(new TurtleLiteralType());
				this.addChildType(new EncodedTurtleURI());
				this.addChildType(new TurtleURI());

				this.setSectionFinder((text, father, type) -> SectionFinderResult.resultList(Strings.splitUnquoted(text,
						" ",
						false,
						TurtleMarkup.TURTLE_QUOTES)));
			}
		}
	}

	@Override
	public Value getNode(OntologyCompiler core, Section<? extends TurtleCollection> section) {
		return core.getRdf2GoCore().createBlankNode(section.getID());
	}

	@NotNull
	@Override
	public StatementProviderResult getStatements(OntologyCompiler compiler, Section<? extends TurtleCollection> section) {
		StatementProviderResult result = new StatementProviderResult(compiler);
		List<Section<CollectionItem>> listItems = new ArrayList<>();
		Sections.successors(section, CollectionItem.class, 2, listItems);
		org.eclipse.rdf4j.model.Resource listNode = getResource(compiler, section);
		if (!listItems.isEmpty()) {
			addListStatements(listNode, 0, listItems, result, compiler, section);
		}
		else {
			result.addStatement(listNode, org.eclipse.rdf4j.model.vocabulary.RDF.REST, org.eclipse.rdf4j.model.vocabulary.RDF.NIL);
		}
		return result;
	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	private void addListStatements(org.eclipse.rdf4j.model.Resource subject, int listIndex, List<Section<CollectionItem>> subList, StatementProviderResult result, OntologyCompiler core, Section<? extends TurtleCollection> collectionSection) {

		Section<CollectionItem> dataSection = subList.get(0);

		// search data value node
		Section<NodeProvider> dataNodeSection = Sections.successor(dataSection,
				NodeProvider.class);

		// add data triple
		result.addStatement(core.getRdf2GoCore().createStatement(subject, org.eclipse.rdf4j.model.vocabulary.RDF.FIRST,
				dataNodeSection.get().getNode(core, dataNodeSection)));

		// go on to next list element
		List<Section<CollectionItem>> nextSublist = subList.subList(1, subList.size());
		if (nextSublist.isEmpty()) {
			// end of list and end of recursion
			result.addStatement(core.getRdf2GoCore()
					.createStatement(subject, org.eclipse.rdf4j.model.vocabulary.RDF.REST, org.eclipse.rdf4j.model.vocabulary.RDF.NIL));
		}
		else {
			listIndex++;
			BNode nextListNode = core.getRdf2GoCore()
					.createBlankNode(collectionSection.getID()
							+ "_" + listIndex);
			result.addStatement(core.getRdf2GoCore()
					.createStatement(subject, org.eclipse.rdf4j.model.vocabulary.RDF.REST, nextListNode));

			addListStatements(nextListNode, listIndex,
					nextSublist, result, core, collectionSection);
		}
	}

	@Override
	public org.eclipse.rdf4j.model.Resource getResource(OntologyCompiler core, Section<? extends TurtleCollection> section) {
		return new BNodeImpl(getNode(core, section).stringValue());
	}
}
