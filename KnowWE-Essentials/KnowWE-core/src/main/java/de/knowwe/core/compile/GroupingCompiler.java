/*
 * Copyright (C) 2020 denkbares GmbH. All rights reserved.
 *
 */

package de.knowwe.core.compile;

import java.util.Collection;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * A compiler that consists of several child compilers
 *
 * @author Tobias Schmee (denkbares GmbH)
 * @created 11.11.20
 */
public interface GroupingCompiler {

	/**
	 * gets all child compilers for a given section
	 *
	 * @param section for which compilers are returned
	 * @return collection containing all child package compilers for this section
	 */
	Collection<PackageCompiler> getChildCompilers(Section<? extends DefaultMarkupType> section);
}
