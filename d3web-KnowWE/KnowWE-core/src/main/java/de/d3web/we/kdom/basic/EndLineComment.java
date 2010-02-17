package de.d3web.we.kdom.basic;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

public class EndLineComment extends DefaultAbstractKnowWEObjectType {

	public EndLineComment() {
		this.sectionFinder = new RegexSectionFinder("//.*$");
	}

}