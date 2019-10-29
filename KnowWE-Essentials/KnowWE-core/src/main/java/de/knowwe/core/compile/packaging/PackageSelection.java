/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile.packaging;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

/**
 *
 * Container type allowing for normal package terms and wildcard package pattern
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.10.19.
 */
public class PackageSelection extends AbstractType {

	public PackageSelection() {
		this.setSectionFinder(new AllTextFinderTrimmed());

		// check for wild card patterns first
		this.addChildType(new PackagePattern());

		// the use normal package term references
		this.addChildType(new PackageTerm());
	}
}
