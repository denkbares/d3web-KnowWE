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

package de.knowwe.core.contexts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * A context is a common environment in which a section-node operates. A context
 * is inherited from the parents. Sections can have multiple contexts.
 * 
 * 
 * @author Fabian Haupt
 * 
 */
public class ContextManager {

	private static ContextManager me;
	private final Map<String, ArticleContextMap> contextmap;

	private ContextManager() {
		contextmap = new HashMap<String, ArticleContextMap>();

	}

	public void attachContextForClass(Section<?> section, Context c) {
		Object o = KnowWEUtils.getStoredObject(section.getArticle(), section,
				c.getClass().getName());
		if (o != null && (o instanceof Set)) {
			@SuppressWarnings("unchecked")
			Set<Context> contextSet = ((Set<Context>) o);
			contextSet.add(c);
		}
		else {
			Set<Context> contextSet = new HashSet<Context>();
			contextSet.add(c);
			KnowWEUtils.storeObject(section.getArticle(), section, c.getClass().getName(),
					contextSet);

		}

	}

	/**
	 * Attaches a context to a specific section.
	 * 
	 * @param section
	 * @param context
	 */
	public void attachContext(Section<?> section, Context context) {

		KnowWEUtils.storeObject(section.getArticle(), section, context.getCID(), context);

		// String title = section.getArticle().getTitle();
		// ArticleContextMap art = contextmap.get(title);
		// if (art == null) {
		// art = new ArticleContextMap();
		// contextmap.put(title, art);
		// }
		// Map<String, Context> contextsForSection = art
		// .getContextMapForSection(section);
		//
		// if (contextsForSection == null) {
		// contextsForSection = new HashMap<String, Context>();
		// contextsForSection.put(context.getCID(), context);
		//
		// contextmap.get(title).put(section,
		// contextsForSection);
		// } else {
		// contextsForSection.put(context.getCID(), context);
		// }
	}

	/**
	 * To remove contexts of one article (objects free for GC)
	 * 
	 * @param article
	 */
	public void detachContexts(String article) {
		this.contextmap.remove(article);
	}

	/**
	 * returns a set of all contextids a section belongs too, including the
	 * inherited ones
	 * 
	 * @param section
	 * @return
	 */
	// public Set<String> getContexts(Section section) {
	// Set<String> contextlist = new HashSet<String>();
	// contextlist.addAll(contextmap.get(section.getArticle().getTitle())
	// .getContextMapForSection(section).keySet());
	// if (section.getFather() != null) {
	// contextlist.addAll(contextmap.get(section.getArticle().getTitle())
	// .get(section.getFather()).keySet());
	// }
	// return contextlist;
	// }

	/**
	 * returns a contextmanagerinstance
	 * 
	 * @return
	 */
	public static synchronized ContextManager getInstance() {
		if (me == null) {
			me = new ContextManager();
		}
		return me;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public Context getContextForClass(Section<?> section, Class<? extends Context> c) {
		return getContext(section, c.getName());
	}

	public Context getContext(Section<?> section, String contextid) {
		return getContext(section, section, contextid);
	}

	/**
	 * returns a context of a section. looks for inherited contexts too
	 * 
	 * @param section
	 * @param contextid
	 * @return
	 */
	public Context getContext(Section<?> section, Section<?> originalSection, String contextid) {
		Object o = KnowWEUtils.getStoredObject(section.getArticle(), section, contextid);
		if (o instanceof Context) {
			if (((Context) o).isValidForSection(section)) {
				return (Context) o;
			}
			return null;
		}
		if (o instanceof Set) {
			@SuppressWarnings("unchecked")
			Set<Context> contextSet = ((Set<Context>) o);
			for (Context context : contextSet) {
				if (context.isValidForSection(originalSection)) return context;
			}
			return null;
		}
		else {
			if (section.getFather() != null) {
				return getContext(section.getFather(), originalSection, contextid);
			}
			else {
				return null;
			}
		}
		// Context erg = null;
		// ArticleContextMap artMap = contextmap.get(section.getArticle()
		// .getTitle());
		// if (artMap != null) {
		// erg = artMap.get(section) != null ? artMap.get(section).get(
		// contextid) : null;
		// }
		// return erg != null ? erg : section.getFather() != null ? getContext(
		// section.getFather(), contextid) : null;
	}

	class ArticleContextMap {

		private final HashMap<Section<?>, Map<String, Context>> artContextMap;

		public Map<Section<?>, Map<String, Context>> getContextmap() {
			return artContextMap;
		}

		public Map<String, Context> getContextMapForSection(Section<?> s) {
			return artContextMap.get(s);
		}

		public void put(Section<?> s, Map<String, Context> map) {
			this.artContextMap.put(s, map);
		}

		public Map<String, Context> get(Section<?> s) {
			return this.artContextMap.get(s);
		}

		private ArticleContextMap() {
			artContextMap = new HashMap<Section<?>, Map<String, Context>>();

		}
	}

}
