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

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author Jochen, Albrecht
 * 
 *         This singleton contains the algorithm which parses the KDOM. The
 *         algorithm searches occurrences that match certain types.
 * @see splitToSections
 * 
 */
public class Sectionizer implements Parser {

	private Map<String, String> parameterMap = null;

	public void addParameterMap(Map<String, String> map) {
		this.parameterMap = map;
	}

	private final Type type;

	public Sectionizer(Type type) {
		this.type = type;
	}

	private static final List<SectionizerModule> sectionizerModules = new ArrayList<SectionizerModule>();

	private static SectionizerModule defaultSectionizerModule = new DefaultSectionizerModule();

	public static void registerSectionizerModule(SectionizerModule sectionizerModule) {
		sectionizerModules.add(sectionizerModule);
	}

	public static void setDefaultSectionizerModule(SectionizerModule defSectionizerModule) {
		if (defSectionizerModule != null) defaultSectionizerModule = defSectionizerModule;
	}

	@Override
	public Section<?> parse(String text, Section<? extends Type> father) {
		Section<?> section = Section.createSection(text, type, father);
		
		// small hack, should be removed soon...
		if (parameterMap != null) {
			KnowWEUtils.storeObject(null, section, SectionFinderResult.ATTRIBUTE_MAP_STORE_KEY,
					parameterMap);
		}

		// fetches the allowed children types of the local type
		ArrayList<Type> types = new ArrayList<Type>();
		if (type.getAllowedChildrenTypes() != null) {
			types.addAll(type.getAllowedChildrenTypes());
		}

		// adding the registered global types to the children-list
		if (KnowWEEnvironment.GLOBAL_TYPES_ENABLED
				&& !(type instanceof TerminalType)
				&& type.allowesGlobalTypes()) {
			types.addAll(KnowWEEnvironment.getInstance().getGlobalTypes());
		}
		
		if (!types.isEmpty()) {
			splitToSections(section.getText(), section, types, 0);
		}
		
		return section;
	}

	public void splitToSections(String text, Section<?> father, ArrayList<Type> types, int posInTypes) {

		if (posInTypes > types.size()) return;

		Type type = posInTypes == types.size() ? PlainText.getInstance() : types.get(posInTypes);

		posInTypes++;

		if (type == null || !(type instanceof Sectionizable)) return;

		SectionFinder finder = ((Sectionizable) type).getSectioFinder();
		if (finder == null) return;

		List<SectionFinderResult> results = finder.lookForSections(text,
				father, type);

		int lastEnd = 0;
		boolean createdSection = false;
		if (results != null) {
			for (SectionFinderResult r : results) {
				if (r == null) {
					continue;
				}

				if (r.getStart() < lastEnd || r.getStart() > r.getEnd()) {
					Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
							"Invalid SectionFinderResults for the Type '"
									+ type.getName() + "'. Results: " + results + ". Result " + r
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
					child = defaultSectionizerModule.createSection(sectionText, type,
							father, r);
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
