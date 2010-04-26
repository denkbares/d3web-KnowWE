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

package de.d3web.we.ci4ke;

import java.util.Map;

import com.ecyrd.jspwiki.PageManager;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;

import de.d3web.we.ci4ke.handling.CIDashboard;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;


/**
 * @author Marc-Oliver Ochlast
 */
public class CI4KETagHandler extends AbstractTagHandler {

	public CI4KETagHandler() {
		super("ci4ke");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		
		CIDashboard board = new CIDashboard(values,topic);
		return board.render();
//		WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
//		WikiContext context = we.createContext(user.getHttpRequest(), WikiContext.VIEW);
//		return we.getPageManager().getProvider().getClass().getName();
//		return we.getWikiProperties().getProperty(PageManager.PROP_PAGEPROVIDER);
//		return "BLA";
	}
}
