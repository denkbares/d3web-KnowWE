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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.term.Term;

public class BlackboardImpl implements Blackboard {

	private List<Information> originalUserInformation;

	private List<Information> alignedUserInformation;

	private List<Information> allInformation;

	private ISetMap<Term, Information> inferenceMap;

	private GlobalSolutionManager globalSolutionManager;

	public BlackboardImpl() {
		super();
		originalUserInformation = new ArrayList<Information>();
		alignedUserInformation = new ArrayList<Information>();
		allInformation = new ArrayList<Information>();
		inferenceMap = new SetMap<Term, Information>();
		globalSolutionManager = new GlobalSolutionManager(new DefaultGlobalSolutionStrategy());
	}

	@Override
	public Information inspect(Information info) {
		Information bestInfo = null;
		bestInfo = getBestInfo(info);
		return bestInfo;
	}

	private Information getBestInfo(Information origin) {
		List<Information> infoHistory = new ArrayList<Information>(
				allInformation);
		Collections.reverse(infoHistory);
		for (Information historyInfo : infoHistory) {
			if (historyInfo.equalsNamespaces(origin)) {
				return historyInfo;
			}
		}
		return null;

	}

	@Override
	public void update(Information info) {
		InformationType infoType = info.getInformationType();
		removeOldInformation(info);
		if (InformationType.OriginalUserInformation.equals(infoType)) {
			originalUserInformation.add(info);
		}
		else if (InformationType.AlignedUserInformation.equals(infoType)) {
			alignedUserInformation.add(info);
		}
		else if ((InformationType.SolutionInformation.equals(infoType) || InformationType.ClusterInformation.equals(infoType))
				&& TerminologyType.diagnosis.equals(info.getTerminologyType())) {
			globalSolutionManager.update(info);
		}

		if (info.getInformationType().equals(InformationType.HeuristicInferenceInformation)
				|| info.getInformationType().equals(InformationType.SetCoveringInferenceInformation)
				|| info.getInformationType().equals(InformationType.CaseBasedInferenceInformation)
					|| info.getInformationType().equals(InformationType.XCLInferenceInformation)) {
		}

		allInformation.add(info);
	}

	private void removeOldInformation(Information info) {
		Collection<Information> toRemove = new ArrayList<Information>();
		for (Information each : allInformation) {
			if (info.equalsNamespaces(each) && info.equalsTypes(each)) {
				toRemove.add(each);
			}
		}
		allInformation.removeAll(toRemove);
		alignedUserInformation.removeAll(toRemove);
		originalUserInformation.removeAll(toRemove);
	}

	@Override
	public void clear(Broker broker) {
		allInformation.clear();
		inferenceMap.clear();
		originalUserInformation.clear();
		alignedUserInformation.clear();
		globalSolutionManager.clear();
	}

	@Override
	public List<Information> getOriginalUserInformation() {
		return originalUserInformation;
	}

	@Override
	public Map<Term, SolutionState> getGlobalSolutions() {
		return globalSolutionManager.getGlobalSolutions();
	}

	@Override
	public ISetMap<Term, Information> getAssumptions() {
		return globalSolutionManager.getAssumptions();
	}

	@Override
	public List<Information> getAllInformation() {
		return allInformation;
	}

	@Override
	public Collection<Information> getInferenceInformation(Term term) {
		return inferenceMap.get(term);
	}

	@Override
	public void removeInformation(String namespace) {
		removeInfo(allInformation, namespace);
		for (Term each : inferenceMap.keySet()) {
			removeInfo(inferenceMap.get(each), namespace);
		}
		removeInfo(originalUserInformation, namespace);
		removeInfo(alignedUserInformation, namespace);
		globalSolutionManager.removeInformation(namespace);
	}

	public static void removeInfo(Collection<Information> coll, String namespace) {
		// TODO NOT STATIC!!!!!!
		Collection<Information> toRemove = new ArrayList<Information>();
		for (Information information : coll) {
			if (information.getNamespace().equals(namespace)) {
				toRemove.add(information);
			}
		}
		coll.removeAll(toRemove);
	}

}
