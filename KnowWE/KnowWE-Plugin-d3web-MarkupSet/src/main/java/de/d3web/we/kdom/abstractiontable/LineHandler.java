package de.d3web.we.kdom.abstractiontable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethodRulebased;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNum;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.Score;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;

public class LineHandler implements D3webCompileScript<TableLine> {

	@Override
	public void compile(D3webCompiler compiler, Section<TableLine> section) throws CompilerMessage {

		if (TableUtils.isHeaderRow(section)) {
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		List<Section<CellContent>> cells = Sections.successors(section, CellContent.class);
		Iterator<Section<CellContent>> cellIter = cells.iterator();

		List<Condition> conditions = new ArrayList<Condition>(cells.size());
		PSAction action = null;

		for (; cellIter.hasNext();) {
			Section<CellContent> cell = cellIter.next();
			Section<?> knowledgeSection = cell.getChildren().get(0);
			if (cellIter.hasNext()) {
				// it's a condition cell
				Condition condition = createCondition(compiler, knowledgeSection);
				if (condition != null) conditions.add(condition);
			}
			else {
				// it's the action cell then
				action = createAction(compiler, knowledgeSection);
			}
		}

		if (action == null || conditions.isEmpty()) {
			int row = TableUtils.getRow(section);
			String message = "Rule for row " + row + " was not created (no "
					+ (action == null ? "action" : "conditions")
					+ " found)";
			throw CompilerMessage.error(message);
		}

		/* Rule rule = */createRule(conditions, action);
		throw CompilerMessage.info();
	}

	private Rule createRule(List<Condition> conditions, PSAction action) {
		Condition condition = combineConditions(conditions);
		Class<? extends PSMethodRulebased> psMethodContext = null;
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

	private PSAction createAction(D3webCompiler compiler, Section<? extends Type> knowledgeSection) {
		Type type = knowledgeSection.get();
		if (type instanceof AnswerReferenceCell) {
			Section<AnswerReferenceCell> answerReference =
					Sections.cast(knowledgeSection, AnswerReferenceCell.class);
			return createActionSetValue(compiler, answerReference);
		}
		else if (type instanceof QuestionNumCell) {
			Section<QuestionNumCell> questionNumCell =
					Sections.cast(knowledgeSection, QuestionNumCell.class);
			return createNumActionSetValue(compiler, questionNumCell);
		}
		else if (type instanceof SolutionScoreCell) {
			Section<SolutionScoreCell> solutionScoreCell =
					Sections.cast(knowledgeSection, SolutionScoreCell.class);
			return createActionHeuristicPS(compiler, solutionScoreCell);
		}
		return null;
	}

	private PSAction createActionHeuristicPS(D3webCompiler compiler, Section<SolutionScoreCell> solutionScoreCell) {
		Score score = solutionScoreCell.get().createScore(compiler, solutionScoreCell);
		if (score == null) return null;
		Solution solution = getSolution(compiler, solutionScoreCell);
		ActionHeuristicPS action = new ActionHeuristicPS();
		action.setSolution(solution);
		action.setScore(score);
		return action;
	}

	private PSAction createActionSetValue(D3webCompiler compiler, Section<AnswerReferenceCell> answerReference) {
		Choice choice = answerReference.get().getTermObject(compiler, answerReference);
		if (choice == null) return null;
		Question question = getQuestion(compiler, answerReference);
		ActionSetQuestion action = new ActionSetQuestion();
		action.setQuestion(question);
		action.setValue(new ChoiceValue(choice));
		return action;
	}

	private PSAction createNumActionSetValue(D3webCompiler compiler, Section<QuestionNumCell> questionNumCell) {
		Question questionNum = getQuestion(compiler, questionNumCell);
		return questionNumCell.get().createNumActionSetValue(compiler, (QuestionNum) questionNum,
				questionNumCell);
	}

	private Condition createCondition(D3webCompiler compiler, Section<? extends Type> knowledgeSection) {
		Type type = knowledgeSection.get();
		if (type instanceof AnswerReferenceCell) {
			Section<AnswerReferenceCell> answerReference =
					Sections.cast(knowledgeSection, AnswerReferenceCell.class);
			return createCondEqual(compiler, answerReference);
		}
		else if (type instanceof QuestionNumCell) {
			Section<QuestionNumCell> questionNumCell =
					Sections.cast(knowledgeSection, QuestionNumCell.class);
			return createCondNum(compiler, questionNumCell);
		}
		else if (type instanceof SolutionStateCell) {
			Section<SolutionStateCell> solutionStateCell =
					Sections.cast(knowledgeSection, SolutionStateCell.class);
			return createCondDState(compiler, solutionStateCell);
		}
		return null;
	}

	private CondEqual createCondEqual(D3webCompiler compiler, Section<AnswerReferenceCell> answerReference) {
		Question question = getQuestion(compiler, answerReference);
		Choice choice = answerReference.get().getTermObject(compiler, answerReference);
		if (choice == null) return null;
		CondEqual cond = new CondEqual(question, new ChoiceValue(choice));
		return cond;
	}

	private Condition createCondNum(D3webCompiler compiler, Section<QuestionNumCell> questionNumCell) {
		Question question = getQuestion(compiler, questionNumCell);
		CondNum condNum = questionNumCell.get().createCondNum(compiler, (QuestionNum) question,
				questionNumCell);
		return condNum;
	}

	private Condition createCondDState(D3webCompiler compiler, Section<SolutionStateCell> solutionStateCell) {
		Solution solution = getSolution(compiler, solutionStateCell);
		CondDState condDState = solutionStateCell.get().createCondDState(compiler, solution,
				solutionStateCell);
		return condDState;
	}

	private Solution getSolution(D3webCompiler compiler, Section<? extends Type> knowledgeSection) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(knowledgeSection);
		Section<SolutionReference> solutionReference = Sections.successor(columnHeader,
				SolutionReference.class);
		if (solutionReference == null) return null;
		Solution solution = solutionReference.get().getTermObject(compiler, solutionReference);
		return solution;
	}

	private Question getQuestion(D3webCompiler compiler, Section<? extends Type> knowledgeSection) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(knowledgeSection);
		Section<QuestionReference> questionReference = Sections.successor(columnHeader,
				QuestionReference.class);
		if (questionReference == null) return null;
		Question question = questionReference.get().getTermObject(compiler, questionReference);
		return question;
	}

}
