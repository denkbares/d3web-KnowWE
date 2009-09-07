package de.d3web.we.utils;

import java.util.Comparator;

import de.d3web.we.kdom.KnowWEObjectType;

public class KnowWEObjectTypeComparator implements Comparator<KnowWEObjectType> {

	@Override
	public int compare(KnowWEObjectType o1, KnowWEObjectType o2) {
		int i = o1.getName().compareTo(o2.getName());
		
		if (i < 0) {
			return -1;
		}
		
		if (i > 0) {
			return 1;
		}
		
		return 0;
	}

}
