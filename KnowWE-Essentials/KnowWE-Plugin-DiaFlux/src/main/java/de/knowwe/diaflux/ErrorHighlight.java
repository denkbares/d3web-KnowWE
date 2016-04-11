/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux;

import de.knowwe.core.user.UserContext;


/**
 * 
 * @author Reinhard Hatko
 * @created 14.01.2013
 */
public class ErrorHighlight implements DiaFluxDisplayEnhancement {

	public static final String[] SCRIPTS = new String[] { "cc/flow/error.js" };
	public static final String[] CSSS = new String[] { "cc/flow/error.css" };


	@Override
	public boolean activate(UserContext user, String scope) {
		if (DiaFluxTraceHighlight.checkForHighlight(user, DiaFluxTraceHighlight.NO_HIGHLIGHT)) return true;
		else return user.getRequest().getSession().getAttribute(DiaFluxTraceHighlight.HIGHLIGHT_KEY) == null;

	}

	@Override
	public String[] getScripts() {
		return SCRIPTS;
	}

	@Override
	public String[] getStylesheets() {
		return CSSS;
	}

}
