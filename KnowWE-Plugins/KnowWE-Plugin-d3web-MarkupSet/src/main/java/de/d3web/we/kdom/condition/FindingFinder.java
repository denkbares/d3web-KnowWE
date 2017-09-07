/*
 * Copyright (C) 2017 denkbares GmbH, Germany
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

package de.d3web.we.kdom.condition;

import java.util.List;

import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class FindingFinder implements SectionFinder {

	private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		if (Strings.containsUnquoted(text, "=")) {

			// if the value is a number this is not taken as a Finding (but left
			// for NumericalFinding)
			List<StringFragment> list = Strings.splitUnquoted(text, "=");
			// Hotfix for AOB when there is nothing behind the "="
			if (list.size() < 2) return null;
			StringFragment answer = list.get(1);
			boolean isNumber = false;
			try {
				//noinspection ResultOfMethodCallIgnored
				Double.parseDouble(answer.getContent().trim());
				isNumber = true;
			}
			catch (NumberFormatException ignored) {
			}
			// return it if answer is NOT a number
			if (!isNumber) {
				return textFinder.lookForSections(text, father, type);
			}
		}
		return null;
	}

}