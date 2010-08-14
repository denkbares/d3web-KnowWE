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

package de.d3web.we.core.dialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;

public class DistributedDialogControl implements DialogControl {

	private List<Dialog> instant;
	private List<Dialog> indicated;
	private List<Dialog> history;
	private Dialog instantCursor;
	private Dialog indicatedCursor;

	public DistributedDialogControl() {
		super();
		history = new LinkedList<Dialog>();
		instant = new LinkedList<Dialog>();
		indicated = new LinkedList<Dialog>();
		instantCursor = null;
		indicatedCursor = null;
	}

	public void delegate(KnowledgeServiceSession kss,
			KnowledgeServiceSession reason, boolean userIndicated, boolean instantly, String comment) {
		Dialog newDialog = new Dialog(kss, reason, comment);
		if (userIndicated) {
			// show really instantly!
			if (instantCursor == null) {
				instant.add(newDialog);
			}
			else {
				int index = instant.indexOf(instantCursor);
				if (index < 0) {
					index = 0;
				}
				if (instant.contains(newDialog)) {
					instant.remove(newDialog);
				}
				instant.add(index, newDialog);
			}
		}
		else {
			if (instantly) {
				int index = instant.indexOf(instantCursor);
				if (index < 0) {
					index = 0;
				}
				if (!instant.contains(newDialog)) {
					instant.add(index, newDialog);
				}
			}
			else {
				if (!indicated.contains(newDialog)) {
					indicated.add(newDialog);
				}
			}
		}
	}

	public void cancelDelegate(KnowledgeServiceSession kss) {
		for (Dialog each : instant) {
			if (each.getDialog().equals(kss)) {
				each.setCancelled(true);
				break;
			}
		}
		for (Dialog each : indicated) {
			if (each.getDialog().equals(kss)) {
				each.setCancelled(true);
				break;
			}
		}
	}

	public void finished(KnowledgeServiceSession kss) {
		for (Dialog each : instant) {
			if (each.getDialog().equals(kss)) {
				each.setFinished(true);
				break;
			}
		}
		for (Dialog each : indicated) {
			if (each.getDialog().equals(kss)) {
				each.setFinished(true);
				break;
			}
		}

	}

	public final List<Dialog> getIndicatedDialogs() {
		return indicated;
	}

	public final List<Dialog> getInstantIndicatedDialogs() {
		return instant;
	}

	public void clear() {
		instant.clear();
		indicated.clear();
		history.clear();
		instantCursor = null;
		indicatedCursor = null;
	}

	public boolean isUserInterventionNeeded() {
		Dialog next = getNextActiveDialog();
		return next != null && next.getComment() != null;
	}

	public boolean isDialogSwitchNeeded() {
		Dialog shown = getShownDialog();
		Dialog next = getNextActiveDialog();
		if (next == null) return false;
		if (shown == null) return true;
		return !shown.equals(next);
	}

	public Dialog getShownDialog() {
		if (instantCursor != null && instantCursor.isActive()) {
			return instantCursor;
		}
		else if (indicatedCursor != null && indicatedCursor.isActive()) {
			return indicatedCursor;
		}
		else return indicatedCursor;
	}

	public Dialog showNextActiveDialog() {
		Dialog current = getShownDialog();
		if (current != null && current.isActive()) {
			return current;
		}
		else {
			Dialog nextInstantDialog = null;
			for (Dialog each : instant) {
				if (each.isActive()) {
					nextInstantDialog = each;
					instantCursor = nextInstantDialog;
					break;
				}
			}
			Dialog nextDialog = null;
			for (Dialog each : indicated) {
				if (each.isActive()) {
					nextDialog = each;
					indicatedCursor = nextDialog;
					break;
				}
			}

			if (nextInstantDialog != null) {
				return instantCursor;
			}
			else {
				return nextDialog;
			}
		}
	}

	public Dialog getNextActiveDialog() {
		Dialog current = getShownDialog();
		if (current != null && current.isActive()) {
			return current;
		}
		else {
			Dialog nextDialog = null;
			for (Dialog each : new ArrayList<Dialog>(instant)) {
				if (each.isActive()) {
					if (nextDialog == null) {
						nextDialog = each;
					}
				}
				else {
					instant.remove(each);
					if (!history.contains(each)) {
						history.add(each);
					}
				}
			}
			if (nextDialog == null) {
				for (Dialog each : new ArrayList<Dialog>(indicated)) {
					if (each.isActive()) {
						if (nextDialog == null) {
							nextDialog = each;
						}
					}
					else {
						indicated.remove(each);
						if (!history.contains(each)) {
							history.add(each);
						}
					}
				}
			}
			return nextDialog;
		}
	}

	public List<Dialog> getHistory() {
		return history;
	}

}
