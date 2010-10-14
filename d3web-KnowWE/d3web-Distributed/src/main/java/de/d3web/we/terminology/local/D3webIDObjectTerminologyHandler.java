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

package de.d3web.we.terminology.local;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.we.terminology.term.TerminologyHandler;

public class D3webIDObjectTerminologyHandler extends TerminologyHandler<IDObject, IDObject> {

	@Override
	protected List<IDObject> fifo(IDObject no) {
		List<IDObject> queue = new ArrayList<IDObject>();
		queue.add(no);
		List<IDObject> result = new ArrayList<IDObject>();
		fifo(queue, result);
		for (IDObject object : queue) {
			result.add(object);
		}
		return result;
	}

	private void fifo(List<? extends IDObject> toExpand, List<IDObject> result) {
		if (toExpand == null || toExpand.isEmpty()) return;
		List<IDObject> newToExpand = new ArrayList<IDObject>();
		for (IDObject each : toExpand) {
			if (!result.contains(each)) {
				result.add(each);
				if (each instanceof NamedObject) {
					NamedObject no = (NamedObject) each;
					for (TerminologyObject child : no.getChildren()) {
						if (!toExpand.contains(child) && !result.contains(child)) {
							newToExpand.add(child);
						}
					}
				}
				if (each instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice) each;
					for (Choice answer : qc.getAllAlternatives()) {
						if (!toExpand.contains(answer) && !result.contains(answer)) {
							newToExpand.add(answer);
						}
					}
				}
			}
		}
		fifo(newToExpand, result);
	}

	@Override
	public D3webIDObjectTerminologyHandler newInstance() {
		return new D3webIDObjectTerminologyHandler();
	}
}
