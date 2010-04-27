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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * A very simple EventManager Events are represented by Strings
 *
 * @author Jochen
 *
 */
public class EventManager {

	private static EventManager instance;

	public static EventManager getInstance() {
		if (instance == null) {
			instance = new EventManager();
		}
		return instance;
	}

	private final Map<String, List<EventListener>> listenerMap = new HashMap<String, List<EventListener>>();

	/**
	 * Creates the listener map by fetching all EventListener extensions from
	 * the PluginManager
	 *
	 *
	 */
	public EventManager() {
		// get all EventListeners
		Extension[] exts = PluginManager.getInstance().getExtensions(
				"KnowWEExtensionPoints",
				"EventListener");
		for (Extension extension : exts) {
			Object o = extension.getSingleton();
			if (o instanceof EventListener) {
				EventListener listener = ((EventListener) o);
				for (String event : listener.getEvents()) {
					List<EventListener> list = null;

					if (listenerMap.containsKey(event)) {
						list = listenerMap.get(event);
					}
					else {
						list = new ArrayList<EventListener>();
						listenerMap.put(event,list);
					}
					list.add(listener);
				}

			}
		}
	}

	/**
	 * Fires events; the calls are distributed in the system where the
	 * corresponding events should be fired (also plugin may fire events)
	 *
	 * NOTE: unique names in event-names not guaranteed!
	 *
	 * @param username
	 * @param s
	 * @param eventName
	 */
	public void fireEvent(String username, Section<? extends KnowWEObjectType> s, String eventName) {

		List<EventListener> listeners = this.listenerMap.get(eventName);
		if (listeners != null) {
			for (EventListener eventListener : listeners) {
				eventListener.notify(username, s, eventName);
			}
		}

	}

}
