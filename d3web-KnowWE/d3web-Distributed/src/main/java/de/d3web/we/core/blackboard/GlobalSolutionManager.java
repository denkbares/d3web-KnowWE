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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.terminology.term.Term;

public class GlobalSolutionManager {

	private Map<Term, SolutionState> solutionsMap;
	private ISetMap<Term, Information> assumptionMap;
	private GlobalSolutionStrategy strategy;
	private DPSEnvironment environment;

	public GlobalSolutionManager(DPSEnvironment environment, GlobalSolutionStrategy strategy) {
		super();
		this.environment = environment;
		this.strategy = strategy;
		solutionsMap = new HashMap<Term, SolutionState>();
		assumptionMap = new SetMap<Term, Information>();
	}

	public Map<Term, SolutionState> getGlobalSolutions() {
		return solutionsMap;
	}

	public ISetMap<Term, Information> getAssumptions() {
		return assumptionMap;
	}

	public void update(Information info) {
		if (info.getValues() == null || info.getValues().isEmpty()) return;
		List<Term> solutionTerms = new ArrayList<Term>();
		for (Term eachTerm : solutionTerms) {
			if (eachTerm == null) continue;
			updateTerm(eachTerm, info);
		}
	}

	private void updateTerm(Term term, Information info) {
		Object obj = info.getValues().get(0);
		if (obj == null || !(obj instanceof SolutionState)) return;
		updateAssuptions(term, info);
		SolutionState state = strategy.calculateState(assumptionMap.get(term));
		solutionsMap.put(term, state);
	}

	private void updateAssuptions(Term term, Information info) {
		Collection<Information> assuptions = assumptionMap.get(term);
		if (assuptions != null) {
			for (Information each : new ArrayList<Information>(assuptions)) {
				if (each.equalsNamespaces(info)) {
					assuptions.remove(each);
				}
			}
		}
		assumptionMap.add(term, info);
	}

	public void clear() {
		solutionsMap.clear();
		assumptionMap.clear();
	}

	public void removeInformation(String namespace) {
		for (Term each : assumptionMap.keySet()) {
			BlackboardImpl.removeInfo(assumptionMap.get(each), namespace);
		}
	}

}
