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

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Session;
import de.d3web.we.basic.WikiEnvironment;
import de.d3web.we.basic.WikiEnvironmentManager;
import de.d3web.we.basic.SessionBroker;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;

public class SemanticAnnotationAction extends DeprecatedAbstractKnowWEAction {

	// private FindingXMLWriter questionWriter;
	private final FindingHTMLWriter questionWriter;

	public SemanticAnnotationAction() {
		questionWriter = new FindingHTMLWriter();
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
		if (topic == null) {
			topic = namespace.substring(0, namespace.indexOf(".."));
		}

		if (targetUrlPrefix == null) {
			targetUrlPrefix = "KnowWE.jsp";
		}
		if (namespace == null || termName == null) {
			return null;
		}

		namespace = java.net.URLDecoder.decode(namespace);

		WikiEnvironment dpse = WikiEnvironmentManager.getInstance()
				.getEnvironments(webname);
		SessionBroker broker = dpse.getBroker(user);

		if (id == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();

		Session session = broker.getServiceSession(namespace);
		TerminologyObject obj = session.getKnowledgeBase().search(id);
		if (obj instanceof Question) {

			if (user != null) {
				sb.append(questionWriter.getHTMLString((Question) obj,
							session,
							namespace, webname, topic, targetUrlPrefix));
			}
			else {
				sb.append(questionWriter.getHTMLString((Question) obj, null,
						namespace, webname, topic, targetUrlPrefix));
			}
		}

		return sb.toString();

	}
}