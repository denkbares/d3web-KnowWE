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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.term.Term;

public class BlackboardImpl implements Blackboard {

	private final DPSEnvironment environment;

	private List<Information> originalUserInformation;

	private List<Information> alignedUserInformation;

	private List<Information> allInformation;

	private ISetMap<Term, Information> inferenceMap;

	private GlobalSolutionManager globalSolutionManager;

	private Map<String, ClusterSolutionManager> clusterManagers;

	public BlackboardImpl(DPSEnvironment environment) {
		super();
		this.environment = environment;
		originalUserInformation = new ArrayList<Information>();
		alignedUserInformation = new ArrayList<Information>();
		allInformation = new ArrayList<Information>();
		inferenceMap = new SetMap<Term, Information>();
		globalSolutionManager = new GlobalSolutionManager(environment,
				new DefaultGlobalSolutionStrategy());
	}

	public void initializeClusterManagers(Broker broker) {
		clusterManagers = new HashMap<String, ClusterSolutionManager>();
		for (String each : environment.getClusters()) {
			clusterManagers.put(each, new ClusterSolutionManager(environment,
					broker, each));
		}
	}

	public Information inspect(Information info) {
		Information bestInfo = null;
		bestInfo = getBestInfo(info);
		if (bestInfo == null) {
			bestInfo = getBestAlignedInfo(info);
		}
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

	private Information getBestAlignedInfo(Information origin) {
		List<Information> infoHistory = new ArrayList<Information>(
				allInformation);
		Collection<Information> alignedInfos = environment
				.getAlignedInformation(origin);
		Collections.reverse(infoHistory);
		for (Information historyInfo : infoHistory) {
			for (Information alignedInfo : alignedInfos) {
				if (historyInfo.equalsNamespaces(alignedInfo)) {
					for (Information alignedHistoryInfo : environment
							.getAlignedInformation(historyInfo)) {
						if (alignedHistoryInfo.equalsNamespaces(origin)) {
							return alignedHistoryInfo;
						}
					}
				}
			}
		}
		return null;
	}

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
		else {
			// clusters:
			if (info.getInformationType().equals(
					InformationType.HeuristicInferenceInformation)
					|| info.getInformationType().equals(
							InformationType.SetCoveringInferenceInformation)
							|| info.getInformationType().equals(
									InformationType.XCLInferenceInformation)) {
				if (info.getTerminologyType().equals(TerminologyType.diagnosis)) {
					String clusterID = environment.getCluster(info
							.getNamespace());
					if (clusterID != null) {
						ClusterSolutionManager manager = clusterManagers.get(clusterID);
						if (manager != null) {
							manager.update(info);
						}
					}
				}
			}
		}
		if (info.getInformationType().equals(InformationType.HeuristicInferenceInformation)
				|| info.getInformationType().equals(InformationType.SetCoveringInferenceInformation)
				|| info.getInformationType().equals(InformationType.CaseBasedInferenceInformation)
					|| info.getInformationType().equals(InformationType.XCLInferenceInformation)) {
			List<Term> solutionTerms = environment.getTerminologyServer().getBroker().getAlignedTerms(
					info.getIdentifiableObjectInstance());
			for (Term eachTerm : new ArrayList<Term>(solutionTerms)) {
				Collection<Information> dummy = inferenceMap.get(eachTerm);
				if (dummy == null) continue;
				for (Information eachInfo : new ArrayList<Information>(dummy)) {
					if (eachInfo.equalsNamespaces(info)) {
						inferenceMap.remove(eachTerm, eachInfo);
					}
				}
			}
			inferenceMap.addAll(solutionTerms, info);
		}

		allInformation.add(info);
	}

	private boolean checkInfoForClusters(Information origin) {
		if (origin.getInformationType().equals(
				InformationType.HeuristicInferenceInformation)
				|| origin.getInformationType().equals(
						InformationType.SetCoveringInferenceInformation)) {
			if (origin.getTerminologyType().equals(TerminologyType.diagnosis)) {
				return !environment.getFriendlyServices(origin.getNamespace())
						.isEmpty();
			}
		}
		return false;
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

	public void clear(Broker broker) {
		allInformation.clear();
		inferenceMap.clear();
		originalUserInformation.clear();
		alignedUserInformation.clear();
		globalSolutionManager.clear();
		initializeClusterManagers(broker);
	}

	public List<Information> getOriginalUserInformation() {
		return originalUserInformation;
	}

	public Map<Term, SolutionState> getGlobalSolutions() {
		return globalSolutionManager.getGlobalSolutions();
	}

	public ISetMap<Term, Information> getAssumptions() {
		return globalSolutionManager.getAssumptions();
	}

	public List<Information> getAllInformation() {
		return allInformation;
	}

	public void setAllInformation(List<Information> infos) {
		alignedUserInformation = new ArrayList<Information>();
		allInformation = infos;
		for (Information information : infos) {
			if (information.getInformationType() != null
					&& information.getInformationType().equals(
							InformationType.OriginalUserInformation)) {
				originalUserInformation.add(information);
			}
			else if (information.getInformationType() != null
					&& information.getInformationType().equals(
							InformationType.AlignedUserInformation)) {
				alignedUserInformation.add(information);
			}
		}
	}

	public Collection<Information> getInferenceInformation(Term term) {
		return inferenceMap.get(term);
	}

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
