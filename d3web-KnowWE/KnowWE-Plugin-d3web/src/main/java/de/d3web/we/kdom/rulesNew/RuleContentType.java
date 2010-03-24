package de.d3web.we.kdom.rulesNew;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.rules.If;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.kdom.rules.RuleAction;
import de.d3web.we.kdom.rules.Then;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;

public class RuleContentType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new AllTextSectionFinder();
		Rule ruleType = new Rule();
		configureChildrenTypes(ruleType);
		this.addChildType(ruleType);

	}

	private void configureChildrenTypes(Rule rule) {
		List<KnowWEObjectType> list = rule.getAllowedChildrenTypes();
		KnowWEObjectType[] array = list.toArray(new KnowWEObjectType[list.size()]);
		for (KnowWEObjectType knowWEObjectType : array) {
			rule.removeChildType(knowWEObjectType);
		}
		rule.addChildType(new If());
		Then then = new Then();
		rule.addChildType(then);
		ConditionArea cond = new ConditionArea();
		cond.setSectionFinder(new AllBeforeTypeSectionFinder(then));
		rule.addChildType(cond);
		rule.addChildType(new RuleAction());
	}
}

class ConditionArea extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.addChildType(new CompositeCondition());
	}
}
