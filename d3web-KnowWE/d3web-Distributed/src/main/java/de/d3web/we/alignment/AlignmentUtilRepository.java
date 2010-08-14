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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.utilities.SetMap;
import de.d3web.we.alignment.aligner.CompleteGlobalAligner;
import de.d3web.we.alignment.aligner.GlobalAligner;
import de.d3web.we.alignment.aligner.LocalAligner;
import de.d3web.we.alignment.method.AlignMethod;
import de.d3web.we.alignment.method.StringAlignMethod;
import de.d3web.we.terminology.global.DefaultGlobalTerminologyHandler;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.global.GlobalTerminologyHandler;
import de.d3web.we.terminology.local.D3webIDObjectTerminologyHandler;
import de.d3web.we.terminology.local.D3webNamedObjectTerminologyHandler;
import de.d3web.we.terminology.local.LocalTerminologyHandler;
import de.d3web.we.terminology.term.D3webTermFactory;
import de.d3web.we.terminology.term.D3webTermUpdater;
import de.d3web.we.terminology.term.TermFactory;
import de.d3web.we.terminology.term.TermUpdater;

public class AlignmentUtilRepository {

	private static AlignmentUtilRepository instance = new AlignmentUtilRepository();

	private AlignmentUtilRepository() {
		super();
		globalAligners = new SetMap<Class, GlobalAligner>();
		localAligners = new SetMap<Class, LocalAligner>();
		methods = new SetMap<Class, AlignMethod>();
		localTerminologyHandler = new HashMap<Class, LocalTerminologyHandler>();
		globalTerminologyHandler = new HashMap<Class, GlobalTerminologyHandler>();
		termUpdater = new HashMap<Class, TermUpdater>();
		termFactory = new HashMap<Class, TermFactory>();

		initialize();
	}

	private void initialize() {
		globalAligners.add(NamedObject.class, new CompleteGlobalAligner());

		// localAligners.add(NamedObject.class, new D3webLocalAligner());

		methods.add(String.class, new StringAlignMethod());

		localTerminologyHandler.put(NamedObject.class, new D3webNamedObjectTerminologyHandler());
		localTerminologyHandler.put(IDObject.class, new D3webIDObjectTerminologyHandler());

		globalTerminologyHandler.put(GlobalTerminology.class, new DefaultGlobalTerminologyHandler());

		termUpdater.put(NamedObject.class, new D3webTermUpdater());

		termFactory.put(NamedObject.class, new D3webTermFactory());
	}

	public static AlignmentUtilRepository getInstance() {
		return instance;
	}

	private SetMap<Class, GlobalAligner> globalAligners;
	private SetMap<Class, LocalAligner> localAligners;
	private SetMap<Class, AlignMethod> methods;
	private Map<Class, LocalTerminologyHandler> localTerminologyHandler;
	private Map<Class, GlobalTerminologyHandler> globalTerminologyHandler;
	private Map<Class, TermUpdater> termUpdater;
	private Map<Class, TermFactory> termFactory;

	public Collection<GlobalAligner> getGlobalAligners(Class context) {
		Set<GlobalAligner> result = globalAligners.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : globalAligners.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = globalAligners.get(key);
					bestContext = key;
				}
			}
		}
		return result;
	}

	public Collection<GlobalAligner> getGlobalAligners() {
		return globalAligners.getAllValues();
	}

	public Collection<LocalAligner> getLocalAligners(Class context) {
		Set<LocalAligner> result = localAligners.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : localAligners.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = localAligners.get(key);
					bestContext = key;
				}
			}
		}
		return result;
	}

	public Collection<LocalAligner> getLocalAligners() {
		return localAligners.getAllValues();
	}

	public Collection<AlignMethod> getMethods(Class context) {
		return methods.get(context);
	}

	public Collection<AlignMethod> getMethods() {
		return methods.getAllValues();
	}

	public LocalTerminologyHandler getLocalTerminogyHandler(Object terminology) {
		LocalTerminologyHandler result = getLocalTerminogyHandler(terminology.getClass());
		result.setTerminology(terminology);
		return result;
	}

	public LocalTerminologyHandler getLocalTerminogyHandler(Class context) {
		LocalTerminologyHandler result = localTerminologyHandler.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : localTerminologyHandler.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = localTerminologyHandler.get(key);
					bestContext = key;
				}
			}
		}
		return (LocalTerminologyHandler) result.newInstance();
	}

	public GlobalTerminologyHandler getGlobalTerminogyHandler(GlobalTerminology terminology) {
		GlobalTerminologyHandler result = getGlobalTerminogyHandler(terminology.getClass());
		result.setTerminology(terminology);
		return result;
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

	public TermUpdater getTermUpdater(Class<? extends Object> context) {
		TermUpdater result = termUpdater.get(context);
		Class bestContext = Object.class;
		if (result == null) {
			for (Class key : termUpdater.keySet()) {
				if (key.isAssignableFrom(context) && bestContext.isAssignableFrom(key)) {
					result = termUpdater.get(key);
					bestContext = key;
				}
			}
		}
		return result;
	}

}
