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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jxl.Cell;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import de.d3web.knowledgeExporter.KnowledgeManager;

public abstract class QDTableWriter extends XlsKnowledgeWriter {

	protected List<String> diagnosisList = new ArrayList<String>();
	protected List<List<String>> diagnosisListParts = new ArrayList<List<String>>();
	protected List<QuestionEntry> questionEntrys = new ArrayList<QuestionEntry>();

	protected QDTableWriter(KnowledgeManager manager) {
		super(manager);
	}

	@Override
	protected void setVerticalAndHorizontalFreeze() {
		for (WritableSheet sheet : wb.getSheets()) {
			sheet.getSettings().setVerticalFreeze(1);
			sheet.getSettings().setHorizontalFreeze(isExtraAnswerColumn() ? 2 : 1);
		}
	}

	/**
	 * 
	 * @param hEntry is the String to search for
	 * @return [0] is the Column, [1] the SheetNumber
	 */
	protected int[] findHeaderEntry(String hEntry) {

		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			WritableSheet sheet = wb.getSheet(i);
			for (Cell cell : sheet.getRow(0)) {
				if (cell.getContents().compareTo(hEntry) == 0) {
					return new int[] {
							cell.getColumn(), i };
				}
			}
		}
		return null;
	}

	/**
	 * Exceltabellen gehen nur bis 255 Spalten, deswegen müssen die Regeln ggf.
	 * auf mehrerer Sheets verteilt werden.
	 */
	protected void splitSolutionList() {
		int index = 0;
		int stepWidth = excelMaxCols - 4;
		while (diagnosisList.size() - index > excelMaxCols - 3) {
			List<String> part = diagnosisList.subList(index, index + stepWidth);
			index += stepWidth;
			diagnosisListParts.add(part);
		}
		diagnosisListParts.add(diagnosisList.subList(index, diagnosisList.size()));
	}

	@Override
	protected void writeSheets() throws WriteException {
		for (List<String> part : diagnosisListParts) {
			WritableSheet sheet = wb.createSheet(KnowledgeManager.getResourceBundle().getString(
					"writer.sheet")
					+ (wb.getNumberOfSheets() + 1), wb.getNumberOfSheets());
			// Diagnoses to the header
			for (int i = 0; i < part.size() + 2; i++) {
				if (i > 1) {
					sheet.addCell(new Label(isExtraAnswerColumn() ? i : i - 1, 0, part.get(i - 2),
							getCellFormatBoldCenter()));
				}

			}
		}
		// Question to column 0
		int row = 1;
		for (QuestionEntry qe : questionEntrys) {
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				WritableSheet sheet = wb.getSheet(i);
				sheet.addCell(new Label(0, row, qe.getQuestion(), getCellFormatBold()));
			}
			row++;
			// Answers
			for (AnswerEntry ae : qe.getAnswerEntrys()) {
				for (int i = 0; i < wb.getNumberOfSheets(); i++) {
					WritableSheet sheet = wb.getSheet(i);
					sheet.addCell(new Label(isExtraAnswerColumn() ? 1 : 0, row,
							isExtraAnswerColumn() ? ae.getAnswer() : " - " + ae.getAnswer()));
				}
				// Scores to the columns of the diagnoses
				for (CellEntry se : ae.getCellEntrys()) {
					int[] coordinates = findHeaderEntry(se.getSolution());
					if (coordinates != null) {
						WritableSheet sheet = wb.getSheet(coordinates[1]);
						sheet.addCell(new Label(coordinates[0], row, se.getContent().toString(),
								getCellFormatCenter()));
					}
				}
				row++;
			}
		}
	}

	protected class QuestionEntry {

		private String question;

		private List<AnswerEntry> answerList = new LinkedList<AnswerEntry>();

		public QuestionEntry(String q, AnswerEntry r) {
			question = q;
			answerList.add(r);
		}

		public List<AnswerEntry> getAnswerEntrys() {
			return answerList;
		}

		public void addEntry(AnswerEntry e) {
			answerList.add(e);
		}

		public String getQuestion() {
			return question;
		}

	}

	protected class AnswerEntry {

		private String answer;
		private List<CellEntry> scoreList = new LinkedList<CellEntry>();

		public AnswerEntry(String a, CellEntry se) {
			this.answer = a;
			scoreList.add(se);
		}

		public String getAnswer() {
			return answer;
		}

		public List<CellEntry> getCellEntrys() {
			return scoreList;
		}

		public void addEntry(CellEntry se) {
			scoreList.add(se);
		}
	}

	protected class CellEntry {

		private String diagnosis;
		private String score;
		private String comp;
		private String content;

		public CellEntry(String diagnosis, String content) {
			this.diagnosis = diagnosis;
			this.content = content;
		}

		public CellEntry(String diagnosis, String score, String comp, String content) {
			this.diagnosis = diagnosis;
			this.score = score;
			this.comp = comp;
			this.content = content;
		}

		public String getSolution() {
			return diagnosis;
		}

		public String getContent() {
			return content;
		}

		public String getScore() {
			return score;
		}

		public String getComp() {
			return comp;
		}

	}

	protected void addEntry(String diagnosis, String question, String answer, String content) {
		addEntry(diagnosis, null, null, question, answer, content);
	}

	protected void addEntry(String diagnosis, String score, String composition, String question, String answer, String content) {
		boolean foundQ = false;
		for (QuestionEntry qe : questionEntrys) {
			if (qe.getQuestion().compareTo(question) == 0) {
				foundQ = true;
				boolean foundC = false;
				for (AnswerEntry ae : qe.getAnswerEntrys()) {
					if (ae.getAnswer().compareTo(answer) == 0) {
						foundC = true;
						ae.addEntry(new CellEntry(diagnosis, content));
						break;
					}
				}
				if (!foundC) {
					qe.addEntry(new AnswerEntry(answer, new CellEntry(diagnosis, content)));
					break;
				}
			}
		}
		if (!foundQ) {
			questionEntrys.add(new QuestionEntry(question, new AnswerEntry(answer, new CellEntry(
					diagnosis, content))));
		}
	}

}
