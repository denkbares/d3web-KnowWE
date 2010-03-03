package de.d3web.we.refactoring.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

import org.ceryle.xml.XHTML;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;
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
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;
import de.d3web.we.kdom.xcl.XCList;
import de.d3web.we.refactoring.session.RefactoringSession;

public class RefactoringSessionDialog extends RefactoringSession {

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
	private KnowWEAction nextAction;
	protected KnowWEParameterMap parameters;
	protected Map<String,String[]> gsonFormMap;
	
	public RefactoringSessionDialog() {
		we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
	}


	@Override
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
	

	@Override
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


	@Override
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
						String question = (clazz == QuestionTreeAnswerID.class)
							? " - Frage: " + findDashTreeFather(object, QuestionID.class).getOriginalText()
							: "";
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


	@Override
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
						String question = (clazz == QuestionTreeAnswerID.class)
							? " - Frage: " + findDashTreeFather(object, QuestionID.class).getOriginalText()
							: "";
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


	@Override
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
		return newName.trim();
	}


	@Override
	protected String findRefactoringSourceCode() {
		// TODO momentan werden nur Refactorings betrachtet, die sich auf dieser Seite befinden - Integration von built-in Refactorings nötig!
		Section<?> section = refManager.getArticle(parameters.getTopic()).getSection();
		// siehe RefactoringTagHandler.java
		Section<?> refactoring = section.findChild(gsonFormMap.get("selectRefactoring")[0]);
		return refactoring.getOriginalText();
	}


	@Override
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


	@Override
	protected void error(String message) {
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


	@Override
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


	public KnowWEAction getNextAction() {
		return nextAction;
	}


	protected void performNextAction(KnowWEAction action) {
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


	@Override
	protected void saveAndFinish() {
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
					// der Artikel wird sicherheitshalber in einen konsistenten Zustand gebracht TODO: siehe saveAndFinish() in RefactoringSessionTestImpl
					// wenn neuer Artikel angelegt werden soll
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

	public void setParameters(KnowWEParameterMap parameters, Map<String, String[]> gsonFormMap) {
		this.parameters = parameters;
		this.gsonFormMap = gsonFormMap;
		String web = parameters.getWeb();
		super.setRefManager(web);
	}

}
