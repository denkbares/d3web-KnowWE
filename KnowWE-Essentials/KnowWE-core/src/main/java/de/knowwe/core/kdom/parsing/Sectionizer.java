/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.core.kdom.parsing;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.kdom.ExclusiveType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * This singleton contains the algorithm which parses the KDOM. The algorithm searches occurrences that match certain
 * types.
 *
 * @author Jochen, Albrecht
 */
public class Sectionizer implements Parser {
	private static final Logger LOGGER = LoggerFactory.getLogger(Sectionizer.class);

	private final Type type;

	public Sectionizer(Type type) {
		this.type = type;
	}

	@Override
	@NotNull
	public Section<?> parse(@NotNull String text, @Nullable Section<? extends Type> parent) {
		Section<?> section = Section.createSection(text, type, parent);

		if (parent != null) {
			// if we have a parent, set offsets right here, because it will be faster
			List<Section<? extends Type>> children = parent.getChildren();
			if (children.size() == 1) {
				section.setOffsetInParent(0);
			}
			else {
				Section<? extends Type> previousSibling = children.get(children.size() - 2);
				section.setOffsetInParent(previousSibling.getOffsetInParent() + previousSibling.getTextLength());
			}
		}

		// fetches the allowed children types of the local type
		List<Type> types = type.getChildrenTypes();
		if (!types.isEmpty()) {
			splitToSections(section, types);
		}
		if (section.children != null) section.children.trimToSize();
		return section;
	}

	protected void splitToSections(Section<?> parent, List<Type> types) {
		splitToSections(parent.getText(), parent, types, 0);
	}

	private void splitToSections(String text, Section<?> parent, List<Type> types, int posInTypes) {
		// use next child type, and PlainText as default last type
		if (posInTypes > types.size()) return;
		Type type = posInTypes == types.size() ? PlainText.getInstance() : types.get(posInTypes);
		posInTypes++;

		// check for valid sectionizable type
		if (type == null) throw new NullPointerException("children type list may not contain null");
		if (!(type instanceof Sectionizable)) {
			splitToSections(text, parent, types, posInTypes);
			return;
		}

		// the look for sections, using the section finder of the child types
		List<SectionFinderResult> results = null;
		SectionFinder finder = ((Sectionizable) type).getSectionFinder();
		if (finder != null) {
			try {
				results = finder.lookForSections(text, parent, type);
			}
			catch (Exception e) {
				LOGGER.error("Unexpected error while sectionizing", e);
			}
		}

		if (results == null) {
			// recursive type, child is repeat of the parent, so just ignore it
			results = Collections.emptyList();
		}

		int lastEnd = 0;
		boolean createdSection = false;
		for (SectionFinderResult result : results) {
			if (result == null) {
				continue;
			}

			if (result.getStart() < lastEnd || result.getStart() > result.getEnd()
					|| result.getStart() < 0 || result.getEnd() > text.length()) {
				LOGGER.warn("Invalid SectionFinderResults for the Type '"
						+ type.getName() + "' in parent section '" + parent.getText() + "' in article '"
						+ parent.getTitle() + "'. Results: " + results + ". Result " + result + " will be skipped.");
				continue;
			}

			if (lastEnd < result.getStart()) {
				int newPosInTypes = (type instanceof ExclusiveType) ? types.size() : posInTypes;
				splitToSections(text.substring(lastEnd, result.getStart()), parent, types, newPosInTypes);
			}

			SectionizerModule.Registry.createSection(parent, text, type, result);
			createdSection = true;
			lastEnd = result.getEnd();
		}
		if (lastEnd < text.length()) {
			int newPosInTypes = (createdSection && (type instanceof ExclusiveType)) ? types.size() : posInTypes;
			splitToSections(text.substring(lastEnd), parent, types, newPosInTypes);
		}
	}
}
