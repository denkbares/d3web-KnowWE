package de.d3web.wisec.scoring;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScoringWeightsConfiguration {
	public int MAX_SUBSTANCES_IN_RATING = 20;
	private String name = "NONAME";
	// the weights for the particular criteria
	private Map<String, Double> weights = new LinkedHashMap<String, Double>();
	
	// P, B, Aqua_Tox, Multiple_Tox, EDC, CMR, LRT, Climatic_Change, Risk_related, Political, Exposure 
	public void setWeights(String[] weightstr) {
		for (int i = 0; i < weightstr.length; i = i+2) {
			String key = weightstr[i];
			String val = weightstr[i+1];
			this.weights.put(key, Double.valueOf(val));
		}
	}

	public double weightFor(String criteriaType) {
		Double weight = this.weights.get(criteriaType);
		if (weight == null) {
			return 0;			
		}
		else {
			return weight.doubleValue();
		}
	}
	
	public Collection<String> getCriterias() {
		return weights.keySet();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
