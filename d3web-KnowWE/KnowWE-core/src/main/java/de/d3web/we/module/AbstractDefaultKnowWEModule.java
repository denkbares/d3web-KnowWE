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

package de.d3web.we.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.TerminalType;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.taghandler.TagHandler;

public abstract class AbstractDefaultKnowWEModule implements KnowWEModule{
		
	@Override
	public void addAction(
			Map<Class<? extends KnowWEAction>, KnowWEAction> map) {
		
	}

//	@Override
//	public void findTypeInstances(Class clazz, List<KnowWEObjectType> instances) {
//		
//	}
	

	@Override
	public abstract List<KnowWEObjectType> getRootTypes();
	
	@Override
	public List<TerminalType> getGlobalTypes() {
		return null;
	}

	@Override
	public void initModule(ServletContext context) {
		
	}
	
	@Override
	public List<TagHandler> getTagHandlers() {
		return new ArrayList<TagHandler>();
	} 
	@Override
	public List<PageAppendHandler> getPageAppendHandlers() {
		return new ArrayList<PageAppendHandler>();
	}
	
	@Override
	public void onSave(String topic) {
		
	}

	@Override
	public void registerKnowledgeRepresentationHandler(KnowledgeRepresentationManager mgr) {
		
	}


}
