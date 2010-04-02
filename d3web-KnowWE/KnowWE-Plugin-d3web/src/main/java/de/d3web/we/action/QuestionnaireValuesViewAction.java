package de.d3web.we.action;

import java.io.IOException;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Value;
import de.d3web.core.session.XPSCase;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.utils.D3webUtils;

public class QuestionnaireValuesViewAction extends AbstractAction {
	
	@Override
	public void execute(ActionContext context) throws IOException {
		
		String questionnaireName = context.getParameter("questionnaire");
		String web = context.getParameter(KnowWEAttributes.WEB);
		StringBuilder result = new StringBuilder();
		
		for (KnowWEArticle article : KnowWEEnvironment.getInstance().getArticleManager(web).getArticles()) {
			XPSCase theCase = D3webUtils.getXPSCase(article.getTitle(), context.getWikiContext(), web);
			if (theCase != null) {
				IDObject io = theCase.getKnowledgeBase().searchObjectForName(questionnaireName);
				if (io instanceof QContainer) {
					QContainer questionnaire = (QContainer) io;
					for (TerminologyObject no : questionnaire.getChildren()) {
						if (no instanceof Question) {
							renderQuestion((Question) no, theCase, result);
						}
					}
					context.getWriter().write(result.toString());
					return;
				}
			}
		}
		
		context.getWriter().write("Unknown Questionnaire: " + questionnaireName);

	}

	private void renderQuestion(Question question, XPSCase theCase,
			StringBuilder result) {
		
		Value v = null;
		
		if (theCase.getAnsweredQuestions().contains(question))
			v = theCase.getBlackboard().getValue(question);

		result.append("<p>");
		result.append(question.getName());
		result.append(" = {");
		if (v instanceof ChoiceValue)
			result.append(v);
		if (v instanceof MultipleChoiceValue) {
			List<ChoiceValue> cvs = (List<ChoiceValue>) v.getValue();
			for (ChoiceValue cv : cvs) {
				result.append(cv);
				result.append(", ");
			}
			result.delete(result.length() - 2, result.length());
		}
		if (v instanceof NumValue) {
			result.append((Double) v.getValue());
		}
		result.append("}</p>");
	}

}
