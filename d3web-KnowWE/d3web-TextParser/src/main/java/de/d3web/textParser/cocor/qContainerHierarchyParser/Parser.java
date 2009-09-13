/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.textParser.cocor.qContainerHierarchyParser;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.report.Message;

public class Parser {
	static final int _EOF = 0;
	static final int _Declaration = 1;
	static final int _ID = 2;
	static final int _StartQContainerNumberInBrackets = 3;
	static final int maxT = 7;
	static final int _comment = 8;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	Scanner scanner;
	Errors errors;

	private Map<Integer, QContainer> ht = new Hashtable<Integer, QContainer>();
	/**
	 * die aktuelle Tiefe des Baumes (Anzahl der Anführungszeichen)
	 */
	private int levelPeak = -1;
	/**
	 * der Token ds letzten gelesenen Dashes
	 */
	private Token lastDashToken;
	private KnowledgeBase kb;
	private KnowledgeBaseManagement kbm;
	/**
	 * Update-Modus.
	 * true: hierbei können nur Eintrage hinzugefügt werden
	 * false: Einträge können überschrieben werden (ist nur bei einer leeren KB von Bedeutung) 
	 */
	private boolean addMode;
	/**
	 * Anzahl der hinzugefügten Frageklassen (alle)
	 */
	private int numberOfInserts = 0;
    /**
     * Anzahl der hinzugefügten Linked-Unter-Frageklassen
     */
    private int numberOfInsertsAsLinkedChild = 0;
    /**
     * Anzahl der hinzugefügten Unter-Diagnosen
     */
    private int numberOfInsertsAsChild = 0;
    /**
     * Toplevel-Modus, wird angewendet bei einem Update, um Frageklassen auf Level 0 hinzuzufügen
     */
    private boolean toplevelMode = false;
    /**
     * Gibt an, ob die Frageklassen, mit der der Fragebogen gestartet wird, angegeben wurden oder nicht
     */
    private boolean setStartQContainers = false;
    /**
     * dient zur Speicherung der Start-Frageklassen
     */
    private TreeMap<Integer, QContainer> startQContainerTreeMap = new TreeMap<Integer, QContainer>();
    /**
     * aktuelle Frageklasse
     */
    private QContainer currentQContainer = null;
    /**
     * Resource-Bundle welches zum Parsen von Meldungen benötigt wird
     */
    private static final ResourceBundle rb = ResourceBundle.getBundle("properties.textParser");
	/**
	 * Überprüft die Zulässigkeit des Levels, auf dem die Frageklasse eingefügt werden soll.
	 * @param level das level der einzutragenden Frageklasse
	 * @param qContainerValue value der einzutragenden Frageklasse
	 * @return true: level erlaubt, false: level falsch gewählt, Fehlermeldung wird geworfen
	 */
	private boolean checkAndSetAllowedLevel(int level, String qContainerValue) {
		boolean returnValue = true;
		if (level > levelPeak+1) {
			int levelDifference = level - (levelPeak+1);
			createMessage(
                    lastDashToken.line, lastDashToken.col, "parser.qContainerHierarchy.error.level",
                    Message.ERROR, qContainerValue, levelDifference);
			returnValue = false;
		} else {
			levelPeak = level;
		}
		return returnValue;
	}
    
    /**
     * Erstellt eine Meldung.
     * @param row Zeile in der Eingabe-Datei
     * @param column Spalte  in der Eingabe-Datei
     * @param key Schlüssel der Meldung
     * @param messageType Message.ERROR, Message.WARNING oder MESSAGE.Note
     * @param values Parameter, welche in den Text der Meldung mit aufgenommen werden sollen
     */
    private void createMessage(
            int row, int column, String key, String messageType, Object ... values) {
        String result = rb.getString("parser.error.unknownError") + ": "+key;
        try {
            result = MessageFormat.format(
                rb.getString(key), values);
        }
        catch (MissingResourceException e) {}
        errors.Error(row, column, result, messageType);        
    }
	/**
	 * Überprüft, ob linkedParent ein Vorgäner von possibleChild ist.
	 * @param linkedParent Vorgänger
	 * @param possibleChild mögliches Kind
	 * @return true: linkedParent ist Vorgänger, false: linkedParent ist nicht Vorgänger
	 */
	private boolean isAncestor(QContainer linkedParent, QContainer possibleChild) {
        boolean isAncestor = false;        
        if(linkedParent != null && linkedParent == possibleChild) return true;
        else {
            List<QContainer> ancestors = (List<QContainer>) linkedParent.getParents();
            for(QContainer nextParent: ancestors) {
                isAncestor = isAncestor(nextParent, possibleChild);
                if(isAncestor) return isAncestor;
            }
        }
        return false;
    }
    
    /**
     * Liefert die Meldung für erfolgreiches Parsen
     * @return Message für erfolgreiches Parsen
     */
    public Message getSuccessNote() {
        Object[] values = {numberOfInserts,
                (numberOfInserts - (numberOfInsertsAsChild + numberOfInsertsAsLinkedChild)),
                numberOfInsertsAsChild, numberOfInsertsAsLinkedChild, addMode};
        String result = rb.getString("parser.error.unknownError");
        try {
            result = MessageFormat.format(
                rb.getString("parser.qContainerHierarchy.success"), values);
        }
        catch (MissingResourceException e) {}
        return new Message(Message.NOTE, result, "", 0,0,"");
    }
    
    /**
     * Meldung, die Informationen über die Start-Frageklassen enthält
     * @return die Meldung
     */
    public Message getInitNotes() {
        String result = null;
        Object[] values = null;
        if(addMode && setStartQContainers)
            result = MessageFormat.format(rb.getString("parser.qContainerHierarchy.newStartQContainers"), values);
        
        if(addMode && !setStartQContainers) 
            result = MessageFormat.format(rb.getString("parser.qContainerHierarchy.oldStartQContainers"), values);
        
        return new Message(Message.NOTE, result, "", 0,0,"");
    }
	
	


	// COCO begin change
	public Parser(Scanner scanner, KnowledgeBase kb, boolean addMode) {
		this.scanner = scanner;
		errors = new Errors(this.scanner.getFilename());
        this.kb = kb;
        this.kbm = KnowledgeBaseManagement.createInstance(kb);
        this.addMode = addMode;
	}
	// COCO end change

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		// COCO begin change
		if (errDist >= minErrDist) errors.Error(t.line, t.col, msg, Message.ERROR);
		// COCO end change
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) { ++errDist; break; }

			if (la.kind == 8) {
			}
			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		boolean[] s = new boolean[maxT+1];
		if (la.kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			for (int i=0; i <= maxT; i++) {
				s[i] = set[syFol][i] || set[repFol][i] || set[0][i];
			}
			SynErr(n);
			while (!s[la.kind]) Get();
			return StartOf(syFol);
		}
	}
	
	void QContainerHierarchy() {
		QContainer rootQContainer = (QContainer)kb.getRootQASet();
		ht.put(-1, rootQContainer);
		
		while (la.kind == 4 || la.kind == 5) {
			if (la.kind == 4) {
				Get();
			} else {
				Get();
			}
		}
		QContainer();
		
		while (la.kind == 1 || la.kind == 6) {
			QContainer();
			
		}
		
	}

	void QContainer() {
		int qContainerLevel = 0;
		
		while (la.kind == 6) {
			Get();
			qContainerLevel = qContainerLevel + 1;
			
		}
		lastDashToken = t;
		
		Expect(1);
		String qContainerValue = t.val;
		qContainerValue = qContainerValue.trim();
		if (qContainerValue.charAt(0) == '\"'
				&& qContainerValue.charAt(qContainerValue.length() - 1) == '\"') {
			qContainerValue = qContainerValue.substring(1, qContainerValue.length() - 1);
		}
																		// semantisches Parsing der Zeile!
		if (addMode == true && qContainerLevel == 0) {
			if (qContainerValue.equals(rb
					.getString("parser.qContainerHierarchy.toplevel"))) {
				ht.put(0, (QContainer)kb.getRootQASet());
				toplevelMode = true;
			} else {
				toplevelMode = false;
				QContainer anchorQContainer = kbm.findQContainer(qContainerValue);
				currentQContainer = anchorQContainer;
				if (anchorQContainer != null) {
					ht.put(qContainerLevel, anchorQContainer);
				} else {
					createMessage(t.line, t.col,
							"parser.qContainerHierarchy.error.anchor",
							Message.ERROR, qContainerValue);
				}
			}
		}
		if (checkAndSetAllowedLevel(qContainerLevel, qContainerValue)
				&& (qContainerLevel != 0 || addMode == false)) {
			QContainer ancestor = ht.get(qContainerLevel - 1);
			QContainer stillContainedQContainer = kbm
					.findQContainer(qContainerValue);
			currentQContainer = stillContainedQContainer;
			if (stillContainedQContainer == null) {
				QContainer child = kbm.createQContainer(qContainerValue, ancestor);
				currentQContainer = child;
				ht.put(qContainerLevel, child);
				numberOfInserts++;
				// direkte Toplevel-Kinder bei update nicht mitrechnen bei
				// numberOfInsertsAsChild, denn sie sind ja eigentlich Toplevel-QContainer
				if (((qContainerLevel > 1) && toplevelMode)
						|| ((qContainerLevel > 0) && !toplevelMode))
					numberOfInsertsAsChild++;
			} else if (!isAncestor(ancestor, stillContainedQContainer)) {
				if (!stillContainedQContainer.getParents().contains(ancestor)) {
					stillContainedQContainer.addLinkedParent(ancestor);
					numberOfInserts++;
					numberOfInsertsAsLinkedChild++;
				} else {
					createMessage(t.line, t.col, "parser.qContainerHierarchy.error.doubleInsertion", Message.ERROR, qContainerValue);
				}
				ht.put(qContainerLevel, stillContainedQContainer);
			} else {
				createMessage(t.line, t.col,
						"parser.qContainerHierarchy.error.loop", Message.ERROR, qContainerValue);
			}
		}
		
		if (la.kind == 3) {
			Get();
			int startQContainerPosition = -1;
			String tValueWithoutBrackets = t.val.substring(1, t.val.length()-1);
			try {
				startQContainerPosition = Integer.valueOf(tValueWithoutBrackets);
			} catch(NumberFormatException e) {
				// COCO: falsches format
				createMessage(t.line, t.col,
							"parser.qContainerHierarchy.error.numberFormatException", Message.ERROR, qContainerValue, tValueWithoutBrackets);
				
			}
			setStartQContainers = true;
			if (!startQContainerTreeMap.containsKey(startQContainerPosition)) {
				startQContainerTreeMap.put(startQContainerPosition, currentQContainer);
			} else if (currentQContainer != startQContainerTreeMap.get(startQContainerPosition)) {
				String alreadyContainedQContainer = startQContainerTreeMap.get(startQContainerPosition).toString();
				createMessage(t.line, t.col, "parser.qContainerHierarchy.error.multipleQCStartPosition", Message.ERROR, qContainerValue, alreadyContainedQContainer, tValueWithoutBrackets);
			} else {
				createMessage(t.line, t.col, "parser.qContainerHierarchy.error.sameQCStartPosition", Message.ERROR, qContainerValue, tValueWithoutBrackets);
			}
			
			
			
		}
		if (la.kind == 2) {
			Get();
			String id = t.val.substring(1, t.val.length());
			id = id.trim();
			QContainer qcSearch = kbm.findQContainer(id);
			if (qcSearch == null) {
				boolean idSet = this.kbm.changeID(currentQContainer, id);
				if (!idSet) {
					createMessage(t.line, t.col, "setting custom ID '" + id + "' for object "
									+ currentQContainer.getText() + " failed", Message.ERROR, qContainerValue, "");
				}
				
				//THIS IS A HOTFIX
				// since the objects are held in a SORTED List (by ID)
				// one may not change the ID afterwards
				// this hotfix creates and removes a dummy object
				// to force a sort
//				QContainer dummy = kbm.createQContainer("dummy", kbm.getKnowledgeBase().getRootQASet());
//				
//				try {
//					kbm.getKnowledgeBase().remove(dummy);
//					kbm.getKnowledgeBase().getRootQASet().removeChild(dummy);
//				} catch (IllegalAccessException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				//END HOTFIX
			} else if (qcSearch != currentQContainer) {
				createMessage(t.line, t.col, "parser.qContainerHierarchy.error.idAlreadyExists", Message.ERROR, currentQContainer.getText(), id);
			}
			
		}
		while (!(la.kind == 0 || la.kind == 4 || la.kind == 5)) {SynErr(8); Get();}
		if (la.kind == 4 || la.kind == 5) {
			if (la.kind == 4) {
				Get();
			} else {
				Get();
			}
			while (la.kind == 4 || la.kind == 5) {
				if (la.kind == 4) {
					Get();
				} else {
					Get();
				}
			}
		} else if (la.kind == 0) {
			Get();
		} else SynErr(9);
		if (setStartQContainers) {
		Iterator<QContainer> iterator = (startQContainerTreeMap).values().iterator();
		List<QContainer> startQContainerList = new LinkedList<QContainer>();
		while(iterator.hasNext()) startQContainerList.add(iterator.next());
		kb.setInitQuestions(startQContainerList);
		}
		
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		QContainerHierarchy();

		Expect(0);
	}

	private boolean[][] set = {
		{T,x,x,x, T,T,x,x, x}

	};
	    
    // COCO begin add
    public List<Message> getErrorMessages() {
        return errors.getMessages();
    }
	// COCO end add
} // end Parser


class Errors {
	public int count = 0;
	public String errMsgFormat = "-- line {0} col {1}: {2}";
	
    // COCO begin add
    private String filename = new String();
    private List<Message> messages = new LinkedList<Message>();
	// COCO end add

	// COCO begin add
	public Errors(String filename) {
		this.filename = filename;
	}
	// COCO end add

    // COCO begin change
	// originally it's called printMsg() )
	private void addMsg(int line, int column, String msg, String messageType) {
        // COCO: es fehlt noch die Angabe der Spaltennnummer und des Filenamen 
        Message message = new Message(messageType, msg, this.filename, line, column, "");
        messages.add(message);
	}
	// COCO end change
	
	public void SynErr (int line, int col, int n) {
			String s;
			switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "Declaration expected"; break;
			case 2: s = "ID expected"; break;
			case 3: s = "StartQContainerNumberInBrackets expected"; break;
			case 4: s = "\"\\n\" expected"; break;
			case 5: s = "\"\\r\" expected"; break;
			case 6: s = "\"-\" expected"; break;
			case 7: s = "??? expected"; break;
			case 8: s = "this symbol not expected in QContainer"; break;
			case 9: s = "invalid QContainer"; break;
				default: s = "error " + n; break;
			}

			// COCO begin change
			ResourceBundle rb = ResourceBundle.getBundle("properties.textParser");
			String result = rb.getString("parser.error.unknownError") + ": "+"parser.error.wrappedError";
	        try {
	            result = MessageFormat.format(
	                rb.getString("parser.error.wrappedError"), s);
	        }
	        catch (MissingResourceException e) {}
	        addMsg(line, col, result, Message.ERROR);
			// COCO end change

			count++;
	}
	
	// COCO delete
	// hier wurde die Funktion SemErr() gelöscht, da sie nicht benötigt wird.

	// COCO begin change
	public void Error (int line, int col, String s, String messageType) {	
       
        // COCO: not in Coco
		addMsg(line, col, s, messageType);
		count++;
	}
	// COCO end change
	
	// COCO delete
	// hier wurde die Funktion Exception() gelöscht, da sie nicht benötigt wird.
	
    // COCO begin add
    public List<Message> getMessages() {
        return messages;      
    }
	// COCO end add

} // Errors

