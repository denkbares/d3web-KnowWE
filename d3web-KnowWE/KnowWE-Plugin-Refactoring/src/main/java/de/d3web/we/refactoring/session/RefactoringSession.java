package de.d3web.we.refactoring.session;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

import org.apache.log4j.Logger;
import org.ceryle.xml.XHTML;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.content.PageRenamer;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.CommentLineType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.decisionTree.QClassID;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;
import de.d3web.we.kdom.objects.QuestionnaireID;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.kdom.xcl.CoveringListContent;
import de.d3web.we.kdom.xcl.XCList;

public class RefactoringSession {
	
	private static Logger log = Logger.getLogger(RefactoringSession.class);
	
	private Thread thread = new Thread(new Runnable() {
		@Override
		public void run() {
			runSession();
		}
	});
	public Thread getThread() {
		return thread;
	}
	private boolean terminated = false;
	public boolean isTerminated() {
		return terminated;
	}
	private final Lock lock = new ReentrantLock();
	public Lock getLock() {
		return lock;
	}
	private final Condition runDialog = lock.newCondition();
	public Condition getRunDialog() {
		return runDialog;
	}
	private final Condition runRefactoring = lock.newCondition();
	public Condition getRunRefactoring() {
		return runRefactoring;
	}
	private KnowWEAction nextAction;
	public KnowWEAction getNextAction() {
		return nextAction;
	}
	
	/**
	 * Key: String - the KDOM-ID of the changed section
	 * Val: String[0] - the section before the change; String[1] - the section after the change
	 */
	private Map<String, String[]> changedSections = new HashMap<String, String[]>();
	/**
	 * Key: String - the name of the changed wiki page
	 * Val: int[0] - the version of the wiki page before the changes - int[0] - the version of the wiki page after the changes
	 */
	private Map<String, int[]> changedWikiPages = new HashMap<String, int[]>();
	
	private KnowWEParameterMap parameters;
	private KnowWEArticleManager manager;
	private Map<String,String[]> gsonFormMap;
	
	public void setParameters(KnowWEParameterMap parameters, Map<String, String[]> gsonFormMap) {
		this.parameters = parameters;
		this.manager = KnowWEEnvironment.getInstance().getArticleManager(parameters.getWeb());
		this.gsonFormMap = gsonFormMap;
	}
	
	public void runSession() {
		try {
			Object[] args = {};
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> refactoringSection = findRefactoringSection();
			String identity = "R_E_F_A_C_T_O_R_I_N_G___S_E_S_S_I_O_N";
//			String identity = "rs";
//			String ls = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			sb.append(identity + ".identity{");
			sb.append(refactoringSection.getOriginalText());
			sb.append("}");
			Class<?> groovyClass = loader.parseClass(sb.toString());
//			GroovyObject gob = new XCLToRules();
			GroovyObject gob = (GroovyObject) groovyClass.newInstance();
			if (gob instanceof groovy.lang.Script) {
				Script script = (Script) gob;
				Binding binding = script.getBinding();
				binding.setVariable(identity, this);
				script.invokeMethod("run", args);
			} else {
				gob.invokeMethod("run", args);
			}
		// TODO Stacktracing der Fehlermeldungen im Wiki anzeigen, nicht nur die Fehlermeldung
		} catch (MissingPropertyException mpe) {
			log.error("MissingPropertyException while interpreting script: " + mpe.getMessage()); // I18N
			if (mpe.getMessage() != null) {
				warning(mpe.getMessage() + XHTML.Tag_br
						+ "MissingPropertyException is often due to the content not being a valid Groovy script."); // I18N
			} else {
				warning("MissingPropertyException thrown while executing Groovy script."); // I18N
			}
		} catch (InstantiationException ie) {
			log.error("unable to instantiate Groovy interpreter: " + ie.getMessage()); // I18N
			if (ie.getMessage() != null) {
				warning(ie.getMessage());
			} else {
				warning("InstantiationException thrown while executing Groovy script."); // I18N
			}
		} catch (IllegalAccessException iae) {
			log.error("illegal access instantiating Groovy interpreter: " + iae.getMessage()); // I18N
			if (iae.getMessage() != null) {
				warning(iae.getMessage());
			} else {
				warning("IllegalAccessException thrown while executing Groovy script."); // I18N
			}
		} catch(NullPointerException npe) {
		    warning(getStackTraceString(npe));
		} catch (Exception e) {
			log.error(e.getClass().getName() + " thrown interpreting Groovy script: " + e.getMessage()); // I18N
			if (e.getMessage() != null) {
				error(e.getClass().getName() + " thrown interpreting Groovy script: " + e.getMessage()); // I18N
				e.printStackTrace(System.err);
			} else {
				warning(e.getClass().getName() + " thrown while executing Groovy script."); // I18N
			}
		}
		Set<String> changedArticles = new HashSet<String>();
		for (String changedSectionID: changedSections.keySet()) {
			String title;
			// es muss so umständlich gemacht werden, wenn man sich die section holen will um den article zu bestimmen kann
			// dies zu problemen führen, denn die section könnte ja gelöscht worden sein.
			if (changedSectionID.contains("/")) {
				title = changedSectionID.substring(0, changedSectionID.indexOf("/"));
			} else {
				title = changedSectionID;
			}
			changedArticles.add(title);
		}
		for (String changedArticle: changedArticles) {
			WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
			int versionBefore = we.getPage(changedArticle).getVersion();
			StringBuilder newArticleText = new StringBuilder();
			manager.findNode(changedArticle).collectTextsFromLeaves(newArticleText);
			manager.replaceKDOMNode(parameters, changedArticle, changedArticle, newArticleText.toString());
			// tracking of article versions
			int versionAfter = we.getPage(changedArticle).getVersion();
			if (! changedWikiPages.containsKey(changedArticle)) {
				changedWikiPages.put(changedArticle, new int[] {versionBefore, versionAfter});
			} else {
				changedWikiPages.put(changedArticle, new int[] {changedWikiPages.get(changedArticle)[0], versionAfter});
			}
		}
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				
				for (String id: changedSections.keySet()) {
					html.append("<br />Die Section mit folgender ID hat sich geändert: " + id + "<br />");
					diff_match_patch dmp = new diff_match_patch();
					LinkedList<Diff> diffs = dmp.diff_main(changedSections.get(id)[0],changedSections.get(id)[1]);
					dmp.diff_cleanupSemantic(diffs);
					html.append(dmp.diff_prettyHtml(diffs) + "<br />");
				}
				html.append("<br />Refactorings abgeschlossen.<br />");
				
				WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
											
				for(String pageName: changedWikiPages.keySet()) {
					html.append("<br />Die Seite [" + pageName + "] wurde verändert von Version " + changedWikiPages.get(pageName)[0] +
					" zu Version " + changedWikiPages.get(pageName)[1] + ". Aktuelle Version der Seite ist " + we.getPage(pageName).getVersion() + 
					".<br />");
				}
				 
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				html.append("<fieldset><div class='left'>"
						+ "<p>Möchten Sie die Änderungen rückgängig machen?</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Undo</label>"
						+ "<select name='selectUndo' class='refactoring'>");
				html.append("<option value='nein'>nein</option>");
				html.append("<option value='ja'>ja</option>");
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		
		if (gsonFormMap.get("selectUndo")[0].equals("ja")) {
			WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
			for (String st: changedWikiPages.keySet()) {
				WikiPage page = we.getPage(st);
				WikiContext wc = new WikiContext(we, page);
				// TODO test, ob changedWikiPages.get(st)[0] == we.getPage(pageName).getVersion()
				try {
					we.saveText(wc, we.getPageManager().getPageText(st, changedWikiPages.get(st)[0]));
				} catch (WikiException e) {
					e.printStackTrace();
				}
			}
		}
		
		// TODO verbessern
		nextAction = new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				return "Refactorings abgeschlossen";
			}
		};
		
		// Ende des Refactorings
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// TODO wie wäre es, wenn sich der Thread gleich aus der HashMap von RefactoringAction selbst enfernt? 
		// sehr wichtig: Thread freigeben, da Script nun fertig
		terminated  = true;
	}

	private String getStackTraceString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String s = sw.toString();
		return s;
	}

	// GROOVY-METHODEN TODO auslagern?!
	
	public void addRulesText(StringBuffer sb, Section<RulesSectionContent> rulesSectionContent) {
		replaceSection(rulesSectionContent, rulesSectionContent.getOriginalText() + sb.toString());
	}

	// TODO wenn es noch keine RulesSection gibt, dann muss die noch gebaut werden
	public Section<RulesSectionContent> findRulesSectionContent(Section<?> knowledgeSection) {
		KnowWEArticle article = knowledgeSection.getArticle();
		Section<RulesSectionContent> rulesSectionContent = article.getSection().findSuccessor(new RulesSectionContent());
		return rulesSectionContent;
	}
	
	// TODO wenn es noch keine CoveringListSection gibt, dann muss die noch gebaut werden
	public Section<CoveringListContent> findCoveringListContent(Section<?> knowledgeSection) {
		KnowWEArticle article = knowledgeSection.getArticle();
		Section<CoveringListContent> coveringListContent = article.getSection().findSuccessor(new CoveringListContent());
		return coveringListContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		replaceSection(knowledgeSection, "\n");
	}

	// TODO für dieses Refactoring sollten die Werte besser berechnet werden
	public void createRulesText(Section<Finding> sec, String solutionID, StringBuffer sb) {
		sb.append("\nIF " + sec.getOriginalText());
		sb.append("\n    THEN ");
		sb.append(solutionID + " = P7");
	}

	public String findSolutionID(Section<?> knowledgeSection) {
		Section<SolutionID> solutionID = knowledgeSection.findSuccessor(new SolutionID());
		return solutionID.getOriginalText();
	}

	public List<Section<Finding>> findFindings(Section<?> knowledgeSection) {
		List<Section<Finding>> findings = new ArrayList<Section<Finding>>();
		knowledgeSection.findSuccessorsOfType(new Finding(), findings);
		return findings;
	}

	public Section<?> findXCList() {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				StringBuffer html = new StringBuffer();
				SortedSet<String> topics = new TreeSet<String>();
				for(Iterator<KnowWEArticle> it = manager.getArticleIterator(); it.hasNext();) {
					topics.add(it.next().getTitle());
				}
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie die zu transformierende Überdeckungsliste aus:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>XCList</label>"
						+ "<select name='selectXCList' class='refactoring'>");
				for(Iterator<String> it = topics.iterator(); it.hasNext();) {
					KnowWEArticle article = manager.getArticle(it.next());
					Section<?> articleSection = article.getSection();
					List<Section<XCList>> xclists = new ArrayList<Section<XCList>>();
					articleSection.findSuccessorsOfType(new XCList(), xclists);
					for (Section<?> xclist : xclists) {
						html.append("<option value='" + xclist.getId() + "'>Seite: " + article.getTitle() + " - XCList: " 
								+ xclist.findSuccessor(new SolutionID()).getOriginalText() + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		Section<?> knowledge = manager.findNode(gsonFormMap.get("selectXCList")[0]);
		return knowledge;
	}
	
	public AbstractKnowWEObjectType findRenamingType() {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Typ des Objekts aus, welches sie umbenennen möchten:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objekttyp</label>"
						+ "<select name='selectRenamingType' class='refactoring'>");
				html.append("<option value='KnowWEArticle'>KnowWEArticle (Wikiseite)</option>");
				html.append("<option value='QuestionnaireID'>QuestionnaireID (Fragebogen)</option>");
				html.append("<option value='QuestionID'>QuestionID (Frage)</option>");
				html.append("<option value='QuestionTreeAnswerID'>QuestionTreeAnswerID (Antwort)</option>");
				html.append("<option value='SolutionID'>SolutionID (Lösung)</option>");
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String typeString = gsonFormMap.get("selectRenamingType")[0];
		AbstractKnowWEObjectType type = getTypeFromString(typeString);
		return type;
	}

	public AbstractKnowWEObjectType getTypeFromString(String typeString) {
		AbstractKnowWEObjectType type = null;
		if(typeString.equals("KnowWEArticle")) {
			type = new KnowWEArticle();
		} else if(typeString.equals("QuestionnaireID")) {
			type = new QuestionnaireID();
		} else if(typeString.equals("QuestionID")) {
			type = new QuestionID();
		} else if(typeString.equals("QuestionTreeAnswerID")) {
			type = new QuestionTreeAnswerID();
		} else if(typeString.equals("SolutionID")) {
			type = new SolutionID();
		}
		return type;
	}
	
	// TODO bereits hier alternative Typen berücksichtigen (QClassID, QuestionnaireID)
	public <T extends AbstractKnowWEObjectType> String findObjectID(final T type) {
		//FIXME doppelte rausfiltern
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				StringBuffer html = new StringBuffer();
				SortedSet<String> topics = new TreeSet<String>();
				for(Iterator<KnowWEArticle> it = manager.getArticleIterator(); it.hasNext();) {
					topics.add(it.next().getTitle());
				}
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Namen des Objekts mit dem Typ <strong>" + type.getName() + "</strong>:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<select name='selectObjectID' class='refactoring'>");
				for(Iterator<String> it = topics.iterator(); it.hasNext();) {
					KnowWEArticle article = manager.getArticle(it.next());
					Section<?> articleSection = article.getSection();
					List<Section<T>> objects = new ArrayList<Section<T>>();
					articleSection.findSuccessorsOfType(type, objects);
					for (Section<?> object : objects) {
						String name = (type instanceof KnowWEArticle) ? object.getId() : object.getOriginalText();
						String question = (type instanceof QuestionTreeAnswerID) ? " - Frage: " + findQuestion(object).getOriginalText() : "";
						html.append("<option value='" + object.getId() + "'>Seite: " + article.getTitle() + question + " - Objekt: " 
								+ name + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String objectID = gsonFormMap.get("selectObjectID")[0];
		return objectID;
	}
	
	private Section<QuestionID> findQuestion(Section<?> answerSection) {
		// TODO verbessern
		return answerSection.getFather().getFather().getFather().getFather().getFather().findChildOfType(DashTreeElement.class).findSuccessor(new QuestionID());
	}
	private Section<QuestionTreeAnswerID> findAnswer(Section<?> diagnosisSection) {
		// TODO verbessern
//		System.out.println(diagnosisID);
//		System.out.println(diagnosisSection.getOriginalText());
		return diagnosisSection.getFather().getFather().getFather().getFather().getFather().findChildOfType(DashTreeElement.class).findSuccessor(new QuestionTreeAnswerID());
	}
	
	private Section<QuestionTreeAnswerID> findAnswerIndicatingQuestion(Section <?> questionSection) {
		// TODO verbessern
		return questionSection.getFather().getFather().getFather().getFather().getFather().findChildOfType(DashTreeElement.class).findSuccessor(new QuestionTreeAnswerID());
	}
	
	public String findNewName() {
		//FIXME doppelte rausfiltern
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den neuen Namen des gewählten Objekts:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<input type='text' name='selectNewName' class='refactoring'>");
				html.append("</div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String newName = gsonFormMap.get("selectNewName")[0];
		return newName;
	}
	
	
	public <T extends AbstractKnowWEObjectType> List<Section<? extends AbstractKnowWEObjectType>> findRenamingList(T type, String objectID) {
		return findRenamingList(type, objectID, null);
	}
	
	public <T extends AbstractKnowWEObjectType> List<Section<? extends AbstractKnowWEObjectType>> findRenamingList(T type, String objectID, String newName) {
		List<Section<? extends AbstractKnowWEObjectType>> fullList = new ArrayList<Section<? extends AbstractKnowWEObjectType>>();
		List<Section<? extends AbstractKnowWEObjectType>> filteredList = new ArrayList<Section<? extends AbstractKnowWEObjectType>>();
		if (type instanceof QuestionTreeAnswerID) {
			Section<QuestionID> question = findQuestion(manager.findNode(objectID));
			List<Section<QuestionTreeAnswerID>> answers = new LinkedList<Section<QuestionTreeAnswerID>>();
			// TODO verbessern
			question.getFather().getFather().getFather().getFather().findSuccessorsOfType(new QuestionTreeAnswerID(), 5 , answers);
			fullList.addAll(answers);
			// hole alle FindingQuestion's welche den gleichen getOriginalText() haben wie die Question, zu welcher die QuestionTreeAnswerID
			// gehört
			List<Section<? extends AbstractKnowWEObjectType>> findingQuestions = findRenamingList(new FindingQuestion(), question.getId());
			// bestimme dafür die passenden Antworten
			for (Section<? extends AbstractKnowWEObjectType> questionSection : findingQuestions) {
				Section<? extends AbstractKnowWEObjectType> answer = questionSection.getFather().findSuccessor(new FindingAnswer());
				fullList.add(answer);
			}
		} else {
			SortedSet<String> topics = new TreeSet<String>();
			for (Iterator<KnowWEArticle> it = manager.getArticleIterator(); it.hasNext();) {
				topics.add(it.next().getTitle());
			}
			for (Iterator<String> it = topics.iterator(); it.hasNext();) {
				KnowWEArticle article = manager.getArticle(it.next());
				Section<?> articleSection = article.getSection();
				List<Section<T>> objects = new ArrayList<Section<T>>();
				articleSection.findSuccessorsOfType(type, objects);
				fullList.addAll(objects);
				if (type instanceof QuestionnaireID || type instanceof QuestionID) {
					List<Section<QClassID>> objects2 = new ArrayList<Section<QClassID>>();
					articleSection.findSuccessorsOfType(new QClassID(), objects2);
					fullList.addAll(objects2);
				}
				if (type instanceof QuestionID) {
					List<Section<FindingQuestion>> objects2 = new ArrayList<Section<FindingQuestion>>();
					articleSection.findSuccessorsOfType(new FindingQuestion(), objects2);
					fullList.addAll(objects2);
				}
				// FIXME Solutions müssen ebenfalls behandelt werden!
			}
		}
		for (Section<? extends AbstractKnowWEObjectType> object : fullList) {
			String name = (newName != null) ? newName : manager.findNode(objectID).getOriginalText();
			if (object.getOriginalText().equals(name)) {
				filteredList.add(object);
			}
		}
		return filteredList;
	}

	public <T extends AbstractKnowWEObjectType> void renameElement(Section<? extends AbstractKnowWEObjectType> section, String newName, T type){
		// FIXME Report und Undo von umbenannten Wiki-Seiten, include-Referenzen mit umbenennen
		if (type instanceof KnowWEArticle) {
			PageRenamer pr = new PageRenamer();
			try {
				WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
				WikiPage page = we.getPage(section.getId());
				WikiContext wc = new WikiContext(we, page);
				pr.renamePage(wc, section.getId(), newName, true);
				
				
				
			} catch (WikiException e) {
				// FIXME Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			replaceSection(section, newName);
		}
	}
	
	// TODO die Refactoring-Session muss noch anständig terminiert werden (dies gilt auch für Sessions, die durch eine warning oder einen error
	// unterbrochen wurden
	public void printExistingElements(final List<Section<? extends AbstractKnowWEObjectType>> existingElements) {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				html.append("<br />Das Refactoring kann nicht durchgeführt werden, da es Konflikte mit folgenden Sektionen gibt:<br /><br />");
				for (Section<? extends AbstractKnowWEObjectType> section: existingElements) {
					html.append("<br />ID: " + section.getId() + "<br />");
					html.append("Inhalt: " + section.getOriginalText() + "<br />");
				}
				html.append("<fieldset><form name='refactoringForm'><div>"
						+ "<input type='button' value='Abbrechen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
	}
	
	// TODO wird im KDOM leider als QuestionID und nicht als SolutionID geparsed, mal mit Jochen reden
	public List<Section<QuestionID>> findSolutions(String value, String pageName) {
		List<Section<QuestionID>> returnList = new LinkedList<Section<QuestionID>>();
		Section<?> articleSection = manager.findNode(pageName);
		List<Section<AnonymousType>> found = new ArrayList<Section<AnonymousType>>();
		// FIXME es muss noch überprüft werden, ob AnonymousType.getName().equals("SetValueArgument") gilt.
		articleSection.findSuccessorsOfType(new AnonymousType(""), found);
		for (Section<AnonymousType> atSection: found) {
			if (atSection.getOriginalText().equals(value)) {
				Section<QuestionID> foundSolution = atSection.getFather().findSuccessor(new QuestionID());
				returnList.add(foundSolution);
			}
		}
		return returnList;
	}
	
	public void createXCLFromFindingsTrace (Section<QuestionID> solution) {
		Section<QuestionTreeAnswerID> answer = findAnswer(solution);
		Section<QuestionID> question = findQuestion(answer);
		StringBuffer sb = new StringBuffer();
		sb.append("\n" + solution.getOriginalText() +"{\n");
		traceFindings(sb, question, answer);
		sb.append("}");
		Section<CoveringListContent> covCon = findCoveringListContent(solution);
		replaceSection(covCon, covCon.getOriginalText() + sb.toString());
	}

	private void traceFindings(StringBuffer sb, Section<QuestionID> question, Section<QuestionTreeAnswerID> answer) {
		sb.append("\t" + question.getOriginalText() + " = " + answer.getOriginalText() + ",\n");
		Section<QuestionTreeAnswerID> nextAnswer = findAnswerIndicatingQuestion(question);
		if(nextAnswer != null) {
			Section<QuestionID> nextQuestion = findQuestion(nextAnswer);
			traceFindings(sb, nextQuestion, nextAnswer);
		}
	}
	
	public void deleteSolutionOccurrences (Section<QuestionID> solution) {
		List<Section<QuestionID>> list = new LinkedList<Section<QuestionID>>();
		solution.getArticle().getSection().findSuccessorsOfType(new QuestionID(), list);
		for(Section<QuestionID> sqid: list) {
			if (sqid.getOriginalText().equals(solution.getOriginalText())) {
				replaceSection(sqid.getFather().getFather().getFather(), "");
			}
		}
	}
	
	public void deleteComments (String sectionID) {
		Section<?> section = manager.findNode(sectionID);
		List<Section<CommentLineType>> list = new LinkedList<Section<CommentLineType>>();
		section.getArticle().getSection().findSuccessorsOfType(new CommentLineType(), list);
		for(Section<CommentLineType> commentLine: list) {
			replaceSection(commentLine, "");
		}
	}
	
	// HILFSMETHODEN
	
	/**
	 * Write the contents of the String <tt>message</tt> to the output as a
	 * warning message.
	 * <p>
	 * The message is contained within a paragraph with a 'warning' class.
	 */
	public void warning(String message) {
		log.warn("Warning: " + message); // I18N
		final StringBuffer html = new StringBuffer();
		html.append(XHTML.STag_p_class);
		html.append("warning");
		html.append(XHTML.QuotCl);
		html.append('\n');
		html.append(message);
		html.append(XHTML.ETag_p);
		performNextAction(new AbstractKnowWEAction() {
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
	private void error(String message) {
		log.error("Error: " + message); // I18N
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
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				return html.toString();
			}
		});
	}

	private void performNextAction(KnowWEAction action) {
		nextAction = action;
		lock.lock();
		runDialog.signal();
		lock.unlock();
		// Hier könnte parallel ausgeführter Code stehen
		lock.lock();
		try {
			runRefactoring.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	private Section<?> findRefactoringSection() {
		// TODO momentan werden nur Refactorings betrachtet, die sich auf dieser Seite befinden
		Section<?> section = manager.getArticle(parameters.getTopic()).getSection();
		// siehe RefactoringTagHandler.java
		Section<?> refactoring = section.findChild(gsonFormMap.get("selectRefactoring")[0]);
		return refactoring;
	}
	
	private void replaceSection(Section<?> section, String newText) {
		if (! changedSections.containsKey(section.getId())) {
			changedSections.put(section.getId(), new String[] {section.getOriginalText(), newText});
		} else {
			changedSections.put(section.getId(), new String[] {changedSections.get(section.getId())[0], newText});
		}
		KnowWEArticle article = section.getArticle(); 
		String topic = article.getTitle();
		customReplaceKDOMNodeWithoutSave(parameters, topic, section.getId(), newText);
		//KnowWEArticle newArticle = new KnowWEArticle(text, article.getTitle(), article.getAllowedChildrenTypes(), article.getWeb());
		//manager.saveUpdatedArticle(newArticle);
	}
	

	public void customReplaceKDOMNodeWithoutSave(KnowWEParameterMap map, String articleName,
			String nodeID, String replacingText) {
		KnowWEArticle art = manager.getArticle(articleName);
		if (art == null) {
			throw new RuntimeException("customReplaceKDOMNodeWithoutSave: Article " + articleName + " could not be found.");
		}
		Section<KnowWEArticle> root = art.getSection();
		replaceNodeTextSetLeaf(root, nodeID, replacingText);
	}
	
	private void replaceNodeTextSetLeaf(Section<?> sec, String nodeID, String replacingText) {
		if (sec.getId().equals(nodeID)) {
			sec.setOriginalText(replacingText);
			sec.removeAllChildren();
			return;
		}
		List<Section<?>> children = sec.getChildren();
		if (children == null || children.isEmpty() || sec.getObjectType() instanceof Include) {
			return;
		}
		for (Section<?> section : children) {
			replaceNodeTextSetLeaf(section, nodeID, replacingText);
		}
	}
}