package de.knowwe.testcases.table;

import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.DefaultErrorRenderer;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;

/**
 * 
 * @author Reinhard Hatko
 * @created 18.01.2011
 */
final class CellAnswerRef extends AnswerReference {

	public CellAnswerRef() {
		setSectionFinder(new ConstraintSectionFinder(getSectionFinder(),
				SingleChildConstraint.getInstance()));
	}

	@Override
	public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {

		Section<? extends Type> headerCell = TestcaseTable.findHeaderCell(s);

		Section<QuestionReference> questionRef = Sections.findSuccessor(headerCell,
				QuestionReference.class);

		return questionRef;
	}

	@Override
	public MessageRenderer getErrorRenderer() {
		return DefaultErrorRenderer.INSTANCE_ERROR;
	}

	@Override
	public MessageRenderer getNoticeRenderer() {
		return null;
	}
}