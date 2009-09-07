package de.d3web.we.kdom;

import java.util.List;


/**
 * @author Jochen
 *
 */
public abstract class SectionFinder {
	
	protected KnowWEObjectType type;
	
	public SectionFinder(KnowWEObjectType type) {
		this.type = type;
	}
	
	
	
	public KnowWEObjectType getType() {
		return type;
	}



	/**
	 * 
	 * Allocates text parts for this module. The resulting array contains substrings of the 
	 * passed text. These specified substrings will be allocated to this module. 
	 * Method will be called multiple times with various article fragments depending on 
	 * previous allocations of preceding modules.
	 * If no interesting section is found in a passed fragment, return 'null' or an array of length 0; 
	 * 
	 * @param text Text fragment of the wiki article source
	 * @return Array of substrings of text that are to be processed by this module (allocation)
	 */
	public abstract List<Section> lookForSections(Section tmpSection, Section father, de.d3web.we.knowRep.KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg);
}
