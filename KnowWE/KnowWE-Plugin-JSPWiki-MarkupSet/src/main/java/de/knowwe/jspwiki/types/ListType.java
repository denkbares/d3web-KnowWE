package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.dashtree.DashSubtree;
import de.knowwe.kdom.dashtree.DashTree;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.LineEndComment;

public class ListType extends AbstractType {

	public ListType() {
		Pattern pattern = Pattern.compile("(^|\n+)((\\*).+?)(?=\n[^(\\*)])",
				Pattern.MULTILINE + Pattern.DOTALL);
		this.setSectionFinder(new RegexSectionFinder(pattern));
		this.addChildType(getNoCommentDashTree());
	}

	private Type getNoCommentDashTree() {

		DashSubtree subtree = new DashSubtree('*', 1);
		subtree.clearChildrenTypes();

		DashTreeElement element = new DashTreeElement(subtree.getKey());
		element.removeChildType(LineEndComment.class);

		subtree.addChildType(element);
		subtree.addChildType(subtree);

		DashTree tree = new DashTree('*', 1);
		tree.clearChildrenTypes();
		tree.addChildType(subtree);

		return tree;
	}
}
