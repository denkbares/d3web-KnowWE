package de.d3web.kernel.psMethods.delegate;

import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.psMethods.PSMethodAdapter;

public class PSMethodDelegate extends PSMethodAdapter {
	private static PSMethodDelegate instance = new PSMethodDelegate();

	public static PSMethodDelegate getInstance() {
		return instance;
	}


	public void propagate(XPSCase theCase, NamedObject nob, Object[] newValue) {
		List<? extends KnowledgeSlice> slices = nob.getKnowledge(this.getClass());
		if(slices == null) return;
		for (KnowledgeSlice slice : slices) {
			((RuleComplex) slice).check(theCase);
		}
	}
}
