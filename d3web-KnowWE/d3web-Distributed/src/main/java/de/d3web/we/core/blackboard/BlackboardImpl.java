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

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.basic.Information;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.term.Term;

public class BlackboardImpl implements Blackboard {

	private List<Information> originalUserInformation;

	private List<Information> allInformation;

	private ISetMap<Term, Information> inferenceMap;

	public BlackboardImpl() {
		super();
		originalUserInformation = new ArrayList<Information>();
		allInformation = new ArrayList<Information>();
		inferenceMap = new SetMap<Term, Information>();
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
	public void clear(Broker broker) {
		allInformation.clear();
		inferenceMap.clear();
		originalUserInformation.clear();
	}

	@Override
	public List<Information> getOriginalUserInformation() {
		return originalUserInformation;
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
