/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.d3web.homepage;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.jspwiki.DefaultLogoAction;

/**
 * Serves the d3web logo in the header of d3web.de instead of the KnowWE logo.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 14.10.2026
 */
public class D3webLogoAction extends DefaultLogoAction {

	private static final String LOGO_PATH = "/webapp/templates/default/skins/d3web/d3web-logo.png";

	@Override
	public String getDefaultLogoPath() {
		return LOGO_PATH;
	}

	@Override
	public String getDarkModeLogoPath() {
		return getDefaultLogoPath();
	}

	@Override
	public void setLogoContentType(UserActionContext context) {
		context.getResponse().setContentType("image/png");
	}

	@Override
	public void setCharsetEncoding(UserActionContext context) {
		context.getResponse().setCharacterEncoding(null);
	}
}
