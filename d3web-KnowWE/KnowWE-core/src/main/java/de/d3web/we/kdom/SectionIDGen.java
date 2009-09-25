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

package de.d3web.we.kdom;

import java.util.HashSet;

public class SectionIDGen {
	
	private HashSet<String> assignedIDs = new HashSet<String>();
	
	private int index;
	
	/**
	 * If the concatenation of ID and Suffix hasn't got assigned yet, the same ID 
	 * gets returned. If it got assigned, the ID will change to a random generated, yet
	 * unassigned ID!
	 */
	public String createID(String id, String suffix) {
		if (id == null || id.equals("")) 
			id = nextGeneratedID();
		
		if (suffix == null)
			suffix = "";
		
		while (isAssignedCombination(id, suffix)) {
			id = nextGeneratedID();
		}
		assignedIDs.add(id + suffix);
		return id + suffix;
	}
	
	/**
	 * Returns whether this ID is already assigned with this particular suffix.
	 */
	private boolean isAssignedCombination(String id, String suffix) {
		if (suffix != null) {
			id = id + suffix;
		}
		return assignedIDs.contains(id);
	}
	
	public boolean isAssignedID(String id) {
		return assignedIDs.contains(id);
	}
	
	public void assignID(String id) {
		assignedIDs.add(id);
	}
	
	public int getNumberOfNodes() {
		return assignedIDs.size();
	}
	
	// TODO: Generate infinite number of IDs...
	private String nextGeneratedID() {
		index++;
		return "Node" + index;
	}
	
	public void increaseIndexNumberBy(int number) {
		index += number;
	}
	
	
}
