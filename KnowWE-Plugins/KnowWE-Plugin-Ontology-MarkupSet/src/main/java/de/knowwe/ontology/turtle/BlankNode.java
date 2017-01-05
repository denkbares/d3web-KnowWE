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

import org.eclipse.rdf4j.model.Resource;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.ontology.turtle.compile.ResourceProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public class BlankNode extends AbstractType implements ResourceProvider<BlankNode> {

	static final char OPEN_BLANK_NODE = '[';
	static final char CLOSE_BLANK_NODE = ']';

	public BlankNode() {
		this.setSectionFinder(new ExpressionInBracketsFinder(OPEN_BLANK_NODE, CLOSE_BLANK_NODE));

		AnonymousType openBracket = new AnonymousType("open bracket");
		openBracket.setSectionFinder(new RegexSectionFinder("^\\" + OPEN_BLANK_NODE));
		openBracket.setRenderer(new MaskBracketsRenderer());
		this.addChildType(openBracket);

		AnonymousType closingBracket = new AnonymousType("closing bracket");
		closingBracket.setRenderer(new MaskBracketsRenderer());
		closingBracket.setSectionFinder(new RegexSectionFinder("\\" + CLOSE_BLANK_NODE + "$"));
		this.addChildType(closingBracket);

		this.addChildType(PredicateObjectSentenceList.getInstance());

	}

	class MaskBracketsRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			result.append(section.getText().replaceAll("\\[", "~[").replaceAll("\\]", "~]"));

		}

	}

	@Override
	public org.eclipse.rdf4j.model.Value getNode(Section<BlankNode> section, Rdf2GoCompiler core) {
		return core.getRdf2GoCore().createBlankNode(section.getID());
	}

	@Override
	public Resource getResource(Section<BlankNode> section, Rdf2GoCompiler core) {
		return (Resource) getNode(section, core);
	}

}
