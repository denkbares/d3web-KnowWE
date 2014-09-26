package de.d3web.we.kdom.rules;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.03.2014
 */
public class RuleTokenType extends AbstractType {

	public RuleTokenType(String... tokens) {
		setSectionFinder(new RuleTokenFinder(tokens));
		this.setRenderer(StyleRenderer.KEYWORDS);
		addChildType(new Indent());
	}
}
