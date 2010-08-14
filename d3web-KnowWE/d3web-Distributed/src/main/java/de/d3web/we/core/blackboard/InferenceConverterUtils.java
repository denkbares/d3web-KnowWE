/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.core.blackboard;

import de.d3web.we.basic.SolutionState;

public class InferenceConverterUtils {

	// public static SolutionState getStateByCovering(double covering) {
	// if(covering > 0.90) {
	// return SolutionState.ESTABLISHED;
	// } else if (covering > 0.50) {
	// return SolutionState.SUGGESTED;
	// } else if (covering > 0.20) {
	// return SolutionState.UNCLEAR;
	// } else if (covering >= 0) {
	// return SolutionState.EXCLUDED;
	// }
	// return SolutionState.UNCLEAR;
	// }

	public static SolutionState getStateByCovering(double covering) {
		if (covering > 0.30) {
			return SolutionState.ESTABLISHED;
		}
		else if (covering > 0.20) {
			return SolutionState.SUGGESTED;
		}
		else if (covering > 0.10) {
			return SolutionState.UNCLEAR;
		}
		else if (covering >= 0) {
			return SolutionState.EXCLUDED;
		}
		return SolutionState.UNCLEAR;
	}

	public static SolutionState getStateByScore(double score) {
		if (score >= 42) {
			return SolutionState.ESTABLISHED;
		}
		else if (score >= 10) {
			return SolutionState.SUGGESTED;
		}
		else if (score > -42) {
			return SolutionState.UNCLEAR;
		}
		else {
			return SolutionState.EXCLUDED;
		}

	}

}
