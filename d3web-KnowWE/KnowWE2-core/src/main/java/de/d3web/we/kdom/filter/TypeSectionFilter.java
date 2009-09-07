package de.d3web.we.kdom.filter;

import de.d3web.we.kdom.Section;

/**
 * a section filter for types
 * @author Fabian Haupt
 *
 */
public class TypeSectionFilter implements SectionFilter {
	private String myType;
	
	public TypeSectionFilter(String type){
	    myType=type;	    
	}
	
	
	@Override
	public boolean accept(Section section) {
		boolean erg=section.getObjectType().getName().equalsIgnoreCase(myType);		
		return erg;
	}
	

}
