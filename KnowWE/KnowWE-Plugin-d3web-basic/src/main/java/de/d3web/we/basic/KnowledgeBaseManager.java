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

package de.d3web.we.basic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompilerStartEvent;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;

/**
 * D3webKnowledgeHandler. Handles Knowledge and its recycling.
 * 
 * @author astriffler
 */
public class KnowledgeBaseManager implements EventListener {

	private static final Map<String, KnowledgeBaseManager> instances = new HashMap<String, KnowledgeBaseManager>();

	public static KnowledgeBaseManager getInstance(String web) {
		KnowledgeBaseManager knowledgeBaseManager = instances.get(web);
		if (knowledgeBaseManager == null) {
			knowledgeBaseManager = new KnowledgeBaseManager(web);
			instances.put(web, knowledgeBaseManager);
		}
		return knowledgeBaseManager;
	}

	private final String web;

	/**
	 * Map for all articles and their KBMs.
	 */
	private final Map<Section<? extends PackageCompileType>, KnowledgeBase> kbs =
			Collections.synchronizedMap(new WeakHashMap<Section<? extends PackageCompileType>, KnowledgeBase>());

	/**
	 * Map to store the last version of the KnowledgeBase.
	 */
	private final Map<Section<? extends PackageCompileType>, KnowledgeBase> lastKB =
			Collections.synchronizedMap(new WeakHashMap<Section<? extends PackageCompileType>, KnowledgeBase>());

	/**
	 * Stores for each Article if the jar file already got built
	 */
	// We no longer can cache knowledge bases, because attachments added as
	// resources could change without anyone knowing. As long as we do not
	// get any notification from JSPWiki that attachments have changed, we
	// need to create a new knowledge base every time.
	// private static Map<String, Boolean> savedToJar = new HashMap<String,
	// Boolean>();

	/**
	 * <b>This constructor SHOULD NOT BE USED!</b>
	 * <p/>
	 * Use D3webModule.getInstance().getKnowledgeRepresentationHandler(String
	 * web) instead!
	 */
	public KnowledgeBaseManager(String web) {
		this.web = web;
		EventManager.getInstance().registerListener(this);
	}

	/**
	 * Returns all topics of this web that own a compiled d3web knowledge base.
	 * 
	 * @created 14.10.2010
	 * @return all topics with a compiled d3web knowledge base
	 */
	public Set<Section<? extends PackageCompileType>> getKnowledgeBaseSections() {
		/*
		 * Iterators are not automatically synchronized in synchronized
		 * collections. Since the iterator of the key set is needed when adding
		 * it to a new set, we synchronize it manually on the same mutex as the
		 * map uses.
		 */
		synchronized (kbs) {
			TreeSet<Section<? extends PackageCompileType>> sections =
					new TreeSet<Section<? extends PackageCompileType>>(kbs.keySet());
			return Collections.unmodifiableSet(sections);
		}
	}

	public URL saveKnowledge(Section<PackageCompileType> compileSection) throws IOException {
		KnowledgeBase base = getKnowledgeBase(compileSection);
		URL home = D3webUtils.getKnowledgeBaseURL(web, base.getId());
		// We no longer can cache knowledge bases, because attachments added as
		// resources could change without anyone knowing. As long as we do not
		// get any notification from JSPWiki that attachments have changed, we
		// need to create a new knowledge base every time.
		PersistenceManager.getInstance().save(base,
				new File(Strings.decodeURL(home.getFile())));
		return home;
	}

	/**
	 * Sets a knowledgebase at the specified article
	 * 
	 * @created 6/08/2012
	 * @param title title of the article
	 */
	public void setKnowledgeBase(Section<? extends PackageCompileType> compileSection, KnowledgeBase kb) {
		kbs.put(compileSection, kb);

	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		Collection<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(2);
		events.add(D3webCompilerStartEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof D3webCompilerStartEvent) {
			Section<? extends PackageCompileType> section = ((D3webCompilerStartEvent) event).getCompiler().getCompileSection();
			lastKB.put(section, getKnowledgeBase(section));
			KnowledgeBase kb = KnowledgeBaseUtils.createKnowledgeBase();
			kb.setId(section.getTitle());
			kbs.put(section, kb);
		}
	}

	public KnowledgeBase getKnowledgeBase(Section<? extends PackageCompileType> compileSection) {
		return kbs.get(compileSection);
	}

}
