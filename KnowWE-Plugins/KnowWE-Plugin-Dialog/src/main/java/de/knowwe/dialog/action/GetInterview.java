/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;

import com.denkbares.collections.MultiMap;
import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.SolutionDisplay;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.protocol.FactProtocolEntry;
import de.d3web.costbenefit.inference.PSMethodCostBenefit;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.interview.Form;
import de.d3web.interview.Interview;
import de.d3web.interview.inference.PSMethodInterview;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.OutDatedSessionNotification;

public class GetInterview extends AbstractAction {

	public static final String PARAM_LANGUAGE = "lang";
	public static final String PARAM_INCLUDE_HISTORY = "history";
	public static final String PARAM_REQUIRE_SOLUTIONS = "requireSolutions";

	@Override
	public void execute(UserActionContext context) throws IOException {
		Locale locale = Utils.parseLocale(context.getParameter(PARAM_LANGUAGE));
		boolean includeHistory = Boolean.parseBoolean(context.getParameter(PARAM_INCLUDE_HISTORY));
		boolean requireSolutions = Boolean.parseBoolean(context.getParameter(PARAM_REQUIRE_SOLUTIONS));

		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);

		StartCase.KnowledgeBaseProvider[] providers = (StartCase.KnowledgeBaseProvider[]) context.getSession()
				.getAttribute(SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);
		// create {@link OutDatedSessionNotification} if necessary
		if (providers.length == 1 && providers[0] instanceof InitWiki.WikiProvider) {
			InitWiki.WikiProvider wikiProvider = (InitWiki.WikiProvider) providers[0];
			if (SessionProvider.hasOutDatedSession(context, wikiProvider.getKnowledgeBase())) {
				NotificationManager.addNotification(context, new OutDatedSessionNotification(
						wikiProvider.getSectionId()));
			}
		}

		Writer writer = context.getWriter();
		Session session = SessionProvider.getSession(context, base);
		assert session != null;

		writer.append("<interview>\n\n");
		PSMethodInterview psm = session.getPSMethodInstance(PSMethodInterview.class);
		Interview interview = session.getSessionObject(psm);
		// append previous questions
		if (includeHistory) {
			writer.append("<history>\n");
			List<FactProtocolEntry> entries = getUserSelectedHistory(session);
			for (FactProtocolEntry entry : entries) {
				String questionName = entry.getTerminologyObjectName();
				TerminologyObject question = base.getManager().search(questionName);
				if (question instanceof Question &&
						!interview.isActive((Question) question)) {
					listQuestions(context, (Question) question, session, locale, writer);
				}
			}
			writer.append("</history>\n\n");
		}

		// append current questions
		writer.append("<questionnaire>\n");
		Form nextForm = interview.nextForm();
		boolean hasQuestion = (nextForm != null) && nextForm.getActiveQuestions().size() >= 1;
		if (hasQuestion) {
			listQuestions(context, nextForm.getActiveQuestions().get(0), session, locale, writer);
		}
		writer.append("</questionnaire>\n\n");

		PSMethodCostBenefit psMethodInstance = session.getPSMethodInstance(PSMethodCostBenefit.class);
		if (psMethodInstance != null) {
			writer.append("<showAlternatives />\n");
		}

		// append current solutions
		// if they are required or if no next question is available
		writer.append("<solutions>\n");
		if (requireSolutions || !hasQuestion) {
			listSolutions(context, session, locale, writer);
		}
		writer.append("</solutions>\n\n");

		writer.append("</interview>");
		context.setContentType("text/xml");
	}

	private List<FactProtocolEntry> getUserSelectedHistory(Session session) {
		Interview interview = session.getSessionObject(session.getPSMethodInstance(PSMethodInterview.class));
		List<FactProtocolEntry> entries =
				session.getProtocol().getProtocolHistory(FactProtocolEntry.class);

		// prepare a list of all next question names
		// to exclude them from the list (because they will be asked in future)
		Set<String> nextNames = new HashSet<>();
		Form nextForm = interview.nextForm();
		if (nextForm != null) {
			for (Question question : nextForm.getActiveQuestions()) {
				nextNames.add(question.getName());
			}
		}
		List<FactProtocolEntry> result = new LinkedList<>();
		for (FactProtocolEntry entry : entries) {
			// if the name will be asked do not show in history
			String name = entry.getTerminologyObjectName();
			if (nextNames.contains(name)) {
				continue;
			}

			// only use question with user selected fact
			String psmName = entry.getSolvingMethodClassName();
			if (psmName == null) continue;
			if (!psmName.equals(PSMethodUserSelected.class.getName())) continue;

			// check if there is already an entry for that terminology object
			boolean isAlreadyIn = false;
			for (FactProtocolEntry exiting : result) {
				if (exiting.getTerminologyObjectName().equals(name)) {
					isAlreadyIn = true;
					break;
				}
			}
			// check if
			// and add only if there is no such entry
			if (!isAlreadyIn) {
				result.add(entry);
			}
		}
		return result;
	}

	private List<Question> toQuestions(InterviewObject interviewObject) {
		List<Question> result = new ArrayList<>();
		if (interviewObject instanceof Question) {
			result.add((Question) interviewObject);
		}
		else {
			// out.append(set.toString());
			// TODO: handle well, currently only works for OQDialogController
			throw new IllegalStateException(
					"currently only works for OneQQuestion-Interviews");
		}
		return result;
	}

	public void listQuestions(UserActionContext context, InterviewObject interviewObject, Session session, Locale locale, Writer out) throws IOException {
		for (Question question : toQuestions(interviewObject)) {
			getInfoCommand(context).appendInfoObject(question, session, locale, out);
		}
	}

	public void listSolutions(UserActionContext context, Session session, Locale locale, Writer out) throws IOException {
		// Group solutions to be displayed
		MultiMap<Solution, Solution> groups =
				KnowledgeBaseUtils.getGroupedSolutions(session, State.ESTABLISHED, State.SUGGESTED);

		// show groups only, sorted by their best solution,
		// but enhance description by their sub-solutions
		for (Solution solution : groups.keySet()) {
			// build a enumeration list of all solutions that are not contexts
			// (and not the group itself) as additional solution description
			StringBuilder items = new StringBuilder();
			for (Solution child : groups.getValues(solution)) {
				if (child == solution) continue;
				if (BasicProperties.getSolutionDisplay(child) == SolutionDisplay.context) continue;
				items.append("<li>").append(MMInfo.getPrompt(child, locale)).append("</li>");
			}
			String summary = null;
			if (items.length() > 0) {
				String intro = (locale.getLanguage().equals("de"))
						? "M&ouml;gliche Defekte an dieser Komponente"
						: "Possible issues at this component";
				summary = intro + ": <ul>" + items + "</ul>";
			}

			getInfoCommand(context).appendInfoObject(solution, session, locale, summary, out);
		}
	}

	private GetInfoObject getInfoCommand(UserActionContext context) throws IOException {
		return (GetInfoObject) Utils.getAction(GetInfoObject.class.getSimpleName());
	}

}
