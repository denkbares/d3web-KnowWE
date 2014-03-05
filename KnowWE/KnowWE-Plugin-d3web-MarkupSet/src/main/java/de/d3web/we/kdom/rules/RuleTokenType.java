package de.d3web.we.kdom.rules;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Created by Albrecht Striffler (denkbares GmbH) on 03.03.14.
 */
public class RuleTokenType extends AbstractType {

	public RuleTokenType(String... tokens) {
		setSectionFinder(new RuleTokenFinder(tokens));
		this.setRenderer(StyleRenderer.KEYWORDS);
		addChildType(new Indent());
	}
}
