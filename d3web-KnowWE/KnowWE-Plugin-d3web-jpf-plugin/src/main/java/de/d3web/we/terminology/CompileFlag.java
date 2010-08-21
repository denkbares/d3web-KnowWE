/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.namespace.KnowWENamespaceManager;
import de.d3web.we.core.namespace.NamespaceInclude;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.store.SectionStore;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class CompileFlag extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	// private static String PRIO_MAP_KEY = "prio_map_key";

	private static String NAMESPACEDEFS_SNAPSHOT_KEY = "namespacedefs_snapshot_key";

	static {
		m = new DefaultMarkup("Compile");
		m.addContentType(new NamespaceIncludeType());

	}

	public CompileFlag() {
		super(m);
	}

	static class NamespaceIncludeType extends DefaultAbstractKnowWEObjectType implements NamespaceInclude {

		public NamespaceIncludeType() {
			this.sectionFinder = new AllTextSectionFinder();
			this.addSubtreeHandler(Priority.PRECOMPILE, new CompileFlagCreateHandler());
			this.addSubtreeHandler(Priority.POSTCOMPILE, new CompileFlagDestroyHandler());
		}

		@Override
		public String getNamespaceToInclude(Section<? extends NamespaceInclude> s) {
			return s.getOriginalText().trim();
		}
	}

	static class CompileFlagCreateHandler extends SubtreeHandler<NamespaceIncludeType> {

		public CompileFlagCreateHandler() {
			super(true);
		}

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<NamespaceIncludeType> s) {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NamespaceIncludeType> s) {

			KnowWENamespaceManager nsMng = KnowWEEnvironment.getInstance().getNamespaceManager(
					article.getWeb());

			if (article.isFullParse() || !s.isReusedBy(article.getTitle())) {
				nsMng.registerNamespaceInclude(article, s);
			}

			if (!s.get().getNamespaceToInclude(s).equals(article.getTitle())) {

				List<Section<?>> namespaceDefinitions = nsMng.getNamespaceDefinitions(
						s.get().getNamespaceToInclude(s));

				KnowWEUtils.storeObject(article, s, NAMESPACEDEFS_SNAPSHOT_KEY,
						namespaceDefinitions);

				List<Section<?>> includedNamespaces = new ArrayList<Section<?>>();

				for (Section<?> nsDef : namespaceDefinitions) {
					List<Section<?>> nodes = new LinkedList<Section<?>>();
					nsDef.getAllNodesPostOrder(nodes);
					includedNamespaces.addAll(nodes);
				}

				TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
						Priority.createPrioritySortedList(includedNamespaces);

				for (Priority priority : prioMap.descendingKeySet()) {
					List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
					for (Section<? extends KnowWEObjectType> section : prioList) {
						section.letSubtreeHandlersCreate(article, priority);
					}
				}

				for (Section<?> nsDef : namespaceDefinitions) {
					nsDef.setReusedStateRecursively(article.getTitle(), true);
				}

			}
			return null;
		}

	}

	static class CompileFlagDestroyHandler extends SubtreeHandler<NamespaceIncludeType> {

		public CompileFlagDestroyHandler() {
			super(true);
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NamespaceIncludeType> s) {
			return null;
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<NamespaceIncludeType> s) {
			return true;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void destroy(KnowWEArticle article, Section<NamespaceIncludeType> s) {

			if (!s.isReusedBy(article.getTitle())) article.setFullParse(true, this);

			if (!article.isFullParse()
					&& !s.get().getNamespaceToInclude(s).equals(article.getTitle())) {

				List<Section<?>> storedNamespaceDefinitions = (List<Section<?>>) KnowWEUtils.getObjectFromLastVersion(
						article, s, NAMESPACEDEFS_SNAPSHOT_KEY);

				List<Section<?>> nodes = new LinkedList<Section<?>>();
				for (Section<?> nsDef : storedNamespaceDefinitions) {
					nsDef.getAllNodesPostOrder(nodes);
				}
				for (Section<?> node : nodes) {
					if (node.isReusedBy(article.getTitle())) {
						SectionStore lastStore = KnowWEEnvironment.getInstance().getArticleManager(
								article.getWeb()).getTypeStore().getLastSectionStore(
								article.getTitle(),
								node.getID());
						if (lastStore != null) {
							// reuse last section store
							KnowWEEnvironment.getInstance().getArticleManager(
									article.getWeb()).getTypeStore().putSectionStore(
											article.getTitle(), node.getID(),
									lastStore);
						}
					}
				}

				TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
						Priority.createPrioritySortedList(nodes);

				for (Priority priority : prioMap.descendingKeySet()) {
					List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
					for (Section<? extends KnowWEObjectType> section : prioList) {
						section.letSubtreeHandlersDestroy(article, priority);
					}
				}

			}


		}

	}
}
