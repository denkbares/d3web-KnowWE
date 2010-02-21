package de.d3web.we.refactoring.session;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.basic.CommentLineType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.decisionTree.QClassID;
import de.d3web.we.kdom.decisionTree.QuestionsSection;
import de.d3web.we.kdom.decisionTree.QuestionsSectionContent;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.defaultMarkup.ContentType;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;
import de.d3web.we.kdom.objects.QuestionnaireID;
import de.d3web.we.kdom.questionTreeNew.QuestionTreeRootType;
import de.d3web.we.kdom.rules.RulesSectionContent;
import de.d3web.we.kdom.xcl.CoveringListContent;
import de.d3web.we.kdom.xcl.XCList;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.refactoring.management.RefactoringManager;

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
	
	private KnowWEParameterMap parameters;
	private Map<String,String[]> gsonFormMap;
	
	private RefactoringManager refManager;
	private WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
	
	public void setParameters(KnowWEParameterMap parameters, Map<String, String[]> gsonFormMap) {
		this.parameters = parameters;
		this.gsonFormMap = gsonFormMap;
		if (this.refManager == null) this.refManager = new RefactoringManager(parameters.getWeb());
	}
	
	private void runSession() {
		try {
			Object[] args = {};
			ClassLoader parent = getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			Section<?> refactoringSection = findRefactoringSection();
			String identity = "refactoringSession";
//			String ls = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			sb.append(identity + ".identity{");
			sb.append(refactoringSection.getOriginalText());
			sb.append("}");
			Class<?> groovyClass = loader.parseClass(sb.toString());
//			GroovyObject gob = new DeleteComments(this);
			GroovyObject gob = (GroovyObject) groovyClass.newInstance();
			if (gob instanceof groovy.lang.Script) {
				Script script = (Script) gob;
				Binding binding = script.getBinding();
				binding.setVariable(identity, this);
				script.invokeMethod("run", args);
			} else {
				gob.invokeMethod("run", args);
			}
		// TODO Stacktracing der Fehlermeldungen im Wiki anzeigen, nicht nur die Fehlermeldung (für alle Fehler)
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
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				
				for (KnowWEArticle changedArticle: refManager.getChangedArticles()) {
					String changedArticleID = changedArticle.getTitle();
					KnowWEArticleManager knowWEManager = KnowWEEnvironment.getInstance().getArticleManager(parameters.getWeb());

					int versionBefore = we.getPage(changedArticleID).getVersion();
					String textBefore = "textBefore";
					try {
						textBefore = we.getPageManager().getPageText(changedArticleID, versionBefore);
					} catch (ProviderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//FIXME der Artikel-KDOM wird gespeichert, aber der Artikel-Quelltext nicht ?!  Ist dieser Save hier nötig?
					refManager.saveUpdatedArticle(changedArticle);
					KnowWEArticle consinstentChangedArticle = refManager.getArticle(changedArticleID);
					knowWEManager.replaceKDOMNode(parameters, changedArticleID, changedArticleID, consinstentChangedArticle.getSection().getOriginalText());
					int versionAfter = we.getPage(changedArticleID).getVersion();
					String textAfter = "textAfter";
					try {
						textAfter = we.getPageManager().getPageText(changedArticleID, versionAfter);
					} catch (ProviderException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					html.append("<br />Der folgende Artikel hat sich geändert: " + changedArticleID + " von Version " +
							versionBefore + " zu Version " + versionAfter + ".<br />");
					diff_match_patch dmp = new diff_match_patch();
					LinkedList<Diff> diffs = dmp.diff_main(textBefore, textAfter);
					dmp.diff_cleanupSemantic(diffs);
					html.append(dmp.diff_prettyHtml(diffs) + "<br />");
				}
				html.append("<br />Refactorings abgeschlossen.<br />");
				
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
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
			for (KnowWEArticle art: refManager.getChangedArticles()) {
				String st = art.getTitle();
				WikiPage page = we.getPage(st);
				WikiContext wc = new WikiContext(we, page);
				// TODO test, ob changedWikiPages.get(st)[0] == we.getPage(pageName).getVersion()
				try {
					we.saveText(wc, we.getPageManager().getPageText(st, we.getPage(st).getVersion() - 1));
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

	// GROOVY-METHODEN
	
	public void addRulesText(StringBuffer sb, Section<RulesSectionContent> rulesSectionContent) {
		replaceSection(rulesSectionContent, rulesSectionContent.getOriginalText() + sb.toString());
	}

	// TODO wenn es noch keine RulesSection gibt, dann muss die noch gebaut werden
	public Section<RulesSectionContent> findRulesSectionContent(Section<?> knowledgeSection) {
		// FIXME temporärer hack reloaded
		KnowWEArticle article = refManager.getArticle(knowledgeSection.getArticle().getTitle());
		
		Section<RulesSectionContent> rulesSectionContent = article.getSection().findSuccessor(RulesSectionContent.class);
		return rulesSectionContent;
	}
	
	public Section<CoveringListContent> findCoveringListContent(String articleID) {
		return findCoveringListContent(refManager.getArticle(articleID).getSection());
	}
	
	public List<Section<XCList>> findXCLs (Section<CoveringListContent> content) {
		List<Section<XCList>> list = new LinkedList<Section<XCList>>();
		content.findSuccessorsOfType(XCList.class, list);
		return list;
	}

	public void setMergedCoveringListContent(Section<CoveringListContent> section , Map<String,Set<String>> map) {
		StringBuffer sb = new StringBuffer();
		for(String s: map.keySet()) {
			sb.append("\n" + s +"{\n");
			for(String finding: map.get(s)) {
				sb.append("\t" + finding + ",\n");
			}
			sb.append("}");
		}
		replaceSection(section, sb.toString());
	}

	public Section<CoveringListContent> findCoveringListContent(Section<?> knowledgeSection) {
		// FIXME temporärer hack reloaded
		KnowWEArticle article = refManager.getArticle(knowledgeSection.getArticle().getTitle());
		
		Section<CoveringListContent> coveringListContent = article.getSection().findSuccessor(CoveringListContent.class);
		if (coveringListContent == null) {
			StringBuilder newArticleText = new StringBuilder(article.getSection().getOriginalText());
			//FIXME ganz böse - der Artikel wird gespeichert in einer Zwischenversion, nur um an die SetCoveringList-section ranzukommen...
			refManager.replaceKDOMNode(article.getTitle(), article.getTitle(), newArticleText.append("\r\n<SetCoveringList-section>\r\n" + 
					"\r\n" + 
					"</SetCoveringList-section>\r\n").toString());
			Section<CoveringListContent> coveringListContentNew = refManager.getArticle(article.getTitle())
				.getSection().findSuccessor(CoveringListContent.class);
			return coveringListContentNew;
		}
		return coveringListContent;
	}

	public void deleteXCList(Section<?> knowledgeSection) {
		replaceSection(knowledgeSection, "\n");
	}

	// TODO für dieses Refactoring sollten die Werte besser berechnet werden - das Refactoring muss auf die komplette Syntax von XCL-Listen
	// ausgedehnt werden
	public void createRulesText(Section<Finding> sec, String solutionID, StringBuffer sb) {
		sb.append("\nIF " + sec.getOriginalText());
		sb.append("\n    THEN ");
		sb.append(solutionID + " = P7");
	}

	public String findSolutionID(Section<?> knowledgeSection) {
		Section<SolutionID> solutionID = knowledgeSection.findSuccessor(SolutionID.class);
		return solutionID.getOriginalText();
	}

	public List<Section<Finding>> findFindings(Section<?> knowledgeSection) {
		List<Section<Finding>> findings = new ArrayList<Section<Finding>>();
		knowledgeSection.findSuccessorsOfType(Finding.class, findings);
		return findings;
	}

	public Section<?> findXCList() {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie die zu transformierende Überdeckungsliste aus:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>XCList</label>"
						+ "<select name='selectXCList' class='refactoring'>");
				for(Iterator<KnowWEArticle> it = refManager.getArticleIterator(); it.hasNext();) {
					KnowWEArticle article = refManager.getArticle(it.next().getTitle());
					Section<?> articleSection = article.getSection();
					List<Section<XCList>> xclists = new ArrayList<Section<XCList>>();
					articleSection.findSuccessorsOfType(XCList.class, xclists);
					for (Section<?> xclist : xclists) {
						html.append("<option value='" + xclist.getId() + "'>Seite: " + article.getTitle() + " - XCList: " 
								+ xclist.findSuccessor(SolutionID.class).getOriginalText() + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		Section<?> knowledge = refManager.findNode(gsonFormMap.get("selectXCList")[0]);
		return knowledge;
	}
	
	public Class<? extends KnowWEObjectType> findRenamingType() {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
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
		String clazzString = gsonFormMap.get("selectRenamingType")[0];
		Class<? extends KnowWEObjectType> clazz = getTypeFromString(clazzString);
		return clazz;
	}

	public Class<? extends KnowWEObjectType> getTypeFromString(String clazzString) {
		Class<? extends KnowWEObjectType> clazz = null;
		if(clazzString.equals("KnowWEArticle")) {
			clazz = KnowWEArticle.class;
		} else if(clazzString.equals("QuestionnaireID")) {
			clazz = QuestionnaireID.class;
		} else if(clazzString.equals("QuestionID")) {
			clazz = QuestionID.class;
		} else if(clazzString.equals("QuestionTreeAnswerID")) {
			clazz = QuestionTreeAnswerID.class;
		} else if(clazzString.equals("SolutionID")) {
			clazz = SolutionID.class;
		}
		return clazz;
	}
	
	// TODO bereits hier alternative Typen berücksichtigen (QClassID, QuestionnaireID)
	public <T extends KnowWEObjectType> String findObjectID(final Class<T> clazz) {
		//TODO mehrfache Einträge für ein Element sollten vermieden werden
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Namen des Objekts mit dem Typ <strong>" + clazz.getName() + "</strong>:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<select name='selectObjectID' class='refactoring'>");
				for(Iterator<KnowWEArticle> it = refManager.getArticleIterator(); it.hasNext();) {
					KnowWEArticle article = refManager.getArticle(it.next().getTitle());
					Section<?> articleSection = article.getSection();
					List<Section<T>> objects = new ArrayList<Section<T>>();
					articleSection.findSuccessorsOfType(clazz, objects);
					for (Section<?> object : objects) {
						String name = (clazz == KnowWEArticle.class) ? object.getId() : object.getOriginalText();
						String question = (clazz == QuestionTreeAnswerID.class) ? " - Frage: " + findQuestion(object).getOriginalText() : "";
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
	
	// TODO bereits hier alternative Typen berücksichtigen (QClassID, QuestionnaireID)
	public <T extends KnowWEObjectType> String[] findObjectIDs(final Class<T> clazz) {
		//TODO mehrfache Einträge für ein Element sollten vermieden werden
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
				StringBuffer html = new StringBuffer();
				html.append("<fieldset><div class='left'>"
						+ "<p>Wählen Sie den Namen des Objekts mit dem Typ <strong>" + clazz.getName() + "</strong>:</p></div>"
						+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Objektname</label>"
						+ "<select multiple size='" + refManager.getArticles().size() + "' name='selectObjectID' class='refactoring'>");
				for(Iterator<KnowWEArticle> it = refManager.getArticleIterator(); it.hasNext();) {
					KnowWEArticle article = refManager.getArticle(it.next().getTitle());
					Section<?> articleSection = article.getSection();
					List<Section<T>> objects = new ArrayList<Section<T>>();
					articleSection.findSuccessorsOfType(clazz, objects);
					for (Section<?> object : objects) {
						String name = (clazz == KnowWEArticle.class) ? object.getId() : object.getOriginalText();
						String question = (clazz == QuestionTreeAnswerID.class) ? " - Frage: " + findQuestion(object).getOriginalText() : "";
						html.append("<option value='" + object.getId() + "'>Seite: " + article.getTitle() + question + " - Objekt: " 
								+ name + "</option>");
					}
				}
				html.append("</select></div><div>"
						+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
				return html.toString();
			}
		});
		String[] objectIDs = gsonFormMap.get("selectObjectID");
		return objectIDs;
	}
	
	private Section<QuestionID> findQuestion(Section<?> answerSection) {
		// TODO verbessern
		return answerSection.getFather().getFather().getFather().getFather().getFather().findChildOfType(DashTreeElement.class).findSuccessor(QuestionID.class);
	}
	private Section<QuestionTreeAnswerID> findAnswer(Section<?> diagnosisSection) {
		// TODO verbessern
		return diagnosisSection.getFather().getFather().getFather().getFather().getFather().findChildOfType(DashTreeElement.class).findSuccessor(QuestionTreeAnswerID.class);
	}
	
	private Section<QuestionTreeAnswerID> findAnswerIndicatingQuestion(Section <?> questionSection) {
		// TODO verbessern
		return questionSection.getFather().getFather().getFather().getFather().getFather().findChildOfType(DashTreeElement.class).findSuccessor(QuestionTreeAnswerID.class);
	}
	
	public String findNewName() {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameters) {
				KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
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
	
	
	public <T extends KnowWEObjectType> List<Section<? extends KnowWEObjectType>> findRenamingList(Class<T> clazz, String objectID) {
		return findRenamingList(clazz, objectID, null);
	}
	
	// TODO den Algorithmus verständlicher gestalten
	public <T extends KnowWEObjectType> List<Section<? extends KnowWEObjectType>> findRenamingList(Class<T> clazz, String objectID, String newName) {
		List<Section<? extends KnowWEObjectType>> fullList = new ArrayList<Section<? extends KnowWEObjectType>>();
		List<Section<? extends KnowWEObjectType>> filteredList = new ArrayList<Section<? extends KnowWEObjectType>>();
		if (clazz == QuestionTreeAnswerID.class) {
			Section<QuestionID> question = findQuestion(refManager.findNode(objectID));
			List<Section<QuestionTreeAnswerID>> answers = new LinkedList<Section<QuestionTreeAnswerID>>();
			// TODO verbessern
			question.getFather().getFather().getFather().getFather().findSuccessorsOfType(QuestionTreeAnswerID.class, 5 , answers);
			fullList.addAll(answers);
			// hole alle FindingQuestion's welche den gleichen getOriginalText() haben wie die Question, zu welcher die QuestionTreeAnswerID
			// gehört
			List<Section<? extends KnowWEObjectType>> findingQuestions = findRenamingList(FindingQuestion.class, question.getId());
			// bestimme dafür die passenden Antworten
			for (Section<? extends KnowWEObjectType> questionSection : findingQuestions) {
				Section<? extends KnowWEObjectType> answer = questionSection.getFather().findSuccessor(FindingAnswer.class);
				fullList.add(answer);
			}
		} else {
			for (Iterator<KnowWEArticle> it = refManager.getArticleIterator(); it.hasNext();) {
				KnowWEArticle article = refManager.getArticle(it.next().getTitle());
				Section<?> articleSection = article.getSection();
				List<Section<T>> objects = new ArrayList<Section<T>>();
				articleSection.findSuccessorsOfType(clazz, objects);
				fullList.addAll(objects);
				if (clazz == QuestionnaireID.class || clazz == QuestionID.class) {
					List<Section<QClassID>> objects2 = new ArrayList<Section<QClassID>>();
					articleSection.findSuccessorsOfType(QClassID.class, objects2);
					fullList.addAll(objects2);
				}
				if (clazz == QuestionID.class) {
					List<Section<FindingQuestion>> objects2 = new ArrayList<Section<FindingQuestion>>();
					articleSection.findSuccessorsOfType(FindingQuestion.class, objects2);
					fullList.addAll(objects2);
				}
				// TODO Solutions müssen ebenfalls behandelt werden!
			}
		}
		for (Section<? extends KnowWEObjectType> object : fullList) {
			String name = (newName != null) ? newName : refManager.findNode(objectID).getOriginalText();
			if (object.getOriginalText().equals(name)) {
				filteredList.add(object);
			}
		}
		return filteredList;
	}

	public <T extends KnowWEObjectType> void renameElement(Section<? extends KnowWEObjectType> section, String newName, Class<T> clazz){
		// TODO Report und Undo von umbenannten Wiki-Seiten ermöglichen
		if (clazz == KnowWEArticle.class) {
			PageRenamer pr = new PageRenamer();
			try {
				WikiPage page = we.getPage(section.getId());
				WikiContext wc = new WikiContext(we, page);
				pr.renamePage(wc, section.getId(), newName, true);
			} catch (WikiException e) {
				// TODO wird z.B. geworfen wenn der Seitenname bereits vorhanden ist
				e.printStackTrace();
			}
			// include-Referenzen werden umbenannt
			for (Iterator<KnowWEArticle> it = refManager.getArticleIterator(); it.hasNext();) {
				KnowWEArticle art = it.next();
				List<Section<Include>> includes = new LinkedList<Section<Include>>();
				art.getSection().findSuccessorsOfType(Include.class, includes);
				for(Section<Include> inc : includes) {
					Map<String,String> attributes = AbstractXMLObjectType.getAttributeMapFor(inc);
					String src = attributes.get("src");
					String articleName = refManager.getArticleName(src);
					String newSrc = src.replaceFirst(articleName, newName);
					StringBuffer replacement = new StringBuffer( "<include src=\"" + newSrc + "\" />");
					replaceSection(inc, replacement.toString());
				}
			}
		} else {
			replaceSection(section, newName);
		}
	}
	
	// TODO die Refactoring-Session muss noch anständig terminiert werden (dies gilt auch für Sessions, die durch eine warning oder einen error
	// unterbrochen wurden
	public void printExistingElements(final List<Section<? extends KnowWEObjectType>> existingElements) {
		performNextAction(new AbstractKnowWEAction() {
			@Override
			public String perform(KnowWEParameterMap parameterMap) {
				StringBuffer html = new StringBuffer();
				html.append("<br />Das Refactoring kann nicht durchgeführt werden, da es Konflikte mit folgenden Sektionen gibt:<br /><br />");
				for (Section<? extends KnowWEObjectType> section: existingElements) {
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
		Section<?> articleSection = refManager.findNode(pageName);
		List<Section<AnonymousType>> found = new ArrayList<Section<AnonymousType>>();
		// TODO es muss noch überprüft werden, ob AnonymousType.getName().equals("SetValueArgument") gilt.
		articleSection.findSuccessorsOfType(AnonymousType.class, found);
		for (Section<AnonymousType> atSection: found) {
			if (atSection.getOriginalText().equals(value)) {
				Section<QuestionID> foundSolution = atSection.getFather().findSuccessor(QuestionID.class);
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
		// FIXME temporärer hack reloaded
		KnowWEArticle article = refManager.getArticle(solution.getArticle().getTitle());
		
		List<Section<QuestionID>> list = new LinkedList<Section<QuestionID>>();
		article.getSection().findSuccessorsOfType(QuestionID.class, list);
		for(Section<QuestionID> sqid: list) {
			if (sqid.getOriginalText().equals(solution.getOriginalText())) {
				replaceSection(sqid.getFather().getFather().getFather(), "");
			}
		}
	}
	
	public void deleteComments (String sectionID) {
		Section<?> section = refManager.findNode(sectionID);
		List<Section<CommentLineType>> list = new LinkedList<Section<CommentLineType>>();
		section.getArticle().getSection().findSuccessorsOfType(CommentLineType.class, list);
		for(Section<CommentLineType> commentLine: list) {
			replaceSection(commentLine, "");
		}
	}
	
	public void transformToQuestionTree (String objectID) {
		Section<?> section = refManager.findNode(objectID);
		Section<QuestionsSection> qs = section.findSuccessor(QuestionsSection.class);
		Section<QuestionsSectionContent> qsc = qs.findSuccessor(QuestionsSectionContent.class);
		replaceSection(qs,"\n%%QuestionTree\n" + qsc.getOriginalText() + "\n%\n"); 
	}
	
	public void transformToQuestionsSection (String objectID) {
		Section<?> section = refManager.findNode(objectID);
		Section<QuestionTreeRootType> qtrt = section.findSuccessor(QuestionTreeRootType.class);
		Section<ContentType> ct = qtrt.findSuccessor(ContentType.class);
		replaceSection(qtrt,"\n<Questions-section>\n" + ct.getOriginalText() + "\n</Questions-section>\n"); 
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
		// TODO momentan werden nur Refactorings betrachtet, die sich auf dieser Seite befinden - Integration von built-in Refactorings nötig!
		Section<?> section = refManager.getArticle(parameters.getTopic()).getSection();
		// siehe RefactoringTagHandler.java
		Section<?> refactoring = section.findChild(gsonFormMap.get("selectRefactoring")[0]);
		return refactoring;
	}
	
	private void replaceSection(Section<?> section, String newText) {
		KnowWEArticle article = section.getArticle(); 
		String topic = article.getTitle();
		refManager.replaceKDOMNodeWithoutSave(topic, section.getId(), newText);
	}
}