package de.d3web.we.basic;

import java.util.Comparator;

public class InformationNamespaceComparator implements Comparator<Information> {

	public int compare(Information o1, Information o2) {
		String ns1 = o1.getNamespace();
		String ns2 = o2.getNamespace();
		return ns1.compareTo(ns2);
	}

}
