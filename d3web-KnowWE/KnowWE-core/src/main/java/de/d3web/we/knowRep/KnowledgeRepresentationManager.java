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

package de.d3web.we.knowRep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class KnowledgeRepresentationManager {
	
	private static KnowledgeRepresentationManager instance;
	
	private boolean usedHandler;
		
	public static KnowledgeRepresentationManager getInstance() {
		if (instance == null)
			instance = new KnowledgeRepresentationManager();
		return instance;
	}
	
	private KnowledgeRepresentationManager() {
		
	}
	
	private Map<String, KnowledgeRepresentationHandler> handlers = new HashMap<String, KnowledgeRepresentationHandler>();
	
	public void registerHandler(String key, KnowledgeRepresentationHandler handler) {
		handlers.put(key, handler);
		
	}
	
	
	public void initArticle(KnowWEArticle art) {
		usedHandler = false;
		for(KnowledgeRepresentationHandler handler : handlers.values()) {
			handler.initArticle(art);
		}
	}
	
	public void finishArticle(KnowWEArticle art) {
		for(KnowledgeRepresentationHandler handler : handlers.values()) {
			handler.finishArticle(art);
		}
	}
	
	public KnowledgeRepresentationHandler getHandler(String key) {
		usedHandler = true;
		return handlers.get(key);
	}
	
	public Collection<KnowledgeRepresentationHandler> getHandlers() {
		usedHandler = true;
		return handlers.values();
	}
	
	public boolean usedHandler() {
		return this.usedHandler;
	}
	
	public boolean buildKnowledge(Section s) {
		boolean usedOldKnowledge = false;
		for(KnowledgeRepresentationHandler handler : handlers.values()) {
			if (handler.buildKnowledge(s)) {
				usedOldKnowledge = true;
			}
		}
		return usedOldKnowledge;
	}

//	public boolean isKnowledgeBuilt(String title) {
//		boolean usingOldKnowledge = false;
//		for(KnowledgeRepresentationHandler handler : handlers.values()) {
//			if (handler.isKnowledgeBuilt(title)) {
//				usingOldKnowledge = true;
//			}
//		}
//		return usingOldKnowledge;
//	}

}
