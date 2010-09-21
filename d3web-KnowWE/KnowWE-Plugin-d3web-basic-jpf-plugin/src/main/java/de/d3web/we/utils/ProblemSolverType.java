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

package de.d3web.we.utils;

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
			if (type.getIdString().equals(idString)) {
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
		if (informationType.equals(InformationType.HeuristicInferenceInformation)) return heuristic;
		if (informationType.equals(InformationType.SetCoveringInferenceInformation)) return setcovering;
		if (informationType.equals(InformationType.CaseBasedInferenceInformation)) return casebased;
		if (informationType.equals(InformationType.XCLInferenceInformation)) return xcl;
		return null;
	}

}
