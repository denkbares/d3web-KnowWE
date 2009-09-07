package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.LineSectionFinder;

public class TextLine extends DefaultAbstractKnowWEObjectType {
	
	public TextLine() {
		this.setNumberedType(true);
		init();
	}
	
	@Override
	public SectionFinder getSectioner() {
		return new LineSectionFinder(this);
	}

	@Override
	protected void init() {
		childrenTypes.add(new LineBreak());
		childrenTypes.add(new LineContent());
	}
	

}
