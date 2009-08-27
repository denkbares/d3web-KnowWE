package de.d3web.we.core.blackboard;

import java.util.Collection;
import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;

public class SetCoveringClusterStrategy implements ClusterSolutionStrategy {

	public SolutionState calculateState(Collection<Information> infos) {
		double score = calculateInference(infos);
		return InferenceConverterUtils.getStateByCovering(score);
	}

	public double calculateInference(Collection<Information> infos) {
		double covered = 0;
		double observed = 0;
		for (Information information : infos) {
			List values = information.getValues();
			if(information.getInformationType().equals(InformationType.SetCoveringInferenceInformation) 
					&& values != null && values.size() == 2) {
				covered += (Double) values.get(0);
				observed += (Double) values.get(1);
			}
		}
		if(observed != 0) {
			return covered / observed;
		}
		return -1;
	}
	
}
