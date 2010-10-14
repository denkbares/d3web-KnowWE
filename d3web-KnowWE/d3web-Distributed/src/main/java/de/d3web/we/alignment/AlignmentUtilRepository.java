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

package de.d3web.we.alignment;

import java.util.HashMap;
import java.util.Map;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.terminology.global.DefaultGlobalTerminologyHandler;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.global.GlobalTerminologyHandler;
import de.d3web.we.terminology.local.D3webIDObjectTerminologyHandler;
import de.d3web.we.terminology.local.D3webNamedObjectTerminologyHandler;
import de.d3web.we.terminology.term.D3webTermFactory;
import de.d3web.we.terminology.term.TermFactory;
import de.d3web.we.terminology.term.TerminologyHandler;

public class AlignmentUtilRepository {

	private static AlignmentUtilRepository instance = new AlignmentUtilRepository();

	private AlignmentUtilRepository() {
		super();
		localTerminologyHandler = new HashMap<Class, TerminologyHandler>();
		globalTerminologyHandler = new HashMap<Class, GlobalTerminologyHandler>();
		termFactory = new HashMap<Class, TermFactory>();
		initialize();
	}

	private void initialize() {
		localTerminologyHandler.put(NamedObject.class, new D3webNamedObjectTerminologyHandler());
		localTerminologyHandler.put(IDObject.class, new D3webIDObjectTerminologyHandler());

		globalTerminologyHandler.put(GlobalTerminology.class, new DefaultGlobalTerminologyHandler());

		termFactory.put(NamedObject.class, new D3webTermFactory());
	}

	public static AlignmentUtilRepository getInstance() {
		return instance;
	}

	private Map<Class, TerminologyHandler> localTerminologyHandler;
	private Map<Class, GlobalTerminologyHandler> globalTerminologyHandler;
	private Map<Class, TermFactory> termFactory;

	public TerminologyHandler getLocalTerminogyHandler(Object terminology) {
		TerminologyHandler result = getLocalTerminogyHandler(terminology.getClass());
		result.setTerminology(terminology);
		return result;
	}

	public TerminologyHandler getLocalTerminogyHandler(Class context) {
		TerminologyHandler result = localTerminologyHandler.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : localTerminologyHandler.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = localTerminologyHandler.get(key);
					bestContext = key;
				}
			}
		}
		return result.newInstance();
	}

	public GlobalTerminologyHandler getGlobalTerminogyHandler(Class context) {
		GlobalTerminologyHandler result = globalTerminologyHandler.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : globalTerminologyHandler.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = globalTerminologyHandler.get(key);
					bestContext = key;
				}
			}
		}
		return (GlobalTerminologyHandler) result.newInstance();
	}

	public TermFactory getTermFactory(Class<? extends Object> context) {
		TermFactory result = termFactory.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : termFactory.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = termFactory.get(key);
					bestContext = key;
				}
			}
		}
		return result;
	}

}
