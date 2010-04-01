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

package de.d3web.we.terminology.local;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;

public class D3webNamedObjectTerminologyHandler extends LocalTerminologyHandler<TerminologyObject, TerminologyObject> {
	
	public D3webNamedObjectTerminologyHandler() {
		super();
	}

	
	protected List<TerminologyObject> fifo(TerminologyObject no) {
		List<TerminologyObject> queue = new ArrayList<TerminologyObject>();
		queue.add(no);
		List<TerminologyObject> result = new ArrayList<TerminologyObject>();
		fifo(queue, result);
		for (TerminologyObject object : queue) {
			//TODO: remove this, when getInfoStore is active 
			if (object instanceof NamedObject) {
				NamedObject nob = (NamedObject) object;
				Boolean privat = (Boolean) nob.getProperties().getProperty(Property.PRIVATE);
				if(privat == null || !privat) {
					result.add(object);
				}
			}
		}
		return result;
	}
	
	private void fifo(List<TerminologyObject> toExpand, List<TerminologyObject> result) {
		if(toExpand == null || toExpand.isEmpty()) return;
		List<TerminologyObject> newToExpand = new ArrayList<TerminologyObject>();
		for (TerminologyObject each : toExpand) {
			if(checkFilter(each) && !result.contains(each)) {
				//if(!each.getText().equals("P000") && !each.getText().equals("Q000")) {
				result.add(each);
				//}
				for (TerminologyObject child : each.getChildren()) {
					if(!toExpand.contains(child) && !result.contains(child)) {
						newToExpand.add(child);
					}
				}
			}
		}
		fifo(newToExpand, result);
	}
	
	
	
	public D3webNamedObjectTerminologyHandler newInstance() {
		return new D3webNamedObjectTerminologyHandler();
	}


	@Override
	public TerminologyObject getTerminologicalObject(String id) {
		for (TerminologyObject no : this) {
			if(no.getId().equals(id)) {
				return no;
			}
		}
		return null;
	}
	
	
	
}
