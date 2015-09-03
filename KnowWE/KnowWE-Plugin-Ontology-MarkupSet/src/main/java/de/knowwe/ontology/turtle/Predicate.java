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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.kdom.renderer.CompositeRenderer;
import de.knowwe.ontology.edit.DropTargetRenderer;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.ontology.turtle.compile.URIProvider;
import de.knowwe.ontology.turtle.lazyRef.LazyURIReference;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;

import java.util.List;

public class Predicate extends AbstractType implements URIProvider<Predicate> {

	public Predicate() {
		this.setSectionFinder(new FirstWordFinder());

        this.addChildType(new PredicateAType());
        this.addChildType(new EncodedTurtleURI());
        this.addChildType(new TurtleURI());
		this.addChildType(new LazyURIReference());

		this.setRenderer(new CompositeRenderer(DelegateRenderer.getInstance(),
				new DropTargetRenderer()));
	}

	@Override
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public Node getNode(Section<Predicate> section, Rdf2GoCompiler core) {
		// there should be exactly one NodeProvider successor
		List<Section<?>> children = section.getChildren();
		for (Section<?> child : children) {
			// explicitly iterating children because Sections.findSuccessor()
			// returns 'this' otherwise
			Section<NodeProvider> nodeProviderChild = Sections.successor(child,
					NodeProvider.class);
			if (nodeProviderChild != null) {
				return nodeProviderChild.get().getNode(nodeProviderChild, core);
			}
		}
		return null;
	}

	@Override
	public URI getURI(Section<Predicate> section, Rdf2GoCompiler core) {
		return (URI) getNode(section, core);
	}
}
