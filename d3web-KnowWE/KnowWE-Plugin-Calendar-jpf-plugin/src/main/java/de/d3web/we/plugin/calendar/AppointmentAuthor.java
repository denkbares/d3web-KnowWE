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

package de.d3web.we.plugin.calendar;

import java.util.Map;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

public class AppointmentAuthor extends AbstractKnowWEObjectType {

	@Override
	protected void init() {

		Map<String, String> persons = CalendarModule.getPersons();

		StringBuffer regexString = new StringBuffer("(");

		for (String elem : persons.keySet()) {
			regexString.append(elem + "|");
		}
		regexString.replace(regexString.length() - 1, regexString.length(), ")");

		sectionFinder = new RegexSectionFinder(regexString.toString());
	}

	@Override
	public void cleanStoredInfos(String articleName) {
		// TODO Auto-generated method stub
	}

}
