package de.d3web.we.d3webModule;

import de.d3web.we.alignment.Alignment;
import de.d3web.we.alignment.AlignmentFilter;

public class TerminologyAlignmentLinkFilter implements AlignmentFilter {

	private static TerminologyAlignmentLinkFilter instance = new TerminologyAlignmentLinkFilter();

	private TerminologyAlignmentLinkFilter() {
		super();
	}
	
	public static TerminologyAlignmentLinkFilter getInstance() {
		return instance;
	}
	
	public boolean accepts(Alignment alignment) {
		Boolean value = alignment.getProperty("visible");
		return value == null || value;
	}

	
}
