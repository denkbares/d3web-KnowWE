/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.core.kdom.basicType;

import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A type that matches the whole text if it only consists of white-spaces and
 * nothing else. It does not match a text partially!
 * 
 * @author Markus Friedrich, Volker Belli (denkbares GmbH)
 * @created 19.03.2012
 */
public class EmptyType extends AbstractType {

	public EmptyType() {
		this.setSectionFinder(new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				if (isEmpty(text)) {
					return SectionFinderResult.singleItemList(0, text.length());
				}
				return null;
			}
		});
	}

	private static boolean isEmpty(String text) {
		// check if the result only consists of white-spaces
		// or non-break-spaces
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (!Character.isWhitespace(c) && c != 160) return false;
		}
		return true;
	}

}
