package de.d3web.we.kdom.rule;

import java.util.List;
import java.util.regex.Pattern;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.objects.TermRelationDefinition;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

/**
 * @author Jochen
 *
 *         Markup for a simple condition-action-rule using the
 *         CompositeCondition @see {@link CompositeCondition}
 *
 *         The action type is given to the constructor. The TerminalConditions
 *         can be set via setTerminalConditions()
 *
 */
public class ConditionActionRule extends TermRelationDefinition {

	ConditionArea condArea = new ConditionArea();

	public ConditionActionRule(AbstractKnowWEObjectType action) {
		sectionFinder = new RegexSectionFinder(
				"(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN"
						+ "|\\s*\\z))",
				Pattern.DOTALL);
		this.addChildType(new If());
		Then then = new Then();
		this.addChildType(then);

		condArea.setSectionFinder(new AllBeforeTypeSectionFinder(then));
		this.addChildType(condArea);
		action.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(action);

		this.setCustomRenderer(new EditSectionRenderer());

	}

	/**
	 * Add the list of TerminalConditions for the Condition here
	 *
	 * @param termConds
	 */
	public void setTerminalConditions(List<KnowWEObjectType> termConds) {
		condArea.compCond.setAllowedTerminalConditions(termConds);
	}

	/**
	 * ConditionArea of the Condition-Action-Rule, instanciates the condition
	 * composite
	 *
	 *
	 * @author Jochen
	 *
	 */
	class ConditionArea extends DefaultAbstractKnowWEObjectType {

		CompositeCondition compCond = null;

		public ConditionArea() {
			compCond = new CompositeCondition();
			this.addChildType(compCond);
		}
	}

}
