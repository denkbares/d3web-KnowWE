package de.d3web.we.core.knowledgeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.dialogControl.DistributedControllerFactory;
import de.d3web.kernel.dialogControl.ExternalClient;
import de.d3web.kernel.dialogControl.ExternalProxy;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.CaseFactory;
import de.d3web.kernel.domainModel.D3WebCase;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisScore;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.IEventSource;
import de.d3web.kernel.domainModel.KBOEventListener;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.ValuedObject;
import de.d3web.kernel.domainModel.XPSCaseEventListener;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.PSMethodInit;
import de.d3web.kernel.psMethods.compareCase.CompareCaseException;
import de.d3web.kernel.psMethods.compareCase.comparators.CompareMode;
import de.d3web.kernel.psMethods.compareCase.facade.ComparisonResultRepository;
import de.d3web.kernel.psMethods.compareCase.facade.SimpleResult;
import de.d3web.kernel.psMethods.delegate.PSMethodDelegate;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.kernel.psMethods.nextQASet.PSMethodNextQASet;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.supportknowledge.PropertiesContainer;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.blackboard.InferenceConverterUtils;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.utils.ConverterUtils;

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
		public void delegate(String targetNamespace, String id, boolean temporary, String comment) {
			Information info = new Information(kss.getNamespace(), id, null, getTerminologyType(id), InformationType.ExternalInformation);
			List<Information> infos = new ArrayList<Information>();
			infos.add(info);
			broker.delegate(infos, targetNamespace, temporary, false, comment, kss);
			/*
			 * solution: let the DDcontroller look at the action of the rule... after last one: execute!
			 */
		}

		@Override
		public void delegateInstanly(String targetNamespace, String id, boolean temporary, String comment) {
			Information info = new Information(kss.getNamespace(), id, null, getTerminologyType(id), InformationType.ExternalInformation);
			List<Information> infos = new ArrayList<Information>();
			infos.add(info);
			broker.delegate(infos, targetNamespace, temporary, true, comment, kss);
		}
		
		@Override
		public void executeDelegation() {
			//normally: already done
			
			/*
			 * solution: let the DDcontroller look at the action of the rule... after last one: execute!
			 */
		}

		

		
	}
	
	private class XCLModelValueListener implements KBOEventListener {
		
		private final D3webKnowledgeServiceSession kss;
		private final Broker broker;
		/**
		 * Map for interpolation of the scores. Keys are precentages,
		 * values are assigned scores.
		 */ 
		private Map<Double, Integer> percentagesToScores = new LinkedHashMap();
		
		
		
		public XCLModelValueListener(D3webKnowledgeServiceSession kss, Broker broker) {
			super();
			this.kss = kss;
			this.broker = broker;
			percentagesToScores.put(0.00, 0);
//			percentagesToScores.put(0.10, 2);
//			percentagesToScores.put(0.20, 5);
//			percentagesToScores.put(0.40, 10);
//			percentagesToScores.put(0.60, 20);
//			percentagesToScores.put(0.80, 40);
//			percentagesToScores.put(0.95, 80);
			percentagesToScores.put(1.00, 100);
		}
		
		


		public void notify(IEventSource source, XPSCase theCase) {
			if(theCase != xpsCase) return;
		
			XCLModel model = (XCLModel) source;
			List<Object> values = new ArrayList<Object>();
			List<Object> xclInferenceValues = new ArrayList<Object>();
			DiagnosisState xclstate = model.getState(theCase);
			Double precision = model.computeXCLScore(theCase);
			Double support = model.computeSupport(theCase);
			xclInferenceValues.add(precision);
			xclInferenceValues.add(support);
			values = new ArrayList<Object>();
			values.add(getLocalSolutionState(xclstate));
			
			
			broker.update(new Information(kss.getNamespace(), model.getSolution().getId(), values, TerminologyType.diagnosis, InformationType.SolutionInformation));
			if(xclInferenceValues.size() > 0) {
				broker.update(new Information(kss.getNamespace(), model.getSolution().getId(), xclInferenceValues, TerminologyType.diagnosis, InformationType.XCLInferenceInformation));
			}
			
		}
		
		/**
		 * Returns the score assigned to the given percentage. The calculation
		 * is based on the interpolation map. If the given percentage is not defined
		 * in the map, an interpolated score will be calculated by considering
		 * the two adjacent defined percentages (linear interpolation).
		 */
		public int getInterpolatedScore(double percentage) {
			double[] adjacentPs = getAdjacentPercentages(percentage);

			if (adjacentPs[0] == adjacentPs[1]) {
				return percentagesToScores.get(adjacentPs[0]);
			}
			
			int lowerValue = percentagesToScores.get(adjacentPs[0]);
			int upperValue = percentagesToScores.get(adjacentPs[1]);
			
			double interpolated = lowerValue 
				+ ((upperValue - lowerValue) 
						* (percentage - adjacentPs[0]) 
						/ (adjacentPs[1] - adjacentPs[0]));
			
			return (int) Math.round(interpolated);
		}
		
		/**
		 * Returns two doubles: the first one is the percentage defined in the
		 * interpolation map, which is less or equal to the given percentage;
		 * the second one is the percentage defined in the interpolation map, 
		 * which is greater or equal to the given percentage.
		 */
		private double[] getAdjacentPercentages(double percentage) {
			double[] ret = new double[2];
			for (double p : percentagesToScores.keySet()) {
				if (p <= percentage) {
					ret[0] = p;
				}
				if (p >= percentage) {
					ret[1] = p;
					break;
				}
			}
			return ret;
		}

	}
	

	private final KnowledgeBase base;
	private final KnowledgeBaseManagement baseManagement;
	private final String id;
	private final Broker broker;
	private XPSCase xpsCase;
	private ComparisonResultRepository crr;
	
	private boolean instantly = true;
	private List<ValuedObject> toChange;
	
	public D3webKnowledgeServiceSession(KnowledgeBase base, Broker broker, String id) {
		super();
		this.base = base;
		this.broker = broker;
		this.id = id;
		baseManagement = KnowledgeBaseManagement.createInstance(base);
		toChange = new ArrayList<ValuedObject>();
		initCase();
		initConnection();
	}

	

	private void initCase() {
		DistributedControllerFactory factory = getControllerFactory();
		xpsCase = CaseFactory.createXPSCase(base, factory);
		((D3WebCase)xpsCase).addUsedPSMethod(PSMethodXCL.getInstance());
		((D3WebCase)xpsCase).addUsedPSMethod(PSMethodDelegate.getInstance());

		
		Collection caseRepository = base.getCaseRepository("train");
		if(caseRepository != null && !caseRepository.isEmpty()) {
			initCBR();
		}
	}

	private void initCBR() {
		Collection caseRepository = base.getCaseRepository("train");
		crr = new ComparisonResultRepository();
		crr.setCurrentCase(xpsCase);
		crr.setCompareMode(CompareMode.BOTH_FILL_UNKNOWN);
	}



	private DistributedControllerFactory getControllerFactory() {
		ExternalClient external = new DefaultExternalClient(this);
		ExternalProxy proxy = new ExternalProxy();
		proxy.addClient(external);
		DistributedControllerFactory factory = new DistributedControllerFactory(proxy);
		return factory;
	}
	
	private void initConnection() {
		xpsCase.addListener(new XPSCaseEventListener() {
			public void notify(XPSCase source, ValuedObject o, Object context) {
				maybeNotifyBroker(o, source, context);
			}
		});	

		
		//also listen to XCL knowledge
		for(KnowledgeSlice slice : base.getAllKnowledgeSlicesFor(PSMethodXCL.class)) {
			if(slice instanceof XCLModel) {
				((XCLModel)slice).addListener(new XCLModelValueListener(this, broker));
			}
		}
	}
	
	public void clear() {
		initCase();
		initConnection();
	}
	
	
	public void processInit() {
		for (Question question : base.getQuestions()) {
			Information info = new Information(id, question.getId(), null, null, null);
			info = broker.request(info, this);
			if(info != null) {
				inform(info);
			}
		}
	}
	
	public void inform(Information info) {
		IDObject object = base.search(info.getObjectID());
	
		List<Object> values = new ArrayList<Object>();
		if(object instanceof QuestionChoice) {
			values.addAll(getAnswers((QuestionChoice)object, info.getValues()));
		} else if(object instanceof QuestionNum) {
			values.addAll(getAnswers((QuestionNum) object, info.getValues()));
		} else if(object instanceof Diagnosis) {
			PropertiesContainer pc = (PropertiesContainer) object;
			Boolean external = (Boolean) pc.getProperties().getProperty(Property.EXTERNAL);
			if(info.getInformationType().equals(InformationType.SolutionInformation)) {
				if(external != null && external) {
					values.addAll(getStatesForDiagnosis(info.getValues()));
				}
			} else {
				// see ClusterSolutionManager
				//values.addAll(getValuesForDiagnosis(info.getValues()));
			}
		}
		
		ValuedObject vo = null;
		if(object instanceof ValuedObject) {
			vo = (ValuedObject) object;
		}
		if(object instanceof QuestionOC) { //[HOTFIX]:!!!!!!!!!!!!!
			if(values.size() > 1) {
				Object value = values.get(0);
				values = new ArrayList<Object>();
				values.add(value);
			}
		}
		
		if(vo instanceof Diagnosis) {
			Diagnosis diag = (Diagnosis) vo;
			if(!(values.isEmpty())) {
				toChange.add(diag);
				if(info.getInformationType().equals(InformationType.SolutionInformation)) {
					xpsCase.setValue(diag, values.toArray(), PSMethodHeuristic.class);
				} else if(info.getInformationType().equals(InformationType.HeuristicInferenceInformation)) {
					DiagnosisScore oldScore = diag.getScore(xpsCase, PSMethodHeuristic.class);
					DiagnosisScore newScore = oldScore.add((DiagnosisScore) values.get(0));
					List newValues = new ArrayList();
					newValues.add(newScore);
					xpsCase.setValue(diag, values.toArray(), PSMethodHeuristic.class);
				}
			}
		} else {
			toChange.add(vo);
			xpsCase.setValue(vo, values.toArray());
		}
	}



	public void request(List<Information> infos) {
		List<Diagnosis> requestedDiagnoses = new ArrayList<Diagnosis>();
		List<QASet> requestedFindings = new ArrayList<QASet>();
		for (Information info : infos) {
			IDObject ido = base.search(info.getObjectID());
			if(ido instanceof QASet) {
				requestedFindings.add((QASet) ido);
			} else if(ido instanceof Diagnosis) {
				requestedDiagnoses.add((Diagnosis) ido);
			}
		}
	
		//[TODO]: no idea if this is correct:
		if(requestedDiagnoses.isEmpty()) {
			for (QASet set : requestedFindings) {
				xpsCase.getQASetManager().propagate(set, null, PSMethodNextQASet.getInstance());
			}
		} else {
			for (QASet set : base.getInitQuestions()) {
				xpsCase.getQASetManager().propagate(set, null, PSMethodInit.getInstance());
			}
		}
	
	}

	
	private Collection<? extends Answer> getAnswers(QuestionNum qn, List values) {
		Collection<Answer> result = new ArrayList<Answer>();
		for (Object answerValue : values) {
			if(answerValue instanceof Double) {
				AnswerNum answer = new AnswerNum();
				answer.setQuestion(qn);
				answer.setValue((Double) answerValue);
				result.add(answer);
			}
		}
		return result;
	}

	private Collection<Answer> getAnswers(QuestionChoice choice, List values) {
		Collection<Answer> result = new ArrayList<Answer>();
		//[HOTFIX]:Peter: known, but another answer
//		if(values.isEmpty()) {
//			result.add(choice.getUnknownAlternative());
//		}
		for (Object object : values) {
			if(object instanceof String) {
				String id = (String) object;
				Answer answer = baseManagement.findAnswer(choice, id);
				if(answer != null) {
					result.add(answer);
				}
			}
		}
		return result;
	}

	private Collection<DiagnosisScore> getValuesForDiagnosis(List values) {
		Collection<DiagnosisScore> result = new ArrayList<DiagnosisScore>();
		for (Object obj : values) {
			if(obj instanceof Double) {
				DiagnosisScore score = new DiagnosisScore();
				score.setScore((Double) obj);
				result.add(score);
			}
		}
		return result;
	}

	private Collection<Object> getStatesForDiagnosis(List values) {
		Collection<Object> result = new ArrayList<Object>();
		if(values.size() == 1) {
			SolutionState state = (SolutionState) values.get(0);
			if(state.equals(SolutionState.ESTABLISHED)) {
				DiagnosisScore score = new DiagnosisScore();
				score.setScore(80);
				result.add(score);
			} else if(state.equals(SolutionState.SUGGESTED)) {
				DiagnosisScore score = new DiagnosisScore();
				score.setScore(20);
				result.add(score);
			} else if(state.equals(SolutionState.EXCLUDED)) {
				DiagnosisScore score = new DiagnosisScore();
				score.setScore(-80);
				result.add(score);
			} else if(state.equals(SolutionState.UNCLEAR)) {
				DiagnosisScore score = new DiagnosisScore();
				score.setScore(0);
				result.add(score);
			}
		}
		return result;
	}


	
	protected void maybeNotifyBroker(ValuedObject valuedObject, XPSCase xpsCase, Object context) {
		// do not inform anyone about some things that were told you by the broker...
		if(toChange.contains(valuedObject)) {
			toChange.remove(valuedObject);
			return;
		}
		InformationType infoType = getInfoType(context);
		
		if(valuedObject instanceof Question) {
			Question question = (Question) valuedObject;
			if(instantly) {
				broker.update(new Information(id, question.getId(), ConverterUtils.toValueList(question.getValue(xpsCase), xpsCase), TerminologyType.symptom, infoType));
			} else {
//				 mag ich grad net
			}
		} else if(valuedObject instanceof Diagnosis) {
			Diagnosis diagnosis = (Diagnosis) valuedObject;
			if(instantly) {
				List solutionList = new ArrayList();
				solutionList.add(getLocalSolutionState(diagnosis.getState(xpsCase, PSMethodHeuristic.class)));
				List inferenceList = new ArrayList();
				inferenceList.add(diagnosis.getScore(xpsCase, PSMethodHeuristic.class).getScore());
				broker.update(new Information(id, diagnosis.getId(), solutionList, TerminologyType.diagnosis, InformationType.SolutionInformation));
				broker.update(new Information(id, diagnosis.getId(), inferenceList, TerminologyType.diagnosis, InformationType.HeuristicInferenceInformation));
			} else {
				// mag ich grad net
			}
		} 
		if(crr != null) {
			crr.setCurrentCase(xpsCase);
			try {
				List simpleResults = crr.getSimpleResults(base.getCaseRepository("train"));
				for (Object eachObj1 : simpleResults) {
					SimpleResult sr = (SimpleResult) eachObj1;
					if(sr.getSimilarity() > 0.9) {
						for (Object eachObj2 : sr.getDiagnoses()) {
							Diagnosis diagnosis = (Diagnosis) eachObj2;
							List solutionList = new ArrayList();
							solutionList.add(SolutionState.ESTABLISHED);
							List inferenceList = new ArrayList();
							inferenceList.add(sr.getSimilarity());
							broker.update(new Information(id, diagnosis.getId(), solutionList, TerminologyType.diagnosis, InformationType.SolutionInformation));
							broker.update(new Information(id, diagnosis.getId(), inferenceList, TerminologyType.diagnosis, InformationType.CaseBasedInferenceInformation));
						}
					} else if(sr.getSimilarity() > 0.5) {
						for (Object eachObj2 : sr.getDiagnoses()) {
							Diagnosis diagnosis = (Diagnosis) eachObj2;
							List solutionList = new ArrayList();
							solutionList.add(SolutionState.SUGGESTED);
							List inferenceList = new ArrayList();
							inferenceList.add(sr.getSimilarity());
							broker.update(new Information(id, diagnosis.getId(), solutionList, TerminologyType.diagnosis, InformationType.SolutionInformation));
							broker.update(new Information(id, diagnosis.getId(), inferenceList, TerminologyType.diagnosis, InformationType.CaseBasedInferenceInformation));
						}
					} else if(sr.getSimilarity() < 0.1) {
						for (Object eachObj2 : sr.getDiagnoses()) {
							Diagnosis diagnosis = (Diagnosis) eachObj2;
							List solutionList = new ArrayList();
							solutionList.add(SolutionState.EXCLUDED);
							List inferenceList = new ArrayList();
							inferenceList.add(sr.getSimilarity());
							broker.update(new Information(id, diagnosis.getId(), solutionList, TerminologyType.diagnosis, InformationType.SolutionInformation));
							broker.update(new Information(id, diagnosis.getId(), inferenceList, TerminologyType.diagnosis, InformationType.CaseBasedInferenceInformation));
						}
					}
				}
			} catch (CompareCaseException e) {
				Logger.getLogger(getClass().getName()).warning(e.getMessage());
			}
		}
		
		if(isFinished()) {
			broker.finished(this);
		}
	}

	public boolean isFinished() {
		return xpsCase.isFinished() || !xpsCase.getQASetManager().hasNextQASet();
	}
	
	private SolutionState getLocalSolutionState(DiagnosisState state) {
		if(state.equals(DiagnosisState.ESTABLISHED)) {
			return SolutionState.ESTABLISHED;
		} else if(state.equals(DiagnosisState.SUGGESTED)) {
			return SolutionState.SUGGESTED;
		} else if(state.equals(DiagnosisState.EXCLUDED)) {
			return SolutionState.EXCLUDED;
		} else if(state.equals(DiagnosisState.UNCLEAR)) {
			return SolutionState.UNCLEAR;
		}
		return SolutionState.CONFLICT;
	}

	public TerminologyType getTerminologyType(String id) {
		IDObject ido = base.search(id);
		if(ido instanceof Diagnosis) {
			return TerminologyType.diagnosis;
		} else if(ido instanceof QASet) {
			return TerminologyType.symptom;
		}
		return null;
	}
	
	private InformationType getInfoType(Object context) {
		InformationType result = null;
		if(context.equals(Object.class)) {
			// Dialog did it!
			result = InformationType.OriginalUserInformation;
		} else {
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

	public XPSCase getXpsCase() {
		return xpsCase;
	}
	
	public String toString() {
		return base.getDiagnoses().toString();
	}

	public String getNamespace() {
		return id;
	}



	public KnowledgeBaseManagement getBaseManagement() {
		return baseManagement;
	}



	
	
}
