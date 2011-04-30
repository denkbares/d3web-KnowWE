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

import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.basic.EndLineComment;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;

public class RuleAction extends AbstractType {

	@Override
	protected void init() {
		sectionFinder = new AllTextSectionFinder();
		boolean notAttached = false;
		try {
			// TODO remove this evil workaround
			// when updating KnowWE architecture
			this.childrenTypes.add((Type) Class.forName(
						"cc.knowwe.tdb.EvalAssignActionType").newInstance());
		}
		catch (InstantiationException e) {
			notAttached = true;
		}
		catch (IllegalAccessException e) {
			notAttached = true;
		}
		catch (ClassNotFoundException e) {
			notAttached = true;
		}
		if (notAttached) {
			Logger.getLogger("KnowWE").log(Level.INFO,
					"cc.knowwe.tdb.EvalAssignActionType is not attached");
		}

		EndLineComment comment = new EndLineComment();
		comment.setCustomRenderer(StyleRenderer.COMMENT);
		this.childrenTypes.add(comment);
		this.childrenTypes.add(new SolutionValueAssignment());
		this.childrenTypes.add(new SetQuestionNumValueAction());
		this.childrenTypes.add(new SetQNumFormulaAction());
		this.childrenTypes.add(new SetQuestionValue());
		this.childrenTypes.add(new ContraIndicationAction());
		this.childrenTypes.add(new InstantIndication());
		this.childrenTypes.add(new RepeatedIndication());
		this.childrenTypes.add(new QASetIndicationAction());

	}
}
