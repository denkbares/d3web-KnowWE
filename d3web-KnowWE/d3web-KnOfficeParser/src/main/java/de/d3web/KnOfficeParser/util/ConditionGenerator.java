/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.KnOfficeParser.util;

import java.util.List;

import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.report.Message;

public class ConditionGenerator {
	
	public static TerminalCondition condNum(QuestionNum qnum, String op, Double d, List<Message> errors, int line, String linetext, String file) {
		TerminalCondition c;
		NumericalInterval range = (NumericalInterval) qnum.getProperties()
		.getProperty(Property.QUESTION_NUM_RANGE);
		if (op.equals("<")) {
			c = new CondNumLess(qnum, d);
			if (range!=null && d <= range.getLeft()) {
				errors.add(MessageKnOfficeGenerator.createIntervallOutOfBoundsWarning(file, line, linetext, d, qnum.getText(), op, range));
			}
		} else if (op.equals("<=")) {
			c = new CondNumLessEqual(qnum, d);
			if (range!=null && d < range.getLeft()) {
				errors.add(MessageKnOfficeGenerator.createIntervallOutOfBoundsWarning(file, line, linetext, d, qnum.getText(), op, range));
			}
		} else if (op.equals("=")) {
			c = new CondNumEqual(qnum, d);
			if (range!=null && (d < range.getLeft()||(d > range.getRight()))) {
				errors.add(MessageKnOfficeGenerator.createIntervallOutOfBoundsWarning(file, line, linetext, d, qnum.getText(), op, range));
			}
		} else if (op.equals(">")) {
			c = new CondNumGreater(qnum, d);
			if (range!=null && d >= range.getRight()) {
				errors.add(MessageKnOfficeGenerator.createIntervallOutOfBoundsWarning(file, line, linetext, d, qnum.getText(), op, range));
			}
		} else if (op.equals(">=")) {
			c = new CondNumGreaterEqual(qnum, d);
			if (range!=null && d > range.getRight()) {
				errors.add(MessageKnOfficeGenerator.createIntervallOutOfBoundsWarning(file, line, linetext, d, qnum.getText(), op, range));
			}
		} else {
			errors.add(MessageKnOfficeGenerator.createUnknownOpException(file, line, linetext, op));
			c = null;
		}
		return c;
	}
	
	public static TerminalCondition condNum(QuestionNum qnum, Double a, Double b, List<Message> errors, int line, String linetext, String file) {
		NumericalInterval range = (NumericalInterval) qnum.getProperties().getProperty(Property.QUESTION_NUM_RANGE);
		if (a > b) {
			errors.add(MessageKnOfficeGenerator.createIntervallRangeError(file, line, linetext));
			return null;
		}
		if ((range != null)
				&& ((range.getRight() <= a) || (range.getLeft() >= b))) {
			errors.add(MessageKnOfficeGenerator.createIntervallOutOfBoundsWarning(file, line, linetext, a, b, qnum.getText(), range));
		}
		return new CondNumIn(qnum, a, b);
	}

}
