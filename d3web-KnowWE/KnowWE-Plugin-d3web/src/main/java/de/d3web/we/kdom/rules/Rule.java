/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.rules;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.rule.D3ruleBuilder;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParseResult;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.include.IncludedFromType;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;
import de.d3web.we.terminology.KnowledgeRecyclingObjectType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class Rule extends DefaultAbstractKnowWEObjectType implements KnowledgeRecyclingObjectType {
	
	public static final String KBID_KEY = "kbid";
	public static final String KDOMID_KEY = "kdomid";
	
	@Override
	protected void init() {
		subtreeHandler.add(new RuleSubTreeHandler());
		sectionFinder = new RuleSectionFinder();
		setCustomRenderer(new RuleRenderer());
		this.childrenTypes.add(new RuleActionLine());
		this.childrenTypes.add(new RuleCondLine());
		
	}
	

	@Override
	public void cleanKnowledge(Section s, KnowledgeBaseManagement kbm) {
		OldRulesEraser.deleteRules(s, kbm);
	}
	
	
	private class RuleRenderer extends KnowWEDomRenderer{
		
//		@Override
//		public String render(Section sec, KnowWEUserContext user) {
//			
//			List<Message> messages = ((Rule) sec.getObjectType()).getMessages(sec);
//			
//			StringBuilder result = new StringBuilder();
//			
//			result.append(KnowWEEnvironment.maskHTML("<pre><span id='"+sec.getId()
//					+"' class = 'XCLRelationInList'><span id=\"\">"));
//			
//			if (messages != null && !messages.isEmpty()) {
//				for (Message m : messages) {
//					result.append(m.getMessageType()+": " + m.getMessageText() 
//							+ (m.getMessageType().equals(Message.NOTE) ? " " + m.getCount() : " Line: " + m.getLineNo()));
//					result.append(KnowWEEnvironment.maskHTML("\n"));
//				}
//				result.append(KnowWEEnvironment.maskHTML("\n"));
//			}
//			
//			result.append(DelegateRenderer.getInstance().render(sec,
//					user) + KnowWEEnvironment.maskHTML("</pre></span></span>\n"));
//
//			return result.toString();
//		}
		
		@Override
		public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
			
			List<Message> messages = ((Rule) sec.getObjectType()).getMessages(sec);
			
			string.append(KnowWEEnvironment.maskHTML("<pre><span id='"+sec.getId()
					+"' class = 'XCLRelationInList'><span id=\"\">"));
			
			if (messages != null) {
				boolean empty = true;
				for (Message m : messages) {
					if (!m.getMessageText().equals(
							MessageKnOfficeGenerator.getResourceBundle().
							getString("rule"))) {
						empty = false;
						string.append(m.getMessageType()+": " + m.getMessageText() 
								+ (m.getMessageType().equals(Message.NOTE) ? " " + m.getCount() : " Line: " + m.getLineNo()));
						string.append(KnowWEEnvironment.maskHTML("\n"));
					}
				}
				if (!empty)
					string.append(KnowWEEnvironment.maskHTML("\n"));
			}
			
			StringBuilder b = new StringBuilder();
			DelegateRenderer.getInstance().render(sec, user, b);
			string.append(b.toString() + KnowWEEnvironment.maskHTML("</pre></span></span>\n"));
		}
	}
	
	public class RuleSubTreeHandler extends D3webReviseSubTreeHandler {

		@Override
		public void reviseSubtree(Section s) {
			
			boolean lazy = false ;
			
			Map<String,String> attributes = AbstractXMLObjectType.getAttributeMapFor(s);
			if(attributes != null && attributes.containsKey("lazy")) {
				String l = attributes.get("lazy");
				if(l != null) {
					if(l.equals("1") | l.equals("on") | l.equals("true") | l.equals("an") ) {
						lazy = true;
					}
				}
			}
			
			KnowledgeBaseManagement kbm = getKBM(s);
			
			if (kbm != null) {
				
				D3ruleBuilder builder = 
					new D3ruleBuilder(s.getId(), lazy,
							new SingleKBMIDObjectManager(kbm));
				
				if (s != null) {
					String text = 
						IncludedFromType.removeIncludedFromTags(s.getOriginalText());
					Reader r = new StringReader(text);
					
					List<Message> bm = 
						builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm), null);
					
					if (builder.getRuleIDs().size() == 1) {
						KnowWEUtils.storeSectionInfo(
								s.getArticle().getWeb(), s.getTitle(), s.getId(),
								KBID_KEY, builder.getRuleIDs().get(0));
					}
					
					((Rule) s.getObjectType()).storeMessages(s, bm);
					
					Report ruleRep = new Report();
					for (Message messageKnOffice : bm) {
						ruleRep.add(messageKnOffice);
					}
					KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
							.getTitle(), s.getOriginalText());
					s.getArticle().getReport().addReport(result);
				}
			}			
		}
	}
	
	public class RuleSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			ArrayList<SectionFinderResult> result =
						new ArrayList<SectionFinderResult>();

			Pattern p = Pattern.compile 
				//("(IF|WENN).*?(?=(\\s*IF|\\s*WENN|\\s*<[/]?includedFrom[^>]*?>|\\s*\\z))", Pattern.DOTALL);
				("(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN|\\s*<[/]?includedFrom[^>]*?>|\\s*\\z))", Pattern.DOTALL);
			Matcher m = p.matcher(text);
			
			while(m.find()) {
				result.add(new SectionFinderResult(m.start(), m.end()));
			}
		
			return result;
		}
	}


}
