package de.d3web.KnOfficeParser.util;

import java.util.List;

import de.d3web.kernel.domainModel.NumericalInterval;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.ruleCondition.CondNumEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreater;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreaterEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumIn;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLess;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLessEqual;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.supportknowledge.Property;
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
