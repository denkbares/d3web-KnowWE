package de.d3web.wisec.scoring;

public class DefaultScoringWeightsConfiguration extends
		ScoringWeightsConfiguration {
	
	public DefaultScoringWeightsConfiguration() {
		String[] weightstr = new String[] {
			"P", "1", "B", "1", "Aqua_Tox", "1", "Multiple_Tox", "1", "EDC", "1", "CMR", "1", 
			"LRT", "1", "Climatic_Change", "1", "Risk_related", "1", "Political", "1", "Exposure", "1"
		};
		setWeights(weightstr);
	}
}
