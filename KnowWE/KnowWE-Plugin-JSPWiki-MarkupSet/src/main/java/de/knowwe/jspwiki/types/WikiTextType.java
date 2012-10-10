package de.knowwe.jspwiki.types;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;

public class WikiTextType extends AbstractType {

	public WikiTextType() {
		this.setSectionFinder(new AllTextSectionFinder());

	}
}
