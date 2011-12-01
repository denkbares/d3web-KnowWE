/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.knowRep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class KnowledgeRepresentationManager {

	// private static KnowledgeRepresentationManager instance;
	//
	//
	// public static KnowledgeRepresentationManager getInstance() {
	// if (instance == null)
	// instance = new KnowledgeRepresentationManager();
	// return instance;
	// }

	private final Map<String, KnowledgeRepresentationHandler> handlers = new HashMap<String, KnowledgeRepresentationHandler>();

	private final String web;

	/**
	 * <b>This constructor SHOULD NOT BE USED!</b>
	 * <p/>
	 * Use KnowWEEnvironment.getInstance().getKnowledgeRepresentationManager(
	 * String web) instead!
	 */
	public KnowledgeRepresentationManager(String web) {
		this.web = web;
	}

	public void registerHandler(String key, KnowledgeRepresentationHandler handler) {
		handlers.put(key, handler);
	}

	public void registerHandler(KnowledgeRepresentationHandler handler) {
		handlers.put(handler.getKey(), handler);
	}

	public void initArticle(KnowWEArticle art) {
		for (KnowledgeRepresentationHandler handler : handlers.values()) {
			handler.initArticle(art);
		}
	}

	public void finishArticle(KnowWEArticle art) {
		for (KnowledgeRepresentationHandler handler : handlers.values()) {
			try {
				handler.finishArticle(art);
			}
			catch (Exception e) {
				List<Message> messages = new ArrayList<Message>();
				messages.add(Messages.error(
						"This page's content caused a serious initialitation error:\n" +
								e.getLocalizedMessage()));
				Messages.storeMessages(art, art.getSection(), getClass(), messages);
				Logger.getLogger("KnowWE-core").log(Level.SEVERE,
						"Page content of page '" +
								art.getTitle() +
								"' caused a serious initialitation error", e);
			}
		}
	}

	public KnowledgeRepresentationHandler getHandler(String key) {
		return handlers.get(key);
	}

	public Collection<KnowledgeRepresentationHandler> getHandlers() {
		return handlers.values();
	}

	public String getWeb() {
		return this.web;
	}

}
