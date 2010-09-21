/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.condition;

import java.util.regex.Pattern;

import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.session.Value;
import de.d3web.report.Message;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Supplies a method for creating a condition from a (Complex)Finding
 * 
 * @author Johannes Dienst
 * 
 */
public class FindingConditionBuilder {

	private static Pattern p = Pattern.compile("\"");

	/**
	 * Creates the right Condition according to the given Comparator.
	 * 
	 * @param kbQuest
	 * @param kbAns
	 * @param comp
	 * @param question
	 * @param answer
	 * @return
	 */
	private static Condition createCondition(KnowWEArticle article, Question kbQuest, Value kbAns, Section comp, Section question, Section answer) {
		// CondEqual(Yes/No), CondNumEqual, CondNumGreater, CondNumGreaterEqual,
		// CondNumLess, CondNumLessEqual
		// Unhandled Conditions: CondKnown, CondNumIn, CondTextContains,
		// CondTextEqual, CondUnknown

		// Return if comp is null because no Condition can be created
		if (comp == null) return null;

		String comparator = comp.getOriginalText().replaceAll(p.toString(), "").trim();
		String answerText = answer.getOriginalText().replaceAll(p.toString(), "").trim();

		// CondEqual and CondNumEqual have same Comparator

		if (kbQuest instanceof QuestionNum) {
			Double valueOf = new Double(-1);
			try {
				valueOf = Double.valueOf(answerText);
			}
			catch (NumberFormatException e) {
				KnowWEUtils.storeSingleMessage(article, answer,
						FindingConditionBuilder.class, Message.class, new Message(
								"Numerical value expected, got: '" + answerText + "'."));
			}

			QuestionNum questionNum = (QuestionNum) kbQuest;
			return createCondNum(article, comp, comparator, valueOf,
					questionNum);
		}
		else {
			if (comparator.equals("=")) return new CondEqual(kbQuest, kbAns);
			else {
				KnowWEUtils.storeSingleMessage(article, comp,
						FindingConditionBuilder.class, Message.class, new Message(
								"Unkown comparator '" + comparator + "'."));
				return null;
			}
		}

	}

	public static Condition createCondNum(KnowWEArticle article,
			Section comp, String comparator, Double valueOf,
			QuestionNum questionNum) {
		KnowWEUtils.clearMessages(article, comp, FindingConditionBuilder.class, Message.class);

		if (comparator.equals("=")) return new CondNumEqual(questionNum, valueOf);
		else if (comparator.equals(">")) return new CondNumGreater(questionNum, valueOf);
		else if (comparator.equals(">=")) return new CondNumGreaterEqual(questionNum, valueOf);
		else if (comparator.equals("<")) return new CondNumLess(questionNum, valueOf);
		else if (comparator.equals("<=")) return new CondNumLessEqual(questionNum, valueOf);
		else {
			KnowWEUtils.storeSingleMessage(article, comp,
					FindingConditionBuilder.class, Message.class, new Message(
							"Unkown comparator '" + comparator + "'."));
			return null;
		}
	}






}
