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

package de.d3web.we.flow;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.RootType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;
import de.d3web.we.taghandler.TagHandler;


public class FlowchartPlugin extends AbstractDefaultKnowWEModule{

	private List<de.d3web.we.taghandler.TagHandler> tagHandlers = new ArrayList<de.d3web.we.taghandler.TagHandler>();

	@Override
	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(FlowchartType.getInstance());
		return types;
	}
	
	private static FlowchartPlugin instance;
	
	public static FlowchartPlugin getInstance() {
		if (instance == null) {
			instance = new FlowchartPlugin();
			RootType.getInstance().addReviseSubtreeHandler(new DelegateSubtreeHandler(new FlowchartSubTreeHandler(), FlowchartType.class));
		}

		return instance;
	}
	
	@Override
	public void initModule(ServletContext context) {
		this.addTagHandler(new FlowchartTagHandler());
		
		//TODO workaround for parsing flow terminology before flow procedures
		
		
		
	}

	protected void addTagHandler(de.d3web.we.taghandler.TagHandler handler) {
		this.tagHandlers.add(handler);
	}
	
	public List<TagHandler> getTagHandlers() {
		return this.tagHandlers;
	} 

}
