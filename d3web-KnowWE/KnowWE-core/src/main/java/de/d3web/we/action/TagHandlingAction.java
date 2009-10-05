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

package de.d3web.we.action;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.TaggingMangler;

public class TagHandlingAction implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String web = parameterMap.getWeb();		
		String topic = parameterMap.getTopic();
		String tagaction = parameterMap.get(KnowWEAttributes.TAGGING_ACTION);
		String tag=parameterMap.get(KnowWEAttributes.TAGGING_TAG);				
		
		TaggingMangler tm=TaggingMangler.getInstance();
		if (tagaction.equals("add")){
			tm.addTag(topic, tag,parameterMap);
		} else if (tagaction.equals("del")){
			tm.removeTag(topic, tag,parameterMap);
		} else if (tagaction.equals("set")){
			tm.setTags(topic,tag,parameterMap);
		} else if (tagaction.equals("pagesearch")){
			String query=parameterMap.get(KnowWEAttributes.TAGGING_QUERY);
			return TaggingMangler.getInstance().getResultPanel(query);
		}
		
		
		return tag;
	}

}
