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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.refactoring.session.RefactoringSession;
/**
 * @author Franz Schwab
 */
public class RefactoringAction extends AbstractKnowWEAction {
	
	private Map<HttpSession, RefactoringSession> sessions = new HashMap<HttpSession, RefactoringSession>();
	
	@Override
	public String perform(final KnowWEParameterMap parameters) {
		//TODO Parameter immer mitteilen nicht vergessen
		HttpSession session = parameters.getSession();
		RefactoringSession rs = sessions.get(session);
		// der Nutzer hatte noch keine RefactoringSession oder die vorherige ist bereits beendet
		if (rs == null || rs.isTerminated()) {
			rs = new RefactoringSession();
			rs.set(parameters);
			sessions.put(session, rs);
			rs.getThread().start();
		// die vorhandene RefactoringSession wird wieder angeworfen
		} else {
			rs.set(parameters);
			rs.getLock().lock();
			rs.getRunScript().signal();
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