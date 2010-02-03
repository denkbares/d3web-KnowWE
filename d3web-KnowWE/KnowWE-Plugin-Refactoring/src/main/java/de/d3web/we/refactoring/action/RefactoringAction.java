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

package de.d3web.we.refactoring.action;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.refactoring.session.RefactoringSession;
/**
 * @author Franz Schwab
 */
public class RefactoringAction extends AbstractKnowWEAction {
	
	private Map<String, RefactoringSession> sessions = new HashMap<String, RefactoringSession>();
	
	@Override
	public String perform(final KnowWEParameterMap parameters) {
		// rs.set(parameters) immer aufrufen nicht vergessen
		String user = parameters.getUser();
		RefactoringSession rs = sessions.get(user);
		Gson gson = new Gson();
		Type mapType = new TypeToken<Map<String,String[]>>(){}.getType();
		Map<String,String[]> gsonFormMap = gson.fromJson(parameters.get("jsonFormMap"),mapType);
		// der Nutzer hatte noch keine RefactoringSession oder die vorherige ist bereits beendet
		// oder die vorherige wurde abgebrochen und der Benutzer startet eine neue
		if (rs == null || rs.isTerminated() || gsonFormMap.containsKey("startNewRefactoringSession")) {
			rs = new RefactoringSession();
			rs.setParameters(parameters, gsonFormMap);
			sessions.put(user, rs);
			rs.getThread().start();
		// die vorhandene RefactoringSession wird wieder angeworfen
		} else {
			rs.setParameters(parameters, gsonFormMap);
			rs.getLock().lock();
			rs.getRunRefactoring().signal();
			rs.getLock().unlock();
		}

		// hier könnte parallel ausgeführter Code stehen
		
		// der Benutzerdialog wird angehalten während das Refactoring läuft
		rs.getLock().lock();
		try {
			rs.getRunDialog().await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			rs.getLock().unlock();
		}
		// wurde das Refactoring wieder pausiert, dann gib die nächste Action zurück
		return rs.getNextAction().perform(parameters);
	}




}