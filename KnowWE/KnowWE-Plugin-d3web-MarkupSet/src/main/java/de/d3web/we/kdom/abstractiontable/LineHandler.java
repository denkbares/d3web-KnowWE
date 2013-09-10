package de.d3web.we.kdom.abstractiontable;

import java.util.ArrayList;
import java.util.Collection;
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
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;

public class LineHandler extends D3webSubtreeHandler<TableLine> {

	@Override
	public Collection<Message> create(Article article, Section<TableLine> section) {

		if (TableUtils.isHeaderRow(section)) return Messages.noMessage();

		List<Section<CellContent>> cells = Sections.findSuccessorsOfType(section, CellContent.class);
		Iterator<Section<CellContent>> cellIter = cells.iterator();

		List<Condition> conditions = new ArrayList<Condition>(cells.size());
		PSAction action = null;

		for (; cellIter.hasNext();) {
			Section<CellContent> cell = cellIter.next();
			Section<? extends Type> knowledgeSection = cell.getChildren().get(0);
			if (cellIter.hasNext()) {
				// it's a condition cell
				Condition condition = createCondition(article, knowledgeSection);
				if (condition != null) conditions.add(condition);
			}
			else {
				// it's the action cell then
				action = createAction(article, knowledgeSection);
			}
		}

		if (action == null || conditions.isEmpty()) {
			int row = TableUtils.getRow(section);
			String message = "Rule for row " + row + " was not created (no "
					+ (action == null ? "action" : "conditions")
					+ " found)";
			return Messages.asList(Messages.error(message));
		}

		Rule rule = createRule(conditions, action);

		return Messages.asList(Messages.notice("Created Rule " + rule.toString()));
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

	private PSAction createAction(Article article, Section<? extends Type> knowledgeSection) {
		Type type = knowledgeSection.get();
		if (type instanceof AnswerReferenceCell) {
			Section<AnswerReferenceCell> answerReference =
					Sections.cast(knowledgeSection, AnswerReferenceCell.class);
			return createActionSetValue(article, answerReference);
		}
		else if (type instanceof QuestionNumCell) {
			Section<QuestionNumCell> questionNumCell =
					Sections.cast(knowledgeSection, QuestionNumCell.class);
			return createNumActionSetValue(article, questionNumCell);
		}
		else if (type instanceof SolutionScoreCell) {
			Section<SolutionScoreCell> solutionScoreCell =
					Sections.cast(knowledgeSection, SolutionScoreCell.class);
			return createActionHeuristicPS(article, solutionScoreCell);
		}
		return null;
	}

	private PSAction createActionHeuristicPS(Article article, Section<SolutionScoreCell> solutionScoreCell) {
		Score score = solutionScoreCell.get().createScore(article, solutionScoreCell);
		if (score == null) return null;
		Solution solution = getSolution(article, solutionScoreCell);
		ActionHeuristicPS action = new ActionHeuristicPS();
		action.setSolution(solution);
		action.setScore(score);
		return action;
	}

	private PSAction createActionSetValue(Article article, Section<AnswerReferenceCell> answerReference) {
		Choice choice = answerReference.get().getTermObject(article, answerReference);
		if (choice == null) return null;
		Question question = getQuestion(article, answerReference);
		ActionSetQuestion action = new ActionSetQuestion();
		action.setQuestion(question);
		action.setValue(new ChoiceValue(choice));
		return action;
	}

	private PSAction createNumActionSetValue(Article article, Section<QuestionNumCell> questionNumCell) {
		Question questionNum = getQuestion(article, questionNumCell);
		return questionNumCell.get().createNumActionSetValue(article, (QuestionNum) questionNum,
				questionNumCell);
	}

	private Condition createCondition(Article article, Section<? extends Type> knowledgeSection) {
		Type type = knowledgeSection.get();
		if (type instanceof AnswerReferenceCell) {
			Section<AnswerReferenceCell> answerReference =
					Sections.cast(knowledgeSection, AnswerReferenceCell.class);
			return createCondEqual(article, answerReference);
		}
		else if (type instanceof QuestionNumCell) {
			Section<QuestionNumCell> questionNumCell =
					Sections.cast(knowledgeSection, QuestionNumCell.class);
			return createCondNum(article, questionNumCell);
		}
		else if (type instanceof SolutionStateCell) {
			Section<SolutionStateCell> solutionStateCell =
					Sections.cast(knowledgeSection, SolutionStateCell.class);
			return createCondDState(article, solutionStateCell);
		}
		return null;
	}

	private CondEqual createCondEqual(Article article, Section<AnswerReferenceCell> answerReference) {
		Question question = getQuestion(article, answerReference);
		Choice choice = answerReference.get().getTermObject(article, answerReference);
		if (choice == null) return null;
		CondEqual cond = new CondEqual(question, new ChoiceValue(choice));
		return cond;
	}

	private Condition createCondNum(Article article, Section<QuestionNumCell> questionNumCell) {
		Question question = getQuestion(article, questionNumCell);
		CondNum condNum = questionNumCell.get().createCondNum(article, (QuestionNum) question,
				questionNumCell);
		return condNum;
	}

	private Condition createCondDState(Article article, Section<SolutionStateCell> solutionStateCell) {
		Solution solution = getSolution(article, solutionStateCell);
		CondDState condDState = solutionStateCell.get().createCondDState(article, solution,
				solutionStateCell);
		return condDState;
	}

	private Solution getSolution(Article article, Section<? extends Type> knowledgeSection) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(knowledgeSection);
		Section<SolutionReference> solutionReference = Sections.findSuccessor(columnHeader,
				SolutionReference.class);
		Solution solution = solutionReference.get().getTermObject(article, solutionReference);
		return solution;
	}

	private Question getQuestion(Article article, Section<? extends Type> knowledgeSection) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(knowledgeSection);
		Section<QuestionReference> questionReference = Sections.findSuccessor(columnHeader,
				QuestionReference.class);
		Question question = questionReference.get().getTermObject(article, questionReference);
		return question;
	}
}
