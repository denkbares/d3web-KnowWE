package de.d3web.KnOfficeParser.util;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionDate;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionSolution;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.qasets.QuestionZC;


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
