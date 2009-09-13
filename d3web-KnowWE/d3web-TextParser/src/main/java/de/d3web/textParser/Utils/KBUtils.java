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

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.QuestionYN;

public class KBUtils {
	/**
	 * Searches for the AnswerChoice of a YN-question
	 * This method is provided because YN-questions may have user-defined answer names
	 * instead of the standard answers YES or NO.
	 * @param question the question
	 * @param answerName answer name to search for
	 * @return the AnswerChoice represented by the given answer name
	 */
	public static AnswerChoice findAnswerYN(KnowledgeBaseManagement kbm, QuestionYN question, String answerName) {
		if (answerName.equals("yes") || answerName.equals("ja") || answerName.equals("true"))
			return kbm.findAnswerChoice(question, "Yes");
		else if (answerName.equals("no") || answerName.equals("nein") || answerName.equals("false"))
			return kbm.findAnswerChoice(question, "No");
		else
			return null;
	}
	
	/**
	 * Searches for a NamedObject with text <CODE>objectName</CODE>.
	 * Searches QContainers, Questions and Diagnoses.
	 * @param objectName name of the object to search for
	 * @return NamedObject with given text
	 */
	public static NamedObject findNamedObject(KnowledgeBaseManagement kbm, String objectName) {
		try {
			NamedObject object = kbm.findQContainer(objectName);
			if (object==null) object = kbm.findQuestion(objectName);
			if (object==null) object = kbm.findDiagnosis(objectName);
			return object;
		}
		catch (NullPointerException n) { return null; }
	}
}
