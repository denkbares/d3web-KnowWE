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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.PSMethodInit;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.info.PropertiesContainer;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.DefaultSession;
import de.d3web.core.session.IEventSource;
import de.d3web.core.session.KBOEventListener;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionEventListener;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.DefaultFact;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.indication.inference.PSMethodNextQASet;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.kernel.dialogControl.DistributedControllerFactory;
import de.d3web.kernel.dialogControl.ExternalClient;
import de.d3web.kernel.dialogControl.ExternalProxy;
import de.d3web.kernel.psMethods.delegate.PSMethodDelegate;
import de.d3web.scoring.inference.PSMethodHeuristic;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.utils.ConverterUtils;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

public class D3webKnowledgeServiceSession implements KnowledgeServiceSession {

	private class DefaultExternalClient extends ExternalClient {

		private final KnowledgeServiceSession kss;

		public DefaultExternalClient(KnowledgeServiceSession kss) {
			super();
			this.kss = kss;
		}

		@Override
		public void init() {
			// nothing to initialize...
		}

		@Override
		public void delegate(String targetNamespace, String id,
				boolean temporary, String comment) {
			Information info = new Information(kss.getNamespace(), id, null,
					getTerminologyType(id), InformationType.ExternalInformation);
			List<Information> infos = new ArrayList<Information>();
			infos.add(info);
			broker.delegate(infos, targetNamespace, temporary, false, comment,
					kss);
			/*
			 * solution: let the DDcontroller look at the action of the rule...
			 * after last one: execute!
			 */
		}

		@Override
		public void delegateInstanly(String targetNamespace, String id,
				boolean temporary, String comment) {
			Information info = new Information(kss.getNamespace(), id, null,
					getTerminologyType(id), InformationType.ExternalInformation);
			List<Information> infos = new ArrayList<Information>();
			infos.add(info);
			broker.delegate(infos, targetNamespace, temporary, true, comment,
					kss);
		}

		@Override
		public void executeDelegation() {
			// normally: already done

			/*
			 * solution: let the DDcontroller look at the action of the rule...
			 * after last one: execute!
			 */
		}

	}

	private class XCLModelValueListener implements KBOEventListener {

		private final D3webKnowledgeServiceSession kss;
		private final Broker broker;
		/**
		 * Map for interpolation of the scores. Keys are precentages, values are
		 * assigned scores.
		 */
		private final Map<Double, Integer> percentagesToScores = new LinkedHashMap<Double, Integer>();

		public XCLModelValueListener(D3webKnowledgeServiceSession kss,
				Broker broker) {
			super();
			this.kss = kss;
			this.broker = broker;
			percentagesToScores.put(0.00, 0);
			// percentagesToScores.put(0.10, 2);
			// percentagesToScores.put(0.20, 5);
			// percentagesToScores.put(0.40, 10);
			// percentagesToScores.put(0.60, 20);
			// percentagesToScores.put(0.80, 40);
			// percentagesToScores.put(0.95, 80);
			percentagesToScores.put(1.00, 100);
		}

		public void notify(IEventSource source, Session theCase) {
			if (theCase != session) return;

			XCLModel model = (XCLModel) source;
			List<Object> values = new ArrayList<Object>();
			List<Object> xclInferenceValues = new ArrayList<Object>();
			Rating xclstate = model.getState(theCase);
			Double precision = model.getInferenceTrace(theCase).getScore();
			Double support = model.getInferenceTrace(theCase).getSupport();
			xclInferenceValues.add(precision);
			xclInferenceValues.add(support);
			values = new ArrayList<Object>();
			values.add(getLocalSolutionState(xclstate));

			broker.update(new Information(kss.getNamespace(), model
					.getSolution().getId(), values, TerminologyType.diagnosis,
					InformationType.SolutionInformation));
			if (xclInferenceValues.size() > 0) {
				broker.update(new Information(kss.getNamespace(), model
						.getSolution().getId(), xclInferenceValues,
						TerminologyType.diagnosis,
						InformationType.XCLInferenceInformation));
			}

		}

	}

	private final KnowledgeBase base;
	private final KnowledgeBaseManagement baseManagement;
	private final String id;
	private final Broker broker;
	private Session session;

	private boolean instantly = true;
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
		initConnection();
	}

	private void initCase() {
		//DistributedControllerFactory factory = getControllerFactory();
		session = SessionFactory.createSession(base);
		((DefaultSession) session).addUsedPSMethod(PSMethodDelegate.getInstance());

	}

	private DistributedControllerFactory getControllerFactory() {
		ExternalClient external = new DefaultExternalClient(this);
		ExternalProxy proxy = new ExternalProxy();
		proxy.addClient(external);
		DistributedControllerFactory factory = new DistributedControllerFactory(
				proxy);
		return factory;
	}

	private void initConnection() {
		session.addListener(new SessionEventListener() {

			public void notify(Session source, TerminologyObject o, Object context) {
				maybeNotifyBroker(o, source, context);
			}
		});

		// also listen to XCL knowledge
		for (KnowledgeSlice slice : base
				.getAllKnowledgeSlicesFor(PSMethodXCL.class)) {
			if (slice instanceof XCLModel) {
				((XCLModel) slice).addListener(new XCLModelValueListener(this,
						broker));
			}
		}
	}

	public void clear() {
		initCase();
		initConnection();
	}

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

	public void inform(Information info) {
		IDObject object = base.search(info.getObjectID());

		// List<Object> values = new ArrayList<Object>();
		Value value = null;
		if (object instanceof QuestionChoice) {
			value = getAnswers((QuestionChoice) object, info.getValues());
			// values.addAll(getAnswers((QuestionChoice) object,
			// info.getValues()));
		}
		else if (object instanceof QuestionNum) {
			value = getAnswers((QuestionNum) object, info.getValues());
			// values.addAll(getAnswers((QuestionNum) object,
			// info.getValues()));
		}
		else if (object instanceof Solution) {
			PropertiesContainer pc = (PropertiesContainer) object;
			Boolean external = (Boolean) pc.getProperties().getProperty(
					Property.EXTERNAL);
			if (info.getInformationType().equals(
					InformationType.SolutionInformation)) {
				if (external != null && external) {
					value = getStatesForDiagnosis(info.getValues());
				}
			}
			else {
				// see ClusterSolutionManager
				// values.addAll(getValuesForDiagnosis(info.getValues()));
			}
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
			if (vo != null) {
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

		// [TODO]: no idea if this is correct:
		if (requestedDiagnoses.isEmpty()) {
			for (QASet set : requestedFindings) {
				session.getQASetManager().propagate(set, null,
						PSMethodNextQASet.getInstance());
			}
		}
		else {
			for (QASet set : base.getInitQuestions()) {
				session.getQASetManager().propagate(set, null,
						PSMethodInit.getInstance());
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

	private Value getStatesForDiagnosis(List<?> values) {
		if (values.size() == 1) {
			SolutionState state = (SolutionState) values.get(0);
			if (state.equals(SolutionState.ESTABLISHED)) {
				return new Rating(State.ESTABLISHED);
			}
			else if (state.equals(SolutionState.SUGGESTED)) {
				return new Rating(State.SUGGESTED);
			}
			else if (state.equals(SolutionState.EXCLUDED)) {
				return new Rating(State.EXCLUDED);
			}
			else if (state.equals(SolutionState.UNCLEAR)) {
				return new Rating(State.UNCLEAR);
			}
		}
		return UndefinedValue.getInstance();
	}

	protected void maybeNotifyBroker(TerminologyObject valuedObject,
			Session session, Object context) {
		// do not inform anyone about some things that were told you by the
		// broker...
		if (toChange.contains(valuedObject)) {
			toChange.remove(valuedObject);
			return;
		}
		InformationType infoType = getInfoType(context);

		if (valuedObject instanceof Question) {
			Question question = (Question) valuedObject;
			if (instantly) {
				broker.update(new Information(id, question.getId(),
						ConverterUtils.toValueList(session.getBlackboard().getValue(question),
						session), TerminologyType.symptom, infoType));
			}
			else {
				// mag ich grad net
			}
		}
		else if (valuedObject instanceof Solution) {
			Solution diagnosis = (Solution) valuedObject;
			if (instantly) {
				List<SolutionState> solutionList = new ArrayList<SolutionState>();
				solutionList.add(getLocalSolutionState(session.getBlackboard().getRating(
						diagnosis)));
				List<Rating> inferenceList = new ArrayList<Rating>();
				inferenceList.add(session.getBlackboard().getRating(diagnosis));
				broker.update(new Information(id, diagnosis.getId(),
						solutionList, TerminologyType.diagnosis,
						InformationType.SolutionInformation));
				broker.update(new Information(id, diagnosis.getId(),
						inferenceList, TerminologyType.diagnosis,
						InformationType.HeuristicInferenceInformation));
			}
			else {
				// mag ich grad net
			}
		}

		if (isFinished()) {
			broker.finished(this);
		}
	}

	public boolean isFinished() {
		return !session.getQASetManager().hasNextQASet();
	}

	private SolutionState getLocalSolutionState(Rating state) {
		if (state.equals(new Rating(Rating.State.ESTABLISHED))) {
			return SolutionState.ESTABLISHED;
		}
		else if (state.equals(new Rating(Rating.State.SUGGESTED))) {
			return SolutionState.SUGGESTED;
		}
		else if (state.equals(new Rating(Rating.State.EXCLUDED))) {
			return SolutionState.EXCLUDED;
		}
		else if (state.equals(new Rating(Rating.State.UNCLEAR))) {
			return SolutionState.UNCLEAR;
		}
		return SolutionState.CONFLICT;
	}

	public TerminologyType getTerminologyType(String id) {
		IDObject ido = base.search(id);
		if (ido instanceof Solution) {
			return TerminologyType.diagnosis;
		}
		else if (ido instanceof QASet) {
			return TerminologyType.symptom;
		}
		return null;
	}

	private InformationType getInfoType(Object context) {
		InformationType result = null;
		if (context.equals(Object.class)) {
			// Dialog did it!
			result = InformationType.OriginalUserInformation;
		}
		else {
			// Kernel did it!
			result = InformationType.HeuristicInferenceInformation;
		}
		return result;
	}

	public boolean isInstantly() {
		return instantly;
	}

	public void setInstantly(boolean instantly) {
		this.instantly = instantly;
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

	public String getId() {
		return id;
	}

}
