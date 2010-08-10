package de.d3web.we.terminology;

import java.util.ArrayList;
import java.util.Collection;
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

public class CompileMarker extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("Compile");
		m.addContentType(new NamespaceIncludeType());

	}

	public CompileMarker() {
		super(m);
	}

	static class NamespaceIncludeType extends DefaultAbstractKnowWEObjectType implements NamespaceInclude {
		
		public NamespaceIncludeType() {
			this.sectionFinder = new AllTextSectionFinder();
			this.addSubtreeHandler(Priority.PRECOMPILE, new RegisterNamespaceInclude());
		}

		@Override
		public Collection<String> getIncludedNamespaces(Section<? extends NamespaceInclude> s) {
			List<String> namespaces = new ArrayList<String>(1);
			namespaces.add(s.getOriginalText().trim());
			return namespaces;
		}
	}

	static class RegisterNamespaceInclude extends SubtreeHandler<NamespaceIncludeType> {

		private static String PRIO_MAP_KEY = "prio_map_key";

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NamespaceIncludeType> s) {
			KnowWENamespaceManager nsMng = KnowWEEnvironment.getInstance().getNamespaceManager(
					article.getWeb());
			nsMng.registerNamespaceInclude(article, s);

			Set<Section<?>> namespaceDefinitions = nsMng.getNamespaceDefinitions(s.getOriginalText().trim());
			List<Section<?>> includedNamespaces = new ArrayList<Section<?>>();

			for (Section<?> nsDef : namespaceDefinitions) {
				List<Section<?>> nodes = new LinkedList<Section<?>>();
				nsDef.getAllNodesPostOrder(nodes);
				includedNamespaces.addAll(nodes);
			}

			TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
					Priority.createPrioritySortedList(includedNamespaces);

			KnowWEUtils.storeSectionInfo(article, s, PRIO_MAP_KEY, prioMap);

			for (Priority priority : prioMap.descendingKeySet()) {
				List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
				for (Section<? extends KnowWEObjectType> section : prioList) {
					section.letSubtreeHandlersCreate(article, priority);
				}
			}

			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void destroy(KnowWEArticle article, Section<NamespaceIncludeType> s) {

			TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> prioMap =
					(TreeMap<Priority, List<Section<? extends KnowWEObjectType>>>)
					KnowWEUtils.getObjectFromLastVersion(article, s, PRIO_MAP_KEY);

			for (Priority priority : prioMap.descendingKeySet()) {
				List<Section<? extends KnowWEObjectType>> prioList = prioMap.get(priority);
				for (Section<? extends KnowWEObjectType> section : prioList) {
					section.letSubtreeHandlersDestroy(article, priority);
				}
			}
			KnowWEEnvironment.getInstance().getNamespaceManager(article.getWeb()).unregisterNamespaceInclude(
					article, s);
			article.setFullParse(true, this);
		}

	}

}
