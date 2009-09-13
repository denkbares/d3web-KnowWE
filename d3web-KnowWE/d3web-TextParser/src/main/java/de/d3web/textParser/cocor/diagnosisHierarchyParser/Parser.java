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

package de.d3web.textParser.cocor.diagnosisHierarchyParser;

import java.util.*;
import java.text.MessageFormat;
import de.d3web.kernel.domainModel.*;
import de.d3web.report.Message;
import de.d3web.kernel.supportknowledge.*;

public class Parser {
	static final int _EOF = 0;
	static final int _Declaration = 1;
	static final int _ID = 2;
	static final int _BracketOpen = 3;
	static final int _BracketClose = 4;
	static final int _Dash = 5;
	static final int _DiagnosisID = 6;
	static final int maxT = 23;
	static final int _comment = 24;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	Scanner scanner;
	Errors errors;

	private Map<Integer, Diagnosis> ht = new Hashtable<Integer, Diagnosis>();
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
     * Anzahl der hinzugefügten Diagnosen (alle)
     */
    private int numberOfInserts = 0;
    /**
     * Anzahl der hinzugefügten Linked-Unter-Diagnosen
     */
    private int numberOfInsertsAsLinkedChild = 0;
    /**
     * Anzahl der hinzugefügten Unter-Diagnosen
     */
    private int numberOfInsertsAsChild = 0;
    /**
     * Toplevel-Modus, wird angewendet bei einem Update, um Diagnosen auf Level 0 hinzuzufügen
     */
    private boolean toplevelMode = false;
    /**
     * aktuelle Diagnose
     */
    private Diagnosis currentDiagnosis = null;
    /**
     * Resource-Bundle welches zum Parsen von Meldungen benötigt wird
     */
    private static final ResourceBundle rb = ResourceBundle.getBundle("properties.textParser");
    
    
    //Begin Change Extension
    private String content = ""; 
    private NamedObject namedObject = null; 
    private HashMap<String,ArrayList<Object>> mapNamedObjects = new HashMap<String,ArrayList<Object>>(); 
    private ArrayList<String> abbr = new ArrayList<String>(); 
    private ArrayList<String> allowedNames = new ArrayList<String>(); 
    private ArrayList<NamedObject> savedLinks = new ArrayList<NamedObject>(); 
    //End Change Extension
    
    
    /**
     * Überprüft die Zulässigkeit des Levels, auf dem die Diagnose eingefügt werden soll.
     * @param level das level der einzutragenden Diagnose
     * @param diagnosisValue value der einzutragenden Diagnose
     * @return true: level erlaubt, false: level falsch gewählt, Fehlermeldung wird geworfen
     */
    private boolean checkAndSetAllowedLevel(int level, String diagnosisValue) {
        boolean returnValue = true;
        if (level > levelPeak+1) {
            int levelDifference = level - (levelPeak+1);
            createMessage(
                    lastDashToken.line, lastDashToken.col, "parser.diagnosisHierarchy.error.level",
                    Message.ERROR, diagnosisValue, levelDifference); //System.out.println("Semantic Level Error recognized in line:" + lastDashToken.line + " column:" + lastDashToken.col);
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
    private boolean isAncestor(Diagnosis linkedParent, Diagnosis possibleChild) {
        boolean isAncestor = false;        
        if(linkedParent != null && linkedParent == possibleChild) return true;
        else {
            List<Diagnosis> ancestors = (List<Diagnosis>) linkedParent.getParents();
            for(Diagnosis nextParent: ancestors) {
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
        // COCO: ist in Wirklichkeit kein Error, bitte umbenennen!
        Object[] values = {numberOfInserts,
                (numberOfInserts - (numberOfInsertsAsChild + numberOfInsertsAsLinkedChild)),
                numberOfInsertsAsChild, numberOfInsertsAsLinkedChild, addMode};
        String result = rb.getString("parser.error.unknownError");
        try {
            result = MessageFormat.format(
                rb.getString("parser.diagnosisHierarchy.success"), values);
        }
        catch (MissingResourceException e) {}
        return new Message(Message.NOTE, result, "", 0,0,"");
    }
    
    //Begin Change Extension
    private void addMMInfo(NamedObject o,String title,String subject,String content){
        MMInfoStorage mmis; 
        DCMarkup dcm = new DCMarkup();
        dcm.setContent(DCElement.TITLE, title);
        dcm.setContent(DCElement.SUBJECT, subject);
        dcm.setContent(DCElement.SOURCE, o.getId());
        MMInfoObject mmi = new MMInfoObject(dcm, content); 
        if(o.getProperties().getProperty(Property.MMINFO) == null){
            mmis = new MMInfoStorage(); 
        }
        else{
            mmis = (MMInfoStorage)o.getProperties().getProperty(Property.MMINFO);
        }
        o.getProperties().setProperty(Property.MMINFO, mmis);
        mmis.addMMInfo(mmi);        
    }
    //End Change Extension
    
    


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

			if (la.kind == 24) {
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
	
	void DiagnosisHierarchy() {
		Diagnosis rootDiagnosis = kb.getRootDiagnosis();
		ht.put(-1, rootDiagnosis);
		
		while (la.kind == 7 || la.kind == 8) {
			if (la.kind == 7) {
				Get();
			} else {
				Get();
			}
		}
		DIAGNOSIS();
		
		while (la.kind == 1 || la.kind == 5) {
			DIAGNOSIS();
			
		}
		
		while (la.kind == 9) {
			DESCRIPTION();
		}
		
	}

	void DIAGNOSIS() {
		int diagnosisLevel = 0;
		
		while (la.kind == 5) {
			Get();
			diagnosisLevel = diagnosisLevel + 1;
			
		}
		lastDashToken = t;
		
		Expect(1);
		String diagnosisValue = t.val;
		diagnosisValue = diagnosisValue.trim();
		if (diagnosisValue.charAt(0) == '\"'
		        && diagnosisValue.charAt(diagnosisValue.length() - 1) == '\"') {
		    diagnosisValue = diagnosisValue.substring(1, diagnosisValue.length() - 1);
		}
		                                                          // semantisches Parsing der Zeile!
		if (addMode == true && diagnosisLevel == 0) {
		    if (diagnosisValue.equals(rb
		            .getString("parser.diagnosisHierarchy.toplevel"))) {
		        ht.put(0, kb.getRootDiagnosis());
		        toplevelMode = true;
		    } else {
		        toplevelMode = false;
		        Diagnosis anchorDiagnosis = kbm.findDiagnosis(diagnosisValue);
		        currentDiagnosis = anchorDiagnosis;
		        if (anchorDiagnosis != null) {
		            ht.put(diagnosisLevel, anchorDiagnosis);
		        } else {
		            createMessage(t.line, t.col,
		                    "parser.diagnosisHierarchy.error.anchor",
		                    Message.ERROR, diagnosisValue);
		        }
		    }
		    namedObject = currentDiagnosis;
		}
		if (checkAndSetAllowedLevel(diagnosisLevel, diagnosisValue)
		        && (diagnosisLevel != 0 || addMode == false)) {
		    
		    String diagnosisText = null;
		    if(diagnosisValue.contains("~")) {
		    	String parts [] = diagnosisValue.split("~");
		    	if(parts.length == 2) {
		    		diagnosisValue = parts[0].trim();
		    		diagnosisText = parts[1].trim();
		    	} 
		    
		    }
		    Diagnosis ancestor = ht.get(diagnosisLevel - 1);
		    Diagnosis stillContainedDiagnosis = kbm
		            .findDiagnosis(diagnosisValue);
		    currentDiagnosis = stillContainedDiagnosis;
		    
		    
		    if (stillContainedDiagnosis == null) {
		        Diagnosis child = kbm.createDiagnosis(diagnosisValue, ancestor);
		        child.getProperties().setProperty(Property.EXPLANATION,diagnosisText);
		        currentDiagnosis = child;
		        ht.put(diagnosisLevel, child);
		        numberOfInserts++;
		        // Toplevel-Kinder bei update nicht mitrechnen bei
		        // numberOfInsertsAsChild!
		        if (((diagnosisLevel > 1) && toplevelMode)
		                || ((diagnosisLevel > 0) && !toplevelMode))
		            numberOfInsertsAsChild++;
		    } else if (!isAncestor(ancestor, stillContainedDiagnosis)) {
		        if (!stillContainedDiagnosis.getParents().contains(ancestor)) {
		            stillContainedDiagnosis.addLinkedParent(ancestor);
		            numberOfInserts++;
		            numberOfInsertsAsLinkedChild++;
		        } else {
		            createMessage(t.line, t.col, "parser.diagnosisHierarchy.error.doubleInsertion", Message.ERROR, diagnosisValue);
		        }
		        ht.put(diagnosisLevel, stillContainedDiagnosis);
		    } else {
		        createMessage(t.line, t.col,
		                "parser.diagnosisHierarchy.error.loop", Message.ERROR, diagnosisValue);
		    }
		    namedObject = currentDiagnosis;
		}
		
		if (la.kind == 2) {
			Get();
			String id = t.val.substring(1, t.val.length());
			id = id.trim();
			Diagnosis dSearch = kbm.findDiagnosis(id);
			if (dSearch == null) {
				boolean idSet = this.kbm.changeID(currentDiagnosis, id);
				if (!idSet) {
					createMessage(t.line, t.col, "setting custom ID '" + id + "' for object "
									+ currentDiagnosis.getText() + " failed", Message.ERROR, currentDiagnosis, "");
				}
			} else if (dSearch != currentDiagnosis) {
			    createMessage(t.line, t.col, "parser.diagnosisHierarchy.error.idAlreadyExists", Message.ERROR, currentDiagnosis.getText(), id);
			}
			
		}
		while (la.kind == 6) {
			DIAGNOSISREFERENZID();
		}
		while (la.kind == 3) {
			Get();
			LINK();
			Expect(4);
		}
		while (!(la.kind == 0 || la.kind == 7 || la.kind == 8)) {SynErr(24); Get();}
		if (la.kind == 7 || la.kind == 8) {
			if (la.kind == 7) {
				Get();
			} else {
				Get();
			}
			while (la.kind == 7 || la.kind == 8) {
				if (la.kind == 7) {
					Get();
				} else {
					Get();
				}
			}
		} else if (la.kind == 0) {
			Get();
		} else SynErr(25);
	}

	void DESCRIPTION() {
		Expect(9);
		DESCRIPTIONID();
		String id = ""; 
		String typ = "info"; 
		String name = ""; 
		content = ""; 
		id = t.val.trim();
		
		Expect(9);
		while (la.kind == 1) {
			TYP();
			typ = t.val.trim();
			typ = typ.toLowerCase(); 
			
		}
		Expect(9);
		while (la.kind == 1) {
			NAME();
			name = t.val.trim();
			
		}
		Expect(9);
		while (StartOf(1)) {
			CONTENT();
		}
		boolean flag = false; 
		if(name.startsWith("\"") && name.endsWith("\"")){
		    name = name.replaceAll("\"","");
		    flag = true; 
		}
		//if(!allowedNames.contains(name) && !flag && !allowedNames.isEmpty()){                                                                               
		//     createMessage(t.line, t.col,
		//    "parser.decisionTree.warning.nameNotAllowed",
		//    Message.WARNING, "Bezeichner nicht erlaubt");                                                                               
		//}
		if(mapNamedObjects.containsKey(id) && mapNamedObjects.get(id)!= null){
		    for(Object namedObject : mapNamedObjects.get(id)){
		        if(namedObject instanceof NamedObject){
		            //name = change(name);
		            addMMInfo((NamedObject)namedObject,name,typ,content);   
		        }
		    }                                                                           
		}
		
		Expect(9);
		while (!(la.kind == 0 || la.kind == 7 || la.kind == 8)) {SynErr(26); Get();}
		if (la.kind == 7 || la.kind == 8) {
			if (la.kind == 7) {
				Get();
			} else {
				Get();
			}
			while (la.kind == 7 || la.kind == 8) {
				if (la.kind == 7) {
					Get();
				} else {
					Get();
				}
			}
		} else if (la.kind == 0) {
			Get();
		} else SynErr(27);
	}

	void DIAGNOSISREFERENZID() {
		Expect(6);
		String id = t.val.trim(); 
		abbr.add(id);
		ArrayList al = mapNamedObjects.get(id);
		if(al == null){
		    al = new ArrayList();
		}
		al.add(namedObject);
		mapNamedObjects.put(id,al);
		
	}

	void LINK() {
		Expect(3);
		Expect(1);
		String url = t.val;
		String typ = "url";
		String decl = ""; 
		
		while (la.kind == 10) {
			Get();
			Expect(1);
			url = url.concat("=" + t.val);
			
		}
		Expect(4);
		while (la.kind == 3) {
			Get();
			Expect(1);
			decl = t.val; 
			//decl = change(decl);
			
			Expect(4);
		}
		if(!savedLinks.contains(namedObject)){
		   savedLinks.add(namedObject);
		}
		addMMInfo(namedObject,decl,typ,url); 
		
	}

	void DESCRIPTIONID() {
		Expect(6);
	}

	void TYP() {
		Expect(1);
	}

	void NAME() {
		Expect(1);
	}

	void CONTENT() {
		if (StartOf(2)) {
			switch (la.kind) {
			case 5: {
				Get();
				break;
			}
			case 10: {
				Get();
				break;
			}
			case 11: {
				Get();
				break;
			}
			case 12: {
				Get();
				break;
			}
			case 13: {
				Get();
				break;
			}
			case 14: {
				Get();
				break;
			}
			case 3: {
				Get();
				break;
			}
			case 4: {
				Get();
				break;
			}
			case 15: {
				Get();
				break;
			}
			case 16: {
				Get();
				break;
			}
			case 17: {
				Get();
				break;
			}
			case 18: {
				Get();
				break;
			}
			case 19: {
				Get();
				break;
			}
			case 20: {
				Get();
				break;
			}
			case 21: {
				Get();
				break;
			}
			}
		} else if (la.kind == 6) {
			Get();
		} else if (la.kind == 2) {
			Get();
		} else if (la.kind == 1) {
			Get();
		} else if (la.kind == 22) {
			Get();
		} else SynErr(28);
		content = content.concat(t.val); 
		
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		DiagnosisHierarchy();

		Expect(0);
	}

	private boolean[][] set = {
		{T,x,x,x, x,x,x,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
		{x,T,T,T, T,T,T,x, x,x,T,T, T,T,T,T, T,T,T,T, T,T,T,x, x},
		{x,x,x,T, T,T,x,x, x,x,T,T, T,T,T,T, T,T,T,T, T,T,x,x, x}

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
			case 3: s = "BracketOpen expected"; break;
			case 4: s = "BracketClose expected"; break;
			case 5: s = "Dash expected"; break;
			case 6: s = "DiagnosisID expected"; break;
			case 7: s = "\"\\n\" expected"; break;
			case 8: s = "\"\\r\" expected"; break;
			case 9: s = "\"|\" expected"; break;
			case 10: s = "\"=\" expected"; break;
			case 11: s = "\"<\" expected"; break;
			case 12: s = "\">\" expected"; break;
			case 13: s = "\"{\" expected"; break;
			case 14: s = "\"}\" expected"; break;
			case 15: s = "\"(\" expected"; break;
			case 16: s = "\")\" expected"; break;
			case 17: s = "\"#\" expected"; break;
			case 18: s = "\"@\" expected"; break;
			case 19: s = "\"\\\'\" expected"; break;
			case 20: s = "\"\\\\\" expected"; break;
			case 21: s = "\"\\\"\" expected"; break;
			case 22: s = "\"/\" expected"; break;
			case 23: s = "??? expected"; break;
			case 24: s = "this symbol not expected in DIAGNOSIS"; break;
			case 25: s = "invalid DIAGNOSIS"; break;
			case 26: s = "this symbol not expected in DESCRIPTION"; break;
			case 27: s = "invalid DESCRIPTION"; break;
			case 28: s = "invalid CONTENT"; break;
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

