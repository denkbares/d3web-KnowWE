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

package de.d3web.we.kdom.questionTreeNew.dialog;

import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.SubTree;
import de.d3web.we.kdom.rendering.CustomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.RenderingMode;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * QuestionDashTreeRenderer.
 * This renderer renders a collapsible/expandable decision tree. This tree
 * is used to guide the user when answering the questions of the
 * decision tree.
 * 
 * @author smark
 * @since 2010/03/09
 * @see KnowWEDomRenderer
 */
public class QuestionDashTreeRenderer extends CustomRenderer {

	@Override
	public boolean doesApply(String user, String topic, RenderingMode type) {
		return true;
	}		
	
	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {

		List<Section<? extends KnowWEObjectType>> children = sec.getChildren(); //get qclass lines 
		
		for (Section<? extends KnowWEObjectType> section : children) {
			renderSubtree(article, section, user, string, true);
		}		
	}
	
	/**
	 * 
	 * @param article
	 * @param section
	 * @param user
	 * @param string
	 * @param li
	 */
	private void renderSubtree(KnowWEArticle article,
			Section<? extends KnowWEObjectType> section,
			KnowWEUserContext user,
			StringBuilder string,
			boolean dte){
		
		string.append(KnowWEUtils.maskHTML("<ul>"));
		
		String c = (dte) ? "head" : "qline";
		
		for (Section<? extends KnowWEObjectType> s : section.getChildren()) {
			
			if( s.getObjectType() instanceof PlainText ) continue;
			
			if( (s.getObjectType() instanceof DashTreeElement 
					|| s.getChildren().size() <= 1) && dte) {
				
				string.append(KnowWEUtils.maskHTML("<li class=\""+c+"\">")); //head class
				c = "";
				DelegateRenderer.getInstance().render(article, s, user, string);
			} else if( s.getObjectType() instanceof SubTree ) {
				string.append(KnowWEUtils.maskHTML("<li class=\"qline\">")); //body class
				DelegateRenderer.getInstance().render(article, s.findChildOfType( DashTreeElement.class ), user, string);
				renderSubtreeChildren(article, s.findChildrenOfType(SubTree.class), user, string);
			}
			string.append(KnowWEUtils.maskHTML("</li>"));
		}
		string.append(KnowWEUtils.maskHTML("</ul>"));	
	}
	
	/**
	 * 
	 * @param article
	 * @param children
	 * @param user
	 * @param string
	 * @param li
	 */
	private void renderSubtreeChildren(KnowWEArticle article,
			List<Section<SubTree>> children,
			KnowWEUserContext user,
			StringBuilder string){
		
		string.append(KnowWEUtils.maskHTML("<ul>"));
		
		for (Section<? extends KnowWEObjectType> s : children) {		
			
			if( s.getChildren().size() > 1 ){
				string.append(KnowWEUtils.maskHTML("<li class=\"\">")); //head class
				DelegateRenderer.getInstance().render(article, s.findChildOfType( DashTreeElement.class ), user, string);
				renderSubtree(article, s, user, string, false);
			} else {
				if( !(s.getObjectType() instanceof PlainText)){
					string.append(KnowWEUtils.maskHTML("<li class=\"\">")); //body class
					DelegateRenderer.getInstance().render(article, s.findChildOfType( DashTreeElement.class ), user, string);
				}
			}
			//}
			string.append(KnowWEUtils.maskHTML("</li>"));
		}
		string.append(KnowWEUtils.maskHTML("</ul>"));
	}
}
