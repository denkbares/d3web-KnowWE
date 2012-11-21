package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.jspwiki.renderer.JSPWikiMarkupIDRenderer;

public class TabbedSectionType extends AbstractType {

	public TabbedSectionType() {
		this.setSectionFinder(new RegexSectionFinder(
				"%%tabbedSection.*?/%\\s*/%", Pattern.DOTALL
						| Pattern.MULTILINE));
		this.addChildType(new SectionHeaderType());
		this.addChildType(new TabTitleType());
		this.setRenderer(new JSPWikiMarkupIDRenderer());
	}
}
