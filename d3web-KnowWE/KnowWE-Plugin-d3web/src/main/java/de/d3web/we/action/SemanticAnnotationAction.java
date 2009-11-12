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

import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.term.Term;

public class SemanticAnnotationAction extends AbstractKnowWEAction {

	// private FindingXMLWriter questionWriter;
	private FindingHTMLWriter questionWriter;

	public SemanticAnnotationAction() {
		questionWriter = new FindingHTMLWriter();
	}

	public static IdentifiableInstance getII(DPSEnvironment dpse, String namespace,
			Term term) {
		IdentifiableInstance ii = null;
		List<IdentifiableInstance> iis = dpse.getTerminologyServer()
				.getBroker().getAlignedIdentifiableInstances(term);
		if (iis != null && !iis.isEmpty()) {
			for (IdentifiableInstance instance : iis) {
				if (instance.getNamespace().equals(namespace)) {
					ii = instance;
				}
			}
		}
		return ii;
	}

	public static Term getTerm(DPSEnvironment dpse, String termName) {
		Term result = null;
		for (GlobalTerminology each : dpse.getTerminologyServer().getBroker()
				.getGlobalTerminologies()) {
			result = each.getTerm(termName, null);
			if (result != null) {
				return result;
			}
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String namespace = parameterMap.get(KnowWEAttributes.SEMANO_NAMESPACE);
		String termName = parameterMap.get(KnowWEAttributes.SEMANO_TERM_NAME);
		String type = parameterMap.get(KnowWEAttributes.SEMANO_TERM_TYPE);
		String user = parameterMap.get(KnowWEAttributes.USER);
		String webname = parameterMap.get(KnowWEAttributes.WEB);
		String id = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String targetUrlPrefix = parameterMap.get("sendToUrl");
		String topic = parameterMap.getTopic();
		if(topic == null) {
			topic = namespace.substring(0, namespace.indexOf(".."));
		}
		
		D3webKnowledgeService service = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(webname, topic);		

		if (targetUrlPrefix == null) {
			targetUrlPrefix = "KnowWE.jsp";
		}
		if (namespace == null || termName == null) {
			return null;
		}

		namespace = java.net.URLDecoder.decode(namespace);

		DPSEnvironment dpse = DPSEnvironmentManager.getInstance()
				.getEnvironments(webname);
		Broker broker = dpse.getBroker(user);

		if(id == null) {
			id = getIDForName(termName,dpse,namespace).getObjectId();
			if(id == null) {
				return null;
			}
		}
		StringBuffer sb = new StringBuffer();

		LocalTerminologyAccess access = dpse.getTerminologyServer()
				.getStorage().getTerminology(TerminologyType.getType(type),
						namespace);
	
		if(access == null) {return "no access to terminlogy server";}
		Object obj = access.getObject(id, null);
		if (obj instanceof Question) {

			if (user != null) {
				KnowledgeServiceSession kss = broker.getSession()
						.getServiceSession(namespace);
				if (kss instanceof D3webKnowledgeServiceSession) {
					sb.append(questionWriter.getHTMLString((Question) obj,
							((D3webKnowledgeServiceSession) kss).getXpsCase(),
							namespace, webname, topic, targetUrlPrefix));
				}
			} else {
				sb.append(questionWriter.getHTMLString((Question) obj, null,
						namespace, webname, topic, targetUrlPrefix));
			}
		}

		return sb.toString();

	}

	public static IdentifiableInstance getIDForName(String termName, DPSEnvironment dpse, String namespace) {
		Term term = null;
		
		term = getTerm(dpse, termName);

		IdentifiableInstance ii = null;
		if (term != null) {
			ii = getII(dpse, namespace, term);
		}
		if (ii == null) {
			return null;
			//return "Question not found in KB: " + termName;
		}

		return ii;
		
		
	}

}
