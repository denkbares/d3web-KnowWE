package de.d3web.we.kdom.rules;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.rule.D3ruleBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.kopic.RulesSection;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.terminology.D3webTerminologyHandler;

public class RuleSubTreeHandler implements ReviseSubTreeHandler {

	@Override
	public void reviseSubtree(Section s, KnowledgeRepresentationManager tm, String web,
			KnowWEDomParseReport rep) {

		KnowledgeRepresentationHandler handler = tm.getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			KnowledgeBaseManagement kbm = ((D3webTerminologyHandler) handler)
					.getKBM(s.getTopic());


			D3ruleBuilder builder = new D3ruleBuilder(s.getId(), true, new SingleKBMIDObjectManager(kbm));
			Section content = s;
			if (content != null) {
				String text = AbstractKopicSection.removeIncludedFromTags(content.getOriginalText());
				Reader r = new StringReader(text);
				List<Message> bm = builder
						.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
				
				Section ruleSection = s.getFather();
				while (ruleSection != null && !(ruleSection.getObjectType() instanceof  RulesSection)) {
					ruleSection = ruleSection.getFather();
				}
				if (ruleSection != null && ruleSection.getObjectType() instanceof  RulesSection) {				
					List<Message> msgs = ((RulesSection) ruleSection.getObjectType()).getMessages(ruleSection);
					
					if (bm.size() == 1 && bm.get(0).getMessageType().equals(Message.NOTE) && !msgs.isEmpty()) {
						// TODO: Ugly, find better way!!
						String newm = bm.get(0).getMessageText();
						Pattern parsed = Pattern.compile("(\\A" + Pattern.quote(newm.substring(0, newm.length() - 1)) + ")(\\d+)");
						Matcher ma1 = parsed.matcher(newm);
						int add = 0;
						int count = 0;
						if (ma1.find()) {
							add = Integer.parseInt(ma1.group(2));
							for (Message m:msgs) {
								String oldm = m.getMessageText();
								Matcher ma2 = parsed.matcher(oldm);
								if (m.getMessageType().equals(Message.NOTE) 
										&& ma2.find()) {
									count = Integer.parseInt(ma2.group(2));
									count += add;
									m.setMessageText(ma2.group(1) + count);
									break;
								}
							}
						}
					} else {
						if (bm.size() == 1 && bm.get(0).getMessageType().equals(Message.NOTE)) {
							msgs.addAll(bm);
						} else {
							((Rule) s.getObjectType()).getMessages(s).addAll(bm);
						}
					}
				}
				Report ruleRep = new Report();
				for (Message messageKnOffice : bm) {
					ruleRep.add(messageKnOffice);
				}
				KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
						.getTopic(), s.getOriginalText());
				rep.addReport(result);
			}
		}
	}

}
