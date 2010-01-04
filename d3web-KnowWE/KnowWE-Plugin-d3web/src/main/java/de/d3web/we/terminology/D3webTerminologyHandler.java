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

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webPersistence;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

/**
 * D3webTerminologyHandler.
 * Handles Knowledge and its recycling.
 * 
 * @author astriffler
 */
public class D3webTerminologyHandler extends KnowledgeRepresentationHandler {
	
	private String web;
	
	/**
	 * Map for all articles an their KBMs.
	 */
	private static Map<String, KnowledgeBaseManagement> kbms = new HashMap<String, KnowledgeBaseManagement>();
	
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
	private Map<String, HashSet<Class<? extends KnowledgeRecyclingObjectType>>> typesToClean 
			= new HashMap<String, HashSet<Class<? extends KnowledgeRecyclingObjectType>>>();
	
	/**
	 * Store all KnowledgeRecyclingObjectTypes present in an article
	 */
	private Map<String, HashSet<Class<? extends KnowledgeRecyclingObjectType>>> recyclingTypes 
			= new HashMap<String, HashSet<Class<? extends KnowledgeRecyclingObjectType>>>();
	
	/**
	 * Stores flag, if the knowledge of an article is build completely.
	 */
	private Map<String, Boolean> finishedKBM = new HashMap<String, Boolean>();
	
	/**
	 * Stores the number of terminology containing Sections in an article
	 */
	private Map<String, Integer> terminologySectionsCount = new HashMap<String, Integer>();
	
	/**
	 * Stores the number of terminology containing Sections in the last version of an article
	 */
	private Map<String, Integer> lastTerminologySectionsCount	= new HashMap<String, Integer>();
	
	/**
	 * Stores for each KnowledgeRecyclingObjectType the number of Sections with it in an article
	 */
	private Map<String, Map<Class<? extends KnowledgeRecyclingObjectType>, Integer>> knowledgeSectionsCount 
			= new HashMap<String, Map<Class<? extends KnowledgeRecyclingObjectType>, Integer>>();
	
	/**
	 * Stores for each KnowledgeRecyclingObjectType the number of Sections with it in the last 
	 * version of an article
	 */
	private Map<String, Map<Class<? extends KnowledgeRecyclingObjectType>, Integer>> lastKnowledgeSectionsCount	
			= new HashMap<String, Map<Class<? extends KnowledgeRecyclingObjectType>, Integer>>();
	
	
	/**
	 * Stores for each Article if the jar file already got built
	 */
	private static Map<String, Boolean> savedToJar = new HashMap<String, Boolean>();
	
	/**
	 * <b>This constructor SHOULD NOT BE USED!</b><p/>
	 * Use D3webModule.getInstance().getKnowledgeRepresentationHandler(String web) instead!
	 */
	public D3webTerminologyHandler(String web) {
		this.web = web;
	}
	
	public Map<String, HashSet<Class<? extends KnowledgeRecyclingObjectType>>> getCleanedTypes() {
		return this.typesToClean;
	}
	
	/**
	 * @param article is the article you need the KBM from
	 * @param s is the knowledge containing section you need the KBM for 
	 * @returns the KBM or <tt>null</tt> for article <tt>article</tt>. <tt>null</tt> 
	 * is returned if the Knowledge of the given section doesn't need to be rebuild.
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
	 * This gets called when an new Article or a new version of an
	 * Article gets build.
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
		Integer lastTerCount = terminologySectionsCount.remove(art.getTitle());
		if (lastTerCount != null) {
			lastTerminologySectionsCount.put(art.getTitle(), lastTerCount);
		}
		Map<Class<? extends KnowledgeRecyclingObjectType>, Integer> lastKnowCountMap 
				= knowledgeSectionsCount.remove(art.getTitle());
		if (lastKnowCountMap != null) {
			lastKnowledgeSectionsCount.put(art.getTitle(), lastKnowCountMap);
		}
		usingNewKBM.put(art.getTitle(), false);
		usingOldKBM.put(art.getTitle(), false);
		finishedKBM.put(art.getTitle(), false);
		typesToClean.put(art.getTitle(), new HashSet<Class<? extends KnowledgeRecyclingObjectType>>());
		recyclingTypes.put(art.getTitle(), new HashSet<Class<? extends KnowledgeRecyclingObjectType>>());
		terminologySectionsCount.put(art.getTitle(), 0);
		knowledgeSectionsCount.put(art.getTitle(), new HashMap<Class<? extends KnowledgeRecyclingObjectType>, Integer>());
		kbms.put(art.getTitle(), KnowledgeBaseManagement.createInstance());
	}
	
	/**
	 * Registers complete KnowledgeBase...
	 * This gets called after revising all Sections of the Article
	 * through their ReviseSubTreeHandler.
	 */
	@Override
	public void finishArticle(KnowWEArticle art) {
			KnowledgeBaseManagement kbm = this.getKBM(art, art.getSection());
			if(!isEmpty(kbm)) {
				DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, 
						art.getTitle(), web);
			}
			finishedKBM.put(art.getTitle(), true);
	}
	
	/**
	 * This method gets called every time the KBM is requested through the 
	 * <tt>getKBM</tt> method and decides whether the KBM or <tt>null</tt> is 
	 * returned. In the same time (or while deciding that) it 
	 * builds or reuses and cleans Knowledge for the given article and Section 
	 * depending on the nature of changes in the article.<p/>
	 * 
	 * <b>Roughly it works like this:</b><p/>
	 * 
	 * As long as non of the Sections calling this method have changed
	 * (respectively got reused), <tt>false</tt> is returned and no flags get set.
	 * As soon as a changed Sections calls this, the method decides whether the old
	 * KnowledgeBase is reusable or if a complete rebuild of the KnowledgeBase
	 * is necessary. Then, on the one hand the needed actions are performed to the
	 * KnowledgeBase, on the other hand, to save this decision for the following 
	 * Sections calling this method, according flags are set.
	 * An additional review is performed when this method is called with the root
	 * Section of the Article through the <tt>finishArticle</tt> method which is
	 * called after all Sections of the Articles are revised. In this review the
	 * number of knowledge containing Sections in the article gets checked against
	 * the number of knowledge containing Sections in the last version of the
	 * article. If it differs it is again checked, depending on the nature of
	 * Sections that are missing, if the KnowledgeBase needs to be rebuild or if
	 * it is simply possible to erase the knowledge of the removed Section from
	 * the KnowledgeBase...
	 * 
	 * @param article is the Article calling this method. Since Sections can be
	 * included, this is not necessarily the article the Section belongs to directly
	 * @param s is the Section for which the KBM is called for.
	 * 
	 * @returns <b>true</b> if the Knowledge of the Section needs to
	 * 	get parsed to the KnowledgeBase or if parsing is completed</p>
	 * <b>false</b> if it is not yet decided if the Knowledge needs to get
	 * reparsed or if Knowledge is already in the reused old KnowledgeBase
	 */
	@Override
	public boolean buildKnowledge(KnowWEArticle article, Section s) {
		String title = article.getTitle();
		
		// if the finishedKBM flag is set, always return true, so the KBM gets
		// returned from the getKBM method
		// the flag gets set after revising all Sections respectively after
		// the knowledge is completely build or recycled, so
		// for example renderer get the KBM without further checks.
		if (finishedKBM.get(title)) {
			return true;
		}
		
		// counts knowledge containing Sections of this version of the article so it
		// can later be compared to the number in the last version.
		if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
			recyclingTypes.get(title).add(((KnowledgeRecyclingObjectType) s.getObjectType()).getClass());
			
			Map<Class<? extends KnowledgeRecyclingObjectType>, Integer> countMap = 
				knowledgeSectionsCount.get(title);
			if (!countMap.containsKey(s.getObjectType().getClass())) {
				countMap.put(((KnowledgeRecyclingObjectType) s.getObjectType()).getClass(), 0);
			}
			countMap.put(((KnowledgeRecyclingObjectType) s.getObjectType()).getClass(), 
					countMap.get(((KnowledgeRecyclingObjectType) s.getObjectType()).getClass()) + 1);
		} else {
			terminologySectionsCount.put(title, terminologySectionsCount.get(title) + 1);
		}
		
		// if this flag is set, all knowledge needs to be reparsed, so
		// return true every time.
		if (usingNewKBM.get(title)) {
			return true;
		}
		
		KnowledgeBaseManagement lastKbm = lastKbms.get(title);
		
		// if this Section has changed or is the root Section
		if (s.getObjectType() instanceof KnowWEArticle 
				|| !s.isReusedBy(title)) {
			
			// in case the useOldKBM flag is already set from a previous Section
			if (usingOldKBM.get(title)) {
				if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
					// add the ObjectType to the List of ObjectTypes whose knowledge need
					// to get cleaned in the KnowledgeBase (happens when root Sections calls
					// the buildKnowledge method), if the ObjectType implements the the
					// Interface KnowledgeRecyclingObjectType, indicating that this is even possible
					typesToClean.get(title).add(((KnowledgeRecyclingObjectType) s.getObjectType()).getClass());
				} else if (!(s.getObjectType() instanceof KnowWEArticle)) {
					// since the Sections with an ObjectTypes not implementing the interface
					// KnowledgeRecyclingObjectType should be parsed and calling this method
					// before the ones implementing that interface, this only happen if something
					// is wrong with that order.
					Logger.getLogger(this.getClass().getName())
						.log(Level.WARNING, "Wrong order of parsing for ObjectType '" +
								s.getObjectType() + "'!");
				} else if (lastTerminologySectionsCount.get(title) != terminologySectionsCount.get(title)) {
					// this is the case, when a terminology containing Section gets removed from the article
					// but only Sections with recyclable knowledge get changed. None the less a complete
					// rebuild is needed.
					kbms.put(title, KnowledgeBaseManagement.createInstance());
					useNewKBM(article, s);
				} else {
					// the Section must be the root Section (objectType instanceof KnowWEArticle)
					// so there is nothing left but to clean the reused KnowledgeBase
					cleanKnowledge(article);
				}
				return true;
			}
			
			// during the building of an Article, this point is only reached the first time 
			// a changed Section or the root Section calls this method
			if (lastKbm != null && isEmpty(kbms.get(title))
					&& (s.getObjectType() instanceof KnowledgeRecyclingObjectType
							|| (s.getObjectType() instanceof KnowWEArticle 
								&& lastTerminologySectionsCount.get(title) 
									== terminologySectionsCount.get(title)))) {
				
				// if there exists a previous version of the KnowledgeBase, there hasn't been
				// added any knowledge to the current KnowledgeBase and the current (changed) Section
				// contains knowledge that can be modified in the KnowledgeBase or the current Section
				// is the root Section but the number of terminology containing Sections hasn't changed
				// in the article, then the last version of the KnowledgeBase can be reused.
				lastKbms.remove(title);
				usingOldKBM.put(title, true);
				kbms.put(title, lastKbm);
				if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
					// remember to clean that type of knowledge in the KnowledgeBase
					typesToClean.get(title).add(((KnowledgeRecyclingObjectType) s.getObjectType()).getClass());
				}
				if (s.getObjectType() instanceof KnowWEArticle) {
					// this is the root Section respectively the last time in the process of
					// building this article this method is called, so its time to clean the
					// KnowledgeBase
					cleanKnowledge(article);
				}
				return true;
			} else {
				// if the last version of the KnowledgeBase can not be reused it needs to be rebuild
				useNewKBM(article, s);
				return true;
			}
		}
		return false;
	}
	
	private void cleanKnowledge(KnowWEArticle article) {
		Map<Class<? extends KnowledgeRecyclingObjectType>, Integer> countMap = knowledgeSectionsCount.get(article.getTitle());
		Map<Class<? extends KnowledgeRecyclingObjectType>, Integer> lastCountMap = lastKnowledgeSectionsCount.get(article.getTitle());
		for (Class<? extends KnowledgeRecyclingObjectType> clazz:lastCountMap.keySet()) {
			if (countMap.get(clazz) == null || !countMap.get(clazz).equals(lastCountMap.get(clazz))) {
				typesToClean.get(article.getTitle()).add(clazz);
			}
		}
		for (Class<? extends KnowledgeRecyclingObjectType> type:typesToClean.get(article.getTitle())) {
			try {
				type.newInstance().cleanKnowledge(article, kbms.get(article.getTitle()));
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			savedToJar.put(article.getTitle(), false);
		}
	}
	
	private void useNewKBM(KnowWEArticle article, Section s) {
		usingNewKBM.put(article.getTitle(), true);
		savedToJar.put(article.getTitle(), false);
		List<Section> sectionsToRevise = article.getAllNodesParsingPostOrder();
		List<Section> strSub = sectionsToRevise.subList(0, sectionsToRevise.indexOf(s));
		for (Section sec:strSub) {
			sec.getObjectType().reviseSubtree(article, sec);
		}
	}

	private boolean isEmpty(KnowledgeBaseManagement kbm) {
		if (kbm.getKnowledgeBase().getAllKnowledgeSlices().size() == 0
				&& kbm.getKnowledgeBase().getQuestions().size() < 1
				&& kbm.getKnowledgeBase().getDiagnoses().size() <= 1) {
			return true;
		} else {
			return false;
		}
		
	}
	
	@Override
	public URL saveKnowledge(String title) {
		KnowledgeBaseManagement kbm = kbms.get(title);
		if (kbm != null) {
			KnowledgeBase base = kbm.getKnowledgeBase();
			URL home = D3webModule.getKbUrl(web, base.getId());
			if (!savedToJar.get(title)) {;
				D3webPersistence.getInstance().getPersistenceManager().save(base, home);
				savedToJar.put(title, true);
			}
			return home;
		}
		return null;
	}
	
}
