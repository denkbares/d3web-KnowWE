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

package de.d3web.we.action;

import java.util.ResourceBundle;

import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.DPSEnvironmentManager;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;

public class ReInitDPSEnvironmentAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(parameterMap.getRequest());

		long time1 = System.currentTimeMillis();
		String web = parameterMap.get(KnowWEAttributes.WEB);
		DPSEnvironment dpse = DPSEnvironmentManager.getInstance().getEnvironments(web);
		dpse.reInitialize();
		// ((GlobalTerminologyRenderer)
		// model.getWebApp().getRenderer("KWiki_globalTerminology")).reInitialize(model,
		// web);

		long time2 = System.currentTimeMillis();
		long diff = time2 - time1;

		StringBuffer html = new StringBuffer();

		html.append("<p class=\"box info\">");
		html.append("<a href=\"#\" id='js-reInit' class='clear-element'>"
				+ rb.getString("KnowWE.buttons.close") + "</a><br />");
		html.append(rb.getString("dpsenv.status") + "<br />");
		html.append(rb.getString("dpsenv.duration") + (((float) diff) / 1000)
				+ rb.getString("dpsenv.seconds") + " <br />");
		html.append("</p>");
		return html.toString();
	}
}