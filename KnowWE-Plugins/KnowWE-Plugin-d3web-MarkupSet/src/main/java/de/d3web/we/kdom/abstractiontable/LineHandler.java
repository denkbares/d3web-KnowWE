package de.d3web.we.kdom.abstractiontable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondKnown;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.CondUnknown;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.Score;
import de.d3web.we.kdom.condition.SolutionStateType;
import de.d3web.we.kdom.rules.utils.RuleCreationUtil;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;

public class LineHandler implements D3webCompileScript<TableLine> {

	private static final Pattern INTERVAL_PATTERN = Pattern.compile("([\\[\\](]) *(\\d+\\.?\\d*) +(\\d+\\.?\\d*) *([\\[\\])])");

	@Override
	public void compile(D3webCompiler compiler, Section<TableLine> section) throws CompilerMessage {

		if (TableUtils.isHeaderRow(section)) {
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		List<Section<CellContent>> cells = Sections.successors(section, CellContent.class);
		Iterator<Section<CellContent>> cellIter = cells.iterator();

		List<Condition> conditions = new ArrayList<>(cells.size());
		List<PSAction> actions = new ArrayList<>();

		while (cellIter.hasNext()) {
			Section<CellContent> cell = cellIter.next();
			if (cellIter.hasNext()) {
				// it's a condition cell
				conditions.addAll(createCondition(compiler, cell));
			}
			else {
				// it's the action cell then
				actions.addAll(createAction(compiler, cell));
			}
		}

		if (actions.isEmpty() || conditions.isEmpty()) {
			int row = TableUtils.getRow(section);
			String message = "Rule for row " + row + " was not created (no "
					+ (actions.isEmpty() ? "action" : "conditions")
					+ " found)";
			throw CompilerMessage.error(message);
		}

		/* Rule rule = */
		RuleCreationUtil.createRules(conditions, actions);
		throw CompilerMessage.info();
	}

	private List<PSAction> createAction(D3webCompiler compiler, Section<CellContent> contentCell) {
		List<PSAction> result = new ArrayList<>();
		List<Section<CellContentValue>> cells = Sections.successors(contentCell, CellContentValue.class);
		for (Section<CellContentValue> valueSection : cells) {
			PSAction action = null;
			CellContentValue.CellType type = valueSection.get().getType(compiler, valueSection);
			if (type == CellContentValue.CellType.ANSWER_REFERENCE) {
				action = createActionSetValue(compiler, valueSection);
			}
			else if (type == CellContentValue.CellType.QUESTION_NUM_VALUE) {
				action = createNumActionSetValue(compiler, valueSection);
			}
			else if (type == CellContentValue.CellType.SOLUTION_SCORE) {
				action = createActionHeuristicPS(compiler, valueSection);
			}
			if (action != null) result.add(action);
		}
		return result;
	}

	private PSAction createActionHeuristicPS(D3webCompiler compiler, Section<CellContentValue> solutionScoreCell) {
		String text = Strings.trim(solutionScoreCell.getText());
		Score score = D3webUtils.getScoreForString(text.toUpperCase());
		if (score == null) {
			Messages.storeMessage(compiler, solutionScoreCell, this.getClass(),
					Messages.error("No valid solution score found in '" + text + "'"));
		}
		else {
			Messages.clearMessages(compiler, solutionScoreCell, this.getClass());
		}
		if (score == null) return null;
		Solution solution = getSolution(compiler, solutionScoreCell);
		return new ActionHeuristicPS(solution, score);
	}

	private PSAction createActionSetValue(D3webCompiler compiler, Section<CellContentValue> answerReference) {
		Choice choice = (Choice) answerReference.get().getTermObject(compiler, answerReference);
		if (choice == null) return null;
		Question question = getQuestion(compiler, answerReference);
		ActionSetQuestion action = new ActionSetQuestion();
		action.setQuestion(question);
		action.setValue(new ChoiceValue(choice));
		return action;
	}

	private PSAction createNumActionSetValue(D3webCompiler compiler, Section<CellContentValue> questionNumCell) {
		Question questionNum = getQuestion(compiler, questionNumCell);
		return createNumActionSetValue(compiler, (QuestionNum) questionNum, questionNumCell);
	}

	private List<Condition> createCondition(D3webCompiler compiler, Section<CellContent> cell) {
		List<Condition> result = new ArrayList<>();
		List<Section<CellContentValue>> cells = Sections.successors(cell, CellContentValue.class);
		for (Section<CellContentValue> valueSection : cells) {
			CellContentValue.CellType type = valueSection.get().getType(compiler, valueSection);
			Condition condition = null;
			if (type == CellContentValue.CellType.ANSWER_REFERENCE) {
				condition = createCondEqual(compiler, valueSection);
			}
			else if (type == CellContentValue.CellType.QUESTION_NUM_VALUE) {
				condition = createCondNum(compiler, valueSection);
			}
			else if (type == CellContentValue.CellType.SOLUTION_STATE) {
				condition = createCondDState(compiler, valueSection);
			}
			if (condition != null) result.add(condition);
		}
		return result;
	}

	private Condition createCondEqual(D3webCompiler compiler, Section<CellContentValue> answerReferenceCell) {
		Question question = getQuestion(compiler, answerReferenceCell);
		if (answerReferenceCell.get(CellContentValue::isKnown)) {
			return new CondKnown(question);
		}
		else if (answerReferenceCell.get(CellContentValue::isUnknown)) {
			return new CondUnknown(question);
		}
		Choice choice = (Choice) answerReferenceCell.get().getTermObject(compiler, answerReferenceCell);
		if (choice == null) return null;
		CondEqual condEqual = new CondEqual(question, new ChoiceValue(choice));
		if (answerReferenceCell.get(CellContentValue::isNegated)) {
			return new CondNot(condEqual);
		}
		return condEqual;
	}

	private Condition createCondNum(D3webCompiler compiler, Section<CellContentValue> questionNumCell) {
		Question question = getQuestion(compiler, questionNumCell);
		String text = Strings.trim(questionNumCell.getText());
		Condition condition = null;

		Matcher matcher = INTERVAL_PATTERN.matcher(text);
		if (matcher.find()) {
			NumericalInterval interval = createInterval(matcher);
			if (interval != null) {
				condition = new CondNumIn((QuestionNum) question, interval);
			}
		}
		else if (questionNumCell.get(CellContentValue::isKnown)) {
			condition = new CondKnown(question);
		}
		else if (questionNumCell.get(CellContentValue::isUnknown)) {
			condition = new CondUnknown(question);
		}
		else {
			condition = createCondNum((QuestionNum) question, text);
		}

		if (condition == null) {
			Messages.storeMessage(compiler, questionNumCell, this.getClass(), Messages.error("Unable to parse '" + text + "'"));
		}
		else {
			Messages.clearMessages(compiler, questionNumCell, this.getClass());
		}
		return condition;
	}

	private Condition createCondDState(D3webCompiler compiler, Section<CellContentValue> cellContent) {
		Solution solution = getSolution(compiler, cellContent);
		String state = Strings.trim(cellContent.getText());
		Rating.State solutionState = SolutionStateType.getSolutionState(state);
		if (solutionState == null) {
			Messages.storeMessage(compiler, cellContent, this.getClass(),
					Messages.error("No valid solution state found in '" + state + "'"));
			return null;
		}
		return new CondDState(solution, solutionState);
	}

	private Solution getSolution(D3webCompiler compiler, Section<? extends Type> cellContent) {
		Section<CellContentValue> columnHeader = TableUtils.getColumnHeader(cellContent, CellContentValue.class);
		return (Solution) columnHeader.get().getTermObject(compiler, columnHeader);
	}

	private Question getQuestion(D3webCompiler compiler, Section<CellContentValue> cellContent) {
		Section<CellContentValue> columnHeader = TableUtils.getColumnHeader(cellContent, CellContentValue.class);
		return (Question) columnHeader.get().getTermObject(compiler, columnHeader);
	}

	public ActionSetQuestion createNumActionSetValue(PackageCompiler compiler, QuestionNum questionNum, Section<CellContentValue> questionNumCell) {
		NumericalInterval interval = questionNum.getInfoStore().getValue(
				BasicProperties.QUESTION_NUM_RANGE);
		String text = Strings.trim(questionNumCell.getText());
		Double parsedDouble = parseDouble("=", text);
		NumValue numValue;
		if (parsedDouble == null) {
			Messages.storeMessage(compiler, questionNumCell, this.getClass(),
					Messages.error("Unable to parse '" + text + "'"));
			return null;
		}
		if (interval != null && interval.contains(parsedDouble)) {
			Messages.storeMessage(compiler, questionNumCell, this.getClass(),
					Messages.error("Unable to parse '" + text + "'"));
			return null;
		}
		else {
			numValue = new NumValue(parsedDouble);
		}
		ActionSetQuestion action = new ActionSetQuestion();
		action.setQuestion(questionNum);
		action.setValue(numValue);
		Messages.clearMessages(compiler, questionNumCell, this.getClass());

		return action;
	}

	private Condition createCondNum(QuestionNum questionNum, String text) {
		Condition condNum = null;
		Double parsedDouble;
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
		else if (text.contains("!=")) {
			parsedDouble = parseDouble("!=", text);
			if (parsedDouble != null) {
				condNum = new CondNot(new CondNumEqual(questionNum, parsedDouble));
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
		catch (NumericalInterval.IntervalException e) {
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
