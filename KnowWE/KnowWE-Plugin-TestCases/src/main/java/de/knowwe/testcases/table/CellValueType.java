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
package de.knowwe.testcases.table;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.EmptyType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.basicType.Number;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;

/**
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class CellValueType extends AbstractType {

	public CellValueType() {
		setSectionFinder(new AllTextFinderTrimmed());
		addChildType(new EmptyType());
		addChildType(new KeywordType("UNKNOWN"));
		addChildType(new KeywordType("-?-"));
		Number number = new Number();
		number.setRenderer(DefaultTextRenderer.getInstance());
		number.setSectionFinder(new ConstraintSectionFinder(number.getSectionFinder(),
				SingleChildConstraint.getInstance()));
		addChildType(number);
		CellAnswerReference aRef = new CellAnswerReference();
		aRef.setRenderer(DefaultTextRenderer.getInstance());
		addChildType(aRef);
		aRef.setSectionFinder(new AllTextFinderTrimmed());
	}
}
