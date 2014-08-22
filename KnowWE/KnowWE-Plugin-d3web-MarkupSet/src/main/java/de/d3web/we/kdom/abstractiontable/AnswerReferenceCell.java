package de.d3web.we.kdom.abstractiontable;

import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableUtils;

public class AnswerReferenceCell extends AnswerReference {

	@Override
	public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> section) {
		Section<TableCellContent> columnHeader = TableUtils.getColumnHeader(section);
		return Sections.successor(columnHeader, QuestionReference.class);
	}

}
