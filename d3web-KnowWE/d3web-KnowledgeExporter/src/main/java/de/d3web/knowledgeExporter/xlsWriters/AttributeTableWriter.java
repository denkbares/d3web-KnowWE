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

package de.d3web.knowledgeExporter.xlsWriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jxl.Cell;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.utilities.Triple;
import de.d3web.knowledgeExporter.KnowledgeManager;
import de.d3web.shared.Abnormality;
import de.d3web.shared.AbstractAbnormality;
import de.d3web.shared.Weight;

public class AttributeTableWriter extends XlsKnowledgeWriter {

	private Set<Question> questions;
	private List<Solution> diagnoses;
	private Set<QASet> qcontainers;

	public AttributeTableWriter(KnowledgeManager manager) {
		super(manager);
	}

	@Override
	protected void setVerticalAndHorizontalFreeze() {
		wb.getSheet(0).getSettings().setVerticalFreeze(1);
		wb.getSheet(0).getSettings().setHorizontalFreeze(isExtraAnswerColumn() ? 2 : 1);
		wb.getSheet(1).getSettings().setVerticalFreeze(1);
		wb.getSheet(1).getSettings().setHorizontalFreeze(1);
		// wb.getSheet(2).getSettings().setVerticalFreeze(1);
		// wb.getSheet(2).getSettings().setHorizontalFreeze(1);

	}

	@Override
	protected void getKnowledge() {

		questions = manager.getQuestions();

		diagnoses = manager.getSolutionList();
		diagnoses.remove(manager.getKB().getRootSolution());

		qcontainers = manager.getQClasses();
		qcontainers.remove(manager.getKB().getRootQASet());

	}

	@Override
	protected void writeSheets() throws WriteException {

		// SHEET 1: Attributtabelle für die Fragen
		writeQuestionSheet();

		// SHEET 2: Attributtabelle für die Diagnosen
		writeSolutionSheet();

		// SHEET 3: Attributtabelle für die Frageklassen
		// writeQClassesSheet();

	}

	private void writeQuestionSheet() throws RowsExceededException, WriteException {
		WritableSheet qSheet = wb.createSheet(KnowledgeManager.getResourceBundle().getString(
				"writer.questions"), 0);

		writeQHeader(qSheet);

		int row = 1;
		for (Question q : questions) {
			qSheet.addCell(new Label(getColumn(Question.class.getSimpleName(), qSheet),
					row, q.getName(), getCellFormatBold()));

			// WEIGHT UND ABNORMALITY DIESER FRAGE RAUSSCHREIBEN
			Collection<KnowledgeSlice> kSlices = q.getAllKnowledge();
			Abnormality abn = null;
			for (KnowledgeSlice ks : kSlices) {
				if (ks instanceof Weight) {
					qSheet.addCell(new Label(getColumn(Weight.class.getSimpleName(), qSheet),
							row, Weight.convertValueToConstantString(((Weight) ks)
									.getQuestionWeightValue().getValue()), getCellFormatCenter()));
				}
				if (ks instanceof Abnormality) {
					abn = (Abnormality) ks;
				}
			}

			int qRow = row;

			row++;
			if (q instanceof QuestionChoice) {
				List<Choice> answers = ((QuestionChoice) q).getAllAlternatives();

				// ANTWORTEN RAUSSCHREIBEN
				for (Choice a : answers) {
					qSheet.addCell(new Label(getColumn(isExtraAnswerColumn() ?
							Choice.class.getSimpleName() : Question.class.getSimpleName(), qSheet),
							row, isExtraAnswerColumn() ? a.getName() : " - " + a.getName()));
					writeMMInfos(a, qSheet, row);

					// ABNORMALITY DER ANTWORT RAUSSCHREIBEN
					if (abn != null) {
						qSheet.addCell(new Label(
								getColumn(Abnormality.class.getSimpleName(), qSheet),
								row,
								AbstractAbnormality.convertValueToConstantString(abn.getValue(new ChoiceValue(
										a))), getCellFormatCenter()));
					}
					row++;
				}
			}
			writeMMInfos(q, qSheet, qRow);
		}

	}

	private void writeQHeader(WritableSheet qSheet) throws RowsExceededException, WriteException {

		qSheet.addCell(new Label(0, 0, KnowledgeManager.getResourceBundle().getString
				("AttributeTableWriter.header.Question"), getCellFormatBoldCenter()));
		if (isExtraAnswerColumn()) {
			qSheet.addCell(new Label(1, 0, KnowledgeManager.getResourceBundle().getString
					("AttributeTableWriter.header.AnswerChoice"), getCellFormatBoldCenter()));
		}

		boolean weight = false;
		boolean abnorm = false;
		boolean prompt = false;
		boolean link = false;
		Iterator<Question> questionsIt = questions.iterator();
		while ((!weight || !abnorm || !prompt || !link) && questionsIt.hasNext()) {
			Question q = questionsIt.next();
			Collection<KnowledgeSlice> kSlices = q.getAllKnowledge();
			for (KnowledgeSlice ks : kSlices) {
				if (ks instanceof Weight) {
					weight = true;
				}
				if (ks instanceof Abnormality) {
					abnorm = true;
				}
			}
			MMInfoStorage storage = (MMInfoStorage) q.getInfoStore().getValue(
					BasicProperties.MMINFO);
			if (storage != null) {
				DCMarkup markup = new DCMarkup();
				markup.setContent(DCElement.SUBJECT, MMInfoSubject.PROMPT.getName());
				Set<MMInfoObject> infoObjects = storage.getMMInfo(markup);
				if (!infoObjects.isEmpty()) {
					prompt = true;
				}
			}
			if (q instanceof QuestionChoice) {
				List<Choice> answers = ((QuestionChoice) q).getAllAlternatives();
				for (Choice a : answers) {
					boolean[] check = checkProperties(a);
					if (check[0]) prompt = true;
					if (check[1]) link = true;
				}
			}
			boolean[] check = checkProperties(q);
			if (check[0]) prompt = true;
			if (check[1]) link = true;

		}
		int i = isExtraAnswerColumn() ? 2 : 1;

		if (prompt) {
			qSheet.addCell(new Label(i, 0, KnowledgeManager.getResourceBundle().getString
					("AttributeTableWriter.header.prompt"), getCellFormatBoldCenter()));
		}
		if (weight) {
			qSheet.addCell(new Label(i + 1, 0, KnowledgeManager.getResourceBundle().getString
					("AttributeTableWriter.header.Weight"), getCellFormatBoldCenter()));
		}
		if (abnorm) {
			qSheet.addCell(new Label(i + 2, 0, KnowledgeManager.getResourceBundle().getString
					("AttributeTableWriter.header.Abnormality"), getCellFormatBoldCenter()));
		}
		if (link) {
			qSheet.addCell(new Label(i + 3, 0, KnowledgeManager.getResourceBundle().getString
					("AttributeTableWriter.header.link"), getCellFormatBoldCenter()));
		}
	}

	private boolean[] checkProperties(IDObject o) {
		boolean[] check = new boolean[2];
		InfoStore infoStore = o.getInfoStore();
		MMInfoStorage storage = (MMInfoStorage) infoStore.getValue(BasicProperties.MMINFO);
		if (storage != null) {
			DCMarkup markup = new DCMarkup();
			markup.setContent(DCElement.SUBJECT, MMInfoSubject.PROMPT.getName());
			Set<MMInfoObject> infoObjects = storage.getMMInfo(markup);
			if (!infoObjects.isEmpty()) {
				check[0] = true;
			}
			markup = new DCMarkup();
			markup.setContent(DCElement.SUBJECT, MMInfoSubject.LINK.getName());
			infoObjects = storage.getMMInfo(markup);
			if (!infoObjects.isEmpty()) {
				check[1] = true;
			}
		}
		return check;
	}

	private void writeSolutionSheet() throws RowsExceededException, WriteException {
		WritableSheet dSheet = wb.createSheet(KnowledgeManager.getResourceBundle().getString(
				"writer.diagnoses"), 1);

		writeDHeader(dSheet);

		int row = 1;
		for (Solution d : diagnoses) {
			dSheet.addCell(new Label(getColumn(Solution.class.getSimpleName(), dSheet),
					row, d.getName(), getCellFormatBold()));
			writeMMInfos(d, dSheet, row);
			row++;
		}
	}

	private void writeDHeader(WritableSheet dSheet) throws RowsExceededException, WriteException {

		dSheet.addCell(new Label(0, 0, KnowledgeManager.getResourceBundle().getString
				("AttributeTableWriter.header.Diagnosis"), getCellFormatBoldCenter()));

		boolean link = false;
		Iterator<Solution> diagnosesIt = diagnoses.iterator();
		while (!link && diagnosesIt.hasNext()) {
			Solution d = diagnosesIt.next();
			if (checkProperties(d)[1]) {
				link = true;

			}
		}

		if (link) {
			dSheet.addCell(new Label(1, 0, KnowledgeManager.getResourceBundle().getString
					("AttributeTableWriter.header.link"), getCellFormatBoldCenter()));
		}
	}

	private void writeMMInfos(IDObject object, WritableSheet sheet, int row)
			throws RowsExceededException, WriteException {
		InfoStore infoStore = object.getInfoStore();
		for (Triple<Property<?>, Locale, Object> p : infoStore.entries()) {
			if (p.getA().equals(BasicProperties.MMINFO)) {
				MMInfoStorage storage = (MMInfoStorage) p.getC();
				if (storage != null) {
					for (Iterator<MMInfoSubject> subjects = MMInfoSubject.getIterator(); subjects.hasNext();) {
						MMInfoSubject subject = subjects.next();

						DCMarkup markup = new DCMarkup();
						markup.setContent(DCElement.SUBJECT, subject.getName());

						Set<MMInfoObject> infoObjects = storage.getMMInfo(markup);

						if (!infoObjects.isEmpty()) {
							for (MMInfoObject o : infoObjects) {
								sheet.addCell(new Label(getColumn(subject.getName(), sheet), row,
										o.getContent()));
							}
						}
					}
				}
			}
			else {
				Object value = p.getC();
				if (value != null) {
					sheet.addCell(new Label(getColumn(p.getA().getName(), sheet), row,
							value.toString()));
				}
			}
		}
	}

	private int getColumn(String className, WritableSheet sheet) throws RowsExceededException, WriteException {
		Cell[] tableHeader = sheet.getRow(0);
		String searchedColumnName = "";
		try {
			searchedColumnName = KnowledgeManager.getResourceBundle()
					.getString("AttributeTableWriter.header." + className);
		}
		catch (Exception e) {
			searchedColumnName = className;
		}

		int i = 0;
		for (Cell columnHeader : tableHeader) {
			if (columnHeader.getContents().equals(searchedColumnName)) {
				return i;
			}
			i++;
		}

		Label newColumnHeader = new Label(i, 0, searchedColumnName, getCellFormatBoldCenter());
		sheet.addCell(newColumnHeader);
		return i;
	}

}
