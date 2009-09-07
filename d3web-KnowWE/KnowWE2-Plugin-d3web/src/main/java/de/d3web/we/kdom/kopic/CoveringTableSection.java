package de.d3web.we.kdom.kopic;

import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.table.D3webBuilder;
import de.d3web.KnOfficeParser.table.TableParser;
import de.d3web.KnOfficeParser.table.TableParser2;
import de.d3web.KnOfficeParser.table.XCLRelationBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.renderer.KopicTableSectionRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class CoveringTableSection extends AbstractKopicSection {

	public static final String TAG = "SetCoveringTable-section";

	public CoveringTableSection() {
		super(TAG);
		renderer = new KopicTableSectionRenderer();
	}

	@Override
	protected void init() {
		childrenTypes.add(new CoveringTableContent());
	}

	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm,
			String web, KnowWEDomParseReport rep) {
		
		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());

			TableParser parser = new TableParser2();
			D3webBuilder builder = new D3webBuilder(s.getId(),
					new XCLRelationBuilder("xcl"), 0, 0, parser, new SingleKBMIDObjectManager(kbm));

			builder.setLazy(true);
			builder.setLazyDiag(true);

			Section content = this.getContentChild(s);
			if (content != null) {
				parser.parse(removeIncludedFromTags(content.getOriginalText()));

				List<Message> errors = builder.checkKnowledge();

				messages.put(s, errors);
				Report ruleRep = new Report();
				for (Message messageKnOffice : errors) {
					ruleRep.add(messageKnOffice);
				}
				KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
						.getTopic(), removeIncludedFromTags(s.getOriginalText()));
				rep.addReport(result);
			}
		}
	}
}
