package de.d3web.we.kdom.abstractiontable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.core.inference.condition.CondNum;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.core.session.values.NumValue;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;

public class QuestionNumCell extends AbstractType {

	private static Pattern INTERVAL_PATTERN = Pattern.compile("(\\[|\\]|\\() *(\\d+\\.?\\d*) +(\\d+\\.?\\d*) *(\\[|\\]|\\))");

	public QuestionNumCell() {
		StyleRenderer renderer = new StyleRenderer("color:rgb(125, 80, 102)");
		renderer.setMaskJSPWikiMarkup(false);
		this.setRenderer(renderer);
	}

	public ActionSetQuestion createNumActionSetValue(Article article, QuestionNum questionNum, Section<QuestionNumCell> questionNumCell) {
		NumericalInterval interval = questionNum.getInfoStore().getValue(
				BasicProperties.QUESTION_NUM_RANGE);
		String text = questionNumCell.getText().trim();
		Double parsedDouble = parseDouble("=", text);
		NumValue numValue = null;
		if (parsedDouble == null) {
			Messages.storeMessage(article, questionNumCell, this.getClass(),
					Messages.error("Unable to parse '" + text + "'"));
			return null;
		}
		if (interval != null && interval.contains(parsedDouble)) {
			Messages.storeMessage(article, questionNumCell, this.getClass(),
					Messages.error("Unable to parse '" + text + "'"));
			return null;
		}
		else {
			numValue = new NumValue(parsedDouble);
		}
		ActionSetQuestion action = new ActionSetQuestion();
		action.setQuestion(questionNum);
		action.setValue(numValue);
		Messages.clearMessages(article, questionNumCell, this.getClass());

		return action;
	}

	public CondNum createCondNum(Article article, QuestionNum questionNum, Section<QuestionNumCell> questionNumCell) {
		String text = questionNumCell.getText().trim();
		CondNum condNum = null;

		Matcher matcher = INTERVAL_PATTERN.matcher(text);
		if (matcher.find()) {
			NumericalInterval interval = createInterval(matcher);
			if (interval != null) {
				condNum = new CondNumIn(questionNum, interval);
			}
		}
		else {
			condNum = createCondNum(questionNum, text);
		}

		if (condNum == null) {
			Messages.storeMessage(article, questionNumCell, this.getClass(),
					Messages.error("Unable to parse '" + text + "'"));
		}
		else {
			Messages.clearMessages(article, questionNumCell, this.getClass());
		}
		return condNum;
	}

	private CondNum createCondNum(QuestionNum questionNum, String text) {
		CondNum condNum = null;
		Double parsedDouble = null;
		if (text.contains("<=")) {
			parsedDouble = parseDouble("<=", text);
			if (parsedDouble != null) {
				condNum = new CondNumLessEqual(questionNum, parsedDouble);
			}
		}
		else if (text.contains(">=")) {
			parsedDouble = parseDouble(">=", text);
			if (parsedDouble != null) {
				condNum = new CondNumGreaterEqual(questionNum, parsedDouble);
			}
		}
		else if (text.contains("<")) {
			parsedDouble = parseDouble("<", text);
			if (parsedDouble != null) {
				condNum = new CondNumLess(questionNum, parsedDouble);
			}
		}
		else if (text.contains(">")) {
			parsedDouble = parseDouble(">", text);
			if (parsedDouble != null) {
				condNum = new CondNumGreater(questionNum, parsedDouble);
			}
		}
		else {
			parsedDouble = parseDouble("=", text);
			if (parsedDouble != null) {
				condNum = new CondNumEqual(questionNum, parsedDouble);
			}
		}
		return condNum;
	}

	private NumericalInterval createInterval(Matcher matcher) {
		String leftBracket = matcher.group(1);
		String leftDigits = matcher.group(2);
		String rightDigits = matcher.group(3);
		String rightBracket = matcher.group(4);
		boolean leftOpen = leftBracket.equals("]") || leftBracket.equals("(");
		double left = Double.parseDouble(leftDigits);
		double right = Double.parseDouble(rightDigits);
		boolean rightOpen = rightBracket.equals("[") || rightBracket.equals(")");
		NumericalInterval numericalInterval = new NumericalInterval(left, right, leftOpen,
				rightOpen);
		try {
			numericalInterval.checkValidity();
		}
		catch (IntervalException e) {
			return null;
		}

		return numericalInterval;
	}

	private Double parseDouble(String remove, String text) {
		String cleaned = text.replaceAll(remove, "").trim();
		try {
			return Double.parseDouble(cleaned);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

}
