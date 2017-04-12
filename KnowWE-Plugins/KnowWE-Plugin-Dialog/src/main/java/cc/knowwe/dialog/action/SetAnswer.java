package cc.knowwe.dialog.action;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import cc.knowwe.dialog.KeepAlive;
import cc.knowwe.dialog.SessionConstants;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import com.denkbares.utils.Log;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Answers some questions specified by the query parameters. It calls the
 * command "GetInterview" afterwards to deliver the current state of the
 * interview.
 * 
 * @author Volker Belli
 */
public class SetAnswer extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		for (String questionID : context.getParameters().keySet()) {
			String valueString = context.getParameter(questionID);
			// before answering we start a heart-beat on the
			// http response stream, to avoid timeouts
			KeepAlive heartbeat = new KeepAlive(context.getOutputStream());
			heartbeat.start();
			try {
				setAnswer(context, questionID, valueString);
			}
			finally {
				heartbeat.terminate();
			}
		}
	}

	public void setAnswer(UserActionContext context, String questionID, String valueString) {

		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);

		Question question = base.getManager().searchQuestion(questionID);
		if (question == null) {
			Log.warning("no question '" + questionID + "' found for answering");
			return;
		}
		Value value;
		if (valueString == null || valueString.isEmpty()
				|| valueString.equalsIgnoreCase(Unknown.getInstance().getId())) {
			// for empty value strings answer with unknown
			value = Unknown.getInstance();
		}
		else if (question instanceof QuestionOC) {
			// valueString should be the ID of the Choice which has to be set
			Choice newChoice = KnowledgeBaseUtils.findChoice(
					(QuestionChoice) question, valueString);
			if (newChoice == null) {// if choice was not found
				return;
			}
			value = new ChoiceValue(newChoice);
		}
		else if (question instanceof QuestionMC) {
			// valueString should be the ID of the Choice which has to be add to
			// the MultipleChoiceValue for this question
			Choice newChoice = KnowledgeBaseUtils.findChoice(
					(QuestionChoice) question, valueString);
			if (newChoice == null) {// if choice was not found
				return;
			}
			// this are the choices which are to be set in the new MCV
			Collection<ChoiceID> choiceIDsToBeSet = new LinkedHashSet<>();
			// if the question has currently a value and this value is a
			// MultipleChoiceValue, its choices have to be set in the new
			// MultipleChoiceValue also
			Value currentValue = session.getBlackboard().getValue(question);
			if (currentValue instanceof MultipleChoiceValue) {
				MultipleChoiceValue multi = (MultipleChoiceValue) currentValue;
				choiceIDsToBeSet.addAll(multi.getChoiceIDs());
			}
			// add the new choice (which was selected by the user)
			choiceIDsToBeSet.add(new ChoiceID(newChoice));
			value = new MultipleChoiceValue(choiceIDsToBeSet);
		}
		else if (question instanceof QuestionNum) {
			valueString = valueString.replace(',', '.');
			value = new NumValue(Double.parseDouble(valueString));
		}
		else if (question instanceof QuestionText) {
			value = new TextValue(valueString);
		}
		else {
			Log.warning("answering questions of type '" + question.getClass().getName()
					+ "' is not supported yet");
			return;
		}

		Fact fact = FactFactory.createUserEnteredFact(question, value);

		D3webUtils.setFindingSynchronized(fact, session, context);

	}
}
