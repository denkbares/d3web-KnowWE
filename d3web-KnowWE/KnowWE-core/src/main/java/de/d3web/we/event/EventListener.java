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

package de.d3web.we.event;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * An eventlistener is an extension to KnowWE, that can listen to events firing
 * during operations handled by KnowWE (e.g., init, page-save...)
 *
 * Events are right now represented by Strings for simplicity
 *
 * @author Jochen
 *
 */
public interface EventListener {

	/**
	 * returns (all) the event names the listener wants to listen to The
	 * Eventmanager will register the listener for these events
	 *
	 * @return
	 */
	public String[] getEvents();


	/**
	 * this method will be called when an event is fired that this listener is
	 * registered for
	 *
	 * @param username
	 * @param s
	 * @param eventName
	 */
	public void notify(String username, Section<? extends KnowWEObjectType> s, String eventName);

}
