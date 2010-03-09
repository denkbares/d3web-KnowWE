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

import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.knowledge.terminology.DiagnosisState;
import de.d3web.core.session.XPSCase;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.renderer.ObjectInfoLinkRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

/**
 * 
 * @author Johannes Dienst
 * 
 * Highlights the Solutions in CoveringList according to state.
 * Also Includes the ObjectInfoLinkRenderer.
 *
 */
public class SolutionIDHighlightingRenderer extends KnowWEDomRenderer {

	private static SolutionIDHighlightingRenderer instance;
	
	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		String solution = sec.getOriginalText().replace("\"", "").trim();

		XPSCase xpsCase = D3webUtils.getXPSCase(sec, user);

		String spanStart = KnowWEUtils.maskHTML("<span style=\"background-color: rgb(");
		String spanStartEnd = KnowWEUtils.maskHTML(";\">");
		String spanEnd = KnowWEUtils.maskHTML("</span>");
		
		if (xpsCase != null) {
			
			List<Diagnosis> diags = xpsCase.getKnowledgeBase().getDiagnoses();
			Collection <KnowledgeSlice> slices =
				xpsCase.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);

			for (Diagnosis d : diags) {

				if (d.getText().equals(solution)) {
					DiagnosisState state; 
					XCLModel diagModel = this.findModel(solution, slices);
					
					if (diagModel == null)
						state = DiagnosisState.UNCLEAR;
					else
						state = diagModel.getState(xpsCase);

					if (state == DiagnosisState.ESTABLISHED) {
						string.append(spanStart + "51, 255, 51)" + spanStartEnd);
					}

					if (state == DiagnosisState.EXCLUDED) {
						string.append(spanStart + "255, 153, 0)" + spanStartEnd);
					}

					if (state == DiagnosisState.SUGGESTED) {
						string.append(spanStart + "220, 200, 11)" + spanStartEnd);
					}

					if (state == DiagnosisState.UNCLEAR) {
						string.append(spanStart + ")" + spanStartEnd);
					}
				}
			}
		} else {
			string.append("");
		}
		
		new ObjectInfoLinkRenderer(
				FontColorRenderer.getRenderer(FontColorRenderer.COLOR1)).
					render(article, sec, user, string);
		string.append(spanEnd);
	}
	
	/**
	 * Finds a Model from a KnowledgeSlice list.
	 * 
	 * @param solution
	 * @return
	 */
	private XCLModel findModel(String solution, Collection<KnowledgeSlice> slices) {
		for (KnowledgeSlice s : slices) {
			if (((XCLModel)s).getSolution().getText().equals(solution))
				return (XCLModel)s;
		}
		return null;
	}

	/**
	 * Singleton.
	 * 
	 * @return
	 */
	public static SolutionIDHighlightingRenderer getInstance() {
		if (instance == null)
			instance = new SolutionIDHighlightingRenderer();
		
		return instance;
	}

}
