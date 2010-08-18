/*
 * Copyright (C) 2010 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.wisec.scoring;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScoringWeightsConfiguration {

	public int MAX_SUBSTANCES_IN_RATING = 10000; // means no cutting
	private String name = "NONAME";
	// the weights for the particular criteria
	private final Map<String, Double> weights = new LinkedHashMap<String, Double>();

	// P, B, Aqua_Tox, Multiple_Tox, EDC, CMR, LRT, Climatic_Change,
	// Risk_related, Political, Exposure
	public void setWeights(String[] weightstr) {
		for (int i = 0; i < weightstr.length; i = i + 2) {
			String key = weightstr[i];
			String val = weightstr[i + 1];
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
