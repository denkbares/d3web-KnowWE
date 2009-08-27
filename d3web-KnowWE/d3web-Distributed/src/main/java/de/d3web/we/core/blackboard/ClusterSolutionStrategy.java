package de.d3web.we.core.blackboard;

import java.util.Collection;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.SolutionState;

public interface ClusterSolutionStrategy {

	public SolutionState calculateState(Collection<Information> infos);

	public double calculateInference(Collection<Information> infos);
	
}
