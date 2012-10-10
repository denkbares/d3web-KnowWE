package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.dashtree.DashTree;

public class OrderedListType extends AbstractType {

	public OrderedListType() {
		Pattern pattern = Pattern.compile("(^|\n+)(#.+?)(?=\n[^(#)])",
				Pattern.MULTILINE + Pattern.DOTALL);
		this.setSectionFinder(new RegexSectionFinder(pattern));
		this.addChildType(new DashTree('#', 1));
	}
}
