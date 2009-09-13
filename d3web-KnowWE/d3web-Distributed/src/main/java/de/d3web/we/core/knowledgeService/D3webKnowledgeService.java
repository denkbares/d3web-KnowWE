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

package de.d3web.we.core.knowledgeService;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.persistence.xml.PersistenceManager;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.terminology.local.D3webLocalDiagnosisTerminology;
import de.d3web.we.terminology.local.D3webLocalSymptomTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public class D3webKnowledgeService implements KnowledgeService {

	private KnowledgeBase base;
	private URL home;
	private String id;
	private boolean loaded;
	
	public D3webKnowledgeService(URL home) {
		super();
		this.home = home;
		initBase();
	}
	
	public D3webKnowledgeService(URL origin, URL home, String id) {
		super();
		this.home = origin;
		initBase();
		this.id = id;
		base.setId(id);
		this.home = home;
		save();
	}
	
	public D3webKnowledgeService(KnowledgeBase base, String id, URL home) {
		super();
		this.base = base;
		this.id = id;
		this.home = home;
		save();
		//load();
		loaded = true;
	}
	
	private void initBase() {
		load();
		id = base.getId();
		if(id == null && home != null) {
			String urlString = home.getPath();
			int i = urlString.lastIndexOf("/");
			urlString = urlString.substring(i, urlString.length());
			id = urlString.substring(1, urlString.length() - 4);
			base.setId(id);
		}
	}

	public String getId() {
		return id;
	}


	public KnowledgeServiceSession createSession(Broker broker) {
		if(!loaded) {
			load();
		}
		return new D3webKnowledgeServiceSession(base, broker, id);
	}


	private void load() {
		PersistenceManager pm = D3webPersistence.getInstance().getPersistenceManager();
		
		Logger.getLogger("\n lade KB: "+"id " + "von "+ home.toString());
		base = pm.load(home);
		Logger.getLogger("\n loaded KB: "+"id " + " questions: "+ base.getQuestions().size());
		loaded = true;
	}

	private void save() {
		D3webPersistence.getInstance().getPersistenceManager().save(base, home);
	}
	
	public Map<TerminologyType, LocalTerminologyAccess> getTerminologies() {
		Map<TerminologyType, LocalTerminologyAccess> result = new HashMap<TerminologyType, LocalTerminologyAccess>();
		LocalTerminologyAccess<IDObject> symptom = new D3webLocalSymptomTerminology(KnowledgeBaseManagement.createInstance(base));
		LocalTerminologyAccess<NamedObject> diagnosis = new D3webLocalDiagnosisTerminology(KnowledgeBaseManagement.createInstance(base));
		result.put(TerminologyType.symptom, symptom);
		result.put(TerminologyType.diagnosis, diagnosis);
		return result;
	}

	public String toString() {
		return "d3web Service " + getId();
	}

	public KnowledgeBase getBase() {
		return base;
	}
	
}
