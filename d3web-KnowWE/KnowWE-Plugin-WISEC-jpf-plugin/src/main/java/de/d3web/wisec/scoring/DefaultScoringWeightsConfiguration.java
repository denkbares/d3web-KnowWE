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

public class DefaultScoringWeightsConfiguration extends
		ScoringWeightsConfiguration {

	public DefaultScoringWeightsConfiguration() {
		String[] weightstr = new String[] {
				"P", "1", "B", "1", "Aqua_Tox", "1", "Multiple_Tox", "1", "EDC", "1", "CMR", "1",
				"LRT", "1", "Climatic_Change", "1", "Risk_related", "1", "Political", "1",
				"Exposure", "1"
		};
		setWeights(weightstr);
	}
}
