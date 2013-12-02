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

import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.kdom.turtle.FirstWordFinder;
import de.knowwe.ontology.turtlePimped.compile.NodeProvider;
import de.knowwe.ontology.turtlePimped.compile.ResourceProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

public class Subject extends AbstractType implements ResourceProvider<Subject> {

	public Subject() {
		this.addChildType(new BlankNode());
		this.addChildType(new BlankNodeID());
		this.addChildType(new TurtleLongURI());
		this.addChildType(new TurtleURI());
		setSectionFinder(new FirstWordFinder());
	}

	@Override
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public Node getNode(Section<Subject> section, Rdf2GoCore core) {
		// there should be exactly one NodeProvider child (while potentially
		// many successors)
		Section<NodeProvider> nodeProviderChild = Sections.findChildOfType(section,
				NodeProvider.class);
		if (nodeProviderChild != null) {
			return nodeProviderChild.get().getNode(nodeProviderChild, core);
		}
		return null;
	}

	@Override
	public Resource getResource(Section<Subject> section, Rdf2GoCore core) {
		return (Resource) getNode(section, core);

	}

}