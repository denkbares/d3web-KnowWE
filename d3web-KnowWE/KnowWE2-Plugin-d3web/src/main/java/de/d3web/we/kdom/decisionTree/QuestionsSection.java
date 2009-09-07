package de.d3web.we.kdom.decisionTree;

import java.io.StringReader;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.decisiontree.D3DTBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class QuestionsSection extends AbstractKopicSection {

	public static final String TAG = "Questions-section";

	public QuestionsSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new QuestionsSectionContent());

	}

	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {

		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());

			Section content = this.getContentChild(s);
			if (content != null) {

				List<de.d3web.report.Message> messages = D3DTBuilder
						.parse(new StringReader(removeIncludedFromTags(content.getOriginalText())), new SingleKBMIDObjectManager(kbm));

				this.messages.put(s, messages);
				Report ruleRep = new Report();
				for (Message messageKnOffice : messages) {
					ruleRep.add(messageKnOffice);
				}
				KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
						.getTopic(), removeIncludedFromTags(s.getOriginalText()));
				rep.addReport(result);
			}
		}
	}

}
