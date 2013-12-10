/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

/* THIS FILE IS GENERATED. DO NOT EDIT */

package de.knowwe.ontology.kdom.turtle;

import org.ontoware.rdf2go.model.node.Node;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.sectionFinder.SplitSectionFinderUnquoted;
import de.knowwe.ontology.kdom.relation.LiteralType;
import de.knowwe.rdf2go.Rdf2GoCore;

public class TurtleObjectSection extends AbstractType {

	public TurtleObjectSection() {

		this.addChildType(new LiteralType());
		this.addChildType(new TurtleObjectTerm());
		setSectionFinder(new SplitSectionFinderUnquoted(",", new char[] {
				'"', '\'' }));
	}

	public Node getNode(Rdf2GoCore core, Section<TurtleObjectSection> section) {
		Section<LiteralType> literal = Sections.findChildOfType(section, LiteralType.class);
		if (literal != null) return literal.get().getLiteral(core, literal);
		Section<TurtleObjectTerm> term = Sections.findChildOfType(section, TurtleObjectTerm.class);
		if (term != null) return term.get().getShortURI(core, term);
		return null;
	}

}