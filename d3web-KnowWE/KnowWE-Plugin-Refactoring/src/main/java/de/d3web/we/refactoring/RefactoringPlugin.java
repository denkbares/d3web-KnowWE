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

package de.d3web.we.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;
import de.d3web.we.refactoring.action.RefactoringAction;
import de.d3web.we.refactoring.action.ShowRefactoringAction;
import de.d3web.we.taghandler.TagHandler;



/**
 * A demo plugin for d3web for use without the usual Maven workspace
 * 
 * Little build howto:
 *  - Create a new run configuration for a maven build:
 *    right click on the project -> run as -> maven build...
 *    
 *  - In the new window, set the following settings:
 *    - Goals: "clean package"
 *    
 *    - Parameter: "knowwe.path" to the path where KnowWE is
 *      The path is relative to this project, for instance: "../d3web-KnowWE/KnowWE/"
 *      
 *    - Parameter: "knowwe.version" to the KnowWE version that is used in your workspace
 *      Currently, that is "0-SNAPSHOT"
 * 
 *  - Run the normal KnowWE build
 *  
 *  - Then run the build for this plugin.
 *  
 *  Remember: You have to rebuild this plugin after rebuilding KnowWE (it cleans everything)
 *  
 * @author Franz Schwab
 */
public class RefactoringPlugin extends AbstractDefaultKnowWEModule {
	
	
	@Override
	public void addAction(Map<Class<? extends KnowWEAction>, KnowWEAction> map) {
		// TODO Auto-generated method stub
		map.put(RefactoringAction.class, new RefactoringAction());
		map.put(ShowRefactoringAction.class, new ShowRefactoringAction());
	}


	/**
	 * Singleton instance
	 */
	private static RefactoringPlugin instance;
	
	
	/**
	 * Singleton is necessary for ALL KnowWE-Modules,
	 * because on wiki startup getInstance() is called by reflections
	 * for instanciation of the module. 
	 * 
	 * @return
	 */
	public static RefactoringPlugin getInstance() {
		if (instance == null)
			instance = new RefactoringPlugin();
		
		return instance;
	}
	
	@Override
	public List<TagHandler> getTagHandlers() {
		List<TagHandler> list = new ArrayList<TagHandler>();
		list.add(new RefactoringTagHandler());
		return list;
	}

	
	/* (non-Javadoc)
	 * @see de.d3web.we.module.AbstractDefaultKnowWEModule#getRootTypes()
	 * 
	 * A module can add (any number of) root types to the system
	 * 
	 */
	@Override
	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> helloWorldrootTypes = new ArrayList<KnowWEObjectType>();
		helloWorldrootTypes.add(new Refactoring());
		return helloWorldrootTypes;
	} 
	

}
