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

import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.Number;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created 19.07.2011
 */
public class ValueType extends AbstractType {

	public ValueType() {
		setSectionFinder(AllTextFinder.getInstance());

		Number number = new Number();
		number.setRenderer(DefaultTextRenderer.getInstance());
		number.setSectionFinder(new ConstraintSectionFinder(number.getSectionFinder(),
				SingleChildConstraint.getInstance()));
		addChildType(number);

		AnswerReference aRef = new AnswerReference() {

			@Override
			public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
				Section<RatedFinding> finding = Sections.ancestor(s, RatedFinding.class);
				return Sections.child(finding, QuestionReference.class);
			}
		};

		// aRef.setCustomRenderer(DefaultTextRenderer.getInstance());
		aRef.setSectionFinder(AllTextFinder.getInstance());

		addChildType(aRef);
	}

}
