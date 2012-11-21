package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

public class CSSType extends AbstractType {
	public CSSType() {
		this.setSectionFinder(new RegexSectionFinder("%%\\(.*?\\).*?%%",
				Pattern.DOTALL | Pattern.MULTILINE));
		this.addChildType(new SectionHeaderType());
		this.addChildType(new SectionContentType(4));
	}
}
