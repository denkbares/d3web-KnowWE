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

/*
 * Created on 23.06.2005
 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;
import de.d3web.report.Message;
import de.d3web.textParser.Utils.*;
import de.d3web.kernel.domainModel.*;
import de.d3web.kernel.domainModel.qasets.*;
import de.d3web.kernel.domainModel.answers.*;
import de.d3web.kernel.supportknowledge.*;
import de.d3web.kernel.psMethods.questionSetter.PSMethodQuestionSetter;
import de.d3web.kernel.psMethods.shared.*;

import java.util.*;
import java.util.logging.*;

/**
 * Generates the knowledge for an attribute table and adds it to the
 * knowledgebase represented by given KnowledgeBaseManagement
 * 
 * @author Andreas Klar
 */
public class AttributeTableKnowledgeGenerator extends KnowledgeGenerator {

	static List<NamedObject> usedNamedObjects = new ArrayList<NamedObject>();

	private AttributeConfigReader attrReader;

	private boolean update;

	private int attributeCount;

	public AttributeTableKnowledgeGenerator(KnowledgeBaseManagement kbm,
			AttributeConfigReader attrReader, boolean update) {
		super(kbm);
		this.attrReader = attrReader;
		this.update = update;
		this.attributeCount = 0;
	}

	/**
	 * Generates attributes from a decision table
	 * 
	 * @param table
	 *            the attribute table which contains the information about
	 *            attributes
	 * @return number of generated attributes
	 */
	@Override
	public Report generateKnowledge(DecisionTable table) {
		report = new Report();
		attributeCount = 0;
		String[][] tableData = table.getTableData();
		int firstDataRow = 1;
		int firstDataColumn = 2;

		for (int i = firstDataRow; i < table.rows(); i++) {
			for (int j = firstDataColumn; j < table.columns(); j++) {
				String attributeValue = tableData[i][j];
				if (attributeValue.equals(""))
					continue;
				String attributeName = tableData[0][j];
				String objectName = tableData[i][0];
				String answerName = null;
				if (objectName.equals("")) {
					objectName = table.getQuestionText(i);
					answerName = tableData[i][1];
				}
				NamedObject object = KBUtils.findNamedObject(kbm, objectName);
				if (object == null)
					continue; // continue if there's no valid object

				// erase all attributes for this NamedObject if mode is REPLACE
				// and object has not been erased before
				if (!update && !usedNamedObjects.contains(object)) {
					int removed = removeAllAttributes(object);
					if (removed > 0)
						report.note(MessageGenerator.removedAttributes(object
								.getText(), removed));
					usedNamedObjects.add(object);
				}

				Property prop = attrReader.getPropertyField(attributeName);

				// special attributes
				if (prop == null) {
					// NUM2Choice
					if (attributeName
							.startsWith(AttributeConfigReader.NUM2CHOICE)
							&& object instanceof QuestionChoice) {
						report.addAll(setNumToChoiceAttribute(
								(QuestionChoice) object, attributeValue));
					}
					// special attributes
					else {
						handleSpecialAttributes(object, answerName,
								attributeName, attributeValue);
					}
				}

				// MMINFO properties
				else if (prop == Property.MMINFO)
					handleMMInfoAttributes(object, attributeName,
							attributeValue);

				// standard attributes
				else
					handleStandardAttributes(object, attributeName,
							attributeValue, prop);
			}
		}

		report.note(MessageGenerator.addedAttributes(table.getSheetName() + ":"
				+ table.getTableNumber(), attributeCount));

		return report;
	}

	public static void reset() {
		usedNamedObjects = new ArrayList<NamedObject>();
	}

	// TODO: count removed Attributes
	private int removeAllAttributes(NamedObject object) {
		int removedCount = 0;
		// remove standard properties (includes MMINFO)
		object.setProperties(new de.d3web.kernel.supportknowledge.Properties());
		// remove shared knowledge (weight, local weight, abnormality,
		// similarity)
		if (object instanceof Question) {
			// weight
			while (true) {
				List<Weight> knowledgeSlices = (List<Weight>) object
						.getKnowledge(PSMethodShared.class,
								PSMethodShared.SHARED_WEIGHT);
				if (knowledgeSlices != null && !knowledgeSlices.isEmpty())
					object.removeKnowledge(PSMethodShared.class,
							knowledgeSlices.get(0),
							PSMethodShared.SHARED_WEIGHT);
				else
					break;
			}
			// local weights
			while (true) {
				List<LocalWeight> knowledgeSlices = (List<LocalWeight>) object
						.getKnowledge(PSMethodShared.class,
								PSMethodShared.SHARED_LOCAL_WEIGHT);
				if (knowledgeSlices != null && !knowledgeSlices.isEmpty())
					object.removeKnowledge(PSMethodShared.class,
							knowledgeSlices.get(0),
							PSMethodShared.SHARED_LOCAL_WEIGHT);
				else
					break;
			}
			// abnormality
			while (true) {
				List<Abnormality> knowledgeSlices = (List<Abnormality>) object
						.getKnowledge(PSMethodShared.class,
								PSMethodShared.SHARED_ABNORMALITY);
				if (knowledgeSlices != null && !knowledgeSlices.isEmpty())
					object.removeKnowledge(PSMethodShared.class,
							knowledgeSlices.get(0),
							PSMethodShared.SHARED_ABNORMALITY);
				else
					break;
			}
		}
		// remove APrioriProbability (diagnoses only)
		if (object instanceof Diagnosis) {
			try {
				((Diagnosis) object).setAprioriProbability(null);
			} catch (ValueNotAcceptedException e) {
				Logger.getLogger(this.getClass().getName()).warning(
						e.getMessage());
			}
		}
		return removedCount;
	}

	/**
	 * Sets the values for standard attributes (standard attributes:
	 * 
	 * @see de.d3web.kernel.supportknowledge.Property
	 * @param object
	 *            NamedObject for which the attribute should be set
	 * @param attributeName
	 *            name of the attribute which should be set
	 * @param attributeValue
	 *            value of the attribute which should be set
	 * @param prop
	 */
	private void handleStandardAttributes(NamedObject object,
			String attributeName, String attributeValue, Property prop) {
		if (attrReader.getAllowedValues(attributeName).get(0).equalsIgnoreCase(
				"boolean")) {
			String temp = attributeValue.toLowerCase();
			if (temp.equals("true") || temp.equals("yes") || temp.equals("ja")) {
				object.getProperties().setProperty(prop, new Boolean(true));
				attributeCount++;
			} else if (temp.equals("false") || temp.equals("no")
					|| temp.equals("nein")) {
				object.getProperties().setProperty(prop, new Boolean(false));
				attributeCount++;
			}
		} else {
			object.getProperties().setProperty(prop, attributeValue);
			attributeCount++;
		}
	}

	/**
	 * Handles attributes which are stored in MMInfoStorage
	 * 
	 * @see de.d3web.kernel.supportknowledge.MMInfoStorage
	 * @param object
	 *            NamedObject for which the attribute should be set
	 * @param attributeName
	 *            name of the attribute which should be set
	 * @param attributeValue
	 *            value of the attribute which should be set
	 */
	private void handleMMInfoAttributes(NamedObject object,
			String attributeName, String attributeValue) {
		MMInfoStorage storage = (MMInfoStorage) object.getProperties()
				.getProperty(Property.MMINFO);
		if (storage == null)
			storage = new MMInfoStorage();
		// read DCElement from AttributeConfigReader
		DCElement dce = attrReader.getDCElement(attributeName);
		if (dce == null) {
			return;
		}
		DCMarkup m = new DCMarkup();
		m.setContent(DCElement.SOURCE, object.getId());
		// TODO: Identifier fï¿½r DCMarkup generieren und setzen
		// m.setContent(DCElement.IDENTIFIER, IDFactory.createId(m));
		if (dce == DCElement.SUBJECT) {
			MMInfoSubject mminfosubj = attrReader
					.getMMInfoSubject(attributeName);
			if (mminfosubj == null) {
				return;
			}
			m.setContent(dce, mminfosubj.getName());
			if (mminfosubj.getName().equals(MMInfoSubject.LINK.getName())
					|| mminfosubj.getName().equals(
							MMInfoSubject.MEDIA.getName())
					|| mminfosubj.getName().equals(MMInfoSubject.URL.getName())
					|| mminfosubj.getName().equals(
							MMInfoSubject.MULTIMEDIA.getName())
					|| mminfosubj.getName()
							.equals(MMInfoSubject.INFO.getName())) {
				String title = attributeName;
				if (attributeValue.indexOf("|") != -1) {
					try {
						title = attributeValue.split("\\|")[0].trim();
						attributeValue = attributeValue.split("\\|")[1].trim();
					} catch (ArrayIndexOutOfBoundsException e) {
						title = attributeName;
					}
				}
				m.setContent(DCElement.TITLE, title);
			}
		} else
			m.setContent(dce, attributeValue);
		storage.addMMInfo(new MMInfoObject(m, attributeValue));
		object.getProperties().setProperty(Property.MMINFO, storage);
		attributeCount++;
	}

	/**
	 * Handles special attributes: A Priori Probability, Weight, Local Weight,
	 * Abnormality, Similarity
	 * 
	 * @param object
	 *            NamedObject for which the attribute should be set
	 * @param answerName
	 *            the name of the answer
	 * @param attributeName
	 *            name of the attribute which should be set
	 * @param attributeValue
	 *            value of the attribute which should be set
	 */
	private void handleSpecialAttributes(NamedObject object, String answerName,
			String attributeName, String attributeValue) {
		if (attributeName.equals(AttributeConfigReader.APRIORI)
				&& object instanceof Diagnosis)
			setAPrioriAttribute((Diagnosis) object, attributeValue);
		else if (attributeName.equals(AttributeConfigReader.SHARED_WEIGHT)
				&& object instanceof Question)
			setWeightAttribute((Question) object, attributeValue);
		else if (attributeName.equals(AttributeConfigReader.SHARED_ABNORMALITY)
				&& object instanceof QuestionChoice)
			setSharedAbnormalityAttribute((QuestionChoice) object, answerName,
					attributeValue);
		else if (attributeName
				.startsWith(AttributeConfigReader.SHARED_LOCAL_WEIGHT)
				&& object instanceof QuestionChoice)
			setLocalWeightAttribute((QuestionChoice) object, answerName,
					attributeName, attributeValue);

	}

	/**
	 * Sets the local weight of a Question
	 * 
	 * @param question
	 *            question for which the attribute should be set
	 * @param answerName
	 *            name of the answer
	 * @param attributeName
	 *            name of the attribute which should be set and name of the
	 *            diagnosis in round brackets
	 * @param attributeValue
	 *            value of the attribute which should be set
	 */
	private void setLocalWeightAttribute(QuestionChoice question,
			String answerName, String attributeName, String attributeValue) {
		AnswerChoice answer = null;
		if (question instanceof QuestionYN) {
			answer = KBUtils.findAnswerYN(kbm, (QuestionYN) question,
					answerName);
		} else {
			answer = kbm.findAnswerChoice(question, answerName);
		}
		if (answer == null)
			return;
		// find the Diagnosis
		String diaName = attributeName.substring(
				attributeName.indexOf("(") + 1, attributeName.indexOf(")"));
		Diagnosis dia = kbm.findDiagnosis(diaName);
		if (dia == null)
			return;
		LocalWeight lweight = new LocalWeight();
		// look for existing local weight for this diagnosis
		Object k = (question).getKnowledge(PSMethodShared.class,
				PSMethodShared.SHARED_LOCAL_WEIGHT);
		if (k != null && k instanceof LinkedList && !((LinkedList) k).isEmpty()) {
			for (Iterator it = ((LinkedList) k).iterator(); it.hasNext();) {
				LocalWeight next = (LocalWeight) it.next();
				if (next.getDiagnosis() == dia) {
					lweight = next;
					continue;
				}
			}
		}
		lweight.setValue(answer, LocalWeight
				.convertConstantStringToValue(attributeValue));
		lweight.setQuestion(question);
		lweight.setDiagnosis(dia);
		attributeCount++;
	}

	/**
	 * Sets the abnormality of an answer for a given question
	 * 
	 * @param question
	 *            question for which the attribute should be set
	 * @param answerName
	 *            name of the answer
	 * @param attributeValue
	 *            string containing a valid abnormality
	 */
	private void setSharedAbnormalityAttribute(QuestionChoice question,
			String answerName, String attributeValue) {
		AnswerChoice answer = null;
		if (question instanceof QuestionYN) {
			answer = KBUtils.findAnswerYN(kbm, (QuestionYN) question,
					answerName);
		} else {
			answer = kbm.findAnswerChoice(question, answerName);
		}
		if (answer == null)
			return;
		Abnormality abn;
		// look for existing abnormality for this question
		Object k = question.getKnowledge(PSMethodShared.class,
				PSMethodShared.SHARED_ABNORMALITY);
		if (k != null && k instanceof LinkedList && !((LinkedList) k).isEmpty()) {
			abn = (Abnormality) ((LinkedList) k).getFirst();
		} else {
			abn = new Abnormality();
		}
		abn.addValue(answer, AbstractAbnormality
				.convertConstantStringToValue(attributeValue));
		abn.setQuestion(question);
		attributeCount++;
	}

	/**
	 * Sets the weight of a question
	 * 
	 * @param question
	 *            Question for which the weight should be set
	 * @param attributeValue
	 *            value containing weight-value
	 */
	private void setWeightAttribute(Question question, String attributeValue) {
		QuestionWeightValue qwv = new QuestionWeightValue();
		qwv.setQuestion(question);
		qwv.setValue(Weight.convertConstantStringToValue(attributeValue));
		Weight weight = new Weight();
		weight.setQuestionWeightValue(qwv);
		attributeCount++;
	}

	/**
	 * Sets the a priori probability of a diagnosis
	 * 
	 * @param diagnosis
	 *            Diagnosis for which the a priori probability should be set
	 * @param attributeValue
	 *            value containing score
	 */
	private void setAPrioriAttribute(Diagnosis diagnosis, String attributeValue) {
		try {
			diagnosis.setAprioriProbability(ScoreFinder
					.getScore(attributeValue));
			this.attributeCount++;
		} catch (ValueNotAcceptedException e) {
		}
	}

	// Begin Change
	private Report setNumToChoiceAttribute(QuestionChoice question,
			String attributeValue) {
		Report report = new Report();
		ArrayList<Double> allValues = new ArrayList<Double>();
		Scanner scan = new Scanner(attributeValue).useDelimiter(";");
		while (scan.hasNext()) {
			Double value = Double.parseDouble(scan.next().replaceAll(",", "."));
			allValues.add(value);
		}
		Double[] allValuesInArray = new Double[allValues.size()];
		Iterator iter = allValues.iterator();
		int i = 0;
		while (iter.hasNext()) {
			allValuesInArray[i] = (Double) iter.next();
			i++;
		}

		if (question.getAllAlternatives().size() - 1 == allValues.size()) {
			Num2ChoiceSchema schema = question.getSchemaForQuestion();
			if (schema == null) {
				schema = new Num2ChoiceSchema();
			}
			schema.setId(question.getId());
			schema.setQuestion(question);
			schema.setSchemaArray(allValuesInArray);
			question.addKnowledge(PSMethodQuestionSetter.class, schema,
					PSMethodQuestionSetter.NUM2CHOICE_SCHEMA);
		} else {
			report.error(new Message(
					"Num2Choice : wrong number of values found"));
		}

		return report;

	}

	// End Change
}
