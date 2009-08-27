package de.d3web.textParser.Utils;

import java.util.List;

import de.d3web.kernel.domainModel.Score;
import de.d3web.report.Message;

public class ConceptNotInKBError extends Message {

	/**
	 * key of the message 
	 */
	protected String key;
	/**
	 * name of the Object 
	 */
	protected String objectName;
	
	public ConceptNotInKBError(String messageType, String messageText, String file, int lineNo, int columnNo, String line) {
		super(messageType, messageText, file, lineNo, columnNo,line);
	}
	
	public ConceptNotInKBError(String messageType, String file, int lineNo, String line) {
		super(messageType, file, lineNo, line);
	}
	
	public ConceptNotInKBError(String result) {
		super(result);
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public static Message buildMessageWithKeyObject(String messageType, String messageText, String file, int lineNo, int columnNo, String line, String key,String object) {
		ConceptNotInKBError m = new ConceptNotInKBError(messageType,messageText,file,lineNo, columnNo, line);
		m.setKey(key);
		m.setObjectName(object);
		return m;
	}
	
	public static boolean isValidScore(String s) {
		List l  = Score.getAllScores();
		for (Object score : l) {
			if(s.equals(((Score)score).getSymbol())) {
				return true;
			}
		}
		return false;
	}
	

}
