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

package de.d3web.we.taghandler;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SolutionStateViewHandler extends AbstractTagHandler {
	
	
	public SolutionStateViewHandler() {
		super("solutionStates");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.Solutions.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		
		return "<div id='sstate-panel' class='panel'><h3>" + rb.getString("KnowWE.Solutions.name") + "</h3><div>" 
			    + "<p>"
			    + "<span id='sstate-update' class='pointer small'>" + rb.getString("KnowWE.Solutions.update") + "</span> - "
			    + "<span id='sstate-clear' class='pointer small'>" + rb.getString("KnowWE.Solutions.clear") + "</span> - "
			    + "<span id='sstate-findings' class='pointer small'>" + rb.getString("KnowWE.Solutions.findings") + "</span>"		    
			    + "</p>"
			    + "<div id='sstate-result'></div>"
			    + "</div></div>";
	}
}
