package de.d3web.we.kdom.kopic;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.dashtree.SolutionsBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class SolutionsSection extends AbstractKopicSection {

	public static final String TAG = "Solutions-section";
	
	public SolutionsSection() {
		super(TAG);
	}
	
	@Override
	protected void init() {
		childrenTypes.add(new SolutionsContent());
	}
	
	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web, 
			KnowWEDomParseReport rep) {

		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		
		if (handler instanceof D3webTerminologyHandler) {
			
			KnowledgeBaseManagement kbm = 
				((D3webTerminologyHandler) handler).getKBM(s.getTopic());

			Section content = this.getContentChild(s);

			if (content != null) {

				List<de.d3web.report.Message> messages = SolutionsBuilder
						.parse(new StringReader(removeIncludedFromTags(content.getOriginalText())), kbm, new SingleKBMIDObjectManager(kbm));

				this.messages.put(s, messages);
				Report ruleRep = new Report();
				for (Message messageKnOffice : messages) {
					ruleRep.add(messageKnOffice);
				}
				KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
						.getTopic(), s.getOriginalText());
				rep.addReport(result);
			}
		}
	}
	
	public void reviseSubtreeOld(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {

		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());
			Reader r = new StringReader(removeIncludedFromTags(s.getOriginalText()));
			de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner dhs = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Scanner(
					r);
			de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser dhp = new de.d3web.textParser.cocor.diagnosisHierarchyParser.Parser(
					dhs, kbm.getKnowledgeBase(), false);
			dhp.Parse();
			List<Message> l = dhp.getErrorMessages();

			rep.addReport(new KnowWEParseResult(new Report(l), s.getTopic(), 
					removeIncludedFromTags(s.getOriginalText())));

		}
	}

}
