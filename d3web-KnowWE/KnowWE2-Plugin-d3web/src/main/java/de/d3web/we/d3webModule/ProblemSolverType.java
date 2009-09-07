package de.d3web.we.d3webModule;

import de.d3web.we.basic.InformationType;

public enum ProblemSolverType {

	heuristic("heuristic"), setcovering("setcovering"), casebased("casebased"), xcl("xcl");
	
	private final String idString;
	
	private ProblemSolverType(String idString) {
		this.idString = idString;
	}
	
	public String getIdString() {
		return idString;
	}
	
	public static ProblemSolverType getType(String idString) {
		for (ProblemSolverType type : values()) {
			if(type.getIdString().equals(idString)) {
				return type;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getIdString();
	}

	public static ProblemSolverType getType(InformationType informationType) {
		if(informationType.equals(InformationType.HeuristicInferenceInformation)) return heuristic;
		if(informationType.equals(InformationType.SetCoveringInferenceInformation)) return setcovering;
		if(informationType.equals(InformationType.CaseBasedInferenceInformation)) return casebased;
		if(informationType.equals(InformationType.XCLInferenceInformation)) return xcl;
		return null;
	}
	
	
}
