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

package de.d3web.we.terminology;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DistributedRegistrationManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

/**
 * D3webKnowledgeHandler. Handles Knowledge and its recycling.
 * 
 * @author astriffler
 */
public class D3webKnowledgeHandler implements KnowledgeRepresentationHandler {

	private String web;

	/**
	 * Map for all articles an their KBMs.
	 */
	private static Map<String, KnowledgeBaseManagement> kbms = new HashMap<String, KnowledgeBaseManagement>();

	/**
	 * Stores for each Article if the jar file already got built
	 */
	private static Map<String, Boolean> savedToJar = new HashMap<String, Boolean>();

	/**
	 * <b>This constructor SHOULD NOT BE USED!</b>
	 * <p/>
	 * Use D3webModule.getInstance().getKnowledgeRepresentationHandler(String
	 * web) instead!
	 */
	public D3webKnowledgeHandler(String web) {
		this.web = web;
	}

	public D3webKnowledgeHandler() {
		this.web = KnowWEEnvironment.DEFAULT_WEB;
	}

	/**
	 * @returns the KBM for the given article
	 * @param title TODO
	 */
	public KnowledgeBaseManagement getKBM(String title) {
		KnowledgeBaseManagement kbm = kbms.get(title);
		if (kbm == null) {
			kbm = KnowledgeBaseManagement.createInstance();
			kbms.put(title, kbm);
		}
		return kbm;
	}

	/**
	 * This gets called when an new Article or a new version of an Article gets
	 * build. Prepares it for new d3web knowledge.
	 */
	@Override
	public void initArticle(KnowWEArticle art) {
		DPSEnvironment env = D3webModule.getDPSE(web);
		String id = art.getTitle() + ".." + KnowWEEnvironment.generateDefaultID(art.getTitle());
		KnowledgeService service = env.getService(id);
		if (service != null) {
			env.removeService(service);
			for (Broker broker : env.getBrokers()) {
				broker.signoff(service);
			}
		}
		if (art.isFullParse()) {
			getKBM(art.getTitle()).clearKnowledgeBase();
		}
		savedToJar.put(art.getTitle(), false);
	}

	/**
	 * Registers complete KnowledgeBase... This gets called after revising all
	 * Sections of the Article through their SubtreeHandler.
	 */
	@Override
	public void finishArticle(KnowWEArticle art) {
		KnowledgeBaseManagement kbm = this.getKBM(art.getTitle());
		if (!isEmpty(kbm)) {
			DistributedRegistrationManager.getInstance().registerKnowledgeBase(kbm,
					art.getTitle(), art.getWeb());
		}
	}

	private boolean isEmpty(KnowledgeBaseManagement kbm) {
		if (kbm.getKnowledgeBase().getAllKnowledgeSlices().size() == 0
				&& kbm.getKnowledgeBase().getQuestions().size() < 1
				&& kbm.getKnowledgeBase().getSolutions().size() <= 1) {
			return true;
		}
		else {
			return false;
		}

	}

	@Override
	public URL saveKnowledge(String title) throws IOException {

		KnowledgeBase base = getKBM(title).getKnowledgeBase();
		URL home = D3webModule.getKbUrl(web, base.getId());
		if (!savedToJar.get(title)) {
			PersistenceManager.getInstance().save(base,
					new File(URLDecoder.decode(home.getFile(), "UTF-8")));
			savedToJar.put(title, true);
		}
		return home;
	}

	@Override
	public String getKey() {
		return "d3web";
	}

	@Override
	public void setWeb(String web) {
		this.web = web;
	}

}
