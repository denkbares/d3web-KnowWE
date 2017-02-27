package de.knowwe.ontology.kdom.clazztree;

import de.knowwe.kdom.dashtree.DashTreeElementContent;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 06.02.17.
 */
public class ClassHierarchyDashtreeElementContent extends DashTreeElementContent {

	public ClassHierarchyDashtreeElementContent() {
		this.addChildType(new ClazzLine());
	}
}
