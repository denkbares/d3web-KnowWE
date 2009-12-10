package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.basic.SquareBracedType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.type.ParameterizedKeyWordType;

public class CondKnownType extends ParameterizedKeyWordType {
	
	public CondKnownType() {
		super("KNOWN", new QuotedQuestion());
	}
	
//	public void init() {
//		this.sectionFinder = new RegexSectionFinder("KNOWN\\[[^\\]]*]");
//		this.childrenTypes.add(new CondKnownKey());
//		this.childrenTypes.add(new SquareBracedType(new QuotedQuestion()));
//		setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR2));
//	}
//	
//	
//	class CondKnownKey extends DefaultAbstractKnowWEObjectType {
//		public void init() {
//			this.sectionFinder = new RegexSectionFinder("KNOWN");
//		}
//	}

}
