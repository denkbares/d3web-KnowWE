package de.d3web.we.kdom.kopic;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.xcl.XCLd3webBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.xclPatternParser.XCLParserHelper;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.renderer.SetCoveringListSectionRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class CoveringListSection extends AbstractKopicSection {

	public static final String TAG = "SetCoveringList-section";

	public CoveringListSection() {
		super(TAG);
		renderer = new SetCoveringListSectionRenderer();
	}

	@Override
	protected void init() {
		childrenTypes.add(new CoveringListContent());
	}

	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {

		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());

			if (this.getMapFor(s).containsKey("parser-version")
					&& this.getMapFor(s).get("parser-version").trim().equals(
							"1")) {
				callOldParser(s, rep, kbm);
			} else {
				callNewParser(s, rep, kbm);
			}
		}
	}

	private void callOldParser(Section s, KnowWEDomParseReport rep,
			KnowledgeBaseManagement kbm) {

		Section content = this.getContentChild(s);
		
		if (content != null) {
			
			StringBuffer buffi = new StringBuffer(removeIncludedFromTags(content.getOriginalText()));
			Report xclRep = XCLParserHelper.getXCLModel
				(kbm.getKnowledgeBase(), buffi);

			KnowWEParseResult result = new KnowWEParseResult(xclRep, s
					.getTopic(), removeIncludedFromTags(s.getOriginalText()));
			rep.addReport(result);
		}
	}

	private void callNewParser(Section s, KnowWEDomParseReport rep,
			KnowledgeBaseManagement kbm) {
		
		XCLd3webBuilder builder = new XCLd3webBuilder(s.getId(), true,
				false, new SingleKBMIDObjectManager(kbm));

		Section content = this.getContentChild(s);
		if (content != null) {
			Reader r = new StringReader(removeIncludedFromTags(content.getOriginalText()));
			List<Message> col = builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm),
					null);
			messages.put(s, col);
			Report xclRep = new Report();
			for (Message messageKnOffice : col) {
				xclRep.add(messageKnOffice);
			}
			KnowWEParseResult result = new KnowWEParseResult(xclRep, s
					.getTopic(), removeIncludedFromTags(s.getOriginalText()));
			rep.addReport(result);
		}
	}
}
