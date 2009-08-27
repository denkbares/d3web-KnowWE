package de.d3web.textParser.Utils;

import de.d3web.kernel.domainModel.qasets.Question;

public class AnswerNotInKBError extends ConceptNotInKBError {

	private Question q;
	
	public AnswerNotInKBError(String messageType, String messageText, String file, int lineNo, int columnNo, String line) {
		super(messageType, messageText, file, lineNo, columnNo,line);
	}
	
	public AnswerNotInKBError(String messageType, String messageText, String file, int lineNo, int columnNo, String line, Question q) {
		super(messageType, messageText, file, lineNo, columnNo,line);
		this.q = q;
	}
	
	public AnswerNotInKBError(String messageText) {
		super(messageText);
	}
	
	public AnswerNotInKBError(String messageText, String datei, int line, String mark, Question q) {
		super(messageText, datei, line, mark);
		this.q = q;
	}
	
	public AnswerNotInKBError(String messageText,Question q) {
		super(messageText);
		this.q = q;
	}

	public Question getQ() {
		return q;
	}
	
	public void setQuestion(Question q) {
		this.q = q;
	}
}
