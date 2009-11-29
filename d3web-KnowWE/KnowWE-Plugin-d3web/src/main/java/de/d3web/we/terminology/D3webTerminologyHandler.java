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


package de.d3web.we.terminology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

/**
 * D3webTerminologyHandler.
 * Handles Knowledge and its recycling.
 * 
 * @author astriffler
 */
public class D3webTerminologyHandler extends KnowledgeRepresentationHandler {
	
	/**
	 * Map for all articles an their KBMs.
	 */
	private Map<String, KnowledgeBaseManagement> kbms = new HashMap<String, KnowledgeBaseManagement>();
	
	/**
	 * Map of the last versions of each KBM.
	 */
	private Map<String, KnowledgeBaseManagement> lastKbms  = new HashMap<String, KnowledgeBaseManagement>();
	
	/**
	 * Stores flag, if an article currently is building a new KnowledgeBase.
	 */
	private Map<String, Boolean> usingNewKBM = new HashMap<String, Boolean>();
	
	/**
	 * Stores flag, if an article currently is modifying and old KnowledgeBase 
	 * instead of building a new one.
	 */
	private Map<String, Boolean> usingOldKBM = new HashMap<String, Boolean>();
	
	/**
	 * Store whether the KnowledgeBase already got cleaned from Knowledge of
	 * a specific ObjectType.
	 */
	private Map<String, HashSet<Class<? extends KnowWEObjectType>>> cleanedTypes 
			= new HashMap<String, HashSet<Class<? extends KnowWEObjectType>>>();
	
	/**
	 * Stores flag, if the knowledge of an article is build completely.
	 */
	private Map<String, Boolean> finishedKBM = new HashMap<String, Boolean>();
	
	/**
	 * Stores the number of knowledge containing Sections in an article
	 */
	private Map<String, Integer> knowledgeSectionsCount = new HashMap<String, Integer>();
	
	/**
	 * Stores the number of knowledge containing Sections in the last version of an article
	 */
	private Map<String, Integer> lastKnowledgeSectionsCount	= new HashMap<String, Integer>();
	
	public Map<String, HashSet<Class<? extends KnowWEObjectType>>> getCleanedTypes() {
		return this.cleanedTypes;
	}
	
	/**
	 * @param article is the article you need the KBM from
	 * @param s is the knowledge containing section you need the KBM for 
	 * @returns the KBM or <tt>null</tt> for article <tt>article</tt>. <tt>null</tt> 
	 * is returned if the Knowledge of the given section doesn't need to get rebuild.
	 */
	public KnowledgeBaseManagement getKBM(KnowWEArticle article, Section s) {
		if (buildKnowledge(article, s)) {
			//System.out.println("Got KBM for " + s.getObjectType().getName());
			return kbms.get(article.getTitle());
		} else {
			return null;
		}
	}

	/**
	 * Initializes modules, kbms, services, flags...
	 */
	@Override
	public void initArticle(KnowWEArticle art) {
		DPSEnvironment env = D3webModule.getDPSE("default_web");
		String id = art.getTitle() + ".." + KnowWEEnvironment.generateDefaultID(art.getTitle());
		KnowledgeService service = env.getService(id);
		if (service != null) {
			env.removeService(service);
			for (Broker broker : env.getBrokers()) {
				broker.signoff(service);
			}
		}
		KnowledgeBaseManagement lastKBM = kbms.remove(art.getTitle());
		if (lastKBM != null) {
			lastKbms.put(art.getTitle(), lastKBM);
		}
		Integer lastCount = knowledgeSectionsCount.remove(art.getTitle());
		if (lastCount != null) {
			lastKnowledgeSectionsCount.put(art.getTitle(), lastCount);
		}
		usingNewKBM.put(art.getTitle(), false);
		usingOldKBM.put(art.getTitle(), false);
		finishedKBM.put(art.getTitle(), false);
		cleanedTypes.put(art.getTitle(), new HashSet<Class<? extends KnowWEObjectType>>());
		knowledgeSectionsCount.put(art.getTitle(), 0);
		kbms.put(art.getTitle(), KnowledgeBaseManagement.createInstance());
	}
	
	/**
	 * Registers complete KnowledgeBase...
	 */
	@Override
	public void finishArticle(KnowWEArticle art) {
			KnowledgeBaseManagement kbm = this.getKBM(art, art.getSection());
			if(!isEmpty(kbm)) {
				DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, 
						art.getTitle(), "default_web");
			}
			finishedKBM.put(art.getTitle(), true);
	}
	
	/**
	 * Builds or reuses and cleans Knowledge for the given article and Section 
	 * depending on the nature of changes in the article.
	 * 
	 * @returns <b>true</b> if the Knowledge of the Section needs to
	 * 	get parsed to the KnowledgeBase or  </p>
	 * <b>false</b> if the Knowledge is already in the KnowledgeBase
	 */
	@Override
	public boolean buildKnowledge(KnowWEArticle article, Section s) {
		String title = article.getTitle();
		
		if (finishedKBM.get(title)) {
			return true;
		}
		
		if (!(s.getObjectType() instanceof KnowledgeRecyclingObjectType)) {
			knowledgeSectionsCount.put(title, knowledgeSectionsCount.get(title) + 1);
		}
		
		if (usingNewKBM.get(title)) {
			return true;
		}
		
		KnowledgeBaseManagement lastKbm = lastKbms.get(title);
		
		if (s.getObjectType() instanceof KnowWEArticle 
				|| !s.isReusedBy(title)) {
			
			if (usingOldKBM.get(title)) {
				if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
					if (!cleanedTypes.get(title).contains(s.getObjectType().getClass())) {
						((KnowledgeRecyclingObjectType) s.getObjectType()).cleanKnowledge(article, s, kbms.get(title));
						cleanedTypes.get(title).add(s.getObjectType().getClass());
					}
				} else if (!(s.getObjectType() instanceof KnowWEArticle)) {
					// KnowledgeRecyclingObjectTypes should be the last ObjectTypes to parse...
					Logger.getLogger(this.getClass().getName())
						.log(Level.WARNING, "Wrong order of parsing for ObjectType '" +
								s.getObjectType() + "'!");
				} else if (lastKnowledgeSectionsCount.get(title) != knowledgeSectionsCount.get(title)) {
					kbms.put(title, KnowledgeBaseManagement.createInstance());
					useNewKBM(article, s);
				}
				return true;
			}
			
			if (lastKbm != null && isEmpty(kbms.get(title))
					&& (s.getObjectType() instanceof KnowledgeRecyclingObjectType
							|| (s.getObjectType() instanceof KnowWEArticle 
								&& lastKnowledgeSectionsCount.get(title) 
									== knowledgeSectionsCount.get(title)))) {
				lastKbms.remove(title);
				usingOldKBM.put(title, true);
				kbms.put(title, lastKbm);
				if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
					((KnowledgeRecyclingObjectType) s.getObjectType()).cleanKnowledge(article, s, lastKbm);
					cleanedTypes.get(title).add(s.getObjectType().getClass());
				}
				return true;
			} else {
				useNewKBM(article, s);
				return true;
			}
		}
		return false;
	}
	
	private void useNewKBM(KnowWEArticle article, Section s) {
		usingNewKBM.put(article.getTitle(), true);
		List<Section> sectionsToRevise = article.getAllNodesParsingPostOrder();
		List<Section> strSub = sectionsToRevise.subList(0, sectionsToRevise.indexOf(s));
		for (Section sec:strSub) {
			sec.getObjectType().reviseSubtree(article, sec);
		}
	}

	private boolean isEmpty(KnowledgeBaseManagement kbm) {
		if (kbm.getKnowledgeBase().getAllKnowledgeSlices().size() == 0
				&& kbm.getKnowledgeBase().getQuestions().size() <= 1
				&& kbm.getKnowledgeBase().getDiagnoses().size() <= 1) {
			return true;
		} else {
			return false;
		}
		
	}
	
}
