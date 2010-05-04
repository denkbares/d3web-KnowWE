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
import java.util.Set;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.SubTree;
import de.d3web.we.kdom.questionTreeNew.QuestionDashTreeElementContent;
import de.d3web.we.kdom.rendering.CustomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.RenderingMode;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * This renderer renders a collapsible/expandable decision tree. This tree
 * is used to guide the user when answering the questions of the
 * decision tree. This renderer only renders the answers of the decision tree.
 * If the question should be present, please use the 
 * {@link QuestionDashTreeRenderer} which provides also the questions.
 * 
 * @author smark
 * @since 2010/03/25
 * @see KnowWEDomRenderer
 */
public class QuestionDashTreeOnlyAnswersRenderer extends CustomRenderer {

	@Override
	public boolean doesApply(String user, String topic, RenderingMode type) {
		return true;
	}		
	
	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {

		List<Section<? extends KnowWEObjectType>> children = sec.getChildren(); //get qclass lines 
		
		for (Section<? extends KnowWEObjectType> section : children) {
			parseSubtree(article, section, user, string, true);
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
	private void parseSubtree(KnowWEArticle article,
			Section<? extends KnowWEObjectType> section,
			KnowWEUserContext user,
			StringBuilder string,
			boolean dte){
		
		string.append(KnowWEUtils.maskHTML("<ul>"));
	
		for (Section<? extends KnowWEObjectType> s : section.getChildren()) {
			
			if( s.getObjectType() instanceof PlainText ) continue;
			
            if( s.getObjectType() instanceof SubTree ) {
				//string.append(KnowWEUtils.maskHTML("<li class=\"qline pointer\"><p>")); //body class
				
				List<Section<SubTree>> children = s.findChildrenOfType(SubTree.class);
				if(children.size() < 1) {
					string.append(KnowWEUtils.maskHTML("<li class=\"qanswer\"><p>"));
					
					renderLine( article, s.findChildOfType( DashTreeElement.class ), user, string);
					//DelegateRenderer.getInstance().render(article, s.findChildOfType( DashTreeElement.class ), user, string);
				} else {
					string.append(KnowWEUtils.maskHTML("<li class=\"qline\"><p>"));
				}
				parseSubtreeChildren(article, children, user, string);
				string.append(KnowWEUtils.maskHTML("</p></li>"));
			}
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
	private void parseSubtreeChildren(KnowWEArticle article,
			List<Section<SubTree>> children,
			KnowWEUserContext user,
			StringBuilder string){
		
		string.append(KnowWEUtils.maskHTML("<ul>"));
		
		for (Section<? extends KnowWEObjectType> s : children) {		
			
			if( s.getChildren().size() > 1 ){
				string.append(KnowWEUtils.maskHTML("<li class=\"\"><p>")); //head class
				
				renderLine( article, s.findChildOfType( DashTreeElement.class ), user, string);
				//DelegateRenderer.getInstance().render(article, s.findChildOfType( DashTreeElement.class ), user, string);
				parseSubtree(article, s, user, string, false);
			} else {
				if( !(s.getObjectType() instanceof PlainText)){
					string.append(KnowWEUtils.maskHTML("<li class=\"\"><p>")); //body class
					renderLine( article, s.findChildOfType( DashTreeElement.class ), user, string);
					//DelegateRenderer.getInstance().render(article, s.findChildOfType( DashTreeElement.class ), user, string);
				}
			}
			//}
			string.append(KnowWEUtils.maskHTML("</p></li>"));
		}
		string.append(KnowWEUtils.maskHTML("</ul>"));
	}
	/**
	 * Renders the collapsible tree and adds if found some additional information that
	 * is stored within an {@link MMInfoStorage} object.
	 * 
	 * @param section
	 * @return
	 */
	private void renderLine(KnowWEArticle article, Section<? extends KnowWEObjectType> section, KnowWEUserContext user, StringBuilder string){
		//render the current section
		DelegateRenderer.getInstance().render(article, section, user, string); 
		
		//render the additional information icon
		Session theCase = D3webUtils.getSession(article.getTitle(), user, article.getWeb());
		if (theCase != null) {
			Section<? extends KnowWEObjectType> child = section.findChildOfType(QuestionDashTreeElementContent.class);
			String name = child.getOriginalText();

			KnowledgeBaseManagement kbm = D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, null, section);
			
			NamedObject o = kbm.findQuestion( name );

			if( o != null ){
			    MMInfoStorage mminfo = (MMInfoStorage) o.getProperties().getProperty(Property.MMINFO);
			    if( mminfo != null) {
				    DCMarkup markup = new DCMarkup();
				    markup.setContent(DCElement.TITLE, "description");
	    	        Set<MMInfoObject> result = mminfo.getMMInfo(markup);
	    	        
	    	        String attr = "";
			        for (MMInfoObject infoObject : result) {
			        	attr = infoObject.getContent();
			        }
			        String imgAlt = D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.QuestionDashTree.image.altdescription");
			        string.append(KnowWEUtils.maskHTML("<img class=\"collapsible-info\" rel=\"{info : '"+attr+"'}\" src=\"KnowWEExtension/images/question.gif\" height=\"16\" width=\"16\" title=\""+imgAlt+"\"/>"));
			    }
			}
		}          
	}	
}
