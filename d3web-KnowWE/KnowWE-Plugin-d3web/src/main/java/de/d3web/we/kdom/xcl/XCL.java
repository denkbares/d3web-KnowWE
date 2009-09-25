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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;
import de.d3web.textParser.xclPatternParser.XCLParserHelper;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.javaEnv.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

public class XCL extends AbstractXMLObjectType {

	
	public XCL() {
		super("XCL");
	}
	
	@Override
	public void init() {
		subtreeHandler.add(new XCLSubTreeHandler());
	}
	
	private class XCLSubTreeHandler extends D3webReviseSubTreeHandler {

		@Override
		public void reviseSubtree(Section s) {
			
			KnowledgeBaseManagement kbm = getKBM(s);
			
			if (kbm != null) {
				
				String parseText = s.getOriginalText();
	
				// HOTFIX
				parseText = parseText.replaceAll("<XCL>", "");
				parseText = parseText.replaceAll("</XCL>", "");
	
				Report p = new Report();
	
				Report singleReport = XCLParserHelper.getXCLModel(kbm
						.getKnowledgeBase(), new StringReader(parseText));
				p.addAll(singleReport);
				s.getArticle().getReport().addReport(new KnowWEParseResult(p, s.getTitle(), s
						.getOriginalText()));
				
				DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, s.getTitle(), s.getArticle().getWeb());
			}
		}
	}



	@Override
	public List<KnowWEObjectType> getAllowedChildrenTypes() {
		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(new XCList());
		return types;
	}
}
