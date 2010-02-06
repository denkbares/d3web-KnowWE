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
package de.d3web.we.plugin.calendar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.knowwe.plugin.Instantiation;


public class CalendarModule implements Instantiation{

	private static Map<String,String> persons;

	public static ResourceBundle getCalendarBundle() {
		
		return ResourceBundle.getBundle("Calendar_messages");
	}
	
	public static ResourceBundle getCalendarBundle(KnowWEUserContext user) {
		
		Locale.setDefault(KnowWEEnvironment.getInstance().getWikiConnector().getLocale(user.getHttpRequest()));
		return getCalendarBundle();
	}
	
	public static Map<String, String> getPersons() {
		return persons;
	}

	private static void importPersons() {
		ResourceBundle rb = ResourceBundle.getBundle("persons");
				
		Map<String, String> persons = new HashMap<String, String>();
		
		Iterator<String> it = rb.keySet().iterator();
		
		while (it.hasNext()) {
			String type = (String) it.next();
			String fullName = rb.getString(type);
			if (fullName == null || fullName.length() == 0) {
				fullName = type;
			}
			persons.put(type, fullName);
		}
			
		CalendarModule.persons = persons;	
	}
	
	@Override
	public void init(ServletContext context) {
		importPersons();
	}
}
