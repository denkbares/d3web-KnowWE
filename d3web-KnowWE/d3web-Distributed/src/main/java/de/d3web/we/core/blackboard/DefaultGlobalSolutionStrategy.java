package de.d3web.we.core.blackboard;


import java.util.Collection;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.SolutionState;

public class DefaultGlobalSolutionStrategy implements GlobalSolutionStrategy {

	public SolutionState calculateState(Collection<Information> infos) {
		SolutionState result = null;
		for (Information each : infos) {
			result = calculateState(result, each);
		}
		return result;
	}
	/*
	private SolutionState calculateState(SolutionState first, Information info) {
		SolutionState second = (SolutionState) info.getValues().get(0);
		if(first == null) {
			return second;
		}
		if(first.equals(SolutionState.CONFLICT)) {
			return SolutionState.CONFLICT;
		}
		if(first.equals(SolutionState.ESTABLISHED)) {
			if(second.equals(SolutionState.EXCLUDED) || second.equals(SolutionState.CONFLICT)) {
				return SolutionState.CONFLICT;
			} 
			return SolutionState.ESTABLISHED;
		} else if(first.equals(SolutionState.SUGGESTED)) {
			if(second.equals(SolutionState.EXCLUDED)) {
				return SolutionState.EXCLUDED;
			} else if(second.equals(SolutionState.ESTABLISHED)) {
				return SolutionState.ESTABLISHED;
			} else if(second.equals(SolutionState.CONFLICT)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.SUGGESTED;
			
		} else if(first.equals(SolutionState.EXCLUDED)) {
			if(second.equals(SolutionState.ESTABLISHED)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.EXCLUDED;
		} 
		return second;
	}*/
	
	private SolutionState calculateState(SolutionState first, Information info) {
		SolutionState second = (SolutionState) info.getValues().get(0);
		if(first == null) {
			return second;
		}
		int comp = first.compareTo(second);
		if(comp < 0) {
			return first;
		} else if (comp > 0) {
			return second;
		} else {
			return second;
		}
	}
}
