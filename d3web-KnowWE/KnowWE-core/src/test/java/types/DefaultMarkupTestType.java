package types;

import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;

public class DefaultMarkupTestType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestMarkup");
		MARKUP.addAnnotation("anno1", true);
		MARKUP.addAnnotation("anno2", true, "anno2value1", "anno2value2", "anno2value3");
		MARKUP.addAnnotation("anno3", false, "anno3value1", "anno3value2", "anno3value3");
		MARKUP.addAnnotation("anno4", false);
	}

	public DefaultMarkupTestType() {
		super(MARKUP);
	}
}
