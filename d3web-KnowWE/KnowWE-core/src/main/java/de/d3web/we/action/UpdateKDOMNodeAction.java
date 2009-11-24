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

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.utils.KnowWEUtils;

/**
 * <p>UpdateTableKDOMNodes class.</p>
 * 
 * 
 * @author smark
 * @see KnowWEAction
 */
public class UpdateKDOMNodeAction extends AbstractKnowWEAction {
		
	@Override
	public String perform(KnowWEParameterMap parameterMap) 
	{
		String web = parameterMap.getWeb();
		String text = parameterMap.get(KnowWEAttributes.TARGET);
		String name = parameterMap.getTopic();
		String id = parameterMap.get(KnowWEAttributes.SECTION_ID);
		
		String newSourceText = "";
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);

		if( text != "" )
		{
			if( id != null ) 
			{
				newSourceText = mgr.replaceKDOMNodeWithoutSave(parameterMap, name, id, text);
				KnowWEEnvironment.getInstance().saveArticle(web, name, newSourceText, parameterMap);
			}
		}
		return "done";
	}
}
