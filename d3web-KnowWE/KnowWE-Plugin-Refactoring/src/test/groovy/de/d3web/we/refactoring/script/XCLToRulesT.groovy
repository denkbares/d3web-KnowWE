package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xcl.XCList;

public class XCLToRulesT extends XCLToRules {
	private Section<?> articleSection;
	public XCLToRulesT(Section<?> articleSection) {
		this.articleSection = articleSection;
	}

	@Override
	public Section<?> findXCList() {
		return articleSection.findSuccessor(XCList.class);
	}
}
