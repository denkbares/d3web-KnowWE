package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

public class TabTitleType extends AbstractType {
	public TabTitleType() {
		this.setSectionFinder(new RegexSectionFinder("%%tab-.*?/%",
				Pattern.DOTALL | Pattern.MULTILINE));
		this.addChildType(new SectionHeaderType());
		this.addChildType(new SectionContentType(4));
	}

}
