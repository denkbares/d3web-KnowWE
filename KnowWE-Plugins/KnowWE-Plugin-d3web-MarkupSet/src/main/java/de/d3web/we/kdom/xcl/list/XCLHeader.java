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

package de.d3web.we.kdom.xcl.list;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.LeftOfTokenFinder;
import de.knowwe.core.kdom.sectionFinder.MultiSectionFinder;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.LineSectionFinderNonBlankTrimmed;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.knowwe.kdom.sectionFinder.UnquotedExpressionFinder;

/**
 * A type for the head of a covering-list defining the solution that is described by that list. The solution is created
 * from the term found. Further, a covering-model is created.
 *
 * @author Jochen Reutelshöfer
 */
public class XCLHeader extends AbstractType {

	public XCLHeader() {
		// this section finder takes all content until the opening (unquoted) bracket "{",
		// or the first line if there is no such bracket
		this.setSectionFinder(new ConstraintSectionFinder(new MultiSectionFinder(
				new LeftOfTokenFinder("{", false),
				LineSectionFinderNonBlankTrimmed.getInstance()),
				AtMostOneFindingConstraint.getInstance()));

		// cut the optional '{'
		this.addChildType(new AnonymousType("bracket",
				new StringSectionFinderUnquoted("{"), StyleRenderer.COMMENT));

		// split multiple solutions by comma and/or semicolon
		this.addChildType(new AnonymousType("split",
				new UnquotedExpressionFinder(","), StyleRenderer.COMMENT));

		// and take the remaining ranges as solution definitions,
		// but also split multiple lines into individual solutions
		this.addChildType(new XCLSolutionDefinition(LineSectionFinderNonBlankTrimmed.getInstance()));
	}
}
