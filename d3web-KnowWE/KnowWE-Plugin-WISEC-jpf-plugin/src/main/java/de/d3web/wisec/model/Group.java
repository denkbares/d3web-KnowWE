/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.wisec.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple datastructure for groups
 * 
 * @author Sebastian Furth
 * @created 01/09/2010
 */
public class Group {

	private static int idcounter = 1;

	private final List<String> substances;
	private final String name;
	private final int id;

	public Group(String name) {
		this.id = idcounter;
		this.name = name;
		this.substances = new ArrayList<String>();
		idcounter++;
	}

	public void addSubstance(String substance) {
		if (!substances.contains(substance)) {
			substances.add(substance);
		}
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}

	public List<String> getSubstances() {
		return substances;
	}

}
