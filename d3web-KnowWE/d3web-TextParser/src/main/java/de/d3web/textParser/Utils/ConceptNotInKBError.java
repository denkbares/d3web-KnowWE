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
