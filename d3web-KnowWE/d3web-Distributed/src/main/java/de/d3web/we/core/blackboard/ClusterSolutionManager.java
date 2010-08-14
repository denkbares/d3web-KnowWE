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
import java.util.List;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

public class ClusterSolutionManager {

	private ISetMap<Term, Information> assumptionMap;
	private List<ClusterSolutionStrategy> strategies;
	private DPSEnvironment environment;
	private Broker broker;
	private String clusterID;

	public ClusterSolutionManager(DPSEnvironment environment, Broker broker, String clusterID) {
		super();
		this.environment = environment;
		this.broker = broker;
		this.strategies = new ArrayList<ClusterSolutionStrategy>();
		strategies.add(new HeuristicClusterStrategy());
		strategies.add(new SetCoveringClusterStrategy());
		this.clusterID = clusterID;
		assumptionMap = new SetMap<Term, Information>();
	}

	public ISetMap<Term, Information> getAssumptions() {
		return assumptionMap;
	}

	public void update(Information info) {
		if (info.getValues() == null || info.getValues().isEmpty()) return;
		List<Term> solutionTerms = environment.getTerminologyServer().getBroker().getAlignedTerms(
				info.getIdentifiableObjectInstance());
		for (Term eachTerm : solutionTerms) {
			if (eachTerm == null) continue;
			updateTerm(eachTerm, info);
		}
	}

	private void updateTerm(Term term, Information info) {
		updateAssuptions(term, info);
		SolutionState state = null;
		for (ClusterSolutionStrategy each : strategies) {
			SolutionState newState = each.calculateState(assumptionMap.get(term));
			state = calculateState(state, newState);
		}
		List<Object> values = new ArrayList<Object>();
		values.add(state);
		broker.update(new Information(clusterID, (String) term.getInfo(TermInfoType.TERM_NAME),
				values, info.getTerminologyType(), InformationType.ClusterInformation));
	}

	private SolutionState calculateState(SolutionState first, SolutionState second) {
		if (first == null) {
			return second;
		}
		if (first.equals(SolutionState.CONFLICT)) {
			return SolutionState.CONFLICT;
		}
		if (first.equals(SolutionState.ESTABLISHED)) {
			if (second.equals(SolutionState.EXCLUDED) || second.equals(SolutionState.CONFLICT)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.ESTABLISHED;
		}
		else if (first.equals(SolutionState.SUGGESTED)) {
			if (second.equals(SolutionState.EXCLUDED)) {
				return SolutionState.EXCLUDED;
			}
			else if (second.equals(SolutionState.ESTABLISHED)) {
				return SolutionState.ESTABLISHED;
			}
			else if (second.equals(SolutionState.CONFLICT)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.SUGGESTED;

		}
		else if (first.equals(SolutionState.EXCLUDED)) {
			if (second.equals(SolutionState.ESTABLISHED)) {
				return SolutionState.CONFLICT;
			}
			return SolutionState.EXCLUDED;
		}
		return second;
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
		assumptionMap.clear();
	}
}
