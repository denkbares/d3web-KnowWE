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

import java.util.List;

import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public class BlankNodeID extends AbstractType implements NodeProvider<BlankNodeID> {

	public BlankNodeID() {
		this.setSectionFinder(new BlankNodeIDFinder());
	}

	class BlankNodeIDFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			if (text.trim().startsWith("_:")) {
				return new AllTextFinderTrimmed().lookForSections(text, father, type);
			}
			return null;
		}
	}

	@Override
	public Value getNode(Section<BlankNodeID> section, Rdf2GoCompiler core) {
		Section<TurtleSentence> content = Sections.ancestor(section,
				TurtleSentence.class);
		return core.getRdf2GoCore().createBlankNode(content.getID() + "_" + section.getText());
	}
}
