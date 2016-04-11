package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;

public class AbstractionTableMarkup extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;

	static {
		MARKUP = new DefaultMarkup("AbstractionTable");
		MARKUP.addContentType(new Table());
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public AbstractionTableMarkup() {
		super(MARKUP);
	}

}
