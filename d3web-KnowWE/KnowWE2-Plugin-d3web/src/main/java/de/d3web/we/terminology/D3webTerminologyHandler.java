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

public class D3webTerminologyHandler extends KnowledgeRepresentationHandler {

	private Map<String, KnowledgeBaseManagement> kbms = new HashMap<String, KnowledgeBaseManagement>();
	
	private Map<String, KnowledgeBaseManagement> lastKbms  = new HashMap<String, KnowledgeBaseManagement>();
	
	private Map<String, Boolean> usingNewKBM = new HashMap<String, Boolean>();
	
	private Map<String, Boolean> usingOldKBM = new HashMap<String, Boolean>();
	
	private Map<String, HashSet<Class<? extends KnowWEObjectType>>> cleanedTypes 
			= new HashMap<String, HashSet<Class<? extends KnowWEObjectType>>>();
	
	private Map<String, Boolean> finishedKBM = new HashMap<String, Boolean>();
	
	public Map<String, HashSet<Class<? extends KnowWEObjectType>>> getCleanedTypes() {
		return this.cleanedTypes;
	}

	public KnowledgeBaseManagement getKBM(Section s) {
		if (buildKnowledge(s)) {
			//System.out.println("Got KBM for " + s.getObjectType().getName());
			return kbms.get(s.getTitle());
		} else {
			return null;
		}
	}

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
		KnowledgeBaseManagement oldKBM = kbms.remove(art.getTitle());
		if (oldKBM != null) {
			lastKbms.put(art.getTitle(), oldKBM);
		}
		usingNewKBM.put(art.getTitle(), false);
		usingOldKBM.put(art.getTitle(), false);
		cleanedTypes.put(art.getTitle(), new HashSet<Class<? extends KnowWEObjectType>>());
		finishedKBM.put(art.getTitle(), false);
		kbms.put(art.getTitle(), KnowledgeBaseManagement.createInstance());
	}

	@Override
	public void finishArticle(KnowWEArticle art) {
			KnowledgeBaseManagement kbm = this.getKBM(art.getSection());
			if(!isEmpty(kbm)) {
				DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm, 
						art.getTitle(), "default_web");
			}
			finishedKBM.put(art.getTitle(), true);
	}

	@Override
	public boolean buildKnowledge(Section s) {
		if (finishedKBM.get(s.getTitle())) {
			return true;
		}
		if (usingNewKBM.get(s.getTitle())) {
			return true;
		}
		KnowledgeBaseManagement lastKbm = lastKbms.get(s.getTitle());
		if (s.getArticle().getChangedSections().containsKey(s.getId()) 
				|| s.getObjectType() instanceof KnowWEArticle) {
			
			if (usingOldKBM.get(s.getTitle())) {
				if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
					if (!cleanedTypes.get(s.getTitle()).contains(s.getObjectType().getClass())) {
						((KnowledgeRecyclingObjectType) s.getObjectType()).cleanKnowledge(s, kbms.get(s.getTitle()));
						cleanedTypes.get(s.getTitle()).add(s.getObjectType().getClass());
					}
				} else if (!(s.getObjectType() instanceof KnowWEArticle)) {
					// KnowledgeRecyclingObjectTypes should be the last ObjectTypes to parse...
					Logger.getLogger(this.getClass().getName())
						.log(Level.WARNING, "Wrong order of parsing for ObjectType '" +
								s.getObjectType() + "'!");
				}
				return true;
			}
			
			if (lastKbm != null && isEmpty(kbms.get(s.getTitle()))
					&& (s.getObjectType() instanceof KnowledgeRecyclingObjectType
							|| s.getObjectType() instanceof KnowWEArticle)) {
				lastKbms.remove(s.getTitle());
				usingOldKBM.put(s.getTitle(), true);
				kbms.put(s.getTitle(), lastKbm);
				if (s.getObjectType() instanceof KnowledgeRecyclingObjectType) {
					((KnowledgeRecyclingObjectType) s.getObjectType()).cleanKnowledge(s, lastKbm);
					cleanedTypes.get(s.getTitle()).add(s.getObjectType().getClass());
				}
				return true;
			} else {
				usingNewKBM.put(s.getTitle(), true);
				List<Section> sectionsToRevise = s.getArticle().getAllNodesParsingPostOrder();
				List<Section> strSub = sectionsToRevise.subList(0, sectionsToRevise.indexOf(s));
				for (Section sec:strSub) {
					sec.getObjectType().reviseSubtree(sec);
				}
				return true;
			}
		}
		return false;
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
