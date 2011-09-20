package de.knowwe.d3web.initValue;

import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;

public class InitValueType extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("InitValue");
		m.addContentType(new InitValueDeclarationType());
		m.addAnnotation(KnowWEPackageManager.ATTRIBUTE_NAME, false);
	}

	public InitValueType() {
		super(m);
	}

}
