package cc.knowwe.dialog.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpSession;

import cc.knowwe.dialog.SessionConstants;
import cc.knowwe.dialog.Utils;
import org.apache.commons.lang.StringUtils;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
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
import de.d3web.interview.inference.PSMethodInterview;
import de.d3web.we.basic.SessionCreatedEvent;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Command to start a new case of a specified knowledge base.
 *
 * @author Volker Belli
 */
public class StartCase extends AbstractAction implements EventListener {

	public static final String PARAM_USER = "user";
	public static final String PARAM_LANGUAGE = "lang";
	public static final String PARAM_RESTART_SESSION = "restart";
	public static final String PARAM_PROTOCOL_PATH = "protocol";

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
	 * Defines a provider that is capable to deliver the knowledge base to be used for a new
	 * session.
	 *
	 * @author volker_belli
	 * @created 22.09.2010
	 */
	public interface KnowledgeBaseProvider {

		/**
		 * Returns the display name of the knowledge base.
		 */
		String getName() throws IOException;

		/**
		 * Returns the description of the knowledge base.
		 */
		String getDescription() throws IOException;

		/**
		 * Returns the icon of the knowledge base.
		 */
		Resource getFavIcon() throws IOException;

		/**
		 * Returns the knowledge base. If required the base is loaded on demand here.
		 */
		KnowledgeBase getKnowledgeBase() throws IOException;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String language = context.getParameter(PARAM_LANGUAGE);
		String protocolPath = context.getParameter(PARAM_PROTOCOL_PATH);

		HttpSession httpSession = context.getSession();
		StartInfo restart = (StartInfo) httpSession.getAttribute(PARAM_RESTART_SESSION);

		// remove the attribute, the restart is only done once
		httpSession.removeAttribute(PARAM_RESTART_SESSION);
		KnowledgeBaseProvider provider = (KnowledgeBaseProvider) httpSession.getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_PROVIDER);

		try {
			KnowledgeBase base = provider.getKnowledgeBase();
			httpSession.setAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE, base);
			if (Strings.isBlank(protocolPath)) {
				startCase(context, base, restart);
			}
			else {
				loadCase(context, base, protocolPath);
			}

			// adapt language to one of the supported ones
			// of the loaded knowledge base
			Locale locale = findBestLocale(base, new Locale(language));
			if (!locale.getLanguage().isEmpty()) {
				language = locale.getLanguage();
			}
			else {
				// the locale is not valid -> no lang
				language = "NO_LANG";
				Log.warning(locale + " is not a valid language. Using: " + language);
			}
		}
		catch (IOException e) {
			Utils.redirectToErrorPage(context, e);
			return;
		}

		// and redirect to interview
		context.sendRedirect("Resource/ui.zip/html/index.html?" + PARAM_LANGUAGE + "=" + language);
	}

	/**
	 * Opens a xml or zip file (and take the first xml file from there) read the records from.
	 *
	 * @throws IOException
	 * @created 31.08.2011
	 */
	private void loadCase(UserActionContext context, KnowledgeBase knowledgebase, String filePath) throws IOException {
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

		Session session = SessionConversionFactory.copyToSession(
				knowledgebase, records.iterator().next());
		wrapFormStrategy(session);
		// set the session in the provider
		SessionProvider.setSession(context, session);
	}

	private void startCase(UserActionContext context, KnowledgeBase knowledgebase, StartInfo restart) throws IOException {
		Session session = SessionProvider.getSession(context, knowledgebase);
		if (restart != null && restart.forceRestart) {
			session = SessionProvider.createSession(context, knowledgebase);
			restart.setDefaultAnswers(session, PSMethodInit.getInstance());
		}
		wrapFormStrategy(session);
	}

	private void wrapFormStrategy(Session session) {
		Interview interview = session.getSessionObject(session.getPSMethodInstance(PSMethodInterview.class));
		FormStrategy originalFormStrategy = interview.getFormStrategy();
		if (!(originalFormStrategy instanceof NextUnansweredQuestionFormStrategy)) {
			interview.setFormStrategy(new SingleQuestionFormStrategyWrapper(originalFormStrategy));
		}
	}

	/**
	 * Initialize a new knowledge base provider and starts a case with that provider. The provider
	 * is use in future for starting cases at this http session.
	 *
	 * @param context  the ActionContext to be used
	 * @param provider the KnwoeldgeBaseProvider to be responsible to create the knowledge bases
	 * @throws IOException if something fails during starting the case
	 * @created 22.09.2010
	 */
	public void startCase(UserActionContext context, KnowledgeBaseProvider provider) throws IOException {
		context.getSession().setAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_PROVIDER, provider);
		execute(context);
	}

	/**
	 * Returns the best-Fit locale of the knowledge base in comparison to specified language.
	 *
	 * @param knowledgeBase the knowledge base to select a language from
	 * @created 05.05.2011
	 */
	private Locale findBestLocale(KnowledgeBase knowledgeBase, Locale preferredLocale) {
		Set<Locale> locales = KnowledgeBaseUtils.getAvailableLocales(knowledgeBase);
		Locale locale = findLocale(preferredLocale, locales);
		if (locale == null) locale = findLocale(Locale.getDefault(), locales);
		if (locale == null) locale = findLocale(Locale.ENGLISH, locales);
		if (locale == null) locale = findLocale(Locale.GERMAN, locales);
		if (locale == null && !locales.isEmpty()) locale = locales.iterator().next();
		if (locale != null) {
			return locale;
		}
		else {
			return preferredLocale;
		}
	}

	private static Locale findLocale(Locale preferredLocale, Set<Locale> locales) {
		String language = preferredLocale.getLanguage();
		for (Locale locale : locales) {
			if (locale.getLanguage().equalsIgnoreCase(language)) {
				return locale;
			}
		}
		return null;
	}

	public static class StartInfo {
		private final boolean forceRestart;
		private final Map<String, String> answers;

		public StartInfo(boolean forceRestart) {
			this(forceRestart, Collections.emptyMap());
		}

		public StartInfo(Map<String, String> answers) {
			this(true, answers);
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
					Log.severe("question does not exists: " + answer.getKey());
					continue;
				}
				Value value = ValueUtils.createValue(question, answer.getValue());
				if (value == null) {
					Log.severe("question is assigned an invalid value: " + answer);
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
