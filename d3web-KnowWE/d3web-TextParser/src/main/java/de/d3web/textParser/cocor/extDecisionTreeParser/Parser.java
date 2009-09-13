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

package de.d3web.textParser.cocor.extDecisionTreeParser;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.NumericalInterval;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerFactory;
import de.d3web.kernel.domainModel.answers.AnswerText;
import de.d3web.kernel.domainModel.formula.FormulaExpression;
import de.d3web.kernel.domainModel.formula.FormulaNumber;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionDate;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.qasets.QuestionZC;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
import de.d3web.kernel.domainModel.ruleCondition.CondChoiceNo;
import de.d3web.kernel.domainModel.ruleCondition.CondChoiceYes;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreater;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreaterEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumIn;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLess;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLessEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondTextEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondUnknown;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;
import de.d3web.kernel.psMethods.nextQASet.PSMethodNextQASet;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.DCMarkup;
import de.d3web.kernel.supportknowledge.MMInfoObject;
import de.d3web.kernel.supportknowledge.MMInfoStorage;
import de.d3web.kernel.supportknowledge.MMInfoSubject;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.report.Message;
import de.d3web.textParser.Utils.ConceptNotInKBError;
import de.d3web.textParser.Utils.ScoreFinder;
import de.d3web.textParser.decisionTable.MessageGenerator;

public class Parser {
	static final int _EOF = 0;
	static final int _declaration = 1;
	static final int _number = 2;
	static final int _newline = 3;
	static final int _dash = 4;
	static final int _parenthesisOpen = 5;
	static final int _parenthesisClose = 6;
	static final int _bracketOpen = 7;
	static final int _bracketClose = 8;
	static final int _ID = 9;
	static final int _descriptionId = 10;
	static final int _allowedNames = 11;
	static final int maxT = 57;
	static final int _comment = 58;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	Scanner scanner;
	Errors errors;

	private KnowledgeBase kb;
	private KnowledgeBaseManagement kbm;
	private boolean addMode;
	
	private int numberOfInsertedQuestions = 0;
	private int numberOfInsertedFollowingQuestions = 0;
    private int numberOfInsertedQContainerIndications = 0;
	private int numberOfInsertedDiagnosisDerivations = 0;
	private int numberOfInsertedQContainerIndicationsByDiagnosis = 0;
	
	public List<Message> successMessages = new LinkedList<Message>();
	
	//Stack to remember path to leaf for create complex conditions (conjunctions)
	private Stack<TerminalCondition> conditionStack = new Stack<TerminalCondition>();
	private boolean complexPathConditions = true;
	
	private HashMap<Question,Integer> questionCounts = new HashMap<Question,Integer>();
	private boolean uniqueQuestionNames = true;
	
    private static final ResourceBundle rb = ResourceBundle.getBundle("properties.textParser");
    
    //Begin Change Extension
    private String content = ""; 
	private NamedObject namedObject = null; 
	private AnswerChoice answer = null; 
	private HashMap<String,ArrayList<Object>> mapNamedObjects = new HashMap<String,ArrayList<Object>>(); 
	private HashMap<String,ArrayList<Object>> mapAnswers = new HashMap<String,ArrayList<Object>>();
	private ArrayList<String> abbr = new ArrayList<String>(); 
	private ArrayList<String> allowedNames = new ArrayList<String>(); 
	private ArrayList<NamedObject> savedLinks = new ArrayList<NamedObject>(); 
	//End Change Extension
    
    /**
     * Setzt die Zähler für die geparsten Elemente zurück
     */
    private void resetNumberCounter() {
	    this.numberOfInsertedQuestions = 0;
	    this.numberOfInsertedFollowingQuestions = 0;
		this.numberOfInsertedQContainerIndications = 0;
		this.numberOfInsertedDiagnosisDerivations = 0;
		this.numberOfInsertedQContainerIndicationsByDiagnosis = 0;
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
    
    private void createDiagnosisUnknownError(
            int row, int column, String key, String messageType, Object ... values) {
        String result = rb.getString("parser.error.unknownError") + ": "+key;
        try {
            result = MessageFormat.format(
                rb.getString(key), values);
        }
        catch (MissingResourceException e) {}
        ConceptNotInKBError err = new ConceptNotInKBError(messageType, result, null, row,column, null );
        if(values !=  null && values.length > 0) {
        	err.setObjectName((String)values[0]);
        }
        err.setKey(MessageGenerator.KEY_INVALID_DIAGNOSIS);
        errors.Error(err);   
    }
    
    private void createDiagnosisOrSIUnknownError(
            int row, int column, String key, String messageType, Object ... values) {
        String result = rb.getString("parser.error.unknownError") + ": "+key;
        try {
            result = MessageFormat.format(
                rb.getString(key), values);
        }
        catch (MissingResourceException e) {}
        ConceptNotInKBError err = new ConceptNotInKBError(messageType, result, null, row,column, null );
        if(values !=  null && values.length > 0) {
        	err.setObjectName((String)values[0]);
        }
        err.setKey(MessageGenerator.KEY_INVALID_DIAGNOSIS);
        errors.Error(err);   
    }
	
    /**
     * Fügt eine Erfolgsmeldung zur Liste hinzu (pro Frageklasse)
     * @param qContainer Name der Frageklasse
     */
    public void addSuccessNote(String qContainer) {
        Object[] values = {
        	qContainer,
		    this.numberOfInsertedQuestions,
		    this.numberOfInsertedFollowingQuestions,
			this.numberOfInsertedQContainerIndications,
			this.numberOfInsertedDiagnosisDerivations,
			this.numberOfInsertedQContainerIndicationsByDiagnosis
    	};
        String result = rb.getString("parser.error.unknownError");
        try {
            result = MessageFormat.format(
                rb.getString("parser.decisionTree.success"), values);
        }
        catch (MissingResourceException e) {}
        successMessages.add(new Message(Message.NOTE, result, "", 0,0,""));
        
        resetNumberCounter();
    }
    
    /**
     * Formatiert den Bezeichner einer Frageklasse, entfernen von Whitespace am Anfang und Ende sowie der Anführungszeichen
     * @param decl unformatierter Name der Frageklasse
     * @return formatierter Name der Frageklasse
     */
    private String formatDeclaration(String decl) {
        // fehler besser abfangen: java.lang.StringIndexOutOfBoundsException: String index out of range: 0
		decl = decl.trim();
		try {
			if (decl.charAt(0) == '\"'
					&& decl.charAt(decl.length() - 1) == '\"') {
				decl = decl.substring(1, decl.length() - 1);
			}
		} catch(StringIndexOutOfBoundsException e) {
			createMessage(t.line, t.col,
					"parser.decisionTree.error.formatDeclaration",
					Message.ERROR);
		}
		return decl;
	}
    
    /**
     * Überprüft, ob das Ende der zu parsenden Datei erreicht ist
     * @return true: Ende ist nicht erreicht, false: Ende ist erreicht
     */
    private boolean isNotEOF() {
		scanner.ResetPeek();
    	Token x = la;
    	while (x.kind == _newline) {
    		x = scanner.Peek();
    	}
    	return x.kind != _EOF;
    }
    
    /**
     * Überprüft, ob in der nächsten Zeile eine Frage der Ebene 1 (1 Dash) steht, also direkt in einer Frageklasse enthalten ist.
     * @return true: ist Frage der ersten Ebene, false: ist nicht Frage der ersten Ebene
     */
    private boolean isFirstLevelQuestion() {
		scanner.ResetPeek();
    	Token x = la;
    	while (x.kind == _newline) {
    		x = scanner.Peek();
    	}
    	return x.kind == _dash && scanner.Peek().kind != _dash;
    }
    
    
    /**
     * Überprüft, ob die nächste Zeile eine bestimmte Anzahl an Anführungsstrichen vorweisen kann.
     * @param dashes Anzahl der Dashes, auf die in der nächsten Zeile überprüft werden soll
     * @return true: geforderte Anzahl liegt vor, false: geforderte Anzahl liegt nicht vor.
     */
    private boolean properAmountOfDashes(int dashes) {
		scanner.ResetPeek();
    	int thisDashes = 0;
    	Token x = la;
    	while (x.kind == _newline) {
			x = scanner.Peek();
		}
    	while (x.kind == _dash) {
    		x = scanner.Peek();
    		thisDashes++;
    	}
    	return thisDashes == dashes;
    }
    
    
    /**
     * zählt die Anführungsstriche der nächsten Zeile
     * @return Anzahl der Anführungsstriche der nächsten Zeile
     */
    private int countDashes() {
		scanner.ResetPeek();
    	int countDashes = 0;
    	Token x = la;
		while (x.kind == _newline) {
			x = scanner.Peek();
		}
		while (x.kind == _dash) {
			x = scanner.Peek();
			countDashes++;
		}
		return countDashes;
	}
	
	/**
	 * Überprüft, ob der String c der Anfang des Tokens ist, der nach dem gerade betrachteten Declaration-Tokens kommt.
	 * Es geht darum zu überprüfen, ob in der gleichen Zeile der Declaration noch ein Token kommt, der z.B. auf einen Fragetyp hinweist. 
	 * @param c der zu suchende String
	 * @param t der aktuelle Token
	 * @return true: der String c wurde als Anfang des nächsten Tokens gefunden, ansonsten false
	 */
	private boolean isFirstNonDeclarationToken(String c, Token t) {
		scanner.ResetPeek();
		while (t.kind != _dash) {
			t = scanner.Peek();
		}
		while (t.kind == _dash) {
			t = scanner.Peek();
		}
		Token sp = scanner.Peek();
		boolean result = ((t.kind==_declaration)&& sp.val.startsWith(c));
		return result;
	}
	
	/**
	 * "handle"-Methoden sind ausgelagerte Methoden, um die syntaktische EBNF im *.atg-File klarer von den semantischen Aktionen zu trennen.
	 * Dies ist eine Handle-Methode für die semantische Behandlung einer Frageklassen-Definition.
	 * Bei allen folgenden handle-Methoden weist die Bezeichnung darauf hin, wo sie in der Syntax eingesetzt wird.
	 * Es werden nur noch Parameter und return-Typen angegeben.
	 * @param q Token des aktuellen, zu suchenden QContainers
	 * @return der QContainer
	 */
	private QContainer handleQContainerDefinition(Token q) {
		resetNumberCounter();
		QContainer qc = null;
		String declaration = q.val.trim();
		declaration = formatDeclaration(q.val);
		qc = kbm.findQContainer(declaration);
		if (qc == null) {
			createMessage(q.line, q.col,
			"parser.decisionTree.error.qContainerNotFound",
			Message.ERROR, declaration);
		}
		if (addMode && (qc.getChildren().size() > 0)) {
			createMessage(q.line, q.col,
			"parser.decisionTree.error.update.qContainerNotEmpty",
			Message.ERROR, declaration);
		}
		return qc;
	}
	
	/**
	 * @param q abstrakte Frage
	 */
	private void handleQuestionAbstract(Question q) {
		q.getProperties().setProperty(Property.ABSTRACTION_QUESTION, true);
	}
	
	/**
	 * @param intervalLeft linke Intervall-Grenze, eingeschlossen
	 * @param intervalRight rechte Intervall-Grenze, eingeschlossen
	 * @param d Name der numerischen Frage
	 * @param unit Einheit des Antworten-Bereichs
	 * @param q Vater-QASet
	 * @return die Frage, die erzeugt wurde
	 */
	private Question handleQuestionNum(Double intervalLeft,
			Double intervalRight, String d, String unit, QASet q) {

		//if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionNum currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionNum)) {
			currentQuestion = (QuestionNum)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionNum(d, q);
		}
		
		if (unit != null)
			currentQuestion.getProperties().setProperty(Property.UNIT, unit);
		if (intervalLeft != null && intervalRight != null) {
			NumericalInterval range = new NumericalInterval(intervalLeft,
					intervalRight);
			currentQuestion.getProperties().setProperty(
					Property.QUESTION_NUM_RANGE, range);
		}
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	public HashMap<Question,Integer> getQuestionCounts() {
		return this.questionCounts;
	}
	
	//counts how often the same questions appeared in decTree for reporting
	private void countQuestionAppearence(Question question) {
		if(question == null) return;
		if(questionCounts.containsKey(question)) {
			Integer cnt = questionCounts.get(question);
			questionCounts.put(question, new Integer(cnt.intValue()+1));
		}else {
			questionCounts.put(question, new Integer(2));
		}
	}
	
	/**
	 * @param d Name der OC-Frage
	 * @param q Vater-QASet
	 * @return die erzeugte Frage
	 */
	private Question handleQuestionOC(String d, QASet q) {
//		if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionOC currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionOC)) {
			currentQuestion = (QuestionOC)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionOC(d, q,
					new AnswerChoice[] {});
		}
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
		/**
	 * @param d Name der ZC-Frage
	 * @param q Vater-QASet
	 * @return die erzeugte Frage
	 */
	private Question handleQuestionZC(String d, QASet q) {
//		if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionZC currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionZC)) {
			currentQuestion = (QuestionZC)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionZC(d, q);
		}
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * @param d Declaration der MC-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionMC(String d, QASet q) {
//		if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionMC currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionMC)) {
			currentQuestion = (QuestionMC)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionMC(d, q,
					new AnswerChoice[] {});
		}
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * wird wohl erstmal nicht gebraucht, die gesamte QuestionDate-Behandlung wird auch von knowme noch nicht unterstützt
	 * @param d Declaration der Date-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionDate(String d, QASet q) {
//		if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionDate currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionDate)) {
			currentQuestion = (QuestionDate)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionDate(d, q);
		}
		
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * @param d Declaration der Text-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionText(String d, QASet q) {
//		if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionText currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionText)) {
			currentQuestion = (QuestionText)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionText(d, q);
		}
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * @param d Declaration der YN-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionYN(String d, QASet q) {
//		if question already exists take existing one else create new
		Question question = kbm.findQuestion(d);
		countQuestionAppearence(question);
		QuestionYN currentQuestion = null;
		if (this.uniqueQuestionNames && question != null && (question instanceof QuestionYN)) {
			currentQuestion = (QuestionYN)question;
			q.addChild(currentQuestion);
			
		}else {
			currentQuestion = kbm.createQuestionYN(d, q);
		}
		
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * handled für AnswerNum den Comparator; anstatt möglicher Methode "handleAnswerNum()".
	 * Behandelt Vergleiche mit nur einem numerischen Wert als Antwort
	 * @param q die Frage, für die der Vergleich durchgeführt werden soll
	 * @param doubleValue der Wert, mit dem verglichen werden soll
	 */
	private void handleComparison(Question q, Token doubleValue) {
		NumericalInterval range = (NumericalInterval) q.getProperties().getProperty(Property.QUESTION_NUM_RANGE);
		double answerValue = Double.parseDouble(doubleValue.val.replaceAll("," , "."));
	    if(range != null && !range.contains(answerValue))
	        createMessage(t.line, t.col,
			"parser.decisionTree.error.notMatchingInterval1",
			Message.ERROR, answerValue);
	}
	
	/**
	 * handled für AnswerNum den Comparator; anstatt möglicher Methode "handleAnswerNum()".
	 * Behandelt Vergleiche mit zwei numerischen Werten als Antwort-Berich (Intervall)
	 * @param q die Frage, für die der Vergleich durchgeführt werden soll
	 * @param doubleValueLeft linke Grenze, mit der verglichen werden soll
	 * @param doubleValueRight rechte Grenze, mit der verglichen werden soll
	 */
	private void handleComparison(Question q, Token doubleValueLeft, Token doubleValueRight) {
		NumericalInterval range = (NumericalInterval) q.getProperties().getProperty(Property.QUESTION_NUM_RANGE);
		double answerValueLeft = Double.parseDouble(doubleValueLeft.val.replaceAll("," , "."));
		double answerValueRight = Double.parseDouble(doubleValueRight.val.replaceAll("," , "."));
	    if(range != null && !(range.contains(answerValueLeft)&&range.contains(answerValueRight)))
	        createMessage(t.line, t.col,
			"parser.decisionTree.error.notMatchingInterval2",
			Message.ERROR, answerValueLeft, answerValueRight);
	}
	
	/**
	 * @param q Frage
	 * @param t Token der AnswerChoice
	 * @return AbstractCondition, welche in die KB eingefügt wurde. null, wenn ein Fehler auftrat.
	 */
	private TerminalCondition handleAnswerChoice(Question q, Token t, String id) {
		AnswerChoice acSearch = kbm.findAnswerChoice((QuestionChoice)q, t.val.trim());
		if (acSearch == null) {
			String declaration = formatDeclaration(t.val);
			if(!id.startsWith(q.getId())) {
				id = q.getId() + id;
			}
			acSearch = AnswerFactory.createAnswerChoice(id, declaration);
			acSearch.setQuestion(q);
			QuestionChoice qc = (QuestionChoice)q;
			qc.addAlternative(acSearch);
			// Begin Change Extension
			answer = acSearch;
			// End Change Extension
			
		} 
		CondEqual c = new CondEqual((QuestionChoice)q, acSearch);
		return c;
	}
	
	/**
	 * wird wohl erstmal nicht gebraucht, die gesamte QuestionDate-Behandlung wird auch von knowme noch nicht unterstützt
	 * @param q Frage
	 * @param t Token der AnswerDate
	 * @return AbstractCondition, welche in die KB eingefügt wurde
	 */
	private TerminalCondition handleAnswerDate(Question q, Token t) {
//		String declaration = formatDeclaration(t.val);
//		AnswerDate answerDate = new AnswerDate();
//		//COCO: todo!
//		//TODO: todo!
//		DateFormat d = new SimpleDateFormat();
//		answerDate.setValue(d.parse(declaration));
//		answerDate.setQuestion(q);
//		// Begin Change Extension
//		answer = (AnswerChoice)answerDate; 
//      // End Change Extension
//		// diese Methode gibts noch nicht
//		CondEqual c = new CondEqual(q, answerDate);
		return null;
	}
	
	/**
	 * @param q Frage
	 * @param t Token der AnswerText
	 * @return AbstractCondition, welche in die KB eingefügt wurde
	 */
	private TerminalCondition handleAnswerText(Question q, Token t) {
		String declaration = formatDeclaration(t.val);
		AnswerText answerText = new AnswerText();
		answerText.setText(declaration);
		answerText.setQuestion(q);
//		answer = (AnswerChoice)answerText; 
		CondTextEqual c = new CondTextEqual((QuestionText)q, declaration);
		return c;
	}
	
	/**
	 * @param q Frage
	 * @param declaration Declaration der AnswerYes
	 * @return AbstractCondition, welche in die KB eingefügt wurde
	 */
	private TerminalCondition handleAnswerYes(Question q) {
		// Begin Change Extension
		answer = ((QuestionYN)q).yes; 
		// End Change Extension
		CondChoiceYes c = new CondChoiceYes((QuestionYN)q);
		return c;
	}
	
	/**
	 * @param q Frage
	 * @param declaration Declaration der AnswerNo
	 * @return AbstractCondition, welche in die KB eingefügt wurde
	 */
	private TerminalCondition handleAnswerNo(Question q) {
		// Begin Change Extension
		answer = ((QuestionYN)q).no; 
		// Begin Change Extension
		CondChoiceNo c = new CondChoiceNo((QuestionYN)q);
		return c;
	}
	
	/**
	 * @param q Frage
	 * @param declaration Declaration der AnswerUnknown
	 * @return AbstractCondition, welche in die KB eingefügt wurde
	 */
	private TerminalCondition handleAnswerUnknown(Question q) {
		CondUnknown c = new CondUnknown(q);
		return c;
	}
	
	/**
	 * @param c Kondition
	 * @param ql Frageliste, die mit dem Feuern der Kondition indiziert wird
	 */
	private void handleQuestionIndication(List ql) {

		if (ql.size() > 0) {
			AbstractCondition cond = conditionStack.peek();
			if(this.complexPathConditions) {
				cond = getActualPathConjunction();
			}

			if (cond != null) {
				String newRuleID = kbm.findNewIDFor(new RuleComplex());
				RuleFactory.createIndicationRule(newRuleID, ql, cond);
			} else {
				createMessage(t.line, t.col,
						"parser.decisionTree.error.conditionNull",
						Message.ERROR, t.val);
			}
		}

		this.numberOfInsertedFollowingQuestions++;
	}
	
	/**
	 * @param c Kondition
	 * @param t Token der Frageklasse, die mit dem Feuern der Kondition indiziert wird
	 */
	private void handleQContainerIndication(List qcl) {
		if (qcl.size() > 0) {
			AbstractCondition cond = conditionStack.peek();
			if(this.complexPathConditions) {
				cond = getActualPathConjunction();
			}
			String newRuleID = kbm.findNewIDFor(new RuleComplex());
			if (cond != null) {
				List <Question> ql = filterQuestions(qcl);
				qcl.removeAll(ql);
				
				if(qcl.size() > 0) {
				RuleFactory.createIndicationRule(newRuleID, qcl, cond);
				}
				
				for (Question question : ql) {
					RuleFactory.createIndicationRule(newRuleID, question, cond);
				}
				
				this.numberOfInsertedQContainerIndications += qcl.size();
			} else {
				createMessage(
						t.line,
						t.col,
						"no path condition found on descend to qcontainer indication rule",
						Message.ERROR);
			}
		}
	}
	
	private List<Question> filterQuestions(List qcl) {
		List<Question> ql = new ArrayList<Question>();
		for (Object object : qcl) {
			if(object instanceof Question) {
				ql.add((Question)object);
			}
		}
		return ql;
	}

	
	/**
	 * Generiert eine Konjunktion über alle TConds auf dem Pfad von der Wurzel
	 * bis zum actuellen element.
	 */
	private AbstractCondition getActualPathConjunction() {
		AbstractCondition cond = null;
		if (this.conditionStack.size() == 1) {
			cond = conditionStack.get(0);
		} else if (this.conditionStack.size() > 1) {
			cond = new CondAnd(new ArrayList(this.conditionStack));
		} else {
			createMessage(
					t.line,
					t.col,
					"no condition found on descend to diagnosis indication rule",
					Message.ERROR);
		}
		return cond;
	}
	
	/**
	 * @param c Kondition 
	 * @param declarationToken Declaration der Diagnose, welche mit dem Feuern der Kondition indiziert wird
	 * @param indicationStrengthToken Token, der die Indikations-Stärke repräsentiert
	 * @return die gefundene Diagnose
	 */
	private Diagnosis handleDiagnosisIndication(Token declarationToken,
			Token indicationStrengthToken) {
				String declaration = formatDeclaration(declarationToken.val);
		Diagnosis diagnosis = kbm.findDiagnosis(declaration);
		String indicationStrength = formatDeclaration(indicationStrengthToken.val);
		if (diagnosis != null) {
			AbstractCondition cond = conditionStack.peek();
			if (this.complexPathConditions) {
				cond = getActualPathConjunction();
			}

			if (cond != null) {
				String newRuleID = kbm.findNewIDFor(new RuleComplex());
				RuleFactory.createHeuristicPSRule(newRuleID, diagnosis,
						ScoreFinder.getScore(indicationStrength), cond);
				return diagnosis;
			} else {
				createMessage(
						t.line,
						t.col,
						"no actual condition found on descend to diagnosis indication rule",
						Message.ERROR, declaration);
			}
			// numberOfCreatedDiagnosisRules++;
		} else {
			String name = declarationToken.val.trim();
			Question q = kbm.findQuestion(name);
			boolean set = false;
			if (q == null && name.endsWith("SET")) {
				q = kbm.findQuestion(name.substring(0, name.length() - 3)
						.trim());
				set = true;
			}
			if (q != null) {
				String newRuleID = kbm.findNewIDFor(new RuleComplex());
				// if (q instanceof QuestionNum) {
				// QuestionNum qNum = ((QuestionNum) q);
				// }

				AbstractCondition cond = conditionStack.peek();
				if (this.complexPathConditions) {
					cond = getActualPathConjunction();
				}
				if (cond != null) {
					

					if (q instanceof QuestionNum) {
						double d = 0;
						try {
							d = Double.parseDouble(indicationStrengthToken.val);
						} catch (Exception e) {
							createMessage(t.line, t.col, "invalid.Value",
									Message.ERROR, indicationStrengthToken.val);
						}
						FormulaNumber num = new FormulaNumber(d);
						FormulaExpression e = new FormulaExpression(
								(QuestionNum) q, num);

						if (set) {
							RuleFactory.createSetValueRule(newRuleID,
									(QuestionNum) q, e, cond);
						} else {
							RuleFactory.createAddValueRule(newRuleID,
									(QuestionNum) q, new Object[] { e }, cond);
						}
					} else if (q instanceof QuestionChoice) {
						AnswerChoice a = kbm.findAnswerChoice((QuestionChoice)q, indicationStrengthToken.val);
						if(a != null) {
							RuleFactory.createSetValueRule(newRuleID, q, new Object[] {a}, cond, null);
						}else {
							createMessage(
									t.line,
									t.col,
									indicationStrengthToken.val+" is not a valid answer for: "+q.toString(),
									Message.ERROR, declaration);
						}
					}
					return null;
				} else {
					createMessage(
							t.line,
							t.col,
							"no actual condition found on descend to diagnosis indication rule",
							Message.ERROR, declaration);
				}
			} else if (q != null && q instanceof QuestionChoice) {

			} else {
				createDiagnosisOrSIUnknownError(t.line, t.col,
						"parser.decisionTree.error.diagnosisOrSINotFound",
						Message.ERROR, declaration);
			}
		}
		this.numberOfInsertedDiagnosisDerivations++;
		return null;
	}
	
	/**
	 * Hier wird einer Liste von Frageklassen eine weitere Frageklasse hinzugefügt.
	 * @param declarationToken Declaration des QContainers (Frageklasse)
	 * @param qcl Frageklassen-Liste
	 */
	private void handleQContainerAccumulation(Token declarationToken, List qcl) {
		String declaration = formatDeclaration(declarationToken.val);
		String refPrefix = "&REF";
		if(declaration.startsWith(refPrefix)) {
			String referencedQuestionname = declaration.substring(refPrefix.length()).trim();
			Question q = kbm.findQuestion(referencedQuestionname);
			if(q == null) {
				createMessage(t.line, t.col,
					"parser.decisionTree.error.questionNotFound",
					Message.ERROR, referencedQuestionname);
			}else {
				qcl.add(q);
				return;
			}
		}
		QContainer qc = kbm.findQContainer(declaration);
		if (qc != null) {
			qcl.add(qc);
		} else {
			createMessage(t.line, t.col,
					"parser.decisionTree.error.qContainerNotFound",
					Message.ERROR, declaration);
		}
	}
	
	/**
	 * @param d Diagnose, welche eine Liste von Frageklassen indizieren soll.
	 * @param indicationStrengthToken Token der Indikations-Stärke
	 * @param qcl Liste der Frageklassen
	 */
	private void handleQContainerIndicationByDiagnosis(Diagnosis d, Token indicationStrengthToken, List qcl) {
		if (qcl.size() > 0) {
			String declarationStrength = formatDeclaration(indicationStrengthToken.val);
			Score score = ScoreFinder.getScore(declarationStrength);
			if (d != null) {
				List ruleList = d.getKnowledge(PSMethodNextQASet.class, MethodKind.FORWARD);
				// sollte normalerweise immer nur eine Regel haben!!!
				List existingQcl = null;
				if (ruleList != null){
					RuleComplex rc = (RuleComplex) ruleList.get(0);
					ActionNextQASet ra = (ActionNextQASet) rc.getAction();
					existingQcl = ra.getQASets();
				}
				// Die Regel muss erzeugt werden
				if(existingQcl == null || existingQcl.isEmpty()) {
					String newRuleID = kbm.findNewIDFor(new RuleComplex());
					if (score.compareTo(Score.P6) >= 0) {
						AbstractCondition c = new CondDState(d, DiagnosisState.ESTABLISHED, PSMethodHeuristic.class);
						RuleFactory.createRefinementRule(newRuleID, qcl, d, c);
					} else if (score.compareTo(Score.P3) >= 0) {
						AbstractCondition c = new CondDState(d, DiagnosisState.SUGGESTED, PSMethodHeuristic.class);
						RuleFactory.createClarificationRule(newRuleID, qcl, d, c);
					} else {
						AbstractCondition c = new CondDState(d, DiagnosisState.UNCLEAR, PSMethodHeuristic.class);
						RuleFactory.createIndicationRule(newRuleID, qcl, c);
						createMessage(t.line, t.col,
								"parser.decisionTree.warning.diagnosisStateUnclear",
								Message.WARNING, d.getText(), score.toString());
					}
					this.numberOfInsertedQContainerIndicationsByDiagnosis++;
				// Es muß ein Fehler geworfen werden, da die Listen nicht übereinstimmen!
				} else {
					boolean bool1 = existingQcl.containsAll(qcl);
					boolean bool2 = qcl.containsAll(existingQcl);
					if ( ! (bool1 && bool2)) {
						createMessage(t.line, t.col, "parser.decisionTree.error.differentQCLists", Message.ERROR, d.getText());
					} // else: Die Regel muss nicht erzeugt werden, da die Liste der QContainer übereinstimmt und bereits eine Regel existiert
				}
			} else {
				createMessage(t.line, t.col,
						"parser.decisionTree.error.diagnosisNotFound",
						Message.ERROR, "null");
			}
		}
	}
	
	private void handleQuestionIdentifier(Question q, String id){
		id = id.substring(1, id.length());
		id = id.trim();
		Question qSearch = kbm.findQuestion(id);
		if (qSearch == null) {
			q.setId(id);
		} else if (qSearch != q) {
			createMessage(t.line, t.col, "parser.decisionTree.error.idAlreadyExists", Message.ERROR, q.getText(), id);
		}
	}
	
	private String handleAnswerChoiceIdentifier(String id) {
		id = id.substring(1, id.length());
		id = id.trim();
		return id;
	}	
	
	//Begin Change Extension
	private void addMMInfo(NamedObject o,String title,String subject,String content){
		if(o == null) return;
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
	
	private String change(String decl){
		if(decl.equals("Durchf\u00fchrung")){
			decl = "realisation";
		}
		else if(decl.equals("Kurze Erkl\u00e4rung")){
			decl = "shortDescription"; 
		}
		else if(decl.equals("Lange Erkl\u00e4rung")){
			decl = "longDescription"; 
		}
		else if(decl.equals("Beispiele")){
			decl = "examples";
		}
		else if(decl.equals("Erl\u00e4terung")){
			decl = "explanation";
		}
		return decl; 
	}
	
	private String revChange(String decl){
		if(decl.equals("realisation")){
			decl = "Durchf\u00fchrung";
		}
		else if(decl.equals("shortDescription")){
			decl = "Kurze Erkl\u00e4rung"; 
		}
		else if(decl.equals("longDescription")){
			decl = "Lange Erkl\u00e4rung"; 
		}
		else if(decl.equals("examples")){
			decl = "Beispiele";
		}
		else if(decl.equals("explanation")){
			decl = "Erl\u00e4terung";
		}
		return decl; 
	}
	
	private QuestionChoice getQuestion(AnswerChoice a){
		Iterator iter = kb.getQuestions().iterator(); 
		while(iter.hasNext()){
			Question question = (Question)iter.next();
			if(question instanceof QuestionChoice){ 
				QuestionChoice q = (QuestionChoice)question;
				Iterator iteranswer = q.getAllAlternatives().iterator();
					while(iteranswer.hasNext()){
						AnswerChoice answer = (AnswerChoice)iteranswer.next(); 
						if(a.equals(answer)){
							return q; 
						}
					}
			}
		}
		return null; 
	}
	private void setAnswerContent(AnswerChoice a, String content){
		QuestionChoice q = getQuestion(a); 
		if(q != null){
			Iterator iteranswer = q.getAllAlternatives().iterator();
			while(iteranswer.hasNext()){
				AnswerChoice answer = (AnswerChoice)iteranswer.next(); 
				if(a.equals(answer)){
					answer.getProperties().setProperty(Property.EXPLANATION, content);
				}
			}
		}
	}
	
	//End Change Extension
	
	//Begin Text Extension
    private void setPrompt(String d, String prompt)
    {       
        Question q = kbm.findQuestion(d);
        
        if(q == null) {
        createMessage(
				t.line,
				t.col,
				"question not found in knnowledgebase for setting prompt: "+d,
				Message.ERROR);
        	Logger.getLogger(this.getClass().getName()).severe("ERROR ON SETTING PROMPT: question not found in knnowledgebase for setting prompt: "+d);
        	return;
        }
           
        MMInfoStorage storage;
        
        if(q.getProperties().getProperty(Property.MMINFO) == null){
            storage = new MMInfoStorage(); 
        }
        else{
            storage = (MMInfoStorage)q.getProperties().getProperty(Property.MMINFO);
        }
    
        DCMarkup markup = new DCMarkup();
        markup.setContent(DCElement.SOURCE, q.getId());
        markup.setContent(DCElement.SUBJECT, MMInfoSubject.PROMPT.getName());      
        
        storage.addMMInfo(new MMInfoObject(markup, prompt));
        q.getProperties().setProperty(Property.MMINFO, storage);
    }
    //End Text Extension
	


	// COCO begin change
	public Parser(Scanner scanner, KnowledgeBase kb, boolean addMode) {
		this(scanner,kb,addMode,true,true);
	}
	
	public Parser(Scanner scanner, KnowledgeBase kb, boolean addMode, boolean complexPathConditions, boolean uniqueQuestionNames) {
		this.scanner = scanner;
		errors = new Errors(this.scanner.getFilename());
        this.kb = kb;
        this.kbm = KnowledgeBaseManagement.createInstance(kb);
        this.addMode = addMode;
        this.complexPathConditions = complexPathConditions;
        this.uniqueQuestionNames = uniqueQuestionNames;
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

			if (la.kind == 58) {
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
	
	void DecisionTree() {
		while (la.kind == 3) {
			Get();
		}
		QContainerDefinition();
		while (isNotEOF() && properAmountOfDashes(0)) {
			Separator();
			if (la.kind == 1) {
				QContainerDefinition();
			} else if (StartOf(1)) {
				while (la.kind == 11 || la.kind == 12) {
					while (la.kind == 11) {
						AllowedNames();
						while (la.kind == 3) {
							Get();
						}
					}
					Expect(12);
					Description();
					Expect(12);
				}
			} else SynErr(58);
		}
		while (la.kind == 3) {
			Get();
		}
		Expect(0);
	}

	void QContainerDefinition() {
		Declaration();
		String qContainerName = formatDeclaration(t.val);
		QContainer q = handleQContainerDefinition(t);
		
		Question thisQ = Question(q);
		while (isFirstLevelQuestion()) {
			thisQ = Question(q);
		}
		addSuccessNote(qContainerName);
		
	}

	void Separator() {
		while (!(la.kind == 0 || la.kind == 3)) {SynErr(59); Get();}
		Expect(3);
		while (la.kind == 3) {
			Get();
		}
	}

	void AllowedNames() {
		Expect(11);
		Expect(27);
		Expect(14);
		while (la.kind == 1) {
			Get();
			String value = t.val.trim();
			String[] values = value.split(",");
			for(String valueTemp : values){
				valueTemp = valueTemp.trim(); 
				if(!valueTemp.equals(",")){
			 		valueTemp = valueTemp.replaceAll("\"","");
			 		if(valueTemp.equals("-")){ 
			 			allowedNames.add("");
			 		}
			 		else{
			 			allowedNames.add(valueTemp);
			 		}
			 	}
			}
			
		}
		if(!savedLinks.isEmpty()){
		Iterator iter = savedLinks.iterator(); 
		while(iter.hasNext()){
			NamedObject link = (NamedObject)iter.next();
			MMInfoStorage mmis = (MMInfoStorage)link.getProperties().getProperty(Property.MMINFO);
			Iterator itermarkups = mmis.getAllDCMarkups().iterator(); 
			while(itermarkups.hasNext()){
				DCMarkup dcmarkup = (DCMarkup)itermarkups.next(); 
					String title = dcmarkup.getContent(DCElement.TITLE); 
					boolean flag = false; 
					if(title.startsWith("\"") && title.endsWith("\"")){
						title = title.replaceAll("\"","");
						flag = true; 
					}
					title = revChange(title);
					if(allowedNames.contains(title) || flag || allowedNames.isEmpty()){
					}
					else{
		 				createMessage(0, 0,
						"parser.decisionTree.warning.nameNotAllowed",
						Message.WARNING, "Bezeichner nicht erlaubt");																								
					//Iterator iterer = mmis.getMMInfo(dcmarkup).iterator();
					//while(iterer.hasNext()){
					//	MMInfoObject mmio = (MMInfoObject)iterer.next(); 
					//	mmis.removeMMInfo(mmio);
					}
						 
					}
			}
		
		}
		
		Expect(15);
		Expect(3);
	}

	void Description() {
		DescriptionID();
		String id = ""; 
		String typ = "info"; 
		String name = ""; 
		content = ""; 
		id = t.val.trim();
		
		Expect(12);
		while (la.kind == 1) {
			Typ();
			typ = t.val.trim();
			typ = typ.toLowerCase(); 
			
		}
		Expect(12);
		while (la.kind == 1) {
			Name();
			name = t.val.trim();
			
		}
		Expect(12);
		while (StartOf(2)) {
			Content();
		}
		boolean flag = false; 
		if(name.startsWith("\"") && name.endsWith("\"")){
			name = name.replaceAll("\"","");
			flag = true; 
		}
		if(!allowedNames.contains(name) && !flag && !allowedNames.isEmpty()){																				
			 createMessage(t.line, t.col,
			"parser.decisionTree.warning.nameNotAllowed",
			Message.WARNING, "Bezeichner nicht erlaubt");																				
		}
		if(mapNamedObjects.containsKey(id) && mapNamedObjects.get(id)!= null){
			for(Object namedObject : mapNamedObjects.get(id)){
				if(namedObject instanceof NamedObject){
					name = change(name);
					addMMInfo((NamedObject)namedObject,name,typ,content);	
				}
			}																			
		}
		else if(mapAnswers.containsKey(id) && mapAnswers.get(id) !=null) {
			for(Object answer : mapAnswers.get(id)){
			 	if(answer instanceof AnswerChoice){
					AnswerChoice a = (AnswerChoice)answer;
					setAnswerContent(a,content);
				}
			}
		}
		
	}

	void Declaration() {
		Expect(1);
	}

	Question Question(QASet q) {
		Question thisQ;
		Separator();
		int l = countDashes();
		
		Dashes();
		Declaration();
		thisQ = null;
		String d = "";
		String prompt = "";
		if(t.val.contains("~")){
		    String one = t.val.substring(0, t.val.indexOf("~")-1);
		    String two = t.val.substring(t.val.indexOf("~")+1);
		    if(one.startsWith("\"")) {
							one = one.substring(1);
						}
						if(two.endsWith("\"")) {
							two = two.substring(0,two.length()-1);
						}
		    d = formatDeclaration(one);
		    prompt = formatDeclaration(two);
		}else{
		    d = formatDeclaration(t.val);
		}
		
		switch (la.kind) {
		case 13: {
			thisQ = QuestionNum(d, q, l);
			break;
		}
		case 16: {
			thisQ = QuestionOC(d, q, l);
			break;
		}
		case 17: {
			thisQ = QuestionMC(d, q, l);
			break;
		}
		case 18: {
			thisQ = QuestionDate(d, q, l);
			break;
		}
		case 19: {
			thisQ = QuestionText(d, q, l);
			break;
		}
		case 20: case 21: {
			thisQ = QuestionYN(d, q, l);
			break;
		}
		case 22: {
			thisQ = QuestionZC(d, q, l);
			break;
		}
		default: SynErr(60); break;
		}
		while (properAmountOfDashes(l + 1)) {
			Question directChildQ = Question(thisQ);
		}
		if(!prompt.equals("") && !d.equals("")){
		   setPrompt(d, prompt);
		}
		
		return thisQ;
	}

	void Dashes() {
		Expect(4);
		while (la.kind == 4) {
			Get();
		}
	}

	Question QuestionNum(String d, QASet q, int l) {
		Question thisQ;
		Expect(13);
		String unit = null;
		
		if (la.kind == 14) {
			Get();
			Unit();
			unit = formatDeclaration(t.val);
			
			Expect(15);
		}
		Double intervalLeft = null;
		Double intervalRight = null;
		
		if (la.kind == 5) {
			Get();
			Expect(2);
			intervalLeft = Double.valueOf(t.val.replaceAll("," , "."));
			
			Expect(2);
			intervalRight = Double.valueOf(t.val.replaceAll("," , "."));
			
			Expect(6);
		}
		thisQ = handleQuestionNum(intervalLeft, intervalRight, d, unit, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerNum(thisQ);
		}
		return thisQ;
	}

	Question QuestionOC(String d, QASet q, int l) {
		Question thisQ;
		Expect(16);
		thisQ = handleQuestionOC(d, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerChoice(thisQ);
		}
		return thisQ;
	}

	Question QuestionMC(String d, QASet q, int l) {
		Question thisQ;
		Expect(17);
		thisQ = handleQuestionMC(d, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerChoice(thisQ);
		}
		return thisQ;
	}

	Question QuestionDate(String d, QASet q, int l) {
		Question thisQ;
		Expect(18);
		thisQ = handleQuestionDate(d, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerDate(thisQ);
		}
		return thisQ;
	}

	Question QuestionText(String d, QASet q, int l) {
		Question thisQ;
		Expect(19);
		thisQ = handleQuestionText(d, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerText(thisQ);
		}
		return thisQ;
	}

	Question QuestionYN(String d, QASet q, int l) {
		Question thisQ;
		if (la.kind == 20) {
			Get();
		} else if (la.kind == 21) {
			Get();
		} else SynErr(61);
		thisQ = handleQuestionYN(d, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerChoice(thisQ);
		}
		return thisQ;
	}

	Question QuestionZC(String d, QASet q, int l) {
		Question thisQ;
		Expect(22);
		thisQ = handleQuestionZC(d, q);
		// Begin Change Extension
		namedObject = thisQ;
		// End Change Extension
		
		while (la.kind == 10) {
			DescriptionIdQuestion();
		}
		if (la.kind == 49 || la.kind == 50) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		while (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerYN(thisQ);
		}
		return thisQ;
	}

	void Unit() {
		Expect(1);
	}

	void DescriptionIdQuestion() {
		Expect(10);
		String id = t.val.trim();
		abbr.add(id);
		ArrayList al = mapNamedObjects.get(id);
		if(al == null){
			al = new ArrayList();
		}
		al.add(namedObject);
		mapNamedObjects.put(id,al);
		
	}

	void IsAbstract(Question q) {
		if (la.kind == 49) {
			Get();
		} else if (la.kind == 50) {
			Get();
		} else SynErr(62);
		handleQuestionAbstract(q);
		
	}

	void QuestionIdentifier(Question q) {
		Expect(9);
		handleQuestionIdentifier(q, t.val);
		
	}

	void AnswerNum(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		TerminalCondition c = Comparison(q);
		conditionStack.push(c);
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (la.kind == 10) {
			DescriptionIdAnswer();
		}
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(qcl);
		handleQuestionIndication(ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		conditionStack.pop();
		
	}

	void AnswerChoice(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		Token answerChoiceToken = t;
		String answerChoiceId = kbm.findNewIDForAnswerChoice((QuestionChoice)q);
		
		if (la.kind == 9) {
			answerChoiceId = AnswerChoiceIdentifier();
		}
		TerminalCondition c  = handleAnswerChoice(q, answerChoiceToken, answerChoiceId);
		conditionStack.push(c);
		List qcl = new LinkedList();
		List ql = new LinkedList();
		
		while (la.kind == 10) {
			DescriptionIdAnswer();
		}
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(q, qcl, ql);
		}
		handleQContainerIndication(qcl);
		handleQuestionIndication(ql);
		conditionStack.pop();
		
	}

	void AnswerDate(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		TerminalCondition c = handleAnswerDate(q, t);
		conditionStack.push(c);
		
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (la.kind == 10) {
			DescriptionIdAnswer();
		}
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(qcl);
		handleQuestionIndication(ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		conditionStack.pop();
		
	}

	void AnswerText(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		TerminalCondition c = handleAnswerText(q, t);
		conditionStack.push(c);
		
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (la.kind == 10) {
			DescriptionIdAnswer();
		}
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(qcl);
		handleQuestionIndication(ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		conditionStack.pop();
		
	}

	void AnswerYN(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		Token answerChoiceToken = t;
		String declaration = formatDeclaration(answerChoiceToken.val);
		TerminalCondition c = null;
		if(declaration.equalsIgnoreCase("yes") || declaration.equalsIgnoreCase("ja")){
			c = handleAnswerYes(q);
		} else if(declaration.equalsIgnoreCase("no") || declaration.equalsIgnoreCase("nein")) {
			c = handleAnswerNo(q);
		} else if(declaration.equalsIgnoreCase("unknown") || declaration.equalsIgnoreCase("unbekannt")) {
			c = handleAnswerUnknown(q);
		} else {
			createMessage(t.line, t.col,
				"parser.decisionTree.error.wrongAnswerYNValue",
				Message.ERROR, t.val);
		}
		conditionStack.push(c);
		
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (la.kind == 10) {
			DescriptionIdAnswer();
		}
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(qcl);
		handleQuestionIndication(ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
			
		conditionStack.pop();
		
	}

	TerminalCondition Comparison(Question q) {
		TerminalCondition c;
		c = null;
		
		switch (la.kind) {
		case 23: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumLess((QuestionNum)q, answerValue);
			
			break;
		}
		case 24: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumLessEqual((QuestionNum)q, answerValue);																			
			
			break;
		}
		case 25: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumGreater((QuestionNum)q, answerValue);																				
			
			break;
		}
		case 26: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumGreaterEqual((QuestionNum)q, answerValue);																				
			
			break;
		}
		case 27: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumEqual((QuestionNum)q, answerValue);																				
			
			break;
		}
		case 7: {
			Get();
			Expect(2);
			Token l = t;
			
			Expect(2);
			Token r = t;
			handleComparison(q, l, r);
			double intervalLeft = Double.parseDouble(l.val.replaceAll("," , "."));
			double intervalRight = Double.parseDouble(r.val.replaceAll("," , "."));																				
			c = new CondNumIn((QuestionNum)q, intervalLeft, intervalRight);																				
			
			Expect(8);
			break;
		}
		default: SynErr(63); break;
		}
		return c;
	}

	void DescriptionIdAnswer() {
		Expect(10);
		String id = t.val.trim();
		abbr.add(id);
		ArrayList al = mapAnswers.get(id);
		if(al == null){
			al = new ArrayList(); 
		}
		al.add(answer);
		mapAnswers.put(id,al);
		
	}

	void Indication(Question qParent, List qcl, List ql) {
		if (isFirstNonDeclarationToken("[", t)) {
			Question q = Question(qParent);
			ql.add(q);
			
		} else if (isFirstNonDeclarationToken("(", t)) {
			Diagnosis();
		} else if (la.kind == 3) {
			QContainerIndication(qcl);
		} else SynErr(64);
	}

	String AnswerChoiceIdentifier() {
		String answerChoiceId;
		Expect(9);
		answerChoiceId = handleAnswerChoiceIdentifier(t.val);
		
		return answerChoiceId;
	}

	void Diagnosis() {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		Token declarationToken = t;
		
		Expect(5);
		IndicationStrength();
		Token indicationStrengthToken = t;
		Diagnosis d = handleDiagnosisIndication(declarationToken, indicationStrengthToken);
		// Begin Change Extension
		namedObject = d; 
		// End Change Extension
		
		
		Expect(6);
		List qcl = new LinkedList();
		
		while (la.kind == 10) {
			DescriptionIdDiagnosis();
		}
		while (la.kind == 7) {
			Get();
			Link();
			Expect(8);
		}
		while (properAmountOfDashes(countDashes + 1)) {
			QContainerIndicationByDiagnosis(qcl);
		}
		handleQContainerIndicationByDiagnosis(d, indicationStrengthToken, qcl);
		
	}

	void QContainerIndication(List qcl) {
		Separator();
		Dashes();
		Declaration();
		handleQContainerAccumulation(t, qcl);
		
	}

	void IndicationStrength() {
		switch (la.kind) {
		case 28: {
			Get();
			break;
		}
		case 29: {
			Get();
			break;
		}
		case 30: {
			Get();
			break;
		}
		case 31: {
			Get();
			break;
		}
		case 32: {
			Get();
			break;
		}
		case 33: {
			Get();
			t.val = "N5x";
			
			break;
		}
		case 34: {
			Get();
			break;
		}
		case 35: {
			Get();
			break;
		}
		case 36: {
			Get();
			break;
		}
		case 37: {
			Get();
			break;
		}
		case 38: {
			Get();
			break;
		}
		case 39: {
			Get();
			break;
		}
		case 40: {
			Get();
			break;
		}
		case 41: {
			Get();
			t.val = "P5x";
			
			break;
		}
		case 42: {
			Get();
			break;
		}
		case 43: {
			Get();
			break;
		}
		case 44: {
			Get();
			t.val = "P1";
			
			break;
		}
		case 45: {
			Get();
			t.val = "P4";
			
			break;
		}
		case 46: {
			Get();
			t.val = "P7";
			
			break;
		}
		case 47: {
			Get();
			t.val = "P7";
			
			break;
		}
		case 48: {
			Get();
			t.val = "P3";
			
			break;
		}
		case 2: {
			Get();
			break;
		}
		case 1: {
			Declaration();
			break;
		}
		case 4: {
			Get();
			t.val = "N1";
			
			if (la.kind == 4) {
				Get();
				t.val = "N4";
				
				if (la.kind == 4) {
					Get();
					t.val = "N7";
					
				}
			}
			break;
		}
		default: SynErr(65); break;
		}
	}

	void DescriptionIdDiagnosis() {
		Expect(10);
		String id = t.val.trim(); 
		abbr.add(id);
		ArrayList al = mapNamedObjects.get(id);
		if(al == null){
			al = new ArrayList();
		}
		al.add(namedObject);
		mapNamedObjects.put(id,al);
		
	}

	void Link() {
		Expect(7);
		Expect(1);
		String url = t.val;
		String typ = "url";
		String decl = ""; 
		
		while (la.kind == 27) {
			Get();
			Expect(1);
			url = url.concat("=" + t.val);
			
		}
		Expect(8);
		while (la.kind == 7) {
			Get();
			Expect(1);
			decl = t.val; 
			decl = change(decl);
			
			Expect(8);
		}
		if(!savedLinks.contains(namedObject)){
		savedLinks.add(namedObject);
		}
		addMMInfo(namedObject,decl,typ,url); 
		
	}

	void QContainerIndicationByDiagnosis(List qcl) {
		Separator();
		Dashes();
		Declaration();
		handleQContainerAccumulation(t, qcl);
		
	}

	void DescriptionID() {
		Expect(10);
	}

	void Typ() {
		Expect(1);
	}

	void Name() {
		Expect(1);
	}

	void Content() {
		switch (la.kind) {
		case 3: case 4: case 5: case 6: case 7: case 8: case 14: case 15: case 23: case 25: case 27: case 51: case 52: case 53: case 54: case 55: {
			switch (la.kind) {
			case 4: {
				Get();
				break;
			}
			case 27: {
				Get();
				break;
			}
			case 3: {
				Get();
				break;
			}
			case 23: {
				Get();
				break;
			}
			case 25: {
				Get();
				break;
			}
			case 14: {
				Get();
				break;
			}
			case 15: {
				Get();
				break;
			}
			case 7: {
				Get();
				break;
			}
			case 8: {
				Get();
				break;
			}
			case 5: {
				Get();
				break;
			}
			case 6: {
				Get();
				break;
			}
			case 51: {
				Get();
				break;
			}
			case 52: {
				Get();
				break;
			}
			case 53: {
				Get();
				break;
			}
			case 54: {
				Get();
				break;
			}
			case 55: {
				Get();
				break;
			}
			}
			break;
		}
		case 10: {
			Get();
			break;
		}
		case 9: {
			Get();
			break;
		}
		case 1: {
			Get();
			break;
		}
		case 56: {
			Get();
			break;
		}
		case 2: {
			Get();
			break;
		}
		default: SynErr(66); break;
		}
		content = content.concat(t.val); 
		
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		DecisionTree();

		Expect(0);
	}

	private boolean[][] set = {
		{T,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{T,x,x,T, x,x,x,x, x,x,x,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,T, T,T,T,T, T,T,T,x, x,x,T,T, x,x,x,x, x,x,x,T, x,T,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,x,x}

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
			case 1: s = "declaration expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "newline expected"; break;
			case 4: s = "dash expected"; break;
			case 5: s = "parenthesisOpen expected"; break;
			case 6: s = "parenthesisClose expected"; break;
			case 7: s = "bracketOpen expected"; break;
			case 8: s = "bracketClose expected"; break;
			case 9: s = "ID expected"; break;
			case 10: s = "descriptionId expected"; break;
			case 11: s = "allowedNames expected"; break;
			case 12: s = "\"|\" expected"; break;
			case 13: s = "\"[num]\" expected"; break;
			case 14: s = "\"{\" expected"; break;
			case 15: s = "\"}\" expected"; break;
			case 16: s = "\"[oc]\" expected"; break;
			case 17: s = "\"[mc]\" expected"; break;
			case 18: s = "\"[date]\" expected"; break;
			case 19: s = "\"[text]\" expected"; break;
			case 20: s = "\"[jn]\" expected"; break;
			case 21: s = "\"[yn]\" expected"; break;
			case 22: s = "\"[info]\" expected"; break;
			case 23: s = "\"<\" expected"; break;
			case 24: s = "\"<=\" expected"; break;
			case 25: s = "\">\" expected"; break;
			case 26: s = "\">=\" expected"; break;
			case 27: s = "\"=\" expected"; break;
			case 28: s = "\"N1\" expected"; break;
			case 29: s = "\"N2\" expected"; break;
			case 30: s = "\"N3\" expected"; break;
			case 31: s = "\"N4\" expected"; break;
			case 32: s = "\"N5\" expected"; break;
			case 33: s = "\"N5+\" expected"; break;
			case 34: s = "\"N6\" expected"; break;
			case 35: s = "\"N7\" expected"; break;
			case 36: s = "\"P1\" expected"; break;
			case 37: s = "\"P2\" expected"; break;
			case 38: s = "\"P3\" expected"; break;
			case 39: s = "\"P4\" expected"; break;
			case 40: s = "\"P5\" expected"; break;
			case 41: s = "\"P5+\" expected"; break;
			case 42: s = "\"P6\" expected"; break;
			case 43: s = "\"P7\" expected"; break;
			case 44: s = "\"+\" expected"; break;
			case 45: s = "\"++\" expected"; break;
			case 46: s = "\"+++\" expected"; break;
			case 47: s = "\"!\" expected"; break;
			case 48: s = "\"?\" expected"; break;
			case 49: s = "\"<abstrakt>\" expected"; break;
			case 50: s = "\"<abstract>\" expected"; break;
			case 51: s = "\"#\" expected"; break;
			case 52: s = "\"@\" expected"; break;
			case 53: s = "\"\\\'\" expected"; break;
			case 54: s = "\"\\\\\" expected"; break;
			case 55: s = "\"\\\"\" expected"; break;
			case 56: s = "\"/\" expected"; break;
			case 57: s = "??? expected"; break;
			case 58: s = "invalid DecisionTree"; break;
			case 59: s = "this symbol not expected in Separator"; break;
			case 60: s = "invalid Question"; break;
			case 61: s = "invalid QuestionYN"; break;
			case 62: s = "invalid IsAbstract"; break;
			case 63: s = "invalid Comparison"; break;
			case 64: s = "invalid Indication"; break;
			case 65: s = "invalid IndicationStrength"; break;
			case 66: s = "invalid Content"; break;
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
	public void Error (Message m) {	
      
        // COCO: not in Coco
		messages.add(m);
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

