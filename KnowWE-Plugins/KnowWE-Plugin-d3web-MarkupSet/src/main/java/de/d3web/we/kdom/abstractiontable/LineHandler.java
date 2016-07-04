package de.d3web.we.kdom.abstractiontable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethodRulebased;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.Score;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.SolutionStateType;
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

	private static Pattern INTERVAL_PATTERN = Pattern.compile("(\\[|\\]|\\() *(\\d+\\.?\\d*) +(\\d+\\.?\\d*) *(\\[|\\]|\\))");

	@Override
	public void compile(D3webCompiler compiler, Section<TableLine> section) throws CompilerMessage {

		if (TableUtils.isHeaderRow(section)) {
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		List<Section<CellContent>> cells = Sections.successors(section, CellContent.class);
		Iterator<Section<CellContent>> cellIter = cells.iterator();

		List<Condition> conditions = new ArrayList<>(cells.size());
		PSAction action = null;

		for (; cellIter.hasNext(); ) {
			Section<CellContent> cell = cellIter.next();
			if (cellIter.hasNext()) {
				// it's a condition cell
				Condition condition = createCondition(compiler, cell);
				if (condition != null) conditions.add(condition);
			}
			else {
				// it's the action cell then
				action = createAction(compiler, cell);
			}
		}

		if (action == null || conditions.isEmpty()) {
			int row = TableUtils.getRow(section);
			String message = "Rule for row " + row + " was not created (no "
					+ (action == null ? "action" : "conditions")
					+ " found)";
			throw CompilerMessage.error(message);
		}

		/* Rule rule = */
		createRule(conditions, action);
		throw CompilerMessage.info();
	}

	private Rule createRule(List<Condition> conditions, PSAction action) {
		Condition condition = combineConditions(conditions);
		Class<? extends PSMethodRulebased> psMethodContext;
		if (action instanceof ActionSetQuestion) {
			psMethodContext = PSMethodAbstraction.class;
		}
		else {
			psMethodContext = PSMethodHeuristic.class;
		}
		return RuleFactory.createRule(action, condition, null, psMethodContext);
	}

	private Condition combineConditions(List<Condition> conditions) {
		if (conditions.size() == 1) return conditions.get(0);
		return new CondAnd(conditions);
	}

	private PSAction createAction(D3webCompiler compiler, Section<CellContent> contentCell) {
		CellContent.CellType type = contentCell.get().getType(compiler, contentCell);
		if (type == CellContent.CellType.ANSWER_REFERENCE) {
			return createActionSetValue(compiler, contentCell);
		}
		else if (type == CellContent.CellType.QUESTION_NUM_VALUE) {
			return createNumActionSetValue(compiler, contentCell);
		}
		else if (type == CellContent.CellType.SOLUTION_SCORE) {
			return createActionHeuristicPS(compiler, contentCell);
		}
		else {
			return null;
		}
	}

	private PSAction createActionHeuristicPS(D3webCompiler compiler, Section<CellContent> solutionScoreCell) {
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
		ActionHeuristicPS action = new ActionHeuristicPS();
		action.setSolution(solution);
		action.setScore(score);
		return action;
	}

	private PSAction createActionSetValue(D3webCompiler compiler, Section<CellContent> answerReference) {
		Choice choice = (Choice) answerReference.get().getTermObject(compiler, answerReference);
		if (choice == null) return null;
		Question question = getQuestion(compiler, answerReference);
		ActionSetQuestion action = new ActionSetQuestion();
		action.setQuestion(question);
		action.setValue(new ChoiceValue(choice));
		return action;
	}

	private PSAction createNumActionSetValue(D3webCompiler compiler, Section<CellContent> questionNumCell) {
		Question questionNum = getQuestion(compiler, questionNumCell);
		return createNumActionSetValue(compiler, (QuestionNum) questionNum, questionNumCell);
	}

	private Condition createCondition(D3webCompiler compiler, Section<CellContent> cell) {
		CellContent.CellType type = cell.get().getType(compiler, cell);
		if (type == CellContent.CellType.ANSWER_REFERENCE) {
			return createCondEqual(compiler, cell);
		}
		else if (type == CellContent.CellType.QUESTION_NUM_VALUE) {
			return createCondNum(compiler, cell);
		}
		else if (type == CellContent.CellType.SOLUTION_STATE) {
			return createCondDState(compiler, cell);
		}
		else {
			return null;
		}
	}

	private Condition createCondEqual(D3webCompiler compiler, Section<CellContent> answerReferenceCell) {
		Question question = getQuestion(compiler, answerReferenceCell);
		Choice choice = (Choice) answerReferenceCell.get().getTermObject(compiler, answerReferenceCell);
		if (choice == null) return null;
		return new CondEqual(question, new ChoiceValue(choice));
	}

	private Condition createCondNum(D3webCompiler compiler, Section<CellContent> questionNumCell) {
		Question question = getQuestion(compiler, questionNumCell);
		String text = Strings.trim(questionNumCell.getText());
		Condition condNum = null;

		Matcher matcher = INTERVAL_PATTERN.matcher(text);
		if (matcher.find()) {
			NumericalInterval interval = createInterval(matcher);
			if (interval != null) {
				condNum = new CondNumIn((QuestionNum) question, interval);
			}
		}
		else {
			condNum = createCondNum((QuestionNum) question, text);
		}

		if (condNum == null) {
			Messages.storeMessage(compiler, questionNumCell, this.getClass(), Messages.error("Unable to parse '" + text + "'"));
		}
		else {
			Messages.clearMessages(compiler, questionNumCell, this.getClass());
		}
		return condNum;
	}

	private Condition createCondDState(D3webCompiler compiler, Section<CellContent> cellContent) {
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
		Section<CellContent> columnHeader = TableUtils.getColumnHeader(cellContent, CellContent.class);
		return (Solution) columnHeader.get().getTermObject(compiler, columnHeader);
	}

	private Question getQuestion(D3webCompiler compiler, Section<CellContent> cellContent) {
		Section<CellContent> columnHeader = TableUtils.getColumnHeader(cellContent, CellContent.class);
		return (Question) columnHeader.get().getTermObject(compiler, columnHeader);
	}

	public ActionSetQuestion createNumActionSetValue(PackageCompiler compiler, QuestionNum questionNum, Section<CellContent> questionNumCell) {
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
				condNum = new CondNot(new CondNumGreater(questionNum, parsedDouble));
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
