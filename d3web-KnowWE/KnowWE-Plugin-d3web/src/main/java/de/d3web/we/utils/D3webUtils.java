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

package de.d3web.we.utils;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.XPSCase;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class D3webUtils {
	
	public static de.d3web.core.knowledge.terminology.Question getQuestion(KnowledgeServiceSession kss, String qid) {
		if(kss instanceof D3webKnowledgeServiceSession) {
			D3webKnowledgeServiceSession session = ((D3webKnowledgeServiceSession)kss);
			KnowledgeBase kb = session.getBaseManagement().getKnowledgeBase();
			return session.getBaseManagement().findQuestion(qid);

		}
		
		return null;
	}
	
	public static AnswerChoice getAnswer(KnowledgeServiceSession kss, String aid, String qid) {
		Question q = getQuestion(kss, qid);
		if(q != null) {
			D3webKnowledgeServiceSession session = ((D3webKnowledgeServiceSession)kss);
			KnowledgeBase kb = session.getBaseManagement().getKnowledgeBase();
			return (AnswerChoice)session.getBaseManagement().findAnswer(q, aid);
		}
		return null;
	}
	
	/**
	 * Gets the XPSCase Object.
	 * 
	 * @param sec
	 * @param user
	 */
	public static XPSCase getXPSCase(Section sec, KnowWEUserContext user) {
		
		String xpsCaseId = sec.getTitle() + ".." + KnowWEEnvironment.generateDefaultID(sec.getTitle());
		Broker broker = D3webModule.getBroker(user.getUsername(), sec.getWeb());
		KnowledgeServiceSession kss = broker.getSession().getServiceSession(xpsCaseId);
		XPSCase xpsCase = null;
		
		if (kss instanceof D3webKnowledgeServiceSession) {
			
			D3webKnowledgeServiceSession d3webKSS = (D3webKnowledgeServiceSession) kss;
			xpsCase = d3webKSS.getXpsCase();
		}
		return xpsCase;
	}
	
	/**
	 * Gets the XPSCase Object.
	 * 
	 * @param user
	 */
	public static XPSCase getXPSCase(String topic, KnowWEUserContext user, String web) {
		
		String xpsCaseId = topic + ".." + KnowWEEnvironment.generateDefaultID(topic);
		Broker broker = D3webModule.getBroker(user.getUsername(), web);
		KnowledgeServiceSession kss = broker.getSession().getServiceSession(xpsCaseId);
		XPSCase xpsCase = null;
		
		if (kss instanceof D3webKnowledgeServiceSession) {
			
			D3webKnowledgeServiceSession d3webKSS = (D3webKnowledgeServiceSession) kss;
			xpsCase = d3webKSS.getXpsCase();
		}
		return xpsCase;
	}

}
