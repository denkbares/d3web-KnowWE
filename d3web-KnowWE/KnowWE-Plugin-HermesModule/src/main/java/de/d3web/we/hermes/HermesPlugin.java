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

package de.d3web.we.hermes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.hermes.action.SearchTimeEventsAction;
import de.d3web.we.hermes.kdom.TimeEventType;
import de.d3web.we.hermes.kdom.renderer.TimeLineHandler;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;
import de.d3web.we.module.PageAppendHandler;
import de.d3web.we.taghandler.TagHandler;

public class HermesPlugin extends AbstractDefaultKnowWEModule {

    private static HermesPlugin instance;

    public static HermesPlugin getInstance() {
	if (instance == null) {
	    instance = new HermesPlugin();
	}
	return instance;
    }

    /**
     * @see de.d3web.we.module.AbstractDefaultKnowWEModule#getRootTypes() The
     *      Type 'DemoSectionType' is registered to the KnowWE-type system. This
     *      method is called once at initialization of KnowWE(-Modules)
     */
    @Override
    public List<KnowWEObjectType> getRootTypes() {
	List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
	rootTypes.add(new TimeEventType());
	return rootTypes;
    }
    
	@Override
	public List<TagHandler> getTagHandlers() {
		List<TagHandler> list = new ArrayList<TagHandler>();
		list.add(new TimeLineHandler());
		list.add(new TimeEventSearchHandler());
		return list;
	} 
	
	@Override
	public List<PageAppendHandler> getPageAppendHandlers() {
		List<PageAppendHandler> handlers = new ArrayList<PageAppendHandler>();
		handlers.add(new AppendTagEditHandler());
		return handlers;
	}
	
	
	@Override
	public void addAction(
			Map<Class<? extends KnowWEAction>, KnowWEAction> map) {
		map.put(SearchTimeEventsAction.class, new SearchTimeEventsAction());
	}
}