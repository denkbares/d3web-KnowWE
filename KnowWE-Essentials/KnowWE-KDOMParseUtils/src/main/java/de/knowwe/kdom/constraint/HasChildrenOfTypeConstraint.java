/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.kdom.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Constraint that only allows to sectionize if there are no other children of
 * the specified type(s) yet.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 12.02.2014
 */
public class HasChildrenOfTypeConstraint implements SectionFinderConstraint {

	private final List<Class<? extends Type>> types =
			new ArrayList<Class<? extends Type>>(1);

	public HasChildrenOfTypeConstraint(Class<? extends Type> type) {
		this.types.add(type);
	}

	public HasChildrenOfTypeConstraint(Class<? extends Type>... type) {
		Collections.addAll(this.types, type);
	}

	@Override
	public <T extends Type> void filterCorrectResults(
			List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		if (!satisfiesConstraint(found, father, type, text)) {
			found.clear();
		}
	}

	private <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		for (Class<?> clazz : types) {
			for (Section<?> child : father.getChildren()) {
				if (clazz.isInstance(child.get())) return true;
			}
		}
		return false;
	}

}
