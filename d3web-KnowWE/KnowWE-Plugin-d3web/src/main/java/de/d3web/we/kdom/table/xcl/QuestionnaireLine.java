package de.d3web.we.kdom.table.xcl;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableColumnHeaderCellContent;
import de.d3web.we.kdom.table.TableLine;

public class QuestionnaireLine extends TableLine{
	
	private static QuestionnaireLine instance = null;
	
	public static QuestionnaireLine getInstance() {
		if (instance == null) {
			instance = new QuestionnaireLine();
			
		}

		return instance;
	}
	
	private QuestionnaireLine() {
		this.addReviseSubtreeHandler(new QuestionnaireLineHandler());
	}
	
	
	static class QuestionnaireLineHandler implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			Section<TableColumnHeaderCellContent> headerCellContent = s.findSuccessor(TableColumnHeaderCellContent.class);
			headerCellContent.setType(QuestionnaireCellContent.getInstance());
			
		
			return null;
		}
		
	}

}
