package de.knowwe.core.packaging;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

public class SinglePackageReference extends DefaultAbstractKnowWEObjectType {

	public SinglePackageReference() {
		this.sectionFinder = new RegexSectionFinder("[\\w-_]+");
		this.setCustomRenderer(new SinglePackageReferenceRenderer());
	}
}

