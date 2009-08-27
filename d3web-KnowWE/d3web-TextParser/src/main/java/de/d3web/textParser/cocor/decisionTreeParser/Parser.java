package de.d3web.textParser.cocor.decisionTreeParser;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NumericalInterval;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerFactory;
import de.d3web.kernel.domainModel.answers.AnswerText;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
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
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;
import de.d3web.kernel.psMethods.nextQASet.PSMethodNextQASet;
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
	static final int maxT = 45;
	static final int _comment = 46;

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
	
    private static final ResourceBundle rb = ResourceBundle.getBundle("properties.textParser");
    
    /**
     * Setzt die Zï¿½hler fï¿½r die geparsten Elemente zurï¿½ck
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
     * @param key Schlï¿½ssel der Meldung
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
        
        String globalErrorKey = null;
        String objectName = null;
        if(key.equals(KEY_DIAGNOSIS_NOT_FOUND)) {
        	globalErrorKey = MessageGenerator.KEY_INVALID_DIAGNOSIS;
        	objectName = (String)values[0];
        }
        errors.Error(row, column, result, messageType,globalErrorKey, objectName );        
    
    }
	
    /**
     * Fï¿½gt eine Erfolgsmeldung zur Liste hinzu (pro Frageklasse)
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
     * Formatiert den Bezeichner einer Frageklasse, entfernen von Whitespace am Anfang und Ende sowie der Anfï¿½hrungszeichen
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
     * ï¿½berprï¿½ft, ob das Ende der zu parsenden Datei erreicht ist
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
     * ï¿½berprï¿½ft, ob in der nï¿½chsten Zeile eine Frage der Ebene 1 (1 Dash) steht, also direkt in einer Frageklasse enthalten ist.
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
     * ï¿½berprï¿½ft, ob die nï¿½chste Zeile eine bestimmte Anzahl an Anfï¿½hrungsstrichen vorweisen kann.
     * @param dashes Anzahl der Dashes, auf die in der nï¿½chsten Zeile ï¿½berprï¿½ft werden soll
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
     * zï¿½hlt die Anfï¿½hrungsstriche der nï¿½chsten Zeile
     * @return Anzahl der Anfï¿½hrungsstriche der nï¿½chsten Zeile
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
	 * ï¿½berprï¿½ft, ob der String c der Anfang des Tokens ist, der nach dem gerade betrachteten Declaration-Tokens kommt.
	 * Es geht darum zu ï¿½berprï¿½fen, ob in der gleichen Zeile der Declaration noch ein Token kommt, der z.B. auf einen Fragetyp hinweist. 
	 * @param c der zu suchende String
	 * @param t der aktuelle Token
	 * @return true: der String c wurde als Anfang des nï¿½chsten Tokens gefunden, ansonsten false
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
		boolean result = (t.kind==_declaration && sp.val.startsWith(c));
		return result;
	}
	
	/**
	 * "handle"-Methoden sind ausgelagerte Methoden, um die syntaktische EBNF im *.atg-File klarer von den semantischen Aktionen zu trennen.
	 * Dies ist eine Handle-Methode fï¿½r die semantische Behandlung einer Frageklassen-Definition.
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
	private Question handleQuestionNum(Double intervalLeft, Double intervalRight, String d, String unit, QASet q) {
		QuestionNum currentQuestion = kbm.createQuestionNum(d, q);               
        if (unit != null) currentQuestion.getProperties().setProperty(Property.UNIT, unit);
        if (intervalLeft != null && intervalRight != null) {
	        NumericalInterval range = new NumericalInterval(intervalLeft, intervalRight);
			currentQuestion.getProperties().setProperty(Property.QUESTION_NUM_RANGE, range);
		}
		this.numberOfInsertedQuestions++;
        return currentQuestion;
	}
	
	/**
	 * @param d Name der OC-Frage
	 * @param q Vater-QASet
	 * @return die erzeugte Frage
	 */
	private Question handleQuestionOC(String d, QASet q) {
		Question currentQuestion = kbm.createQuestionOC(d, q, new AnswerChoice[] {});
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * @param d Declaration der MC-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionMC(String d, QASet q) {
		Question currentQuestion = kbm.createQuestionMC(d, q, new AnswerChoice[] {});
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * wird wohl erstmal nicht gebraucht, die gesamte QuestionDate-Behandlung wird auch von knowme noch nicht unterstï¿½tzt
	 * @param d Declaration der Date-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionDate(String d, QASet q) {
		Question currentQuestion = kbm.createQuestionDate(d, q);
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * @param d Declaration der Text-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionText(String d, QASet q) {
		Question currentQuestion = kbm.createQuestionText(d, q);
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * @param d Declaration der YN-Frage
	 * @param q Vater-QASet
	 * @return erzeugte Frage
	 */
	private Question handleQuestionYN(String d, QASet q) {
		Question currentQuestion = kbm.createQuestionYN(d, q);
		this.numberOfInsertedQuestions++;
		return currentQuestion;
	}
	
	/**
	 * handled fï¿½r AnswerNum den Comparator; anstatt mï¿½glicher Methode "handleAnswerNum()".
	 * Behandelt Vergleiche mit nur einem numerischen Wert als Antwort
	 * @param q die Frage, fï¿½r die der Vergleich durchgefï¿½hrt werden soll
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
	 * handled fï¿½r AnswerNum den Comparator; anstatt mï¿½glicher Methode "handleAnswerNum()".
	 * Behandelt Vergleiche mit zwei numerischen Werten als Antwort-Berich (Intervall)
	 * @param q die Frage, fï¿½r die der Vergleich durchgefï¿½hrt werden soll
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
	 * @return AbstractCondition, welche in die KB eingefï¿½gt wurde. null, wenn ein Fehler auftrat.
	 */
	private AbstractCondition handleAnswerChoice(Question q, Token t, String id) {
		AnswerChoice acSearch = kbm.findAnswerChoice((QuestionChoice)q, id);
		if (acSearch == null) {
			String declaration = formatDeclaration(t.val);
			AnswerChoice answerChoice = AnswerFactory.createAnswerChoice(id, declaration);
			answerChoice.setQuestion(q);
			QuestionChoice qc = (QuestionChoice)q;
			qc.addAlternative(answerChoice);
			CondEqual c = new CondEqual((QuestionChoice)q, answerChoice);
			return c;
		} else {
			createMessage(t.line, t.col, "parser.decisionTree.error.ChoiceAnswerIdAlreadyExists", Message.ERROR, q.getText(), id);
			return null;
		}
	}
	
	/**
	 * wird wohl erstmal nicht gebraucht, die gesamte QuestionDate-Behandlung wird auch von knowme noch nicht unterstï¿½tzt
	 * @param q Frage
	 * @param t Token der AnswerDate
	 * @return AbstractCondition, welche in die KB eingefï¿½gt wurde
	 */
	private AbstractCondition handleAnswerDate(Question q, Token t) {
//		String declaration = formatDeclaration(t.val);
//		AnswerDate answerDate = new AnswerDate();
//		//COCO: todo!
//		//TODO: todo!
//		DateFormat d = new SimpleDateFormat();
//		answerDate.setValue(d.parse(declaration));
//		answerDate.setQuestion(q);
//		// diese Methode gibts noch nicht
//		CondEqual c = new CondEqual(q, answerDate);
		return null;
	}
	
	/**
	 * @param q Frage
	 * @param t Token der AnswerText
	 * @return AbstractCondition, welche in die KB eingefï¿½gt wurde
	 */
	private AbstractCondition handleAnswerText(Question q, Token t) {
		String declaration = formatDeclaration(t.val);
		AnswerText answerText = new AnswerText();
		answerText.setText(declaration);
		answerText.setQuestion(q);
		CondTextEqual c = new CondTextEqual((QuestionText)q, declaration);
		return c;
	}
	
	/**
	 * @param q Frage
	 * @param declaration Declaration der AnswerYes
	 * @return AbstractCondition, welche in die KB eingefï¿½gt wurde
	 */
	private AbstractCondition handleAnswerYes(Question q) {
		CondChoiceYes c = new CondChoiceYes((QuestionYN)q);
		return c;
	}
	
	/**
	 * @param q Frage
	 * @param declaration Declaration der AnswerNo
	 * @return AbstractCondition, welche in die KB eingefï¿½gt wurde
	 */
	private AbstractCondition handleAnswerNo(Question q) {
		CondChoiceNo c = new CondChoiceNo((QuestionYN)q);
		return c;
	}
	
	/**
	 * @param q Frage
	 * @param declaration Declaration der AnswerUnknown
	 * @return AbstractCondition, welche in die KB eingefï¿½gt wurde
	 */
	private AbstractCondition handleAnswerUnknown(Question q) {
		CondUnknown c = new CondUnknown(q);
		return c;
	}
	
	/**
	 * @param c Kondition
	 * @param ql Frageliste, die mit dem Feuern der Kondition indiziert wird
	 */
	private void handleQuestionIndication(AbstractCondition c, List ql) {
	    if(c != null ) {
	    	if(ql.size() > 0) {
		        String newRuleID = kbm.findNewIDFor(new RuleComplex()); 
		        RuleFactory.createIndicationRule(newRuleID, ql, c);
		    }
	    } else {
			createMessage(t.line, t.col,
					"parser.decisionTree.error.conditionNull",
					Message.ERROR, t.val);
	    }
	    this.numberOfInsertedFollowingQuestions++;
	}
	
	/**
	 * @param c Kondition
	 * @param t Token der Frageklasse, die mit dem Feuern der Kondition indiziert wird
	 */
	private void handleQContainerIndication(AbstractCondition c, List qcl) {
		if(qcl.size() > 0) {
            String newRuleID = kbm.findNewIDFor(new RuleComplex());
            RuleFactory.createIndicationRule(newRuleID, qcl, c);
		    this.numberOfInsertedQContainerIndications += qcl.size();
		}
	}
	
	private static String KEY_DIAGNOSIS_NOT_FOUND = "parser.decisionTree.error.diagnosisNotFound";
	
	
	/**
	 * @param c Kondition 
	 * @param declarationToken Declaration der Diagnose, welche mit dem Feuern der Kondition indiziert wird
	 * @param indicationStrengthToken Token, der die Indikations-Stï¿½rke reprï¿½sentiert
	 * @return die gefundene Diagnose
	 */
	private Diagnosis handleDiagnosisIndication(AbstractCondition c, Token declarationToken, Token indicationStrengthToken){
		String declaration = formatDeclaration(declarationToken.val);
		Diagnosis diagnosis = kbm.findDiagnosis(declaration);
		String indicationStrength = formatDeclaration(indicationStrengthToken.val);
        if(diagnosis != null) {
            String newRuleID = kbm.findNewIDFor(new RuleComplex()); 
	        RuleFactory.createHeuristicPSRule(newRuleID, diagnosis, ScoreFinder.getScore(indicationStrength), c);
            //numberOfCreatedDiagnosisRules++;
        } else {
			createMessage(t.line, t.col,
					KEY_DIAGNOSIS_NOT_FOUND,
					Message.ERROR, declaration);
	    }
	    this.numberOfInsertedDiagnosisDerivations++;
	    return diagnosis;
	}
	
	/**
	 * Hier wird einer Liste von Frageklassen eine weitere Frageklasse hinzugefï¿½gt.
	 * @param declarationToken Declaration des QContainers (Frageklasse)
	 * @param qcl Frageklassen-Liste
	 */
	private void handleQContainerAccumulation(Token declarationToken, List qcl) {
		String declaration = formatDeclaration(declarationToken.val);
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
	 * @param indicationStrengthToken Token der Indikations-Stï¿½rke
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
				// Es muï¿½ ein Fehler geworfen werden, da die Listen nicht ï¿½bereinstimmen!
				} else {
					boolean bool1 = existingQcl.containsAll(qcl);
					boolean bool2 = qcl.containsAll(existingQcl);
					if ( ! (bool1 && bool2)) {
						createMessage(t.line, t.col, "parser.decisionTree.error.differentQCLists", Message.ERROR, d.getText());
					} // else: Die Regel muss nicht erzeugt werden, da die Liste der QContainer ï¿½bereinstimmt und bereits eine Regel existiert
				}
			} else {
				createMessage(t.line, t.col,
						"parser.decisionTree.error.diagnosisNotFound",
						Message.ERROR, d.getText());
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
		if (errDist >= minErrDist) errors.Error(t.line, t.col, msg, Message.ERROR, null, null);
		// COCO end change
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) { ++errDist; break; }

			if (la.kind == 46) {
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
			QContainerDefinition();
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
		while (!(la.kind == 0 || la.kind == 3)) {SynErr(46); Get();}
		Expect(3);
		while (la.kind == 3) {
			Get();
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
		String d = formatDeclaration(t.val);
		
		switch (la.kind) {
		case 10: {
			thisQ = QuestionNum(d, q, l);
			break;
		}
		case 13: {
			thisQ = QuestionOC(d, q, l);
			break;
		}
		case 14: {
			thisQ = QuestionMC(d, q, l);
			break;
		}
		case 15: {
			thisQ = QuestionDate(d, q, l);
			break;
		}
		case 16: {
			thisQ = QuestionText(d, q, l);
			break;
		}
		case 17: case 18: {
			thisQ = QuestionYN(d, q, l);
			break;
		}
		default: SynErr(47); break;
		}
		while (properAmountOfDashes(l + 1)) {
			Question directChildQ = Question(thisQ);
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
		Expect(10);
		String unit = null;
		
		if (la.kind == 11) {
			Get();
			Unit();
			unit = formatDeclaration(t.val);
			
			Expect(12);
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
		
		if (la.kind == 43 || la.kind == 44) {
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
		Expect(13);
		thisQ = handleQuestionOC(d, q);
		
		if (la.kind == 43 || la.kind == 44) {
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
		Expect(14);
		thisQ = handleQuestionMC(d, q);
		
		if (la.kind == 43 || la.kind == 44) {
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
		Expect(15);
		thisQ = handleQuestionDate(d, q);
		
		if (la.kind == 43 || la.kind == 44) {
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
		Expect(16);
		thisQ = handleQuestionText(d, q);
		
		if (la.kind == 43 || la.kind == 44) {
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
		if (la.kind == 17) {
			Get();
		} else if (la.kind == 18) {
			Get();
		} else SynErr(48);
		thisQ = handleQuestionYN(d, q);
		
		if (la.kind == 43 || la.kind == 44) {
			IsAbstract(thisQ);
		}
		if (la.kind == 9) {
			QuestionIdentifier(thisQ);
		}
		if (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerYN(thisQ);
		}
		if (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerYN(thisQ);
		}
		if (properAmountOfDashes(l + 1) && !isFirstNonDeclarationToken("[", t)) {
			AnswerYN(thisQ);
		}
		return thisQ;
	}

	void Unit() {
		Expect(1);
	}

	void IsAbstract(Question q) {
		if (la.kind == 43) {
			Get();
		} else if (la.kind == 44) {
			Get();
		} else SynErr(49);
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
		AbstractCondition c = Comparison(q);
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(c, q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(c, qcl);
		handleQuestionIndication(c, ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		
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
		AbstractCondition c  = handleAnswerChoice(q, answerChoiceToken, answerChoiceId);
		List qcl = new LinkedList();
		List ql = new LinkedList();
		
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(c, q, qcl, ql);
		}
		handleQContainerIndication(c, qcl);
		handleQuestionIndication(c, ql);
		
	}

	void AnswerDate(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		AbstractCondition c = handleAnswerDate(q, t);
		
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(c, q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(c, qcl);
		handleQuestionIndication(c, ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		
	}

	void AnswerText(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		AbstractCondition c = handleAnswerText(q, t);
		
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(c, q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(c, qcl);
		handleQuestionIndication(c, ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		
	}

	void AnswerYN(Question q) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		Token answerChoiceToken = t;
		String declaration = formatDeclaration(answerChoiceToken.val);
		AbstractCondition c = null;
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
		
		List qcl = new LinkedList();
		List ql = new LinkedList();
		boolean hasIndication = false;
		
		while (properAmountOfDashes(countDashes + 1)) {
			Indication(c, q, qcl, ql);
			hasIndication = true;
			
		}
		handleQContainerIndication(c, qcl);
		handleQuestionIndication(c, ql);
		if (!hasIndication) createMessage(t.line, t.col,
			"parser.decisionTree.warning.answerNotImportable",
			Message.WARNING, q.getText());
		
	}

	AbstractCondition Comparison(Question q) {
		AbstractCondition c;
		c = null;
		
		switch (la.kind) {
		case 19: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumLess((QuestionNum)q, answerValue);
			
			break;
		}
		case 20: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumLessEqual((QuestionNum)q, answerValue);																			
			
			break;
		}
		case 21: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumGreater((QuestionNum)q, answerValue);																				
			
			break;
		}
		case 22: {
			Get();
			Expect(2);
			handleComparison(q, t);
			double answerValue = Double.parseDouble(t.val.replaceAll("," , "."));
			c = new CondNumGreaterEqual((QuestionNum)q, answerValue);																				
			
			break;
		}
		case 23: {
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
		default: SynErr(50); break;
		}
		return c;
	}

	void Indication(AbstractCondition c, Question qParent, List qcl, List ql) {
		if (isFirstNonDeclarationToken("[", t)) {
			Question q = Question(qParent);
			ql.add(q);
			
		} else if (isFirstNonDeclarationToken("(", t)) {
			Diagnosis(c);
		} else if (la.kind == 3) {
			QContainerIndication(qcl);
		} else SynErr(51);
	}

	String AnswerChoiceIdentifier() {
		String answerChoiceId;
		Expect(9);
		answerChoiceId = handleAnswerChoiceIdentifier(t.val);
		
		return answerChoiceId;
	}

	void Diagnosis(AbstractCondition c) {
		int countDashes = countDashes();
		
		Separator();
		Dashes();
		Declaration();
		Token declarationToken = t;
		
		Expect(5);
		IndicationStrength();
		Token indicationStrengthToken = t;
		Diagnosis d = handleDiagnosisIndication(c, declarationToken, indicationStrengthToken);
		
		Expect(6);
		List qcl = new LinkedList();
		
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
		case 24: {
			Get();
			break;
		}
		case 25: {
			Get();
			break;
		}
		case 26: {
			Get();
			break;
		}
		case 27: {
			Get();
			break;
		}
		case 28: {
			Get();
			break;
		}
		case 29: {
			Get();
			t.val = "N5x";
			
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
			t.val = "P5x";
			
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
			t.val = "P1";
			
			break;
		}
		case 41: {
			Get();
			t.val = "P4";
			
			break;
		}
		case 42: {
			Get();
			t.val = "P7";
			
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
		default: SynErr(52); break;
		}
	}

	void QContainerIndicationByDiagnosis(List qcl) {
		Separator();
		Dashes();
		Declaration();
		handleQContainerAccumulation(t, qcl);
		
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		DecisionTree();

		Expect(0);
	}

	private boolean[][] set = {
		{T,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x}

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
	private void addMsg(int line, int column, String msg, String messageType,String key, String objectName) {
        // COCO: es fehlt noch die Angabe der Spaltennnummer und des Filenamen 
		Message message = ConceptNotInKBError.buildMessageWithKeyObject(messageType, msg, this.filename, line, column, "",key,objectName);
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
			case 10: s = "\"[num]\" expected"; break;
			case 11: s = "\"{\" expected"; break;
			case 12: s = "\"}\" expected"; break;
			case 13: s = "\"[oc]\" expected"; break;
			case 14: s = "\"[mc]\" expected"; break;
			case 15: s = "\"[date]\" expected"; break;
			case 16: s = "\"[text]\" expected"; break;
			case 17: s = "\"[jn]\" expected"; break;
			case 18: s = "\"[yn]\" expected"; break;
			case 19: s = "\"<\" expected"; break;
			case 20: s = "\"<=\" expected"; break;
			case 21: s = "\">\" expected"; break;
			case 22: s = "\">=\" expected"; break;
			case 23: s = "\"=\" expected"; break;
			case 24: s = "\"N1\" expected"; break;
			case 25: s = "\"N2\" expected"; break;
			case 26: s = "\"N3\" expected"; break;
			case 27: s = "\"N4\" expected"; break;
			case 28: s = "\"N5\" expected"; break;
			case 29: s = "\"N5+\" expected"; break;
			case 30: s = "\"N6\" expected"; break;
			case 31: s = "\"N7\" expected"; break;
			case 32: s = "\"P1\" expected"; break;
			case 33: s = "\"P2\" expected"; break;
			case 34: s = "\"P3\" expected"; break;
			case 35: s = "\"P4\" expected"; break;
			case 36: s = "\"P5\" expected"; break;
			case 37: s = "\"P5+\" expected"; break;
			case 38: s = "\"P6\" expected"; break;
			case 39: s = "\"P7\" expected"; break;
			case 40: s = "\"+\" expected"; break;
			case 41: s = "\"++\" expected"; break;
			case 42: s = "\"+++\" expected"; break;
			case 43: s = "\"<abstrakt>\" expected"; break;
			case 44: s = "\"<abstract>\" expected"; break;
			case 45: s = "??? expected"; break;
			case 46: s = "this symbol not expected in Separator"; break;
			case 47: s = "invalid Question"; break;
			case 48: s = "invalid QuestionYN"; break;
			case 49: s = "invalid IsAbstract"; break;
			case 50: s = "invalid Comparison"; break;
			case 51: s = "invalid Indication"; break;
			case 52: s = "invalid IndicationStrength"; break;
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
	        addMsg(line, col, result, Message.ERROR,null,null);
			// COCO end change

			count++;
	}
	
	// COCO delete
	// hier wurde die Funktion SemErr() gelï¿½scht, da sie nicht benï¿½tigt wird.

	// COCO begin change
	public void Error (int line, int col, String s, String messageType, String messageKey, String objectName) {	
       
        // COCO: not in Coco
		addMsg(line, col, s, messageType, messageKey, objectName);
		count++;
	}
	// COCO end change
	
	// COCO delete
	// hier wurde die Funktion Exception() gelï¿½scht, da sie nicht benï¿½tigt wird.
	
    // COCO begin add
    public List<Message> getMessages() {
        return messages;      
    }
	// COCO end add

} // Errors

