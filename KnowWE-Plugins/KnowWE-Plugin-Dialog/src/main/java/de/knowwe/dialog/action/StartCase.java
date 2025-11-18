/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PSMethodInit;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.interview.FormStrategy;
import de.d3web.interview.Interview;
import de.d3web.interview.NextUnansweredQuestionFormStrategy;
import de.d3web.interview.SingleQuestionFormStrategyWrapper;
import de.d3web.we.basic.SessionCreatedEvent;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;

/**
 * Command to start a new case of a specified knowledge base.
 *
 * @author Volker Belli
 */
public class StartCase extends AbstractAction implements EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(StartCase.class);

	public static final String PARAM_USER = "user";
	public static final String PARAM_LANGUAGE = "lang";
	public static final String PARAM_PROTOCOL_PATH = "protocol";

	public static final String HTTP_SESSION_START_INFO = "com.denkbares.dialog.startInfo";
	public static final String HTTP_SESSION_RECENT_START_INFO = "com.denkbares.dialog.recentInfo";

	public StartCase() {
		synchronized (EventManager.getInstance()) {
			EventManager.getInstance().registerListener(this, EventManager.RegistrationType.WEAK);
		}
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Collections.singletonList(SessionCreatedEvent.class);
	}

	@Override
	public void notify(Event genericEvent) {
		SessionCreatedEvent event = (SessionCreatedEvent) genericEvent;
		event.getContext().getSession().setAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE,
				event.getSession().getKnowledgeBase());
	}

	/**
	 * Defines a provider that is capable to deliver the knowledge base to be used for a new session.
	 *
	 * @author volker_belli
	 * @created 22.09.2010
	 */
	public interface KnowledgeBaseProvider {

		/**
		 * Returns the display name of the knowledge base.
		 * @param context
		 */
		String getName(UserActionContext context) throws IOException;

		/**
		 * Returns the description of the knowledge base.
		 * @param context
		 */
		String getDescription(UserActionContext context) throws IOException;

		/**
		 * Returns the icon of the knowledge base.
		 * @param context
		 */
		Resource getFavIcon(UserActionContext context) throws IOException;

		/**
		 * Returns the knowledge base. If required the base is loaded on demand here.
		 * @param context
		 */
		KnowledgeBase getKnowledgeBase(UserActionContext context) throws IOException;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String language = context.getParameter(PARAM_LANGUAGE);
		List<Locale> preferredLocales = new ArrayList<>();
		if (language != null) {
			preferredLocales.add(new Locale(language));
		}
		if (preferredLocales.isEmpty()) {
			Collections.addAll(preferredLocales, KnowWEUtils.getBrowserLocales(context));
		}
		String protocolPath = context.getParameter(PARAM_PROTOCOL_PATH);

		HttpSession httpSession = context.getSession();
		StartInfo startInfo = (StartInfo) httpSession.getAttribute(HTTP_SESSION_START_INFO);
		if (startInfo == null) startInfo = parseURLParamQuestions(context);

		// move the attribute
		httpSession.removeAttribute(HTTP_SESSION_START_INFO);
		httpSession.setAttribute(HTTP_SESSION_RECENT_START_INFO, startInfo);
		KnowledgeBaseProvider provider = (KnowledgeBaseProvider) httpSession.getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_PROVIDER);

		Session session;
		try {
			KnowledgeBase base = provider.getKnowledgeBase(context);
			httpSession.setAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE, base);
			if (Strings.isBlank(protocolPath)) {
				session = startCase(context, base, startInfo);
			}
			else {
				session = loadCase(context, base, protocolPath);
			}

			// adapt language to one of the supported ones
			// of the loaded knowledge base
			Locale locale = Locales.findBestLocale(preferredLocales, KnowledgeBaseUtils.getAvailableLocales(base));

			if (!locale.getLanguage().isEmpty()) {
				language = locale.getLanguage();
			}
			else {
				// the locale is not valid -> no lang
				language = "NO_LANG";
				LOGGER.warn(locale + " is not a valid language. Using: " + language);
			}
		}
		catch (IOException e) {
			Utils.redirectToErrorPage(context, e);
			return;
		}

		// and redirect to interview
		context.sendRedirect("Resource/ui.zip/html/index.html?" + PARAM_LANGUAGE + "=" + language + "&fromFile=" + session
				.getSessionObject(LoadedSessionContext.getInstance())
				.isLoadedFromFile());
	}

	@NotNull
	private StartInfo parseURLParamQuestions(UserActionContext context) {
		Map<String, String> answers = new LinkedHashMap<>();
		int index = 1;
		while (true) {
			String objectName = context.getParameter("o" + index);
			String valueString = context.getParameter("v" + index);
			if (objectName == null || valueString == null) {
				break;
			}
			answers.put(Strings.decodeURL(objectName), Strings.decodeURL(valueString));
			index++;
		}
		return new StartInfo(answers);
	}

	/**
	 * Opens a xml or zip file (and take the first xml file from there) read the records from.
	 *
	 * @created 31.08.2011
	 */
	private Session loadCase(UserActionContext context, KnowledgeBase knowledgebase, String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.isFile()) throw new IOException("no such file: " + file.getCanonicalPath());

		Collection<SessionRecord> records;
		if (StringUtils.endsWithIgnoreCase(file.getName(), ".zip")) {
			// try to read the zip file containing the protocol
			try (ZipFile zipFile = new ZipFile(file)) {
				// look for desired entry
				ZipEntry matchedEntry = null;
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (entry.isDirectory()) continue;
					// if we find the protocol use it

					if (Strings.endsWithIgnoreCase("/" + entry.getName(), "/"
							+ DownloadProtocol.FILENAME_SESSION_PROTOCOL_XML)) {
						matchedEntry = entry;
						break;
					}
					// otherwise take the first xml file
					if (matchedEntry == null
							&& Strings.endsWithIgnoreCase(entry.getName(), ".xml")) {
						matchedEntry = entry;
					}
				}
				if (matchedEntry == null) {
					throw new IOException(
							"zip file does not contain a valid protocol: "
									+ file.getCanonicalPath());
				}
				// we do not need separate try - finally to close entry stream
				// because it will be closed together with the zip file anyway
				InputStream inputStream = zipFile.getInputStream(matchedEntry);
				records = SessionPersistenceManager.getInstance().loadSessions(inputStream);
				inputStream.close();
			}
		}
		else {
			// or try to read the protocol itself
			records = SessionPersistenceManager.getInstance().loadSessions(file);
		}

		Session session = SessionConversionFactory.replayToSession(
				knowledgebase, records.iterator().next());

		session.getSessionObject(LoadedSessionContext.getInstance()).setFile(file);

		wrapFormStrategy(session);
		// set the session in the provider
		SessionProvider.setSession(context, session);
		return session;
	}

	private Session startCase(UserActionContext context, KnowledgeBase knowledgebase, StartInfo restart) {
		Session session = SessionProvider.getSession(context, knowledgebase);
		if (session == null || (restart != null && restart.forceRestart)) {
			session = SessionProvider.createSession(context, knowledgebase);
			assert session != null;
			if (restart != null) {
				restart.setDefaultAnswers(session, PSMethodInit.getInstance());
			}
		}
		wrapFormStrategy(session);
		return session;
	}

	private void wrapFormStrategy(Session session) {
		Interview interview = Interview.get(session);
		FormStrategy originalFormStrategy = interview.getFormStrategy();
		if (!(originalFormStrategy instanceof NextUnansweredQuestionFormStrategy)) {
			interview.setFormStrategy(new SingleQuestionFormStrategyWrapper(originalFormStrategy));
		}
	}

	/**
	 * Initialize a new knowledge base provider and starts a case with that provider. The provider is use in future for
	 * starting cases at this http session.
	 *
	 * @param context  the ActionContext to be used
	 * @param provider the KnwoeldgeBaseProvider to be responsible to create the knowledge bases
	 * @throws IOException if something fails during starting the case
	 * @created 22.09.2010
	 */
	public void startCase(UserActionContext context, KnowledgeBaseProvider provider, StartInfo startInfo) throws IOException {
		context.getSession().setAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_PROVIDER, provider);
		context.getSession().setAttribute(HTTP_SESSION_START_INFO, startInfo);
		execute(context);
	}

	public static class StartInfo {
		private static final Logger LOGGER = LoggerFactory.getLogger(StartInfo.class);
		private final boolean forceRestart;
		private final Map<String, String> answers;

		public StartInfo(boolean forceRestart) {
			this(forceRestart, Collections.emptyMap());
		}

		public StartInfo(Map<String, String> answers) {
			this(!answers.isEmpty(), answers);
		}

		public StartInfo(boolean forceRestart, @Nullable StartInfo source) {
			this(forceRestart, (source == null) ? Collections.emptyMap() : source.answers);
		}

		private StartInfo(boolean forceRestart, Map<String, String> answers) {
			this.forceRestart = forceRestart;
			this.answers = answers;
		}

		public Map<Question, Value> getAnswerValues(KnowledgeBase base) {
			TerminologyManager manager = base.getManager();
			Map<Question, Value> answerValues = new HashMap<>();
			for (Entry<String, String> answer : answers.entrySet()) {
				Question question = manager.searchQuestion(answer.getKey());
				if (question == null) {
					LOGGER.error("question does not exists: " + answer.getKey());
					continue;
				}
				Value value = ValueUtils.createValue(question, answer.getValue());
				if (value == null) {
					LOGGER.error("question is assigned an invalid value: " + answer);
					continue;
				}
				answerValues.put(question, value);
			}
			return answerValues;
		}

		public void setDefaultAnswers(Session session, PSMethod psm) {
			Map<Question, Value> values = getAnswerValues(session.getKnowledgeBase());
			for (Entry<Question, Value> entry : values.entrySet()) {
				Fact fact = FactFactory.createFact(entry.getKey(), entry.getValue(), psm, psm);
				session.getBlackboard().addValueFact(fact);
			}
		}
	}
}
