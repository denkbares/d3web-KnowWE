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
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.constraint.ExclusiveType;
import de.d3web.we.kdom.constraint.SectionFinderConstraint;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.include.IncludeSectionFinderResult;
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.store.SectionStore;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.PairOfInts;

/**
 * @author Jochen (some changes by astriffler)
 *
 * This singleton contains the algorithm which parses the KDOM. The algorithm
 * searches occorrences that match certain types.
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
	public void splitToSections(String text,
			List<KnowWEObjectType> allowedTypes, Section father,
			KnowWEArticle article) {

		// initializes the list with just one single section
		ArrayList<Section> sectionList = new ArrayList<Section>();
		sectionList.add(new UndefinedSection(text, 0, article));

		// SEARCH ALL SPECIAL SECTIONS IN DEFINED ORDER
		for (KnowWEObjectType ob : allowedTypes) {

			SectionFinder finder = ob.getSectioner();
			if (finder == null) {
				continue;
			}

			// thisSection is used as "pseudo-Index" to iterate over the, while
			// its modified
			// (avoid concurrentModificationException)
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
				if (!(thisSection instanceof UndefinedSection)) {
					thisSection = nextSection;
					continue;
				}

				if(hasExclusiveSon(father, thisSection)) {
					thisSection = nextSection;
					continue;
				}


				// find the sub-sections
				List<SectionFinderResult> results = finder.lookForSections(secText,
						father);

				if (results == null) {
					thisSection = nextSection;
					continue;
				}

				Collections.sort(results);
				validateNonOverlaps(results, secText, ob);
				validateConstraints(results, father, ob);

				List<Section> findings = new ArrayList<Section>();
				for (SectionFinderResult result:results) {
					if (result != null) {
						Section s = null;

						// Update mechanism
						// try to get unchanged Sections from old article
						if (article.getLastVersionOfArticle() != null
								&& !article.isFullParse()
								&& !(result instanceof ExpandedSectionFinderResult)
								&& !ob.isNotRecyclable()
								&& !(ob.isLeafType() && !(ob instanceof Include))) {

							List<Class<? extends KnowWEObjectType>> path = father.getPathFromArticleToThis();
							path.add(ob.getClass());
							Map<String, Section> sectionsOfSameType = article.getLastVersionOfArticle()
									.findChildrenOfTypeMap(path);

							Section match = sectionsOfSameType.remove(secText
									.substring(result.getStart(), result.getEnd()));

							if (match != null && (!match.isReusedBy(match.getTitle()) && !match.hasReusedSuccessor)) {

								match.setReusedBy(match.getTitle(), true);

								Section ancestor = match.getFather();
								while (ancestor != null) {
									ancestor.hasReusedSuccessor = true;
									ancestor = ancestor.getFather();
								}

								s = match;
								s.setOffSetFromFatherText(thisSection.getOffSetFromFatherText()
										+ result.getStart());
								s.setFather(father);
								father.addChild(s);

								//System.out.println("Used old " + s.getObjectType().getName());
								List<Section> newNodes = new ArrayList<Section>();
								s.getAllNodesPreOrder(newNodes);
								for (Section node:newNodes) {

									if (node.getObjectType() instanceof Include) {
										article.getIncludeSections().add(node);
									}

									SectionStore oldStore = KnowWEUtils.getLastSectionStore(node.getWeb(), father.getTitle(), node.id);

									if (node.getTitle().equals(father.getTitle())) {
										node.setReusedBy(node.getTitle(), true);
										node.article = article;

										if (!(node.preAssignedID && node == s)) {
											if (node.specificID == null) {
												node.id = new SectionID(node.father, node.objectType).toString();
											} else {
												node.id = new SectionID(node.getArticle(), node.specificID).toString();
											}
										}
									}

									//System.out.print(oldStore.getAllObjects().isEmpty() ? "" : "#" + node.getId() + " put " + oldStore.getAllObjects() + "\n");
									if (oldStore != null) {
										KnowWEUtils.putSectionStore(node.getWeb(), father.getTitle(), node.id, oldStore);
									}
								}
							}

						}
						if (s == null) {
							if (result instanceof ExpandedSectionFinderResult) {
								s = createExpandedSection((ExpandedSectionFinderResult) result, father);
							} else if (result instanceof IncludeSectionFinderResult) {
								s = Section.createTypedSection(thisSection.getOriginalText().substring(result.getStart(),
										result.getEnd()), ob, father, thisSection.getOffSetFromFatherText()
										+ result.getStart(), article, result.getId(), false,
										((IncludeSectionFinderResult) result).getIncludeAddress(),ob);
								KnowWEEnvironment.getInstance().getIncludeManager(s.getWeb()).registerInclude(s);
							} else {
								s = Section.createTypedSection(thisSection.getOriginalText().substring(result.getStart(),
										result.getEnd()), ob, father, thisSection.getOffSetFromFatherText()
										+ result.getStart(), article, result.getId(), false, null,ob);
							}

						}
						s.setPosition(new PairOfInts(result.getStart(), result.getEnd()));
						findings.add(s);
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
//						long start = System.currentTimeMillis();
//						Validator.getConsoleInstance().validateSubTree(father);
//						System.out.println("###" + (System.currentTimeMillis() - start));
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
				father, article);
	}

	private boolean hasExclusiveSon(Section father, Section thisSection) {
		int offset = thisSection.getOffSetFromFatherText();

		//check if left brother is exclusive
		Section<?> leftBrother = father.getChildSectionAtPosition(offset-1);
		if(leftBrother != null && leftBrother.get().getSectioner().getConstraints().contains(ExclusiveType.getInstance())) {
			return true;
		}

		//and if right  brother is exclusive
		Section<?> rightBrother = father.getChildSectionAtPosition(offset+thisSection.getOriginalText().length());
		if(rightBrother != null && rightBrother.get().getSectioner().getConstraints().contains(ExclusiveType.getInstance())) {
			return true;
		}

		return false;
	}

	private void validateConstraints(List<SectionFinderResult> results, Section father,
			KnowWEObjectType ob) {
		List<SectionFinderConstraint> constraints = ob.getSectioner().getConstraints();
		for (SectionFinderConstraint sectionFinderConstraint : constraints) {
			if(!sectionFinderConstraint.satisfiesConstraint(results, father, ob)) {
				sectionFinderConstraint.filterCorrectResults(results, father, ob);
			}
		}

	}

	private Section createExpandedSection(ExpandedSectionFinderResult result, Section father) {
		Section s = Section.createTypedSection(result.getText(), result.getObjectType(), father, result.getStart(),
				father.getArticle(), null, true, null,result.getObjectType());
		if (s.getOffSetFromFatherText() < 0 || s.getOffSetFromFatherText() > father.getOriginalText().length()
				|| !father.getOriginalText().substring(s.getOffSetFromFatherText()).startsWith(s.getOriginalText())) {
			s.setOffSetFromFatherText(father.getOriginalText().indexOf(s.getOriginalText()));
		}

		for (ExpandedSectionFinderResult childResult:result.getChildren()) {
			createExpandedSection(childResult, s);
		}
		return s;
	}

	private void validateNonOverlaps(List<SectionFinderResult> results, String text, KnowWEObjectType type) {
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
			validateNonOverlaps(results, text, type);
		}

	}

	private void associateAllUndefinedSectionsToPlaintextOfFather(
			ArrayList<Section> sectionList, Section father, KnowWEArticle article) {
		for (Section section : sectionList) {
			if (section instanceof UndefinedSection) {
				Section.createTypedSection(section.getOriginalText(), PlainText.getInstance(),
						father, section.getOffSetFromFatherText(),
						article, null, false, null,PlainText.getInstance());
			}
		}

	}

}
