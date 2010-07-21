package de.d3web.we.kdom.constraint;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;


/**
 * @author Jochen
 * 
 * this constraint is only used a marker
 *
 */
public class ExclusiveType implements SectionFinderConstraint{
	
	private static ExclusiveType instance = null;
	
	public static ExclusiveType getInstance() {
		if (instance == null) {
			instance = new ExclusiveType();
			
		}

		return instance;
	}
	
	private ExclusiveType() {
		
	}

	@Override
	public void filterCorrectResults(List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		// this constraint is only used a marker
		
	}

	@Override
	public boolean satisfiesConstraint(List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		// this constraint is only used a marker
		return false;
	}

}
