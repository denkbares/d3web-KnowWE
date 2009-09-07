package de.d3web.we.kdom.kopic;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.rule.D3ruleBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.complexRule.ComplexRuleParserManagement;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rules.RuleContent;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class RulesSection extends AbstractKopicSection {

	public static final String TAG = "Rules-section";

	public RulesSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new RuleContent());
		
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new RuleSectionRenderer();
	}

//	@Override
//	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
//			KnowWEDomParseReport rep) {
//
//		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
//		if (handler instanceof D3webTerminologyHandler) {
//			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
//					.getKBM(s.getTopic());
//
//			Collection<KnowledgeSlice> rules = kbm.getKnowledgeBase()
//					.getAllKnowledgeSlices();
//
//			if (this.getMapFor(s).containsKey("parser-version")
//					&& this.getMapFor(s).get("parser-version").trim().equals(
//							"1")) {
//				callOldParser(s,rep, kbm);
//			} else {
//				callNewParser(s, rep, kbm);
//			}
//		}
//	}
//
//	private void callOldParser(Section s, KnowWEDomParseReport rep,
//			KnowledgeBaseManagement kbm) {
//		Section content = this.getContentChild(s);
//		ComplexRuleParserManagement parser = new ComplexRuleParserManagement(
//				new StringReader(removeIncludedFromTags(removeIncludedFromTags(content.getOriginalText()))), kbm, null, false, false, false);
//		Report report = parser.getReport();
//		List<Message> knOffciceMessages = new ArrayList<Message>();
//		for (Message m : report.getAllMessages()) {
//			knOffciceMessages.add(new Message(m.getMessageType(), m.getMessageText(), m.getFilename(), m.getLineNo(), m.getColumnNo(), m.getLine()));
//		}
//		messages.put(s,knOffciceMessages);
//		
//		KnowWEParseResult result = new KnowWEParseResult(report, s
//				.getTopic(), removeIncludedFromTags(s.getOriginalText()));
//		rep.addReport(result);
//	}
//
//	private void callNewParser(Section s, KnowWEDomParseReport rep,
//			KnowledgeBaseManagement kbm) {
//		
//		Collection<KnowledgeSlice> rules = kbm.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodHeuristic.class);
//		
//		D3ruleBuilder builder = new D3ruleBuilder(s.getId(), true, new SingleKBMIDObjectManager(kbm));
//		Section content = this.getContentChild(s);
//		if (content != null) {
//			String text = removeIncludedFromTags(content.getOriginalText());
//			Reader r = new StringReader(text);
//			Collection<Message> col = builder
//					.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
//
//			Collection<KnowledgeSlice> rulesAfter = kbm.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodHeuristic.class);
//			
//
//			messages.put(s, col);
//			Report ruleRep = new Report();
//			for (Message messageKnOffice : col) {
//				ruleRep.add(messageKnOffice);
//			}
//			KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
//					.getTopic(), removeIncludedFromTags(s.getOriginalText()));
//			rep.addReport(result);
//		}
//	}

}
