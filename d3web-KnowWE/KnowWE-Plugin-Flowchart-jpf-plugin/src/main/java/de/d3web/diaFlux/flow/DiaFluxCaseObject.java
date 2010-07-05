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
package de.d3web.diaFlux.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.blackboard.SessionObject;
import de.d3web.diaFlux.inference.PathEntry;

/**
 *
 * @author Reinhard Hatko
 * Created on: 04.11.2009
 */
public class DiaFluxCaseObject extends SessionObject {
	
	private final Map<String, FlowData> map;
	private final List<PathEntry> pathEnds;
	private boolean continueFlowing;
	
	
	public DiaFluxCaseObject(CaseObjectSource theSourceObject, Map<String, FlowData> map) {
		super(theSourceObject);
		this.map = Collections.unmodifiableMap(map);
		this.pathEnds = new ArrayList<PathEntry>();
	}
	
	

	public boolean contains(String id) {
		return map.containsKey(id);
	}
	
	public FlowData getFlowDataFor(String id) {
		return map.get(id);
	}
	
	
	public int size() {
		return map.size();
	}


	public Collection<FlowData> getFlows() {
		return map.values();
	}

	public Set<String> getIDs() {
		return map.keySet();
	}
	
	
	public List<PathEntry> getPathEnds() {
		return pathEnds;
	}
	
	
	
	public void setContinueFlowing(boolean continueFlowing) {
		this.continueFlowing = continueFlowing;
	}
	
	public boolean isContinueFlowing() {
		return continueFlowing;
	}
	
	

}
