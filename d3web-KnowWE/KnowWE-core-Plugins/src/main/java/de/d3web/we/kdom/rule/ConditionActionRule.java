package de.d3web.we.kdom.rule;

import java.util.List;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

public class ConditionActionRule extends DefaultAbstractKnowWEObjectType {

	ConditionArea condArea = new ConditionArea();

	public ConditionActionRule(KnowWEObjectType action) {

		sectionFinder = new RegexSectionFinder(
				"(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN"
						+ "|\\s*\\z))",
				Pattern.DOTALL);
		this.addChildType(new If());
		Then then = new Then();
		this.addChildType(then);

		condArea.setSectionFinder(new AllBeforeTypeSectionFinder(then));
		this.addChildType(condArea);
		this.addChildType(action);
	}

	public void setTerminalConditions(List<KnowWEObjectType> termConds) {
		condArea.compCond.setAllowedTerminalConditions(termConds);
	}

	class ConditionArea extends DefaultAbstractKnowWEObjectType {

		CompositeCondition compCond = null;

		public ConditionArea() {
			compCond = new CompositeCondition();
			this.addChildType(compCond);
		}
	}

}
