/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */
package de.knowwe.core.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * 
 * @author Jochen
 * @created 29.07.2010
 */
public abstract class ConditionalSectionFinder implements SectionFinder {

	private SectionFinder finder = null;

	public ConditionalSectionFinder(SectionFinder internalFinder) {
		this.finder = internalFinder;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		List<SectionFinderResult> result = new ArrayList<>();
		if (!text.isEmpty()) {
			if (condition(text, father)) {
				return finder.lookForSections(text, father, type);
			}
		}
		return result;
	}

	protected abstract boolean condition(String text, Section<?> father);

}
