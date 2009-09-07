package de.d3web.we.kdom.xcl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;
import de.d3web.textParser.xclPatternParser.XCLParserHelper;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class XCL extends AbstractXMLObjectType {

	
	public XCL() {
		super("XCL");
	}
	

	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {
		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());
			String parseText = s.getOriginalText();

			// HOTFIX
			parseText = parseText.replaceAll("<XCL>", "");
			parseText = parseText.replaceAll("</XCL>", "");

			Report p = new Report();

			Report singleReport = XCLParserHelper.getXCLModel(kbm
					.getKnowledgeBase(), new StringReader(parseText));
			p.addAll(singleReport);
			rep.addReport(new KnowWEParseResult(p, s.getTopic(), s
					.getOriginalText()));
			
			DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, s.getTopic(), web);
		}
	}



	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(new XCList());
		return types;
	}
}
