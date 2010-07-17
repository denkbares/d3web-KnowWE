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

			SectionFinder finder = ob.getSectioner();
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

				List<Section<?>> findings = new ArrayList<Section<?>>();
				for (SectionFinderResult result:results) {
					if (result != null) {
						Section<?> s = null;

						// Update mechanism
						// try to get unchanged Sections from the last version of the article
						if (!article.isFullParse()
								&& !(result instanceof ExpandedSectionFinderResult)
								&& !ob.isNotRecyclable()
								&& !(ob.isLeafType() && !(ob instanceof Include))) {

							// get path of of ObjectTypes the Section to be created would have
							List<Class<? extends KnowWEObjectType>> path = father.getPathFromArticleToThis();
							path.add(ob.getClass());

							// find all Sections with same path of ObjectTypes in the last version
							Map<String, List<Section<?>>> sectionsOfSameType
									= article.getLastVersionOfArticle()
										.findChildrenOfTypeMap(path);

							List<Section<?>> matches = sectionsOfSameType.remove(secText
									.substring(result.getStart(), result.getEnd()));
							
							Section<?> match = null;
							if (matches != null && matches.size() == 1) {
								match = matches.get(0);
							}

							// don't reuse matches that are already reused elsewhere...
							// the same section object would be hooked in the KDOM twice
							// -> conflict with IDs an other stuff
							if (match != null
									&& (!match.isReusedBy(match.getTitle())
									&& !match.hasReusedSuccessor)
									&& !match.isDirty()) {

								// mark ancestors, that they have an reused successor
								Section<?> ancestor = match.getFather();
								while (ancestor != null) {
									ancestor.hasReusedSuccessor = true;
									ancestor = ancestor.getFather();
								}

								// store position in the list of children from
								// the last version of the KDOM to be able to
								// determine, if the position has changes in the
								// new version of the KDOM

								match.lastPositions = KnowWEUtils.getPositionInKDOM(match);

								// use match instead of creating a new Section
								// (thats the idea of updating ;) )
								s = match;
								s.setOffSetFromFatherText(thisSection.getOffSetFromFatherText()
										+ result.getStart());
								s.setFather(father);
								father.addChild(s);

								// perform necessary actions on complete reused KDOM subtree
								List<Section<?>> newNodes = new ArrayList<Section<?>>();
								s.getAllNodesPreOrder(newNodes);
								for (Section<?> node : newNodes) {

									List<Integer> lastPositions = KnowWEUtils.getPositionInKDOM(
											node, match);
									lastPositions.addAll(match.lastPositions);
									node.lastPositions = lastPositions;

									if (node.getObjectType() instanceof Include) {
										article.getIncludeSections().add((Section<Include>) node);
									}

									SectionStore lastStore = KnowWEEnvironment.getInstance()
											.getArticleManager(father.getWeb()).getTypeStore()
											.getLastStoredObjects(father.getTitle(), node.getID());

									// don't do the following if the node is included
									if (node.getTitle().equals(father.getTitle())) {
										// mark as reused (so its not reused again)
										node.setReusedBy(node.getTitle(), true);
										// update pointer to article
										node.article = article;

										// if the result comes with an ID, use it
										// for the found match
										// update the id for all other nodes
										if (result.getId() != null && node == s) {
											node.setID(result.getId().toString());
											node.setSpecificID(result.getId().getSpecificID());
										} else {
											if (node.getSpecificID() == null) {
												node.setID(new SectionID(node.father,
														node.objectType).toString());
											} else {
												node.setID(new SectionID(node.getArticle(),
														node.getSpecificID()).toString());
											}
										}
									}

									if (lastStore != null) {
										// reuse last section store
										KnowWEEnvironment.getInstance().getArticleManager(
												father.getWeb()).getTypeStore().putSectionStore(
														father.getTitle(), node.getID(), lastStore);
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
								KnowWEEnvironment.getInstance().getIncludeManager(s.getWeb()).registerInclude(
										(Section<Include>) s);

							} else {
								s = Section.createTypedSection(thisSection.getOriginalText().substring(result.getStart(),
										result.getEnd()), ob, father, thisSection.getOffSetFromFatherText()
										+ result.getStart(), article, result.getId(), false, null,ob);
							}

						}
						s.startPosFromTmp = new PairOfInts(result.getStart(), result.getEnd());
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
						// the generated section is already expanded and is just hooked into the tree
//						long start = System.currentTimeMillis();
//						Validator.getConsoleInstance().validateSubTree(father);
//						System.out.println("###" + (System.currentTimeMillis() - start));
						newSections.add(firstFinding);
					} else {
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

	private boolean hasExclusiveSon(Section<?> father, Section<?> thisSection) {
		int offset = thisSection.getOffSetFromFatherText();

		//check if left brother is exclusive
		Section<?> leftBrother = father.getChildSectionAtPosition(offset-1);
		if(leftBrother != null) {
			List<SectionFinderConstraint> constraints = leftBrother.get().getSectioner().getConstraints();
			if (constraints != null && constraints.contains(ExclusiveType.getInstance())) {
				return true;
			}
		}

		//and if right  brother is exclusive
		Section<?> rightBrother = father.getChildSectionAtPosition(offset+thisSection.getOriginalText().length());
		if(rightBrother != null) {
			List<SectionFinderConstraint> constraints = rightBrother.get().getSectioner().getConstraints();
			if (constraints != null && constraints.contains(ExclusiveType.getInstance())) {
				return true;
			}
		}

		return false;
	}

	private void validateConstraints(List<SectionFinderResult> results, Section<?> father,
			KnowWEObjectType ob) {
		List<SectionFinderConstraint> constraints = ob.getSectioner().getConstraints();

		if (constraints == null)
			return;

		for (SectionFinderConstraint sectionFinderConstraint : constraints) {
			if(!sectionFinderConstraint.satisfiesConstraint(results, father, ob)) {
				sectionFinderConstraint.filterCorrectResults(results, father, ob);
			}
		}

	}

	private Section<?> createExpandedSection(ExpandedSectionFinderResult result, Section<?> father) {
		Section<?> s = Section.createTypedSection(result.getText(), result.getObjectType(), father,
				result.getStart(), father.getArticle(), null, true, null, result.getObjectType());
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
			ArrayList<Section<?>> sectionList, Section<?> father, KnowWEArticle article) {
		for (Section<?> section : sectionList) {
			if (section instanceof UndefinedSection) {
				Section.createTypedSection(section.getOriginalText(), PlainText.getInstance(),
						father, section.getOffSetFromFatherText(),
						article, null, false, null,PlainText.getInstance());
			}
		}

	}

}
