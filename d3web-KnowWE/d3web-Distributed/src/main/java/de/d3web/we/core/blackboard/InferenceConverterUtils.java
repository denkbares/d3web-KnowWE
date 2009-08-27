package de.d3web.we.core.blackboard;

import de.d3web.we.basic.SolutionState;

public class InferenceConverterUtils {

//	public static SolutionState getStateByCovering(double covering) {
//		if(covering > 0.90) {
//			return SolutionState.ESTABLISHED;
//		} else if (covering > 0.50) {
//			return SolutionState.SUGGESTED;
//		} else if (covering > 0.20) {
//			return SolutionState.UNCLEAR;
//		} else if (covering >= 0) {
//			return SolutionState.EXCLUDED;
//		}
//		return SolutionState.UNCLEAR;
//	}
	
	public static SolutionState getStateByCovering(double covering) {
		if(covering > 0.30) {
			return SolutionState.ESTABLISHED;
		} else if (covering > 0.20) {
			return SolutionState.SUGGESTED;
		} else if (covering > 0.10) {
			return SolutionState.UNCLEAR;
		} else if (covering >= 0) {
			return SolutionState.EXCLUDED;
		}
		return SolutionState.UNCLEAR;
	}

	public static SolutionState getStateByScore(double score) {
		if(score >= 42) {
			return SolutionState.ESTABLISHED;
		} else if (score >= 10) {
			return SolutionState.SUGGESTED;
		} else if (score > -42) {
			return SolutionState.UNCLEAR;
		} else {
			return SolutionState.EXCLUDED;
		}
		
	}
	
	
}
