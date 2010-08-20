/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.hermes;

import java.util.HashMap;

import de.d3web.we.core.semantic.TagEditPanel;
import de.d3web.we.kdom.rendering.PageAppendHandler;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AppendTagEditHandler implements PageAppendHandler {

	private final TagHandler tagHandler = new TagEditPanel();

	@Override
	public String getDataToAppend(String topic, String web, KnowWEUserContext user) {
		return "\\\\[{If group='Editoren'\n\n"
				+ tagHandler.render(topic, user, new HashMap<String, String>(), web) + "}]";
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
