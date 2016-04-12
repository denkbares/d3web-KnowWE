/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
 * @created 23.10.2012
 */
public class DiaFluxDiffDisplay implements DiaFluxDisplayEnhancement {

	public static String[] SCRIPTS = new String[] { "cc/diff/diafluxdiff.js" };
	public static String[] CSSS = new String[] { "cc/diff/diafluxdiff.css" };

	public static final String DIFF_HIGHLIGHT = "diff";
	public static final String SCOPE = "diafluxdiff";

	@Override
	public boolean activate(UserContext user, String scope) {
		// TODO jspwiki independent way
		boolean contains = user.getRequest().getServletPath().contains("Diff.jsp");
		return contains;
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
