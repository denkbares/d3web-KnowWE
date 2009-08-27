package de.d3web.we.core.blackboard;

import java.util.Collection;
import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;

public class HeuristicClusterStrategy implements ClusterSolutionStrategy {

	public SolutionState calculateState(Collection<Information> infos) {
		double score = calculateInference(infos);
		return InferenceConverterUtils.getStateByScore(score);
	}

	public double calculateInference(Collection<Information> infos) {
		double score = 0;
		for (Information information : infos) {
			List values = information.getValues();
			if(information.getInformationType().equals(InformationType.HeuristicInferenceInformation) 
					&& values != null && values.size() == 1) {
				score += (Double) values.get(0);
			}
		}
		return score;
	}
	
}
