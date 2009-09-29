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
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.PairOfInts;

/**
 * @author Jochen
 * 
 * This singleton contains the algorithm which parses the KDOM. The algorithm
 * searches occorrences that match the current type structure.
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

	/**
	 * DO NEVER TOUCH THIS ALGORITHM because this works
	 * 
	 * Working for types as priority in list: -searches type occorances in text
	 * -creates according section nodes which parse itself recursively
	 * -unallocated text parts are temporarely store as UndefinedSection which
	 * can be allocated by later types of the type-list
	 * 
	 * When list is done, UndefinedSections are made PlainText-nodes
	 * 
	 * @param text
	 *            The text to be searched for type occorrances
	 * @param allowedTypes
	 *            The types that are searched for, priority ordered
	 * @param fatherSection
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
	public void splitToSections(String text,
			List<KnowWEObjectType> allowedTypes, Section fatherSection,
			KnowWEArticle article) {

		// initializes the list with just one single section
		ArrayList<Section> sectionList = new ArrayList<Section>();
		sectionList.add(new UndefinedSection(text, 0, article));

		// SEARCH ALL SPECIAL SECTIONS IN DEFINED ORDER
		for (KnowWEObjectType ob : allowedTypes) {

			SectionFinder finder = ob.getSectioner();
			if (finder == null) {
//				Logger.getLogger(this.getClass().getName()).severe(
//						"SectionFinder is null: Type: "
//								+ ob.getClass().getName());
				continue;
			}

			// thisSection is used as "pseudo-Index" to iterate over the, while
			// its modified
			// (avoid concurrentModificationException)

			//if(sectionList.size() == 0) {
			//	break;
			//}
			Section thisSection = sectionList.get(0);

			while (thisSection != null) {

				// the position of the current section in sectionList
				int index = sectionList.indexOf(thisSection);

				// Set the next Section:
				Section nextSection;
				// last element reached
				if (index == sectionList.size() - 1)
					nextSection = null;
				// currentSection is not last element in sectionList
				else
					nextSection = sectionList.get(index + 1);

				// get the text from the current Section
				String secText = thisSection.getOriginalText();

				// skip sections already taken by a sectionFinder (they are not
				// undefined anymore)
				// if(!thisSection.getType().equals(Section.SECTION_TYPE_UNDEF))
				// {
				if (!(thisSection instanceof UndefinedSection)) {
					thisSection = nextSection;
					continue;
				}

				// find the sub-sections
				List<SectionFinderResult> results = finder.lookForSections(thisSection.getOriginalText(), fatherSection);
				
				if (results == null) {
					thisSection = nextSection;
					continue;
				}
				
				validateResults(results, secText, ob);
				
				List<Section> findings = new ArrayList<Section>();		
				for (SectionFinderResult result:results) {
					if (result != null) {
						if (result instanceof ExpandedSectionFinderResult) {
							findings.add(createExpandedSection((ExpandedSectionFinderResult) result, fatherSection));
						} else {
							findings.add(Section.createSection(ob, fatherSection, 
								thisSection, result.getStart(), result.getEnd(), article, result.getId()));
					
						}
					}
				}
				
				
				if (findings.size() > 1) {
					Collections.sort(findings);
				}
				// this sectionFinder has found something!
				if (!findings.isEmpty()) {	
					List<Section> newSections = new ArrayList<Section>();
					Section firstFinding = findings.get(0);
					if (findings.size() == 1 && firstFinding.isExpanded()) {
						// the generated section is already expanded and is just hanged into the tree
						newSections.add(firstFinding);
					} else {
						PairOfInts positionOfFirstFinding = firstFinding
								.getPosition();
						Section start = new UndefinedSection(secText.substring(
								0, positionOfFirstFinding.getFirst()), thisSection
										.getOffSetFromFatherText(), article);
						if (start.getOriginalText().length() > 0)
							newSections.add(start);
						for (int i = 0; i < findings.size(); i++) {
							PairOfInts position = findings.get(i).getPosition();
							Section nextNewSection = findings.get(i);
							if (nextNewSection.getOriginalText().length() > 0)
								newSections.add(nextNewSection);
							Section afterSection;

							// there will be another section => make a new
							// undefined
							// section until this next section starts
							int second = position.getSecond();
							if (i + 1 < findings.size()) {
								int first = findings.get(i + 1).getPosition()
										.getFirst();
								afterSection = new UndefinedSection(secText
										.substring(second, first),
										thisSection.getOffSetFromFatherText()
												+ second, article);
								// there will be no more new section => make the
								// rest of this section undefined
							} else {
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
				fatherSection, article);
	}

	private Section createExpandedSection(ExpandedSectionFinderResult result, Section father) {
		Section s = Section.createExpandedSection(result.getText(), result.getObjectType(), father,
				result.getStart(), father.getArticle());
		if (s.getOffSetFromFatherText() < 0 || s.getOffSetFromFatherText() > father.getOriginalText().length() 
				|| !father.getOriginalText().substring(s.getOffSetFromFatherText()).startsWith(s.getOriginalText())) {
			s.setOffSetFromFatherText(father.getOriginalText().indexOf(s.getOriginalText()));
		}
		
		for (ExpandedSectionFinderResult childResult:result.getChildren()) {
			createExpandedSection(childResult, s);
		}
		return s;
	}

	private void validateResults(List<SectionFinderResult> results, String text, KnowWEObjectType type) {
		if (results == null)
			return;
		int lastValue = 0;
		int i = 0;
		int invalid = -1;
		for (SectionFinderResult result : results) {
			if (result instanceof ExpandedSectionFinderResult) {
				continue;
			}
			int a = result.getStart();
			int b = result.getEnd();
			if (a < lastValue) {
				invalid = i;
				Logger.getLogger(type.getName()).severe(
						"INVALID SECTIONIZING in " + type.getName() 
						+ ": start = " + a + ", lastEnd = " + lastValue + ", Text: " + text);
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
						+ ": end = " + b + ", length = " + text.length() + ", Text: " + text);
			}
			if (invalid != -1) {
				break;
			}
			lastValue = b;
			i++;
		}
		if (invalid != -1) {
			results.remove(invalid);
			validateResults(results, text, type);
		}

	}

	private void associateAllUndefinedSectionsToPlaintextOfFather(
			ArrayList<Section> sectionList, Section fatherSection, KnowWEArticle article) {
		for (Section section : sectionList) {
			if (section instanceof UndefinedSection) {
				new Section(section.getOriginalText(), PlainText.getInstance(),
						fatherSection, section.getOffSetFromFatherText(),
						article, new SectionID(article.getIDGen()), false);
			}
		}

	}

}
