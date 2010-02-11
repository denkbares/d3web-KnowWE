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

package de.d3web.KnOfficeParser.util;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.core.terminology.QASet;
import de.d3web.core.terminology.Question;
import de.d3web.core.terminology.QuestionDate;
import de.d3web.core.terminology.QuestionMC;
import de.d3web.core.terminology.QuestionNum;
import de.d3web.core.terminology.QuestionOC;
import de.d3web.core.terminology.QuestionSolution;
import de.d3web.core.terminology.QuestionText;
import de.d3web.core.terminology.QuestionYN;
import de.d3web.core.terminology.QuestionZC;


public class D3webQuestionFactory {
	public static Question createQuestion(String name, String type, IDObjectManagement idom) {
		return createQuestion(idom, idom.getKnowledgeBase().getRootQASet(), name, type);
	}
	
	public static Question createQuestion(IDObjectManagement idom, QASet parent, String name, String type) {
		Question q;
		if (type.equalsIgnoreCase("oc")) {
			q = idom.createQuestionOC(name, parent, new String[0]);
		} else if (type.equalsIgnoreCase("mc")) {
			q = idom.createQuestionMC(name, parent, new String[0]);
		} else if (type.equalsIgnoreCase("num")) {
			q = idom.createQuestionNum(name, parent);
		} else if (type.equalsIgnoreCase("date")) {
			q = idom.createQuestionDate(name, parent);
		} else if (type.equalsIgnoreCase("info")) {
			q = idom.createQuestionZC(name, parent);
		} else if (type.equalsIgnoreCase("yn")||type.equalsIgnoreCase("jn")) {
			q = idom.createQuestionYN(name, parent);
		} else if (type.equalsIgnoreCase("state")) {
			q = idom.createQuestionState(name, parent);
		} else if (type.equalsIgnoreCase("text")) {
			q = idom.createQuestionText(name, parent);
		} else {
			q=null;
		}
		return q;
	}
	
	public static boolean checkType(Question q, String type) {
		if (type==null) {
			return true;
		}else if (q instanceof QuestionYN) {
			if (type.equalsIgnoreCase("yn")||type.equalsIgnoreCase("jn")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionNum) {
			if (type.equalsIgnoreCase("num")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionZC) {
			if (type.equalsIgnoreCase("info")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionOC) {
			if (type.equalsIgnoreCase("oc")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionMC) {
			if (type.equalsIgnoreCase("mc")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionDate) {
			if (type.equalsIgnoreCase("date")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionText) {
			if (type.equalsIgnoreCase("text")) {
				return true;
			} else {
				return false;
			}
		} else if (q instanceof QuestionSolution) {
			if (type.equalsIgnoreCase("state")) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
