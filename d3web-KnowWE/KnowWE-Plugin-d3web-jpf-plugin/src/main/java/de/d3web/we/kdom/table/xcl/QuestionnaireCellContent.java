package de.d3web.we.kdom.table.xcl;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class QuestionnaireCellContent extends
		CoveringTableHeaderColumnCellContent {

	private static QuestionnaireCellContent instance = null;

	public static QuestionnaireCellContent getInstance() {
		if (instance == null) {
			instance = new QuestionnaireCellContent();

		}

		return instance;
	}

	private QuestionnaireCellContent() {
		this.addSubtreeHandler(new QuestionnaireCellHandler());
	}

	class QuestionnaireCellHandler extends D3webSubtreeHandler<QuestionnaireCellContent> {

		public static final String KEY_REPORT = "report_message";

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionnaireCellContent> s) {

			KnowledgeBaseManagement mgn = getKBM(article);

			if (mgn == null) {
				return null;
			}

			SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);

			String name = trimQContainerDeclarationSyntax(s.getOriginalText());

			QContainer q = mgr.findQContainer(name);

			if (q == null) {
				// create question
				QContainer q2 = mgr.createQContainer(name, mgr.getKbm()
						.getKnowledgeBase().getRootQASet());
				if (mgr.getKnowledgeBase().getInitQuestions().size() == 0) {
					ArrayList<QASet> tmp = new ArrayList<QASet>();
					tmp.add(q2);
					mgr.getKnowledgeBase().setInitQuestions(tmp);
				}

				if (q2 != null) {
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
							"Created questionnaire " + " : " + name));
				}
				else {
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
							"Failed creating questionnaire " + " : " + name));
				}

			}
			else {
				// TODO: CHECK Type match!
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Questionnaire already defined: " + name));

			}

			return null;

		}
	}

	// TODO refactor
	public static boolean hasQContainerDeclarationSyntax(String text) {
		return text.trim().startsWith("-") && text.trim().endsWith("-");
	}

	// TODO refactor
	public static String trimQContainerDeclarationSyntax(String text) {
		if (hasQContainerDeclarationSyntax(text)) {
			text = text.trim();
			text = text.substring(1, text.length() - 1);
			return text.trim();
		}

		return text;
	}
}
