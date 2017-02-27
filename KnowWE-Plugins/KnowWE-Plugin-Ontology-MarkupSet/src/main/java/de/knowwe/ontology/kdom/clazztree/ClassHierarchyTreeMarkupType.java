package de.knowwe.ontology.kdom.clazztree;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 06.02.17.
 */
public class ClassHierarchyTreeMarkupType extends DefaultMarkupType {

	public ClassHierarchyTreeMarkupType(DefaultMarkup markup) {
		super(markup);
	}

	public static final String ANNOTATION_RELATION = "relation";
	public static final String ANNOTATION_NAMESPACE = "namespace";

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("classhierarchy");
		m.addContentType(new HierarchyDashTree(new ClassHierarchyDashtreeElementContent()));
		m.addAnnotation(ANNOTATION_RELATION, false);
		m.addAnnotation(ANNOTATION_NAMESPACE);
	}

	public ClassHierarchyTreeMarkupType() {
		super(m);
	}
}


