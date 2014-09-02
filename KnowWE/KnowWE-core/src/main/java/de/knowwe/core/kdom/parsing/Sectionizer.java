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

import java.util.ArrayList;
import java.util.List;

import de.d3web.utils.Log;
import de.knowwe.core.kdom.ExclusiveType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * This singleton contains the algorithm which parses the KDOM. The algorithm searches occurrences
 * that match certain types.
 *
 * @author Jochen, Albrecht
 */
public class Sectionizer implements Parser {


	private final Type type;

	public Sectionizer(Type type) {
		this.type = type;
	}

	private static final List<SectionizerModule> sectionizerModules = new ArrayList<>();

	private static SectionizerModule defaultSectionizerModule = new DefaultSectionizerModule();

	public static void registerSectionizerModule(SectionizerModule sectionizerModule) {
		sectionizerModules.add(sectionizerModule);
	}

	public static void setDefaultSectionizerModule(SectionizerModule defSectionizerModule) {
		if (defSectionizerModule != null) {
			defaultSectionizerModule = defSectionizerModule;
		}
	}

	@Override
	public Section<?> parse(String text, Section<? extends Type> parent) {
		Section<?> section = Section.createSection(text, type, parent);

		// fetches the allowed children types of the local type
		ArrayList<Type> types = new ArrayList<Type>();
		if (type.getChildrenTypes() != null) {
			types.addAll(type.getChildrenTypes());
		}

		if (!types.isEmpty()) {
			splitToSections(section.getText(), section, types, 0);
		}

		return section;
	}

	public void splitToSections(String text, Section<?> father, ArrayList<Type> types, int posInTypes) {

		if (posInTypes > types.size()) return;

		Type type = posInTypes == types.size()
				? PlainText.getInstance()
				: types.get(posInTypes);

		posInTypes++;

		if (type == null) throw new NullPointerException("children type list may not contain null");

		if (!(type instanceof Sectionizable)) {
			splitToSections(text, father, types, posInTypes);
			return;
		}

		List<SectionFinderResult> results = null;

		SectionFinder finder = ((Sectionizable) type).getSectionFinder();
		if (finder != null) {
			try {
				results = finder.lookForSections(text, father, type);
			}
			catch (Exception e) {
				Log.severe("Unexpected error while sectionizing", e);
			}
		}

		int lastEnd = 0;
		boolean createdSection = false;
		if (results != null) {
			for (SectionFinderResult r : results) {
				if (r == null) {
					continue;
				}

				if (r.getStart() < lastEnd || r.getStart() > r.getEnd()
						|| r.getStart() < 0 || r.getEnd() > text.length()) {
					Log.warning("Invalid SectionFinderResults for the Type '"
							+ type.getName() + "'. Results: " + results
							+ ". Result " + r
							+ " will be skipped.");
					continue;
				}

				if (lastEnd < r.getStart()) {
					splitToSections(text.substring(lastEnd, r.getStart()), father, types,
							type instanceof ExclusiveType
									? types.size()
									: posInTypes);
				}

				Section<?> child = null;
				String sectionText = text.substring(r.getStart(), r.getEnd());
				for (SectionizerModule sModule : sectionizerModules) {
					child = sModule.createSection(sectionText, type, father, r);
					if (child != null) break;
				}
				if (child == null) {
					defaultSectionizerModule.createSection(sectionText, type, father, r);
				}
				createdSection = true;
				lastEnd = r.getEnd();
			}
		}
		if (lastEnd < text.length()) {
			splitToSections(text.substring(lastEnd, text.length()), father,
					types, type instanceof ExclusiveType && createdSection
					? types.size()
					: posInTypes);
		}
	}

}
