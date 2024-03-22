package de.knowwe.core.kdom.parsing;

import java.util.List;
import java.util.Map;

import de.knowwe.core.user.UserContext;

/**
 * Utility class for
 * {@link Sections#replace(UserContext, Map)}
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.12.2011
 */
public class SectionInfo implements Comparable<SectionInfo> {

	boolean sectionExists = false;
	String oldId = null;
	String oldText = null;
	String newText = null;
	String title = null;
	String web = null;
	int offSet = -1;
	List<Integer> positionInKDOM = null;

	@Override
	public int compareTo(SectionInfo si) {
		if (sectionExists && !si.sectionExists) return -1;
		if (!sectionExists && si.sectionExists) return 1;
		if (!sectionExists && !si.sectionExists) return 0;
		return Integer.compare(offSet, si.offSet);
	}
}
