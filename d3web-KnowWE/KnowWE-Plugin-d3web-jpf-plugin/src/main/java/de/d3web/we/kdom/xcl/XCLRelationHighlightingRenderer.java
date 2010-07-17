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
import de.d3web.core.session.Session;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.renderer.FontColorBackgroundRenderer;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.inference.PSMethodXCL;

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
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		
		String kbrelId = (String)KnowWEUtils.getStoredObject(sec.getArticle().getWeb(), sec.getTitle(), sec.getID(), CoveringListContent.KBID_KEY);
		
		StringBuilder buffi = new StringBuilder();
		if (kbrelId == null) {
			DelegateRenderer.getInstance().render(article, sec, user, buffi);
			string.append(new StringBuilder(KnowWEUtils.maskHTML(buffi.toString())));
			return;
		}

		Session session = D3webUtils.getSession(article.getTitle(), user, article.getWeb());
		
		if (session != null) {
			
			// Get the KnowledgeSlices from KB and find the XCLRelation to be rendered
			Collection<KnowledgeSlice> models = session.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice knowledgeSlice : models) {
				if(knowledgeSlice instanceof XCLModel) {
					// Check if model contains the relation
					XCLRelation relation = ((XCLModel)knowledgeSlice).findRelation(kbrelId);
					
					if (relation != null) { // nothing gets rendered if relation is null??
						
						// eval the Relation to find the right Rendering
						try {
							boolean fulfilled = relation.eval(session);
							// Highlight Relation
							string.append( this.renderRelation(article, sec, user, fulfilled));
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
		string.append(this.renderText(article, sec, user));
		
	}
	
	
	private String renderText(KnowWEArticle article, Section sec, KnowWEUserContext user) {

		StringBuilder buffi = new StringBuilder();
		
		FontColorBackgroundRenderer.getRenderer(null, null).render(article, sec, user, buffi);
		return KnowWEUtils.maskHTML(buffi.toString());
		
		
	}
	

	/***
	 * Replaces the SpecialDelegateRenderer functionality to enable highlighting
	 * of only Relations without their RelationWeights.
	 * 
	 * @param sec
	 * @param user
	 * @param web
	 * @param topic
	 * @param fulfilled 
	 * @return
	 */
	private String renderRelation(KnowWEArticle article, Section sec, KnowWEUserContext user, boolean fulfilled) {

		StringBuilder buffi = new StringBuilder();
		
		// b true: Color green
		if (fulfilled) {
			// Iterate over children of the relation.
			// When (Complex)Finding call the FontcolorBackgroundRenderer
			 List<Section> children = sec.getChildren();
			for (Section s: children) {
				buffi.append(this.renderRelationChild(article, s, fulfilled, user, "#33FF33"));
			}
			
		} else {
			// b false: Color red
			 List<Section> children = sec.getChildren();
			for (Section s: children) {
				buffi.append(this.renderRelationChild(article, s, fulfilled, user, "#FF9900"));
			}
			
		}
		return KnowWEUtils.maskHTML(buffi.toString());
	}

	
	
	
	private String renderRelationChild(KnowWEArticle article, Section sec, boolean fulfilled, KnowWEUserContext user, String color) {
		StringBuilder buffi = new StringBuilder();
		KnowWEObjectType type = sec.getObjectType();
		
		if (type instanceof ComplexFinding) {
			((ComplexFinding)type).
				getBackgroundColorRenderer(color).render(article, sec, user, buffi);
		} else if (type instanceof XCLRelationWeight) { //renders contradiction in red if fulfilled
			
			if (fulfilled && sec.getOriginalText().trim().equals("[--]")) {
				FontColorBackgroundRenderer.getRenderer(FontColorRenderer.COLOR2, null).render(article, sec, user, buffi);
			} else {
				type.getRenderer().render(article, sec, user, buffi);
			}
			
		} else {
			type.getRenderer().render(article, sec, user, buffi);
		}

		return buffi.toString();
	}
}
