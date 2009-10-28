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

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.condition.NegatedFinding;
import de.d3web.we.kdom.renderer.FontColorBackgroundRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Highlights XCLRelations.
 * Answer Right: Green
 * Answer wrong: Red
 * Answer unknown: No Highlighting
 * 
 * @author Johannes Dienst
 *
 */
public class XCLRelationHighlightingRenderer extends KnowWEDomRenderer {

	private static XCLRelationHighlightingRenderer instance;

	public static synchronized XCLRelationHighlightingRenderer getInstance() {
		if (instance == null)
			instance = new XCLRelationHighlightingRenderer();
		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		
		String kbrelId = (String)KnowWEUtils.getStoredObject(sec.getArticle().getWeb(), sec.getTitle(), sec.getId(), CoveringListContent.KBID_KEY);
		
		StringBuilder buffi = new StringBuilder();
		if (kbrelId == null) {
			DelegateRenderer.getInstance().render(sec, user, buffi);
			string = new StringBuilder(KnowWEEnvironment.maskHTML(buffi.toString()));
			return;
		}

		XPSCase xpsCase = D3webUtils.getXPSCase(sec, user);
		
		if (xpsCase != null) {
			
			// Get the KnowledgeSlices from KB and find the XCLRelation to be rendered
			Collection<KnowledgeSlice> models = xpsCase.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice knowledgeSlice : models) {
				if(knowledgeSlice instanceof XCLModel) {
					
					// Check if model contains the relation
					if (((XCLModel)knowledgeSlice).findRelation(kbrelId) != null) {
						
						// eval the Relation to find the right Rendering
						try {
							boolean b = ((XCLModel)knowledgeSlice).findRelation(kbrelId).eval(xpsCase);
							// Highlight Relation
							string.append( this.renderRelationChildren(sec, user, true, b));
							return;
						} catch (Exception e) {
							// Call the XCLRelationMarkerHighlightingRenderer
							// without any additional info
//							string.append(this.renderRelationChildren(sec, user, false, false);
						}
					}
				}
			} 		
		}
		
		string.append(this.renderRelationChildren(sec, user, false, false));
	}

	/***
	 * Replaces the SpecialDelegateRenderer functionality to enable highlighting
	 * of only Relations without their RelationWeights.
	 * 
	 * @param sec
	 * @param user
	 * @param web
	 * @param topic
	 * @param b 
	 * @return
	 */
	private String renderRelationChildren(Section sec, KnowWEUserContext user,
			boolean flag, boolean b) {

		StringBuilder buffi = new StringBuilder();
		
		if (!flag) {
			FontColorBackgroundRenderer.getRenderer(null, null).render(sec, user, buffi);
			return KnowWEEnvironment.maskHTML(buffi.toString());
		}
		
		// b true: Color green
		if (b) {
			// Iterate over children of the relation.
			// When (Complex)Finding call the FontcolorBackgroundRenderer
			for (Section s: sec.getChildren()) {
				buffi.append(this.renderRelationChild(s, user, "#33FF33"));
			}
			
		} else {
			// b false: Color red
			for (Section s: sec.getChildren()) {
				buffi.append(this.renderRelationChild(s, user, "#FF9900"));
			}
			
		}
		return KnowWEEnvironment.maskHTML(buffi.toString());
	}

	private String renderRelationChild(Section sec, KnowWEUserContext user, String color) {
		StringBuilder buffi = new StringBuilder();
		if (sec.getObjectType() instanceof ComplexFinding) {
			((ComplexFinding)sec.getObjectType()).
				getBackgroundColorRenderer(color).render(sec, user, buffi);
		} else if (sec.getObjectType() instanceof Finding) {
			((Finding)sec.getObjectType()).
				getBackgroundColorRenderer(color).render(sec, user, buffi);
		} else if (sec.getObjectType() instanceof NegatedFinding) {
			((NegatedFinding)sec.getObjectType()).
				getBackgroundColorRenderer(color).render(sec, user, buffi);
		} else {
			sec.getObjectType().getRenderer().render(sec, user, buffi);
		}

		return buffi.toString();
	}
}
