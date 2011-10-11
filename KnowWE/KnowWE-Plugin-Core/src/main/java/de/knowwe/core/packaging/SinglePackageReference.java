package de.knowwe.core.packaging;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

public class SinglePackageReference extends AbstractType {

	public SinglePackageReference() {
		this.sectionFinder = new RegexSectionFinder("[\\w-_]+");
		this.setCustomRenderer(new SinglePackageReferenceRenderer());
	}
}

