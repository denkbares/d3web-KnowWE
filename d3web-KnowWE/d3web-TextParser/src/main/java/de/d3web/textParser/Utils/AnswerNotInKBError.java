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
