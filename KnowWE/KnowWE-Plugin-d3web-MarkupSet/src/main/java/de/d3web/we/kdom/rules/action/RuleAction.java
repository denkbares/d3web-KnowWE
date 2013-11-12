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

package de.d3web.we.kdom.rules.action;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.EndLineComment;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.kdom.renderer.StyleRenderer;

public class RuleAction extends AbstractType {

	public RuleAction() {
		setSectionFinder(AllTextSectionFinder.getInstance());
		EndLineComment comment = new EndLineComment();
		comment.setRenderer(StyleRenderer.COMMENT);
		this.addChildType(comment);
		this.addChildType(new SolutionValueAssignment());
		this.addChildType(new SetQuestionNumValueAction());
		this.addChildType(new SetQNumFormulaAction());
		this.addChildType(new SetQuestionValue());
		this.addChildType(new ContraIndicationAction());
		this.addChildType(new InstantIndication());
		this.addChildType(new RepeatedIndication());
		this.addChildType(new QASetIndicationAction());

	}
}
