package de.d3web.we.kdom.bulletLists.scoring;

import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;

public class CreateScoresHandler implements ReviseSubTreeHandler {

	@Override
	public void reviseSubtree(Section s) {

		Section scoringSection = KnowWEObjectTypeUtils.getAncestorOfType(s,
				ScoringListType.class);
		List<String> targets = ScoringListType
				.getScoringTargets(scoringSection);

		String defaultValue = ScoringListType.getDefaultValue(scoringSection);

		for (String string : targets) {
			createScoringRule(string, defaultValue, s);
		}

	}

	private void createScoringRule(String string, String defaultValue, Section s) {
		if (string.contains("=")) {
			String solution = string.substring(0, string.indexOf("=")).trim();
			String score = string.substring(string.indexOf("=") + 1).trim();
			String question = s.getOriginalText();

			KnowledgeBaseManagement kbm = D3webModule.getInstance()
					.getKnowledgeRepresentationHandler().getKBM(s);

			boolean lazy = isLazy(s);

			Diagnosis d = kbm.findDiagnosis(solution);
			if (d == null && lazy) {
				d = createSolution(solution, kbm);
			}

			Question q = kbm.findQuestion(question);

			QuestionOC qc = (QuestionOC) q;
			if (q == null && lazy) {
				qc = createQuestion(question, defaultValue, kbm,s);

			}

			AbstractCondition cond = createCondition(qc, kbm.findAnswerChoice(
					qc, defaultValue));

			Score scoreV = getScore(score);

			if (scoreV != null && d != null && cond != null) {

				RuleComplex rule = RuleFactory.createHeuristicPSRule(s.getId(), d, scoreV, cond);
				
				KnowWEUtils.storeSectionInfo(
						s.getArticle().getWeb(), s.getTitle(), s.getId(),
						Rule.KBID_KEY, rule.getId());
				
			} else {
				// TODO ERRORHANDLING
			}
		}

	}

	private boolean isLazy(Section s) {
		Section scoringSection = KnowWEObjectTypeUtils.getAncestorOfType(s,
				ScoringListType.class);
		Map<String, String> attributes = AbstractXMLObjectType
				.getAttributeMapFor(scoringSection);

		if (attributes.containsKey("lazy")) {
			String value = attributes.get("lazy");
			if (value.equals("true") || value.equals("1") || value.equals("on")
					|| value.equals("an")) {
				return true;
			}

		}
		return false;
	}

	private Diagnosis createSolution(String solution,
			KnowledgeBaseManagement mgn) {
		Diagnosis d = mgn.findDiagnosis(solution);
		if (d == null) {
			d = mgn.createDiagnosis(solution, mgn.getKnowledgeBase()
					.getRootDiagnosis());
		}
		return d;
	}

	private Score getScore(String score) {

		// why is there no helper method in the d3web-Kernel?
		List<Score> l = Score.getAllScores();
		for (Score score2 : l) {
			if (score2.toString().equals(score)) {
				return score2;
			}
		}
		return null;
	}

	private QuestionOC createQuestion(String question, String defaultValue,
			KnowledgeBaseManagement kbm, Section s) {

		AnswerChoice a1 = new AnswerChoice(s.getId()+defaultValue);
		a1.setText(defaultValue);
		
		AnswerChoice a2 = new AnswerChoice(s.getId()+"not " + defaultValue);
		a2.setText("not " + defaultValue);
		
		

		QuestionOC q = kbm.createQuestionOC(question, kbm.getKnowledgeBase()
				.getRootQASet(), new AnswerChoice[]{a1,a2});

		return q;
	}

	private AbstractCondition createCondition(Question q, Answer a) {
		return new CondEqual(q, a);
	}

}
