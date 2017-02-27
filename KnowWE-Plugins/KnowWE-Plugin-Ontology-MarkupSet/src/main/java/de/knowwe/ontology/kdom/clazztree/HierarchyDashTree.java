package de.knowwe.ontology.kdom.clazztree;

import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.dashtree.DashTree;
import de.knowwe.kdom.dashtree.DashTreeElementContent;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 06.02.17.
 */
public class HierarchyDashTree extends DashTree {



	public HierarchyDashTree(DashTreeElementContent contentType) {
		replaceDashTreeElementContentType(contentType);
		this.setSectionFinder(AllTextFinder.getInstance());
	}

}
