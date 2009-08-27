package de.d3web.we.core.blackboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.terminology.term.Term;

public class GlobalSolutionManager {

	private Map<Term, SolutionState> solutionsMap;
	private ISetMap<Term, Information> assumptionMap; 
	private GlobalSolutionStrategy strategy;
	private DPSEnvironment environment;
	
	
	public GlobalSolutionManager(DPSEnvironment environment, GlobalSolutionStrategy strategy) {
		super();
		this.environment = environment;
		this.strategy = strategy;
		solutionsMap = new HashMap<Term, SolutionState>();
		assumptionMap = new SetMap<Term, Information>();
	}
	

	public Map<Term, SolutionState> getGlobalSolutions() {
		return solutionsMap;
	}

	public ISetMap<Term, Information> getAssumptions() {
		return assumptionMap;
	}
	
	public void update(Information info) {
		if(info.getValues() == null || info.getValues().isEmpty()) return;
		List<Term> solutionTerms = new ArrayList<Term>();
		if(InformationType.SolutionInformation.equals(info.getInformationType())) {
			solutionTerms = environment.getTerminologyServer().getBroker().getAlignedTerms(info.getIdentifiableObjectInstance());
		} else if(InformationType.ClusterInformation.equals(info.getInformationType())) {
			Term term = environment.getTerminologyServer().getGlobalTerminology(info.getTerminologyType()).getTerm(info.getObjectID(), null);
			solutionTerms.add(term);
		}
		for (Term eachTerm : solutionTerms) {
			if(eachTerm == null) continue;
			updateTerm(eachTerm, info);
		}
	}
	
	private void updateTerm(Term term, Information info) {
		Object obj = info.getValues().get(0);
		if(obj == null || !(obj instanceof SolutionState)) return;
		updateAssuptions(term, info);
		SolutionState state = strategy.calculateState(assumptionMap.get(term));
		solutionsMap.put(term, state);
	}


	private void updateAssuptions(Term term, Information info) {
		Collection<Information> assuptions = assumptionMap.get(term);
		if(assuptions != null) {
			for (Information each : new ArrayList<Information>(assuptions)) {
				if(each.equalsNamespaces(info)) {
					assuptions.remove(each);
				}
			}
		}
		assumptionMap.add(term, info);
	}


	public void clear() {
		solutionsMap.clear();
		assumptionMap.clear();
	}
	
	public void removeInformation(String namespace) {
		for (Term each : assumptionMap.keySet()) {
			BlackboardImpl.removeInfo(assumptionMap.get(each), namespace);
		}
	}
	
}
