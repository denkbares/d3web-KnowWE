/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.action;

import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.xcl.CoveringListSection;

/**
 * 
 * @author smark
 */
public class SaveDialogAsXCLAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String topic = parameterMap.get(KnowWEAttributes.TOPIC);
		String user = parameterMap.getUser();
		String web = parameterMap.getWeb();
		String solution = parameterMap.get("XCLSolution");

		Session c = getSession(web, topic, user);

		if (c != null) {
			StringBuffer newXCL = new StringBuffer();
			newXCL.append("\n\"" + solution + "\" {\n");

			List<? extends Question> answeredQuestions = c.getBlackboard().getAnsweredQuestions();

			Solution d = findSolution(web, topic, solution);
			if (isSolutionNew(d)) {
				d = getKBM(c.getKnowledgeBase()).createSolution(solution,
						c.getKnowledgeBase().getRootSolution());
			}
			else {
				return null;
			}

			// build relations
			createXCLRelation(c, answeredQuestions, newXCL, d);

			newXCL.append("}\n");

			// insert new XCLRelation into article
			KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(
					parameterMap.getWeb());
			KnowWEArticle a = mgr.getArticle(topic);
			String articleText = a.collectTextsFromLeaves();

			articleText = articleText.replace("</" + CoveringListSection.TAG + ">",
					newXCL + "\n</" + CoveringListSection.TAG + ">");
			KnowWEEnvironment.getInstance().saveArticle(web, topic, articleText,
					parameterMap);

		}
		return null;
	}

	/**
	 * 
	 * @param c
	 * @param answeredQuestions
	 * @param content
	 */
	private void createXCLRelation(Session c, List<? extends Question> answeredQuestions, StringBuffer newXCL,
			Solution d) {
		for (Question q : answeredQuestions) {
			Value theanswer = c.getBlackboard().getValue(q);
			if (UndefinedValue.isNotUndefinedValue(theanswer)) {
				newXCL.append("\"" + q.getName() + "\" = \"" + theanswer.getValue()
						+ "\",\n");
			}
			// for (Object o : answers) {
			// if( o instanceof Answer ){
			// Answer a = (Answer) o;
			// newXCL.append("\"" + q.getText() + "\" = \"" + a.getValue( c ) +
			// "\",\n");
			// }
			// }
		}
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	private boolean isSolutionNew(Solution d) {
		if (d == null) return true;
		return false;
	}

	/**
	 * @param web
	 * @param topic
	 * @param user
	 * @return
	 */
	private Session getSession(String web, String topic, String user) {
		D3webKnowledgeService knowledgeServiceInTopic = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		D3webKnowledgeService service = D3webModule.getAD3webKnowledgeServiceInTopic(web,
				topic);
		service.getBase();

		if (knowledgeServiceInTopic == null) return null;
		String kbid = knowledgeServiceInTopic.getId();

		Broker broker = D3webModule.getBroker(user, web);
		D3webKnowledgeServiceSession serviceSession = broker.getSession().getServiceSession(
				kbid);
		Session c = null;

		if (serviceSession instanceof D3webKnowledgeServiceSession) {
			c = (serviceSession).getSession();
		}

		if (serviceSession == null) {
			kbid = KnowWEEnvironment.WIKI_FINDINGS
					+ ".."
					+ KnowWEEnvironment.generateDefaultID(KnowWEEnvironment.WIKI_FINDINGS);
			serviceSession = broker.getSession().getServiceSession(kbid);
			if (serviceSession instanceof D3webKnowledgeServiceSession) {
				c = (serviceSession).getSession();
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
	private Solution findSolution(String web, String topic, String solution) {
		D3webKnowledgeService ks = D3webModule.getAD3webKnowledgeServiceInTopic(web,
				topic);

		Solution d = getKBM(ks.getBase()).findSolution(solution);
		return d;
	}

	/**
	 * 
	 * @param kb
	 * @return
	 */
	private KnowledgeBaseManagement getKBM(KnowledgeBase kb) {
		return KnowledgeBaseManagement.createInstance(kb);
	}

}
