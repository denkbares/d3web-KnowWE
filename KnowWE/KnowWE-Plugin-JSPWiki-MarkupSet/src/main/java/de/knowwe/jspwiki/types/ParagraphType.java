package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.jspwiki.renderer.JSPWikiMarkupIDRenderer;

public class ParagraphType extends AbstractType {

	public ParagraphType() {
		Pattern pattern = Pattern.compile("^.+?([\n\r]{4,}|\\z)",
				Pattern.MULTILINE + Pattern.DOTALL);
		this.setSectionFinder(new RegexSectionFinder(pattern));
		this.addChildType(new VerbatimType());
		this.addChildType(new ListType());
		this.addChildType(new OrderedListType());
		this.addChildType(new BoldType());
		this.addChildType(new ItalicType());
		this.addChildType(new StrikeThroughType());
		this.addChildType(new ImageType());
		this.addChildType(new LinkType());
		this.addChildType(new WikiTextType());
		this.setRenderer(new JSPWikiMarkupIDRenderer());
	}
}
