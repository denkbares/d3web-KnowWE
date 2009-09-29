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

package de.d3web.we.action;

import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.xcl.CoveringListSection;

/**
 * 
 * @author smark
 */
public class SaveDialogAsXCLAction implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		String topic = parameterMap.get(KnowWEAttributes.TOPIC);
		String user = parameterMap.getUser();
		String web = parameterMap.getWeb();
		String solution = parameterMap.get( "XCLSolution" );
		
		XPSCase c = getXPSCase(web, topic, user);
		
		if( c != null ){
			StringBuffer newXCL = new StringBuffer();
			newXCL.append("\n\"" + solution + "\" {\n");
			
			List<? extends Question> answeredQuestions = c.getAnsweredQuestions();

			Diagnosis d = findDiagnosis(web, topic, solution);
			if( isDiagnosisNew( d )){
				d = getKBM( c.getKnowledgeBase() ).createDiagnosis(solution, c.getKnowledgeBase().getRootDiagnosis());
			} else {
				return null;
			}
						
			//build relations
			createXCLRelation(c, answeredQuestions, newXCL, d);
			
			newXCL.append("}\n");
			
			//insert new XCLRelation into article
			KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(parameterMap.getWeb());
			KnowWEArticle a = mgr.getArticle( topic );
			String articleText = a.collectTextsFromLeaves();
			
			articleText = articleText.replace("</" + CoveringListSection.TAG + ">", newXCL + "\n</" + CoveringListSection.TAG + ">");
			KnowWEEnvironment.getInstance().saveArticle(web, topic, articleText, parameterMap);
		
		}		
		return null;
	}
	
	/**
	 * 
	 * @param c
	 * @param answeredQuestions
	 * @param content
	 */
	private void createXCLRelation(XPSCase c, List<? extends Question> answeredQuestions, StringBuffer newXCL,
			Diagnosis d){
		for( Question q : answeredQuestions ){
			List answers = q.getValue( c );
			for (Object o : answers) {
				if( o instanceof Answer ){
					Answer a = (Answer) o;

					newXCL.append("\"" + q.getText() + "\" = \"" + a.getValue( c ) + "\",\n");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param d
	 * @return
	 */
	private boolean isDiagnosisNew(Diagnosis d){
		if(d == null) return true;
		return false;
	}
		
	/**
	 * @param web
	 * @param topic
	 * @param user
	 * @return
	 */
	private XPSCase getXPSCase(String web, String topic, String user){
		D3webKnowledgeService knowledgeServiceInTopic = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);
		D3webKnowledgeService service = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);	
		service.getBase();
		
		if(knowledgeServiceInTopic == null) return null;
		String kbid = knowledgeServiceInTopic.getId();
		
		Broker broker = D3webModule.getBroker(user,web);
		broker.activate(broker.getSession().getServiceSession(kbid), null, true, false, null);
		broker.getDialogControl().showNextActiveDialog();
		KnowledgeServiceSession serviceSession = broker.getSession().getServiceSession(kbid);
		XPSCase c = null;
		if(serviceSession instanceof D3webKnowledgeServiceSession) {
			c = ((D3webKnowledgeServiceSession)serviceSession).getXpsCase();
		}
		if(serviceSession == null) {
			kbid =  KnowWEEnvironment.WIKI_FINDINGS+".."+KnowWEEnvironment.generateDefaultID(KnowWEEnvironment.WIKI_FINDINGS);
			 serviceSession = broker.getSession().getServiceSession(kbid);
			 if(serviceSession instanceof D3webKnowledgeServiceSession) {
					c = ((D3webKnowledgeServiceSession)serviceSession).getXpsCase();
				}
		}
		return c;
	}
	
	/**
	 * 
	 * @param web
	 * @param topic
	 * @param solution
	 * @return
	 */
	private Diagnosis findDiagnosis(String web, String topic, String solution){
		D3webKnowledgeService ks = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);
		
		Diagnosis d = getKBM( ks.getBase()).findDiagnosis(solution);
		return d;
	}
	
	/**
	 * 
	 * @param kb
	 * @return
	 */
	private KnowledgeBaseManagement getKBM(KnowledgeBase kb){
		return KnowledgeBaseManagement.createInstance( kb );
	}
	
}
