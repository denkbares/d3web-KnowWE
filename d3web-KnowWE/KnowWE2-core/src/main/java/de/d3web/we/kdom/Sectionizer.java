package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.utils.PairOfInts;

/**
 * @author Jochen
 * 
 * This singleton contains the algorithm which parses the KDOM. The algorithm
 * searches occorrences that match certain types.
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
			String topic, KnowledgeRepresentationManager mgn, KnowWEDomParseReport report,
			IDGenerator idgen) {

		// initializes the list with just one single section
		SectionList sectionList = new SectionList();
		sectionList.add(new UndefinedSection(text, fatherSection, 0));

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
				List<Section> findings = finder.lookForSections(thisSection,
						fatherSection, mgn, report, idgen);
				
				clearNulls(findings);
				
				if (findings != null && findings.size() > 1) {
					Collections.sort(findings);
					validateFindings(findings, secText);
				}
				// this sectionFinder has found something!
				if (findings != null && !findings.isEmpty()) {
						
					List<Section> newSections = new ArrayList<Section>();
					Section firstFinding = findings.get(0);
					if (findings.size() == 1 && firstFinding.isExpanded()) {
						// the generated section is already expanded and is just hanged into the tree
						newSections.add(firstFinding);
						firstFinding.setFather(fatherSection);
						firstFinding.setOffSetFromFatherText(fatherSection.getOriginalText()
								.indexOf(firstFinding.getOriginalText()));
						fatherSection.addChild(firstFinding);
						validateExpandedFinding(fatherSection);
					} else {
						PairOfInts positionOfFirstFinding = firstFinding
								.getPosition();
						Section start = new UndefinedSection(secText.substring(
								0, positionOfFirstFinding.getFirst()),
								thisSection, thisSection
										.getOffSetFromFatherText());
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
										.substring(second, first), thisSection,
										thisSection.getOffSetFromFatherText()
												+ second);
								// there will be no more new section => make the
								// rest of this section undefined
							} else {
								afterSection = new UndefinedSection(secText
										.substring(second, secText.length()),
										thisSection, thisSection
												.getOffSetFromFatherText()
												+ second);
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
				fatherSection, idgen);
	}

	private void validateExpandedFinding(Section section) {
		if (section.getChildren() == null || section.getChildren().isEmpty()) {
			return;
		}
		StringBuilder text = new StringBuilder();
		// make sure father is set right
		for (Section child:section.getChildren()) {
			text.append(child.getOriginalText());
			child.setFather(section);
			validateExpandedFinding(child);
		}
		// correct offset
		if (section.getOriginalText().equals(text.toString())) {
			// fast and secure if possible
			int offset = 0;
			for (Section child:section.getChildren()) {
				child.setOffSetFromFatherText(offset);
				offset += child.getOriginalText().length();
			}
		} else {
			// not so fast and weak
			for (Section child:section.getChildren()) {
				if (child.getOffSetFromFatherText() < 0) {
					child.setOffSetFromFatherText(section.getOriginalText().indexOf(child.getOriginalText()));
				}
			}
		}
	}

	private void validateFindings(List<Section> findings, String text) {
		if (findings == null)
			return;
		int lastValue = 0;
		int i = 0;
		int invalid = -1;
		for (Section match : findings) {
			PairOfInts pairOfInts = match.getPosition();
			if(pairOfInts == null) {
				Logger.getLogger(match.getObjectType().getName()).severe(
						"INVALID SECTIONIZING: no beginning and end found");
				break;
			}
			int a = pairOfInts.getFirst();
			int b = pairOfInts.getSecond();
			if (a < lastValue) {
				invalid = i;
				Exception e = new InvalidSectionValuesException(match);
				Logger.getLogger(match.getObjectType().getName()).severe(
						"INVALID SECTIONIZING: Type: "
								+ match.getObjectType().getClass().getName()
								+ " :" + e.getMessage());
			}
			if (b < a) {
				invalid = i;
				Exception e = new InvalidSectionValuesException(match);
				Logger.getLogger(match.getObjectType().getName()).severe(
						"INVALID SECTIONIZING: Type: "
								+ match.getObjectType().getClass().getName()
								+ " :" + e.getMessage());
			}
			if (b > text.length()) {
				invalid = i;
				Exception e = new InvalidSectionValuesException(match);
				Logger.getLogger(match.getObjectType().getName()).severe(
						"INVALID SECTIONIZING: Type: "
								+ match.getObjectType().getClass().getName()
								+ " :" + e.getMessage());
			}
			lastValue = b;
			i++;
		}
		if (invalid != -1) {
			Section s = findings.remove(invalid);
			s.getFather().removeChild(s);

			validateFindings(findings, text);
		}

	}

	private void clearNulls(List<Section> findings) {
		if (findings == null)
			return;
		int k = -1;
		for (int i = 0; i < findings.size(); i++) {
			if (findings.get(i) == null) {
				k = i;
				break;
			}
		}
		if (k != -1) {
			findings.remove(k);
			clearNulls(findings);
		}

	}

	private void associateAllUndefinedSectionsToPlaintextOfFather(
			SectionList sectionList, Section fatherSection, IDGenerator idgen) {
		for (Section section : sectionList) {
			if (section instanceof UndefinedSection) {
				new Section(section.getOriginalText(), PlainText.getInstance(),
						fatherSection, section.getOffSetFromFatherText(),
						fatherSection.getTopic(), null, null, idgen);
			}
		}

	}

}
