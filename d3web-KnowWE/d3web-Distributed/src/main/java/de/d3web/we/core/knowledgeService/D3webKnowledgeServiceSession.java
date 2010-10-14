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

package de.d3web.we.core.knowledgeService;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.interviewmanager.EmptyForm;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.we.core.broker.Broker;

public class D3webKnowledgeServiceSession {

	private final KnowledgeBase base;
	private final KnowledgeBaseManagement baseManagement;
	private final String id;
	private final Broker broker;
	private Session session;

	private final List<TerminologyObject> toChange;

	public D3webKnowledgeServiceSession(KnowledgeBase base, Broker broker,
			String id) {
		super();
		this.base = base;
		this.broker = broker;
		this.id = id;
		baseManagement = KnowledgeBaseManagement.createInstance(base);
		toChange = new ArrayList<TerminologyObject>();
		initCase();
	}

	private void initCase() {
		// DistributedControllerFactory factory = getControllerFactory();
		session = SessionFactory.createSession(base);
	}

	public void clear() {
		initCase();
	}

	private Value getAnswers(QuestionNum qn, List values) {
		for (Object answerValue : values) {
			if (answerValue instanceof Double) {
				return new NumValue((Double) answerValue);
			}
		}
		return UndefinedValue.getInstance();
	}

	private Value getAnswers(QuestionChoice question, List values) {
		if (question instanceof QuestionMC) {
			return baseManagement.findMultipleChoiceValue((QuestionMC) question,
					(List<String>) values);
		}
		// for one-chocie questions
		else {
			for (Object object : values) {
				if (object instanceof String) {
					String id = (String) object;
					Value answer = baseManagement.findValue(question, id);
					if (answer != null && answer != UndefinedValue.getInstance()) {
						return answer;
					}
				}
			}
		}
		return UndefinedValue.getInstance();
	}

	public boolean isFinished() {
		return session.getInterview().nextForm().equals(EmptyForm.getInstance());
	}

	public Session getSession() {
		return session;
	}

	@Override
	public String toString() {
		return base.getSolutions().toString();
	}

	public String getNamespace() {
		return id;
	}

	public KnowledgeBaseManagement getBaseManagement() {
		return baseManagement;
	}
}
