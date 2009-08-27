package de.d3web.we.basic;

public enum InformationType {
	
	OriginalUserInformation("OriginalUserInformation"), 
	AlignedUserInformation("AlignedUserInformation"), 
	HeuristicInferenceInformation("HeuristicInferenceInformation"), 
	SetCoveringInferenceInformation("SetCoveringInferenceInformation"), 
	XCLInferenceInformation("XCLInferenceInformation"),
	SolutionInformation("SolutionInformation"),
	ExternalInformation("ExternalInformation"),
	ClusterInformation("ClusterInformation"), 
	CaseBasedInferenceInformation("CaseBasedInferenceInformation");
	
	private final String idString;
	
	private InformationType(String idString) {
		this.idString = idString;
	}
	
	public String getIdString() {
		return idString;
	}
	
	public static InformationType getType(String idString) {
		for (InformationType type : values()) {
			if(type.getIdString().equals(idString)) {
				return type;
			}
		}
		return null;
	}
	
}
