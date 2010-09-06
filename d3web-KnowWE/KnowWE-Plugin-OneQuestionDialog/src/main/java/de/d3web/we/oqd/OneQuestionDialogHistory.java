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
package de.d3web.we.oqd;

import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.InterviewObject;

/**
 * 
 * @author Florian Ziegler
 * @created 06.09.2010
 */
public class OneQuestionDialogHistory {

	private static OneQuestionDialogHistory instance = null;
	private final LinkedList<InterviewObject> interviewObjects;

	private OneQuestionDialogHistory() {
		interviewObjects = new LinkedList<InterviewObject>();
	}

	public static OneQuestionDialogHistory getInstance() {
		if (instance == null) {
			instance = new OneQuestionDialogHistory();
		}
		return instance;
	}

	/**
	 * adds an interviewObject to the history, if it is not already there
	 * 
	 * @created 06.09.2010
	 * @param o
	 */
	public void addInterviewObject(InterviewObject o) {
		if (o != null && !interviewObjects.contains(o)) {
			interviewObjects.add(o);
		}
	}

	public List<InterviewObject> getInterviewObjects() {
		return interviewObjects;
	}

	/**
	 * returns the predecessor of an InterviewObject
	 * @created 06.09.2010
	 * @param o
	 * @return
	 */
	public InterviewObject getPredecessorOf(InterviewObject o) {
		int index = interviewObjects.indexOf(o);
		if (index == 0) {
			return null;
		}
		else {
			return interviewObjects.get(index - 1);
		}
	}

	/**
	 * removes the last element of the history
	 * 
	 * @created 06.09.2010
	 */
	public void removeLast() {
		interviewObjects.removeLast();
	}
}
