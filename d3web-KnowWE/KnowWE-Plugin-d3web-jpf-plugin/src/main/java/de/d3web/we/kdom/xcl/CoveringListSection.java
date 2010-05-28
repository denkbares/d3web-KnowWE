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

package de.d3web.we.kdom.xcl;

import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.kopic.renderer.SetCoveringListSectionRenderer;

public class CoveringListSection extends AbstractKopicSection {

	public static final String TAG = "SetCoveringList-section";
	
	public CoveringListSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		this.childrenTypes.add(new CoveringListContent());
		//subtreeHandler.add(new CoveringListSectionSubTreeHandler());
		setCustomRenderer(new SetCoveringListSectionRenderer());
	}
	
//	private class CoveringListSectionSubTreeHandler extends D3webReviseSubTreeHandler {
//	
//		@Override
//		public void reviseSubtree(KnowWEArticle article, Section s) {
//	
//			KnowledgeBaseManagement kbm = getKBM(article, s);
//			
//			if (kbm != null) {
//				
//				if (AbstractXMLObjectType.getAttributeMapFor(s).containsKey("parser-version")
//						&& AbstractXMLObjectType.getAttributeMapFor(s).get("parser-version").trim().equals("1")) {
//					callOldParser(s, s.getReport(), kbm);
//				} else {
//					callNewParser(s, s.getReport(), kbm);
//				}
//			}
//		}
//	
//		private void callOldParser(Section s, KnowWEDomParseReport rep,
//				KnowledgeBaseManagement kbm) {
//	
//			Section content = ((AbstractKopicSection) s.getObjectType()).getContentChild(s);
//			
//			if (content != null) {
//				
//				StringBuffer buffi = new StringBuffer(TextInclude.removeTextIncludeTags(content.getOriginalText()));
//				Report xclRep = XCLParserHelper.getXCLModel
//					(kbm.getKnowledgeBase(), buffi);
//	
//				KnowWEParseResult result = new KnowWEParseResult(xclRep, s
//						.getTitle(), s.getOriginalText());
//				rep.addReport(result);
//			}
//		}
//	
//		private void callNewParser(Section s, KnowWEDomParseReport rep,
//				KnowledgeBaseManagement kbm) {
//			
//			XCLd3webBuilder builder = new XCLd3webBuilder(s.getId(), true,
//					false, new SingleKBMIDObjectManager(kbm));
//	
//			Section content = ((AbstractKopicSection) s.getObjectType()).getContentChild(s);
//			if (content != null) {
//				Reader r = new StringReader(TextInclude.removeTextIncludeTags(content.getOriginalText()));
//				List<Message> col = builder.addKnowledge(r, new SingleKBMIDObjectManager(kbm),
//						null);
//				storeMessages(s,col);
//				Report xclRep = new Report();
//				for (Message messageKnOffice : col) {
//					xclRep.add(messageKnOffice);
//				}
//				KnowWEParseResult result = new KnowWEParseResult(xclRep, s
//						.getTitle(), s.getOriginalText());
//				rep.addReport(result);
//			}
//		}
//	}
}
