package de.d3web.we.kdom.rule;

import java.util.regex.Pattern;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.objects.KnowWETermMarker;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

/**
 * @author Jochen
 * 
 *         Container for ConditionActionRule
 * 
 */
public class ConditionActionRule extends DefaultAbstractKnowWEObjectType implements KnowWETermMarker {

	public ConditionActionRule() {
		sectionFinder = new RegexSectionFinder(
				"(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN"
						+ "|\\s*\\z))\\p{Space}*",
				Pattern.DOTALL);

		this.setCustomRenderer(new EditSectionRenderer());
	}

	public ConditionActionRule(AbstractKnowWEObjectType action) {
		sectionFinder = new RegexSectionFinder(
				"(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN"
						+ "|\\s*\\z))",
				Pattern.DOTALL);

		this.setCustomRenderer(new EditSectionRenderer());
		this.addChildType(new ConditionActionRuleContent(action));
	}

}
