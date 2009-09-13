/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.core.blackboard;


import java.util.Collection;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.SolutionState;

public class DefaultGlobalSolutionStrategy implements GlobalSolutionStrategy {

	public SolutionState calculateState(Collection<Information> infos) {
		SolutionState result = null;
		for (Information each : infos) {
			result = calculateState(result, each);
		}
		return result;
	}
	/*
	private SolutionState calculateState(SolutionState first, Information info) {
		SolutionState second = (SolutionState) info.getValues().get(0);
		if(first == null) {
			return second;
		}
		if(first.equals(SolutionState.CONFLICT)) {
			return SolutionState.CONFLICT;
		}
		if(first.equals(SolutionState.ESTABLISHED)) {
			if(second.equals(SolutionState.EXCLUDED) || second.equals(SolutionState.CONFLICT)) {
				return SolutionState.CONFLICT;
			} 
			return SolutionState.ESTABLISHED;
		} else if(first.equals(SolutionState.SUGGESTED)) {
			if(second.equals(SolutionState.EXCLUDED)) {
				return SolutionState.EXCLUDED;
			} else if(second.equals(SolutionState.ESTABLISHED)) {
				return SolutionState.ESTABLISHED;
			} else if(second.equals(SolutionState.CONFLICT)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.SUGGESTED;
			
		} else if(first.equals(SolutionState.EXCLUDED)) {
			if(second.equals(SolutionState.ESTABLISHED)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.EXCLUDED;
		} 
		return second;
	}*/
	
	private SolutionState calculateState(SolutionState first, Information info) {
		SolutionState second = (SolutionState) info.getValues().get(0);
		if(first == null) {
			return second;
		}
		int comp = first.compareTo(second);
		if(comp < 0) {
			return first;
		} else if (comp > 0) {
			return second;
		} else {
			return second;
		}
	}
}
