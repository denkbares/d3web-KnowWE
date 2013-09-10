package de.d3web.we.kdom.abstractiontable;

import de.knowwe.core.compile.packaging.PackageAnnotationNameType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageTermReference;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;

public class AbstractionTableMarkup extends DefaultMarkupType {

	private static DefaultMarkup markup = null;

	static {
		markup = new DefaultMarkup("AbstractionTable");
		markup.addContentType(new Table());
		markup.addAnnotation(PackageManager.PACKAGE_ATTRIBUTE_NAME, false);
		markup.addAnnotationNameType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageAnnotationNameType());
		markup.addAnnotationContentType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageTermReference());
	}

	public AbstractionTableMarkup() {
		super(markup);
	}

}
