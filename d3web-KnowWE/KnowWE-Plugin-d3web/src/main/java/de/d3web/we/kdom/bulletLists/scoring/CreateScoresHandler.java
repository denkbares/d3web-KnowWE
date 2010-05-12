package de.d3web.we.kdom.bulletLists.scoring;

import java.util.List;
import java.util.Map;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.scoring.Score;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;

public class CreateScoresHandler implements SubtreeHandler {

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

		Section scoringSection = KnowWEObjectTypeUtils.getAncestorOfType(s,
				BulletScoring.class);
		List<String> targets = BulletScoring
				.getScoringTargets(scoringSection);

		if (targets == null) return null;

		String defaultValue = BulletScoring.getDefaultValue(scoringSection);

		for (String string : targets) {
			createScoringRule(article, string, defaultValue, s);
		}
		return null;

	}

	private void createScoringRule(KnowWEArticle article, String string, String defaultValue, Section s) {
		if (string.contains("=")) {
			String solution = string.substring(0, string.indexOf("=")).trim();
			String score = string.substring(string.indexOf("=") + 1).trim();
			String question = s.getOriginalText();

			KnowledgeBaseManagement kbm = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, this, s);

			if (kbm == null) return; // dirty hack for testing

			boolean lazy = isLazy(s);

			Solution d = kbm.findSolution(solution);
			if (d == null && lazy) {
				d = createSolution(solution, kbm);
			}

			Question q = kbm.findQuestion(question);

			QuestionOC qc = (QuestionOC) q;
			if (q == null && lazy) {
				qc = createQuestion(question, defaultValue, kbm, s);

			}

			Condition cond = createCondition(qc, new ChoiceValue(kbm.findChoice(
					qc, defaultValue)));

			Score scoreV = getScore(score);

			if (scoreV != null && d != null && cond != null) {

				Rule rule = RuleFactory.createHeuristicPSRule(s.getId(), d, scoreV, cond);
				KnowWEUtils.storeSectionInfo(
						s.getArticle().getWeb(), article.getTitle(), s.getId(),
						de.d3web.we.kdom.rules.Rule.KBID_KEY, rule.getId());

			}
			else {
				// TODO ERRORHANDLING
			}
		}

	}

	private boolean isLazy(Section s) {
		Section scoringSection = KnowWEObjectTypeUtils.getAncestorOfType(s,
				BulletScoring.class);
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

	private Solution createSolution(String solution,
			KnowledgeBaseManagement mgn) {
		Solution d = mgn.findSolution(solution);
		if (d == null) {
			d = mgn.createSolution(solution, mgn.getKnowledgeBase()
					.getRootSolution());
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

		Choice a1 = new Choice(s.getId() + defaultValue);
		a1.setText(defaultValue);

		Choice a2 = new Choice(s.getId() + "not " + defaultValue);
		a2.setText("not " + defaultValue);

		QuestionOC q = kbm.createQuestionOC(question, kbm.getKnowledgeBase()
				.getRootQASet(), new Choice[] {
				a1, a2 });

		return q;
	}

	private Condition createCondition(Question q, Value a) {
		return new CondEqual(q, a);
	}

}
