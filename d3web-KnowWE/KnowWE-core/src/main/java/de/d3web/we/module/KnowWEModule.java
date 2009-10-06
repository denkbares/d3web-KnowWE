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

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.TerminalType;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.taghandler.TagHandler;

/**
 * @author Jochen
 *
 * A KnowWEMdule is a (heavy-weight) functional unit that can be added to KnowWE as 
 * a plugin. To add a module it to KnowWE this interface needs to be implemented (or 
 * one of its abstract implementations). The module class needs to be registered
 * in the modules.txt file to allow KnowWE to initialize the module on system startup.
 * (Of course the necessary class files need to be made available to class-loader by 
 * a suitable build-process.) All the methods of this interface are called on initialization
 * of KnowWE. 
 * All the methods are OPTIONAL, thus can be implemented empty. 
 *   
 * 
 * 
 */
public interface KnowWEModule {

	/**
	 * Is called once on initialization of the KnowWEEnvironment
	 * 
	 * @param loader Enables loading of wiki articles for settings etc.
	 */
	void initModule(ServletContext context);
	

	/**
	 * Is called once per page on save
	 */
	void onSave(String topic) ;	
	
	
	
	/**
	 * Allows to register actions. Actions can be executed from the web.
	 * Usually be clicking on pregenerated links on the wiki pages.
	 * 
	 * COMMENT: registering is not strictly necessary: Because if an action 
	 * request comes in, the class for this action is loaded for name if having correct package and found in classpath. 
	 * An action instance is created and executed.
	 * 
	 * 
	 * @param map
	 */
	void addAction(Map<Class<? extends KnowWEAction>, KnowWEAction> map);
	
	
	
	/**
	 * If wiki content should be translated into a custom knowledge representation (beyond OWL, e.g., d3web) - 
	 * this  can be done by introducing an KnowledgeRepresentationHandler to the KnowledgeRepresentationManager.
	 * It is passed to all lookForSections() and reviseSubtree() calls.
	 * 
	 * 
	 * @param mgr
	 */
	void registerKnowledgeRepresentationHandler(KnowledgeRepresentationManager mgr);

	
	/**
	 * Here a module can register its types to the KDOM-type-tree. The types given here
	 * are automatically parsed to KDOMs in every article.
	 * This is THE (only) method to introduce new markup to KnowWE.
	 * 
	 * The given types are expected to have their children types already attached if 
	 * any. These are also processed automatically by the KDOM parsing engine.
	 * 
	 * @return
	 */
	List<KnowWEObjectType> getRootTypes();
	
	/**
	 * Here global types can be added to the system. Global types are always active
	 * at any level of the KDOM parsing process. Global types need to be TerminalTypes, thus
	 * cannot have children in the parse-tree.
	 * 
	 * DANGER: This can invade the markups/parsing of other modules!
	 * 
	 * @return
	 */
	List<TerminalType> getGlobalTypes();


//	void findTypeInstances(Class clazz,
//			List<KnowWEObjectType> instances);


	/**
	 * A KnowWE module can bring in tagHandlers and register them by this method.
	 * 
	 * COMMENT: Alternatively, those taghandlers can also be introduced separately using the taghandler.text file.
	 * There the class of the taghandler is listed and will be loaded on KnowWE initialization.
	 * 
	 * @return
	 */
	List<TagHandler> getTagHandlers();
	
	
	/**
	 * A KnowWE module can bring in PageAppendHandlers and register them by this method.
	 * 
	 * These handlers allow a module to append some content to the wiki-page content
	 * There are 2 kinds of appendHandlers one append content at top of the page,
	 * the other appends at the bottom
	 * 
	 * @return
	 */
	List<PageAppendHandler> getPageAppendHandlers();

}
