package de.d3web.we.kdom.constraint;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class SingleChildConstraint implements SectionFinderConstraint {
	
	
	private static SingleChildConstraint instance = null;
	
	public static SingleChildConstraint getInstance() {
		if (instance == null) {
			instance = new SingleChildConstraint();
			
		}

		return instance;
	}

	@Override
	public void filterCorrectResults(
			List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		
		
		if(!satisfiesConstraint(found, father, type)){
			found.clear();
		}
		else {
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			SectionFinderResult e = found.get(0);
			result.clear();
			result.add(e);
		}
	}

	@Override
	public boolean satisfiesConstraint(List<SectionFinderResult> found,
			Section father, KnowWEObjectType type) {
		List<Section<? extends KnowWEObjectType>> findChildrenOfType = father.findChildrenOfType(type.getClass());
		if(findChildrenOfType != null && findChildrenOfType.size() > 0) {
			return false;
		}
		
		
		return true;
	}

}
