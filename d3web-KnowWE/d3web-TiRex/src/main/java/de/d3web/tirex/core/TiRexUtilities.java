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

package de.d3web.tirex.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wcohen.ss.Levenstein;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;

/**
 * A singleton class that contains numerous methods necessary to run the
 * TiRexInterpreter
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class TiRexUtilities {

	/**
	 * The unique instance of this class.
	 */
	private static TiRexUtilities instance;

	/**
	 * A Levenstein object that is used to compute the edit-distance between two
	 * Strings in some of the methods used in this class.
	 */
	private Levenstein levenstein;

	private TiRexUtilities() {
		levenstein = new Levenstein();
	}

	/**
	 * @return The unique instance.
	 */
	public static TiRexUtilities getInstance() {
		if (instance == null) {
			instance = new TiRexUtilities();
		}

		return instance;
	}

	/**
	 * @param knowledge
	 *            The knowledgebase, which the questions and answers are to be
	 *            read from.
	 * @param getAnswers
	 *            If this argument is "false", then only the questions are read
	 *            and returned, else the answers are extracted as well.
	 * @return A textual representation of the knowledgebase.
	 */
	public String getAllQuestionsAnswersAndDiagnosesAsString(
			KnowledgeBase knowledge, boolean getAnswers) {
		StringBuffer buffer = new StringBuffer();

		List<Diagnosis> diagnoses = knowledge.getDiagnoses();
		for (Diagnosis diagnosis : diagnoses) {
			buffer.append(" Diagnose: " + diagnosis.getText() + "(Typ: "
					+ diagnosis.getClass() + " ID: " + diagnosis.getId() + ")\n");
		}

		List<Question> questions = knowledge.getQuestions();
		for (Question question : questions) {
			buffer.append("  Frage: " + question.getText() + " (Typ: "
					+ question.getClass() + " ID: " + question.getId() + ")\n");

			if (getAnswers) {
				Collection<AnswerChoice> answers = getAllPossibleAnswers(question);

				for (Answer answer : answers) {
					if (answer instanceof AnswerChoice) {
						buffer.append(((AnswerChoice) answer).getText() + "\n");
					}
				}
			}

			buffer.append("\n");
		}

		return buffer.toString();
	}

	/**
	 * @param reader
	 *            The Reader, which is to be returned as a String.
	 * @return A textual representation of the Reader.
	 * @throws IOException
	 */
	public String getReaderAsString(Reader reader) throws IOException {
		StringBuffer text = new StringBuffer();
		BufferedReader bufferedReader = new BufferedReader(reader);

		String line = bufferedReader.readLine();

		while (line != null) {
			text.append(line + "\n");
			line = bufferedReader.readLine();
		}

		return text.toString();
	}

	/**
	 * @param kb
	 *            The KnowledgeBase which the answers are to be extracted from.
	 * @return All the predefined answers that could be found in the
	 *         KnowledgeBase are in a Collection. Currently the only predefined
	 *         Answers that I know of are Objects of the Class "AnswerChoice".
	 */
	public Collection<AnswerChoice> getAllPossibleAnswers(KnowledgeBase kb) {
		Collection<AnswerChoice> coll = new ArrayList<AnswerChoice>();

		for (Question q : kb.getQuestions()) {
			coll.addAll(getAllPossibleAnswers(q));
		}

		return coll;
	}

	/**
	 * @param q
	 *            The Question which the answers are to be extracted from.
	 * @return All the predefined answers that could be found for the given
	 *         Question are returned in a Collection.
	 */
	public Collection<AnswerChoice> getAllPossibleAnswers(Question q) {
		return (q instanceof QuestionChoice) ? ((QuestionChoice) q)
				.getAllAlternatives() : new ArrayList<AnswerChoice>();
	}

	/**
	 * @param expression
	 *            The expression (figure of speech) which is usually an answer
	 *            to a QuestionNum.
	 * @param regex
	 *            KnOfficePairs A Map that contains the regular expressions
	 *            which are to be used to parse the given expression. The map
	 *            also contains the KnOffice expressions, which are to be used,
	 *            if the given expression is successfully parsed.
	 * @return The KnOffice shape of the given expression if it is successfully
	 *         parsed, otherwise the expression is returned unchanged.
	 */
	public String convertNumericalExpressionToKnOffice(String expression,
			Map<String, String> regexKnOfficePairs) {
		String convertedExpression = expression;

		if(regexKnOfficePairs == null) return null;
		Set<String> regexes = regexKnOfficePairs.keySet();
		
		for (String regex : regexes) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(expression);

			if (m.find()) {
				String knOffice = regexKnOfficePairs.get(regex);

				int groups = m.groupCount();

				
				for (int i = 1; i <= groups; i++) {
					if(i == groups) {
						// EDIT!
						// last group gets deleted
						// because later cannot be parsed by TextParser-system
						knOffice = knOffice.replaceAll("\\$" + i, "");
					}else {
						knOffice = knOffice.replaceAll("\\$" + i, m.group(i));
					}
				}

				convertedExpression = knOffice;
			}
		}

		return convertedExpression;
	}

	/**
	 * @param array
	 *            The array which is to be converted.
	 * @return A Collection of Objects, which were grouped in the given array.
	 */
	public <T> Collection<T> convertArrayToCollection(T[] array) {
		Collection<T> collection = new ArrayList<T>();

		for (T element : array) {
			collection.add(element);
		}

		return collection;
	}

	/**
	 * @param s1
	 *            First String.
	 * @param s2
	 *            Second String.
	 * @return True is returned, if the Edit-Distance (Levenstein-Distance) of
	 *         the two given Strings is within certain predefined boundaries.
	 *         The boundaries are defined in the TiRexSettings ResourceBundle.
	 */
	public boolean levensteinDistanceIsOK(String s1, String s2) {
		double score = Math.abs(levenstein.score(s1, s2));

		Collection<String> distances = TiRexSettings.getInstance()
				.getAllowedEditDistancesForLevenstein();

		for (String distance : distances) {
			String[] distanceAndLimits = distance.split(" in ");
			double dist = Double.parseDouble(distanceAndLimits[0]);
			double lowerLimit = Double.parseDouble(distanceAndLimits[1]
					.split("-")[0]);
			double upperLimit = Double.parseDouble(distanceAndLimits[1]
					.split("-")[1]);

			if (s2.length() >= lowerLimit && s2.length() <= upperLimit
					&& score <= dist) {
				return true;
			}
		}

		return false;
	}
}
