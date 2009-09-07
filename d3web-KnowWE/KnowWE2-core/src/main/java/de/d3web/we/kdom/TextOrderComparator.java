package de.d3web.we.kdom;

import java.util.Comparator;

public class TextOrderComparator implements Comparator<Section> {

	@Override
	public int compare(Section arg0, Section arg1) {
		if(arg0.getOffSetFromFatherText() > arg1.getOffSetFromFatherText()) return 1;
		if(arg0.getOffSetFromFatherText() < arg1.getOffSetFromFatherText()) return -1;
		return 0;
		
		
	}
	
}
