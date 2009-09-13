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
 * All modules are called in the order, that is defined in the initModules() method
 * in KnowWEEnvironment initialization. The article source text is cut-up in disjoint sections by 
 * the SectionFinder of the Modules. The SectionFinder will only receive text parts, that had not been 
 * allocated by a SectionFinder of another Module that is registered earlier (higher priority).
 * 
 * WORKFLOW:
 * Init of KnowWEEnvironment calls:
 * <ol>
 * <li>init(KnowWETopicLoader loader);</li>
 * </ol>
 * 
 * Saving of a Wiki article:
 * <ol>
 * <li>preCacheModifications(String text);</li> 
 * <li>insertKnowledge(String topic, String web, String text, KnowledgeBaseManagement kbm);</li>
 * </ol>
 * 
 * Rendering/View of an article:
 * (If no preCacheModifications is found in the system, this is headed by preCacheModifications
 * and insertKnowledge)
 * <ol>
 * <li>renderPreTranslate(String output, String topic);</li>
 * <li>renderPostTranslateToHTML(String output, String topic, String user, String id);</li>
 * </ol>
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
	
	List<TerminalType> getGlobalTypes();


	void findTypeInstances(Class clazz,
			List<KnowWEObjectType> instances);


	/**
	 * A KnowWE module can carry tagHandlers with it and register them by this method.
	 * 
	 * COMMENT: Alternatively, those taghandlers can also be introduced separately using the taghandler.text file.
	 * There the class of the taghandler is listed and will be loaded on KnowWE initialization.
	 * 
	 * @return
	 */
	List<TagHandler> getTagHandlers();

}
