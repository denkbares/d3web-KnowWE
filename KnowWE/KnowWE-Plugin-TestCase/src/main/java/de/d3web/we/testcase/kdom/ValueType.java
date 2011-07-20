/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testcase.kdom;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.basic.Number;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.rendering.DefaultTextRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created 19.07.2011
 */
public class ValueType extends AbstractType {

	@Override
	protected void init() {
		setSectionFinder(new AllTextSectionFinder());

		Number number = new Number();
		number.setCustomRenderer(DefaultTextRenderer.getInstance());
		number.setSectionFinder(new ConstraintSectionFinder(number.getSectionFinder(),
				SingleChildConstraint.getInstance()));
		addChildType(number);

		AnswerReference aRef = new AnswerReference() {
			@Override
			public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
				Section<RatedFinding> finding = Sections.findAncestorOfType(s, RatedFinding.class);
				return Sections.findChildOfType(finding, QuestionReference.class);
			}
		};

		// aRef.setCustomRenderer(DefaultTextRenderer.getInstance());
		aRef.setSectionFinder(new AllTextSectionFinder());

		addChildType(aRef);
	}

}
