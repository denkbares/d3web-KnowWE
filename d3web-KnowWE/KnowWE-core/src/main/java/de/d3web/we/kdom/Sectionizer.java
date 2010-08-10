/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.PairOfInts;

/**
 * @author Jochen, Albrecht
 * 
 *         This singleton contains the algorithm which parses the KDOM. The
 *         algorithm searches occurrences that match certain types.
 * @param <T>
 * @see splitToSections
 * 
 */
public class Sectionizer {

	/**
	 * Singleton instance
	 */
	private static Sectionizer instance;

	/**
	 * Singleton lazy factory
	 * 
	 * @return
	 */
	public static synchronized Sectionizer getInstance() {
		if (instance == null)
			instance = new Sectionizer();
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private final List<SectionizerModule> sectionizerModules = new ArrayList<SectionizerModule>();

	private SectionizerModule defaultSectionizerModule = new DefaultSectionizerModule();

	public void registerSectionizerModule(SectionizerModule sectionizerModule) {
		sectionizerModules.add(sectionizerModule);
	}

	public void setDefaultSectionizerModule(SectionizerModule defSectionizerModule) {
		if (defSectionizerModule != null) this.defaultSectionizerModule = defSectionizerModule;
	}

	// protected TreeMap<Priority, List<SectionizerModule>> sModMap = new
	// TreeMap<Priority, List<SectionizerModule>>();
	//
	//
	// public void addSectionizerModule(SectionizerModule sModule) {
	// List<SectionizerModule> sModList = sModMap.get(Priority.DEFAULT);
	// if (sModList == null) {
	// sModList = new ArrayList<SectionizerModule>();
	// sModMap.put(Priority.DEFAULT, sModList);
	// }
	// sModList.add(sModule);
	// }
	//
	// public void addSectionizerModule(Priority p, SectionizerModule sModule) {
	// List<SectionizerModule> sModList = sModMap.get(p);
	// if (sModList == null) {
	// sModList = new ArrayList<SectionizerModule>();
	// sModMap.put(p, sModList);
	// }
	// else {
	// if (p == Priority.HIGHEST) {
	// throw new IllegalArgumentException(
	// "There can only be one highest-priority "
	// + SectionizerModule.class.getSimpleName());
	// }
	// if (p == Priority.LOWEST) {
	// throw new IllegalArgumentException(
	// "There can only be one lowest-priority "
	// + SectionizerModule.class.getSimpleName());
	// }
	// }
	// sModList.add(sModule);
	// }

	/**
	 * DO NEVER TOUCH THIS ALGORITHM because this works
	 * 
	 * Working for types as priority in list: -searches type occurences in text
	 * -creates according section nodes which parse itself recursively
	 * -unallocated text parts are temporarily store as UndefinedSection which
	 * can be allocated by later types of the type-list
	 * 
	 * When list is done, UndefinedSections are made PlainText-nodes
	 * 
	 * @param text
	 *            The text to be searched for type occurrences
	 * @param allowedTypes
	 *            The types that are searched for, priority ordered
	 * @param father
	 *            Father-node section
	 * @param topic
	 *            topic name
	 * @param mgn
	 *            Knowledgebase
	 * @param report
	 *            ParseReport
	 * @param idgen
	 *            IDGenerator to generate unique IDs for new nodes
	 */
	@SuppressWarnings("unchecked")
	public void splitToSections(String text,
			List<KnowWEObjectType> allowedTypes, Section<?> father,
			KnowWEArticle article) {

		// initializes the list with just one single section
		ArrayList<Section<?>> sectionList = new ArrayList<Section<?>>();
		sectionList.add(new UndefinedSection(text, 0, article));

		// SEARCH ALL SPECIAL SECTIONS IN DEFINED ORDER
		for (KnowWEObjectType ob : allowedTypes) {

			// for the case that somehow null came into childrentypes-list
			if (ob == null) {
				continue;
			}

			ISectionFinder finder = ob.getSectioner();
			if (finder == null) {
				continue;
			}

			// thisSection is used as "pseudo-Index" to iterate over the, while
			// its modified
			// (avoid concurrentModificationException)
			Section<?> thisSection = sectionList.get(0);
			while (thisSection != null) {

				// the position of the current section in sectionList
				int index = sectionList.indexOf(thisSection);

				// Set the next Section:
				Section<?> nextSection;
				// last element reached
				if (index == sectionList.size() - 1) nextSection = null;
				// currentSection is not last element in sectionList
				else nextSection = sectionList.get(index + 1);

				// get the text from the current Section
				String secText = thisSection.getOriginalText();

				// skip sections already taken by a sectionFinder (they are not
				// undefined anymore)
				if (!(thisSection instanceof UndefinedSection)) {
					thisSection = nextSection;
					continue;
				}

				if (hasExclusiveSon(father, thisSection)) {
					thisSection = nextSection;
					continue;
				}

				// find the sub-sections
				List<SectionFinderResult> results = finder.lookForSections(secText,
						father, ob);

				if (results == null) {
					thisSection = nextSection;
					continue;
				}

				Collections.sort(results);
				validateNonOverlaps(results, secText, ob);


				List<Section<?>> findings = new ArrayList<Section<?>>();
				for (SectionFinderResult result : results) {
					// here the actual Section objects will be 'created' possibly using incremental parse-moduls
					Section s = createSection(article, ob, father, thisSection, secText, result);
					if (s != null) {
						s.startPosFromTmp = new PairOfInts(result.getStart(),
								result.getEnd());
						findings.add(s);
					}
				}

				if (findings.size() > 1) {
					Collections.sort(findings);
				}
				// this sectionFinder has found something!
				if (!findings.isEmpty()) {
					List<Section<?>> newSections = new ArrayList<Section<?>>();
					Section<?> firstFinding = findings.get(0);
					if (findings.size() == 1 && firstFinding.isExpanded()) {
						// the generated section is already expanded and is just
						// hooked into the tree
						// long start = System.currentTimeMillis();
						// Validator.getConsoleInstance().validateSubTree(father);
						// System.out.println("###" +
						// (System.currentTimeMillis() - start));
						newSections.add(firstFinding);
					}
					else {
						PairOfInts positionOfFirstFinding = firstFinding
								.startPosFromTmp;
						Section<?> start = new UndefinedSection(secText.substring(
								0, positionOfFirstFinding.getFirst()), thisSection
										.getOffSetFromFatherText(), article);
						if (start.getOriginalText().length() > 0)
							newSections.add(start);
						for (int i = 0; i < findings.size(); i++) {
							PairOfInts position = findings.get(i).startPosFromTmp;
							Section<?> nextNewSection = findings.get(i);
							if (nextNewSection.getOriginalText().length() > 0)
								newSections.add(nextNewSection);
							Section<?> afterSection;

							// there will be another section => make a new
							// undefined
							// section until this next section starts
							int second = position.getSecond();
							if (i + 1 < findings.size()) {
								int first = findings.get(i + 1).startPosFromTmp
										.getFirst();
								afterSection = new UndefinedSection(secText
										.substring(second, first),
										thisSection.getOffSetFromFatherText()
												+ second, article);
								// there will be no more new section => make the
								// rest of this section undefined
							}
							else {
								afterSection = new UndefinedSection(secText
										.substring(second, secText.length()),
										thisSection.getOffSetFromFatherText()
												+ second, article);
							}
							if (afterSection.getOriginalText().length() > 0)
								newSections.add(afterSection);
						}
					}
					sectionList.remove(thisSection);
					sectionList.addAll(index, newSections);
				}
				thisSection = nextSection;

			}

		}
		associateAllUndefinedSectionsToPlaintextOfFather(sectionList,
				father, article);
	}

	private Section<?> createSection(KnowWEArticle article,
			KnowWEObjectType ob,
			Section<?> father,
			Section<?> thisSection,
			String secText,
			SectionFinderResult result) {

		Section<?> s = null;
		if (result != null) {


			for (SectionizerModule sModule : sectionizerModules) {
				s = sModule.createSection(article, ob, father, thisSection, secText, result);
				if (s != null) return s;
			}
			s = defaultSectionizerModule.createSection(article, ob, father, thisSection, secText,
					result);

		}
		return s;
	}

	private boolean hasExclusiveSon(Section<?> father, Section<?> thisSection) {
		int offset = thisSection.getOffSetFromFatherText();

		// check if left brother is exclusive
		Section<?> leftBrother = father.getChildSectionAtPosition(offset - 1);
		if (leftBrother != null) {
			if (leftBrother.get() instanceof de.d3web.we.kdom.sectionFinder.ExclusiveType) {
				return true;
			}
		}

		// and if right brother is exclusive
		Section<?> rightBrother = father.getChildSectionAtPosition(offset
				+ thisSection.getOriginalText().length());
		if (rightBrother != null) {
			if (rightBrother.get() instanceof de.d3web.we.kdom.sectionFinder.ExclusiveType) {
				return true;
			}
		}

		return false;
	}





	private void validateNonOverlaps(List<SectionFinderResult> results, String text, KnowWEObjectType type) {
		if (results == null)
			return;
		int lastValue = 0;
		int i = 0;
		int invalid = -1;
		for (SectionFinderResult result : results) {
			if (result.excludeFromValidating()) {
				continue;
			}
			int a = result.getStart();
			int b = result.getEnd();
			if (a < lastValue) {
				invalid = i;
				Logger.getLogger(type.getName()).severe(
						"INVALID SECTIONIZING in " + type.getName()
								+ ": start = " + a + ", lastEnd = " + lastValue
								+ ", Text: " + text);
			}
			if (b < a) {
				invalid = i;
				Logger.getLogger(type.getName()).severe(
						"INVALID SECTIONIZING in " + type.getName()
								+ ": start = " + a + ", end = " + b + ", Text: " + text);
			}
			if (b > text.length()) {
				invalid = i;
				Logger.getLogger(type.getName()).severe(
						"INVALID SECTIONIZING in " + type.getName()
								+ ": end = " + b + ", length = " + text.length()
								+ ", Text: " + text);
			}
			if (invalid != -1) {
				break;
			}
			lastValue = b;
			i++;
		}
		if (invalid != -1) {
			results.remove(invalid);
			validateNonOverlaps(results, text, type);
		}

	}

	private void associateAllUndefinedSectionsToPlaintextOfFather(
			ArrayList<Section<?>> sectionList, Section<?> father, KnowWEArticle article) {
		for (Section<?> section : sectionList) {
			if (section instanceof UndefinedSection) {
				Section.createTypedSection(section.getOriginalText(),
						PlainText.getInstance(),
						father, section.getOffSetFromFatherText(),
						article, null, false);
			}
		}

	}

}
