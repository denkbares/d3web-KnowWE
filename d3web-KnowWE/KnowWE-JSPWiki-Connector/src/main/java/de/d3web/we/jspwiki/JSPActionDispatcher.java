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

package de.d3web.we.jspwiki;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEFacade;
import de.d3web.we.core.KnowWEParameterMap;

public class JSPActionDispatcher extends KnowWEActionDispatcher {
	
	private KnowWEFacade fac;
	
	public JSPActionDispatcher ()  {
		fac = KnowWEFacade.getInstance();
	}
		 
	@Override
	public String performAction(KnowWEParameterMap parameterMap) {
		String action = parameterMap.get("action");
		String renderer =  parameterMap.get("renderer");
		parameterMap.put("env", "JSPWiki");
		parameterMap.put("KWikiWeb", "default_web");
		
		// TODO Search that class ;)
		if(renderer != null && renderer.equals("TirexToXCLRenderer")) {			
			return fac.tirexToXCL(parameterMap);
		}
		
		// TODO has special functions in facade. Do not delete!
		if(renderer != null && renderer.equals("ParseWebOffline")) {
			//parameterMap.put("dataFolder","/var/lib/kjspwiki");
			return fac.parseAll(parameterMap);
		}
		
		if(action != null) {
			return fac.performAction(parameterMap);
		}		
		
		return "no action found";
	}

}

