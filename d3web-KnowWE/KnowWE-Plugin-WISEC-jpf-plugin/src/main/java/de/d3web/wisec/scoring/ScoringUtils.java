package de.d3web.wisec.scoring;

import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;

public class ScoringUtils {
	
	public static double criteriaFactorFor(String criteriaValue) {
		if      (criteriaValue.equals("1")) return 1;
		else if (criteriaValue.equals("2")) return 0.5;
		else if (criteriaValue.equals("3")) return 0.25;
		else if (criteriaValue.equals("X")) return -0.25;
		else if (criteriaValue.equals("u")) return -0.5;
		else return 0;
	}
	
	public static double computeScoreFor(WISECModel model, ScoringWeightsConfiguration weights, Substance substance, String criteriaType) {
		double scoring = 0;
		double inclucedLists = 0;
		for (SubstanceList list : model.getSubstanceListsContaining(substance.getName())) {
			String criteriaValue = list.criteria.get(criteriaType);
			if (criteriaValue != null) {
				inclucedLists ++;
				scoring += criteriaFactorFor(criteriaValue) * weights.weightFor(criteriaType);
			}
		}
		if (inclucedLists == 0) {
			return 0;
		}
		else {
			return scoring / inclucedLists;
		}
	}

	public static Double computeTotalScoreFor(WISECModel model, ScoringWeightsConfiguration configuration, Substance substance) {
		double totalScore = 0;
		for (String  criteriaName : configuration.getCriterias()) {
			totalScore += computeScoreFor(model, configuration, substance, criteriaName);
		}
		return Double.valueOf(totalScore);
	}
	
	public static String prettyPrint(double score) {
		return prettyPrint(score, 4);
	}
	
	public static String prettyPrint(double score, int decimalPlace) {
		String formatStr = "%10."+decimalPlace+"f";
		return String.format(formatStr, score);
	}
	
}
