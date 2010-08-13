package de.d3web.we.terminology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
			this.addSubtreeHandler(Priority.PRECOMPILE, new RegisterNamespaceInclude());
			this.addSubtreeHandler(Priority.POSTCOMPILE, new UnregisterNamespaceInclude());
		}

		@Override
		public String getNamespaceToInclude(Section<? extends NamespaceInclude> s) {
			return s.getOriginalText().trim();
		}
	}

	static class RegisterNamespaceInclude extends SubtreeHandler<NamespaceIncludeType> {

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<NamespaceIncludeType> s) {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NamespaceIncludeType> s) {

			KnowWENamespaceManager nsMng = KnowWEEnvironment.getInstance().getNamespaceManager(
					article.getWeb());
			nsMng.registerNamespaceInclude(article, s);
			
			if (s.get().getNamespaceToInclude(s).equals(article.getTitle())) return null;

			List<Section<?>> namespaceDefinitions = nsMng.getNamespaceDefinitions(s.getOriginalText().trim());

			KnowWEUtils.storeSectionInfo(article, s, NAMESPACEDEFS_SNAPSHOT_KEY,
					namespaceDefinitions);

			List<Section<?>> includedNamespaces = new ArrayList<Section<?>>();

			for (Section<?> nsDef : namespaceDefinitions) {
				List<Section<?>> nodes = new LinkedList<Section<?>>();
				nsDef.getAllNodesPostOrder(nodes);
				includedNamespaces.addAll(nodes);
			}

			TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
					Priority.createPrioritySortedList(includedNamespaces);

			// KnowWEUtils.storeSectionInfo(article, s, PRIO_MAP_KEY, prioMap);

			for (Priority priority : prioMap.descendingKeySet()) {
				List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
				for (Section<? extends KnowWEObjectType> section : prioList) {
					section.letSubtreeHandlersCreate(article, priority);
				}
			}

			for (Section<?> nsDef : namespaceDefinitions) {
				nsDef.setReusedStateRecursively(article.getTitle(), true);
			}

			return null;
		}

	}

	static class UnregisterNamespaceInclude extends SubtreeHandler<NamespaceIncludeType> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NamespaceIncludeType> s) {
			return null;
		}


		@Override
		@SuppressWarnings("unchecked")
		public void destroy(KnowWEArticle article, Section<NamespaceIncludeType> s) {

			if (!article.isFullParse()
					|| !s.get().getNamespaceToInclude(s).equals(article.getTitle())) {

				List<Section<?>> namespaceDefinitions = KnowWEEnvironment.getInstance().getNamespaceManager(
						article.getWeb()).getNamespaceDefinitions(s.getOriginalText().trim());

				List<Section<?>> storedNamespaceDefinitions = (List<Section<?>>) KnowWEUtils.getObjectFromLastVersion(
						article, s, NAMESPACEDEFS_SNAPSHOT_KEY);

				Set<Section<?>> lastNamespaceDefinitions;

				if (storedNamespaceDefinitions == null) {
					lastNamespaceDefinitions = new HashSet<Section<?>>(0);
				}
				else {
					lastNamespaceDefinitions = new HashSet<Section<?>>(storedNamespaceDefinitions);
				}

				lastNamespaceDefinitions.removeAll(namespaceDefinitions);

				List<Section<?>> includedNamespaces = new ArrayList<Section<?>>();

				for (Section<?> nsDef : lastNamespaceDefinitions) {
					List<Section<?>> nodes = new LinkedList<Section<?>>();
					nsDef.getAllNodesPostOrder(nodes);
					includedNamespaces.addAll(nodes);
				}

				TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
						Priority.createPrioritySortedList(includedNamespaces);


				for (Priority priority : prioMap.descendingKeySet()) {
					List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
					for (Section<? extends KnowWEObjectType> section : prioList) {
						section.letSubtreeHandlersDestroy(article, priority);
					}
				}

				for (Section<?> nsDef : lastNamespaceDefinitions) {
					nsDef.setReusedBy(article.getTitle(), false);
				}
			}

			KnowWEEnvironment.getInstance().getNamespaceManager(article.getWeb()).unregisterNamespaceInclude(
					article, s);

			article.setFullParse(true, this);
		}

	}
}
