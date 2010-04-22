package de.d3web.we.refactoring.dialog;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.MissingPropertyException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

import org.ceryle.xml.XHTML;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.refactoring.RefactoringTagHandler;
import de.d3web.we.refactoring.management.RefactoringManager;

public class RefactoringSession {

	RefactoringScript rs;

	public RefactoringSession() {
		try {
			we = WikiEngine.getInstance(
					KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
		}
		catch (NullPointerException e) {
			we = null;
		}
	}

	protected static Logger log = Logger.getLogger(RefactoringSession.class.getName());
	protected RefactoringManager refManager;
	protected WikiEngine we;
	private Thread thread = new Thread(new Runnable() {

		@Override
		public void run() {
			runSession();
		}
	});
	private boolean terminated = false;
	private final Lock lock = new ReentrantLock();
	private final Condition runDialog = lock.newCondition();
	private final Condition runRefactoring = lock.newCondition();
	private DeprecatedAbstractKnowWEAction nextAction;
	protected KnowWEParameterMap parameters;
	protected Map<String, String[]> gsonFormMap;

	public void setRefManager(String web) {
		if (this.refManager == null) this.refManager = new RefactoringManager(web);
	}

	public void runSession() {
		try {
			RefactoringScript rs = findRefactoringScript();
			// String ls = System.getProperty("line.separator");
			rs.setSession(this);
			rs.run();
			// TODO Stacktracing der Fehlermeldungen im Wiki anzeigen, nicht nur
			// die Fehlermeldung (für alle Fehler)
		}
		catch (MissingPropertyException mpe) {
			log.severe("MissingPropertyException while interpreting script: " + mpe.getMessage()); // I18N
			if (mpe.getMessage() != null) {
				warning(mpe.getMessage()
						+ XHTML.Tag_br
						+ "MissingPropertyException is often due to the content not being a valid Groovy script."); // I18N
			}
			else {
				warning("MissingPropertyException thrown while executing Groovy script."); // I18N
			}
		}
		catch (NullPointerException npe) {
			warning(getStackTraceString(npe));
		}
		catch (Exception e) {
			log.severe(e.getClass().getName() + " thrown interpreting Groovy script: "
					+ e.getMessage()); // I18N
			if (e.getMessage() != null) {
				error(e.getClass().getName() + " thrown interpreting Groovy script: "
						+ e.getMessage()); // I18N
				e.printStackTrace(System.err);
			}
			else {
				warning(e.getClass().getName() + " thrown while executing Groovy script."); // I18N
			}
		}
		saveAndFinish();
	}

	protected void saveAndFinish() {
		performNextAction(new DeprecatedAbstractKnowWEAction() {

			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Möchten Sie die Änderungen rückgängig machen?</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Undo</label>"
						+ "<select name='selectUndo' class='refactoring'>");
				html.append("<option value='nein'>nein</option>");
				html.append("<option value='ja'>ja</option>");
				html
						.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");

				for (KnowWEArticle changedArticle : refManager.getChangedArticles()) {
					String changedArticleID = changedArticle.getTitle();
					KnowWEArticleManager knowWEManager = KnowWEEnvironment.getInstance().getArticleManager(
							parameters.getWeb());

					int versionBefore = we.getPage(changedArticleID).getVersion();
					String textBefore = "textBefore";
					try {
						textBefore = we.getPageManager().getPageText(changedArticleID,
								versionBefore);
					}
					catch (ProviderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// der Artikel wird sicherheitshalber in einen konsistenten
					// Zustand gebracht TODO: siehe saveAndFinish() in
					// RefactoringSessionTestImpl
					// wenn neuer Artikel angelegt werden soll
					// FIXME: dies ist wieder so ein Hack :-)
					refManager.saveUpdatedArticle(changedArticle);
					KnowWEArticle consinstentChangedArticle = refManager.getArticle(changedArticleID);
					Map<String, String> nodesMap = new HashMap<String, String>();
					nodesMap.put(changedArticleID, consinstentChangedArticle.getSection().getOriginalText());
					knowWEManager.replaceKDOMNodes(parameters, changedArticleID, nodesMap);
					int versionAfter = we.getPage(changedArticleID).getVersion();
					String textAfter = "textAfter";
					try {
						textAfter = we.getPageManager().getPageText(changedArticleID, versionAfter);
					}
					catch (ProviderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					html.append("<br />Der folgende Artikel hat sich geändert: " + changedArticleID
							+ " von Version " + versionBefore
							+ " zu Version " + versionAfter + ".<br />");
					diff_match_patch dmp = new diff_match_patch();
					LinkedList<Diff> diffs = dmp.diff_main(textBefore, textAfter);
					dmp.diff_cleanupSemantic(diffs);
					html.append(dmp.diff_prettyHtml(diffs) + "<br />");
				}
				html.append("<br />Refactorings abgeschlossen.<br />");

				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js",
						KnowWERessourceLoader.RESOURCE_SCRIPT);
				return html.toString();
			}
		});

		if (gsonFormMap.get("selectUndo")[0].equals("ja")) {
			for (KnowWEArticle art : refManager.getChangedArticles()) {
				String st = art.getTitle();
				WikiPage page = we.getPage(st);
				WikiContext wc = new WikiContext(we, page);
				// TODO test, ob changedWikiPages.get(st)[0] ==
				// we.getPage(pageName).getVersion()
				try {
					we.saveText(wc, we.getPageManager().getPageText(st,
							we.getPage(st).getVersion() - 1));
				}
				catch (WikiException e) {
					e.printStackTrace();
				}
			}
		}

		// TODO verbessern
		nextAction = new DeprecatedAbstractKnowWEAction() {

			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				return "Refactorings abgeschlossen";
			}
		};

		// Ende des Refactorings
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// TODO wie wäre es, wenn sich der Thread gleich aus der HashMap von
		// RefactoringAction selbst enfernt?
		// sehr wichtig: Thread freigeben, da Script nun fertig
		terminated = true;
	}

	private String getStackTraceString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String s = sw.toString();
		return s;
	}

	/**
	 * Write the contents of the String <tt>message</tt> to the output as a
	 * warning message.
	 * <p>
	 * The message is contained within a paragraph with a 'warning' class.
	 */
	public void warning(String message) {
		log.warning("Warning: " + message); // I18N
		final StringBuffer html = new StringBuffer();
		html.append(XHTML.STag_p_class);
		html.append("warning");
		html.append(XHTML.QuotCl);
		html.append('\n');
		html.append(message);
		html.append(XHTML.ETag_p);
		performNextAction(new DeprecatedAbstractKnowWEAction() {

			@Override
			public String perform(KnowWEParameterMap parameters) {
				return html.toString();
			}
		});
	}

	/**
	 * Process a fatal error by clearing the existing buffer and populating it
	 * with an error message.
	 * <p>
	 * The message is contained within a paragraph with an 'error' class.
	 */
	protected void error(String message) {
		log.severe("Error: " + message); // I18N
		final StringBuffer html = new StringBuffer();
		html.setLength(0);
		html.append(XHTML.STag_div_class);
		html.append("error");
		html.append(XHTML.QuotCl);
		html.append('\n');
		html.append(XHTML.STag_b);
		html.append("Groovy Script Failed: "); // I18N
		html.append(XHTML.ETag_b);
		html.append(XHTML.Tag_br);
		if (message != null) { // error message, if any
			html.append(message);
			html.append(XHTML.Tag_br);
		}
		html.append(XHTML.ETag_div);
		performNextAction(new DeprecatedAbstractKnowWEAction() {

			@Override
			public String perform(KnowWEParameterMap parameters) {
				return html.toString();
			}
		});
	}

	public Thread getThread() {
		return thread;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public Lock getLock() {
		return lock;
	}

	public Condition getRunDialog() {
		return runDialog;
	}

	public Condition getRunRefactoring() {
		return runRefactoring;
	}

	public DeprecatedAbstractKnowWEAction getNextAction() {
		return nextAction;
	}

	protected void performNextAction(DeprecatedAbstractKnowWEAction action) {
		nextAction = action;
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// Hier könnte parallel ausgeführter Code stehen
		lock.lock();
		try {
			runRefactoring.await();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			lock.unlock();
		}
	}

	public void setParameters(KnowWEParameterMap parameters, Map<String, String[]> gsonFormMap) {
		this.parameters = parameters;
		this.gsonFormMap = gsonFormMap;
		String web = parameters.getWeb();
		this.setRefManager(web);
	}

	// GROOVY-METHODEN

	// TODO die Refactoring-Session muss noch anständig terminiert werden (dies
	// gilt auch für Sessions, die durch eine warning oder einen error
	// unterbrochen wurden

	protected RefactoringScript findRefactoringScript() {
		RefactoringScript rs = null;
		if (gsonFormMap.get("optgroup")[0].equals("custom")) {
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> section = refManager.getArticle(parameters.getTopic()).getSection();
			// siehe RefactoringTagHandler.java
			Section<?> refactoring = section.findChild(gsonFormMap.get("selectRefactoring")[0]);
			StringBuffer sb = new StringBuffer(
					"package de.d3web.we.refactoring.script; "
							+ "public class RefactoringScriptGroovyFromWiki extends RefactoringScriptGroovy{ @Override public void run() { ");
			sb.append(refactoring.getOriginalText());
			sb.append("}}");
			GroovyCodeSource gcs = new GroovyCodeSource(sb.toString(),
					"RefactoringScriptGroovyFromWiki", "/refactoring-plugin-groovy");

			// FIXME eventuell sollte RefactoringScript die Klasse Script (von
			// Groovy) erweitern.
			// Vorteil: es muss nicht viel geändert werden (nur die Signatur der
			// run()-Methode,
			// es ist dann ein richtiges Groovy-Script und man kann das ganze
			// über die GroovyShell steuern
			// und man spart sich die hässliche String-Zusammensetzung des
			// Wiki-Skripts sowie die eigenen Definitionen von
			// propertyMissingException.
			// Script sc = null;
			// GroovyShell shell = null;
			// shell.parse();
			// CompilerConfiguration config = new CompilerConfiguration();
			// config.setScriptBaseClass("Script");
			// GroovyShell gs = new GroovyShell();
			// gs.evaluate(gcs);

			Class<?> groovyClass = loader.parseClass(gcs);
			rs = null;
			try {
				rs = (RefactoringScript) groovyClass.newInstance();
			}
			catch (InstantiationException ie) {
				log.severe("unable to instantiate Groovy interpreter: " + ie.getMessage()); // I18N
				if (ie.getMessage() != null) {
					warning(ie.getMessage());
				}
				else {
					warning("InstantiationException thrown while executing Groovy script."); // I18N
				}
			}
			catch (IllegalAccessException iae) {
				log.severe("illegal access instantiating Groovy interpreter: " + iae.getMessage()); // I18N
				if (iae.getMessage() != null) {
					warning(iae.getMessage());
				}
				else {
					warning("IllegalAccessException thrown while executing Groovy script."); // I18N
				}
			}
		}
		else if (gsonFormMap.get("optgroup")[0].equals("built-in")) {
			Class<? extends RefactoringScript> scriptClass = RefactoringTagHandler.SCRIPTS.get(gsonFormMap.get("selectRefactoring")[0]);
			try {
				rs = scriptClass.newInstance();
			}
			catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rs;
	}
}