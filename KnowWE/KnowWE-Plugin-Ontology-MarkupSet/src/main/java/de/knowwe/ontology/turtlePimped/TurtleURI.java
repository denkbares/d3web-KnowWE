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

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtlePimped.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class TurtleURI extends AbbreviatedResourceReference implements NodeProvider<TurtleURI> {

	public TurtleURI() {

		ConstraintSectionFinder c = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		setSectionFinder(c);
		c.addConstraint(AtMostOneFindingConstraint.getInstance());
	}


	@Override
	public Node getNode(Section<TurtleURI> section, Rdf2GoCore core) {
		String turtleURIText = section.getText();
		Section<ResourceReference> ref = Sections.findSuccessor(section, ResourceReference.class);
		if (ref != null) {
			Identifier identifier = ref.get().getTermIdentifier(ref);
			String shortURI = Strings.encodeURL(identifier.toExternalForm());
			String[] idPath = identifier.getPathElements();
				if (idPath.length == 2) {
					String suffix = Strings.encodeURL(idPath[1]);
					shortURI = idPath[0] + ":" + suffix;
				}
			String longURI = Rdf2GoUtils.expandNamespace(core, shortURI);
			return core.createURI(longURI);
		}
		else {

		if (turtleURIText.startsWith(":")) {
			turtleURIText = "lns:" + turtleURIText;
		}
		String uri = Rdf2GoUtils.expandNamespace(core, turtleURIText);
		return core.createURI(uri);
		}
	}

}