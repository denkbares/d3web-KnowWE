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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.DefaultFact;
import de.d3web.core.session.interviewmanager.EmptyForm;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.core.broker.Broker;

public class D3webKnowledgeServiceSession implements KnowledgeServiceSession {

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

	@Override
	public void clear() {
		initCase();
	}

	@Override
	public void processInit() {
		for (Question question : base.getQuestions()) {
			Information info = new Information(id, question.getId(), null,
					null, null);
			info = broker.request(info, this);
			if (info != null) {
				inform(info);
			}
		}
	}

	@Override
	public void inform(Information info) {
		IDObject object = base.search(info.getObjectID());

		// List<Object> values = new ArrayList<Object>();
		Value value = null;
		List theValues = info.getValues();
		if (theValues.size() > 0 && theValues.get(0).equals(Unknown.getInstance().getId())) {
			value = Unknown.getInstance();
		}
		else if (object instanceof QuestionChoice) {
			value = getAnswers((QuestionChoice) object, theValues);
			// values.addAll(getAnswers((QuestionChoice) object,
			// info.getValues()));
		}
		else if (object instanceof QuestionNum) {
			value = getAnswers((QuestionNum) object, theValues);
			// values.addAll(getAnswers((QuestionNum) object,
			// info.getValues()));
		}

		TerminologyObject vo = null;
		if (object instanceof TerminologyObject) {
			vo = (TerminologyObject) object;
		}

		if (vo instanceof Solution) {
			Solution diag = (Solution) vo;
			if (value != null) {
				toChange.add(diag);
				if (info.getInformationType().equals(
						InformationType.SolutionInformation)) {
					session.getBlackboard().addValueFact(
							new DefaultFact(diag, value, new Object(),
									PSMethodHeuristic.getInstance()));
				}
				else if (info.getInformationType().equals(
						InformationType.HeuristicInferenceInformation)) {
					session.getBlackboard().addValueFact(
							new DefaultFact(diag, value, new Object(),
									PSMethodHeuristic.getInstance()));
				}
			}
		}
		else {
			if (vo != null && value != null) {
				toChange.add(vo);
				session.getBlackboard().addValueFact(
						new DefaultFact(vo, value, PSMethodUserSelected.getInstance(),
								PSMethodUserSelected.getInstance()));
			}
			else {
				Logger.getLogger(this.getClass().getName()).log(
						Level.WARNING,
						"ValuedObject is null: " + object.getClass().getName() + " :"
								+ object.toString());
			}
		}
	}

	@Override
	public void request(List<Information> infos) {
		List<Solution> requestedDiagnoses = new ArrayList<Solution>();
		List<QASet> requestedFindings = new ArrayList<QASet>();
		for (Information info : infos) {
			IDObject ido = base.search(info.getObjectID());
			if (ido instanceof QASet) {
				requestedFindings.add((QASet) ido);
			}
			else if (ido instanceof Solution) {
				requestedDiagnoses.add((Solution) ido);
			}
		}
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

	@Override
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

	@Override
	public String getNamespace() {
		return id;
	}

	public KnowledgeBaseManagement getBaseManagement() {
		return baseManagement;
	}
}
