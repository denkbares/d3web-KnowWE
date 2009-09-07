package de.d3web.we.kdom.TiRex;

import de.d3web.we.kdom.sectionFinder.SentenceSectionFinder;

public class TiRexSentence extends TiRexChunk {

	@Override
	protected void init() {
		this.sectionFinder = new SentenceSectionFinder(this);

	}
	


}
