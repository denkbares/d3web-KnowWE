package de.d3web.we.terminology.global;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TerminologyHandler;

public abstract class GlobalTerminologyHandler extends TerminologyHandler<GlobalTerminology, Term> {

	protected List<Term> fifo(GlobalTerminology gt) {
		/*
		List<Term> queue = new LinkedList<Term>();
		for (Term term : gt.getRoots()) {
			if(checkFilter(term)) {
				queue.add(term);;
			}
		}
		queue = fifo(queue);
		for (Term term : gt.getUnstructuredTerms()) {
			if(checkFilter(term)) {
				queue.add(term);;
			}
		}*/
		return new ArrayList<Term>(gt.getAllTerms());
	}
	/*
	private List<Term> fifo(List<? extends Term> toExpand) {
		List<Term> result = new LinkedList<Term>();
		for (Term term : toExpand) {
			if(checkFilter(term)) {
				result.add(term);
			}
			result.addAll(fifo(new ArrayList<Term>(term.getChildren())));
		}
		return result;
	}
	
	*/
	
}
